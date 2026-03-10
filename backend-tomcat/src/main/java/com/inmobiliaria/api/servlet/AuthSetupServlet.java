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
import java.util.Map;

/**
 * Crear la primera cuenta de administrador (solo si aún no hay ninguno).
 * Útil cuando se usa MySQL: creás tu cuenta desde el navegador en /admin/setup.
 */
@WebServlet("/api/auth/setup")
public class AuthSetupServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setContentType("application/json");
    try {
      AdminUserRepository repo = new AdminUserRepository();
      long count = repo.count();
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of("setupRequired", count == 0));
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al verificar estado");
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
      if (repo.count() > 0) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Ya existe un administrador. Usá la pantalla de login.");
        return;
      }
      repo.createAdmin(email, password);
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Cuenta creada. Ya podés iniciar sesión."));
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear la cuenta");
    }
  }
}
