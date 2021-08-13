package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileSignResultDto {

    private byte[] signature;

    private byte[] certificate;

    private byte[] digest;

}
