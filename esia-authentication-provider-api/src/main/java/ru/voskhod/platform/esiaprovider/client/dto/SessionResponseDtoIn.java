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
public class SessionResponseDtoIn {

    private String sessionToken;

    private UUID currentUserId;
    private String userAccountStatus;

    private String reason;
    private int reasonCode;

    private String otherInfo;

}
