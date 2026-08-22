/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.controller;

import cn.zhuatech.llmops.common.ApiResponse;
import cn.zhuatech.llmops.service.ModelCostForecastService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/llmops/insights")
public class ModelCostForecastController {
    private final ModelCostForecastService service;

    public ModelCostForecastController(ModelCostForecastService service) {
        this.service = service;
    }

    @PostMapping("/model-cost-forecast")
    public ApiResponse<ModelCostForecastService.Result> forecast(
        @Valid @RequestBody ModelCostForecastService.Request request) {
        return ApiResponse.ok(service.forecast(request));
    }
}
