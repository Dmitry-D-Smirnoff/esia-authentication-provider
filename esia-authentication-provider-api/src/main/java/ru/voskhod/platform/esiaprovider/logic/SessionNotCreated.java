package ru.voskhod.platform.esiaprovider.logic;

import lombok.RequiredArgsConstructor;
import ru.voskhod.platform.esiaprovider.api.dto.SessionResponseDto;

import javax.ws.rs.core.Response;

@RequiredArgsConstructor
public class SessionNotCreated extends Exception {

    public final Response.Status responseStatus;

    public final SessionResponseDto sessionResponseDto;
}