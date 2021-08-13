package ru.voskhod.platform.esiaprovider.client.dto;

import lombok.Data;

@Data
public class VerifySignatureDataRequest {

    private byte[] data;

    private byte[] signature;

    private byte[] certificate;
}
