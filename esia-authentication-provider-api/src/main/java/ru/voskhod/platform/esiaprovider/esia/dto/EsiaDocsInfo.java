package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaDocsInfo implements Serializable {

    public Integer size;
    public String eTag;
    public EsiaDocInfo[] elements;

}
