package ru.voskhod.platform.esiaprovider.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EsiaGroupDto {

    private UUID id;

    @NonNull
    private String sysname;

    @NonNull
    private String name;

    private String description;


    public EsiaGroupDto(@NonNull String sysname, @NonNull String name, String description) {
        this.sysname = sysname;
        this.name = name;
        this.description = description;
    }

}
