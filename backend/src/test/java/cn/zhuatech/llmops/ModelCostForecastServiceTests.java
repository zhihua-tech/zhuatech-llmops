/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.llmops;

import cn.zhuatech.llmops.service.ModelCostForecastService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelCostForecastServiceTests {
    private final ModelCostForecastService service = new ModelCostForecastService();

    @Test
    void warnsWhenForecastApproachesBudget() {
        var result = service.forecast(new ModelCostForecastService.Request(
            "KNOWLEDGE-ASSISTANT", 100000, 1000, 300, new BigDecimal("2"),
            new BigDecimal("8"), new BigDecimal("0.5"), new BigDecimal("400")));

        assertEquals(new BigDecimal("360.00"), result.forecastMonthlyCost());
        assertEquals(new BigDecimal("0.9000"), result.budgetUsageRate());
        assertEquals("WARNING", result.status());
    }

    @Test
    void blocksExpansionWhenForecastExceedsBudget() {
        var result = service.forecast(new ModelCostForecastService.Request(
            "REPORT-AGENT", 200000, 1500, 900, new BigDecimal("3"),
            new BigDecimal("12"), new BigDecimal("0.2"), new BigDecimal("1000")));

        assertEquals("OVER_BUDGET", result.status());
    }
}
