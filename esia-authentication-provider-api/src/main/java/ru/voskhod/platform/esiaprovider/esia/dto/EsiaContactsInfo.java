package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaContactsInfo {

    public Integer size;
    public EsiaContactInfo[] elements;

}
