package ru.voskhod.platform.esiaprovider.logic;

import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class LoggingSettings extends SettingsManagerBase4Logging {

    public final Setting<String> userAccountIdentification = ofString("UserAccountIdentification", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Идентификация учетной записи пользователя")
    );

    public final Setting<String> userAccountAuthentication = ofString("UserAccountAuthentication", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Аутентификация учетной записи пользователя")
    );

    public final Setting<String> userSessionCreate = ofString("UserSessionCreate", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Создание сессии пользователя")
    );

    public final Setting<String> userSessionRemove = ofString("UserSessionRemove", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Разрыв сессии пользователя")
    );


    public final Setting<String> esiaUserPersonalDataRequest = ofString("EsiaUserPersonalDataRequest", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Отправка запроса в ЕСИА на получение персональных данных пользователя")
    );
    public final Setting<String> esiaUserPersonalDataResponse = ofString("EsiaUserPersonalDataResponse", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Получение ответа на запрос в ЕСИА на получение персональных данных пользователя")
    );

    public final Setting<String> esiaUserContactsRequest = ofString("EsiaUserContactsRequest", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Отправка запроса в ЕСИА на получение контактов пользователя")
    );
    public final Setting<String> esiaUserContactsResponse = ofString("EsiaUserContactsResponse", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Получение ответа на запрос в ЕСИА на получение контактов пользователя")
    );

    public final Setting<String> esiaUserDocumentsRequest = ofString("EsiaUserDocumentsRequest", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Отправка запроса в ЕСИА на получение информации о документах пользователя")
    );
    public final Setting<String> esiaUserDocumentsResponse = ofString("EsiaUserDocumentsResponse", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Получение ответа на запрос в ЕСИА на получение информации о документах пользователя")
    );

    public final Setting<String> esiaUserOrganizationsRequest = ofString("EsiaUserOrganizationsRequest", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Отправка запроса в ЕСИА на получение списка организаций, в которых состоит пользователь")
    );
    public final Setting<String> esiaUserOrganizationsResponse = ofString("EsiaUserOrganizationsResponse", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Получение ответа на запрос в ЕСИА на получение списка организаций, в которых состоит пользователь")
    );

    public final Setting<String> esiaUserOrganizationGroupsRequest = ofString("EsiaUserOrganizationGroupsRequest", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Отправка запроса в ЕСИА на получение списка групп выбранной пользователем организации")
    );
    public final Setting<String> esiaUserOrganizationGroupsResponse = ofString("EsiaUserOrganizationGroupsResponse", setup -> setup
            .required()
            .defaultValue(SecurityLogging.MODE_ALL)
            .description("Получение ответа на запрос в ЕСИА на получение списка групп выбранной пользователем организации")
    );

}
