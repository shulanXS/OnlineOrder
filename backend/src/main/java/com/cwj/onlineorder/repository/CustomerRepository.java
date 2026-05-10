// ---------------------------------------------------------------------------
// OnlineOrder - Repository: CustomerRepository
// ---------------------------------------------------------------------------
// The Repository layer is the bridge between Java code and the PostgreSQL database.
// Spring Data JDBC automatically generates the SQL implementation — you only
// declare the interface, and Spring fills in the code at runtime.
//
// Key concepts for newcomers:
//   - "Repository interface" = you write the interface; Spring provides the implementation.
//   - "ListCrudRepository<EntityType, PrimaryKeyType>" = the base interface that
//     gives you free CRUD methods: save(), findById(), findAll(), deleteById(), count(), existsById().
//     "List" prefix means findAll() returns a List (not Iterable).
//   - "Derived query methods" = Spring Data reads method names like "findByEmail" and
//     automatically generates the SQL: "SELECT * FROM customers WHERE email = ?".
//     No implementation needed — just name the method correctly!
//   - "@Query" = write custom SQL manually when derived queries aren't enough.
//   - "@Modifying" = required on any method that changes data (INSERT, UPDATE, DELETE).
//     Without this annotation, Spring will throw an exception at runtime.
//
// Spring Data JDBC naming conventions for derived queries:
//   findBy + field name  -> SELECT ... WHERE field = ?
//   findAllBy + field    -> SELECT ... WHERE field = ?
//   getBy + field        -> same as findBy, but returns the entity directly (not Optional)
//   deleteBy + field     -> DELETE ... WHERE field = ?
//
// More examples:
//   findByFirstNameAndLastName(String first, String last)  -> WHERE first_name = ? AND last_name = ?
//   findByAgeGreaterThan(int age)                           -> WHERE age > ?
//   existsByEmail(String email)                            -> SELECT COUNT(*) > 0 FROM customers WHERE email = ?
// ---------------------------------------------------------------------------
package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.CustomerEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Repository for CustomerEntity. Extends ListCrudRepository which provides:
//   save(entity)           -> INSERT or UPDATE
//   findById(id)           -> SELECT * FROM customers WHERE id = ?
//   findAll()              -> SELECT * FROM customers
//   deleteById(id)         -> DELETE FROM customers WHERE id = ?
//   count()                -> SELECT COUNT(*) FROM customers
//   existsById(id)         -> SELECT COUNT(*) > 0 FROM customers WHERE id = ?
public interface CustomerRepository extends ListCrudRepository<CustomerEntity, Long> {

    // Spring Data automatically implements this from the method name:
    // "find" + "By" + "FirstName"  =>  SELECT * FROM customers WHERE first_name = ?
    // Returns a List because multiple customers can share the same first name.
    List<CustomerEntity> findByFirstName(String firstName);

    // Same pattern: SELECT * FROM customers WHERE last_name = ?
    List<CustomerEntity> findByLastName(String lastName);

    // Spring Data derives: SELECT * FROM customers WHERE email = ?
    // Returns null if no customer with that email exists.
    // Note: unlike findById() which returns Optional, this returns the entity directly.
    CustomerEntity findByEmail(String email);

    // CUSTOM SQL QUERY — derived queries can't handle UPDATE statements.
    // @Modifying is REQUIRED for INSERT, UPDATE, DELETE queries.
    // The @Query annotation lets you write raw SQL with named parameters (:email, :firstName, :lastName).
    //
    // SQL equivalent:
    //   UPDATE customers
    //   SET first_name = 'Alice', last_name = 'Smith'
    //   WHERE email = 'alice@example.com'
    //
    // Named parameters (:paramName) are injected from the Java method parameters in order.
    @Modifying
    @Transactional
    @Query("UPDATE customers SET first_name = :firstName, last_name = :lastName WHERE email = :email")
    void updateNameByEmail(String email, String firstName, String lastName);
}
