/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.controller;

import cn.zhuatech.llmops.common.ApiResponse;
import cn.zhuatech.llmops.service.CanaryReleaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/llmops")
public class CanaryReleaseController {
    private final CanaryReleaseService service;

    public CanaryReleaseController(CanaryReleaseService service) {
        this.service = service;
    }

    @PostMapping("/canary-release")
    public ApiResponse<CanaryReleaseService.Result> evaluate(@Valid @RequestBody CanaryReleaseService.Request request) {
        return ApiResponse.ok(service.evaluate(request));
    }
}
