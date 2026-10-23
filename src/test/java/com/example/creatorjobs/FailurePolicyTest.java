package com.example.creatorjobs;

public final class FailurePolicyTest {
    public static void main(String[] args) {
        FailurePolicy policy = new FailurePolicy();
        FailureDecision alert = policy.decide(new JobFailure("monthly-download", "subscriber-884", "asset-204", "checksum"));
        if (!alert.alert() || !alert.status().equals("alerted")) {
            throw new AssertionError("complete delivery failures must alert");
        }
        FailureDecision ignored = policy.decide(new JobFailure("", "subscriber-884", "asset-204", "checksum"));
        if (ignored.alert() || !ignored.status().equals("ignored")) {
            throw new AssertionError("unidentified jobs must not alert");
        }
        System.out.println("FailurePolicyTest passed");
    }
}
