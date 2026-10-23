package com.example.creatorjobs;

public final class CreatorJobService {
    private final FailurePolicy policy;
    private final InfraiErrorsClient errors;

    public CreatorJobService(FailurePolicy policy, InfraiErrorsClient errors) {
        this.policy = policy;
        this.errors = errors;
    }

    public FailureDecision process(JobFailure failure) throws Exception {
        FailureDecision decision = policy.decide(failure);
        if (decision.alert()) {
            errors.capture(failure);
        }
        return decision;
    }
}
