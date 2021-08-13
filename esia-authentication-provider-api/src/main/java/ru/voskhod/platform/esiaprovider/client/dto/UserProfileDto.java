package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileDto {

    private UUID id;

    private Integer version;

    private String snils;

    private String status;

    private String statusChangeReason;

    private String nameFirst;

    private String nameLast;

    private String nameMiddle;

    private String inn;

    private String docSer;

    private String docNum;

    private LocalDate docIssueDate;

    private String docIssuePlace;

    private Integer esiaSbjId;

    private String gender;

    private LocalDate birthDate;

    private String birthPlace;

    private String citizenship;

    private String email;

    private Boolean hasUserAccounts;

    private String otherInfo;

    private SupplementaryAttributeDto[] userProfileAttributes = new SupplementaryAttributeDto[0];

    private StampsDto stamps;

}
