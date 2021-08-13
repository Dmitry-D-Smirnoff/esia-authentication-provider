package ru.voskhod.platform.esiaprovider.logic;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.voskhod.platform.common.settings.SettingsManager;
import ru.voskhod.platform.esiaprovider.client.AccessManagerRestClient;
import ru.voskhod.platform.esiaprovider.client.dto.LoggingEventSettingDto;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class SettingsManagerBase4Logging extends SettingsManager<LoggingEventSettingDto, String> {

    @Inject
    private AccessManagerRestClient accessManagerRestClient;


    @Override
    protected String settingKey(LoggingEventSettingDto settingElement) {
        return settingElement.getSysname();
    }

    @Override
    protected String settingValue(LoggingEventSettingDto settingElement) {
        return settingElement.getMode();
    }

    @Override
    @SneakyThrows
    protected List<LoggingEventSettingDto> readSettings() {
        return accessManagerRestClient.getLoggingSettingList();
    }
}
