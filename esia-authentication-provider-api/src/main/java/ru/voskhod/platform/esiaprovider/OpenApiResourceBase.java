package ru.voskhod.platform.esiaprovider;

import io.swagger.v3.core.filter.OpenAPISpecFilter;
import io.swagger.v3.core.filter.SpecFilter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static io.swagger.v3.jaxrs2.integration.ServletConfigContextUtils.getContextIdFromServletConfig;

public abstract class OpenApiResourceBase {

    private static Logger LOGGER = LoggerFactory.getLogger(OpenApiResourceBase.class);

    protected Response getOpenApi(HttpHeaders headers,
                                  ServletConfig config,
                                  Application app,
                                  UriInfo uriInfo,
                                  String type) throws Exception {

        String ctxId = getContextIdFromServletConfig(config);
        OpenApiContext ctx = new JaxrsOpenApiContextBuilder()
                .servletConfig(config)
                .application(app)
                .resourcePackages(resourcePackages)
                .configLocation(configLocation)
                .openApiConfiguration(openApiConfiguration)
                .ctxId(ctxId)
                .buildContext(true);
        OpenAPI oas = ctx.read();
        boolean pretty = false;
        if (ctx.getOpenApiConfiguration() != null && Boolean.TRUE.equals(ctx.getOpenApiConfiguration().isPrettyPrint())) {
            pretty = true;
        }

        if (oas != null) {
            if (ctx.getOpenApiConfiguration() != null && ctx.getOpenApiConfiguration().getFilterClass() != null) {
                try {
                    OpenAPISpecFilter filterImpl = (OpenAPISpecFilter) Class.forName(ctx.getOpenApiConfiguration().getFilterClass()).newInstance();
                    SpecFilter f = new SpecFilter();
                    oas = f.filter(oas, filterImpl, getQueryParams(uriInfo.getQueryParameters()), getCookies(headers),
                            getHeaders(headers));
                } catch (Exception e) {
                    LOGGER.error("failed to load filter", e);
                }
            }
        }

        if (oas == null) {
            return Response.status(404).build();
        }

        if (StringUtils.isNotBlank(type) && type.trim().equalsIgnoreCase("yaml")) {
            return Response.status(Response.Status.OK)
                    .entity(pretty ? indentArrays(Yaml.pretty(oas)) : Yaml.mapper().writeValueAsString(oas))
                    .type("application/yaml")
                    .build();
        } else {
            return Response.status(Response.Status.OK)
                    .entity(pretty ? Json.pretty(oas) : Json.mapper().writeValueAsString(oas))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
    }

    private String indentArrays(String oas) {
        Stack<Integer> incStack = new Stack<>();

        int indentPre = 0;
        int indentInc = 0;

        StringBuilder result = new StringBuilder();
        for (String line : oas.split("\n")) {

            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') {
                indent++;
            }

            int arrayInc = line.charAt(indent) == '-' ? 2 : 0;
            indent += arrayInc;

            if (indent > indentPre) {
                incStack.push(indentInc);
                indentInc += arrayInc;
                indentPre = indent;
            } else if (indent < indentPre) {
                for (int i = 0; i < indentPre - indent; i += 2) {
                    indentInc = incStack.pop();
                }
                indentPre = indent;
            }

            if (indentInc > 0) {
                result.append(StringUtils.repeat(' ', indentInc));
            }
            result.append(line).append('\n');

        }

        return result.toString();
    }

    private static Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
        Map<String, List<String>> output = new HashMap<String, List<String>>();
        if (params != null) {
            for (String key : params.keySet()) {
                List<String> values = params.get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    private static Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap<String, String>();
        if (headers != null) {
            for (String key : headers.getCookies().keySet()) {
                Cookie cookie = headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            }
        }
        return output;
    }

    private static Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap<String, List<String>>();
        if (headers != null) {
            for (String key : headers.getRequestHeaders().keySet()) {
                List<String> values = headers.getRequestHeaders().get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    protected String configLocation;

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public OpenApiResourceBase configLocation(String configLocation) {
        setConfigLocation(configLocation);
        return this;
    }

    protected Set<String> resourcePackages;

    public Set<String> getResourcePackages() {
        return resourcePackages;
    }

    public void setResourcePackages(Set<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    public OpenApiResourceBase resourcePackages(Set<String> resourcePackages) {
        setResourcePackages(resourcePackages);
        return this;
    }

    protected OpenAPIConfiguration openApiConfiguration;

    public OpenAPIConfiguration getOpenApiConfiguration() {
        return openApiConfiguration;
    }

    public void setOpenApiConfiguration(OpenAPIConfiguration openApiConfiguration) {
        this.openApiConfiguration = openApiConfiguration;
    }

    public OpenApiResourceBase openApiConfiguration(OpenAPIConfiguration openApiConfiguration) {
        setOpenApiConfiguration(openApiConfiguration);
        return this;
    }

}
