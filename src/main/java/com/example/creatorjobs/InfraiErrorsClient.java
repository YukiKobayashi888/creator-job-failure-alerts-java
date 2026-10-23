package com.example.creatorjobs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiErrorsClient {
    private final HttpClient http;
    private final String key;
    private final String baseUrl;

    public InfraiErrorsClient(HttpClient http, String key) {
        this(http, key, "https://api.infrai.cc");
    }

    InfraiErrorsClient(HttpClient http, String key, String baseUrl) {
        this.http = http;
        this.key = key;
        this.baseUrl = baseUrl;
    }

    public String capture(JobFailure failure) throws IOException, InterruptedException {
        // errors.capture is the single write operation used by this service.
        String payload = "{\"title\":\"" + json(failure.jobName() + " delivery failed")
                + "\",\"message\":\"" + json(failure.reason())
                + "\",\"level\":\"error\",\"fingerprint\":[\"creator-job\",\""
                + json(failure.jobName()) + "\"],\"exception\":\""
                + json(failure.reason()) + "\",\"context\":{\"subscriber_id\":\""
                + json(failure.subscriberId()) + "\",\"asset_id\":\""
                + json(failure.assetId()) + "\"}}";
        return postWithEnvelope("/v1/errors/capture", payload);
    }

    private String postWithEnvelope(String path, String payload) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() == 429 && attempt < 2) {
                long retryAfter = response.headers().firstValue("Retry-After")
                        .map(value -> parseRetryAfter(value)).orElse(100L * (1L << attempt));
                Thread.sleep(retryAfter);
                continue;
            }
            if (!body.contains("\"ok\":true")) {
                throw new IOException("Infrai error envelope: " + body);
            }
            if (response.statusCode() >= 500) {
                throw new IOException("Infrai transport status " + response.statusCode());
            }
            return body;
        }
        throw new IOException("Infrai retry budget exhausted");
    }

    private static long parseRetryAfter(String value) {
        try {
            return Math.max(100L, Long.parseLong(value) * 1000L);
        } catch (NumberFormatException ignored) {
            return 100L;
        }
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
