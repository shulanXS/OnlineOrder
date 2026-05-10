# OnlineOrder - 在线订餐系统

> 基于 Spring Boot + React 的前后端分离在线订餐脚手架项目。

---

## 项目概览

OnlineOrder 是一个现代化的在线订餐平台，具备完整的用户认证、餐厅浏览、购物车管理和订单处理功能。项目采用主流技术栈设计，可作为快速二次开发的基础脚手架。

### 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3 | Java 21, Web MVC |
| 安全 | Spring Security + JWT | 无状态 Token 认证 |
| 数据访问 | Spring Data JDBC | 轻量级 ORM |
| 数据库 | PostgreSQL | 主数据存储 |
| 迁移 | Flyway | 版本化数据库迁移 |
| API 文档 | SpringDoc OpenAPI | Swagger UI |
| 前端框架 | React 18 | 函数式组件 |
| 构建工具 | Vite 5 | 快速开发体验 |
| 路由 | React Router v6 | 声明式路由 |
| 状态管理 | Zustand | 认证状态管理 |
| 数据获取 | TanStack React Query | 服务端状态管理 |
| UI 组件 | Ant Design 4 | 企业级 UI |

---

## 快速开始

### 前置条件

- JDK 21+
- Node.js 18+
- PostgreSQL 14+
- Gradle 8+

### 1. 克隆并安装依赖

```bash
# 后端：下载依赖
cd backend && ./gradlew dependencies --quiet

# 前端：安装 npm 依赖
cd ../frontend && npm install
```

### 2. 配置数据库

```bash
# 创建 PostgreSQL 数据库
createdb onlineorder

# 复制环境变量模板
cd backend
cp .env.example .env
# 编辑 .env，填入数据库密码和 JWT_SECRET
```

### 3. 启动后端

```bash
cd backend

# 开发环境（使用 database-init.sql 初始化数据）
./gradlew bootRun

# 或启用 Flyway 迁移（生产推荐）
FLYWAY_ENABLED=true ./gradlew bootRun
```

后端启动在 http://localhost:8093

### 4. 启动前端

```bash
cd frontend
npm run dev
```

前端启动在 http://localhost:5173

### 5. 访问应用

- 打开 http://localhost:5173
- 注册账号并登录
- 浏览餐厅、添加购物车、下单

---

## 项目结构

```
OnlineOrder/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/cwj/onlineorder/
│   │   ├── OnlineOrderApplication.java   # 启动类
│   │   ├── AppConfig.java               # 安全与 CORS 配置入口
│   │   ├── DevRunner.java               # 开发环境数据初始化
│   │   │
│   │   ├── controller/                  # REST 控制器层
│   │   │   ├── AuthController.java      # 登录 / 注册 / 当前用户
│   │   │   ├── CartController.java      # 购物车增删改查 / 结账
│   │   │   ├── MenuController.java      # 餐厅与菜品查询
│   │   │   ├── OrderController.java     # 订单历史 / 状态更新
│   │   │   ├── HealthController.java    # 健康检查探活
│   │   │   └── GlobalExceptionHandler.java  # 统一异常处理
│   │   │
│   │   ├── service/                     # 业务逻辑层
│   │   │   ├── CustomerService.java     # 用户注册 / 查询
│   │   │   ├── CartService.java         # 购物车业务
│   │   │   ├── MenuItemService.java     # 菜品业务
│   │   │   ├── OrderService.java        # 订单业务
│   │   │   └── RestaurantService.java  # 餐厅业务
│   │   │
│   │   ├── repository/                 # 数据访问层 (Spring Data JDBC)
│   │   │   ├── CustomerRepository.java
│   │   │   ├── CartRepository.java
│   │   │   ├── CartItemRepository.java
│   │   │   ├── MenuItemRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── OrderDetailRepository.java
│   │   │   └── RestaurantRepository.java
│   │   │
│   │   ├── entity/                     # 领域实体 (Java Record)
│   │   │   ├── CustomerEntity.java
│   │   │   ├── CartEntity.java
│   │   │   ├── CartItemEntity.java
│   │   │   ├── MenuItemEntity.java
│   │   │   ├── OrderEntity.java
│   │   │   ├── OrderDetailEntity.java
│   │   │   └── RestaurantEntity.java
│   │   │
│   │   ├── model/                      # DTO / 请求体
│   │   │   ├── ApiResult.java          # 统一响应包装器
│   │   │   ├── CustomerDto.java
│   │   │   ├── CartDto.java / CartItemDto.java
│   │   │   ├── OrderDto.java / OrderDetailDto.java
│   │   │   ├── RestaurantDto.java / MenuItemDto.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── LoginRequest.java
│   │   │   └── RegisterBody.java
│   │   │
│   │   ├── security/                   # JWT 安全组件
│   │   │   ├── JwtAuthFilter.java      # Token 解析过滤器
│   │   │   ├── JwtTokenProvider.java   # Token 生成与验证
│   │   │   └── CustomUserDetailsService.java
│   │   │
│   │   └── exception/                  # 自定义异常
│   │       ├── CustomerNotFoundException.java
│   │       └── MenuItemNotFoundException.java
│   │
│   ├── src/main/resources/
│   │   ├── application.yaml            # 主配置文件
│   │   └── db/migration/               # Flyway 迁移脚本
│   │       ├── V1__initial_schema.sql
│   │       ├── V2__add_orders_status.sql
│   │       └── V3__rename_order_items_to_cart_items.sql
│   │
│   ├── src/test/java/com/cwj/onlineorder/  # 单元测试
│   │   ├── CustomerServiceTest.java
│   │   ├── CartServiceTest.java
│   │   └── OrderServiceTest.java
│   │
│   ├── build.gradle
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── .env.example
│
└── frontend/                           # React 前端 (Vite)
    ├── src/
    │   ├── main.jsx                    # React 入口
    │   ├── App.jsx                     # 路由与错误边界
    │   │
    │   ├── api/                        # API 封装层 (Axios)
    │   │   ├── apiClient.js            # 统一配置、拦截器、错误处理
    │   │   ├── authApi.js              # 认证接口
    │   │   ├── cartApi.js              # 购物车接口
    │   │   ├── restaurantApi.js         # 餐厅接口
    │   │   └── orderApi.js             # 订单接口
    │   │
    │   ├── hooks/                      # React Query Hooks
    │   │   ├── useCart.js              # 购物车增删改查 + 乐观更新
    │   │   └── useOrders.js            # 订单历史查询
    │   │
    │   ├── stores/                     # Zustand 状态管理
    │   │   └── authStore.js            # 认证状态（Token / 用户信息）
    │   │
    │   ├── components/                 # 通用组件
    │   │   ├── Layout.jsx              # 全局布局（Header + Content）
    │   │   ├── ProtectedRoute.jsx       # 路由鉴权守卫
    │   │   └── ErrorBoundary.jsx       # React 错误边界
    │   │
    │   └── pages/                      # 页面组件
    │       ├── LoginPage.jsx
    │       ├── RegisterPage.jsx
    │       ├── RestaurantPage.jsx
    │       ├── CartPage.jsx
    │       └── OrdersPage.jsx
    │
    ├── vite.config.js
    ├── eslint.config.js
    └── package.json
```

