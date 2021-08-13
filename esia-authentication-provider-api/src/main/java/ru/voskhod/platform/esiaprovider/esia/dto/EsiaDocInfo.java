package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaDocInfo implements Serializable {

    public Integer id;
    public String type;
    public String vrfStu;
    public String series;
    public String number;
    public String issueDate;
    public String issueId;
    public String issuedBy;
    public String eTag;

}
