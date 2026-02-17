package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// Permite usar @PreAuthorize en el controlador

public class SecurityConfig{
    @Bean
    public SecurityFilterChain configuration(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("pistaPadel/auth/**"))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("pistaPadel/auth/register", "/pistaPadel/auth/login", "/pistaPadel/auth/me").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    public UserDetailsService usuarios(){
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("usuario")
                .password("password123")
                .roles("USER")
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password123")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
