package org.cryptomator.hubcli.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * This test is used in conjunction with GraalVM's <code>-agentlib:native-image-agent</code> JVM flag (see surefire plugin config)
 * to generate reflect-config.json required for a working native image.
 */
public class DtoSerializationTest {

	ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

	@Test
	public void device() throws IOException {
		var original = new DeviceDto("id", "name", "type", "publicKey", "privateKey", "ownerId", Instant.now());
		var serialized = mapper.writer().writeValueAsString(original);
		var deserialized = mapper.reader().readValue(serialized, DeviceDto.class);
		Assertions.assertEquals(original, deserialized);
	}

	@Test
	public void group() throws IOException {
		var original = new GroupDto("id", "name", "type");
		var serialized = mapper.writer().writeValueAsString(original);
		var deserialized = mapper.reader().readValue(serialized, GroupDto.class);
		Assertions.assertEquals(original, deserialized);
	}

	@Test
	public void user() throws IOException {
		var original = new UserDto("id", "name", "type", "publicKey", "privateKey", "setupCode");
		var serialized = mapper.writer().writeValueAsString(original);
		var deserialized = mapper.reader().readValue(serialized, UserDto.class);
		Assertions.assertEquals(original, deserialized);
	}

	@Test
	public void vault() throws IOException {
		var original = new VaultDto(UUID.randomUUID(), "name", "description", false, "creationTime", "masterkey",1, "salt", "authPubKey", "authPrivKey");
		var serialized = mapper.writer().writeValueAsString(original);
		var deserialized = mapper.reader().readValue(serialized, VaultDto.class);
		Assertions.assertEquals(original, deserialized);
	}

}
