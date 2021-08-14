package ru.voskhod.platform.esiaprovider.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.voskhod.platform.esiaprovider.api.dto.AuthCodeDto;
import ru.voskhod.platform.esiaprovider.api.dto.AuthCodeExtendedDto;
import ru.voskhod.platform.esiaprovider.api.dto.OrgDto;
import ru.voskhod.platform.esiaprovider.api.dto.SessionResponseDto;
import ru.voskhod.platform.esiaprovider.logic.CookieUtils;
import ru.voskhod.platform.esiaprovider.logic.ProviderAuthenticationLogic;
import ru.voskhod.platform.esiaprovider.logic.SessionNotCreated;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;

@Path("esiaauth")
@Tag(name = "esiaauth")
public class ProviderAuthenticationApi {

    @Inject
    private CookieUtils cookieUtils;

    @EJB
    private ProviderAuthenticationLogic authBl;
/*
    @POST
    @Path("getorgs")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(summary = "Возвращает из ЕСИА список организаций пользователя на основании кода авторизации")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешно",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrgDto.class)))
            ),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/response_400"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/response_409")}
    )
    public Response esiaGetOrganizationsByAuthCode(@Parameter(description = "авторизационный код ЕСИА и state (случайный набор байт из ЕСИА)") AuthCodeDto body,
                                                   @Context SecurityContext securityContext) {

        return Response.ok().entity(authBl.getOrganizationsByAuthCode(body)).build();
    }
*/
    @POST
    @Path("getorgs")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(summary = "Возвращает список организаций пользователя по сегментам его учетных записей из ЦАП на основании кода авторизации ЕСИА")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешно",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrgDto.class)))
            ),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/response_400"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/response_409")}
    )
    public Response esiaGetOrganizationsByAuthCode(@Parameter(description = "авторизационный код ЕСИА и state (случайный набор байт из ЕСИА)") AuthCodeDto body,
                                                   @Context SecurityContext securityContext) {

        return Response.ok().entity(authBl.getSegmentOrganizationsByAuthCode(body)).build();
    }

    @POST
    @Path("auth/code")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @Operation(summary = "Выполняет аутентификацию в ЕСИА и в платформе на основе кода авторизации")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Успешно",
                    content = @Content(schema = @Schema(implementation = SessionResponseDto.class)),
                    headers = @Header(name = "Set-Cookie", ref = "#/components/headers/session_token")
            ),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/response_400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/response_401"),
            @ApiResponse(
                    responseCode = "403",
                    description = "В создании сессии отказано",
                    content = @Content(schema = @Schema(implementation = SessionResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Превышено максимальное число одновременных сессий",
                    content = @Content(schema = @Schema(implementation = SessionResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/response_500")}
    )
    public Response esiaAuthenticateByAuthCode(
            @Parameter(description = "авторизационный код ЕСИА и state (случайный набор байт из ЕСИА)") AuthCodeExtendedDto body) {

        try {
            SessionResponseDto dto = authBl.authenticateByAuthCode(body);
            return Response.ok()
                    .entity(dto)
                    .cookie(cookieUtils.create(dto.getSessionToken()))
                    .build();

        } catch (SessionNotCreated e) {
            return Response
                    .status(e.responseStatus)
                    .entity(e.sessionResponseDto)
                    .cookie(cookieUtils.create(e.sessionResponseDto.getSessionToken()))
                    .build();
        }

    }

    @GET
    @Path("login")
    @Produces({"application/json"})
    @Operation(summary = "Формирует URL для аутентификации через ЕСИА и возвращает её с кодом 307 (temporary redirect)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "307", description = "temporary redirect"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/response_400"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/response_409")})
    public Response esiaLogin(
            @Parameter(description = "идентификатор организации в ЕСИА") @QueryParam("org_id") String orgId,
            @HeaderParam("Referer") URI referer) {

        return Response.temporaryRedirect(
                authBl.getAccessCodeUrl(orgId, referer)
        ).build();

    }

    @GET
    @Path("logout")
    @Produces({"application/json"})
    @Operation(
            summary = "Обрывает сессию в платформе; формирует URL ЕСИА для выхода из системы  и возвращает её с кодом 307 (temporary redirect)",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "307", description = "temporary redirect"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/response_401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/response_403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/response_500")})
    public Response esiaLogout(@HeaderParam("Referer") URI referer,
                               @Context SecurityContext securityContext) {

        return Response.temporaryRedirect(authBl.logout(securityContext, referer))
                .cookie(cookieUtils.create(null))
                .build();

    }

    @GET
    @Path("esialogoutredirect")
    @Produces({"application/json"})
    @Operation(
            summary = "Формирует URL ЕСИА для выхода из ЕСИА и возвращает его с кодом 307 (temporary redirect)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "307", description = "temporary redirect"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/response_500")})
    public Response esiaLogoutRedirect(
            @HeaderParam("Referer") URI referer,
            @Context SecurityContext securityContext
    ) {

        return Response.temporaryRedirect(authBl.esiaLogoutRedirect(referer))
                .cookie(cookieUtils.create(null))
                .build();

    }

}
