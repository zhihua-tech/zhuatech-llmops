/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CanaryReleaseServiceTest {
    private final CanaryReleaseService service = new CanaryReleaseService();

    @Test
    void stableBucketAndFullCanarySelectCandidate() {
        var request = request(100, 200, 0.1, 300, 0, true, true);
        var first = service.evaluate(request);
        var second = service.evaluate(request);
        assertEquals(CanaryReleaseService.Decision.ACTIVE, first.decision());
        assertEquals("model-v2", first.selectedVersion());
        assertEquals(first.routingBucket(), second.routingBucket());
        assertEquals("model-v1", service.evaluate(request(0, 200, 0.1, 300, 0, true, true)).selectedVersion());
    }

    @Test
    void insufficientSamplesHoldOnBaseline() {
        var result = service.evaluate(request(50, 10, 0.1, 300, 0, true, true));
        assertEquals(CanaryReleaseService.Decision.HOLD, result.decision());
        assertEquals("model-v1", result.selectedVersion());
    }

    @Test
    void safetyOrSloBreachTriggersRollback() {
        assertEquals(CanaryReleaseService.Decision.ROLLBACK,
                service.evaluate(request(50, 200, 0.1, 300, 1, true, true)).decision());
        assertEquals(CanaryReleaseService.Decision.ROLLBACK,
                service.evaluate(request(50, 200, 5, 300, 0, true, true)).decision());
        assertEquals(CanaryReleaseService.Decision.ROLLBACK,
                service.evaluate(request(50, 200, 0.1, 1000, 0, true, true)).decision());
    }

    @Test
    void missingRecoveryBlocksAndInvalidVersionIsRejected() {
        assertEquals(CanaryReleaseService.Decision.BLOCKED,
                service.evaluate(request(50, 200, 0.1, 300, 0, false, true)).decision());
        var input = request(50, 200, 0.1, 300, 0, true, true);
        assertThrows(IllegalArgumentException.class, () -> service.evaluate(new CanaryReleaseService.Request(
                input.tenantId(), input.userId(), input.releaseId(), "model-v1", "model-v1",
                input.canaryPercent(), input.observedRequests(), input.minSampleRequests(),
                input.candidateErrorPercent(), input.maxErrorPercent(), input.candidateP95LatencyMs(),
                input.maxP95LatencyMs(), input.criticalSafetyEvents(), true, true)));
    }

    private CanaryReleaseService.Request request(int percent, long samples, double error, long latency,
                                                  int safetyEvents, boolean rollback, boolean fallback) {
        return new CanaryReleaseService.Request("tenant-a", "user-1", "release-1", "model-v1", "model-v2",
                percent, samples, 100, error, 1, latency, 500, safetyEvents, rollback, fallback);
    }
}
