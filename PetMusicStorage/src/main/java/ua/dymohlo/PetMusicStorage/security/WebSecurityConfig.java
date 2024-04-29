package ua.dymohlo.PetMusicStorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
    private final JWTTokenConfig jwtTokenConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().disable()
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/host_page", "/register", "/login").permitAll()
                        .antMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .antMatchers("/personal_office/**").authenticated()
                        .antMatchers(HttpMethod.GET, "/main").permitAll()
                        .antMatchers("/free_subscription").hasAnyRole("FREE", "OPTIMAL", "MAXIMUM", "ADMIN")
                        .antMatchers("/optimal_subscription").hasAnyRole("MAXIMUM", "OPTIMAL", "ADMIN")
                        .antMatchers("/maximum_subscription").hasAnyRole("MAXIMUM", "ADMIN")
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore((Filter) jwtTokenConfig, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}