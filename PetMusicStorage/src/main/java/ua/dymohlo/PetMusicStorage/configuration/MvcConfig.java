package ua.dymohlo.PetMusicStorage.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/styles/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry
                .addResourceHandler("/images/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry
                .addResourceHandler("/js/script/**")
                .addResourceLocations("classpath:/static/script/");
        registry
                .addResourceHandler("/ico/**")
                .addResourceLocations("classpath:/static/ico/");
        registry
                .addResourceHandler("/mp3/**")
                .addResourceLocations("classpath:/static/mp3/");
    }
}