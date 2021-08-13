package ru.voskhod.platform.esiaprovider.esia;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import ru.voskhod.platform.core.security.client.PlatformSessionManager;
import ru.voskhod.platform.core.security.client.StatusAssertionFailed;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.function.Function;

/**
 * Класс отвечает за выполнение отправки rest-вызовов к ЕСИА. Внутри инкапсулирует токен доступа
 * к ЕСИА, поэтому экземпляр этого класса должен создаваться для каждого запроса на создание сессии.
 * После этого его необходимо закрыть вызовом метода {@link #close()}.
 */
@RequiredArgsConstructor
public class EsiaRestClient implements Closeable {

    private final String token;
    private final CloseableHttpClient client = HttpClients.createDefault();

    private RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectTimeout(10000)
            .setConnectionRequestTimeout(10000)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void setConnectionTimeout(int timeout) {
        this.requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
    }

    private <R extends HttpRequestBase> Response request(Function<URI, R> requestProducer, String url) throws URISyntaxException, IOException {

        R request = requestProducer.apply(new URI(url));
        request.setConfig(requestConfig);

        PlatformSessionManager.applySessionToken(request, token);
        try (CloseableHttpResponse response = client.execute(request)) {
            return new Response(response);
        }
    }


    public Response get(String url) throws IOException, URISyntaxException {
        return request(HttpGet::new, url);
    }


    public class Response {

        public final HttpResponse httpResponse;
        public final byte[] entity;

        public Response(HttpResponse httpResponse) throws IOException {
            this.httpResponse = httpResponse;
            this.entity = IOUtils.toByteArray(httpResponse.getEntity().getContent());
        }

        public <E> E getEntity(Class<E> entityClass) {
            try {
                return objectMapper.readValue(entity, entityClass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String getEntityString() {
            return new String(entity);
        }

        public void assertStatusCode(int statusCode) {

            if (httpResponse.getStatusLine().getStatusCode() != statusCode) {

                String base = MessageFormat.format(
                        "Результат вызова {0} не равен ожидаемому {1}",
                        httpResponse.getStatusLine().getStatusCode(),
                        statusCode);

                String message = getEntityString();
                if (message == null || message.isEmpty()) {
                    message = httpResponse.getStatusLine().getReasonPhrase();
                }
                throw new StatusAssertionFailed(base + "\n" + message);
            }
        }

    }


    public void close() throws IOException {
        client.close();
    }

}
