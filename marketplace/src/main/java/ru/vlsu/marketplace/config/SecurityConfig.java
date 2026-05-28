package ru.vlsu.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.vlsu.marketplace.services.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/about", "/auth", "/auth/**", "/catalog", "/product/**", "/brands", "/brand/*/logo", "/main.css", "/css/**", "/js/**", "/images/**", "/photoformain/**", "/brends/**", "/product/*/image", "/error").permitAll()
                .requestMatchers("/cart/**", "/orders/**", "/favorites/**", "/profile/**", "/reviews/**").authenticated()
                .requestMatchers("/seller/**").hasAnyAuthority("ROLE_seller", "ROLE_admin")
                .requestMatchers("/moderation/**").hasAnyAuthority("ROLE_moderator", "ROLE_admin")
                .requestMatchers("/admin/**").hasAuthority("ROLE_admin")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/profile/logout")
                .logoutSuccessUrl("/")
            )
            .rememberMe(remember -> remember
                .tokenValiditySeconds(86400)
                .key("marketplaceSecretKey")
                .userDetailsService(userDetailsService)
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
