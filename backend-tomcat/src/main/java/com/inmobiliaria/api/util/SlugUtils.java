package com.inmobiliaria.api.util;

import java.text.Normalizer;

public final class SlugUtils {
  private SlugUtils() {
  }

  public static String slugify(String text) {
    if (text == null || text.isBlank()) {
      return "propiedad";
    }

    String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
    normalized = normalized.replaceAll("\\p{M}", "");
    normalized = normalized.replaceAll("[^a-z0-9]+", "-");
    normalized = normalized.replaceAll("(^-|-$)", "");
    return normalized.isBlank() ? "propiedad" : normalized;
  }
}
