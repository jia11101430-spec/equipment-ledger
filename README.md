# 工业设备台账管理系统

一个用于学习 Java 后端工程化的设备、车间与维修记录管理系统。

## 当前进度

- [X] MySQL 数据库与三张核心表
- [X] 车间管理接口：新增与查询车间
- [X] 设备管理接口：查询、新增、编辑、删除设备
- [X] 维修记录接口：新增与查询
- [X] 数据统计接口：设备状态数量
- [ ] Vue 3 前端页面
- [X] Spring Boot 健康检查接口：`GET /api/health`

## 技术栈

- 后端：Java 21、Spring Boot、Maven
- 数据库：MySQL
- 前端：Vue 3（后续接入）

## 本地数据库运行

运行数据库相关测试或启动后端前，需要确认 MySQL80 服务正在运行，并在当前 PowerShell 设置环境变量：

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$env:DB_USERNAME = "root"
$securePassword = Read-Host -Prompt "输入 MySQL root 密码" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
```

设置完成后，在项目根目录运行：

mvn -f backend/pom.xml test
mvn -f backend/pom.xml spring-boot:run

数据库账号和密码不写入 Git 仓库。
