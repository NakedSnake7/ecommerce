package com.ecomerce.store.theme;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ThemeModelAdvice {

    private final StoreThemeResolver themeResolver;

    public ThemeModelAdvice(StoreThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
    }

    @ModelAttribute("theme")
    public String theme(HttpServletRequest request) {
        return themeResolver.getTheme(request);
    }
}