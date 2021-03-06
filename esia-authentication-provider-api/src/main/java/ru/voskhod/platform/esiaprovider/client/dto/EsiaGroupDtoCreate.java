package ru.voskhod.platform.esiaprovider.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EsiaGroupDtoCreate {

    @NonNull
    private String sysname;

    @NonNull
    private String name;

    private String description;

}
