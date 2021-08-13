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
public class UserAccountDtoRead {

    private static final String STATUS_Deleted    = "DELETED";
    private static final String STATUS_Registered = "REGISTERED";
    private static final String STATUS_Active     = "ACTIVE";

    public static final UUID TYPE_COMMON = UUID.fromString("4af111db-0e0c-47b6-a87a-d19e2cf83f1b");


    private UUID id;

    private Integer version;

    private String login;

    private UUID segmentId;

    private String segmentSysname;

    private String segmentName;

    private Boolean isSystemSegment;

    private Boolean isPreferred;

    private String accountType;

    private UUID accountTypeId;

    private String accountTypeName;

    private String status;

    private String statusName;

    private String statusChangeReason;

    private Instant statusExpirationDate;

    private String statusAfterExpiration;

    private UUID userProfileId;

    private Instant lastUseDate;

    private Instant statusUpdateDate;

    private Instant passwordUpdateDate;

    private SupplementaryAttributeDto[] userAccountAttributes = new SupplementaryAttributeDto[0];

    private StampsDto stamps;


    public boolean isActive() {
        return STATUS_Active.equals(status);
    }

    public boolean isRegistered() {
        return STATUS_Registered.equals(status);
    }

    public boolean isDeleted() {
        return STATUS_Deleted.equals(status);
    }

}
