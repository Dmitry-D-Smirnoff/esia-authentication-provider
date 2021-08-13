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
public class UserAccountDtoCreate {

    private UUID accountTypeId;

    private String login;

    private String password;

    private String status;

    private UUID userProfileId;

    private UUID segmentId;

    private SupplementaryAttributeDto[] userAccountAttributes = new SupplementaryAttributeDto[0];

}
