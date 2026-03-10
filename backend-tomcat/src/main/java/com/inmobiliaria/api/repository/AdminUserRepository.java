package com.inmobiliaria.api.repository;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminUserRepository {

  /** Busca un admin por email. Devuelve { id, email, password_hash } o null. */
  public AdminRow findByEmail(String email) throws SQLException {
    if (email == null || email.isBlank()) return null;
    String sql = "select id, email, password_hash from admin_users where lower(trim(email)) = lower(trim(?))";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        AdminRow row = new AdminRow();
        row.id = rs.getLong(1);
        row.email = rs.getString(2);
        String hash = rs.getString(3);
        row.passwordHash = hash != null ? hash.trim() : null;
        return row;
      }
    }
  }

  /** Verifica que la contraseña en texto plano coincida con el hash guardado. */
  public boolean verifyPassword(String plainPassword, String passwordHash) {
    if (plainPassword == null) return false;
    if (passwordHash == null || passwordHash.isBlank()) return false;
    try {
      return BCrypt.checkpw(plainPassword, passwordHash.trim());
    } catch (Exception e) {
      return false;
    }
  }

  /** Crea un admin (para seed). passwordPlain se hashea con BCrypt. */
  public void createAdmin(String email, String passwordPlain) throws SQLException {
    String hash = BCrypt.hashpw(passwordPlain, BCrypt.gensalt(10));
    String sql = "insert into admin_users (email, password_hash, created_at) values (?, ?, ?)";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email.trim());
      ps.setString(2, hash);
      ps.setString(3, java.time.Instant.now().toString());
      ps.executeUpdate();
    }
  }

  /** Actualiza la contraseña de un admin por email (nueva en texto plano, se hashea). */
  public boolean updatePasswordByEmail(String email, String newPasswordPlain) throws SQLException {
    if (email == null || email.isBlank() || newPasswordPlain == null || newPasswordPlain.isBlank()) return false;
    String hash = BCrypt.hashpw(newPasswordPlain, BCrypt.gensalt(10));
    String sql = "update admin_users set password_hash = ? where lower(trim(email)) = lower(trim(?))";
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, hash);
      ps.setString(2, email);
      return ps.executeUpdate() > 0;
    }
  }

  public long count() throws SQLException {
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement("select count(*) from admin_users");
         ResultSet rs = ps.executeQuery()) {
      rs.next();
      return rs.getLong(1);
    }
  }

  /** Lista todos los administradores (id, email, created_at). No expone password_hash. */
  public java.util.List<AdminListRow> findAll() throws SQLException {
    String sql = "select id, email, created_at from admin_users order by created_at asc";
    java.util.List<AdminListRow> list = new java.util.ArrayList<>();
    try (Connection conn = Database.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        AdminListRow row = new AdminListRow();
        row.id = rs.getLong(1);
        row.email = rs.getString(2);
        String t = rs.getString(3);
        row.createdAt = t != null ? t.trim() : null;
        list.add(row);
      }
    }
    return list;
  }

  /** Verifica si ya existe un admin con ese email (case-insensitive). */
  public boolean existsByEmail(String email) throws SQLException {
    if (email == null || email.isBlank()) return false;
    return findByEmail(email) != null;
  }

  public static class AdminRow {
    public long id;
    public String email;
    public String passwordHash;
  }

  /** Fila para listado de admins (sin contraseña). */
  public static class AdminListRow {
    public long id;
    public String email;
    public String createdAt;
  }
}
