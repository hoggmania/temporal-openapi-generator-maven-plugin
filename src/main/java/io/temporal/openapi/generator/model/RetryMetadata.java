package io.temporal.openapi.generator.model;

/**
 * Metadata for retry configuration in Temporal Activities
 */
public class RetryMetadata {
    private final int initialInterval;
    private final int maximumInterval;
    private final double backoffCoefficient;
    private final int maximumAttempts;

    public RetryMetadata(int initialInterval, int maximumInterval, 
                        double backoffCoefficient, int maximumAttempts) {
        this.initialInterval = initialInterval;
        this.maximumInterval = maximumInterval;
        this.backoffCoefficient = backoffCoefficient;
        this.maximumAttempts = maximumAttempts;
    }

    public static RetryMetadata defaultRetry() {
        return new RetryMetadata(1, 100, 2.0, 3);
    }

    public static RetryMetadata safeIdempotentRetry() {
        // Idempotent operations can retry more aggressively
        return new RetryMetadata(1, 300, 2.0, 5);
    }

    public static RetryMetadata nonIdempotentRetry() {
        // Non-idempotent operations should be more conservative
        return new RetryMetadata(2, 60, 1.5, 2);
    }

    public int getInitialInterval() {
        return initialInterval;
    }

    public int getMaximumInterval() {
        return maximumInterval;
    }

    public double getBackoffCoefficient() {
        return backoffCoefficient;
    }

    public int getMaximumAttempts() {
        return maximumAttempts;
    }
}
