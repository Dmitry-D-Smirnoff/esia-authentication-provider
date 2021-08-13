package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaContactInfo {

    public static final String TYPE_MobilePhone = "MBT";
    public static final String TYPE_HomePhone   = "PHN";
    public static final String TYPE_Email       = "EML";

    public static final String STATE_NotVerified = "NOT_VERIFIED";
    public static final String STATE_Verified    = "VERIFIED";


    public int id;
    public String type;
    public String vrfStu;
    public String value;

}