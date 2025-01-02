package com.inn.cafe.JWT;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
	
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	CustomerUserDetailsService customerUserDetailsService;
    
	Claims claims = null;
    String username = null;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.info("JwtFilter || doFilterInternal method invoked");
		
		// List of URLs to bypass the filter
        String[] bypassUrls = {"/user/login", "/user/signup", "/user/forgotPassword"};

        // Check if the current request URI matches any of the bypass URLs
        String requestURI = request.getRequestURI();
        for (String url : bypassUrls) {
        	
            if (requestURI.contains(url)) {
            	log.info("JwtFilter || By passing this request");
            	filterChain.doFilter(request, response);  // Bypass the filter for this request
                return;
            }
        }

        final String requestTokenHeader = request.getHeader("Authorization");

  
        String jwtToken = null;


        // JWT Token is in the form "Bearer token". Remove Bearer word and get the token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
                claims = jwtUtil.extractAllClaims(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            } catch (SignatureException e) {
                System.out.println("JWT Token signature validation failed");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Once we get the token, validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.customerUserDetailsService.loadUserByUsername(username);

            // if token is valid configure Spring Security to manually set authentication
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
	
		
	}
	
	public boolean isAdmin() {
		return "admin".equalsIgnoreCase((String)claims.get("role"));
	}
	
	public boolean isUser() {
		return "user".equalsIgnoreCase((String)claims.get("role"));
	}
	
	public String getCurrentUser() {
		return username;
	}

}
