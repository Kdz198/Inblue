package fpt.org.inblue.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SwaggerConfig {

    @Value("${app.openapi.prod-url}")
    private String prodUrl;

    @Value("${app.openapi.dev-url}")
    private String devUrl;

    @Value("${SWAGGER_USERNAME:}")
    private String swaggerUsername;

    @Value("${SWAGGER_PASSWORD:}")
    private String swaggerPassword;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url(prodUrl).description("Production Server"),
                        new Server().url(devUrl).description("Local Development")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    @Profile("prod")
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder)
            throws Exception {

        UserDetails admin = User.builder()
                .username(swaggerUsername)
                .password(passwordEncoder.encode(swaggerPassword))
                .roles("SWAGGER_ADMIN")
                .build();
        UserDetailsService localUserManager = new InMemoryUserDetailsManager(admin);

        http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(localUserManager);

        return http.build();
    }
}
