package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.repository.PropertyRepository;
import com.inmobiliaria.api.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/properties")
public class PublicPropertiesServlet extends HttpServlet {
  private final PropertyRepository repository = new PropertyRepository();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JsonUtil.writeJson(
        resp,
        HttpServletResponse.SC_OK,
        repository.findPublicPage(
          req.getParameter("operation"),
          req.getParameter("operationTag"),
          req.getParameter("type"),
          parseDouble(req.getParameter("minPrice")),
          parseDouble(req.getParameter("maxPrice")),
          req.getParameter("aptoCredito"),
          req.getParameter("location"),
          parseBoolean(req.getParameter("featured")),
          parseInteger(req.getParameter("page")),
          parseInteger(req.getParameter("limit"))
        )
      );
    } catch (Exception e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al cargar propiedades");
    }
  }

  private Double parseDouble(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Integer parseInteger(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(value);
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
