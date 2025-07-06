package org.sikawofie.authservice.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("bearerAuth"))  .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info()
                        .title("Authentication Service API")
                        .description("API documentation for the Authentication Service application")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Roland Antwi")
                                .url("https://sikawofie.com")
                                .email("rolandantwisenior47@gmail.com")));
    }




    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/admin/**")
                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(PreAuthorize.class) &&
                        method.getAnnotation(PreAuthorize.class).value().contains("ADMIN"))
                .build();
    }
}
