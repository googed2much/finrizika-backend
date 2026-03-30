package com.finrizika.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${app.dev-mode:false}")
    private boolean DEV_MODE;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // FOR TESTING PURPOSES ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if(DEV_MODE){
            HttpSession stest = request.getSession(true);
            if (stest.getAttribute("id") == null) {
                stest.setAttribute("id", 1L);
            }
            return true;
        }
        // ------------------------------------------------------

        String path = request.getRequestURI();

        if (path.equals("/login") || path.startsWith("/api/users/login") || path.startsWith("/favicon.ico") || path.startsWith("/assets") || path.matches(".*\\.(css|js|png|jpg|ico|html)$")) {
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
            return false;
        }

        return true;
    }

}
