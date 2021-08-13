package ru.voskhod.platform.esiaprovider.test.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.undertow.util.Headers;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import ru.voskhod.platform.core.security.Pau;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

class IntegrationTest {

    private static final boolean DEBUG = true;

    protected static String AUTHENTICATOR_URI;

    protected static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @BeforeAll
    static void runContainers() {

        if (DEBUG) {

            AUTHENTICATOR_URI = "http://localhost:28080/authenticator/api";

        } else {

//            Network network = Network.newNetwork();
//
//            GenericContainer db = new GenericContainer<>("kbakaras/security-db-test")
//                    .withNetwork(network)
//                    .withNetworkAliases("security-postgres")
//                    .withExposedPorts(5432)
//                    .waitingFor(Wait.forListeningPort());
//
//            GenericContainer authenticator = new GenericContainer<>("kbakaras/authenticator-test")
//                    .withNetwork(network)
//                    .withExposedPorts(8080, 8787);
//
//            db.start();
//
//            authenticator.start();

//            BASE_URI = "http://0.0.0.0:" + authenticator.getMappedPort(8080) + "/authenticator/api";

        }

    }

    protected String getSessionToken(HttpClient client) throws IOException {

        HttpUriRequest requestSession = new HttpPost(AUTHENTICATOR_URI + "/internalauth/auth");

        byte[] basicToken = Base64.getEncoder().encode(
                ("sysadmin:12345678").getBytes(StandardCharsets.UTF_8)
        );
        requestSession.setHeader(Headers.AUTHORIZATION_STRING, Pau.AUTH_BASIC + " " + new String(basicToken));

        HttpResponse responseSession = client.execute(requestSession);

        JsonNode node = objectMapper.readTree(responseSession.getEntity().getContent());
        return node.get("sessionToken").asText();

    }

    protected void runTest(Consumer<RequestContext> context) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

            String sessionToken = getSessionToken(client);

            context.accept(request -> {
                try {
                    request.setHeader(Headers.AUTHORIZATION_STRING, Pau.AUTH_BEARER + " " + sessionToken);
                    return client.execute(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setEntity(HttpEntityEnclosingRequest request, Object entity) {
        try {

            request.setHeader(Headers.CONTENT_TYPE_STRING, ContentType.APPLICATION_JSON.toString());
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(entity)));

        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected JsonNode getEntity(HttpResponse response) {
        try {

            return objectMapper.readTree(response.getEntity().getContent());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    protected interface RequestContext {
        HttpResponse execute(HttpUriRequest request);
    }

}
