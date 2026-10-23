package com.example.creatorjobs;

import java.net.http.HttpClient;

public final class CreatorJobsApplication {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Set INFRAI_API_KEY before running the capture demo");
        }
        JobFailure failure = new JobFailure("monthly-download", "subscriber-884", "asset-204", "processor rejected checksum");
        CreatorJobService service = new CreatorJobService(
                new FailurePolicy(), new InfraiErrorsClient(HttpClient.newHttpClient(), key));
        System.out.println(service.process(failure));
    }
}
