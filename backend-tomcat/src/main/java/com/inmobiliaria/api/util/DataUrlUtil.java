package com.inmobiliaria.api.util;

import java.util.Base64;

public final class DataUrlUtil {
  private DataUrlUtil() {
  }

  public static boolean isDataUrl(String value) {
    return value != null && value.startsWith("data:");
  }

  public static DecodedDataUrl decode(String value) {
    if (!isDataUrl(value)) {
      throw new IllegalArgumentException("Valor no es un data URL");
    }

    int commaIndex = value.indexOf(',');
    if (commaIndex < 0) {
      throw new IllegalArgumentException("Data URL invalido");
    }

    String metadata = value.substring(5, commaIndex);
    String payload = value.substring(commaIndex + 1);
    String[] parts = metadata.split(";");
    String contentType = parts.length > 0 && !parts[0].isBlank()
      ? parts[0]
      : "application/octet-stream";

    byte[] bytes = Base64.getDecoder().decode(payload);
    return new DecodedDataUrl(contentType, bytes);
  }

  public record DecodedDataUrl(String contentType, byte[] bytes) {
  }
}
