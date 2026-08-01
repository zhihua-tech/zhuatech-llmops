/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.llmops.ai;
import org.springframework.stereotype.Component; import java.util.Map;
public interface AiProvider { AiResult execute(String prompt,Map<String,String> context); record AiResult(String provider,String answer,Map<String,Object> evidence){} }
@Component class DemoAiProvider implements AiProvider { public AiResult execute(String prompt,Map<String,String> context){return new AiResult("demo-llmops-provider","已生成演示评测摘要，生产发布仍须通过质量、安全、时延和人工审批门禁。",Map.of("qualityScore",91.8,"safetyPassed",true,"gate","pending"));} }
