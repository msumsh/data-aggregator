package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.*;

public class ApiRecord {
    private final long id;
    private final String source;
    private final Instant timestamp;
    private final JsonNode data;
    private static long counter = 0;

    public ApiRecord(String source, JsonNode data) {
        this.id = ++counter;
        this.source = source;
        this.timestamp = Instant.now();
        this.data = data;
    }

    @JsonCreator
    public ApiRecord(
            @JsonProperty("id") long id,
            @JsonProperty("source") String source,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("data") JsonNode data) {
        this.id = id;
        this.source = source;
        this.timestamp = timestamp;
        this.data = data;
    }

    public long getId() {
        return this.id;
    }

    public String getSource() {
        return this.source;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public JsonNode getData() {
        return this.data;
    }

}