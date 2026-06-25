package restaurant_service.com.restaurant_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter ;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
    {
        return http.csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/api/restaurants/ping", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/**", "/api/menu-items/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/restaurants").hasAnyAuthority("ADMIN", "RESTAURANT")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/**").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/**").hasAnyAuthority("ADMIN", "RESTAURANT")

                        .requestMatchers(HttpMethod.POST, "/api/menu-items/**").hasAuthority("RESTAURANT")
                        .requestMatchers(HttpMethod.PUT, "/api/menu-items/**").hasAuthority("RESTAURANT")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu-items/**").hasAuthority("RESTAURANT")

                        .anyRequest().authenticated()
                ).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class).
                sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
