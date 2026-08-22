/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.service;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModelCostForecastService {
    private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");

    public Result forecast(Request request) {
        BigDecimal requests = BigDecimal.valueOf(request.monthlyRequests());
        BigDecimal cacheFactor = BigDecimal.ONE.subtract(request.cacheHitRate().multiply(new BigDecimal("0.8")));
        BigDecimal inputTokens = requests.multiply(BigDecimal.valueOf(request.averageInputTokens())).multiply(cacheFactor);
        BigDecimal outputTokens = requests.multiply(BigDecimal.valueOf(request.averageOutputTokens()));
        BigDecimal inputCost = inputTokens.divide(ONE_MILLION, 8, RoundingMode.HALF_UP)
            .multiply(request.inputPricePerMillion());
        BigDecimal outputCost = outputTokens.divide(ONE_MILLION, 8, RoundingMode.HALF_UP)
            .multiply(request.outputPricePerMillion());
        BigDecimal monthlyCost = inputCost.add(outputCost).setScale(2, RoundingMode.HALF_UP);
        BigDecimal budgetUsage = request.monthlyBudget().signum() == 0 ? BigDecimal.ONE
            : monthlyCost.divide(request.monthlyBudget(), 4, RoundingMode.HALF_UP);

        String status = monthlyCost.compareTo(request.monthlyBudget()) > 0 ? "OVER_BUDGET"
            : budgetUsage.compareTo(new BigDecimal("0.80")) >= 0 ? "WARNING" : "WITHIN_BUDGET";
        List<String> actions = new ArrayList<>();
        if (request.cacheHitRate().compareTo(new BigDecimal("0.40")) < 0) actions.add("提高系统提示词与知识检索结果缓存命中率");
        if (request.averageOutputTokens() > 800) actions.add("设置输出长度预算并压缩冗余回答");
        if ("OVER_BUDGET".equals(status)) actions.add("暂停扩量并进行模型路由与批处理成本评审");
        if (actions.isEmpty()) actions.add("保持当前模型路由并按周复核实际账单偏差");
        return new Result(request.applicationCode(), monthlyCost, budgetUsage, status, actions);
    }

    public record Request(@NotBlank String applicationCode, @Min(0) long monthlyRequests,
                          @Min(0) int averageInputTokens, @Min(0) int averageOutputTokens,
                          @DecimalMin("0") BigDecimal inputPricePerMillion,
                          @DecimalMin("0") BigDecimal outputPricePerMillion,
                          @DecimalMin("0") @DecimalMax("1") BigDecimal cacheHitRate,
                          @DecimalMin("0.01") BigDecimal monthlyBudget) {}

    public record Result(String applicationCode, BigDecimal forecastMonthlyCost,
                         BigDecimal budgetUsageRate, String status, List<String> actions) {}
}
