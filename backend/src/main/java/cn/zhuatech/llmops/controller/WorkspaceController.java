/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.llmops.controller;

import cn.zhuatech.llmops.ai.AiProvider;
import cn.zhuatech.llmops.common.ApiResponse;
import cn.zhuatech.llmops.dto.LlmOpsDto.*;
import cn.zhuatech.llmops.service.LlmOpsService;
import cn.zhuatech.llmops.service.ReleaseGateService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@RestController
@RequestMapping("/api/shopfloor")
@PreAuthorize("hasAnyRole('DOMAIN_USER','ADMIN')")
public class WorkspaceController {
    private final LlmOpsService service;
    private final AiProvider ai;
    private final ReleaseGateService releaseGate;

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public WorkspaceController(LlmOpsService service, AiProvider ai, ReleaseGateService releaseGate) {
        this.service = service;
        this.ai = ai;
        this.releaseGate = releaseGate;
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @GetMapping("/dashboard")
    public ApiResponse<Dashboard> dashboard() { return ApiResponse.ok(service.shopfloorDashboard()); }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @PostMapping("/work-orders/{id}/reports")
    public ApiResponse<ReportResult> report(@PathVariable Long id, @Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok("反馈提交成功", service.report(id, request));
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @PostMapping("/ai-preview")
    public ApiResponse<AiProvider.AiResult> preview(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(ai.execute(body.getOrDefault("prompt", ""), Map.of("mode", "demo")));
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @PostMapping("/release-gate")
    public ApiResponse<ReleaseGateService.GateResult> evaluateRelease(@Valid @RequestBody ReleaseGateService.GateRequest request) {
        return ApiResponse.ok("发布门禁评估完成", releaseGate.evaluate(request));
    }
}
