package com.inmobiliaria.api.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Genera un hash BCrypt para una contraseña. Sirve para actualizar o insertar
 * un admin desde MySQL/phpMyAdmin (la columna password_hash debe tener este hash).
 *
 * Uso desde la carpeta backend-tomcat:
 *   mvn -q exec:java -Dexec.mainClass="com.inmobiliaria.api.util.PasswordHashUtil" -Dexec.args="TuContraseña123!"
 * Imprime el hash en una línea; copialo y usalo en UPDATE o INSERT en admin_users.
 */
public final class PasswordHashUtil {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Uso: mvn -q exec:java -Dexec.mainClass=\"com.inmobiliaria.api.util.PasswordHashUtil\" -Dexec.args=\"TuContraseña\"");
      System.err.println("Genera el hash BCrypt para pegar en MySQL (columna password_hash de admin_users).");
      return;
    }
    String password = args[0];
    String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
    System.out.println(hash);
  }
}
