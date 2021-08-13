package ru.voskhod.platform.esiaprovider;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import ru.voskhod.platform.common.exception.DataConflictException;
import ru.voskhod.platform.common.exception.EntityValidationException;
import ru.voskhod.platform.common.exception.IncorrectTokenException;
import ru.voskhod.platform.common.exception.NothingFoundException;
import ru.voskhod.platform.common.exception.RuntimeExceptionMapper;
import ru.voskhod.platform.common.exception.UnauthenticatedException;
import ru.voskhod.platform.common.exception.UnauthorizedException;
import ru.voskhod.platform.common.settings.MissingRequiredSettingException;
import ru.voskhod.platform.core.security.client.StatusAssertionFailed;
import ru.voskhod.platform.esiaprovider.logic.CookieUtils;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper4Provider extends RuntimeExceptionMapper {

    @Inject
    private CookieUtils cookieUtils;

    public RuntimeExceptionMapper4Provider() {
        super(true);
    }

    @Override
    protected void configure(ExceptionRegistry exceptionRegistry) {
        exceptionRegistry.register(IncorrectTokenException.class)
                .status(Response.Status.BAD_REQUEST)
                .messageFunction((exception, root) -> exception.getMessage());
        exceptionRegistry.register(IllegalArgumentException.class)
                .status(Response.Status.BAD_REQUEST)
                .message("Неверные параметры запроса");
        exceptionRegistry.register(EntityValidationException.class)
                .status(Response.Status.BAD_REQUEST)
                .messageFunction((exception, root) -> exception.getMessage())
                .fieldsFunction((exception, root) -> exception.getFields());
        exceptionRegistry.register(NothingFoundException.class)
                .statusFunction((exception, root) -> exception.isResource()
                        ? Response.Status.NOT_FOUND
                        : Response.Status.BAD_REQUEST)
                .messageFunction((exception, root) -> exception.getMessage())
                .fieldsFunction((exception, root) -> exception.getFields());
        exceptionRegistry.register(InternalServiceException.class)
                .status(Response.Status.CONFLICT)
                .message("Ошибка на сервере. Обратитесь к администратору")
                .hideException();
        exceptionRegistry.register(IllegalStateException.class)
                .status(Response.Status.CONFLICT)
                .message("Текущее состояние не позволяет выполнить действие");
        exceptionRegistry.register(OptimisticLockException.class)
                .status(Response.Status.CONFLICT)
                .message("Элемент изменён другим пользователем");
        exceptionRegistry.register(DataConflictException.class)
                .status(Response.Status.CONFLICT)
                .messageFunction((exception, root) -> exception.getMessage())
                .fieldsFunction((exception, root) -> exception.getFields());
        exceptionRegistry.register(MissingRequiredSettingException.class)
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .message("Ошибка на сервере. Обратитесь к администратору")
                .hideException();
        exceptionRegistry.register(ForbiddenException.class)
                .status(Response.Status.FORBIDDEN)
                .message("Недостаточно прав доступа");
        exceptionRegistry.register(UnauthorizedException.class)
                .status(Response.Status.FORBIDDEN)
                .message("Недостаточно прав доступа");
        exceptionRegistry.register(UnauthenticatedException.class)
                .status(Response.Status.UNAUTHORIZED)
                .message("Пользователь не аутентифицирован")
                .cookiesFunction(this::getCleanCookie)
                .hideException();
        exceptionRegistry.register(MismatchedInputException.class)
                .status(Response.Status.BAD_REQUEST)
                .message("Тело запроса не соответствует спецификации API");
        exceptionRegistry.register(InvalidFormatException.class)
                .status(Response.Status.BAD_REQUEST)
                .message("Данные переданы в неверном формате");
        exceptionRegistry.register(StatusAssertionFailed.class)
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .message("Ошибка на сервере. Обратитесь к администратору")
                .hideException();
    }

    private <E extends RuntimeException> NewCookie[] getCleanCookie(E exception, Throwable root) {

        return new NewCookie[]{
                cookieUtils.create(null)
        };
    }
}
