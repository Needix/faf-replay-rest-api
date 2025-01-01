package de.needix.games.faf.replay.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Configure authorization rules
                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/api/v1/replays/analyse/**").authenticated()  // Require authentication for this endpoint
                                .anyRequest()
                                .permitAll()  // Allow all other requests without authentication
                )
                .formLogin(login -> login
                        .loginPage("/login").permitAll() // Custom login page (optional)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // Custom logout endpoint (optional)
                        .logoutSuccessUrl("/") // Redirect after logout
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults()); // Enable basic authentication (for testing or APIs)

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Add in-memory users with roles and authorities (used for testing)
        var user1 = User.withUsername("admin")
                .password(passwordEncoder().encode("admin_password"))
                .roles("ADMIN") // Grant ROLE_ADMIN
                .build();

        var user2 = User.withUsername("user")
                .password(passwordEncoder().encode("user_password"))
                .roles("USER")  // Grant ROLE_USER
                .build();

        return new InMemoryUserDetailsManager(user1, user2);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt password encoding for security
        return new BCryptPasswordEncoder();
    }
}