package org.cryptomator.hubcli;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.Masterkey;
import org.cryptomator.cryptolib.common.EncryptingWritableByteChannel;
import org.cryptomator.hubcli.util.JWEHelper;
import org.cryptomator.hubcli.util.KeyHelper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

@Command(name = "create-vault",//
        description = "Create a new vault")
class CreateVault implements Callable<Integer> {

    @CommandLine.Mixin
    Common common;

    @CommandLine.Mixin
    AccessToken accessToken;

    @Option(names = {"--name"}, required = true, description = "name of the vault")
    String name;
    @Option(names = {"--description"}, description = "description of the vault")
    String description;
    @Option(names = {"--path"}, required = true, description = "path where to create the vault (folder) ")
    Path path;

    @Override
    public Integer call() throws IOException, InterruptedException, GeneralSecurityException, JOSEException, UnexpectedStatusCodeException {
        final var vaultId = UUID.randomUUID();
        var csprng = SecureRandom.getInstanceStrong();
        try (var backend = new Backend(accessToken.value, common.getApiBase());
             var masterkey = Masterkey.generate(csprng)) {
            var user = backend.getUserService().getMe(false);
            var userPublicKeyBytes = BaseEncoding.base64().decode(user.publicKey());
            var userPublicKey = KeyHelper.readX509EncodedEcPublicKey(userPublicKeyBytes);

            var vaultConfigString = createVaultConfig(vaultId, masterkey);
            var jwe = JWEHelper.encryptVaultKey(masterkey, userPublicKey);
            backend.getVaultService().createOrUpdateVault(vaultId, name, description, false);
            backend.getVaultService().grantAccess(vaultId, user.id(), jwe.serialize());

            createLocalVault(masterkey, csprng, vaultConfigString);
        }
        return 0;
    }


    private String createVaultConfig(UUID vaultId, Masterkey masterkey) throws IOException, InterruptedException, JOSEException {
        //get Hub config
        var hubConfig = common.getConfig();

        //we are using GSON impl here, otherwise the object will be escaped
        var successUri = common.getApiBase().resolve("../app/unlock-success?vault=" + vaultId);
        var errorUri = common.getApiBase().resolve("../app/unlock-error?vault=" + vaultId);
        JsonObject json = new JsonObject();
        json.add("clientId", new JsonPrimitive("cryptomator"));
        json.add("authEndpoint", new JsonPrimitive(hubConfig.getAuthEndpoint().toString()));
        json.add("tokenEndpoint", new JsonPrimitive(hubConfig.getTokenEndpoint().toString()));
        json.add("authSuccessUrl", new JsonPrimitive(successUri.toString()));
        json.add("authErrorUrl", new JsonPrimitive(errorUri.toString()));
        json.add("apiBaseUrl", new JsonPrimitive(common.getApiBase().toString()));

        //create jwt
        String kid = "hub+" + common.getApiBase().resolve("vaults/" + vaultId);
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .keyID(kid)
                .type(JWT)
                .customParam("hub", json)
                .build();
        JWTClaimsSet payload = new JWTClaimsSet.Builder()
                .jwtID(vaultId.toString())
                .claim("format", 8)
                .claim("cipherCombo", "SIV_GCM")
                .claim("shorteningThreshold", 220)
                .build();

        var jwt = new SignedJWT(header, payload);
        // Apply the HMAC protection
        JWSSigner signer = new MACSigner(masterkey);
        jwt.sign(signer);
        return jwt.serialize();
    }

    private void createLocalVault(Masterkey masterkey, SecureRandom csprng, String vaultConfig) throws IOException {
        var vaultPath = path.resolve(name);
        Files.createDirectory(vaultPath);
        try (Cryptor cryptor = CryptorProvider.forScheme(CryptorProvider.Scheme.SIV_GCM).provide(masterkey.copy(), csprng)) {
            // save vault config:
            Path vaultConfigPath = vaultPath.resolve("vault.cryptomator");
            Files.writeString(vaultConfigPath, vaultConfig, StandardCharsets.US_ASCII, WRITE, CREATE_NEW);
            // create "d" dir and root:
            String dirHash = cryptor.fileNameCryptor().hashDirectoryId("");
            Path vaultCipherRootPath = vaultPath.resolve("d").resolve(dirHash.substring(0, 2)).resolve(dirHash.substring(2));
            Files.createDirectories(vaultCipherRootPath);
            // create dirId backup:
            try (var channel = Files.newByteChannel(vaultCipherRootPath.resolve("dirid.c9r"), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE); //
                 var encryptingChannel = new EncryptingWritableByteChannel(channel, cryptor)) {
                encryptingChannel.write(ByteBuffer.wrap("".getBytes(StandardCharsets.US_ASCII)));
            }
        }
    }
}
