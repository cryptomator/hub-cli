package org.cryptomator.hubcli.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.cryptomator.hubcli.util.WordEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * This test is primarily used in conjunction with GraalVM's <code>-agentlib:native-image-agent</code> JVM flag (see surefire plugin config)
 * to generate reflect-config.json required for a working native image.
 */
public class WordEncoderTest {

	@Test
	public void encodeAndDecode() {
		byte[] input = new byte[15];
		WordEncoder encoder = new WordEncoder();
		String encoded = encoder.encodePadded(input);
		byte[] decoded = encoder.decode(encoded);
		Assertions.assertArrayEquals(input, decoded);
	}
}
