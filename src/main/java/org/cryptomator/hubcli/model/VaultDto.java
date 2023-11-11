package org.cryptomator.hubcli.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

public record VaultDto(@JsonProperty("id") UUID id, //
					   @JsonProperty("name") String name, //
					   @JsonProperty("description") String description, //
					   @JsonProperty("archived") boolean archived, //
					   //TODO: To use instant we need jackson jsr310
					   @JsonProperty("creationTime") String creationTime, //
					   // Legacy properties ("Vault Admin Password"):
					   @JsonProperty("masterkey") String masterkey, //
					   @JsonProperty("iterations") Integer iterations, //
					   @JsonProperty("salt") String salt, //
					   @JsonProperty("authPublicKey") String authPublicKey, //
					   @JsonProperty("authPrivateKey") String authPrivateKey //
) {

	public String toJson() throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(this);
	}
}
