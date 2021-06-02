package com.crumbs.fss.repository;

import com.crumbs.fss.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findAllByRestaurantId(Long restaurantId, Pageable pageable);
}
