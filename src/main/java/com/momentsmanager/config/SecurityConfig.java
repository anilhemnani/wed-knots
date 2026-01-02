package com.momentsmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String requestUri = request.getRequestURI();

            // Redirect to appropriate login page based on the requested resource
            if (requestUri.startsWith("/admin")) {
                response.sendRedirect("/login/admin");
            } else if (requestUri.startsWith("/host")) {
                response.sendRedirect("/login/host");
            } else if (requestUri.startsWith("/guest")) {
                response.sendRedirect("/login/guest");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    response.sendRedirect("/admin/dashboard");
                } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HOST"))) {
                    response.sendRedirect("/host/dashboard");
                } else {
                    response.sendRedirect("/guest/dashboard");
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/register", "/css/**", "/js/**", "/set-password", "/set-password-host").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")  // Protect H2 console - admin only
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/host/**").hasRole("HOST")
                .requestMatchers("/guest/**").hasRole("GUEST")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint())
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/")  // Redirect to home if session is invalid
                .maximumSessions(3)  // Allow up to 3 concurrent sessions per user
                .maxSessionsPreventsLogin(false)  // Don't block new logins, expire oldest session
                .expiredUrl("/")  // Redirect if session expired due to concurrent login
            )
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .successHandler(customSuccessHandler())
                .loginProcessingUrl("/do-not-use-spring-login") // disables default /login POST
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)  // Invalidate session on logout
                .deleteCookies("JSESSIONID")  // Delete session cookie
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        // Allow H2 console frames (needed for H2 web console)
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin())
        );

        return http.build();
    }
}
