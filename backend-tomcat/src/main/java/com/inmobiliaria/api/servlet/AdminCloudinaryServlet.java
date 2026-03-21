package com.inmobiliaria.api.servlet;

import com.inmobiliaria.api.util.CloudinaryImageService;
import com.inmobiliaria.api.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/admin/cloudinary/signature")
public class AdminCloudinaryServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, CloudinaryImageService.createSignedUploadRequest());
    } catch (Exception e) {
      e.printStackTrace(System.err);
      String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al preparar subida a Cloudinary: " + message);
    }
  }
}
