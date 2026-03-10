package com.inmobiliaria.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Añade cabeceras de seguridad: X-Frame-Options, X-Content-Type-Options,
 * X-XSS-Protection, Content-Security-Policy (relajada para APIs y formularios).
 * Strict-Transport-Security solo en HTTPS (se puede activar en producción).
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    httpResponse.setHeader("X-Frame-Options", "DENY");
    httpResponse.setHeader("X-Content-Type-Options", "nosniff");
    httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
    // CSP básica: permite same-origin y APIs; ajustar en producción si usás scripts externos
    httpResponse.setHeader("Content-Security-Policy",
      "default-src 'self'; script-src 'self' 'unsafe-inline' https://challenges.cloudflare.com; frame-src https://challenges.cloudflare.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' https://challenges.cloudflare.com;");

    chain.doFilter(request, response);
  }
}
