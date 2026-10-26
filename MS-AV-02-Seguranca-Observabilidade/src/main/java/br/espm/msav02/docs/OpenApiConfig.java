package br.espm.msav02.docs;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * 01 - Documentação de endpoints (OpenAPI 3 + Swagger UI)
 *
 * Swagger UI:  http://localhost:8080/swagger-ui.html
 * JSON OpenAPI: http://localhost:8080/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "MS-AV-02 - Segurança e Observabilidade",
        version = "1.0.0",
        description = "API de login com bloqueio por tentativas, roles via JdbcUserDetailsManager e métricas para Prometheus/Grafana."))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
