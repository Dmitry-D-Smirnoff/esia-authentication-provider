package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoggingEventSettingDto {

    private UUID id;

    private String sysname;
    private String name;
    private String mode;

    private Instant modificationDate;
    private UUID modifierId;
    private String modifierFio;

}
