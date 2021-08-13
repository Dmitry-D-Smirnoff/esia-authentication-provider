package ru.voskhod.platform.esiaprovider.esia;

import org.apache.http.client.utils.URIBuilder;
import ru.voskhod.platform.esiaprovider.logic.PlatformSettings;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import static java.text.MessageFormat.format;

/**
 * Бин отвечает за формирование параметризованных URL для обращения к ресурсам ЕСИА по rest-интерфейсу.
 * Шаблоны берутся из системных настроек, параметры, если необходимы, передаются как аргументы в
 * соответствующих методах этого бина.
 */
@Stateless
public class EsiaUriManager {

    private static final String HTTPS_SCHEME = "https";

    @EJB
    private PlatformSettings settings;


    public URIBuilder authCodeBuilder() {
        return buildURLBuilder(settings.esia.authCodePath.value());
    }

    // "/idp/ext/Logout?client_id={0}&redirect_url={1}"
    public URIBuilder logoutBuilder() {
        return buildURLBuilder(settings.esia.logoutPath.value());
    }

    public String tokenUrl() {
        return buildURLBuilder(settings.esia.accessTokenPath.value()).toString();
    }


    // "/rs/prns/{0}"
    public String userInfoUrl(String userId) {
        return buildURLBuilder(format(settings.esia.userDataPath.value(), userId)).toString();
    }

    // "/rs/prns/{0}/roles"
    public String userOrganizationsInfoUrl(String userId) {
        return buildURLBuilder(
                format(settings.esia.userDataPath.value(), userId) +
                        settings.esia.organizationDataSuffix.value()
        ).toString();
    }

    // "/rs/prns/{oid}/docs?embed=(elements)"
    public String userDocumentsInfoUrl(String userId) {
        return buildURLBuilder(
                format(settings.esia.userDataPath.value(), userId) +
                settings.esia.documentDataSuffix.value()
        ).toString();
    }

    // "/rs/orgs/{0}/emps/{1}/grps?embed=(elements)"
    public String userOrganizationGroupsInfoUrl(String organizationId, String userId) {
        return buildURLBuilder(format(settings.esia.userGroupDataPath.value(), organizationId, userId)).toString();
    }

    // "/rs/prns/{oid}/ctts?embed=(elements)"
    public String userContactsInfoUrl(String userId) {
        return buildURLBuilder(format(settings.esia.userContactsPath.value(), userId)).toString();
    }


    private URIBuilder buildURLBuilder(String resourcePath) {
        return new URIBuilder()
                .setScheme(HTTPS_SCHEME)
                .setHost(settings.esia.esiaHost.value())
                .setPath(resourcePath);
    }

}
