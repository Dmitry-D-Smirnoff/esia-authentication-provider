package ru.voskhod.platform.esiaprovider.logic;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import ru.voskhod.platform.common.exception.UnauthenticatedException;
import ru.voskhod.platform.core.utils.CollectionUpdater;
import ru.voskhod.platform.core.utils.LazyCheck;
import ru.voskhod.platform.core.utils.Reliable;
import ru.voskhod.platform.esiaprovider.api.dto.OrgDto;
import ru.voskhod.platform.esiaprovider.client.AccessManagerRestClient;
import ru.voskhod.platform.esiaprovider.client.SegmentRegistryRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.EsiaGroupDto;
import ru.voskhod.platform.esiaprovider.client.dto.SecurityGroupDto;
import ru.voskhod.platform.esiaprovider.client.dto.SegmentDto;
import ru.voskhod.platform.esiaprovider.client.dto.SupplementaryAttributeDto;
import ru.voskhod.platform.esiaprovider.client.dto.UserAccountDtoCreate;
import ru.voskhod.platform.esiaprovider.client.dto.UserAccountDtoRead;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDto;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDtoBaseUpdatable;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDtoCreate;
import ru.voskhod.platform.esiaprovider.client.dto.UserProfileDtoUpdate;
import ru.voskhod.platform.esiaprovider.esia.EsiaUserClient;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaDocInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaGroupInfo;
import ru.voskhod.platform.esiaprovider.esia.dto.EsiaUserInfo;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class ProviderDataLogic {

    private static final String ESIA_DATE_FORMAT = "dd.MM.yyyy";
    private static final String ATTRIBUTE_TRUSTED = "ESIA_IS_SUBJECT_TRUSTED";

    private static final Supplier<UnauthenticatedException> EXCEPTION_AutoRegistration = () ->
            new UnauthenticatedException("Автоматическая регистрация не разрешена");

    private static final Supplier<UnauthenticatedException> EXCEPTION_NoSnilsNorEmail = () ->
            new UnauthenticatedException("Для идентификации необходимо хотя бы одно из значений: СНИЛС, EMail");

    @EJB
    private PlatformSettings settings;

    @Inject
    private SegmentRegistryRestClient segmentRegistryRestClient;

    @Inject
    private AccessManagerRestClient accessManagerRestClient;


    /**
     * Выполняет идентификацию (и, при необходимости, регистрацию нового) пользователя на
     * основании аутентификации ЕСИА.
     * <br/><br/>
     * Автоматическая регистрация нового пользователя возможна, если задан {@code segmentId},
     * либо включена настройка, разрешающая регистрацию пользователей без привязки к сегменту.
     * Системная настройка Esia.AllowRegistration позволяет включить/отключить возможность
     * автоматической регистрации пользователей ЕСИА.
     * <br/><br/>
     * Если при аутентификации задан ОГРН, то в каждом случае успешной идентификации
     * выполняется актуализация справочника групп ЕСИА в платформе и синхронизация их
     * с группами безопасности платформы.
     * <br/><br/>
     * Если у найденной (либо у новой созданной) учётной записи статус равен "ACTIVE",
     * то в качестве результата аутентификации создаётся сессия пользователя в платформе.
     */
    public UserAccountDtoRead identify0signUp(UUID segmentId, EsiaUserClient client) throws IOException, URISyntaxException {

        val allowRegistrationCheck = new LazyCheck(() -> settings.esia.allowRegistration.value())
                .onFail(() -> {
                    throw EXCEPTION_AutoRegistration.get();
                });


        UserProfileDto profile = findProfile(
                client.info.get().snils,
                client.contactEmail.get().map(contact -> contact.value).orElse(null));
        UserAccountDtoRead account = null;

        if (profile == null) {
            allowRegistrationCheck.make();
            profile = createUserProfile(client);

        } else {
            profile = updateUserProfile(profile, client);

            List<UserAccountDtoRead> accounts = accessManagerRestClient
                    .findUserAccountsInSegment(UserAccountDtoRead.TYPE_COMMON, segmentId, profile.getId());

            // Если получен пустой список УЗ, доступна автоматическая регистрация (если включена настройка).
            // Если получен список состоящий только из удалённых УЗ, отказываем в аутентификации.
            // Если в списке есть ровно одна неудалённая УЗ, работаем с ней.
            // Если более одной неудалённой УЗ, отказываем в аутентификации.
            if (accounts.size() > 0) {
                accounts = accounts
                        .stream()
                        .filter(dto -> !dto.isDeleted())
                        .collect(Collectors.toList());

                if (accounts.size() == 0) {
                    throw new UnauthenticatedException("Учётная запись удалена");
                } else if (accounts.size() > 1) {
                    throw new UnauthenticatedException("Обнаружено более одной учётной записи");
                }

                account = accounts.get(0);
            }
        }

        if (account == null) {
            allowRegistrationCheck.make();

            val segment = segmentRegistryRestClient.findSegmentById(segmentId);
            if (segment == null) {
                throw new UnauthenticatedException(MessageFormat.format(
                        "Сегмент по идентификатору ''{0}'' не найден", segmentId));
            }

            val newAccount = new UserAccountDtoCreate();
            newAccount.setAccountTypeId(UserAccountDtoRead.TYPE_COMMON);
            newAccount.setSegmentId(segment.getId());
            newAccount.setUserProfileId(profile.getId());
            newAccount.setStatus(settings.am.defaultUserAccountStatus.value());
            account = accessManagerRestClient.createUserAccount(newAccount);
        }

        return account;
    }

    private UserProfileDto findProfile(String snils, String email) throws IOException, URISyntaxException {

        snils = StringUtils.stripToNull(snils);
        email = StringUtils.stripToNull(email);

        if (snils == null && email == null) {
            throw EXCEPTION_NoSnilsNorEmail.get();
        }

        UserProfileDto profile = null;

        if (snils != null) {
            profile = accessManagerRestClient.findUserProfileBySnils(snils);
        }

        if (profile == null && email != null) {
            profile = accessManagerRestClient.findUserProfileByEmail(email);
        }

        return profile;
    }

    public OrgDto[] findUserSegmentOrganizations(EsiaUserClient client) throws IOException, URISyntaxException {

        UserProfileDto profile = findProfile(
                client.info.get().snils,
                client.contactEmail.get().map(contact -> contact.value).orElse(null));
        List<OrgDto> organizations = new ArrayList<>();

        if (profile == null) {
            throw new UnauthenticatedException("Профиль пользователя не найден в МЗИ ТОР КНД по СНИЛС/email");
        } else {
            List<UserAccountDtoRead> accounts = accessManagerRestClient
                    .findUserAccounts(UserAccountDtoRead.TYPE_COMMON, profile.getId());

            // Если получен список, состоящий только из удалённых УЗ, отказываем в аутентификации.
            if (accounts.size() > 0) {
                accounts = accounts
                        .stream()
                        .filter(dto -> !dto.isDeleted())
                        .collect(Collectors.toList());

                if (accounts.size() == 0) {
                    throw new UnauthenticatedException("Не найдены активные учетные записи в МЗИ ТОР КНД");
                }

                for(UserAccountDtoRead account : accounts){
                    SegmentDto segment = segmentRegistryRestClient.findSegmentById(account.getSegmentId());
                    if(segment==null || segment.getId() == null) continue;
                    OrgDto org = new OrgDto();
                    org.setEsiaOrgId(segment.getId().toString());
                    org.setOGRN(segment.getSysname());
                    org.setFullName(segment.getDescription());
                    org.setShortName(segment.getName());
                    org.setBranchName("Нет данных");
                    org.setType("Сведения о Сегменте из МЗИ ТОР КНД");
                    organizations.add(org);
                }

            } else {
                throw new UnauthenticatedException("Не найдены активные учетные записи в МЗИ ТОР КНД");
            }

        }

        return organizations.toArray(new OrgDto[0]);
    }

    private UserProfileDto updateUserProfile(UserProfileDto profile, EsiaUserClient client) throws IOException, URISyntaxException {
        val profileUpdate = new UserProfileDtoUpdate();
        updateUserProfileData(profileUpdate, profile.getUserProfileAttributes(), client);
        return accessManagerRestClient.updateUserProfile(profileUpdate, profile.getId());
    }

    private UserProfileDto createUserProfile(EsiaUserClient client) throws IOException, URISyntaxException {
        val profileCreate = new UserProfileDtoCreate();
        updateUserProfileData(profileCreate, new SupplementaryAttributeDto[0], client);
        profileCreate.setEsiaSbjId(new Integer(client.userId));
        return accessManagerRestClient.createUserProfile(profileCreate);
    }

    private void updateUserProfileData(UserProfileDtoBaseUpdatable profile,
                                       SupplementaryAttributeDto[] attributes,
                                       EsiaUserClient client) {

        EsiaUserInfo data = client.info.get();
        profile.setSnils(StringUtils.stripToNull(data.snils));
        profile.setBirthDate(parseLocalDate(data.birthDate));
        profile.setBirthPlace(StringUtils.stripToNull(data.birthPlace));
        profile.setCitizenship(StringUtils.stripToNull(data.citizenship));
        profile.setGender(StringUtils.stripToNull(data.gender));
        profile.setInn(StringUtils.stripToNull(data.inn));
        profile.setNameFirst(StringUtils.stripToNull(data.firstName));
        profile.setNameLast(StringUtils.stripToNull(data.lastName));
        profile.setNameMiddle(StringUtils.stripToNull(data.middleName));

        Optional<EsiaDocInfo> passport = client.passport.get();
        profile.setDocIssueDate(passport.map(doc -> doc.issueDate).map(ProviderDataLogic::parseLocalDate).orElse(null));
        profile.setDocIssuePlace(passport.map(doc -> doc.issuedBy).map(StringUtils::stripToNull).orElse(null));
        profile.setDocNum(passport.map(doc -> doc.number).map(StringUtils::stripToNull).orElse(null));
        profile.setDocSer(passport.map(doc -> doc.series).map(StringUtils::stripToNull).orElse(null));

        profile.setPhoneNumber(client.contactMobile.get().map(contact -> contact.value).orElse(null));
        profile.setEmail(client.contactEmail.get().map(contact -> contact.value).orElse(null));


        Optional<SupplementaryAttributeDto> trustedAttribute = Arrays
                .stream(attributes)
                .filter(attribute -> ATTRIBUTE_TRUSTED.equals(attribute.getKey()))
                .findFirst();

        if (trustedAttribute.isPresent()) {
            trustedAttribute.get().setValue(Boolean.toString(client.info.get().trusted));

        } else {
            attributes = Arrays.copyOf(attributes, attributes.length + 1);
            attributes[attributes.length - 1] = new SupplementaryAttributeDto(
                    ATTRIBUTE_TRUSTED, Boolean.toString(client.info.get().trusted)
            );
        }

        profile.setUserProfileAttributes(attributes);

    }

    private static LocalDate parseLocalDate(String value) {
        return value != null && value.length() > 0
                ? LocalDate.parse(value, DateTimeFormatter.ofPattern(ESIA_DATE_FORMAT))
                : null;
    }


    public void processGroups(List<EsiaGroupInfo> esiaGroupInfoList, UserAccountDtoRead account) {

        val intendedSecurityGroups = new HashSet<SecurityGroupDto>();
        if (esiaGroupInfoList != null && !esiaGroupInfoList.isEmpty()) {
            esiaGroupInfoList.forEach(esiaGroupInfo -> get0createEsiaGroup(esiaGroupInfo)
                    .getPreexisted() // Искать связи имеет смысл только для тех групп, которые уже существовали
                    .map(esiaGroup -> getSecurityGroupFor(esiaGroup, account.getSegmentId()))
                    .ifPresent(intendedSecurityGroups::addAll)
            );
        }

        List<SecurityGroupDto> securityGroups = getSecurityGroupFor(account);
        new CollectionUpdater<>(SecurityGroupDto::getId, SecurityGroupDto::getId)

                .createElement(securityGroup ->
                        createUserAccount2SecurityGroup(account, securityGroup)
                )

                .deleteElement(securityGroup ->
                        removeUserAccount2SecurityGroup(account, securityGroup)
                )

                .collection(securityGroups, intendedSecurityGroups);
    }

    @SneakyThrows({IOException.class, URISyntaxException.class})
    private List<SecurityGroupDto> getSecurityGroupFor(EsiaGroupDto esiaGroup, UUID segmentId) {
        return accessManagerRestClient.getSecurityGroupsListByEsiaGroupId(esiaGroup.getId(), segmentId);
    }

    /**
     * Возвращает текущие группы безопасности указанной учётной записи, имеющие связь с какими-либо группами ЕСИА.
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private List<SecurityGroupDto> getSecurityGroupFor(UserAccountDtoRead account) {
        return accessManagerRestClient.getSecurityGroupsListByAccountId(account.getId());
    }

    /**
     * Находит либо создаёт в AccessManager внешнюю группу безопасности, соответствующую указанной группе из ЕСИА.
     */
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private Reliable<EsiaGroupDto> get0createEsiaGroup(EsiaGroupInfo group) {
        return Reliable.ofNullable(
                accessManagerRestClient.findEsiaGroupBySysname(group.grp_id),
                () -> createEsiaGroup(group)
        );
    }

    @SneakyThrows({IOException.class, URISyntaxException.class})
    private EsiaGroupDto createEsiaGroup(EsiaGroupInfo esiaGroupInfo) {
        return accessManagerRestClient.createEsiaGroup(esiaGroupInfo);
    }

    @SneakyThrows({IOException.class, URISyntaxException.class})
    private void removeUserAccount2SecurityGroup(UserAccountDtoRead account, SecurityGroupDto securityGroup) {
        accessManagerRestClient.removeUserAccountFromSecurityGroup(account.getId(), securityGroup.getId());
    }

    @SneakyThrows({IOException.class, URISyntaxException.class})
    private void createUserAccount2SecurityGroup(UserAccountDtoRead account, SecurityGroupDto securityGroup) {
        accessManagerRestClient.addUserAccountToSecurityGroup(account.getId(), securityGroup.getId());
    }

}
