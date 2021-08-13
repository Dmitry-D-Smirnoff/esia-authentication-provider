package ru.voskhod.platform.esiaprovider.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Ответ на запрос маркера доступа ЕСИА
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsiaOAuthAccessTokenResponse {
    /**
     * Маркер идентификации
     */
    @JsonProperty("id_token")
    private String idToken;

    /**
     * Маркер доступа для данного ресурса(если он запрашивался)
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Время, в течение которого истекает срок действия маркера (в секундах)
     */
    @JsonProperty("expires_in")
    private int expiresPeriod;

    /**
     * Идентификатор запроса
     */
    @JsonProperty("state")
    private String state;

    /**
     * Тип предоставленного маркера (в настоящее время ЕСИА поддерживает только значение "Bearer")
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * Маркер обновления для данного ресурса
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

}
