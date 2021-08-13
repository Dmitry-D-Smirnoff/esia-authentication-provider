package ru.voskhod.platform.esiaprovider;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class OpenapiFilter extends OpenApiFilterBase {

    @Override
    public Optional<OpenAPI> filterOpenAPI(OpenAPI openAPI, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {

        addFaultResponses(openAPI);

        headerComponents(openAPI).put("session_token",
                new Header()
                        .description("httpOnly-кука SESSION_TOKEN со значением сессионного токена")
                        .schema(new StringSchema()));

        securitySchemeComponents(openAPI).put("bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("json"));

        sort(openAPI, "esia-authentication-provider-openapi.yaml");

        ensureResponse500(openAPI);

        return Optional.of(openAPI);

    }
}
