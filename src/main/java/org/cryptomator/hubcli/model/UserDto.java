package org.cryptomator.hubcli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(@JsonProperty("id") String id,
					  @JsonProperty("name") String name,
					  @JsonProperty("type") String type,
					  @JsonProperty("publicKey") String publicKey,
					  @JsonProperty("privateKey") String privateKey,
					  @JsonProperty("setupCode") String setupCode) {
}
