package ru.voskhod.platform.esiaprovider.security;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;
import ru.voskhod.platform.core.security.IdentityContext;
import ru.voskhod.platform.core.security.Pau;

import java.util.Optional;

/**
 * Механизм аутентификации для Аутентификатора. Использует малую часть функционала
 * {@link AuthenticationMechanism}. Умеет из http-заголовков извлекать авторизационные токены,
 * анализировать их и помещать результат в {@link SecurityContext} и {@link IdentityContext}
 * приложения.
 * <br/><br/>
 * Приоритет использования: Basic-заголовок, кука с сессией, Bearer-заголовок.
 */
public class AuthenticatorAuthenticationMechanism implements AuthenticationMechanism {

    static final String AUTH_MECHANISM_NAME = "Authenticator";

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {

        Optional<Account> account = Pau
                .extractBasic(exchange.getRequestHeaders().get(Headers.AUTHORIZATION))
                .map(Pau.AuthenticationBasic::getPrincipal)
                .map(Pau::account);

        if (!account.isPresent()) {
            account = Optional
                    .ofNullable(exchange.getRequestCookies().get(Pau.AUTH_COOKIE))
                    .map(Cookie::getValue)
                    .filter(value -> !value.isEmpty())
                    .map(Pau::bearer)
                    .map(Pau.AuthenticationBearer::getPrincipal)
                    .map(Pau::account);
        }

        if (!account.isPresent()) {
            account = Pau
                    .extractBearer(exchange.getRequestHeaders().get(Headers.AUTHORIZATION))
                    .map(Pau.AuthenticationBearer::getPrincipal)
                    .map(Pau::account);

        }

        if (!account.isPresent()) {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }

        securityContext.authenticationComplete(account.get(), AUTH_MECHANISM_NAME, false);
        Pau.setIdentity(account.get().getPrincipal());

        return AuthenticationMechanismOutcome.AUTHENTICATED;

    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        return new ChallengeResult(true);
    }
}
