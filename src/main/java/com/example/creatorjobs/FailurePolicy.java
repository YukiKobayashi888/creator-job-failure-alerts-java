package com.example.creatorjobs;

public final class FailurePolicy {
    public FailureDecision decide(JobFailure failure) {
        if (failure.jobName().isBlank() || failure.assetId().isBlank()) {
            return new FailureDecision(false, "ignored", "missing job or asset identity");
        }
        return new FailureDecision(true, "alerted", "delivery failure requires review: " + failure.reason());
    }
}
