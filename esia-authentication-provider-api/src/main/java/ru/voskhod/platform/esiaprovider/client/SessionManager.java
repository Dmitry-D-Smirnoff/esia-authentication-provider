package ru.voskhod.platform.esiaprovider.client;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import ru.voskhod.platform.core.security.client.PlatformSessionManager;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionManager extends PlatformSessionManager {

    public static final String ENV_NAME = "ESIA_AUTHENTICATION_PROVIDER_SYSTEM_TOKEN";

    @Override
    protected String provideSessionToken() {
        val token = System.getenv(ENV_NAME);
        if (StringUtils.isBlank(token)) {
            throw new IllegalStateException(
                    "Не обнаружена переменная окружения, содержащая сессионный токен для ЕСИА-провайдера (" + ENV_NAME + ")");
        }

        return token;
    }
}
