package com.finrizika.app;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.startsWith("/api/users/login") ||
                path.startsWith("/assets") ||
                path.equals("/login") ||
                path.contains(".")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("id") == null) {
            if (request.getRequestURI().startsWith("/api")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else {
                response.sendRedirect("/login");
            }
        }

        return true;
    }

}
