package com.example.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public JwtAuthenticationToken(String username) {
        super(username, null);  // Không có mật khẩu, chỉ cần username
    }

    // Nếu bạn muốn thêm quyền cho user, có thể thêm vào constructor này
    public JwtAuthenticationToken(String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, null, authorities);
    }
} 