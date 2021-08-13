package ru.voskhod.platform.esiaprovider.client.dto;

import java.time.LocalDate;

public interface UserProfileDtoBaseUpdatable {

    void setSnils(String snils);

    void setNameFirst(String nameFirst);

    void setNameLast(String nameLast);

    void setNameMiddle(String nameMiddle);

    void setInn(String inn);

    void setDocSer(String docSer);

    void setDocNum(String docNum);

    void setDocIssueDate(LocalDate docIssueDate);

    void setDocIssuePlace(String docIssuePlace);

    void setGender(String gender);

    void setBirthDate(LocalDate birthDate);

    void setBirthPlace(String birthPlace);

    void setCitizenship(String citizenship);

    void setEmail(String email);

    void setPhoneNumber(String phoneNumber);

    void setOtherInfo(String otherInfo);

    void setUserProfileAttributes(SupplementaryAttributeDto[] userProfileAttributes);

}
