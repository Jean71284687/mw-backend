package mweb.mw_backend.config;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthenticationProvider authProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // API públicas
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers("/api/categories/**").permitAll()

                                                // Web públicas
                                                .requestMatchers("/", "/web/products/**", "/web/categories/**")
                                                .permitAll()
                                                .requestMatchers("/web/auth/login", "/web/auth/register",
                                                                "/web/auth/logout", "/web/auth/test-register")
                                                .permitAll()

                                                // Web privadas
                                                .requestMatchers("/web/cart/**", "/web/orders/**", "/web/wishlist/**",
                                                                "/web/reviews/**")
                                                .authenticated()

                                                // Recursos estáticos
                                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                                                .anyRequest().authenticated())
                                // 👇 Permitir sesión para formularios web
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                                .authenticationProvider(authProvider)
                                // 👇 Evitar que el filtro JWT intercepte las rutas /web/**
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
