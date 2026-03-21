package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.model.PropertyImage;
import com.inmobiliaria.api.repository.PropertyRepository;
import com.inmobiliaria.api.util.CloudinaryImageService;
import com.inmobiliaria.api.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@MultipartConfig(
  fileSizeThreshold = 1024 * 1024,
  maxFileSize = 20L * 1024 * 1024,
  maxRequestSize = 100L * 1024 * 1024
)
@WebServlet("/api/admin/properties/*")
public class AdminPropertiesServlet extends HttpServlet {
  private final PropertyRepository repository = new PropertyRepository();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Long id = extractId(req);

    try {
      if (id == null) {
        if (parseBoolean(req.getParameter("stats")) == Boolean.TRUE) {
          JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, repository.findAdminStats());
          return;
        }

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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    if (extractId(req) != null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Ruta invalida");
      return;
    }

    try {
      Property payload = readPayload(req);
      JsonUtil.writeJson(resp, HttpServletResponse.SC_CREATED, repository.create(payload));
    } catch (IllegalArgumentException e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage() != null ? e.getMessage() : "Solicitud invalida");
    } catch (Exception e) {
      e.printStackTrace(System.err);
      String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear propiedad: " + msg);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    Long id = extractId(req);
    if (id == null) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Falta el id");
      return;
    }

    try {
      Property payload = readPayload(req);
      Property updated = repository.update(id, payload);
      if (updated == null) {
        JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Propiedad no encontrada");
        return;
      }
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, updated);
    } catch (IllegalArgumentException e) {
      JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage() != null ? e.getMessage() : "Solicitud invalida");
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

  private Property readPayload(HttpServletRequest req) throws IOException, ServletException {
    if (isMultipartRequest(req)) {
      return readMultipartPayload(req);
    }

    Property payload = JsonUtil.readBody(req, Property.class);
    return payload != null ? payload : new Property();
  }

  private Property readMultipartPayload(HttpServletRequest req) throws IOException, ServletException {
    Part propertyPart = req.getPart("property");
    if (propertyPart == null) {
      throw new IllegalArgumentException("Falta el payload de la propiedad");
    }

    String json = new String(propertyPart.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    Property payload = JsonUtil.gson().fromJson(json, Property.class);
    if (payload == null) {
      payload = new Property();
    }

    payload.images = resolveImages(req, payload.images);
    return payload;
  }

  private List<PropertyImage> resolveImages(HttpServletRequest req, List<PropertyImage> requestedImages)
    throws IOException, ServletException {
    if (requestedImages == null || requestedImages.isEmpty()) {
      return new ArrayList<>();
    }

    List<PropertyImage> resolved = new ArrayList<>();
    for (PropertyImage image : requestedImages) {
      if (image == null) {
        continue;
      }

      Part filePart = findImagePart(req, image.uploadToken);
      if (filePart != null && filePart.getSize() > 0) {
        PropertyImage uploadedImage = CloudinaryImageService.uploadPropertyImage(filePart, image);
        uploadedImage.order = image.order;
        uploadedImage.isPrimary = image.isPrimary;
        resolved.add(uploadedImage);
        continue;
      }

      if (hasStoredImageReference(image)) {
        image.uploadToken = null;
        resolved.add(image);
      }
    }

    return resolved;
  }

  private Part findImagePart(HttpServletRequest req, String uploadToken) throws IOException, ServletException {
    if (uploadToken == null || uploadToken.isBlank()) {
      return null;
    }
    return req.getPart("imageFile_" + uploadToken);
  }

  private boolean hasStoredImageReference(PropertyImage image) {
    return hasText(image.url) || hasText(image.largeUrl) || hasText(image.mediumUrl) || hasText(image.thumbnailUrl);
  }

  private boolean isMultipartRequest(HttpServletRequest req) {
    String contentType = req.getContentType();
    return contentType != null && contentType.toLowerCase().startsWith("multipart/");
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
