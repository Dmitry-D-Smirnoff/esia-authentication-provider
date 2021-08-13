package ru.voskhod.platform.esiaprovider.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthCodeDto {

    @NotNull
    @Schema(required = true,
            title = "значение авторизационного кода ЕСИА")
    private String code;

    @NotNull
    @Schema(required = true,
            title = "набор случайных символов, имеющий вид 128-битного идентификатора запроса (необходимо для защиты от перехвата) - приходит из ЕСИА")
    private String state;

}
