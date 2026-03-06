package com.ubcmmhcsoftware.newsletter.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

/**
 * CloudEvents-style event published by Membership Service when a membership is created.
 * Schema: contracts/schemas/events/membership-created.json
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MembershipCreatedEvent {

    @JsonProperty("specversion")
    private String specVersion;

    @JsonProperty("type")
    private String type;

    @JsonProperty("source")
    private String source;

    @JsonProperty("data")
    private DataPayload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        @JsonProperty("membershipId")
        private UUID membershipId;

        @JsonProperty("email")
        private String email;

        @JsonProperty("newsletterOptIn")
        private boolean newsletterOptIn;
    }
}
