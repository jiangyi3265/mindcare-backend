# MindCare Backend

MindCare 心理健康服务项目的后端基础服务，为管理后台与用户端提供统一鉴权、系统管理和业务 API 扩展能力。

## 项目简介

`mindcare-backend` 基于 RuoYi 前后端分离版整理为独立的 Spring Boot 多模块工程。除用户、角色、菜单、日志、JWT 鉴权等通用能力外，当前代码已实现 MindCare 三端业务接口：量表、课程和活动内容发布，用户端匿名安装身份，测评、咨询、课程进度、活动报名与留言记录同步，以及后台运营概览和状态处理。

用户端通过随机客户端标识与独立凭证建立同步身份；数据库只保存凭证的 BCrypt 摘要。管理接口继续使用 RuoYi 登录与权限体系，公开用户端接口只开放在 `/app/mindcare/**`，并在服务层校验客户端凭证和业务字段。

## 技术栈

- Java 8、Maven 多模块工程
- Spring Boot 2.5.15、Spring Framework 5.3.39
- Spring Security 5.7.14、JWT
- MyBatis、PageHelper
- MySQL、Druid 1.2.27
- Redis、Lettuce
- Swagger / Springfox 3.0.0
- Fastjson2、Apache POI、Logback

## 关联仓库

| 项目 | 说明 | GitHub |
| --- | --- | --- |
| mindcare-backend | 后端服务 | [mindcare-backend](https://github.com/jiangyi3265/mindcare-backend) |
| mindcare-admin | 管理后台 | [mindcare-admin](https://github.com/jiangyi3265/mindcare-admin) |
| mindcare-app | 用户端 | [mindcare-app](https://github.com/jiangyi3265/mindcare-app) |

## 快速启动

准备 JDK 8、Maven、MySQL 8 和 Redis。创建 `mindcare` 数据库后依次执行：

```bash
mysql -u root -p mindcare < sql/ry_20250522.sql
mysql -u root -p mindcare < sql/mindcare.sql
```

随后设置运行所需环境变量：

```bash
export DB_URL='jdbc:mysql://localhost:3306/mindcare?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai'
export DB_USERNAME='root'
export DB_PASSWORD='请填写本地数据库密码'
export TOKEN_SECRET='请填写足够长的随机密钥'
export ADMIN_BOOTSTRAP_PASSWORD='首次部署使用的管理员密码'
```

Windows PowerShell 可使用 `$env:DB_PASSWORD='...'` 等同名变量。然后构建并运行：

```bash
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

默认端口为 `8080`。Redis 默认连接 `localhost:6379`；如有密码，请设置 `REDIS_PASSWORD`。Druid 控制台默认关闭，可通过 `DRUID_CONSOLE_ENABLED`、`DRUID_USERNAME` 和 `DRUID_PASSWORD` 显式启用并配置。

初始化脚本不会提供可公开使用的默认管理员密码：种子管理员使用随机且不可恢复的密码哈希。首次部署时临时设置 `ADMIN_BOOTSTRAP_PASSWORD`，应用启动后即可用 `admin` 和该密码登录；完成首次登录并修改密码后，应移除这个环境变量，避免后续启动重复覆盖密码。

> 仓库不包含任何生产密码或 Token。生产部署应由环境变量或专用密钥管理服务注入敏感配置。

## 项目结构

```text
ruoyi-admin/       应用入口、Web 控制器和运行配置
ruoyi-framework/   安全、缓存、Web 与数据源等框架配置
ruoyi-system/      用户、角色、菜单、字典等系统服务
ruoyi-common/      公共模型、工具、注解和通用依赖
sql/               MySQL 初始化脚本
bin/               启停辅助脚本
```

`ruoyi-quartz` 与 `ruoyi-generator` 源码仍保留在目录中，但当前父工程未启用这两个模块。

MindCare 业务代码位于 `ruoyi-system` 的 `Mindcare*` 领域、Mapper 与服务中，管理/用户端控制器位于 `ruoyi-admin/src/main/java/com/ruoyi/web/controller/mindcare/`。`sql/mindcare.sql` 包含业务表、初始量表/课程/活动和动态菜单权限。

## 三端接口

- 管理端：`/mindcare/dashboard`、`/mindcare/content/**`、`/mindcare/record/**`、`/mindcare/client/**`
- 用户端：`/app/mindcare/client/register`、`/app/mindcare/bootstrap`、`/app/mindcare/records`
- 用户端写操作需要 `X-Client-Id` 与 `X-Client-Token` 请求头；正式生产可在此基础上替换为微信登录或统一账号体系。

## 简历描述示例

参与 MindCare 心理健康服务后端建设，基于 Spring Boot、Spring Security、MyBatis、MySQL 与 Redis 实现内容发布、匿名终端身份、测评/预约/学习/活动记录同步及运营处理接口，打通管理端与多端用户应用。

## 开源说明

本项目基于 RuoYi 二次整理，原项目版权与许可信息见 [LICENSE](./LICENSE)。
