// ---------------------------------------------------------------------------
// OnlineOrder - Repository: RestaurantRepository
// ---------------------------------------------------------------------------
// Repository for RestaurantEntity. This is the simplest possible repository —
// it only inherits the standard CRUD methods from ListCrudRepository.
//
// No custom query methods are needed because:
//   - findAll()       returns every restaurant (for browsing)
//   - findById(id)    finds a specific restaurant by ID
//   - save(entity)    creates or updates a restaurant
//
// If you need search functionality later (e.g., find restaurants by name),
// you can add derived query methods like:
//   List<RestaurantEntity> findByNameContaining(String keyword)
//   List<RestaurantEntity> findByNameStartsWithIgnoreCase(String prefix)
// ---------------------------------------------------------------------------
package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.RestaurantEntity;
import org.springframework.data.repository.ListCrudRepository;

// Repository for RestaurantEntity. Inherits:
//   save(entity)           -> INSERT or UPDATE
//   findById(id)           -> SELECT * FROM restaurants WHERE id = ?
//   findAll()              -> SELECT * FROM restaurants (ordered by PK)
//   deleteById(id)         -> DELETE FROM restaurants WHERE id = ?
//   count()                -> SELECT COUNT(*) FROM restaurants
//   existsById(id)         -> SELECT COUNT(*) > 0 FROM restaurants WHERE id = ?
public interface RestaurantRepository extends ListCrudRepository<RestaurantEntity, Long> {
    // Intentionally empty.
    // All needed methods are provided by ListCrudRepository.
    //
    // Future examples if you need custom queries:
    //   List<RestaurantEntity> findByNameContaining(String keyword);
    //   List<RestaurantEntity> findByNameStartsWithIgnoreCase(String prefix);
}
