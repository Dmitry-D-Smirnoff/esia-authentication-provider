package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemSettingDto {

    private UUID id;

    private String subSystem;
    private String sysname;
    private String description;
    private String value;

    private Instant modificationDate;
    private UUID modifierId;
    private String modifierFio;


    public String systemSettingKey() {
        return MessageFormat.format("{0}.{1}", subSystem, sysname);
    }

}
