package ua.dymohlo.PetMusicStorage.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
//http://localhost:8080/swagger-ui/index.html#/ - swagger link
    private final String URL = "http://localhost:8080";
    private final String TITLE = "Pet music storage";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url(URL)))
                .info(new Info().title(TITLE));
    }

}

