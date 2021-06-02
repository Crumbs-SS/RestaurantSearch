package com.crumbs.fss.repository;


import com.crumbs.fss.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT DISTINCT r FROM restaurant r JOIN r.menuItems m WHERE m.restaurant.id = r.id " +
            "AND LOWER(m.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    Page<Restaurant> findRestaurantsByMenuItem(String menuItemName, Pageable pageable);

    @Query("SELECT DISTINCT r FROM restaurant r JOIN r.menuItems m WHERE m.restaurant.id = r.id " +
            "AND LOWER(m.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Restaurant> findRestaurantsByMenuItem(String menuItemName, Sort sort);

}
