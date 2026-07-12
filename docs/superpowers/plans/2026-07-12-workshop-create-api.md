# 新增车间接口 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 `POST /api/workshops`，把车间数据保存到 MySQL，并返回新建车间及其生成的 ID。

**Architecture:** 保持现有的 Controller -> Service -> Repository 分层。Controller 接收 JSON，Service 负责名称校验，Repository 用 `JdbcTemplate` 写入 `workshops` 表。`WorkshopExceptionHandler` 将空名称和数据库唯一约束冲突转换为明确的 HTTP 响应。

**Tech Stack:** Java 21、Spring Boot 3.5、Spring MVC、Spring JDBC (`JdbcTemplate`)、MySQL、JUnit 5、MockMvc、Maven。

---

### Task 1: 创建成功的接口闭环

**Files:**
- Create: `backend/src/main/java/com/jia/equipmentledger/workshop/CreateWorkshopRequest.java`
- Create: `backend/src/test/java/com/jia/equipmentledger/workshop/WorkshopCreateApiTest.java`
- Modify: `backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopController.java`
- Modify: `backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopService.java`
- Modify: `backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopRepository.java`

- [ ] **Step 1: 写创建成功的失败测试**

```java
package com.jia.equipmentledger.workshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkshopCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsWorkshopAndReturnsIt() throws Exception {
        String name = "创建接口测试车间-" + UUID.randomUUID().toString().substring(0, 8);
        String requestBody = """
                {
                  "name": "%s",
                  "managerName": "王工",
                  "location": "一号厂房",
                  "phone": "13800000000",
                  "description": "测试创建车间"
                }
                """.formatted(name);

        mockMvc.perform(post("/api/workshops")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.managerName").value("王工"))
                .andExpect(jsonPath("$.location").value("一号厂房"))
                .andExpect(jsonPath("$.phone").value("13800000000"))
                .andExpect(jsonPath("$.description").value("测试创建车间"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workshops WHERE name = ?", Integer.class, name);
        assertThat(count).isEqualTo(1);
    }
}
```

- [ ] **Step 2: 运行测试，确认它因为缺少 POST 映射而失败**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=WorkshopCreateApiTest
```

Expected: `Status expected:<201> but was:<405>`。现有 Controller 只有 `GET /api/workshops`，所以 POST 会得到 `405 Method Not Allowed`。

- [ ] **Step 3: 实现最小创建功能**

创建 `CreateWorkshopRequest.java`：

```java
package com.jia.equipmentledger.workshop;

public record CreateWorkshopRequest(
        String name,
        String managerName,
        String location,
        String phone,
        String description
) {
}
```

将 `WorkshopRepository.java` 更新为：

```java
package com.jia.equipmentledger.workshop;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WorkshopRepository {

    private final JdbcTemplate jdbcTemplate;

    public WorkshopRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<WorkshopResponse> findAll() {
        String sql = """
                SELECT id, name, manager_name, location, phone, description
                FROM workshops
                ORDER BY id ASC
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new WorkshopResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("manager_name"),
                resultSet.getString("location"),
                resultSet.getString("phone"),
                resultSet.getString("description")
        ));
    }

    public WorkshopResponse create(CreateWorkshopRequest request) {
        String sql = """
                INSERT INTO workshops (name, manager_name, location, phone, description)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.name());
            statement.setString(2, request.managerName());
            statement.setString(3, request.location());
            statement.setString(4, request.phone());
            statement.setString(5, request.description());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建车间后未获取到 ID");
        }

        return new WorkshopResponse(
                key.longValue(),
                request.name(),
                request.managerName(),
                request.location(),
                request.phone(),
                request.description()
        );
    }
}
```

将 `WorkshopService.java` 更新为：

```java
package com.jia.equipmentledger.workshop;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkshopResponse> listWorkshops() {
        return workshopRepository.findAll();
    }

    @Transactional
    public WorkshopResponse createWorkshop(CreateWorkshopRequest request) {
        return workshopRepository.create(request);
    }
}
```

将 `WorkshopController.java` 更新为：

```java
package com.jia.equipmentledger.workshop;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @GetMapping
    public List<WorkshopResponse> listWorkshops() {
        return workshopService.listWorkshops();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkshopResponse createWorkshop(@RequestBody CreateWorkshopRequest request) {
        return workshopService.createWorkshop(request);
    }
}
```

- [ ] **Step 4: 运行测试，确认创建成功**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=WorkshopCreateApiTest
```

