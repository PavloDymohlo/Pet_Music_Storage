package ua.dymohlo.PetMusicStorage.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@Slf4j
public class WebSecurityConfig {
    private final JWTTokenConfig jwtTokenConfig;

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf().disable()
            .cors().disable()
            .authorizeRequests(authorize -> authorize
                    .antMatchers("/register", "/host_page", "/login", "/error").permitAll()
                    .antMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    .antMatchers("/personal_office/**").authenticated()
                    .antMatchers("/music/get_music_page").authenticated()
                    .antMatchers(HttpMethod.GET, "/main").permitAll()
                    .antMatchers("/free_subscription").hasAnyRole("FREE", "OPTIMAL", "MAXIMUM", "ADMIN")
                    .antMatchers("/optimal_subscription").hasAnyRole("MAXIMUM", "OPTIMAL", "ADMIN")
                    .antMatchers("/maximum_subscription").hasAnyRole("MAXIMUM", "ADMIN")
                    .antMatchers("/admin_office/**", "/users").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore((Filter) jwtTokenConfig, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.sendRedirect("/error");
            });

    return http.build();
}

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/static/**", "/styles/**", "/images/**", "/js/**","/ico/**","/mp3/**");
    }
}