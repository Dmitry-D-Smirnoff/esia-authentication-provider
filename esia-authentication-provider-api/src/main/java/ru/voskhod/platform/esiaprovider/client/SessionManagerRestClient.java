package ru.voskhod.platform.esiaprovider.client;

import ru.voskhod.platform.core.security.client.PlatformRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.SessionRequestDtoOut;
import ru.voskhod.platform.esiaprovider.client.dto.SessionResponseDtoIn;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.UUID;

import static java.lang.String.format;

@ApplicationScoped
public class SessionManagerRestClient {

    private final static String SESSION_MANAGER_URI = "http://" + PlatformRestClient.ROUTER_HOST_PORT + "/session-manager/api";


    @Inject
    private SessionManager sessionManager;

    private PlatformRestClient client;


    @PostConstruct
    private void init() {
        client = new PlatformRestClient(sessionManager);
    }

    @PreDestroy
    public void closeConnection() {
        client.closeConnection();
    }


    public SessionResponseDtoIn provideUserSession(UUID currentUserId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.post(
                format("%1$s/v2/sessions/provide/%2$s", SESSION_MANAGER_URI, currentUserId.toString()),
                new SessionRequestDtoOut("{\"ESIA\": true}"));

        int httpCode = response.httpResponse.getStatusLine().getStatusCode();
        if (httpCode == 200 || httpCode == 529) {
            return response.getEntity(SessionResponseDtoIn.class);

        } else {
            throw new RuntimeException(MessageFormat.format(
                    "От менеджера сессий получен неизвестный код возврата {0}",
                    httpCode));
        }
    }

    public void deleteUserSession(UUID userSessionId) throws IOException, URISyntaxException {
        PlatformRestClient.Response response = client.delete(
                format("%s/sessions/%s/delete", SESSION_MANAGER_URI, userSessionId.toString()));
        response.assertStatusCode(200);
    }

}
