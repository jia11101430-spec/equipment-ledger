# 学习记录



## 2026-07-12：Spring Boot 最小闭环



- 我创建了 `equipment-ledger` GitHub 仓库，并完成了第一次推送。

- 我使用 Spring Initializr 创建了 Maven 与 Spring Web 项目。

- 我使用 `mvn test` 验证 Spring Boot 默认项目能正常启动。

- 我先写了 `HealthControllerTest`，因为接口不存在而得到 HTTP 404。

- 我新增 `HealthController` 后，测试通过。

- 我使用 `Invoke-RestMethod` 真实访问了 `GET /api/health`，得到 `status: ok` 和 `service: equipment-ledger`。

- 我通过 `feature/health-api` 分支完成开发、推送，并合并回 `main`。



## 今天理解的内容



- 浏览器或接口工具发送 HTTP 请求后，Spring Boot 根据请求路径找到控制器方法。

- `@RestController` 表示这个类负责提供接口；返回的 Java `Map` 会自动转换为 JSON。

- `@RequestMapping("/api")` 与 `@GetMapping("/health")` 组合为接口路径 `/api/health`。

- 测试先失败、实现后通过，能证明新代码确实满足需求。



## 下一步



配置 MySQL，创建 `workshops`、`devices` 和 `maintenance_records` 三张核心表。


