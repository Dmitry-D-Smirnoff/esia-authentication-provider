package ru.voskhod.platform.esiaprovider.client;

import ru.voskhod.platform.core.security.client.PlatformRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.FileSignResultDto;
import ru.voskhod.platform.esiaprovider.client.dto.VerifySignatureDataRequest;
import ru.voskhod.platform.esiaprovider.client.dto.VerifySignatureDataResponse;
import ru.voskhod.platform.esiaprovider.logic.PlatformSettings;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;

@ApplicationScoped
public class CryptoServiceRestClient {

    @EJB
    private PlatformSettings settings;

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


    public FileSignResultDto sign(byte[] data) throws IOException, URISyntaxException {
        PlatformRestClient.Response response = client.post(settings.esia.gostCryptoServiceUrl.value(), data);
        response.assertStatusCode(200);
        return response.getEntity(FileSignResultDto.class);
    }

    public VerifySignatureDataResponse verify(VerifySignatureDataRequest request) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.post(settings.esia.gostCryptoServiceVerifyUrl.value(), request);
        response.assertStatusCode(200);
        return response.getEntity(VerifySignatureDataResponse.class);

    }

}
