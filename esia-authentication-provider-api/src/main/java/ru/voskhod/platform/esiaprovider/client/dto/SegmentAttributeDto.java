package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Атрибут сегмента")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmentAttributeDto {

    private String key;
    private String value;

}
