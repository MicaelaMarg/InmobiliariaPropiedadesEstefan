package com.inmobiliaria.api.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de contraseña segura: mínimo 8 caracteres, al menos una mayúscula, un número y un símbolo.
 */
public final class PasswordStrengthUtil {
  private static final int MIN_LENGTH = 8;

  private PasswordStrengthUtil() {
  }

  /** Devuelve null si es válida, o mensaje de error. */
  public static String validate(String password) {
    if (password == null || password.length() < MIN_LENGTH) {
      return "La contraseña debe tener al menos " + MIN_LENGTH + " caracteres.";
    }
    List<String> errors = new ArrayList<>();
    if (!password.matches(".*[A-Z].*")) {
      errors.add("al menos una mayúscula");
    }
    if (!password.matches(".*[0-9].*")) {
      errors.add("al menos un número");
    }
    if (!password.matches(".*[^A-Za-z0-9].*")) {
      errors.add("al menos un símbolo (ej. !@#$%&*)");
    }
    if (errors.isEmpty()) return null;
    return "La contraseña debe tener: " + String.join(", ", errors) + ".";
  }

  public static boolean isValid(String password) {
    return validate(password) == null;
  }
}
