package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.AdminUserRepository;
import com.inmobiliaria.api.repository.PasswordResetTokenRepository;
import com.inmobiliaria.api.util.JsonUtil;
import com.inmobiliaria.api.util.PasswordStrengthUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet("/api/auth/reset-password")
public class ResetPasswordServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setContentType("application/json");

    Map<?, ?> body;
    try {
      body = JsonUtil.gson().fromJson(req.getReader(), Map.class);
    } catch (Exception e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cuerpo JSON inválido");
      return;
    }
    String token = body != null && body.get("token") != null ? body.get("token").toString().trim() : "";
    String newPassword = body != null && body.get("newPassword") != null ? body.get("newPassword").toString() : "";
    if (token.isEmpty()) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Falta el token");
      return;
    }
    if (newPassword == null || newPassword.isBlank()) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "La contraseña es obligatoria");
      return;
    }
    String strengthError = PasswordStrengthUtil.validate(newPassword);
    if (strengthError != null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, strengthError);
      return;
    }

    try {
      PasswordResetTokenRepository tokenRepo = new PasswordResetTokenRepository();
      String adminEmail = tokenRepo.findValidTokenEmail(token);
      if (adminEmail == null) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "El enlace no es válido o ya expiró. Solicitá uno nuevo.");
        return;
      }

      AdminUserRepository adminRepo = new AdminUserRepository();
      if (!adminRepo.updatePasswordByEmail(adminEmail, newPassword)) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo actualizar la contraseña");
        return;
      }
      tokenRepo.markUsed(token);

      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Contraseña actualizada. Ya podés iniciar sesión."));
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al restablecer la contraseña");
    }
  }
}