Expected: `Tests run: 1, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

### Task 2: 增加名称校验与重名冲突响应

**Files:**
- Create: `backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopExceptionHandler.java`
- Modify: `backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopService.java`
- Modify: `backend/src/test/java/com/jia/equipmentledger/workshop/WorkshopCreateApiTest.java`

- [ ] **Step 1: 在测试中加入空名称和重名场景**

在 `WorkshopCreateApiTest.java` 中加入以下两个测试方法：

```java
    @Test
    void rejectsBlankWorkshopName() throws Exception {
        mockMvc.perform(post("/api/workshops")
                        .contentType("application/json")
                        .content("{\"name\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("车间名称不能为空"));
    }

    @Test
    void rejectsDuplicateWorkshopName() throws Exception {
        String name = "重名测试车间-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("INSERT INTO workshops (name) VALUES (?)", name);

        mockMvc.perform(post("/api/workshops")
                        .contentType("application/json")
                        .content("{\"name\": \"%s\"}".formatted(name)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("车间名称已存在"));
    }
```

- [ ] **Step 2: 运行测试，确认新场景失败**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=WorkshopCreateApiTest
```

Expected: 空名称目前会错误地得到 `201`；重名目前会触发未转换的数据库异常而不是 `409`。

- [ ] **Step 3: 添加最小校验和异常转换**

将 `WorkshopService.java` 的 `createWorkshop` 方法替换为：

```java
    @Transactional
    public WorkshopResponse createWorkshop(CreateWorkshopRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("车间名称不能为空");
        }

        CreateWorkshopRequest normalizedRequest = new CreateWorkshopRequest(
                request.name().trim(),
                request.managerName(),
                request.location(),
                request.phone(),
                request.description()
        );
        return workshopRepository.create(normalizedRequest);
    }
```

创建 `WorkshopExceptionHandler.java`：

```java
package com.jia.equipmentledger.workshop;

import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WorkshopController.class)
public class WorkshopExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateName(DuplicateKeyException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "车间名称已存在"));
    }
}
```

- [ ] **Step 4: 运行测试，确认三个创建场景都通过**

Run:

```powershell
mvn -f backend/pom.xml test -Dtest=WorkshopCreateApiTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

### Task 3: 回归验证、手动验证与提交

**Files:**
- Modify: `docs/learning-log.md`
- Verify: `backend/src/test/java/com/jia/equipmentledger/workshop/WorkshopCreateApiTest.java`

- [ ] **Step 1: 运行完整回归测试**

Run:

```powershell
mvn -f backend/pom.xml test
```

Expected: 8 个测试通过，`Failures: 0, Errors: 0`，并显示 `BUILD SUCCESS`。

- [ ] **Step 2: 启动应用并手动创建一条车间数据**

在一个已设置 `DB_USERNAME` 和 `DB_PASSWORD` 的终端运行：

```powershell
mvn -f backend/pom.xml spring-boot:run
```

看到 `Tomcat started on port 8080` 后，在第二个终端运行：

```powershell
$body = @{
  name = "手动验证车间"
  managerName = "赵工"
  location = "三号厂房"
  phone = "13900000000"
  description = "用于验证新增车间接口"
} | ConvertTo-Json

Invoke-WebRequest http://localhost:8080/api/workshops `
  -Method Post `
  -ContentType "application/json" `
  -Body $body | Select-Object StatusCode, Content

Invoke-WebRequest http://localhost:8080/api/workshops | Select-Object StatusCode, Content
```

Expected: POST 返回 `201` 和包含 `id` 的 JSON；随后 GET 返回 `200` 且列表包含“手动验证车间”。手动验证后在服务终端按 `Ctrl + C` 停止应用。

- [ ] **Step 3: 在学习日志记录本次闭环**

在 `docs/learning-log.md` 末尾增加：

```markdown
## 2026-07-12：新增车间接口

- 我通过 `POST /api/workshops` 将车间 JSON 数据保存到 MySQL。
- 我使用 `CreateWorkshopRequest` 区分“客户端提交的数据”和 `WorkshopResponse`。
- 我使用 JDBC 的 `GeneratedKeyHolder` 获取数据库自动生成的车间 ID。
- 我让空名称返回 HTTP 400，让重复车间名称返回 HTTP 409。
- 我先运行创建接口测试，再运行全部回归测试，并通过真实 HTTP 请求验证了新增和查询。
```

- [ ] **Step 4: 检查、提交并推送功能分支**

Run:

```powershell
git status
git add `
  backend/src/main/java/com/jia/equipmentledger/workshop/CreateWorkshopRequest.java `
  backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopController.java `
  backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopExceptionHandler.java `
  backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopRepository.java `
  backend/src/main/java/com/jia/equipmentledger/workshop/WorkshopService.java `
  backend/src/test/java/com/jia/equipmentledger/workshop/WorkshopCreateApiTest.java `
  docs/learning-log.md
git diff --staged
git commit -m "feat: add workshop creation API"
git push -u origin feature/workshop-create-api
git status -sb
```

Expected: 功能分支与 `origin/feature/workshop-create-api` 同步。随后按既有流程合并到 `main`，验证合并结果后删除功能分支和 worktree。
