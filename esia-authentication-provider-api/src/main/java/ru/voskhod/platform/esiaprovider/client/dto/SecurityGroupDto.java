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
public class SecurityGroupDto {

    private UUID id;

    private Integer version;

    private UUID segmentId;

    private String segmentSysname;

    private String segmentName;

    private String sysname;

    private String name;

    private String description;

    private boolean crossSegmentMembershipAllowed;

    private boolean hasUserAccounts;

    private boolean hasRoles;

    private SupplementaryAttributeDto[] securityGroupAttributes = new SupplementaryAttributeDto[0];

    private StampsDto stamps;

}
