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
public class StampsDto {

    private Instant creationDate;
    private Instant modificationDate;

    private UUID creatorId;
    private UUID modifierId;

    private String creatorFio;
    private String modifierFio;

}
