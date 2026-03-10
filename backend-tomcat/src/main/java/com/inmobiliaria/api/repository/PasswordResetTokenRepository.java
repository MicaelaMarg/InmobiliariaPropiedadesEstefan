package com.inmobiliaria.api.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordResetTokenRepository {

  /** Guarda un token de recuperación (válido 1 hora). */
  public void save(String token, String adminEmail, String expiresAt) throws SQLException {
    String sql = "insert into password_reset_tokens (token, admin_email, expires_at, used) values (?, ?, ?, false)";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, token);
      ps.setString(2, adminEmail);
      ps.setString(3, expiresAt);
      ps.executeUpdate();
    }
  }

  /** Busca un token; devuelve el email del admin o null si no existe, está usado o expiró. */
  public String findValidTokenEmail(String token) throws SQLException {
    if (token == null || token.isBlank()) return null;
    String sql = "select admin_email from password_reset_tokens where token = ? and used = false and expires_at > ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, token.trim());
      ps.setString(2, java.time.Instant.now().toString());
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getString("admin_email") : null;
      }
    }
  }

  /** Marca el token como usado. */
  public void markUsed(String token) throws SQLException {
    String sql = "update password_reset_tokens set used = true where token = ?";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, token);
      ps.executeUpdate();
    }
  }
}
