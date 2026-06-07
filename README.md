# 音域 Yinyu

## 项目简介

音域（`yinyu`）是一个仿企鹅音乐的个人全栈项目，采用前后端分离架构，包含：

- 一个面向普通用户的音乐网站前端
- 一个面向运营管理的后台前端
- 一个基于 Spring Boot 的 Java 后端

项目围绕音乐平台常见场景进行实现，包括首页推荐、歌单、歌手、歌曲播放、排行榜、用户注册登录、个人音乐库，以及后台内容管理等能力。

## 项目结构

```text
music-java
├─ README.md
├─ yinyu.sql                     # 数据库初始化脚本
├─ yinyu-front                   # 前端工程
│  ├─ yinyu-front-web            # 用户端前端
│  └─ yinyu-front-admin          # 管理后台前端
└─ yinyu-java                    # 后端工程
   ├─ yinyu-common               # 公共模块：实体、DTO、VO、Mapper、Service 等
   ├─ yinyu-web                  # 用户端接口服务
   └─ yinyu-admin                # 管理端接口服务
```

## 功能概览

### 用户端

- 首页推荐展示
- 歌单列表与歌单详情
- 歌手列表与歌手详情
- 排行榜浏览
- 歌曲搜索
- 在线播放音频
- 用户注册、登录、退出登录
- 个人资料维护、头像上传、密码修改
- 我的音乐：收藏歌单、点赞歌曲、播放记录

### 管理后台

- 管理员验证码登录
- 仪表盘数据总览
- 歌曲管理
- 歌单管理
- 歌手管理
- 首页推荐位管理
- 系统字典管理
- 用户管理
- 图片/音频文件上传

## 技术栈

### 前端

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios
- Element Plus
- JavaScript
- Sass（用户端使用 `sass-embedded`）

### 后端

- Java 21
- Spring Boot 3.5.4
- Spring MVC
- MyBatis Spring Boot Starter 3.0.5
- MySQL
- Lombok
- Easy Captcha

## 运行环境

建议使用以下环境版本：

- JDK 21
- Maven 3.9+
- Node.js 20.19+ 或 22.12+
- MySQL 5.7+ 或 8.x

## 默认端口

### 前端

- 用户端：`http://127.0.0.1:3001`
- 管理后台：`http://127.0.0.1:3000`

### 后端

- 用户端接口：`http://127.0.0.1:6060/api`
- 管理端接口：`http://127.0.0.1:6061/api`

### 前端代理关系

- `yinyu-front-web` 将 `/api` 代理到 `http://127.0.0.1:6060`
- `yinyu-front-admin` 将 `/api` 代理到 `http://127.0.0.1:6061`

## 数据库说明

项目默认数据库名为 `yinyu`，配置文件中的连接信息如下：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yinyu?allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf8
    username: root
    password: root
```

初始化步骤：

1. 在 MySQL 中创建数据库 `yinyu`
2. 执行根目录下的 `yinyu.sql`
3. 如本地账号密码不同，请修改：
   - `yinyu-java/yinyu-web/src/main/resources/application.yml`
   - `yinyu-java/yinyu-admin/src/main/resources/application.yml`

## 文件上传说明

项目使用本地文件系统存储上传资源，后端配置中默认目录为：

```yml
project:
  folder: C:/webser/
```

实际上传目录为：

```text
C:/webser/uploads/
```

说明：

- 上传的图片、音频会按分类和月份自动分目录保存
- 请先确认本地存在可写目录，或按需修改 `project.folder`
- 文件访问统一通过 `/api/files/**` 提供

## 默认账号

### 管理后台

后台配置文件内置了默认管理员账号：

- 用户名：`admin`
- 密码：`123456`

对应配置位置：

- `yinyu-java/yinyu-admin/src/main/resources/application.yml`

### 普通用户

- 普通用户默认不提供固定账号
- 需要通过前台注册后登录使用

## 启动方式

推荐按照以下顺序启动项目。

### 1. 启动后端

在 `yinyu-java` 目录执行：

```bash
mvn clean install
```

然后分别启动两个后端服务：

```bash
# 启动用户端接口
mvn -pl yinyu-web spring-boot:run
```

```bash
# 启动管理端接口
mvn -pl yinyu-admin spring-boot:run
```

也可以直接在 IDE 中运行以下启动类：

- `com.yinyu.YinyuWebApplication`
- `com.yinyu.YinyuAdminApplication`

### 2. 启动用户端前端

在 `yinyu-front/yinyu-front-web` 目录执行：

```bash
npm install
npm run dev
```

启动后访问：

- `http://127.0.0.1:3001`

### 3. 启动管理后台前端

在 `yinyu-front/yinyu-front-admin` 目录执行：

```bash
npm install
npm run dev
```

启动后访问：

- `http://127.0.0.1:3000`

## 接口模块说明

### `yinyu-web`

面向用户端前端，主要提供：

- 首页数据
- 歌曲、歌单、歌手、排行榜查询
- 用户注册登录
- 用户音乐库相关操作
- 文件访问与音频流式播放

### `yinyu-admin`

面向后台前端，主要提供：

- 管理员登录与会话校验
- 仪表盘统计
- 歌曲、歌单、歌手增删改查
- 首页推荐管理
- 字典管理
- 用户管理
- 文件上传

### `yinyu-common`

公共模块，主要包含：

- 实体对象与枚举
- DTO / VO
- Mapper 与 XML
- Service 接口与实现
- 全局异常处理
- 通用响应结构

## 开发提示

- 用户端请求默认开启 `withCredentials: true`，如跨域部署需注意 Cookie / Session 配置
- 后台登录依赖 Session，会话失效后前端会自动跳转登录页
- 用户端歌曲播放使用 HTTP Range，支持音频分段加载
- 当前仓库根目录已经包含数据库脚本，适合本地快速初始化

