package com.SKALA.LikeCloudy.Repository;

import com.SKALA.LikeCloudy.Entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository {
    Optional<MenuEntity> findByMenuType(String menuType); // 예 : "A", "B"
    List<MenuEntity> findAllByMenuDate(LocalDate menuDate);
}
