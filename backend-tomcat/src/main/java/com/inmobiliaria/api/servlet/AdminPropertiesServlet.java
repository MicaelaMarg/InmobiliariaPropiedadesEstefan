package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.repository.PropertyRepository;
import com.inmobiliaria.api.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/admin/properties/*")
public class AdminPropertiesServlet extends HttpServlet {
  private final PropertyRepository repository = new PropertyRepository();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Long id = extractId(req);

    try {
      if (id == null) {
        JsonUtil.writeJson(
          resp,
          HttpServletResponse.SC_OK,
          repository.findAdmin(
            req.getParameter("search"),
            req.getParameter("status"),
            parseBoolean(req.getParameter("isPublished"))
          )
        );
        return;
      }

      Property property = repository.findById(id);
      if (property == null) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Propiedad no encontrada");
        return;
      }

      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, property);
    } catch (Exception e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al cargar propiedades");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (extractId(req) != null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Ruta invalida");
      return;
    }

    try {
      Property payload = JsonUtil.readBody(req, Property.class);
      if (payload == null) payload = new Property();
      JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED, repository.create(payload));
    } catch (Exception e) {
      e.printStackTrace(System.err);
      String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear propiedad: " + msg);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Long id = extractId(req);
    if (id == null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Falta el id");
      return;
    }

    try {
      Property payload = JsonUtil.readBody(req, Property.class);
      Property updated = repository.update(id, payload);
      if (updated == null) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Propiedad no encontrada");
        return;
      }
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, updated);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al actualizar propiedad: " + msg);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Long id = extractId(req);
    if (id == null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Falta el id");
      return;
    }

    try {
      if (!repository.delete(id)) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Propiedad no encontrada");
        return;
      }
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, java.util.Map.of("success", true));
    } catch (Exception e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al eliminar propiedad");
    }
  }

  private Long extractId(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
      return null;
    }

    String raw = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
    if (raw.contains("/")) {
      return null;
    }

    try {
      return Long.parseLong(raw);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Boolean parseBoolean(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return "1".equals(value) || "true".equalsIgnoreCase(value);
  }
}
