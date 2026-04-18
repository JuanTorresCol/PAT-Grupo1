package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/pistaPadel/auth/**").permitAll()
                        .requestMatchers("/pistaPadel/admin/**").permitAll()
                        .requestMatchers("/pistaPadel/reservations/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}