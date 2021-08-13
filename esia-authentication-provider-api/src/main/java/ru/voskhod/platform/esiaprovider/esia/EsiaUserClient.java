package ru.voskhod.platform.esiaprovider.esia;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import ru.voskhod.platform.common.exception.UnauthenticatedException;
import ru.voskhod.platform.core.utils.Lazy;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaContactInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaContactsInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaDocInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaDocsInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaGroupInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaGroupsInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaOrganizationInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaOrganizationsInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaUserInfo;
import ru.voskhod.platform.esiaprovider.logic.LoggingSettings;
import ru.voskhod.platform.esiaprovider.logic.SecurityLogging;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * Класс инкапсулирует вызовы к ЕСИА для получения данных пользователя. Для этого внутри него используется
 * класс {@link EsiaRestClient}. Экземпляр рассчитан на обработку данных по одному токену, соответственно,
 * он должен создаваться для каждого запроса на создание сессии и закрываться методом {@link #close()}
 * после завершения работы с ним. Метод {@link #close()} транслируется внутреннему экземпляру {@link EsiaRestClient}
 * для закрытия rest-соединения с ЕСИА.
 * <br/><br/>
 * Экземпляр данного класса создаётся при помощи stateless-бина {@link EsiaUserClientFactory}, который по
 * авторизационному коду ЕСИА выполняет получение токена доступа, определение идентификатора пользователя,
 * проверку подписи токена. При успешном завершении перечисленных действий будет создан экземпляр данного
 * класса, готовый к работе.
 */
public class EsiaUserClient implements Closeable {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String TYPE_PASSPORT     = "RF_PASSPORT";
    private static final String TYPE_MOBILE       = "MBT";
    private static final String TYPE_EMAIL        = "EML";


    public final String accessToken;
    public final String userId;
    public final String organizationId;

    private final EsiaUriManager esiaUriManager;
    private final SecurityLogging securityLogging;
    private final LoggingSettings loggingSettings;

    private final EsiaRestClient client;


    /**
     * Возвращает dto, содержащий основную информацию о текущем пользователе.
     */
    public final Lazy<EsiaUserInfo> info;

    /**
     * Список всех документов текущего пользователя. Используется в дальнейшем для
     * извлечения информации о паспорте.
     */
    private final Lazy<List<EsiaDocInfo>> documents;

    /**
     * Возвращает информацию о паспорте текущего пользователя, если она имеется в доступном массиве документов.
     */
    public final Lazy<Optional<EsiaDocInfo>> passport;

    /**
     * Список всех организаций текущего пользователя, в том числе и неактивных. Используется для
     * последующей фильтрации.
     */
    private final Lazy<List<EsiaOrganizationInfo>> organizations;

    /**
     * Возвращает список всех активных организаций текущего пользователя.
     */
    public final Lazy<List<EsiaOrganizationInfo>> activeOrganizations;

    /**
     * Возвращает текущую активную организацию. Если при создании экземпляра не была указана
     * организация, либо если она не является активной, метод вернёт пустой контейнер.
     */
    public final Lazy<Optional<EsiaOrganizationInfo>> currentActiveOrganization;

    /**
     * Возвращает список групп пользователя, для текущей организации, если она является активной.
     * Если организация не активна, либо если идентификатор организации не был указан при создании
     * экземпляра клиента, будет возвращён null.
     */
    public final Lazy<List<EsiaGroupInfo>> currentActiveGroups;

    /**
     * Список контактов текущего пользователя. Используется в дальнейшем для извлечения email
     * и номера мобильного телефона.
     */
    private final Lazy<List<EsiaContactInfo>> contacts;

    /**
     * Возвращает контакт, содержащий мобильный телефон пользователя, если имеется.
     */
    public final Lazy<Optional<EsiaContactInfo>> contactMobile;

    /**
     * Возвращает контакт, содержащий email пользователя, если имеется.
     */
    public final Lazy<Optional<EsiaContactInfo>> contactEmail;


    EsiaUserClient(String accessToken, String userId, String organizationId,
                   EsiaUriManager esiaUriManager, SecurityLogging securityLogging, LoggingSettings loggingSettings) {

        this.accessToken = accessToken;
        this.userId = userId;
        this.organizationId = organizationId;

        this.esiaUriManager  = esiaUriManager;
        this.securityLogging = securityLogging;
        this.loggingSettings = loggingSettings;

        this.client = new EsiaRestClient(accessToken);

        info = Lazy.of(this::getUserData);

        documents = Lazy.of(() -> arrayToList(getUserDocuments().elements));

        passport = Lazy.of(() ->
                documents
                        .get()
                        .stream()
                        .filter(docInfo -> TYPE_PASSPORT.equals(docInfo.type))
                        .findFirst());

        organizations = Lazy.of(() -> arrayToList(getUserOrganizations().elements));

        activeOrganizations = Lazy.of(() ->
                organizations
                        .get()
                        .stream()
                        .filter(info -> info.active != null && info.active)
                        .collect(Collectors.toList()));

        currentActiveOrganization = Lazy.of(() -> {

            if (this.organizationId == null) {
                return Optional.empty();
            }

            return activeOrganizations
                    .get()
                    .stream()
                    .filter(info -> this.organizationId.equals(info.oid))
                    .findFirst();
        });

        currentActiveGroups = Lazy.of(() ->
                currentActiveOrganization
                        .get()
                        .map(activeOrganization -> getUserGroupsForOrganization(activeOrganization.oid))
                        .map(activeGroups -> activeGroups.elements == null ? new EsiaGroupInfo[0] : activeGroups.elements)
                        .map(Arrays::asList)
                        .orElse(null)
        );

        contacts = Lazy.of(() -> arrayToList(getUserContacts().elements));

        contactMobile = Lazy.of(() ->
                contacts.get()
                        .stream()
                        .filter(contact -> TYPE_MOBILE.equals(contact.type))
                        .findFirst()
                );

        contactEmail = Lazy.of(() ->
                contacts.get()
                        .stream()
                        .filter(contact -> TYPE_EMAIL.equals(contact.type))
                        .findFirst()
        );

    }

    private <D> List<D> arrayToList(D[] array) {
        return array == null ? Collections.emptyList() : Arrays.asList(array);
    }


    @SneakyThrows
    private EsiaUserInfo getUserData() {

        securityLogging.instantInfo(loggingSettings.esiaUserPersonalDataRequest);
        val log = securityLogging.newLogger(loggingSettings.esiaUserPersonalDataResponse);

        EsiaRestClient.Response response = client.get(esiaUriManager.userInfoUrl(userId));
        response.assertStatusCode(200);
        log.markSuccess();

        EsiaUserInfo userInfo = response.getEntity(EsiaUserInfo.class);
        log.currentSnils(userInfo.snils);

        if (!STATUS_REGISTERED.equals(userInfo.status)) {
            throw new UnauthenticatedException(format(
                    "Статус пользователя ЕСИА ({0}) не допускает аутентификацию",
                    userInfo.status));
        }

        return userInfo;
    }

    @SneakyThrows
    private EsiaOrganizationsInfo getUserOrganizations() {

        securityLogging.instantInfo(loggingSettings.esiaUserOrganizationsRequest);
        val log = securityLogging.newLogger(loggingSettings.esiaUserOrganizationsResponse);
        log.currentSnils(info.get().snils);

        EsiaRestClient.Response response = client.get(esiaUriManager.userOrganizationsInfoUrl(userId));
        response.assertStatusCode(200);
        log.markSuccess();

        EsiaOrganizationsInfo organizationsInfo = response.getEntity(EsiaOrganizationsInfo.class);
        String ogrnStr = arrayToList(organizationsInfo.elements)
                .stream()
                .map(info -> info.ogrn)
                .collect(Collectors.joining(", "));

        if (StringUtils.isNotBlank(ogrnStr)) {
            log.message("ОГРН организаций пользователя: " + ogrnStr);
        }

        return organizationsInfo;
    }

    @SneakyThrows
    private EsiaGroupsInfo getUserGroupsForOrganization(String organizationId) {

        securityLogging.instantInfo(loggingSettings.esiaUserOrganizationGroupsRequest);
        val log = securityLogging.newLogger(loggingSettings.esiaUserOrganizationGroupsResponse);
        log.currentSnils(info.get().snils);
        log.currentOgrn(organizations
                .get()
                .stream()
                .filter(info -> organizationId.equals(info.oid))
                .findFirst()
                .map(info -> info.ogrn)
                .orElse(null));

        EsiaRestClient.Response response = client.get(esiaUriManager.userOrganizationGroupsInfoUrl(organizationId, userId));
        response.assertStatusCode(200);
        log.markSuccess();

        EsiaGroupsInfo groupsInfo = response.getEntity(EsiaGroupsInfo.class);
        String grpIdStr = arrayToList(groupsInfo.elements)
                .stream()
                .map(info -> info.grp_id)
                .collect(Collectors.joining(", "));

        if (StringUtils.isNotBlank(grpIdStr)) {
            log.message("Мнемоники групп пользователя: " + grpIdStr);
        }

        return groupsInfo;
    }

    @SneakyThrows
    private EsiaDocsInfo getUserDocuments() {

        securityLogging.instantInfo(loggingSettings.esiaUserDocumentsRequest);
        val log = securityLogging.newLogger(loggingSettings.esiaUserDocumentsResponse);
        log.currentSnils(info.get().snils);

        EsiaRestClient.Response response = client.get(esiaUriManager.userDocumentsInfoUrl(userId));
        response.assertStatusCode(200);
        log.markSuccess();

        return response.getEntity(EsiaDocsInfo.class);
    }

    @SneakyThrows
    private EsiaContactsInfo getUserContacts() {

        securityLogging.instantInfo(loggingSettings.esiaUserContactsRequest);
        val log = securityLogging.newLogger(loggingSettings.esiaUserContactsResponse);
        log.currentSnils(info.get().snils);

        EsiaRestClient.Response response = client.get(esiaUriManager.userContactsInfoUrl(userId));
        response.assertStatusCode(200);
        log.markSuccess();

        return response.getEntity(EsiaContactsInfo.class);
    }


    public void close() throws IOException {
        client.close();
    }

}
