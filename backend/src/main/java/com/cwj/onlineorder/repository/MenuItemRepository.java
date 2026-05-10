// ---------------------------------------------------------------------------
// OnlineOrder - Repository: MenuItemRepository
// ---------------------------------------------------------------------------
// Repository for MenuItemEntity.
//
// Spring Data JDBC naming convention used here:
//   "getBy" prefix (instead of "findBy") = same behavior but signals that the
//   method returns the entity directly rather than an Optional.
//
// getByRestaurantId(Long restaurantId)
//   -> SELECT * FROM menu_items WHERE restaurant_id = ?
//   -> Returns all menu items belonging to the given restaurant.
//      Returns an empty list if the restaurant has no menu items.
//
// Note: If you want a method that also loads the associated restaurant entity
// (rather than just the restaurant_id number), you would need to use
// @Query with a JOIN or configure entity traversal. Spring Data JDBC does not
// auto-fetch related entities like JPA's @ManyToOne/@OneToMany does.
// ---------------------------------------------------------------------------
package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.MenuItemEntity;
import org.springframework.data.repository.ListCrudRepository;
import java.util.List;

// Repository for MenuItemEntity.
public interface MenuItemRepository extends ListCrudRepository<MenuItemEntity, Long> {

    // Derived query: "get" + "By" + "RestaurantId"
    // Spring Data generates: SELECT * FROM menu_items WHERE restaurant_id = ?
    //
    // "getBy" vs "findBy" in Spring Data JDBC:
    //   findBy  -> returns Optional<Entity> (safe: null check required)
    //   getBy   -> returns Entity directly (throws if not found)
    // Both work; "getBy" is slightly more concise for known-use cases.
    //
    // Note: This only returns the menu_items rows. The restaurant data itself
    // (name, address, etc.) is NOT loaded automatically. You need the
    // RestaurantRepository to fetch that separately.
    //
    // If you wanted to fetch menu items AND their restaurant in one query,
    // you would write a custom @Query with a JOIN:
    //   @Query("SELECT m.*, r.name FROM menu_items m JOIN restaurants r ON m.restaurant_id = r.id WHERE m.restaurant_id = :restaurantId")
    //   List<MenuItemEntity> getByRestaurantIdWithRestaurant(Long restaurantId);
    List<MenuItemEntity> getByRestaurantId(Long restaurantId);
}
