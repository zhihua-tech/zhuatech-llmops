/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.service;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 汇总质量、安全、性能、成本和人工审批结果，形成可审计的模型发布门禁。 */
@Service
public class ReleaseGateService {
    private static final double MIN_QUALITY = 85.0;
    private static final double MIN_SAFETY = 95.0;
    private static final int MAX_P95_LATENCY_MS = 2500;
    private static final double MAX_COST_PER_1K_TOKENS = 0.08;

    public GateResult evaluate(GateRequest request) {
        List<String> failedGates = new ArrayList<>();
        if (request.qualityScore() < MIN_QUALITY) failedGates.add("QUALITY");
        if (request.safetyScore() < MIN_SAFETY) failedGates.add("SAFETY");
        if (request.p95LatencyMs() > MAX_P95_LATENCY_MS) failedGates.add("LATENCY");
        if (request.costPer1kTokens() > MAX_COST_PER_1K_TOKENS) failedGates.add("COST");
        if (!request.approvalPassed()) failedGates.add("HUMAN_APPROVAL");

        Map<String, Object> thresholds = new LinkedHashMap<>();
        thresholds.put("minQualityScore", MIN_QUALITY);
        thresholds.put("minSafetyScore", MIN_SAFETY);
        thresholds.put("maxP95LatencyMs", MAX_P95_LATENCY_MS);
        thresholds.put("maxCostPer1kTokens", MAX_COST_PER_1K_TOKENS);
        boolean passed = failedGates.isEmpty();
        return new GateResult(passed, passed ? "RELEASE" : "HOLD", List.copyOf(failedGates), Map.copyOf(thresholds), passed ? "允许进入灰度发布" : "修复失败门禁后重新评测");
    }

    public record GateRequest(
        @NotBlank(message = "请输入模型版本") String modelVersion,
        @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") double qualityScore,
        @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") double safetyScore,
        @Positive(message = "P95 时延必须大于 0") int p95LatencyMs,
        @PositiveOrZero(message = "推理成本不能为负数") double costPer1kTokens,
        boolean approvalPassed
    ) {}

    public record GateResult(
        boolean passed,
        String decision,
        List<String> failedGates,
        Map<String, Object> thresholds,
        String nextAction
    ) {}
}
