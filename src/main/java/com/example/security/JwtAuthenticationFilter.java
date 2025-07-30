package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Bỏ qua filter cho các endpoint auth
        String path = request.getRequestURI();  
        if (path.startsWith("/api/auth/")) {
            System.out.println("Bypass JWT filter for: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");

        // Kiểm tra xem token có hợp lệ và bắt đầu với "Bearer "
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Lấy phần token sau "Bearer "

            try {
                // Lấy email từ token
                String email = jwtTokenUtil.extractUsername(token);

                // Kiểm tra nếu email không null và Authentication chưa được thiết lập trong SecurityContext
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Kiểm tra tính hợp lệ của token
                    if (jwtTokenUtil.validateToken(token, email)) {
                        // Tạo authorities mặc định là USER (có thể cải thiện sau)
                        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                        
                        // Tạo đối tượng Authentication với UsernamePasswordAuthenticationToken
                        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

                        // Thiết lập thông tin chi tiết cho Authentication
                        ((AbstractAuthenticationToken) authentication).setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Đặt Authentication vào SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        System.out.println("JWT Authentication successful for: " + email);
                    }
                }
            } catch (Exception ex) {
                System.err.println("JWT Authentication failed: " + ex.getMessage());
            }
        }

        // Tiếp tục chuỗi bộ lọc
        filterChain.doFilter(request, response);
    }
} 