---

## API 文档

启动后端后访问：

- Swagger UI: http://localhost:8093/swagger-ui.html
- OpenAPI JSON: http://localhost:8093/v3/api-docs

### 接口列表

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /auth/login | 用户登录 | 否 |
| POST | /auth/register | 用户注册 | 否 |
| GET | /auth/me | 当前登录用户 | 是 |
| GET | /health | 健康检查 | 否 |
| GET | /restaurants/menu | 所有餐厅及菜品 | 否 |
| GET | /restaurant/{id}/menu | 单个餐厅菜品 | 否 |
| GET | /cart | 获取购物车 | 是 |
| POST | /cart | 添加商品到购物车 | 是 |
| POST | /cart/items/{menuItemId} | 更新商品数量 | 是 |
| POST | /cart/checkout | 结账生成订单 | 是 |
| GET | /orders | 订单历史 | 是 |
| PATCH | /orders/{orderId}/status | 更新订单状态 | 是 |

### 统一响应格式

成功响应：

```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

错误响应：

```json
{
  "success": false,
  "data": null,
  "error": {
    "status": 400,
    "code": "BAD_REQUEST",
    "message": "菜品不存在"
  }
}
```

### 认证方式

1. 调用 `POST /auth/login` 或 `POST /auth/register` 获取 JWT Token
2. 在请求头中携带：`Authorization: Bearer <token>`
3. Token 有效期 24 小时，过期后前端自动提示重新登录

---

## 环境变量说明

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| DATABASE_HOST | 是 | localhost | 数据库主机 |
| DATABASE_PORT | 是 | 5432 | 数据库端口 |
| DATABASE_NAME | 是 | onlineorder | 数据库名 |
| DATABASE_USERNAME | 是 | - | 数据库用户名 |
| DATABASE_PASSWORD | 是 | - | 数据库密码 |
| JWT_SECRET | 是 | - | JWT 签名密钥（至少 32 字节） |
| CORS_ORIGINS | 否 | http://localhost:5173 | 允许的跨域来源（逗号分隔） |
| FLYWAY_ENABLED | 否 | false | 是否启用 Flyway 迁移 |
| SERVER_PORT | 否 | 8093 | 服务端口 |
| LOG_LEVEL_ROOT | 否 | INFO | 日志级别 |

---

## 数据库迁移 (Flyway)

```bash
# 启用 Flyway 并运行迁移
FLYWAY_ENABLED=true ./gradlew bootRun

# 手动触发迁移
./gradlew flywayMigrate
```

迁移脚本位于 `backend/src/main/resources/db/migration/`。

---

## 测试

```bash
cd backend

# 运行单元测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "*CartServiceTest*"

# 运行测试并生成覆盖率报告
./gradlew test jacocoTestReport
```

> 注意：`OnlineOrderApplicationTests` 需要运行中的 PostgreSQL 数据库，已通过 `@Disabled` 注释禁用。

---

## Docker 部署

```bash
# 构建并启动
cd backend
docker-compose up -d
```

---

## 开发指南

### 添加新的 API 接口

1. **Entity**：在 `entity/` 中定义新的领域实体
2. **Repository**：在 `repository/` 中定义数据访问接口
3. **Service**：在 `service/` 中编写业务逻辑
4. **Controller**：在 `controller/` 中定义 REST 端点
5. **DTO**：在 `model/` 中定义请求/响应数据模型
6. **前端**：在 `api/` 中封装接口，在 `hooks/` 中创建 React Query Hook

### 添加新的数据库表

1. 在 `db/migration/` 中创建新的 Flyway 脚本 (`V{n}__description.sql`)
2. 运行 `FLYWAY_ENABLED=true ./gradlew bootRun` 应用迁移

### 前端开发约定

- API 调用统一通过 `api/` 下的模块，不在组件内直接使用 axios
- 服务端状态使用 React Query (`hooks/`)，Zustand 仅用于全局客户端状态
- 所有页面组件放在 `pages/`，可复用组件放在 `components/`

---

## 许可证

MIT License
