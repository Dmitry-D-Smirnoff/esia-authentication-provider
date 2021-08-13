package ru.voskhod.platform.esiaprovider.logic.test;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;
import ru.voskhod.platform.esiaprovider.logic.ProviderAuthenticationLogic;

import java.net.URI;
import java.net.URISyntaxException;

public class RedirectUriTest {

    @Test
    void test() throws Exception {

        // Абсолютный redirectUri, нет Referer
        URI redirectUri = new URI("http://192.168.104.248/public/authutil/auth/login");
        String result = buildRedirectUri(redirectUri, null);
        Assertions.assertEquals(redirectUri.toString(), result);

        // Абсолютный redirectUri, Referer
        URI referer = new URI("http://localhost:28080/authenticator/api");
        result = buildRedirectUri(redirectUri, referer);
        Assertions.assertEquals(redirectUri.toString(), result);

        // Относительный redirectUri, нет Referer
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> buildRedirectUri(new URI("/public/authutil/auth/login"), null)
        );

        // Относительный redirectUri, относительный Referer
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> buildRedirectUri(
                        new URI("/public/authutil/auth/login"),
                        new URI("/public/authutil/auth/login"))
        );


        redirectUri = new URI("/public/authutil/auth/login");
        result = buildRedirectUri(redirectUri, referer);
        Assertions.assertEquals("http://localhost:28080/public/authutil/auth/login", result);

        redirectUri = new URI("public/authutil/auth/login");
        result = buildRedirectUri(redirectUri, referer);
        Assertions.assertEquals("http://localhost:28080/public/authutil/auth/login", result);

    }

    private String buildRedirectUri(URI redirectUri, URI referer) throws Exception {
        return Whitebox.invokeMethod(ProviderAuthenticationLogic.class, "buildRedirectUri", redirectUri, referer);
    }


    @Test
    void testUriBuilder() throws URISyntaxException {

        // Так делать не надо:
        URI uri = buildURLBuilder("auth?query=1").build();
        System.out.println(uri.toString());
        Assertions.assertEquals("https://esia-portal1.test.gosuslugi.ru/auth%3Fquery=1", uri.toString());

        // Рассчитано, что параметры будут добавляться с помощью специального метода (так правильно):
        uri = buildURLBuilder("auth").addParameter("query", "1").build();
        Assertions.assertEquals("https://esia-portal1.test.gosuslugi.ru/auth?query=1", uri.toString());
        System.out.println(uri.toString());

    }

    private URIBuilder buildURLBuilder(String resourcePath) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost("esia-portal1.test.gosuslugi.ru")
                .setPath(resourcePath);
        return builder;
    }

}
