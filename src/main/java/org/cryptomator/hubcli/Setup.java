package org.cryptomator.hubcli;

import com.nimbusds.jose.Payload;
import org.cryptomator.cryptolib.common.P384KeyPair;
import org.cryptomator.hubcli.model.DeviceDto;
import org.cryptomator.hubcli.model.UserDto;
import org.cryptomator.hubcli.util.JWEHelper;
import org.cryptomator.hubcli.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "setup", description = "Initialize key pairs and registers with Hub. Prints this user's setup code to STDOUT on success.")
public class Setup implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(Setup.class);

	@Mixin
	Common common;

	@Mixin
	AccessToken accessToken;

	@Mixin
	P12 p12;

	@Override
	public Integer call() throws IOException, InterruptedException, ParseException {
		// generate device key:
		if (Files.exists(p12.file)) {
			throw new IllegalStateException("Already set up (file exists: " + p12.file + ")");
		}

		// parse access token:
		var jwt = accessToken.parsed();
		if (jwt.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Access token expired");
		}
		var subject = jwt.getJWTClaimsSet().getSubject();

		// generate setup code:
		var setupCode = UUID.randomUUID().toString();

		// generate and store device key pair:
		var deviceKeyPair = P384KeyPair.generate();
		var deviceId = KeyHelper.getKeyId(deviceKeyPair.getPublic());
		deviceKeyPair.store(p12.file, p12.password);

		// generate and encrypt user key pair:
		var userKeyPair = P384KeyPair.generate();
		var keyProtectedByDevice = JWEHelper.encryptUserKey(userKeyPair.getPrivate(), deviceKeyPair.getPublic());
		var keyProtectedBySetupCode = JWEHelper.encryptUserKey(userKeyPair.getPrivate(), setupCode);
		var setupCodeProtectedByKey = JWEHelper.encrypt(new Payload(Map.of("setupCode", setupCode)), userKeyPair.getPublic());

		// prepare JSON for PUT requests:
		var user = new UserDto(subject, "Hub CLI", "USER", //
				Base64.getEncoder().encodeToString(userKeyPair.getPublic().getEncoded()), //
				keyProtectedBySetupCode.serialize(), //
				setupCodeProtectedByKey.serialize() //
		);
		var device = new DeviceDto(deviceId, "Hub CLI", "DESKTOP", //
				Base64.getEncoder().encodeToString(deviceKeyPair.getPublic().getEncoded()), //
				keyProtectedByDevice.serialize(), //
				subject, //
				Instant.now() //
		);
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			backend.getUserService().createOrUpdateMe(user);
			backend.getDeviceService().createOrUpdate(deviceId, device);
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}

		// print setup code to STDOUT
		System.out.println(setupCode);
		return 0;
	}

}
