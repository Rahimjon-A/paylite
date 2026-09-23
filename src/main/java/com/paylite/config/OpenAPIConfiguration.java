package com.paylite.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    private static final String TITLE = "Paylite bailing web service";
    private static final String VERSION = "1.0";
    private static final String DESC = "REST API documentation for the Paylite.";
    private static final String NAME = "Rakhimjon Abdurakhimov";
    private static final String EMAIL = "rakhimjon_abdurakhimov@gmail.com";
    private static final String KEYCLOAK_AUTH = "http://localhost:9080/realms/paylite/protocol/openid-connect/auth";
    private static final String KEYCLOAK_TOKEN = "http://localhost:9080/realms/paylite/protocol/openid-connect/token";
    public static final String KEY = "keycloak";

    @Bean
    public OpenAPI payLiteOpenAPI() {
        return new OpenAPI()
            .info(new Info().title(TITLE).version(VERSION).description(DESC).contact(new Contact().name(NAME).email(EMAIL)))
            .components(
                new Components()
                    .addSecuritySchemes(
                        KEY,
                        new SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                new OAuthFlows().authorizationCode(new OAuthFlow().authorizationUrl(KEYCLOAK_AUTH).tokenUrl(KEYCLOAK_TOKEN))
                            )
                    )
            );
    }
}
