package ru.voskhod.platform.esiaprovider;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.IOUtils;
import ru.voskhod.platform.common.exception.ErrorMessageDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class OpenApiFilterBase extends AbstractSpecFilter {

    /**
     * Метод выполняет сортировку схем и путей в генерируемой спецификации OpenAPI. Вначале
     * выполняется попытка выполнить сортировку на основании образца, заданного параметром
     * {@literal templateName} (выполняется поиск ресурса в META-INF/[templateName]). Если
     * попытка не удаётся, будет выполнена обычная сортировка в алфавитном порядке.
     * <br/><br/>
     * В качестве образца обычно используется исходная спецификация (при подходе specification-first).
     * @param openAPI      Спецификация, которую нужно сортировать
     * @param templateName Имя файла, содержащего образец для сортировки
     */
    protected void sort(OpenAPI openAPI, String templateName) {

        boolean sorted = false;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/" + templateName)) {
            if (is != null) {
                String yaml = IOUtils.toString(is, StandardCharsets.UTF_8);
                OpenAPI template = new OpenAPIV3Parser().readContents(yaml).getOpenAPI();

                sortSchemas(openAPI, template);
                sortPaths(openAPI, template);

                sorted = true;
            }
        } catch (IOException ignore) {}

        if (!sorted) {
            sortSchemas(openAPI);
            sortPaths(openAPI);
        }

    }

    protected void sortPaths(OpenAPI openAPI) {
        openAPI.setPaths(
                openAPI.getPaths().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue,
                                Paths::new))
        );
    }

    protected void sortSchemas(OpenAPI openAPI) {
        openAPI.getComponents().setSchemas(
                openAPI.getComponents().getSchemas().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new))
        );
    }

    protected void sortSchemas(OpenAPI openAPI, OpenAPI template) {
        Map<String, Integer> index = index(template.getComponents().getSchemas());

        openAPI.getComponents().setSchemas(
                openAPI.getComponents().getSchemas().entrySet().stream()
                        .sorted((s1, s2) -> compare(index.get(s1.getKey()), index.get(s2.getKey())))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new))
        );
    }

    protected void sortPaths(OpenAPI openAPI, OpenAPI template) {
        Map<String, Integer> index = index(template.getPaths());

        openAPI.setPaths(
                openAPI.getPaths().entrySet().stream()
                        .sorted((s1, s2) -> compare(index.get(s1.getKey()), index.get(s2.getKey())))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue,
                                Paths::new))
        );
    }

    private Map<String, Integer> index(Map<String, ?> components) {
        Map<String, Integer> index = new HashMap<>();
        int i = 0;
        for (String key: components.keySet()) {
            index.put(key, i++);
        }
        return index;
    }

    private int compare(Integer v1, Integer v2) {
        return (v1 != null ? v1 : new Integer(-1)).compareTo(v2 != null ? v2 : -1);
    }


    protected void addBearerSecurityScheme(OpenAPI openAPI) {

        Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
        if (securitySchemes == null) {
            securitySchemes = new LinkedHashMap<>();
            openAPI.getComponents().setSecuritySchemes(securitySchemes);
        }
        securitySchemes.put("bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("json"));
    }

    protected void addFaultResponses(OpenAPI openAPI) {
        Map<String, ApiResponse> responses = openAPI.getComponents().getResponses();
        if (responses == null) {
            responses = new LinkedHashMap<>();
            openAPI.getComponents().setResponses(responses);
        }
        addFaultResponses(responses);
    }

    protected void addFaultResponses(Map<String, ApiResponse> responses) {

        responses.put("response_400", faultResponse("Некорректные входные данные"));
        responses.put("response_401", faultResponse("Пользователь не аутентифицирован"));
        responses.put("response_403", faultResponse("Действие запрещено"));
        responses.put("response_404", faultResponse("Ресурс не найден"));
        responses.put("response_409", faultResponse("Конфликт данных"));
        responses.put("response_500", faultResponse("Ошибка на сервере"));

    }

    /**
     * Добавляет ответ с кодом 500 ко всем операциям, в которых его не было явно.
     */
    protected void ensureResponse500(OpenAPI openAPI) {
        openAPI.getPaths()
                .values()
                .stream()
                .map(PathItem::readOperations)
                .flatMap(Collection::stream)
                .map(Operation::getResponses)
                .forEach(responses -> {
                    if (!responses.containsKey("500")) {
                        responses.put("500", new ApiResponse().$ref("#/components/responses/response_500"));
                    }
                });
    }


    protected Map<String, Parameter> parameterComponents(OpenAPI openAPI) {
        return initComponents(
                openAPI.getComponents().getParameters(),
                openAPI.getComponents()::setParameters);
    }

    protected Map<String, RequestBody> requestBodyComponents(OpenAPI openAPI) {
        return initComponents(
                openAPI.getComponents().getRequestBodies(),
                openAPI.getComponents()::setRequestBodies);
    }

    protected Map<String, Header> headerComponents(OpenAPI openAPI) {
        return initComponents(
                openAPI.getComponents().getHeaders(),
                openAPI.getComponents()::setHeaders);
    }

    protected Map<String, SecurityScheme> securitySchemeComponents(OpenAPI openAPI) {
        return initComponents(
                openAPI.getComponents().getSecuritySchemes(),
                openAPI.getComponents()::setSecuritySchemes);
    }

    private <C> Map<String, C> initComponents(Map<String, C> components, Consumer<Map<String, C>> setter) {
        if (components == null) {
            components = new LinkedHashMap<>();
            setter.accept(components);
        }
        return components;
    }


    protected static ApiResponse faultResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json",
                                new MediaType().schema(new Schema<ErrorMessageDto>().$ref("#/components/schemas/ErrorMessageDto"))));
    }

}
