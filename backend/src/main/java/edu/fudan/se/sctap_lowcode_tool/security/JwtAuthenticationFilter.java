package edu.fudan.se.sctap_lowcode_tool.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求中获取 JWT Token
            String jwt = jwtTokenProvider.getJwtFromRequest(request);

            // 验证 Token
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 如果 Token 有效，则获取用户身份信息并设置到 Spring Security 上下文
                var authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // 如果出现异常，这里可以进行日志记录或者其他处理
            logger.error("Cannot set user authentication in security context", ex);
        }

        // 继续过滤链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}
