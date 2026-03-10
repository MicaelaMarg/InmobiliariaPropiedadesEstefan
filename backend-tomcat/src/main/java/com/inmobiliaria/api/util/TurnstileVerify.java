package com.inmobiliaria.api.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Verificación server-side de Cloudflare Turnstile.
 * Si TURNSTILE_SECRET_KEY no está definida, no se valida (útil en desarrollo).
 */
public final class TurnstileVerify {
  private static final String SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
  private static final Duration TIMEOUT = Duration.ofSeconds(10);

  private TurnstileVerify() {
  }

  /**
   * Verifica el token con la API de Cloudflare. Si no hay secret configurado, devuelve true.
   */
  @SuppressWarnings("unchecked")
  public static boolean verify(String token, String remoteIp) {
    String secret = System.getenv("TURNSTILE_SECRET_KEY");
    if (secret == null || secret.isBlank()) {
      return true;
    }
    if (token == null || token.isBlank()) {
      return false;
    }
    try {
      Map<String, String> body = Map.of(
        "secret", secret,
        "response", token,
        "remoteip", remoteIp != null ? remoteIp : ""
      );
      String json = JsonUtil.gson().toJson(body);
      HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(SITEVERIFY_URL))
        .timeout(TIMEOUT)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
        .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      Map<String, Object> result = JsonUtil.gson().fromJson(response.body(), Map.class);
      return Boolean.TRUE.equals(result != null ? result.get("success") : false);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      return false;
    }
  }
}
