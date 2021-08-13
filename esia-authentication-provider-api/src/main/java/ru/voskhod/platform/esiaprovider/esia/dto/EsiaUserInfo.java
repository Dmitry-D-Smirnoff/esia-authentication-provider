package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaUserInfo implements Serializable {

    public String firstName;
    public String lastName;
    public String middleName;

    public String snils;
    public String inn;

    public String birthDate;
    public String birthPlace;

    public String citizenship;
    public String gender;

    public String status;
    public boolean trusted;
    public String verifying;

    public long updatedOn;

    public String rIdDoc;

    public String regCtxCfmSte;

    public String[] chosenCfmTypes;

    public String regType;

    public boolean containsUpCfmCode;

    public String eTag;

}