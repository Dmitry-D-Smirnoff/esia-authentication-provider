package ru.voskhod.platform.esiaprovider.security;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * Класс предназначен для подключения к приложению механизма аутентификации Аутентификатора
 * ({@link AuthenticatorAuthenticationMechanism}). Для этого его нужно объявить в виде сервиса
 * в папке {@literal META-INF/services}.
 */
@SuppressWarnings("unused")
public class AuthenticatorAuthenticationServletExtension implements ServletExtension {

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addAuthenticationMechanism(
                AuthenticatorAuthenticationMechanism.AUTH_MECHANISM_NAME,
                new AuthenticationMechanismFactory() {
                    @Override
                    public AuthenticationMechanism create(String mechanismName, IdentityManager identityManager, FormParserFactory formParserFactory, Map<String, String> properties) {
                        return new AuthenticatorAuthenticationMechanism();
                    }
                }
        );
    }

}
