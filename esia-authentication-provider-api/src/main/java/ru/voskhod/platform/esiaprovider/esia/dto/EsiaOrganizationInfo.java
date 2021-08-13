package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaOrganizationInfo implements Serializable {

    public String oid;
    public String prnOid;
    public String fullName;
    public String shortName;
    public String ogrn;
    public String type;
    public String chief;
    public String admin;
    public String email;
    public Boolean active;
    public Boolean hasRightOfSubstitution;
    public Boolean hasApprovalTabAccess;

}
