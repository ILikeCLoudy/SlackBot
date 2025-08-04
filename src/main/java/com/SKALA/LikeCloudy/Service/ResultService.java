package com.SKALA.LikeCloudy.Service;

import com.SKALA.LikeCloudy.Repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class ResultService {
    private final MenuRepository menuRepository;

}
