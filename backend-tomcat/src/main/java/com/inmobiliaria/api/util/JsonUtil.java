package com.inmobiliaria.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class JsonUtil {
  private static final Gson GSON = new GsonBuilder().serializeNulls().create();

  private JsonUtil() {
  }

  public static Gson gson() {
    return GSON;
  }

  public static <T> T readBody(HttpServletRequest request, Class<T> type) throws IOException {
    try (Reader reader = request.getReader()) {
      return GSON.fromJson(reader, type);
    }
  }

  public static void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("application/json");
    response.getWriter().write(GSON.toJson(body));
  }

  public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
    writeJson(response, status, Map.of("error", message));
  }
}
