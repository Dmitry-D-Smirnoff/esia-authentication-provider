package ru.voskhod.platform.esiaprovider.test.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaUserInfo;

import java.io.IOException;

class EsiaUserInfoTest {

    @Test
    void fromJsonTest() throws IOException {

        String jsonString = IOUtils.resourceToString("/EsiaUserInfo.json", null);
        Assertions.assertDoesNotThrow(() -> new ObjectMapper().readValue(jsonString, EsiaUserInfo.class));

    }

}