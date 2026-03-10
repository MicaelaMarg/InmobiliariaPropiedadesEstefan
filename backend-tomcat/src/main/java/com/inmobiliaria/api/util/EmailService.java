package com.inmobiliaria.api.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Envío de correo vía Gmail SMTP.
 * Configurar: SMTP_USER (email), SMTP_APP_PASSWORD (contraseña de aplicación de Gmail), FRONTEND_URL (ej. http://localhost:5173).
 */
public final class EmailService {
  private static volatile String smtpUser;
  private static volatile String smtpPassword;
  private static volatile String frontendUrl;

  private EmailService() {
  }

  public static void init(String user, String password, String baseUrl) {
    smtpUser = user;
    smtpPassword = password;
    frontendUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl.replaceAll("/+$", "") : null;
  }

  public static boolean isConfigured() {
    return smtpUser != null && !smtpUser.isBlank() && smtpPassword != null && !smtpPassword.isBlank() && frontendUrl != null;
  }

  /** Envía el correo de recuperación de contraseña con el enlace que incluye el token. */
  public static void sendPasswordResetEmail(String toEmail, String resetToken) throws MessagingException {
    if (!isConfigured()) {
      throw new IllegalStateException("Email no configurado: SMTP_USER, SMTP_APP_PASSWORD y FRONTEND_URL deben estar definidos");
    }
    String link = frontendUrl + "/admin/reset-password?token=" + resetToken;
    String subject = "Recuperar contraseña - Inmobiliaria";
    String body = "Hola,\n\nSolicitaste restablecer tu contraseña de administrador.\n\n"
      + "Hacé clic en el siguiente enlace para elegir una nueva contraseña (válido 1 hora):\n\n"
      + link + "\n\nSi no solicitaste esto, ignorá este correo.\n";

    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
      @Override
      protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
        return new jakarta.mail.PasswordAuthentication(smtpUser, smtpPassword);
      }
    });

    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(smtpUser));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
    msg.setSubject(subject);
    msg.setText(body, "UTF-8");
    Transport.send(msg);
  }
}
