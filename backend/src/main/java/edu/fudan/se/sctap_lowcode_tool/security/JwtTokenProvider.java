package edu.fudan.se.sctap_lowcode_tool.security;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "mySecretKey";
    private final long EXPIRATION_TIME = 86400000; // 1 天

    public String createToken(String username, Set<ProjectInfo> projects) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("projects", projects);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

