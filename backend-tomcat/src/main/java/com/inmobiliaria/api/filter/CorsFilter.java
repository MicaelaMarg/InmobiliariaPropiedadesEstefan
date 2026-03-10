package com.inmobiliaria.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String origin = httpRequest.getHeader("Origin");
    if (origin == null || origin.isBlank()) origin = "*";
    httpResponse.setHeader("Vary", "Origin");
    httpResponse.setHeader("Access-Control-Allow-Origin", origin);
    httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    httpResponse.setHeader("Access-Control-Max-Age", "86400");

    if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
      httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }

    chain.doFilter(request, response);
  }
}
