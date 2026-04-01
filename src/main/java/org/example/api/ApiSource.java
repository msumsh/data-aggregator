package org.example.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.ApiRecord;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public abstract class ApiSource {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final String name;

    public ApiSource(String baseUrl, String name) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    protected String getBaseUrl() {
        return this.baseUrl;
    }

    protected String getName() {
        return this.name;
    }

    protected String buildQuery(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!query.isEmpty()) {
                query.append("&");
            }

            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null) continue;
            if (value == null) value = "";

            query.append(key)
                    .append("=")
                    .append(value);
        }

        return query.toString();
    }

    public ApiRecord parseResponse(String data) throws IOException {
        JsonNode root = objectMapper.readTree(data);

        return new ApiRecord(getName(), root);
    }

    public abstract String buildUrl();
    public abstract Map<String, String> getDefaultParams();
}
