package somsomcore.zipcheck.global.security;

import somsomcore.zipcheck.global.config.CorsConfig;
import somsomcore.zipcheck.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] WHITELIST = {
            "/", "/swagger/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/auth/**", "/health"
    };

    private static final String[] GET_WHITELIST = {
            "/api/test/**", "/api/report/addrList"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers(WHITELIST).permitAll()
                                .requestMatchers("/api/pdfs/**").permitAll()  // 파일 관련 테스트를 위해 추가
                                .requestMatchers(HttpMethod.GET, GET_WHITELIST).permitAll()
                                .requestMatchers("/api/**").authenticated()
                                .anyRequest().authenticated()
                )
                .addFilter(corsConfig.corsFilter())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}