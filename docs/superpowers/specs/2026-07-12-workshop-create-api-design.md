# 新增车间接口设计

## 目标

为工业设备台账管理系统增加创建车间的后端接口，使车间数据可以持久化保存到 MySQL，并可由现有列表接口查询。

## 接口

`POST /api/workshops`

请求体示例：

```json
{
  "name": "装配一车间",
  "managerName": "王工",
  "location": "一号厂房",
  "phone": "13800000000",
  "description": "负责设备装配"
}
```

`name` 必填。`managerName`、`location`、`phone` 与 `description` 可选。

成功时返回 HTTP `201 Created` 与新建车间的完整 JSON，包括数据库生成的 `id`。

## 处理流程

`WorkshopController` 接收 JSON 请求，并调用 `WorkshopService`。服务层校验车间名称；`WorkshopRepository` 使用 `JdbcTemplate` 插入 `workshops` 表，读取数据库生成的 ID，并返回 `WorkshopResponse`。

## 错误处理

- 名称为空或仅包含空白字符：HTTP `400 Bad Request`。
- 名称与已有车间重复：HTTP `409 Conflict`。
- 不在本次范围内的错误由 Spring Boot 的默认错误处理机制处理。

## 测试

- 创建包含所有字段的车间时，返回 `201`、完整响应字段和生成的 ID。
- 缺少或传入空白名称时，返回 `400`。
- 创建重复名称时，返回 `409`。
- 测试使用真实 MySQL 与 `@Transactional`，测试完成后自动回滚数据。
- 保留并运行现有健康检查、数据库连接、车间列表测试作为回归测试。

## 非目标

- 不实现前端页面。
- 不实现车间修改、删除、分页或搜索。
- 不在本次引入 JPA，继续使用 Spring JDBC 与 `JdbcTemplate`。
