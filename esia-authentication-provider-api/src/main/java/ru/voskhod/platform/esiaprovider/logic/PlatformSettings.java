package ru.voskhod.platform.esiaprovider.logic;

import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class PlatformSettings extends SettingsManagerBase4Platform {

    public final AccessManagerSubsystem am = new AccessManagerSubsystem();
    public class AccessManagerSubsystem {

        public Setting<String> defaultUserAccountStatus = ofString("AccessManager.DefaultUserAccountStatus",
                setup -> setup
                        .defaultValue("REGISTERED")
                        .description("Статус учётной записи, назначаемый автоматически при создании")
        );

        public Setting<String> moduleName = ofString("AccessManager.ModuleName",
                setup -> setup
                        .required()
                        .defaultValue("Модуль защиты прикладного ПО")
                        .description("Наименование модуля")
        );

        public Setting<String> cookieDomain = ofString("AccessManager.CookieDomain",
                setup -> setup
                        .description("Домен для сессионной http-only cookie")
        );

        public Setting<Boolean> cookieSecure = ofBoolean("AccessManager.CookieSecure",
                setup -> setup
                        .required()
                        .defaultValue(false)
                        .description("Флаг безопасности для сессионной http-only cookie")
        );

        public Setting<Integer> httpClientConnectTimeout = ofInteger("AccessManager.HttpClientConnectTimeout",
                setup -> setup
                        .defaultValue(10)
                        .description("Таймаут подключения к серверу для HTTP-клиента в сервисах МЗИ")
        );
    }

    public final EsiaSubsystem esia = new EsiaSubsystem();
    public class EsiaSubsystem {

        public final Setting<String> esiaHost = ofString("Esia.Host", setup -> setup
                .required()
                .description("Адрес сервиса ЕСИА")
        );

        public final Setting<String> authCodePath = ofString("Esia.AuthCodePath", setup -> setup
                .required()
                .description("Адрес для получения авторизационного кода")
        );

        public final Setting<String> accessTokenPath = ofString("Esia.AccessTokenPath", setup -> setup
                .required()
                .description("Адрес для получения маркера идентификации")
        );

        public final Setting<String> userDataPath = ofString("Esia.UserDataPath", setup -> setup
                .required()
                .description("Адрес для получения данных о пользователе")
        );

        public final Setting<String> userGroupDataPath = ofString("Esia.UserGroupDataPath", setup -> setup
                .required()
                .description("Адрес (шаблон) для получения данных о группах организации, к которым привязан пользователь")
        );

        public final Setting<String> userContactsPath = ofString("Esia.UserContactsPath", setup -> setup
                .required()
                .description("Адрес (шаблон) для получения контактов пользователя")
        );

        public final Setting<String> logoutPath = ofString("Esia.LogoutPath", setup -> setup
                .required()
                .description("Адрес для выхода пользователя из системы ЕСИА")
        );

        public final Setting<String> clientId = ofString("Esia.ClientId", setup -> setup
                .required()
                .description("Мнемоника системы в ЕСИА")
        );

        public final Setting<String> keystorePassword = ofString("Esia.KeystorePassword", setup -> setup
                .required()
                .description("Пароль к хранилищу сертификатов")
        );

        public final Setting<String> certificateAlias = ofString("Esia.CertificateAlias", setup -> setup
                .required()
                .description("Алиас сертификата")
        );

        public final Setting<String> userDataScope = ofString("Esia.UserDataScope", setup -> setup
                .required()
                .description("Запрашиваемые права доступа к данным пользователя")
        );

        public final Setting<String> loginPage = ofString("Esia.LoginPage", setup -> setup
                .required()
                .description("Страница фронтенда для получения профиля пользователя")
        );

        public final Setting<String> keystoreFile = ofString("Esia.KeystoreFile", setup -> setup
                .required()
                .description("Контейнер (хранилище) сертификата закрытого ключа")
        );

        public final Setting<String> redirectUri = ofString("Esia.RedirectUri", setup -> setup
                .required()
                .description("Адрес для перенаправления пользователя после подтверждения разрешение на доступ к ресурсу")
        );

        public final Setting<String> logoutRedirectUri = ofString("Esia.LogoutRedirectUri", setup -> setup
                .required()
                .description("Адрес для перенаправления пользователя после завершения сессии в ЕСИА")
        );

        public final Setting<String> organizationDataSuffix = ofString("Esia.OrganizationDataSuffix", setup -> setup
                .required()
                .description("Окончание адреса для получения данных о перечне организаций, сотрудником которых является пользователь")
        );

        public final Setting<String> documentDataSuffix = ofString("Esia.DocumentDataSuffix", setup -> setup
                .required()
                .description("Окончание адреса для получения данных о перечне документов пользователя")
        );

        public final Setting<String> organizationDataScope = ofString("Esia.OrganizationDataScope", setup -> setup
                .required()
                .description("Запрашиваемые права доступа к данным организации")
        );

        public final Setting<String> jwtPublicKeyBase64 = ofString("Esia.JwtPublicKeyBase64", setup -> setup
                .required()
                .description("Публичный ключ ЕСИА для проверки токенов JWT")
        );

        public final Setting<String> jwtIssuer = ofString("Esia.JwtIssuer", setup -> setup
                .required()
                .description("Issuer для проверки токенов JWT ЕСИА")
        );

        public final Setting<String> jwtType = ofString("Esia.JwtType", setup -> setup
                .defaultValue("JWT")
                .description("Тип токена ЕСИА")
        );

        public final Setting<String> jwtAlgorithm = ofString("Esia.JwtAlgorithm", setup -> setup
                .defaultValue("RS256")
                .description("Алгоритм подписи токена ЕСИА")
        );

        public final Setting<String> jwtClaimClientIdKey = ofString("Esia.JwtClaimClientIdKey", setup -> setup
                .defaultValue("client_id")
                .description("Имя поля в токене, по которому задается мнемоника системы")
        );

        public final Setting<String> jwtClaimUserIdKey = ofString("Esia.JwtClaimUserIdKey", setup -> setup
                .defaultValue("urn:esia:sbj_id")
                .description("Имя поля в токене, по которому задается идентификатор пользователя")
        );

        public final Setting<String> jwtClaimClientScopeKey = ofString("Esia.JwtClaimClientScopeKey", setup -> setup
                .defaultValue("scope")
                .description("Имя поля в токене, по которому задается scope доступа")
        );

        public final Setting<Boolean> allowPersonAuth = ofBoolean("Esia.AllowPersonAuth", setup -> setup
                .defaultValue(false)
                .description("Разрешать ли аутентификацию через ЕСИА физических лиц, не имеющих привязку к организации")
        );

        public final Setting<Boolean> allowRegistration = ofBoolean("Esia.AllowRegistration", setup -> setup
                .defaultValue(false)
                .description("Разрешать ли автоматическую регистрацию пользователей в системе")
        );

        public final Setting<Boolean> allowGroupSynchronization = ofBoolean(
                "Esia.AllowGroupSynchronization", setup -> setup
                        .defaultValue(false)
                        .description("Разрешать ли синхронизацию групп ЕСИА с группами безопасности платформы")
        );

        public final Setting<String> requestSignatureAlgorithm = ofString("Esia.RequestSignatureAlgorithm", setup -> setup
                .required()
                .description("Тип алгоритма, используемого для подписи запросов на получение авторизационного кода и токена доступа ЕСИА")
        );

        public final Setting<String> gostCryptoServiceUrl = ofString("Esia.GostCryptoServiceUrl", setup -> setup
                .required()
                .description("URL ресурса криптосервиса, обеспечивающего подпись по алгоритму ГОСТ")
        );

        public final Setting<String> gostCryptoServiceVerifyUrl = ofString("Esia.GostCryptoServiceVerifyUrl", setup -> setup
                .required()
                .description("URL ресурса криптосервиса, обеспечивающего проверку подписи токенов по алгоритму ГОСТ")
        );

        public final Setting<String> gostJwtPublicKeyCertBase64 = ofString("Esia.GostJwtPublicKeyCertBase64", setup -> setup
                .required()
                .description("Сертификат открытого ключа ЕСИА")
        );
    }

    public Setting<String> systemName = ofString("System.SystemName",
            setup -> setup
                    .required()
                    .defaultValue("Платформа ЦТ")
                    .description("Наименование системы")
    );
}
