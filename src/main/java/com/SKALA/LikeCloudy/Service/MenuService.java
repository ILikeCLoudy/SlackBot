package com.SKALA.LikeCloudy.Service;
import com.SKALA.LikeCloudy.DTO.MenuDTO;
import com.SKALA.LikeCloudy.Entity.MenuEntity;
import com.SKALA.LikeCloudy.Repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    // 오늘 날짜의 메뉴를 등록
    public void registerMenu(List<MenuDTO> menuDTOList) {
        LocalDate today = LocalDate.now();

        List<MenuEntity> menus = menuDTOList.stream()
                .map(dto -> MenuEntity.builder()
                        .menuType(MenuEntity.MenuType.valueOf(dto.getMenuCode()))
                        .name(dto.getMenuName())
                        .menuDate(today)
                        .build())
                .toList();

        menuRepository.saveAll(menus);
    }
    //오늘 날짜의 모든 메뉴를 조회합니다!
    public List<MenuDTO> getTodayMenus() {
        LocalDate today = LocalDate.now();

        return menuRepository.findAllByMenuDate(today).stream()
                .map(entity -> new MenuDTO(
                        entity.getMenuType().name(), //enum -> String ("A" ~ "E")
                        entity.getName()))
                .toList();
    }
}
