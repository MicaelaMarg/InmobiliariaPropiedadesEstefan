package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.AdminRepository;
import com.inmobiliaria.api.util.JsonUtil;
import com.inmobiliaria.api.util.PasswordUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/admin/setup")
public class AdminSetupServlet extends HttpServlet {

  private final AdminRepository repository = new AdminRepository();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    Map<String, String> body = JsonUtil.readJson(req);

    String email = body.get("email");
    String password = body.get("password");

    if (email == null || password == null) {
      JsonUtil.writeError(resp, 400, "Datos incompletos");
      return;
    }

    if (repository.countAdmins() > 0) {
      JsonUtil.writeError(resp, 400, "Ya existe un administrador");
      return;
    }

    String hash = PasswordUtil.hash(password);

    repository.createAdmin(email, hash);

    JsonUtil.writeJson(resp, 200, Map.of("success", true));
  }
}
