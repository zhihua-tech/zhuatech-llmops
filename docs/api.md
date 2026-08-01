# LLMOps API 摘要

版权所有 © 2026 上海如静知华信息科技有限公司。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录并获取 JWT |
| GET | `/api/admin/dashboard` | 模型运营治理数据 |
| GET | `/api/admin/work-orders` | 模型发布任务清单 |
| GET | `/api/shopfloor/dashboard` | 模型评测工作台 |
| POST | `/api/shopfloor/work-orders/{id}/reports` | 提交模型评测结果 |
| POST | `/api/shopfloor/ai-preview` | 调用可替换 AI Provider 生成评测摘要 |
| POST | `/api/shopfloor/release-gate` | 汇总质量、安全、时延、成本和人工审批门禁 |
