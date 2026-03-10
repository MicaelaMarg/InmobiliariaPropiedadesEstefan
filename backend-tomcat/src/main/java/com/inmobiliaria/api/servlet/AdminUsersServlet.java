package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.AdminUserRepository;
import com.inmobiliaria.api.util.JsonUtil;
import com.inmobiliaria.api.util.PasswordStrengthUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Gestión de administradores desde el panel (solo usuarios ya logueados como admin).
 * GET: lista todos los admins (id, email, createdAt).
 * POST: crea un nuevo admin (email, password). Misma validación de contraseña que /auth/setup.
 */
@WebServlet(urlPatterns = "/api/admin/users")
public class AdminUsersServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setContentType("application/json");
    try {
      AdminUserRepository repo = new AdminUserRepository();
      List<AdminUserRepository.AdminListRow> list = repo.findAll();
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of("users", list));
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al listar administradores");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setContentType("application/json");

    if (!"application/json".equalsIgnoreCase(req.getContentType() != null ? req.getContentType().split(";")[0].trim() : "")) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Content-Type debe ser application/json");
      return;
    }

    Map<?, ?> body;
    try {
      body = JsonUtil.gson().fromJson(req.getReader(), Map.class);
    } catch (Exception e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cuerpo JSON inválido");
      return;
    }
    if (body == null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cuerpo vacío");
      return;
    }
    String email = body.get("email") != null ? body.get("email").toString().trim() : "";
    String password = body.get("password") != null ? body.get("password").toString() : "";
    if (email.isEmpty()) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "El email es obligatorio");
      return;
    }
    if (password == null || password.isBlank()) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "La contraseña es obligatoria");
      return;
    }
    String strengthError = PasswordStrengthUtil.validate(password);
    if (strengthError != null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, strengthError);
      return;
    }

    try {
      AdminUserRepository repo = new AdminUserRepository();
      if (repo.existsByEmail(email)) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, "Ya existe un administrador con ese email");
        return;
      }
      repo.createAdmin(email, password);
      JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Administrador creado correctamente"));
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear el administrador");
    }
  }
}
