package ru.voskhod.platform.esiaprovider.client;

import lombok.NonNull;
import lombok.val;
import ru.voskhod.platform.core.security.client.PlatformRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.SegmentDto;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static java.lang.String.format;

@ApplicationScoped
public class SegmentRegistryRestClient {

    private final static String SEGMENT_REGISTRY_URI = "http://" + PlatformRestClient.ROUTER_HOST_PORT + "/segment-registry/api";

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


    public SegmentDto findSegmentById(@NonNull UUID segmentId) throws IOException, URISyntaxException {

        val id = URLEncoder.encode(segmentId.toString(), StandardCharsets.UTF_8.name());

        PlatformRestClient.Response response = client.get(
                format("%s/segments/%s", SEGMENT_REGISTRY_URI, id));

        response.assertStatusCode(200);

        return response.getEntity(SegmentDto.class);
    }

}
