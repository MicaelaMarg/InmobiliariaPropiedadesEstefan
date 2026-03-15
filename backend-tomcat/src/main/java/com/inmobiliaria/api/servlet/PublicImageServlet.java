package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.model.PropertyImageAsset;
import com.inmobiliaria.api.repository.PropertyRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/public/images/*")
public class PublicImageServlet extends HttpServlet {
  private final PropertyRepository repository = new PropertyRepository();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Long imageId = parseImageId(req.getPathInfo());
    if (imageId == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    try {
      PropertyImageAsset asset = repository.findPublicImageAsset(imageId, req.getParameter("variant"));
      if (asset == null || asset.bytes == null || asset.contentType == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType(asset.contentType);
      resp.setHeader("Cache-Control", "public, max-age=31536000, immutable");
      resp.setHeader("ETag", "\"" + asset.cacheKey + "\"");
      resp.setContentLength(asset.bytes.length);
      resp.getOutputStream().write(asset.bytes);
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private Long parseImageId(String pathInfo) {
    if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
      return null;
    }

    String normalized = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
    int slashIndex = normalized.indexOf('/');
    String rawId = slashIndex >= 0 ? normalized.substring(0, slashIndex) : normalized;

    try {
      return Long.parseLong(rawId);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
