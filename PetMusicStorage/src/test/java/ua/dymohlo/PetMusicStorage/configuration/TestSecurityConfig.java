package ua.dymohlo.PetMusicStorage.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/auto_renew_subscription", "/login","/payment").permitAll()
                .antMatchers("/admin_office/**").hasRole("ADMIN")
                .antMatchers("/free_subscription").authenticated()
                .anyRequest().permitAll();
        return http.build();
    }
}