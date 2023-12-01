package org.cryptomator.hubcli.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GroupDto(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("type") String type) {}
