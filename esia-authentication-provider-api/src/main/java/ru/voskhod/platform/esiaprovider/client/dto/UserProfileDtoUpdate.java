package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileDtoUpdate implements UserProfileDtoBaseUpdatable {

    private Integer version;

    private String snils;

    @NotNull
    private String nameFirst;

    @NotNull
    private String nameLast;

    private String nameMiddle;

    private String inn;

    private String docSer;

    private String docNum;

    private LocalDate docIssueDate;

    private String docIssuePlace;

    private String gender;

    private LocalDate birthDate;

    private String birthPlace;

    private String citizenship;

    private String email;

    private String phoneNumber;

    private String otherInfo;

    private SupplementaryAttributeDto[] userProfileAttributes = new SupplementaryAttributeDto[0];

}
