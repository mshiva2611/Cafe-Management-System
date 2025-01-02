package com.inn.cafe.JWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class JwtUtil {
	
	private String secret = "shivammishrahjgfxhfdfdsdeadsddsdawd123456789";
	
	
	public String extractUsername(String token) {
		log.info("JwtUtil || Inside extractUsername method ");
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
    	log.info("JwtUtil || Inside extractExpiration method ");
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    	log.info("JwtUtil || Inside extractClaim method ");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @SuppressWarnings("deprecation")
	public Claims extractAllClaims(String token) {
    	log.info("JwtUtil || Inside extractAllClaims method ");
        return Jwts
                .parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
    	log.info("JwtUtil || Inside isTokenExpired method ");
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username, String role) {
    	log.info("JwtUtil || Inside generateToken method ");
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    @SuppressWarnings("deprecation")
	private String createToken(Map<String, Object> claims, String subject) {
    	log.info("JwtUtil || Inside createToken method ");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))

                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

//    public Boolean validateToken(String token, String username) {
//        final String extractedUsername = extractUsername(token);
//        return (extractedUsername.equals(username) && !isTokenExpired(token));
//    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
    	log.info("JwtUtil || Inside validateToken method ");
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
