/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.service;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据灰度遥测执行发布门禁，并为同一租户、用户和版本生成稳定路由。
 *
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Service
public class CanaryReleaseService {
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public Result evaluate(Request request) {
        if (request.baselineVersion().equals(request.candidateVersion())) {
            throw new IllegalArgumentException("基线与候选模型版本不能相同");
        }
        if (request.canaryPercent() < 0 || request.canaryPercent() > 100
                || request.candidateErrorPercent() < 0 || request.candidateErrorPercent() > 100
                || request.maxErrorPercent() < 0 || request.maxErrorPercent() > 100
                || request.candidateP95LatencyMs() < 0 || request.maxP95LatencyMs() <= 0
                || request.observedRequests() < 0 || request.minSampleRequests() < 1
                || request.criticalSafetyEvents() < 0) {
            throw new IllegalArgumentException("灰度发布指标超出允许范围");
        }
        List<String> reasons = new ArrayList<>();
        int bucket = bucket(request.tenantId(), request.userId(), request.releaseId());
        if (!request.rollbackReady() || !request.fallbackReady()) {
            if (!request.rollbackReady()) reasons.add("回滚方案尚未就绪");
            if (!request.fallbackReady()) reasons.add("基线或降级模型不可用");
            return new Result(Decision.BLOCKED, request.baselineVersion(), bucket, List.copyOf(reasons));
        }
        if (request.criticalSafetyEvents() > 0 || request.candidateErrorPercent() > request.maxErrorPercent()
                || request.candidateP95LatencyMs() > request.maxP95LatencyMs()) {
            if (request.criticalSafetyEvents() > 0) reasons.add("候选模型发生严重安全事件");
            if (request.candidateErrorPercent() > request.maxErrorPercent()) reasons.add("候选模型错误率超标");
            if (request.candidateP95LatencyMs() > request.maxP95LatencyMs()) reasons.add("候选模型 P95 延迟超标");
            return new Result(Decision.ROLLBACK, request.baselineVersion(), bucket, List.copyOf(reasons));
        }
        if (request.observedRequests() < request.minSampleRequests()) {
            reasons.add("灰度样本量不足，暂缓扩大流量");
            return new Result(Decision.HOLD, request.baselineVersion(), bucket, List.copyOf(reasons));
        }
        String version = bucket < request.canaryPercent() * 100
                ? request.candidateVersion() : request.baselineVersion();
        return new Result(Decision.ACTIVE, version, bucket, List.of("灰度指标通过，按稳定分桶路由"));
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    private int bucket(String tenantId, String userId, String releaseId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((tenantId + ":" + userId + ":" + releaseId).getBytes(StandardCharsets.UTF_8));
            return (int) (Integer.toUnsignedLong(ByteBuffer.wrap(digest).getInt()) % 10000);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record Request(@NotBlank String tenantId, @NotBlank String userId, @NotBlank String releaseId,
                          @NotBlank String baselineVersion, @NotBlank String candidateVersion,
                          @Min(0) int canaryPercent, @Min(0) long observedRequests,
                          @Positive long minSampleRequests,
                          @DecimalMin("0") @DecimalMax("100") double candidateErrorPercent,
                          @DecimalMin("0") @DecimalMax("100") double maxErrorPercent,
                          @Min(0) long candidateP95LatencyMs, @Positive long maxP95LatencyMs,
                          @Min(0) int criticalSafetyEvents, boolean rollbackReady,
                          boolean fallbackReady) {}

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record Result(Decision decision, String selectedVersion, int routingBucket, List<String> reasons) {}

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public enum Decision { ACTIVE, HOLD, BLOCKED, ROLLBACK }
}
