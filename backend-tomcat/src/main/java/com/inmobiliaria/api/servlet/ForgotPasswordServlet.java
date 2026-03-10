package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.AdminUserRepository;
import com.inmobiliaria.api.repository.PasswordResetTokenRepository;
import com.inmobiliaria.api.util.EmailService;
import com.inmobiliaria.api.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/auth/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
  private static final long TOKEN_VALIDITY_SECONDS = 3600; // 1 hora

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
    String email = body != null && body.get("email") != null ? body.get("email").toString().trim() : "";
    if (email.isEmpty()) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "El email es obligatorio");
      return;
    }

    try {
      AdminUserRepository adminRepo = new AdminUserRepository();
      AdminUserRepository.AdminRow admin = adminRepo.findByEmail(email);
      if (admin == null) {
        System.err.println("[inmobiliaria] Forgot password: no hay admin con email " + email);
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Si ese email está registrado, recibirás un enlace para restablecer tu contraseña."));
        return;
      }

      String token = UUID.randomUUID().toString().replace("-", "");
      String expiresAt = Instant.now().plusSeconds(TOKEN_VALIDITY_SECONDS).toString();
      PasswordResetTokenRepository tokenRepo = new PasswordResetTokenRepository();
      tokenRepo.save(token, admin.email, expiresAt);

      if (EmailService.isConfigured()) {
        EmailService.sendPasswordResetEmail(admin.email, token);
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Si ese email está registrado, recibirás un enlace para restablecer tu contraseña."));
      } else {
        String baseUrl = System.getenv("FRONTEND_URL");
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "http://localhost:5173";
        String resetLink = baseUrl.replaceAll("/+$", "") + "/admin/reset-password?token=" + token;
        System.err.println("[inmobiliaria] ===== ENLACE PARA RESTABLECER CONTRASEÑA (copiá y abrí en el navegador) =====");
        System.err.println(resetLink);
        System.err.println("[inmobiliaria] ===== El correo no está configurado; por eso el enlace está acá. Para recibirlo por email, configurá SMTP_USER, SMTP_APP_PASSWORD y FRONTEND_URL. =====");
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, Map.of(
          "message",
          "El correo no está configurado en el servidor. Revisá la pestaña Output de NetBeans (donde corre Tomcat): ahí se imprimió el enlace para restablecer la contraseña. Copialo y abrilo en el navegador."
        ));
      }
    } catch (jakarta.mail.MessagingException e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo enviar el correo. Revisá la configuración SMTP.");
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar la solicitud");
    }
  }
}
