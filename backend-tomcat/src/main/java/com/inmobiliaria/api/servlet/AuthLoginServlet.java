package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.AdminUserRepository;
import com.inmobiliaria.api.repository.LoginAttemptRepository;
import com.inmobiliaria.api.util.JsonUtil;
import com.inmobiliaria.api.util.JwtUtil;
import com.inmobiliaria.api.util.TurnstileVerify;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet("/api/auth/login")
public class AuthLoginServlet extends HttpServlet {
  private static final long JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 horas

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
    if (email.isEmpty() || password.isEmpty()) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email y contraseña son obligatorios");
      return;
    }

    String turnstileToken = body.get("turnstileToken") != null ? body.get("turnstileToken").toString().trim() : "";
    if (!TurnstileVerify.verify(turnstileToken, req.getRemoteAddr())) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
        "Verificación de seguridad fallida. Recargá la página e intentá de nuevo.");
      return;
    }

    String identifier = LoginAttemptRepository.key(email, req.getRemoteAddr());
    LoginAttemptRepository attemptRepo = new LoginAttemptRepository();

    try {
      long lockedSec = attemptRepo.getLockedRemainingSeconds(identifier);
      if (lockedSec > 0) {
        long min = lockedSec / 60;
        JsonUtil.writeError(resp, 429, // Too Many Requests (SC_TOO_MANY_REQUESTS en Servlet 6.1+)
          "Demasiados intentos fallidos. Esperá " + min + " minutos antes de volver a intentar.");
        return;
      }

      AdminUserRepository repo = new AdminUserRepository();
      AdminUserRepository.AdminRow admin = repo.findByEmail(email);
      if (admin == null) {
        attemptRepo.recordFailure(identifier);
        System.err.println("[inmobiliaria] Login 401: no existe admin con email " + email);
        JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Email o contraseña incorrectos");
        return;
      }
      if (!repo.verifyPassword(password, admin.passwordHash)) {
        attemptRepo.recordFailure(identifier);
        System.err.println("[inmobiliaria] Login 401: contraseña incorrecta para " + email);
        JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Email o contraseña incorrectos");
        return;
      }

      attemptRepo.recordSuccess(identifier);
      String token = JwtUtil.createToken(admin.email, admin.id, JWT_EXPIRATION_MS);
      Map<String, Object> response = Map.of(
        "token", token,
        "user", Map.of("email", admin.email)
      );
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, response);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al iniciar sesión");
    }
  }
}
