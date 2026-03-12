package com.inmobiliaria.api.listener;

import com.inmobiliaria.api.repository.Database;
import com.inmobiliaria.api.util.EmailService;
import com.inmobiliaria.api.util.JwtUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class BootstrapListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    jakarta.servlet.ServletContext context = sce.getServletContext();
    try {
      Database.init(context);
    } catch (Exception e) {
      System.err.println("[inmobiliaria] Error al inicializar la base de datos: " + e.getMessage());
      e.printStackTrace(System.err);
      throw new IllegalStateException("No se pudo inicializar la base de datos", e);
    }

    JwtUtil.initFromContext(context);
    initEmailService(context);
  }

  private static void initEmailService(jakarta.servlet.ServletContext context) {
    String user = firstNonBlank(
      context.getInitParameter("inmobiliaria.smtp.user"),
      System.getProperty("smtp.user"),
      System.getenv("SMTP_USER")
    );
    String password = firstNonBlank(
      context.getInitParameter("inmobiliaria.smtp.password"),
      System.getProperty("smtp.app.password"),
      System.getenv("SMTP_APP_PASSWORD")
    );
    String frontendUrl = firstNonBlank(
      context.getInitParameter("inmobiliaria.frontend.url"),
      System.getProperty("frontend.url"),
      System.getenv("FRONTEND_URL")
    );
    EmailService.init(user, password, frontendUrl);
  }

  private static String firstNonBlank(String... values) {
    for (String v : values) {
      if (v != null && !v.isBlank()) return v;
    }
    return null;
  }
}
