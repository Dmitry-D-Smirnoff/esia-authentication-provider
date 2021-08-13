package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentDto {

    private UUID id;

    private Integer version;

    private String sysname;

    private String name;

    private String description;

    private SegmentAttributeDto[] attributes = new SegmentAttributeDto[0];

    private UUID[] parentIdList = new UUID[0];

    private StampsDto stamps;

}
