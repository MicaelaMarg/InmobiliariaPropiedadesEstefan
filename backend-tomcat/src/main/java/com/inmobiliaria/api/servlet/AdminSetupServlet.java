package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.Database;
import com.inmobiliaria.api.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/admin/setup")
public class AdminSetupServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String email = req.getParameter("email");
    String password = req.getParameter("password");

    if (email == null || password == null) {
      JsonUtil.writeError(resp, 400, "Datos incompletos");
      return;
    }

    try (Connection conn = Database.getConnection()) {

      String sql = "INSERT INTO admin_users (email, password_hash) VALUES (?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);

      ps.setString(1, email);
      ps.setString(2, password);

      ps.executeUpdate();

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);

      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, result);

    } catch (Exception e) {
      JsonUtil.writeError(resp, 500, "No se pudo crear la cuenta");
    }
  }
}
