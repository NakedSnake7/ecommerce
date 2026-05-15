package com.webempresarial.store.theme;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StoreThemeResolver {

    // THEMES VÁLIDOS
    private static final Set<String> VALID_THEMES = Set.of(
        "WebEmpresarial",
        "stride",
        "espacio"
    );

    public String getTheme(HttpServletRequest request) {

        String host = request.getHeader("X-Forwarded-Host");

        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }

        // DEFAULT
        if (host == null || host.isBlank()) {
            return "WebEmpresarial";
        }

        host = host.toLowerCase().split(":")[0];

        // =========================
        // LOCALHOST
        // =========================

        if (
            host.equals("localhost") ||
            host.equals("127.0.0.1")
        ) {
            return "WebEmpresarial";
        }

        // =========================
        // DOMINIO PRINCIPAL
        // =========================

        if (
            host.equals("webempresarial.com") ||
            host.equals("www.webempresarial.com")
        ) {
            return "WebEmpresarial";
        }

        // =========================
        // SUBDOMINIOS
        // stride.midominio.com
        // =========================

        if (host.endsWith("midominio.com")) {

            String subdomain = host.split("\\.")[0];

            if (VALID_THEMES.contains(subdomain)) {
                return subdomain;
            }
        }

        // =========================
        // FALLBACK
        // =========================

        return "WebEmpresarial";
    }

    public String view(HttpServletRequest request, String page) {
        return "themes/" + getTheme(request) + "/" + page;
    }

    public String fragment(HttpServletRequest request, String fragment) {
        return "themes/" + getTheme(request) + "/fragments/" + fragment;
    }
}