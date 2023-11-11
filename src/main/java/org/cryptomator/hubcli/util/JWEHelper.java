package org.cryptomator.hubcli.util;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.cryptomator.cryptolib.api.Masterkey;
import org.cryptomator.cryptolib.api.MasterkeyLoadingFailedException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;

public class JWEHelper {

	private static final String JWE_PAYLOAD_KEY_FIELD = "key";
	private static final String EC_ALG = "EC";
	private static final int P2S_LEN = 16;
	private static final int P2C = 1_000_000;

	private JWEHelper() {}

	public static JWEObject encryptUserKey(ECPrivateKey userKey, ECPublicKey deviceKey) {
		var encodedUserKey = Base64.getEncoder().encodeToString(userKey.getEncoded());
		var payload = new Payload(Map.of(JWE_PAYLOAD_KEY_FIELD, encodedUserKey));
		return encrypt(payload, deviceKey);
	}

	public static JWEObject encryptVaultKey(Masterkey vaultKey, ECPublicKey userKey) throws InvalidJweKeyException {
		var encodedUserKey = Base64.getEncoder().encodeToString(vaultKey.getEncoded());
		var payload = new Payload(Map.of(JWE_PAYLOAD_KEY_FIELD, encodedUserKey));
		return encrypt(payload, userKey);
	}

	public static JWEObject encrypt(Payload payload, ECPublicKey recipient) {
		try {
			var keyGen = new ECKeyGenerator(Curve.P_384);
			var ephemeralKeyPair = keyGen.generate();
			var header = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES, EncryptionMethod.A256GCM).ephemeralPublicKey(ephemeralKeyPair.toPublicJWK()).build();
			var jwe = new JWEObject(header, payload);
			jwe.encrypt(new ECDHEncrypter(recipient));
			return jwe;
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}

	public static JWEObject encryptUserKey(ECPrivateKey userKey, String setupCode) {
		try {
			var encodedUserKey = Base64.getEncoder().encodeToString(userKey.getEncoded());
			var header = new JWEHeader.Builder(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256GCM).build(); // salt + iteration count set during encryption
			var payload = new Payload(Map.of(JWE_PAYLOAD_KEY_FIELD, encodedUserKey));
			var jwe = new JWEObject(header, payload);
			jwe.encrypt(new PasswordBasedEncrypter(setupCode, P2S_LEN, P2C));
			return jwe;
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}

	public static ECPrivateKey decryptUserKey(JWEObject jwe, String setupCode) throws InvalidJweKeyException {
		try {
			jwe.decrypt(new PasswordBasedDecrypter(setupCode));
			return decodeUserKey(jwe);
		} catch (JOSEException e) {
			throw new InvalidJweKeyException(e);
		}
	}

	public static ECPrivateKey decryptUserKey(JWEObject jwe, ECPrivateKey deviceKey) throws InvalidJweKeyException {
		try {
			jwe.decrypt(new ECDHDecrypter(deviceKey));
			return decodeUserKey(jwe);
		} catch (JOSEException e) {
			throw new InvalidJweKeyException(e);
		}
	}

	private static ECPrivateKey decodeUserKey(JWEObject decryptedJwe) {
		try {
			var keySpec = readKey(decryptedJwe, JWE_PAYLOAD_KEY_FIELD, PKCS8EncodedKeySpec::new);
			var factory = KeyFactory.getInstance(EC_ALG);
			var privateKey = factory.generatePrivate(keySpec);
			if (privateKey instanceof ECPrivateKey ecPrivateKey) {
				return ecPrivateKey;
			} else {
				throw new IllegalStateException(EC_ALG + " key factory not generating ECPrivateKeys");
			}
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(EC_ALG + " not supported");
		} catch (InvalidKeySpecException e) {
			throw new MasterkeyLoadingFailedException("Unexpected JWE payload", e);
		}
	}

	public static Masterkey decryptVaultKey(JWEObject jwe, ECPrivateKey privateKey) throws InvalidJweKeyException {
		try {
			jwe.decrypt(new ECDHDecrypter(privateKey));
			return readKey(jwe, JWE_PAYLOAD_KEY_FIELD, Masterkey::new);
		} catch (JOSEException e) {
			throw new InvalidJweKeyException(e);
		}
	}

	private static <T> T readKey(JWEObject jwe, String keyField, Function<byte[], T> rawKeyFactory) throws MasterkeyLoadingFailedException {
		Preconditions.checkArgument(jwe.getState() == JWEObject.State.DECRYPTED);
		var fields = jwe.getPayload().toJSONObject();
		if (fields == null) {
			throw new MasterkeyLoadingFailedException("Expected JWE payload to be JSON");
		}
		var keyBytes = new byte[0];
		try {
			if (fields.get(keyField) instanceof String key) {
				keyBytes = BaseEncoding.base64().decode(key);
				return rawKeyFactory.apply(keyBytes);
			} else {
				throw new IllegalArgumentException("JWE payload doesn't contain field " + keyField);
			}
		} catch (IllegalArgumentException e) {
			throw new MasterkeyLoadingFailedException("Unexpected JWE payload", e);
		} finally {
			Arrays.fill(keyBytes, (byte) 0x00);
		}
	}

	public static class InvalidJweKeyException extends MasterkeyLoadingFailedException {

		public InvalidJweKeyException(Throwable cause) {
			super("Invalid key", cause);
		}
	}
}
