package com.ecommerce.store.theme;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class StoreThemeResolver {

	public String getTheme(HttpServletRequest request) {

	    String host = request.getHeader("X-Forwarded-Host");

	    if (host == null || host.isBlank()) {
	        host = request.getServerName();
	    }

	    if (host == null) {
	        return "stride";
	    }

	    host = host.toLowerCase().split(":")[0];

	    if (host.endsWith("midominio.com")) {
	        return host.split("\\.")[0];
	    }

	    return "stride";
	}

    public String view(HttpServletRequest request, String page) {
        return "themes/" + getTheme(request) + "/" + page;
    }

    public String fragment(HttpServletRequest request, String fragment) {
        return "themes/" + getTheme(request) + "/fragments/" + fragment;
    }
}