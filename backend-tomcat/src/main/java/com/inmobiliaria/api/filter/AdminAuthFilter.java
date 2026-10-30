package com.inmobiliaria.api.filter;

import com.inmobiliaria.api.repository.AdminUserRepository;
import com.inmobiliaria.api.util.JsonUtil;
import com.inmobiliaria.api.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/admin/*")
public class AdminAuthFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
      chain.doFilter(request, response);
      return;
    }

    String authorization = httpRequest.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
      JsonUtil.writeError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Falta token de administrador");
      return;
    }
    String token = authorization.substring(7).trim();
    Claims claims;
    try {
      claims = JwtUtil.parseToken(token);
    } catch (Exception e) {
      JsonUtil.writeError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
      return;
    }

    // El token solo vale mientras el administrador siga existiendo (si se lo elimina, pierde el acceso enseguida).
    try {
      AdminUserRepository.AdminRow admin = new AdminUserRepository().findByEmail(claims.get("email", String.class));
      Number userId = claims.get("userId", Number.class);
      if (admin == null || userId == null || admin.id != userId.longValue()) {
        JsonUtil.writeError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
        return;
      }
    } catch (Exception e) {
      JsonUtil.writeError(httpResponse, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "No se pudo verificar la sesión");
      return;
    }

    chain.doFilter(request, response);
  }
}
