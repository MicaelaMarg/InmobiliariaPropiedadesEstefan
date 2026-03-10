package com.inmobiliaria.api.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Protección contra fuerza bruta: límite de intentos por identificador (email o IP)
 * y bloqueo temporal (ej. 15 min tras 5 fallos).
 */
public class LoginAttemptRepository {
  private static final int MAX_ATTEMPTS = 5;
  private static final long LOCK_MINUTES = 15;

  /** Normaliza el identificador para la clave (email o IP). */
  public static String key(String email, String ip) {
    String e = email != null ? email.trim().toLowerCase() : "";
    String i = ip != null ? ip.trim() : "";
    return (e + "|" + i).intern();
  }

  /** Verifica si está bloqueado. Devuelve segundos restantes de bloqueo o 0 si no está bloqueado. */
  public long getLockedRemainingSeconds(String identifier) throws SQLException {
    if (identifier == null || identifier.isBlank()) return 0;
    String sql = "select locked_until from login_attempts where identifier = ? and locked_until > ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, identifier);
      ps.setString(2, Instant.now().toString());
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return 0;
        String until = rs.getString("locked_until");
        try {
          Instant lockEnd = Instant.parse(until);
          long sec = lockEnd.getEpochSecond() - Instant.now().getEpochSecond();
          return Math.max(0, sec);
        } catch (Exception e) {
          return 0;
        }
      }
    }
  }

  /** Registra un intento fallido. Si alcanza MAX_ATTEMPTS, bloquea por LOCK_MINUTES. */
  public void recordFailure(String identifier) throws SQLException {
    if (identifier == null || identifier.isBlank()) return;
    try (Connection conn = Database.getConnection()) {
      long remaining = getLockedRemainingSeconds(identifier);
      if (remaining > 0) return; // ya bloqueado
      int count = getAttemptCount(conn, identifier) + 1;
      String lockedUntilVal = count >= MAX_ATTEMPTS ? Instant.now().plusSeconds(LOCK_MINUTES * 60).toString() : null;
      try (PreparedStatement del = conn.prepareStatement("delete from login_attempts where identifier = ?")) {
        del.setString(1, identifier);
        del.executeUpdate();
      }
      try (PreparedStatement ins = conn.prepareStatement("insert into login_attempts (identifier, attempt_count, locked_until) values (?, ?, ?)")) {
        ins.setString(1, identifier);
        ins.setInt(2, count);
        ins.setString(3, lockedUntilVal);
        ins.executeUpdate();
      }
    }
  }

  private int getAttemptCount(Connection conn, String identifier) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("select attempt_count from login_attempts where identifier = ?")) {
      ps.setString(1, identifier);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt("attempt_count") : 0;
      }
    }
  }

  /** Limpia intentos tras login exitoso. */
  public void recordSuccess(String identifier) throws SQLException {
    if (identifier == null || identifier.isBlank()) return;
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement("delete from login_attempts where identifier = ?")) {
      ps.setString(1, identifier);
      ps.executeUpdate();
    }
  }
}
