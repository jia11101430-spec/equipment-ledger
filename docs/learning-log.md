# 学习记录

## 2026-07-12：Spring Boot 最小闭环

- 我创建了 `equipment-ledger` GitHub 仓库，并完成了第一次推送。
- 我使用 Spring Initializr 创建了 Maven 与 Spring Web 项目。
- 我使用 `mvn test` 验证 Spring Boot 默认项目能正常启动。
- 我先写了 `HealthControllerTest`，因为接口不存在而得到 HTTP 404。
- 我新增 `HealthController` 后，测试通过。
- 我使用 `Invoke-RestMethod` 真实访问了 `GET /api/health`，得到 `status: ok` 和 `service: equipment-ledger`。
- 我通过 `feature/health-api` 分支完成开发、推送，并合并回 `main`。

### 今天理解的内容

- 浏览器或接口工具发送 HTTP 请求后，Spring Boot 根据请求路径找到控制器方法。
- `@RestController` 表示这个类负责提供接口；返回的 Java `Map` 会自动转换为 JSON。
- `@RequestMapping("/api")` 与 `@GetMapping("/health")` 组合为接口路径 `/api/health`。
- 测试先失败、实现后通过，能证明新代码确实满足需求。

### 下一步

配置 MySQL，创建 `workshops`、`devices` 和 `maintenance_records` 三张核心表。

## 2026-07-12：MySQL 数据库与建表脚本

- 我发现 MySQL80 服务无法启动，通过错误日志定位到电脑名包含 Emoji，导致默认 PID 文件创建失败。
- 我在 `my.ini` 中指定了英文 PID 文件 `mysql.pid`，随后 MySQL80 服务成功启动。
- 我以 `root@localhost` 登录 MySQL，创建了 `equipment_ledger` 数据库，并使用 utf8mb4 字符集。
- 我创建了 `workshops`、`devices`、`maintenance_records` 三张表。
- 我理解了两条外键：设备的 `workshop_id` 指向车间；维修记录的 `device_id` 指向设备。
- 我为设备状态、设备编号、价格和维修费用加入了约束，并为常用查询加入了索引。
- 我把建表语句保存到 `database/schema.sql`，并在独立测试数据库中从零执行，确认三张表均能创建成功。
- 我通过 `feature/database-schema` 分支提交并合并了数据库脚本。

## 2026-07-12：Spring Boot 连接 MySQL

- 我先写了原生 JDBC 连接测试；在没有 MySQL 驱动时，测试以 `No suitable driver` 失败。
- 我在 `pom.xml` 中加入 MySQL Connector/J，原生 JDBC 测试随后通过。
- 我再写了 Spring `DataSource` 测试；它先因为没有数据源 Bean 而失败。
- 我加入 Spring JDBC 依赖后，错误收敛为缺少数据源配置。
- 我在 `application.properties` 中配置数据库 URL，并通过 `${DB_USERNAME}` 和 `${DB_PASSWORD}` 从环境变量读取账号密码，没有把密码提交到 Git。
- 我运行了全部 4 个测试，全部通过。
- 我启动应用并真实访问 `/api/health`，确认应用在连接 MySQL 后仍正常运行。

## 2026-07-12：新增车间接口

- `POST /api/workshops` 会将车间数据持久化到数据库。
- `CreateWorkshopRequest` 用于接收创建请求，`WorkshopResponse` 用于返回创建后的数据。
- 通过 `GeneratedKeyHolder` 获取数据库自动生成的车间 ID。
- 车间名称为空或缺失时返回 400；名称重复时返回 409；带前后空格的名称会先去除空格。
- Maven 共 12 个测试全部通过，0 failures、0 errors；并已在浏览器中手动确认 UTF-8 中文数据正常。

## 2026-07-16：查询设备列表接口

- 我新增了 `GET /api/devices`，从 `devices` 表读取设备列表并返回 JSON。
- `DeviceController` 只负责 HTTP 映射，`DeviceService` 负责事务边界，`DeviceRepository` 负责 SQL 查询。
- 我使用 `DeviceResponse` 将数据库字段转换为接口字段，并保留 `BigDecimal`、`LocalDate` 等合适的 Java 类型。
- 我为“先写入设备，再通过 MockMvc 查询已持久化数据”的场景补充了集成测试。
- 设置当前终端的数据库环境变量后，Maven 共运行 13 个测试，全部通过，确认接口能够读取 MySQL 中已持久化的设备。

## 2026-07-16：设备新增、编辑、删除接口

- 新增 `POST /api/devices`、`PUT /api/devices/{id}` 和 `DELETE /api/devices/{id}`。
- 设备编号和名称为必填字段；价格不能为负数；状态限制为 `IN_USE`、`MAINTENANCE` 和 `RETIRED`。
- 重复设备编号返回 409，不存在的设备返回 404，删除成功返回 204。
- 通过 MockMvc 和 MySQL 集成测试验证了新增、编辑、删除、输入校验及不存在设备场景。

## 2026-07-16：维修记录与设备状态统计

- 新增 `POST /api/maintenance-records` 和 `GET /api/maintenance-records`。
- 维修记录必须关联已存在的设备，维修时间不能为空，维修费用不能为负数。
- 新增 `GET /api/statistics/devices/status`，按 `IN_USE`、`MAINTENANCE`、`RETIRED` 分组统计设备数量。
- 使用 MockMvc 和 MySQL 集成测试验证维修记录持久化、查询、输入校验和状态统计；全部 19 项测试通过。

## 2026-07-16：Vue 3 前端联调与设备 CRUD

- 初始化 Vue 3 前端项目。
- 使用 `ref` 管理设备列表、表单和加载状态。
- 使用 `v-model` 绑定表单输入。
- 使用 `fetch` 调用后端 REST API。
- 理解 Vite 代理与 Spring Boot 的前后端联调。
- 完成设备的查询、新增、编辑和删除。
- 能够解释 `GET`、`POST`、`PUT`、`DELETE` 的用途。
- 能够解释 Controller、Service、Repository 的职责。
- 能够解释新增或修改后为什么要重新请求设备列表。

### 遇到的问题

- 新 PowerShell 窗口不会继承 Java 和数据库环境变量。
- 后端曾因使用 Java 17 而无法运行 Java 21+ 编译的代码。
- 重新设置 `JAVA_HOME`、`DB_USERNAME` 和 `DB_PASSWORD` 后解决。

### 下一步

- 整理项目文档和启动说明。
- 检查 Git 改动。
- 提交并推送 DAY7 成果。
