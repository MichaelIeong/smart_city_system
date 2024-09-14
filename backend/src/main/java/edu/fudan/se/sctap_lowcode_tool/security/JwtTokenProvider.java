package edu.fudan.se.sctap_lowcode_tool.security;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "YourVeryLongSecretKeyAtLeast32Bytes";
    private final long EXPIRATION_TIME = 86400000; // 1 天

    // 创建 JWT Token
    public String createToken(String username, Set<ProjectInfo> projects) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("projects", projects);

        // 打印用于调试
        System.out.println("Creating JWT token for user: " + username);

        // 创建密钥
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 从 JWT Token 中提取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}