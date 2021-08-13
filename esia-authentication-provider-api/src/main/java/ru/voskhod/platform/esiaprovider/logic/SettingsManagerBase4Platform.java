package ru.voskhod.platform.esiaprovider.logic;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.voskhod.platform.common.settings.SettingsManager;
import ru.voskhod.platform.esiaprovider.client.AccessManagerRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.SystemSettingDto;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class SettingsManagerBase4Platform extends SettingsManager<SystemSettingDto, String> {

    @Inject
    private AccessManagerRestClient accessManagerRestClient;


    @Override
    protected String settingKey(SystemSettingDto settingElement) {
        return settingElement.systemSettingKey();
    }

    @Override
    protected String settingValue(SystemSettingDto settingElement) {
        return settingElement.getValue();
    }


    @Override
    @SneakyThrows
    protected List<SystemSettingDto> readSettings() {
        return accessManagerRestClient.getSystemSettingList();
    }

}
