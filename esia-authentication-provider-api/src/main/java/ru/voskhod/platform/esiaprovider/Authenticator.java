package ru.voskhod.platform.esiaprovider;

import ru.voskhod.platform.common.exception.UnauthenticatedException;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;

@ApplicationScoped
public class Authenticator {

    public static final Supplier<UnauthenticatedException> exceptionAccountIsNotServed = () ->
            new UnauthenticatedException("Обслуживание учётной записи запрещено");

    public static final Supplier<UnauthenticatedException> exceptionPrincipalNotFound = () ->
            new UnauthenticatedException("В контексте безопасности отсутствует информация о пользователе");

    public static final Supplier<UnauthenticatedException> exceptionTokenNotFound = () ->
            new UnauthenticatedException("Не обнаружен токен безопасности, либо отсутствует идентификатор сеанса");

    public static final Function<UUID, UnauthenticatedException> exceptionAccountNotFound = id ->
            new UnauthenticatedException(format("Учётная запись по идентификатору ''{0}'' не найдена", id));

}
