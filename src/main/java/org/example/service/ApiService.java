package org.example.service;

import org.example.api.ApiSource;
import org.example.exceptions.InvalidResponseException;
import org.example.exceptions.SendingHttpRequestException;
import org.example.model.ApiRecord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class ApiService {
    private final HttpClient httpClient;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public ApiService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ApiRecord fetchData(ApiSource source) throws InvalidResponseException, SendingHttpRequestException, IOException {
        Objects.requireNonNull(source, "ApiSource cannot be null");
        String url = source.buildUrl();
        Objects.requireNonNull(url, "buildUrl() returned null");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(600))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new SendingHttpRequestException("I/O error occurred while sending the request: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SendingHttpRequestException("Sending the request operation was interrupted: " + e.getMessage());
        }


        if (response.statusCode() != 200) {
            throw new InvalidResponseException("For " + url + " invalid response status code: ", response.statusCode());
        }

        String body = response.body();

        return source.parseResponse(body);
    }
}
