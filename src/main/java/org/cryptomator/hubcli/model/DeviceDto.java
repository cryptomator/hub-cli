package org.cryptomator.hubcli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceDto(@JsonProperty("id") String id,
						@JsonProperty("name") String name,
						@JsonProperty("type") String type,
						@JsonProperty("publicKey") String publicKey,
						@JsonProperty("userPrivateKey") String userPrivateKey,
						@JsonProperty("owner") String ownerId,
						@JsonProperty("creationTime") Instant creationTime) {
}
