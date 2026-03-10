package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.repository.PropertyRepository;
import com.inmobiliaria.api.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/properties/by-slug/*")
public class PublicPropertyBySlugServlet extends HttpServlet {
  private final PropertyRepository repository = new PropertyRepository();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String slug = extractTail(req);
    if (slug == null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Slug invalido");
      return;
    }

    try {
      Property property = repository.findBySlugPublic(slug);
      if (property == null) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Propiedad no encontrada");
        return;
      }
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, property);
    } catch (Exception e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al cargar propiedad");
    }
  }

  private String extractTail(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
      return null;
    }
    return pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
  }
}
