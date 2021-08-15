package ru.voskhod.platform.esiaprovider.logic;

import lombok.val;
import org.modelmapper.ModelMapper;
import ru.voskhod.platform.common.exception.UnauthenticatedException;
import ru.voskhod.platform.core.security.dto.SessionTokenDto;
import ru.voskhod.platform.core.security.principal.SessionTokenPrincipal;
import ru.voskhod.platform.esiaprovider.Authenticator;
import ru.voskhod.platform.esiaprovider.InternalServiceException;
import ru.voskhod.platform.esiaprovider.api.dto.AuthCodeDto;
import ru.voskhod.platform.esiaprovider.api.dto.AuthCodeExtendedDto;
import ru.voskhod.platform.esiaprovider.api.dto.OrgDto;
import ru.voskhod.platform.esiaprovider.api.dto.SessionResponseDto;
import ru.voskhod.platform.esiaprovider.client.AccessManagerRestClient;
import ru.voskhod.platform.esiaprovider.client.SessionManagerRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.SessionResponseDtoIn;
import ru.voskhod.platform.esiaprovider.client.dto.UserAccountDtoRead;
import ru.voskhod.platform.esiaprovider.esia.EsiaUserClient;
import ru.voskhod.platform.esiaprovider.esia.EsiaUserClientFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Stateless
public class ProviderAuthenticationLogic {

    private static final Supplier<IllegalArgumentException> EXCEPTION_NoReferer = () ->
            new IllegalArgumentException("Заголовок 'Referer' должен присутствовать в запросе");

    @EJB
    private EsiaUserClientFactory esiaUserClientFactory;

    @EJB
    public ProviderDataLogic providerDataLogic;

    @EJB
    private LoggingSettings loggingSettings;

    @EJB
    private PlatformSettings settings;

    @Inject
    private ModelMapper modelMapper;

    @Inject
    private SessionManagerRestClient sessionManagerRestClient;

    @Inject
    private SecurityLogging securityLogging;

    @Inject
    private AccessManagerRestClient accessManagerRestClient;


    private static String buildRedirectUri(URI redirectUri, URI referer) {

        if (!redirectUri.isAbsolute()) {

            if (referer == null || !referer.isAbsolute()) {
                throw EXCEPTION_NoReferer.get();
            }

            try {

                String path = redirectUri.getPath();
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }

                return new URI(
                        referer.getScheme(),
                        null,
                        referer.getHost(),
                        referer.getPort(),
                        path,
                        redirectUri.getQuery(),
                        redirectUri.getFragment()
                ).toString();

            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }

        }

