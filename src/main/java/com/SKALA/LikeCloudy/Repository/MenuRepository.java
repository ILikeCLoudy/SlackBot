package com.SKALA.LikeCloudy.Repository;

import com.SKALA.LikeCloudy.Entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository {
    Optional<MenuEntity> findByMenuCode(String menuCode); // 예 : "A", "B"
}
