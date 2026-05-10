package com.cwj.onlineorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * OnlineOrder 应用主入口类。
 *
 * 本类是整个 Spring Boot 应用的启动入口，
 * 通过 @SpringBootApplication 和 @EnableCaching 两个注解完成应用的初始化配置。
 */
@SpringBootApplication
/**
 * @SpringBootApplication 是三个常用注解的组合：
 *
 *   @Configuration
 *     将本类标记为"配置类"，告诉 Spring 容器本类可以定义 Bean（@Bean 注解的方法返回的对象）。
 *     也可以用于导入其他配置类，实现配置模块化。
 *
 *   @EnableAutoConfiguration
 *     启用 Spring Boot 的自动配置机制。
 *     Spring Boot 会根据 classpath 中存在的依赖（jar 包）自动推断并配置合理的默认组件。
 *     例如：
 *       - classpath 有 spring-boot-starter-webmvc  → 自动配置嵌入式 Tomcat、Spring MVC
 *       - classpath 有 spring-boot-starter-data-jdbc → 自动配置 JdbcTemplate、DataSource
 *       - classpath 有 spring-boot-starter-security → 自动配置 SecurityFilterChain
 *     自动配置可以被 application.yaml 中的配置覆盖，也可以通过 @EnableAutoConfiguration(exclude = ...)
 *     或 spring.autoconfigure.exclude 属性禁用特定的自动配置类。
 *
 *   @ComponentScan
 *     组件扫描：告诉 Spring 在本类所在的包（com.cwj.onlineorder）及其子包中
 *     自动查找所有标注了 @Component、@Service、@Repository、@Controller 等注解的类，
 *     并将它们注册为 Spring 容器中的 Bean。
 *     因此本项目中的 CustomerService、CartController、CustomerRepository 等
 *     全部自动被 Spring 扫描并注册，无需手动一个个声明。
 *
 * 综上所述，@SpringBootApplication 放在主入口类上意味着：
 *   "这是应用的根包，从这里开始扫描所有组件；从这里开始应用自动配置；从这里开始定义配置 Bean。"
 */
@EnableCaching
/**
 * 启用 Spring 缓存抽象层。
 *
 * 本注解是 @Cacheable、@CacheEvict 等缓存注解生效的前提条件。
 * 如果没有 @EnableCaching，即使方法上标注了 @Cacheable，也不会产生任何缓存效果。
 *
 * 实际的缓存实现（Caffeine、Redis、EhCache 等）在 application.yaml 中通过
 * spring.cache.caffeine.spec 等配置项指定。
 * @EnableCaching 只需要加一次，通常放在主入口类或专门的缓存配置类上。
 *
 * 注意：当前项目中的 @Cacheable / @CacheEvict 注解已全部移除（因自调用不生效），
 * 本注解保留仅作框架完整性标识，未来如需重新启用缓存功能，无需再次添加此注解。
 */
public class OnlineOrderApplication {

    public static void main(String[] args) {
        /**
         * Spring Boot 应用启动入口。
         *
         * SpringApplication.run() 执行以下步骤：
         *   1. 创建 ApplicationContext（Spring IoC 容器）。
         *   2. 加载并处理所有 @Configuration 类（包括 AppConfig 等）。
         *   3. 执行自动配置（根据 classpath 推断并配置 DataSource、Security、MVC 等）。
         *   4. 执行 @ComponentScan，实例化所有业务 Bean（Service、Repository、Controller 等）。
         *   5. 运行数据库初始化脚本（database-init.sql，mode=always 每次启动都执行）。
         *   6. 启动嵌入式 Tomcat Web 服务器，监听 8093 端口。
         *   7. 执行 DevRunner（ApplicationRunner 接口的实现），初始化开发测试数据。
         *   8. 应用保持运行状态，直到被终止（Ctrl+C 或 kill）。
         *
         * 传入 args 的作用：
         *   命令行参数可以通过 ${} 在 application.yaml 中引用，
         *   也可以在 Spring Boot Actuator 端点中使用。
         *   常见用法：--server.port=8080（临时覆盖配置中的端口）。
         */
        SpringApplication.run(OnlineOrderApplication.class, args);
    }
}