        return redirectUri.toString();

    }

    /**
     * Метод запрашивает в ЕСИА информацию по организациям, к которым имеет отношение
     * аутентифицируемый пользователь. Если пользователь не связан ни с одной организацией,
     * метод возвращает пустой массив.
     */
    public OrgDto[] getOrganizationsByAuthCode(AuthCodeDto dto) {

        try (EsiaUserClient client = esiaUserClientFactory.createClient(dto.getCode(), null)) {

            return client.activeOrganizations
                    .get()
                    .stream()
                    .map(info -> modelMapper.map(info, OrgDto.class))
                    .toArray(OrgDto[]::new);

        } catch (IOException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException | SignatureException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Метод запрашивает в ЦАП информацию по сегментам, к которым относятся учетные записи
     * аутентифицируемого пользователя. Если пользователь не связан ни с одной организацией,
     * метод возвращает пустой массив.
     */
    public OrgDto[] getSegmentOrganizationsByAuthCode(AuthCodeDto dto) {

        try (EsiaUserClient client = esiaUserClientFactory.createClient(dto.getCode(), null)) {

            return providerDataLogic.findUserSegmentOrganizations(client);

        } catch (IOException
                | UnrecoverableKeyException
                | CertificateException
                | NoSuchAlgorithmException
                | KeyStoreException
                | SignatureException
                | InvalidKeySpecException
                | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public URI getAccessCodeUrl(String esiaOrganizationId, URI referer) {

        try {

            return esiaUserClientFactory.getAccessCodeUrl(
                    esiaOrganizationId,
                    buildRedirectUri(new URI(settings.esia.redirectUri.value()), referer)
            );

        } catch (URISyntaxException
                | CertificateException
                | UnrecoverableKeyException
                | NoSuchAlgorithmException
                | KeyStoreException
                | IOException
                | SignatureException
                | InvalidKeySpecException e) {

            throw new InternalServiceException(e);
        }
    }

    /**
     * Метод выполняет аутентификацию пользователя в платформе на основании переданного
     * аутентификационного кода ЕСИА, ОГРН (опционально) и идентификатора сегмента
     * (опционально).<br/><br/>
     * <p>
     * Если при аутентификации задан ОГРН, то будет выполнена проверка, что в ЕСИА для
     * данного пользователя есть связь с соответствующей организацией, и эта организация
     * <i>активна</i>, в противном случае в аутентификации будет отказано.<br/><br/>
     * <p>
     * Если при аутентификации ОГРН не задан, аутентификация продолжится только в том
     * случае, если в системе задана настройка, разрешающая аутентификацию без привязки
     * к организации.<br/><br/>
     * <p>
     * В процессе аутентификации пользователь может быть автоматически зарегистрирован
     * (если не был ранее), для него будет создан профиль и учётная запись. При этом
     * автоматическая регистрация пользователя, для которого сегмент не указан, произойдёт
     * только в том случае, если она разрешена соответствующей системной настройкой
     * (<i>AllowSegmentlessUserRegistration</i>).<br/><br/>
     * <p>
     * Если при аутентификации задан ОГРН, то в каждом случае успешной аутентификации
     * выполняется актуализация справочника групп ЕСИА в платформе и синхронизация их
     * с группами безопасности платформы.
     */
    public SessionResponseDto authenticateByAuthCode(AuthCodeExtendedDto body) throws SessionNotCreated {

        val logSessionCreate  = securityLogging.newLogger(loggingSettings.userSessionCreate);
        val logAuthentication = securityLogging.newLogger(loggingSettings.userAccountAuthentication);

        OrgDto orgDto = body.getOrganization();

        try (EsiaUserClient client = esiaUserClientFactory.createClient(
                body.getCode(),
                orgDto == null ? null : orgDto.getEsiaOrgId())) {

            logAuthentication.markSuccess();

            // region Идентификация
            val logIdentification = securityLogging.newLogger(loggingSettings.userAccountIdentification);

            if (body.getSegmentId() == null) {
                throw new UnauthenticatedException("Не указан идентификатор сегмента");
            }

            // ПРОВЕРКА ИСКЛЮЧЕНА - организации мы получаем из ЦАП
            // Проверка на запрет входа без привязки к организации
            /*
            if (orgDto == null && !settings.esia.allowPersonAuth.value()) {
                throw new UnauthenticatedException(format(
                        "Для пользователя ЕСИА ''{0}'' не задана организация", client.userId
                ));
            }
            */

            // ПРОВЕРКА ИСКЛЮЧЕНА - организации мы получаем из ЦАП
            // Проверка на наличие и активность соответствующей организации в ЕСИА
            /*
            if (orgDto != null && !client.currentActiveOrganization.get().isPresent()) {
                throw new UnauthenticatedException(format(
                        "Не найдена активная организация пользователя по oid ''{0}''",
                        client.organizationId));
            }
            */

            // Нахождение существующей, либо регистрация новой учётной записи
            UserAccountDtoRead account = providerDataLogic.identify0signUp(body.getSegmentId(), client);

            logIdentification.markSuccess();
            logIdentification.currentUserId(account.getId());

            logAuthentication.currentUserId(account.getId());
            logSessionCreate.currentUserId(account.getId());
            // endregion

            // Обновление групп
            if (settings.esia.allowGroupSynchronization.value() && (account.isActive() || account.isRegistered())) {
                providerDataLogic.processGroups(client.currentActiveGroups.get(), account);
            }

            SessionResponseDtoIn dtoIn = sessionManagerRestClient.provideUserSession(account.getId());

            if (dtoIn.getReasonCode() == 401) {
                throw new UnauthenticatedException("Пользователь не аутентифицирован");
            }

            SessionResponseDto dtoOut = new SessionResponseDto();
            dtoOut.setCurrentUserId(dtoIn.getCurrentUserId());
            dtoOut.setSessionToken(dtoIn.getSessionToken());
            dtoOut.setUserAccountStatus(dtoIn.getUserAccountStatus());
            dtoOut.setReason(dtoIn.getReason());

            switch (dtoIn.getReasonCode()) {
                case 403:
                    throw new SessionNotCreated(Response.Status.FORBIDDEN, dtoOut);
                case 429:
                    throw new SessionNotCreated(Response.Status.TOO_MANY_REQUESTS, dtoOut);
                case 200:
                    dtoOut.setEsiaToken(client.accessToken);
                    return dtoOut;
                default:
                    throw new RuntimeException(MessageFormat.format(
                            "Неизвестный код возврата {0}",
                            dtoIn.getReasonCode()));
            }

        } catch (IOException
                | UnrecoverableKeyException
                | CertificateException
                | NoSuchAlgorithmException
                | KeyStoreException
                | SignatureException
                | InvalidKeySpecException
                | URISyntaxException e) {

            logSessionCreate.markFail();
            logSessionCreate.message(e.getMessage());
            throw new RuntimeException(e);

        } catch (Throwable e) {
            logSessionCreate.markFail();
            logSessionCreate.message(e.getMessage());
            throw e;
        }
    }


    public URI logout(SecurityContext securityContext, URI referer) {

        SecurityLogging.SecurityLogger logSessionRemove = securityLogging
                .newLogger(loggingSettings.userSessionRemove);

        try {

            // region Идентификация
            SecurityLogging.SecurityLogger logIdentification = securityLogging
                    .newLogger(loggingSettings.userAccountIdentification);

            SessionTokenPrincipal principal = (SessionTokenPrincipal) Optional
                    .ofNullable(securityContext.getUserPrincipal())
                    .filter(p -> p instanceof SessionTokenPrincipal)
                    .orElseThrow(Authenticator.exceptionPrincipalNotFound);

            SessionTokenDto dto = Optional
                    .ofNullable(principal.getSessionTokenDto())
                    .filter(d -> d.getCurrentUserId() != null)
                    .filter(d -> d.getSessionId() != null)
                    .orElseThrow(Authenticator.exceptionTokenNotFound);

            UserAccountDtoRead account = Optional
                    .ofNullable(accessManagerRestClient.findUserAccountById(dto.getCurrentUserId()))
                    .orElseThrow(() -> Authenticator.exceptionAccountNotFound.apply(dto.getCurrentUserId()));

            logIdentification.markSuccess();
            logIdentification.currentUserId(account.getId());

            logSessionRemove.currentUserId(account.getId());
            // endregion

            if (!account.isActive()) {
                throw Authenticator.exceptionAccountIsNotServed.get();
            }

            sessionManagerRestClient.deleteUserSession(dto.getSessionId());

            return esiaUserClientFactory.getLogoutUri(buildRedirectUri(new URI(settings.esia.logoutRedirectUri.value()), referer));
        } catch (Throwable e) {
            logSessionRemove.message(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public URI esiaLogoutRedirect(URI referer) {
        try {
            return esiaUserClientFactory.getLogoutUri(
                    buildRedirectUri(new URI(settings.esia.logoutRedirectUri.value()), referer)
            );

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
