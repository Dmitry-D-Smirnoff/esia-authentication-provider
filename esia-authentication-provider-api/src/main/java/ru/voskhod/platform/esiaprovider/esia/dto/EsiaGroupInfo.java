package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaGroupInfo implements Serializable {

    public String grp_id;

    public String name;
    public String description;

    public String system;

    public String itSystem;
    public String itSystemName;

    public Boolean requireSignature;

}
