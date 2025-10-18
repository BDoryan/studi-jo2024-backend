package studi.doryanbessiere.jo2024.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration file for OpenAPI (Swagger) documentation.
 * This class sets up the basic information about the API,
 * including title, description, version, contact, and license.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jo2024OpenAPI() {
        OpenAPI open_api = new OpenAPI();

        Server local_server = new Server();
        local_server.setUrl("http://localhost:8080");
        local_server.setDescription("Serveur local de développement");

        Server production_server = new Server();
        production_server.setUrl("https://jo2024-api.doryanbessiere.fr");
        production_server.setDescription("Serveur de production");

        open_api.addServersItem(local_server);
        open_api.addServersItem(production_server);

        open_api.setInfo(new Info()
                        .title("API Jeux Olympiques 2024")
                        .description("""
                                Bienvenue sur la documentation de l'API des Jeux Olympiques 2024 !
                                
                                Remplir des informations
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Doryan Bessiere")
                                .email("doryanbessiere.pro@gmail.com")
                                .url("https://www.doryanbessiere.fr")));

        open_api.setExternalDocs(new ExternalDocumentation()
                .description("Dépôt GitHub du projet JO 2024 (Backend)")
                .description("Dépôt GitHub du projet")
                .url("https://github.com/BDoryan/studi-jo2024-backend"));

        return open_api;
    }
}
