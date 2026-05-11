package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.CustomerEntity;
import org.springframework.data.repository.ListCrudRepository;

/**
 * 顾客数据访问层。
 *
 * 基于 Spring Data JDBC 的 ListCrudRepository 接口，
 * 自动提供 save()、findById()、findAll()、deleteById() 等 CRUD 方法。
 *
 * 设计说明：
 * - findByEmail() 返回实体或 null（而非 Optional），因为 email 有唯一约束
 * - email 字段在数据库层统一小写存储，所有查询传入前需先小写化
 * - 复杂的 UPDATE 操作（如 updateNameByEmail）使用自定义 SQL，其他场景优先用 save() 覆盖更新
 *
 * @see com.cwj.onlineorder.entity.CustomerEntity
 */
public interface CustomerRepository extends ListCrudRepository<CustomerEntity, Long> {

    /**
     * 根据邮箱查询顾客。
     *
     * 注意：返回 null 表示无结果。email 有唯一约束，最多只会有 0 或 1 条记录。
     * 调用方应确保 email 已做小写化处理。
     *
     * @param email 顾客邮箱（小写化后的邮箱地址）
     * @return 顾客实体，或 null
     */
    CustomerEntity findByEmail(String email);
}
