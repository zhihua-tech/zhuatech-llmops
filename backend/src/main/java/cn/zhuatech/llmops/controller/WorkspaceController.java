/* Copyright 2026 上海如静知华信息科技有限公司 */
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

@RestController
@RequestMapping("/api/shopfloor")
@PreAuthorize("hasAnyRole('DOMAIN_USER','ADMIN')")
public class WorkspaceController {
    private final LlmOpsService service;
    private final AiProvider ai;
    private final ReleaseGateService releaseGate;

    public WorkspaceController(LlmOpsService service, AiProvider ai, ReleaseGateService releaseGate) {
        this.service = service;
        this.ai = ai;
        this.releaseGate = releaseGate;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Dashboard> dashboard() { return ApiResponse.ok(service.shopfloorDashboard()); }

    @PostMapping("/work-orders/{id}/reports")
    public ApiResponse<ReportResult> report(@PathVariable Long id, @Valid @RequestBody ReportRequest request) {
        return ApiResponse.ok("反馈提交成功", service.report(id, request));
    }

    @PostMapping("/ai-preview")
    public ApiResponse<AiProvider.AiResult> preview(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(ai.execute(body.getOrDefault("prompt", ""), Map.of("mode", "demo")));
    }

    @PostMapping("/release-gate")
    public ApiResponse<ReleaseGateService.GateResult> evaluateRelease(@Valid @RequestBody ReleaseGateService.GateRequest request) {
        return ApiResponse.ok("发布门禁评估完成", releaseGate.evaluate(request));
    }
}
