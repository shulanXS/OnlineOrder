# OnlineOrder - 新手向完全指南

> 本指南面向 Java 和 Spring Boot 初次开发者，详细讲解这个项目的每一行代码、每一个概念、每一处设计决策。

**目录**

1. [项目概览](#1-项目概览)
2. [技术栈解析](#2-技术栈解析)
3. [开发环境搭建](#3-开发环境搭建)
4. [项目结构全览](#4-项目结构全览)
5. [分层架构详解](#5-分层架构详解)
6. [数据库设计](#6-数据库设计)
7. [实体层 (Entity)](#7-实体层-entity)
8. [数据传输对象 (DTO)](#8-数据传输对象-dto)
9. [仓储层 (Repository)](#9-仓储层-repository)
10. [服务层 (Service)](#10-服务层-service)
11. [控制器层 (Controller)](#11-控制器层-controller)
12. [安全配置 (AppConfig)](#12-安全配置-appconfig)
13. [应用配置 (application.yaml)](#13-应用配置-applicationyaml)
14. [REST API 参考](#14-rest-api-参考)
15. [缓存机制](#15-缓存机制)
16. [密码安全](#16-密码安全)
17. [单元测试](#17-单元测试)
18. [常见问题与调试](#18-常见问题与调试)
19. [如何扩展这个项目](#19-如何扩展这个项目)

---

## 1. 项目概览

### 1.1 这是什么项目？

**OnlineOrder** 是一个**餐厅外卖点餐平台的 REST API 后端**。用户可以：

- 注册账号并登录
- 浏览餐厅列表和菜单
- 将菜品加入购物车
- 结账（清空购物车）

### 1.2 项目属性

| 属性 | 值 |
|------|-----|
| 项目名称 | OnlineOrder |
| 应用类型 | REST API 后端（无前端页面） |
| 数据库 | PostgreSQL |
| 认证方式 | Spring Security（BCrypt 密码 + Session） |
| 缓存 | Caffeine（内存缓存） |
| 构建工具 | Gradle |
| Java 版本 | 21 |

### 1.3 快速理解

```
用户 (浏览器/App)
    │
    │  HTTP 请求 (JSON)
    ▼
┌─────────────────────────┐
│   Spring Boot 应用       │
│   运行在端口 8093        │
│                         │
│  ┌─────────────────┐    │
│  │  Controller 层   │ ◄── HTTP 路由 (GET /cart, POST /cart)
│  └────────┬────────┘    │
│           ▼              │
│  ┌─────────────────┐    │
│  │  Service 层      │ ◄── 业务逻辑 (密码加密, 购物车计算)
│  └────────┬────────┘    │
│           ▼              │
│  ┌─────────────────┐    │
│  │  Repository 层   │ ◄── 数据库查询 (SQL 自动生成)
│  └────────┬────────┘    │
│           ▼              │
│  ┌─────────────────┐    │
│  │  PostgreSQL DB  │    │
│  └─────────────────┘    │
└─────────────────────────┘
```

---

## 2. 技术栈解析

### 2.1 Spring Boot 是什么？

**Spring Boot** 是 Spring 框架的"一键启动"版本。传统 Spring 项目需要大量配置文件，Spring Boot 通过"约定大于配置"将这一切简化。

```java
@SpringBootApplication
public class OnlineOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineOrderApplication.class, args); // 一行启动整个应用
    }
}
```

这相当于传统 Spring 的：
- `web.xml` 配置 DispatcherServlet
- `applicationContext.xml` 配置数据源、事务管理器
- `spring-mvc.xml` 配置注解驱动、视图解析器
- 打包成 WAR 部署到 Tomcat

**全部用一行 `SpringApplication.run()` 替代了。**

### 2.2 Spring Data JDBC vs JPA

| | Spring Data JDBC | Spring Data JPA |
|--|-----------------|-----------------|
| 底层 | JDBC（手写 SQL 映射） | Hibernate（JPA 实现） |
| 实体关系 | 手动处理 | `@ManyToOne` 等注解自动处理 |
| 性能 | 轻量，快速 | 较重，功能多 |
| SQL | 接近原生 SQL | HQL/JPQL（对象查询语言） |
| 一级缓存 | 无 | 有（会话级） |

本项目用 **Spring Data JDBC**，因为它更轻量、直接。对于中小型项目，JDBC 的简单性反而是优势。

### 2.3 Java Record（记录类型）

Java 16 引入的 `record` 是一种特殊的类，专门用于存放不可变数据：

```java
// 传统类：需要手写构造器、getter、equals、hashCode、toString
public class MenuItemEntity {
    private Long id;
    private String name;
    public MenuItemEntity(Long id, String name) { this.id = id; this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
    // ... 还有 equals, hashCode, toString 要写
}

// Record：编译器自动生成所有这些
public record MenuItemEntity(Long id, String name) {}

// 使用
var item = new MenuItemEntity(1L, "Whopper");
// 自动生成：构造器、getId()、getName()、equals()、hashCode()、toString()
// 访问方式：item.id()、item.name()（不是 getId()、getName()！）
```

**Record 的特点：**
- 所有字段自动 `final`（不可变）
- 自动生成构造器、访问器、equals、hashCode、toString
- 不能继承其他类
- 适合 DTO、Entity、参数对象

### 2.4 Spring Security 认证流程

```
登录请求 POST /login
    │
    ▼
Spring Security FilterChain
    │
    ▼
UsernamePasswordAuthenticationFilter
    │  提取 username (=email) 和 password
    ▼
JdbcUserDetailsManager
    │
    ├─► 查询 customers 表（密码哈希）
    │      SQL: SELECT email, password, enabled FROM customers WHERE email = ?
    │
    ▼
BCrypt 密码比对
    │  将用户输入的密码哈希后，与数据库存储的哈希比对
    │  BCrypt 会自动提取数据库哈希中的 salt 并使用相同的盐哈希输入密码
    │
    ▼
AuthenticationSuccessHandler
    │  返回 HTTP 200 OK（而非重定向）
    │
    ▼
用户登录成功，后续请求携带 Session Cookie
```

### 2.5 Gradle vs Maven

| | Gradle | Maven |
|--|--------|-------|
| 配置语言 | Groovy DSL 或 Kotlin DSL | XML |
| 依赖管理 | 智能传递依赖图 | 扁平传递 |
| 速度 | 更快（增量构建、守护进程） | 较慢 |
| 本项目 | `build.gradle` | — |

`build.gradle` 关键部分：

```groovy
plugins {
    id 'org.springframework.boot' version '4.0.5'  // Spring Boot 插件（打 JAR 包）
    id 'io.spring.dependency-management' version '1.1.7'  // 版本管理插件
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'  // 实现依赖
    runtimeOnly 'org.postgresql:postgresql:42.7.7'  // 仅运行时需要
    testImplementation 'org.junit.jupiter:junit-jupiter'  // 仅测试需要
}
```

---

## 3. 开发环境搭建

### 3.1 依赖项

- **Java 21**（必需，`build.gradle` 中 `toolchain.languageVersion = JavaLanguageVersion.of(21)`）
- **Docker Desktop**（运行 PostgreSQL 数据库）
- **IntelliJ IDEA**（推荐，Ultimate 或 Community 均可）
- **Gradle 9.4.1**（通过 Wrapper 自动下载，无需手动安装）

### 3.2 启动数据库

```bash
# 在项目根目录运行
docker-compose up -d

# 验证容器运行
docker ps
# 应该看到 postgres:15.2-alpine 容器在端口 5432
```

这会启动一个 PostgreSQL 15.2 容器，数据库名为 `onlineorder`，密码为 `secret`。

### 3.3 启动应用

```bash
# 方式一：使用 Gradle Wrapper（推荐，自动下载 Gradle）
./gradlew bootRun

# 方式二：先构建，再运行 JAR
./gradlew build
java -jar build/libs/OnlineOrder-0.0.1-SNAPSHOT.jar
```

应用启动后运行在 `http://localhost:8093`。

### 3.4 启动时发生的事

```
SpringApplication.run()
    │
    ▼
1. 创建 ApplicationContext（Spring IOC 容器）
    │
    ▼
2. 扫描并注册所有 Bean
    │  @ComponentScan 从 com.cwj.onlineorder 包开始
    │  找到：@Service (4个), @Repository (5个), @Controller (3个), @Component (1个)
    │
    ▼
3. 自动配置
    │  Spring Boot 根据 classpath 自动配置：
    │  - DataSource (来自 application.yaml)
    │  - SecurityFilterChain (来自 AppConfig.java)
    │  - Jackson (JSON 序列化)
    │  - Caffeine 缓存
    │
    ▼
4. 执行数据库初始化
    │  运行 src/main/resources/database-init.sql
    │  创建 6 张表，插入 3 家餐厅 + 30 条菜品数据
    │
    ▼
5. 执行 DevRunner
    │  插入测试用户 (user_a, user_b)
    │  创建购物车
    │  添加一些菜品到购物车
    │
    ▼
6. 启动 Tomcat 服务器
    │  监听端口 8093
    │
    ▼
应用启动完成！
```

### 3.5 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests CartServiceTests

# 查看测试报告
open build/reports/tests/test/index.html
```

---

## 4. 项目结构全览

```
OnlineOrder/
├── build.gradle                          # Gradle 构建配置
├── settings.gradle                       # 项目名称
├── gradlew / gradlew.bat                 # Gradle Wrapper 脚本
├── docker-compose.yml                    # PostgreSQL Docker 配置
│
├── src/main/java/com/cwj/onlineorder/
│   ├── OnlineOrderApplication.java      # 应用入口（main 方法）
│   ├── AppConfig.java                   # 安全配置 + Bean 定义
│   ├── DevRunner.java                   # 开发时数据初始化（启动时执行）
│   │
│   ├── entity/                          # 数据库表对应的 Java 对象
│   │   ├── CustomerEntity.java          # 用户
│   │   ├── RestaurantEntity.java        # 餐厅
│   │   ├── MenuItemEntity.java          # 菜品
│   │   ├── CartEntity.java              # 购物车
│   │   └── OrderItemEntity.java        # 购物车中的单项
│   │
│   ├── model/                           # 数据传输对象（API 请求/响应格式）
│   │   ├── RestaurantDto.java
│   │   ├── MenuItemDto.java
│   │   ├── OrderItemDto.java
│   │   ├── CartDto.java
│   │   ├── RegisterBody.java            # 注册请求体
│   │   └── AddToCartBody.java          # 添加购物车请求体
│   │
│   ├── repository/                      # 数据访问层（数据库查询）
│   │   ├── CustomerRepository.java
│   │   ├── RestaurantRepository.java
│   │   ├── MenuItemRepository.java
│   │   ├── OrderItemRepository.java
│   │   └── CartRepository.java
│   │
│   ├── service/                         # 业务逻辑层
│   │   ├── CustomerService.java         # 用户注册、查询
│   │   ├── RestaurantService.java      # 餐厅列表（含菜单）
│   │   ├── MenuItemService.java         # 菜品查询
│   │   └── CartService.java             # 购物车增删查
│   │
│   └── controller/                      # HTTP 请求处理层
│       ├── CustomerController.java       # POST /signup
│       ├── MenuController.java          # GET /restaurants/... 和 /restaurant/...
│       └── CartController.java          # GET/POST /cart, POST /cart/checkout
│
├── src/main/resources/
│   ├── application.yaml                  # Spring Boot 配置
│   ├── database-init.sql                 # 数据库建表 + 种子数据
│   ├── static/                          # 静态资源目录（空）
│   └── templates/                        # 模板目录（空，供 Thymeleaf 使用）
│
└── src/test/java/com/cwj/onlineorder/
    ├── CartServiceTests.java             # CartService 单元测试
    └── OnlineOrderApplicationTests.java  # Spring 上下文加载测试
```

---

## 5. 分层架构详解

### 5.1 为什么要分层？

```
┌──────────────────────────────────────────────────────────────┐
│                     Controller 层                           │
│   职责：处理 HTTP 请求/响应                                   │
│   不知道业务逻辑怎么写，只负责"有人要来拿购物车，我就调用购物车服务"│
└────────────────────────────┬─────────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                      Service 层                              │
│   职责：业务逻辑                                              │
│   不知道数据存在哪张表，只负责"加菜要增加数量，累加价格"          │
└────────────────────────────┬─────────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                    Repository 层                              │
│   职责：数据库访问                                            │
│   不知道业务逻辑，只负责"执行这条 SQL，帮我查购物车表"            │
└────────────────────────────┬─────────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                      Database                                │
│   职责：持久化存储                                            │
└──────────────────────────────────────────────────────────────┘
```

**分层的好处：**
- **关注点分离**：每层只做自己的事
- **可测试性**：可以单独测试 Service 层（mock Repository）
- **可维护性**：改数据库不影响 Controller
- **可复用性**：同一 Service 可被多个 Controller 调用

### 5.2 一个请求的完整旅程

以 `POST /cart`（添加菜品到购物车）为例：

```
1. HTTP POST /cart
   请求体: { "menuId": 5 }
   Header: Cookie: JSESSIONID=abc123  (用户已登录)

2. CartController.addToCart()
   - @AuthenticationPrincipal 注入当前登录用户 (Spring Security 提供)
   - 用 email 查询 CustomerEntity (得到 customerId)
   - 调用 cartService.addMenuItemToCart(customerId, 5)

3. CartService.addMenuItemToCart()
   - 调用 cartRepository.getByCustomerId(customerId)  // 查购物车
   - 调用 menuItemRepository.findById(5)              // 查菜品价格
   - 调用 orderItemRepository.findByCartIdAndMenuItemId(...) // 查是否已存在
   - 如果不存在：orderItemRepository.save(新行, quantity=1)
   - 如果存在：orderItemRepository.save(更新行, quantity+1)
   - 调用 cartRepository.updateTotalPrice(...)          // 更新总价
   - @CacheEvict 清除缓存

4. Repository 层
   Spring Data JDBC 生成 SQL：
   SELECT * FROM carts WHERE customer_id = ?
   SELECT * FROM menu_items WHERE id = ?
   SELECT * FROM order_items WHERE cart_id = ? AND menu_item_id = ?
   INSERT INTO order_items (...) VALUES (...)
   UPDATE carts SET total_price = ? WHERE id = ?

5. 数据库执行 SQL，结果返回

6. 响应 HTTP 200 OK（空响应体）
```

---

## 6. 数据库设计

### 6.1 ER 图

```
┌────────────────┐      ┌────────────────┐      ┌────────────────┐
│  customers     │1:N   │    carts       │1:N   │  order_items   │
│───────────────-│      │────────────────│      │────────────────│
│ id (PK)       │◄────│customer_id(FK) │◄────│ cart_id (FK)  │
│ email (UNIQUE)│      │ id (PK)        │      │ menu_item_id(FK│
│ password      │      │ total_price    │      │ price (快照)   │
│ enabled       │      └────────────────┘      │ quantity       │
│ first_name    │                               └───────┬────────┘
│ last_name     │                                       │ N:1
└─────────┬──────┘                                       ▼
          │ 1:N                                   ┌────────────────┐
          ▼                                       │  menu_items    │
┌────────────────┐       N:1                      │────────────────│
│  authorities   │                                │ id (PK)        │
│────────────────│                                │ restaurant_id  │
│ email (FK)    │                                │ name           │
│ authority     │                                │ price          │
└────────────────┘                                │ description    │
                                                  │ image_url      │
                                                  └───────┬────────┘
                                                          │ N:1
                                                          ▼
                                                  ┌────────────────┐
                                                  │  restaurants   │
                                                  │────────────────│
                                                  │ id (PK)        │
                                                  │ name           │
                                                  │ address        │
                                                  │ phone          │
                                                  │ image_url      │
                                                  └────────────────┘
```

### 6.2 表详解

#### customers 表 — 用户

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | SERIAL PRIMARY KEY | 自动递增主键 |
| `email` | TEXT UNIQUE NOT NULL | 登录用户名，唯一 |
| `password` | TEXT NOT NULL | BCrypt 哈希后的密码 |
| `enabled` | BOOLEAN DEFAULT TRUE | 账户是否启用（禁用=不能登录） |
| `first_name` | TEXT | 名 |
| `last_name` | TEXT | 姓 |

**注意**：`password` 列存储的是 BCrypt 哈希值，不是明文密码。例如明文 `"myPassword123"` 会被存为 `"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"`。

#### authorities 表 — 用户角色

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | SERIAL PRIMARY KEY | 自动递增主键 |
| `email` | TEXT NOT NULL (FK) | 引用 customers.email |
| `authority` | TEXT NOT NULL | 角色名，如 "ROLE_USER" |

Spring Security 用这张表判断用户能访问哪些路径。

#### carts 表 — 购物车

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | SERIAL PRIMARY KEY | 自动递增主键 |
| `customer_id` | INTEGER UNIQUE NOT NULL | 引用 customers.id，每个用户最多一个购物车 |
| `total_price` | NUMERIC NOT NULL | 购物车总价（应用层维护，不自动计算） |

#### menu_items 表 — 菜品

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | SERIAL PRIMARY KEY | 自动递增主键 |
| `restaurant_id` | INTEGER NOT NULL (FK) | 所属餐厅，级联删除 |
| `name` | TEXT NOT NULL | 菜品名称 |
| `price` | NUMERIC NOT NULL | 单价 |
| `description` | TEXT | 描述（可为空） |
| `image_url` | TEXT | 图片 URL（可为空） |

#### order_items 表 — 购物车中的单项

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | SERIAL PRIMARY KEY | 自动递增主键 |
| `menu_item_id` | INTEGER NOT NULL (FK) | 菜品 ID |
| `cart_id` | INTEGER NOT NULL (FK) | 购物车 ID |
| `price` | NUMERIC NOT NULL | **价格快照**：加入购物车时的价格 |
| `quantity` | INTEGER NOT NULL | 数量 |

**为什么 `price` 是快照？** 设想用户把 Whopper 加入购物车时价格是 $6.39。第二天餐厅把价格改成 $7.99。用户的购物车仍然显示 $6.39 — 他是在价格变更前同意的价格。这个快照保护消费者。

### 6.3 种子数据

`database-init.sql` 在每次应用启动时执行（`INIT_DB=always`），插入：

| 餐厅 | 菜品数 | 代表菜品 |
|------|--------|---------|
| Burger King (ID=1) | 10 | Whopper, Chicken Fries, Impossible Whopper |
| SGD Tofu House (ID=2) | 10 | 各种豆腐锅、烤牛排、海鲜饼 |
| Fashion Wok (ID=3) | 10 | 各种炒菜、水煮鱼 |

**警告**：`INIT_DB=always` 会在每次启动时**先删除所有表再重建**。这意味着你添加的数据会在重启后丢失。只适合开发环境。生产环境应该改用 `INIT_DB=never` 并通过 Flyway/Liquibase 管理迁移。

---

## 7. 实体层 (Entity)

### 7.1 什么是 Entity？

Entity 是数据库表在 Java 代码中的"镜像"。每一行数据对应一个 Entity 实例。

```java
@Table("customers")  // 告诉 Spring：这个类对应 customers 表
public record CustomerEntity(
    @Id Long id,     // 主键字段
    String email,    // 对应 customers.email 列
    String password, // 对应 customers.password 列
    ...
)
```

### 7.2 @Id 和自增主键

```java
@Id Long id
```

- `@Id` 标记这是主键
- 传 `null` 时，Spring Data 会使用数据库的 `SERIAL` 自增行为
- 保存后，`id` 字段会被填充为数据库生成的值

```java
// 新建用户（id 未知）
var customer = new CustomerEntity(null, "alice@mail.com", "hash", true, "Alice", "Smith");
// 保存到数据库
var saved = customerRepository.save(customer);
// saved.id() 现在有了值（如 5），由数据库自动生成
System.out.println(saved.id());  // 5
```

### 7.3 为什么用 Record 作为 Entity？

1. **简洁**：无需手写构造器、getter、equals、hashCode、toString
2. **不可变**：所有字段默认 final，数据不会意外被修改
3. **适合数据传输**：Entity 通常是只读的（从数据库读出，用完即丢）

### 7.4 Spring Data JDBC 不处理外键关系

```java
// MenuItemEntity 有一个 restaurantId 字段
public record MenuItemEntity(
    Long id,
    Long restaurantId,  // 这只是数字，不是 RestaurantEntity 对象！
    ...
)
```

这与 JPA/Hibernate 不同。JPA 的 `@ManyToOne` 会自动加载关联对象：

```java
// JPA 风格（不是本项目的做法）
@ManyToOne
@JoinColumn(name = "restaurant_id")
private RestaurantEntity restaurant;  // 自动加载关联对象
```

本项目用 Spring Data JDBC，**不自动加载关联对象**。如果需要餐厅对象，要单独查询：

```java
// 手动加载关联对象
MenuItemEntity menuItem = menuItemRepository.findById(id);
RestaurantEntity restaurant = restaurantRepository.findById(menuItem.restaurantId());
```

这样做更透明，没有隐式行为，但需要更多手写代码。

---

## 8. 数据传输对象 (DTO)

### 8.1 为什么需要 DTO？

Entity 是数据库结构，DTO 是 API 结构。它们不同！

**Entity 可能有不该暴露的字段：**

```java
public record CustomerEntity(
    Long id,
    String email,
    String password,    // 密码哈希！不能暴露给客户端！
    boolean enabled,
    String firstName,
    String lastName
)
```

如果直接返回 `CustomerEntity`，客户端会看到密码哈希。**这是严重的安全漏洞！**

**DTO 方案：**

```java
public record CustomerDto(
    String email,
    String firstName,
    String lastName
    // 没有 password，没有 enabled！
)
```

### 8.2 DTO 的第二个作用：塑形 (Projection)

客户端可能需要一个不同的数据结构。比如浏览餐厅时，希望每家餐厅的菜单嵌套在里面：

```json
{
  "id": 1,
  "name": "Burger King",
  "menu_items": [
    { "id": 1, "name": "Whopper", "price": 6.39 },
    { "id": 2, "name": "Chicken Fries", "price": 4.89 }
  ]
}
```

数据库里餐厅和菜品是两张独立的表。要实现这个嵌套，Service 层负责把两个 Entity 合并成一个 DTO：

```java
public RestaurantDto(RestaurantEntity entity, List<MenuItemDto> menuItems) {
    // 从 entity 提取字段
    this(entity.id(), entity.name(), ...);
    // 从参数传入嵌套数据
    this.menuItems = menuItems;
}
```

### 8.3 Entity 到 DTO 的转换模式

```java
// 构造器转换法：DTO 提供一个接收 Entity 的构造器
public record MenuItemDto(MenuItemEntity entity) {
    // entity.id() -> this.id
    // entity.name() -> this.name
    // entity.description() -> this.description
    // entity.price() -> this.price
    // entity.imageUrl() -> this.imageUrl
    // entity.restaurantId() -> 故意不包含在 DTO 中
}

// 使用
MenuItemEntity entity = menuItemRepository.findById(5L);
MenuItemDto dto = new MenuItemDto(entity);  // 一行转换
```

---

## 9. 仓储层 (Repository)

### 9.1 Spring Data 的魔力

你只需要定义**接口**，Spring 为你生成**实现**：

```java
public interface MenuItemRepository extends ListCrudRepository<MenuItemEntity, Long> {
    List<MenuItemEntity> getByRestaurantId(Long restaurantId);  // Spring 自动实现！
}
```

Spring 读取方法名 `getByRestaurantId`，生成 SQL：

```sql
SELECT * FROM menu_items WHERE restaurant_id = ?
```

### 9.2 ListCrudRepository 提供的免费方法

```java
public interface CustomerRepository extends ListCrudRepository<CustomerEntity, Long> {}

// 免费获得：
customerRepository.save(entity)         // INSERT 或 UPDATE
customerRepository.findById(id)         // SELECT WHERE id = ?
customerRepository.findAll()            // SELECT * (全部)
customerRepository.existsById(id)       // SELECT COUNT(*) > 0
customerRepository.count()              // SELECT COUNT(*)
customerRepository.deleteById(id)       // DELETE WHERE id = ?
customerRepository.deleteAll()          // DELETE * (慎用)
```

### 9.3 派生查询方法命名规则

| 方法名 | 生成的 SQL |
|--------|-----------|
| `findByEmail(String e)` | `SELECT * FROM customers WHERE email = ?` |
| `findByFirstName(String n)` | `SELECT * FROM customers WHERE first_name = ?` |
| `findByLastNameContaining(String s)` | `SELECT * WHERE last_name LIKE '%'||?||'%'` |
| `findByAgeGreaterThan(int a)` | `SELECT * WHERE age > ?` |
| `existsByEmail(String e)` | `SELECT COUNT(*) > 0 WHERE email = ?` |
| `deleteById(Long id)` | `DELETE WHERE id = ?` |

### 9.4 自定义 SQL 查询

当派生查询不够用时，用 `@Query` 注解写原生 SQL：

```java
@Modifying
@Query("UPDATE customers SET first_name = :firstName, last_name = :lastName WHERE email = :email")
void updateNameByEmail(String email, String firstName, String lastName);
```

- `:firstName` 是命名参数，值从 Java 方法参数 `firstName` 传入
- `@Modifying` 告诉 Spring 这是修改数据的查询（INSERT/UPDATE/DELETE），必须有
- 没有 `@Modifying`，Spring 会抛出 `IncorrectUsageException`

### 9.5 @Modifying 的重要性

```java
// 正确：有 @Modifying
@Modifying
@Query("DELETE FROM order_items WHERE cart_id = :cartId")
void deleteByCartId(Long cartId);

// 错误：没有 @Modifying，运行时抛出异常
@Query("DELETE FROM order_items WHERE cart_id = :cartId")
void deleteByCartId(Long cartId);  // MissingQueryException: Modifying queries can only use void or int as return type
```

---

## 10. 服务层 (Service)

### 10.1 Service 层做什么？

Service 是业务逻辑的核心。它：
- 组合多个 Repository 调用
- 实现业务规则
- 管理事务

### 10.2 @Transactional 详解

```java
@Transactional
public void addMenuItemToCart(long customerId, long menuItemId) {
    // 步骤 1: 保存订单项
    orderItemRepository.save(newOrderItem);

    // 步骤 2: 更新总价
    cartRepository.updateTotalPrice(cart.id(), newTotal);

    // 如果步骤 2 抛出异常：
    // -> 步骤 1 的数据库修改也会被撤销（回滚）
}
```

**为什么需要事务？** 保证数据一致性。如果不开启事务，步骤 1 成功但步骤 2 失败，数据库会处于不一致状态。

### 10.3 依赖注入（Constructor Injection）

```java
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;

    // 通过构造函数注入依赖
    public CartService(
            CartRepository cartRepository,
            MenuItemRepository menuItemRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderItemRepository = orderItemRepository;
    }
}
```

Spring 在启动时：
1. 扫描到 `@Service` 注解的 `CartService`
2. 发现构造函数需要 3 个参数：`CartRepository`、`MenuItemRepository`、`OrderItemRepository`
3. 自动找到这些 bean 并传入构造函数
4. 创建 `CartService` 实例并注册

**为什么用 `final`？** 依赖在构造时注入后不再改变，保证线程安全。

---

## 11. 控制器层 (Controller)

### 11.1 @RestController 详解

```java
@RestController
public class CartController {
    @GetMapping("/cart")
    public CartDto getCart(@AuthenticationPrincipal User user) {
        return cartService.getCart(customer.id());
    }
}
```

`@RestController` = `@Controller` + 所有方法上加了 `@ResponseBody`

```java
// 这两段代码完全等价：
@RestController
class X { @GetMapping("/x") public Object get() { return obj; } }

// 等价于
@Controller
@ResponseBody  // 方法返回值写入 HTTP 响应体
class X { @GetMapping("/x") public Object get() { return obj; } }
```

没有 `@ResponseBody`，Spring 会把返回值当作**视图模板名称**去查找（如 `return "cart"` 会去找 `cart.html`）。`@ResponseBody` 告诉 Spring："把返回值直接序列化到响应体"。

### 11.2 @RequestBody 详解

```java
@PostMapping("/cart")
public void addToCart(@RequestBody AddToCartBody body) { ... }
```

`@RequestBody` 做三件事：
1. 读取 HTTP 请求体（raw JSON 文本）
2. 用 Jackson 把 JSON 反序列化为 Java 对象
3. 注入到方法参数

```
HTTP 请求:
POST /cart
Content-Type: application/json
Body: { "menuId": 5 }

        ▼ Jackson 反序列化
        AddToCartBody(menuId=5)

        ▼ 注入方法参数
        addToCart(body)  // body.menuId() == 5
```

### 11.3 @PathVariable 详解

```java
@GetMapping("/restaurant/{restaurantId}/menu")
public List<MenuItemEntity> getMenu(@PathVariable("restaurantId") long restaurantId) { ... }
```

`{restaurantId}` 是 URL 路径的一部分：

```
GET /restaurant/1/menu
       └──{restaurantId}──
       restaurantId = 1
```

### 11.4 @AuthenticationPrincipal 详解

```java
@GetMapping("/cart")
public CartDto getCart(@AuthenticationPrincipal User user) { ... }
```

Spring Security 在每个请求上维护一个 `SecurityContext`，里面有所属用户的 `Authentication` 对象。`@AuthenticationPrincipal` 从中提取当前用户。

```java
user.getUsername()  // 返回 email（本项目用作登录名）
user.getPassword()  // 返回 BCrypt 哈希（永远不要暴露！）
user.getAuthorities() // 返回权限列表（ROLE_USER）
```

---

## 12. 安全配置 (AppConfig)

### 12.1 SecurityFilterChain 的工作原理

Spring Security 通过一个**过滤器链**（Filter Chain）处理每个 HTTP 请求：

```
HTTP 请求
    │
    ▼
SecurityFilterChain
    │
    ├─► CSRF Filter        （检查 CSRF token，我们禁用了）
    ├─► Session Management  （检查登录状态）
    ├─► Authorization Filter（检查路径权限）
    ├─► Basic Auth Filter   （处理 HTTP Basic Auth）
    ├─► Form Login Filter   （处理表单登录）
    └─► ...更多过滤器...
        │
        ▼
Controller
```

### 12.2 路径权限配置详解

```java
.authorizeHttpRequests(auth -> auth
    // 公开路径（permitAll）
    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
    //                              └─► /css/**, /js/**, /images/**, /webjars/**

    .requestMatchers(HttpMethod.GET, "/", "/index.html", "/*.json", "/*.png", "/static/**").permitAll()

    // 注册、登录、登出是公开的
    .requestMatchers(HttpMethod.POST, "/login", "/logout", "/signup").permitAll()

    // 餐厅浏览是公开的（用户不需要登录就能看菜单）
    .requestMatchers(HttpMethod.GET, "/restaurants/**", "/restaurant/**").permitAll()

    // 其他所有请求都需要登录
    .anyRequest().authenticated()
)
```

### 12.3 JdbcUserDetailsManager 自定义 SQL

Spring Security 默认期望表名是 `users` 和 `authorities`，列名是 `username`、`password`、`enabled`。我们的数据库用的是 `customers` 和 `authorities`，列名也不同，所以要自定义 SQL：

```java
userDetailsManager.setUsersByUsernameQuery(
    "SELECT email, password, enabled FROM customers WHERE email = ?"
    //  ✓ email 对应 username
    //  ✓ password 字段名一致
    //  ✓ enabled 字段名一致
);

userDetailsManager.setAuthoritiesByUsernameQuery(
    "SELECT email, authorities FROM authorities WHERE email = ?"
    //  注意：列名叫 "authorities" 不是 "authority"
);
```

---

## 13. 应用配置 (application.yaml)

```yaml
server:
  port: 8093  # Tomcat 监听端口

spring:
  datasource:
    # 连接 PostgreSQL，${VAR:default} 表示优先用环境变量，否则用默认值
    url: jdbc:postgresql://${DATABASE_URL:localhost}:${DATABASE_PORT:5432}/onlineorder
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:secret}
    driver-class-name: org.postgresql.Driver

  sql:
    init:
      mode: ${INIT_DB:always}  # always = 每次启动运行 init 脚本
      schema-locations: "classpath:database-init.sql"

  cache:
    caffeine:
      spec: expireAfterWrite=60s  # 缓存写入后 60 秒过期

  jackson:
    default-property-inclusion: non_null    # null 字段不序列化到 JSON
    property-naming-strategy: SNAKE_CASE    # Java 驼峰命名 -> JSON 蛇形命名
                                              # firstName -> first_name
                                              # imageUrl  -> image_url

logging:
  level:
    org.springframework.jdbc.core: DEBUG    # 打印每条执行的 SQL
    org.springframework.jdbc.datasource.init: DEBUG  # 打印初始化 SQL
```

---

## 14. REST API 参考

### 14.1 公开端点（无需登录）

#### 获取所有餐厅（含菜单）

```
GET /restaurants/menu
```

响应：
```json
[
  {
    "id": 1,
    "name": "Burger King",
    "address": "773 N Mathilda Ave, Sunnyvale, CA 94085",
    "phone": "6505550000",
    "image_url": "https://...",
    "menu_items": [
      {
        "id": 1,
        "name": "Chicken Fries - 9 Pc",
        "description": "Made with white meat chicken...",
        "price": 4.89,
        "image_url": "https://..."
      }
    ]
  }
]
```

#### 获取单个餐厅的菜单

```
GET /restaurant/{restaurantId}/menu
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `restaurantId` | long | 餐厅 ID（1=Burger King, 2=SGD Tofu House, 3=Fashion Wok）|

### 14.2 需要登录的端点

#### 注册账号

```
POST /signup
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "myPassword123",
  "firstName": "Alice",
  "lastName": "Smith"
}
```

响应：`201 Created`（成功）或 `500 Internal Server Error`（邮箱已存在）

#### 登录

```
POST /login
Content-Type: application/x-www-form-urlencoded

username=alice@example.com&password=myPassword123
```

响应：`200 OK`（成功，带 Session Cookie）或 `401 Unauthorized`（失败）

#### 查看购物车

```
GET /cart
Cookie: JSESSIONID=xxx
```

响应：
```json
{
  "id": 1,
  "total_price": 21.18,
  "order_items": [
    {
      "order_item_id": 5,
      "menu_item_id": 3,
      "restaurant_id": 1,
      "price": 10.59,
      "quantity": 2,
      "menu_item_name": "Whopper Meal",
      "menu_item_description": "...",
      "menu_item_image_url": "https://..."
    }
  ]
}
```

#### 添加菜品到购物车

```
POST /cart
Cookie: JSESSIONID=xxx
Content-Type: application/json

{ "menuId": 3 }
```

响应：`200 OK`（空响应体）

**注意**：重复添加同一菜品会增加数量，不是创建新行。

#### 结账

```
POST /cart/checkout
Cookie: JSESSIONID=xxx
```

响应：`200 OK`（清空购物车中的所有菜品）

---

## 15. 缓存机制

### 15.1 什么是缓存？

缓存是把计算结果存起来，下次需要时直接取用，避免重复计算。

```
没有缓存:
  请求 1: 查询 DB (慢) -> 返回结果          [耗时 200ms]
  请求 2: 查询 DB (慢) -> 返回结果          [耗时 200ms]
  请求 3: 查询 DB (慢) -> 返回结果          [耗时 200ms]

有缓存 (60秒 TTL):
  请求 1: 查询 DB (慢) -> 存入缓存 -> 返回 [耗时 200ms]
  请求 2: 查缓存 (快) -> 直接返回          [耗时 1ms]
  请求 3: 查缓存 (快) -> 直接返回          [耗时 1ms]
  60秒后缓存过期:
  请求 4: 查询 DB (慢) -> 存入缓存 -> 返回 [耗时 200ms]
```

### 15.2 本项目的缓存策略

```java
@Cacheable("restaurants")   // 缓存 "restaurants" 缓存区的返回值
public List<RestaurantDto> getRestaurants() { ... }

@CacheEvict(cacheNames = "cart", key = "#customerId")  // 清除缓存
@Transactional
public void addMenuItemToCart(long customerId, long menuItemId) { ... }
```

| 缓存区 | 方法 | TTL | 说明 |
|--------|------|-----|------|
| `restaurants` | `RestaurantService.getRestaurants()` | 60秒 | 所有餐厅（含菜单），数据不常变 |
| `cart` | `CartService.getCart()` | 60秒 | 每个客户的购物车（按 customerId 区分）|

### 15.3 缓存失效策略

添加菜品或清空购物车后，必须清除缓存，否则 `getCart()` 会返回旧的（已过期）数据：

```java
@CacheEvict(cacheNames = "cart", key = "#customerId")
public void addMenuItemToCart(long customerId, long menuItemId) {
    // ... 业务逻辑 ...
    // 缓存清除在方法成功后自动执行
}
```

---

## 16. 密码安全

### 16.1 绝对不要明文存储密码！

```
错误 ❌:
  数据库: password = "myPassword123"

正确 ✅:
  数据库: password = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

  解密? 不可能！BCrypt 是单向函数。
  只能验证：输入 "myPassword123" -> BCrypt 哈希 -> 与数据库比对 -> 匹配 = 密码正确
```

### 16.2 BCrypt 工作原理

```
注册时:
  用户输入密码: "myPassword123"
              ▼
  BCrypt 哈希 (随机 salt + 多次哈希)
              ▼
  数据库存储: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
                                  │  │
                                  │  └── 哈希结果
                                  └── cost factor = 10 (2^10 = 1024 轮哈希)
  相同密码不同哈希:
  "myPassword123" -> "$2a$10$Ej4XwZ...Kq"  (不同的 salt!)
  "myPassword123" -> "$2a$10$Ab6Yx...Lp"  (又是不同的 salt!)

登录时:
  用户输入: "myPassword123"
              ▼
  BCrypt 从数据库哈希中提取 salt
              ▼
  用相同的 salt 哈希输入
              ▼
  比对哈希值
```

### 16.3 在代码中使用 PasswordEncoder

```java
// 注册时：加密密码
String rawPassword = "myPassword123";
String hashedPassword = passwordEncoder.encode(rawPassword);
// "$2a$10$..."

// 登录时：验证密码
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
// 输入正确: true
// 输入错误: false
```

---

## 17. 单元测试

### 17.1 什么是单元测试？

单元测试是针对代码中最小单元（通常是一个方法）的自动化测试。它验证代码在各种情况下是否正确工作。

### 17.2 本项目的测试策略

**CartServiceTests** — 单元测试：
- 使用 **Mockito** 创建模拟对象（Mock Objects）
- 不连接真实数据库
- 测试 CartService 的业务逻辑

**OnlineOrderApplicationTests** — 集成测试：
- 加载完整的 Spring 上下文
- 连接真实数据库
- 验证整个应用能正常启动

### 17.3 测试结构：Arrange-Act-Assert

```java
@Test
void addMenuItemToCart_whenOrderNotExist_shouldCreateOneOrderItem() {

    // ---- Arrange: 准备测试数据和模拟行为 ----
    // 创建"假"的数据库返回值
    Mockito.when(cartRepository.getByCustomerId(1L)).thenReturn(cartEntity);
    Mockito.when(menuItemRepository.findById(2L)).thenReturn(Optional.of(menuItem));
    Mockito.when(orderItemRepository.findByCartIdAndMenuItemId(3L, 2L))
            .thenReturn(null);  // 该菜品不在购物车中

    // ---- Act: 调用要测试的方法 ----
    cartService.addMenuItemToCart(1L, 2L);

    // ---- Assert: 验证结果 ----
    // 验证 save() 被调用，且参数正确
    OrderItemEntity expectedOrderItem = new OrderItemEntity(null, 2L, 3L, 10.0, 1);
    Mockito.verify(orderItemRepository).save(expectedOrderItem);

    // 验证 updateTotalPrice 被调用，总价增加了 10.0
    Mockito.verify(cartRepository).updateTotalPrice(3L, 10.0);
}
```

### 17.4 为什么用 Mock？

| | 真实对象 | Mock 对象 |
|--|---------|----------|
| 数据来源 | 数据库 | 你在测试中定义 |
| 速度 | 慢（网络 IO） | 快（内存） |
| 依赖 | 需要数据库运行 | 不需要 |
| 行为 | 固定 | 可编程（返回什么都行） |

---

## 18. 常见问题与调试

### 18.1 数据库连接失败

```
org.springframework.boot.jdbcmetadataCannotConnectException:
  Cannot determine embedded database driver class for database type NONE
```

**解决**：
```bash
docker-compose up -d  # 确保 PostgreSQL 容器运行
```

### 18.2 端口被占用

```
LifecycleException: Protocol handler start failed:
  java.net.BindException: Address already in use (Bind failed)
```

**解决**：
```bash
# 找到占用端口的进程
lsof -i :8093
# 杀掉它
kill -9 <PID>
# 或改端口：application.yaml 中改 server.port: 8094
```

### 18.3 登录失败 (401)

1. 检查数据库中是否有该用户：`SELECT * FROM customers WHERE email = 'xxx'`
2. 检查 authorities 表：`SELECT * FROM authorities WHERE email = 'xxx'`
3. 检查密码是否用 BCrypt 存储（以 `$2a$` 开头）
4. 注意 email 的大小写（本项目做了 `toLowerCase()` 处理）

### 18.4 缓存导致数据不更新

如果添加菜品后，`GET /cart` 返回旧数据：
- 缓存 TTL 是 60 秒，等待过期
- 或者重启应用（清空内存缓存）
- 生产环境：实现缓存监控和手动清除

### 18.5 调试 SQL 日志

`application.yaml` 已配置：

```yaml
logging:
  level:
    org.springframework.jdbc.core: DEBUG  # 打印 SQL 和参数
```

重启应用后，控制台会显示每条执行的 SQL 语句及参数值。

---

## 19. 如何扩展这个项目

### 19.1 添加新字段

**场景**：给餐厅添加"评分"字段。

1. 修改数据库：`ALTER TABLE restaurants ADD COLUMN rating DECIMAL(2,1);`
2. 修改 Entity：
```java
@Table("restaurants")
public record RestaurantEntity(
    @Id Long id,
    String name,
    String address,
    String phone,
    String imageUrl,
    Double rating  // 新增
) { }
```
3. 修改 DTO（暴露给 API）：
```java
public record RestaurantDto(
    Long id, String name, String address, String phone,
    String imageUrl, Double rating,  // 新增
    List<MenuItemDto> menuItems
) { }
```
4. 修改 Service 中的转换逻辑

### 19.2 添加新的 API 端点

**场景**：添加删除购物车中单个菜品的 API。

1. 在 `OrderItemRepository` 添加方法：
```java
void deleteByCartIdAndMenuItemId(Long cartId, Long menuItemId);
```

2. 在 `CartService` 添加业务逻辑：
```java
@Transactional
public void removeMenuItemFromCart(Long customerId, Long menuItemId) {
    CartEntity cart = cartRepository.getByCustomerId(customerId);
    OrderItemEntity item = orderItemRepository.findByCartIdAndMenuItemId(cart.id(), menuItemId);
    if (item != null) {
        cartRepository.updateTotalPrice(cart.id(), cart.totalPrice() - item.price() * item.quantity());
        orderItemRepository.deleteByCartIdAndMenuItemId(cart.id(), menuItemId);
    }
}
```

3. 在 `CartController` 添加端点：
```java
@DeleteMapping("/cart/{menuItemId}")
public void removeFromCart(
        @AuthenticationPrincipal User user,
        @PathVariable Long menuItemId) {
    CustomerEntity customer = customerService.getCustomerByEmail(user.getUsername());
    cartService.removeMenuItemFromCart(customer.id(), menuItemId);
}
```

### 19.3 添加管理员角色

1. 数据库添加管理员角色：
```sql
INSERT INTO authorities (email, authority) VALUES ('admin@mail.com', 'ROLE_ADMIN');
```

2. 修改 `AppConfig` 的安全规则：
```java
.requestMatchers(HttpMethod.DELETE, "/restaurants/**").hasRole("ADMIN")  // 只有 ADMIN 能删餐厅
```

### 19.4 添加分页功能

Spring Data 内置分页支持：

```java
// Repository
public interface RestaurantRepository extends ListCrudRepository<RestaurantEntity, Long> {
    Page<RestaurantEntity> findAll(Pageable pageable);  // 分页查询
}

// Controller
@GetMapping("/restaurants")
public Page<RestaurantDto> getRestaurants(Pageable pageable) {
    return restaurantService.getRestaurants(pageable);
}

// 使用: GET /restaurants?page=0&size=10
```

### 19.5 从 development 迈向 production 的改进清单

| 改进项 | 说明 |
|--------|------|
| `@Valid` + `@NotBlank` 等 | 添加输入校验，防止恶意数据 |
| `@ControllerAdvice` | 全局异常处理，统一错误响应格式 |
| `INIT_DB=never` | 生产环境不自动重建数据库 |
| Flyway/Liquibase | 数据库迁移管理（版本化 SQL 脚本） |
| Redis | 分布式缓存（多实例共享，比 Caffeine 更好） |
| `@Transactional(readOnly = true)` | 只读事务优化查询性能 |
| JWT 认证 | 无状态认证，支持移动端和微服务 |
| REST 文档 (SpringDoc) | 自动生成 OpenAPI/Swagger 文档 |
| Actuator | 应用健康检查、性能监控端点 |
| Docker 部署 | `docker-compose` 含应用容器 + PostgreSQL |

---

*本文档由代码注释辅助生成。每个 Java 文件都包含详细的新手向注释，建议配合源代码一起阅读。*
