package ru.voskhod.platform.esiaprovider.integration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import ru.voskhod.platform.core.security.client.PlatformRestClient;
import ru.voskhod.platform.esiaprovider.client.SessionManager;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaContactsInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaUserInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

@EnabledIfSystemProperty(named = "ESIA_ACCESS_TOKEN", matches = ".+")
public class EsiaClientTest {

    private final String accessToken = System.getenv("ESIA_ACCESS_TOKEN");


    @Test
    void test() throws IOException, URISyntaxException {

        String jwtClaimUserIdKey = "urn:esia:sbj_id";


        DecodedJWT decoded = JWT.decode(accessToken);
        Long userId = decoded.getClaim(jwtClaimUserIdKey).asLong();

        PlatformRestClient client = new PlatformRestClient(new SessionManager() {
            @Override
            protected String provideSessionToken() {
                return accessToken;
            }
        });

        String schema = "https://";
        String esiaHost = "esia-portal1.test.gosuslugi.ru";
        String resourceUser     = MessageFormat.format("/rs/prns/{0}", userId.toString());
        String resourceContacts = MessageFormat.format("/rs/prns/{0}/ctts?embed=(elements)", userId.toString());

        PlatformRestClient.Response response;

        response = client.get(schema + esiaHost + resourceUser);
        assertCode(response, 200);
        EsiaUserInfo userInfo = response.getEntity(EsiaUserInfo.class);

        response = client.get(schema + esiaHost + resourceContacts);
        assertCode(response, 200);
        EsiaContactsInfo contacts = response.getEntity(EsiaContactsInfo.class);

        System.out.println(contacts.elements[0].id);
    }

    protected void assertCode(PlatformRestClient.Response response, int code) {
        Assertions.assertEquals(code, response.httpResponse.getStatusLine().getStatusCode(), "http-result");
    }

}
