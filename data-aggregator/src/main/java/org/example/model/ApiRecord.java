package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class ApiRecord {
    private final long id;
    private final String source;
    private final JsonNode data;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                    .withZone(ZoneOffset.UTC);
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private final Instant timestamp;
    private static final AtomicLong counter = new AtomicLong(0);

    public ApiRecord(String source, JsonNode data) {
        this.id = counter.incrementAndGet();
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

    @JsonIgnore
    public String getFormattedTimestamp() {
        return TIMESTAMP_FORMATTER.format(timestamp);
    }
}