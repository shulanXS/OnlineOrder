package com.cwj.onlineorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 开发环境数据初始化 Runner。
 *
 * 仅在 dev profile 下执行。
 *
 * 注意：实际的初始化数据（餐厅、菜品等）已移至 Flyway 迁移脚本
 * (V1__initial_schema.sql)。此 Runner 仅用于开发时的额外初始化逻辑，
 * 如打印启动信息、验证数据等。
 */
@Component
@Profile("dev")
public class DevRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevRunner.class);

    @Override
    public void run(ApplicationArguments args) {
        logger.info("========================================");
        logger.info("  OnlineOrder 开发环境已启动");
        logger.info("  数据库迁移：Flyway (V1__initial_schema.sql)");
        logger.info("  示例餐厅数据已预填充");
        logger.info("========================================");
    }
}
