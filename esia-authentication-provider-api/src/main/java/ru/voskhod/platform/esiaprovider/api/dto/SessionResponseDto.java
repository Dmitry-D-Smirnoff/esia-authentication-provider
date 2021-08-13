package ru.voskhod.platform.esiaprovider.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionResponseDto {

    @Schema(title = "сессионный токен")
    private String sessionToken;

    @Schema(title = "идентификатор текущей учетной записи")
    private UUID currentUserId;

    @Schema(title = "статус учетной записи")
    private String userAccountStatus;

    @Schema(title = "токен доступа ЕСИА")
    private String esiaToken;

    @Schema(title = "причина отказа в выдаче токена")
    private String reason;

}
