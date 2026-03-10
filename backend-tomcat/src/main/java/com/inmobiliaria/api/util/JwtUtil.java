package com.inmobiliaria.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletContext;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

public final class JwtUtil {
  private static volatile String secret;

  private JwtUtil() {
  }

  /** Llamar desde el listener al arrancar (context o env JWT_SECRET). */
  public static void setSecret(String jwtSecret) {
    secret = jwtSecret != null && !jwtSecret.isBlank() ? jwtSecret : null;
  }

  public static void initFromContext(ServletContext context) {
    String s = firstNonBlank(
      context.getInitParameter("inmobiliaria.jwt.secret"),
      System.getProperty("inmobiliaria.jwt.secret"),
      System.getenv("JWT_SECRET")
    );
    if (s == null || s.isBlank()) {
      s = "inmobiliaria-dev-secret-cambiar-en-produccion";
    }
    setSecret(s);
  }

  public static String createToken(String email, long userId, long expirationMs) {
    SecretKey key = Keys.hmacShaKeyFor(getSecretBytes());
    return Jwts.builder()
      .subject(email)
      .claim("userId", userId)
      .claim("email", email)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expirationMs))
      .signWith(key)
      .compact();
  }

  /** Valida el token y devuelve los claims; lanza si es inválido o expirado. */
  public static Claims parseToken(String token) throws ExpiredJwtException, SignatureException {
    return Jwts.parser()
      .verifyWith(Keys.hmacShaKeyFor(getSecretBytes()))
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }

  public static boolean isValid(String token) {
    if (token == null || token.isBlank()) return false;
    try {
      parseToken(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static byte[] getSecretBytes() {
    String s = secret;
    if (s == null || s.isBlank()) {
      s = "inmobiliaria-dev-secret-cambiar-en-produccion-min-32";
    }
    byte[] b = s.getBytes(StandardCharsets.UTF_8);
    // HS256 requiere al menos 32 bytes
    if (b.length < 32) {
      byte[] padded = new byte[32];
      for (int i = 0; i < 32; i++) padded[i] = b[i % b.length];
      return padded;
    }
    return b.length > 64 ? java.util.Arrays.copyOf(b, 64) : b;
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) return value;
    }
    return null;
  }
}
