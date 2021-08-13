package ru.voskhod.platform.esiaprovider.esia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaGroupsInfo implements Serializable {

    public Integer size;
    public EsiaGroupInfo[] elements;

}
