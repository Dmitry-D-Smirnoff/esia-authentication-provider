package ru.voskhod.platform.esiaprovider.client;

import lombok.NonNull;
import lombok.val;
import ru.voskhod.platform.core.security.client.PlatformRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.EsiaGroupDto;
import ru.voskhod.platform.esiaprovider.client.dto.EsiaGroupDtoCreate;
import ru.voskhod.platform.esiaprovider.client.dto.LoggingEventSettingDto;
import ru.voskhod.platform.esiaprovider.client.dto.SecurityGroupDto;
import ru.voskhod.platform.esiaprovider.client.dto.SystemSettingDto;
import ru.voskhod.platform.esiaprovider.client.dto.UserAccountDtoCreate;
import ru.voskhod.platform.esiaprovider.client.dto.UserAccountDtoRead;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDto;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDtoCreate;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDtoUpdate;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaGroupInfo;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

@ApplicationScoped
public class AccessManagerRestClient {

    private final static String ACCESS_MANAGER_URI = "http://" + PlatformRestClient.ROUTER_HOST_PORT + "/access-manager/api";


    @Inject
    private SessionManager sessionManager;

    private PlatformRestClient client;


    @PostConstruct
    private void init() {
        client = new PlatformRestClient(sessionManager);
    }

    @PreDestroy
    public void closeConnection() {
        client.closeConnection();
    }


    public List<SystemSettingDto> getSystemSettingList() throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%1$s/systemsettings?limit=-1", ACCESS_MANAGER_URI));

        response.assertStatusCode(200);

        return response.getEntityList(SystemSettingDto.class);
    }

    public List<LoggingEventSettingDto> getLoggingSettingList() throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%1$s/loggingeventsettings?limit=-1", ACCESS_MANAGER_URI));

        response.assertStatusCode(200);

        return response.getEntityList(LoggingEventSettingDto.class);
    }


    public UserProfileDto findUserProfileBySnils(@NonNull String snils) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/userprofiles?snils=%s", ACCESS_MANAGER_URI,
                        urlEncode(snils)));

        response.assertStatusCode(200);

        return response.getEntityList(UserProfileDto.class)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public UserProfileDto findUserProfileByEmail(@NonNull String email) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/userprofiles?email=%s", ACCESS_MANAGER_URI,
                        urlEncode(email)));

        response.assertStatusCode(200);

        return response.getEntityList(UserProfileDto.class)
                .stream().findFirst()
                .orElse(null);
    }


    public UserProfileDto createUserProfile(@NonNull UserProfileDtoCreate profile) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.post(
                format("%s/userprofiles", ACCESS_MANAGER_URI), profile);

        response.assertStatusCode(201);

        return response.getEntity(UserProfileDto.class);
    }

    public UserProfileDto updateUserProfile(@NonNull UserProfileDtoUpdate profile, @NonNull UUID profileId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.put(
                format("%s/userprofiles/%s", ACCESS_MANAGER_URI, profileId), profile);

        response.assertStatusCode(200);

        return response.getEntity(UserProfileDto.class);
    }


    public List<UserAccountDtoRead> findUserAccountsInSegment(UUID accountTypeId, UUID segmentId, UUID profileId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/users?user_profile_id=%s&segment_id=%s&type_id=%s", ACCESS_MANAGER_URI,
                        profileId, segmentId, accountTypeId));

        response.assertStatusCode(200);

        return response.getEntityList(UserAccountDtoRead.class);
    }

    public List<UserAccountDtoRead> findUserAccounts(UUID accountTypeId, UUID profileId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/users?user_profile_id=%s&type_id=%s", ACCESS_MANAGER_URI,
                        profileId, accountTypeId));

        response.assertStatusCode(200);

        return response.getEntityList(UserAccountDtoRead.class);
    }

    public UserAccountDtoRead findUserAccountById(@NonNull UUID accountId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/users/%s", ACCESS_MANAGER_URI, accountId));

        response.assertStatusCode(200);

        return response.getEntity(UserAccountDtoRead.class);
    }

    public UserAccountDtoRead createUserAccount(@NonNull UserAccountDtoCreate account) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.post(
                format("%s/users", ACCESS_MANAGER_URI), account);

        response.assertStatusCode(201);

        return response.getEntity(UserAccountDtoRead.class);
    }


    /**
     * Возвращает текущие группы безопасности указанной учётной записи, имеющие связь с какими-либо группами ЕСИА.
     */
    public List<SecurityGroupDto> getSecurityGroupsListByAccountId(@NonNull UUID accountId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/users/%s/securitygroups?has-esia-group=true", ACCESS_MANAGER_URI, accountId));

        response.assertStatusCode(200);

        return response.getEntityList(SecurityGroupDto.class);
    }

    public List<SecurityGroupDto> getSecurityGroupsListByEsiaGroupId(@NonNull UUID esiaGroupId,
                                                                     @NonNull UUID segmentId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/securitygroups?esia_group_id=%s&segment_id=%s&include_multisegment=true",
                        ACCESS_MANAGER_URI, esiaGroupId, segmentId));

        response.assertStatusCode(200);

        return response.getEntityList(SecurityGroupDto.class);
    }

    public void addUserAccountToSecurityGroup(@NonNull UUID userAccountId, @NonNull UUID securityGroupId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.put(
                format("%s/securitygroups/%s/user/%s", ACCESS_MANAGER_URI,
                        securityGroupId, userAccountId), null);

        response.assertStatusCode(200);
    }

    public void removeUserAccountFromSecurityGroup(@NonNull UUID userAccountId, @NonNull UUID securityGroupId) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.delete(
                format("%s/securitygroups/%s/user/%s", ACCESS_MANAGER_URI,
                        securityGroupId, userAccountId));

        response.assertStatusCode(200);
    }

    public EsiaGroupDto findEsiaGroupBySysname(@NonNull String esiaGroupSysname) throws IOException, URISyntaxException {

        PlatformRestClient.Response response = client.get(
                format("%s/esiagroups?sysname=%s", ACCESS_MANAGER_URI,
                        urlEncode(esiaGroupSysname)));

        response.assertStatusCode(200);

        return response.getEntityList(EsiaGroupDto.class)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public EsiaGroupDto createEsiaGroup(@NonNull EsiaGroupInfo esiaGroupInfo) throws IOException, URISyntaxException {

        val dto = new EsiaGroupDtoCreate(esiaGroupInfo.grp_id, esiaGroupInfo.name, esiaGroupInfo.description);

        PlatformRestClient.Response response = client.post(
                format("%s/esiagroups", ACCESS_MANAGER_URI), dto);

        response.assertStatusCode(201);

        return response.getEntity(EsiaGroupDto.class);
    }


    private String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

}
