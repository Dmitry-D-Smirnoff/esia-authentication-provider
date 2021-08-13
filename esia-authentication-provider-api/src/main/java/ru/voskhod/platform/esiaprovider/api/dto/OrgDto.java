package ru.voskhod.platform.esiaprovider.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(title = "Организация")
public class OrgDto {

    @NotNull
    @Schema(required = true,
            title = "идентификатор организации в ЕСИА")
    private String esiaOrgId;

    @Schema(title = "основной государственный регистрационный номер организации")
    @JsonProperty("OGRN")
    private String OGRN;

    @Schema(title = "полное наименование организации")
    private String fullName;

    @Schema(title = "сокращенное наименование организации")
    private String shortName;

    @Schema(title = "наименование филиала")
    private String branchName;

    @Schema(title = "тип организации")
    private String type;

}
