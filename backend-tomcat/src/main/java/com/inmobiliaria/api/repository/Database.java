package com.inmobiliaria.api.repository;

import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.model.PropertyImage;
import com.inmobiliaria.api.util.SlugUtils;
import jakarta.servlet.ServletContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.Instant;
import java.util.List;

public final class Database {

  private static volatile String jdbcUrl;
  private static volatile String dbUser;
  private static volatile String dbPassword;

  private Database(){}

  public static synchronized void init(ServletContext context) throws SQLException {

    if(jdbcUrl != null){
      return;
    }

    try{
      Class.forName("com.mysql.cj.jdbc.Driver");
    }catch(ClassNotFoundException e){
      throw new IllegalStateException("No se pudo cargar el driver MySQL", e);
    }

    String mysqlUrl = firstNonBlank(
      System.getenv("MYSQL_URL"),
      System.getenv("MYSQL_URL_INTERNAL")
    );

    if (mysqlUrl != null) {
      configureFromMysqlUrl(mysqlUrl);
    } else {
      String host = firstNonBlank(System.getenv("MYSQLHOST"), System.getenv("MYSQL_HOST"));
      String port = firstNonBlank(System.getenv("MYSQLPORT"), System.getenv("MYSQL_PORT"), "3306");
      String db = firstNonBlank(System.getenv("MYSQLDATABASE"), System.getenv("MYSQL_DATABASE"));
      String user = firstNonBlank(System.getenv("MYSQLUSER"), System.getenv("MYSQL_USER"));
      String password = firstNonBlank(System.getenv("MYSQLPASSWORD"), System.getenv("MYSQL_PASSWORD"));

      if(host == null || host.isBlank()){
        throw new IllegalStateException("MYSQLHOST/MYSQL_HOST no está configurado en Railway");
      }
      if(db == null || db.isBlank()){
        throw new IllegalStateException("MYSQLDATABASE/MYSQL_DATABASE no está configurado en Railway");
      }
      if(user == null || user.isBlank()){
        throw new IllegalStateException("MYSQLUSER/MYSQL_USER no está configurado en Railway");
      }
      if(password == null){
        throw new IllegalStateException("MYSQLPASSWORD/MYSQL_PASSWORD no está configurado en Railway");
      }

      jdbcUrl = buildJdbcUrl(host, port, db);
      dbUser = user;
      dbPassword = password;
    }

    System.out.println("[inmobiliaria] Conectado a MySQL: " + jdbcUrl);

    try(Connection connection = getConnection();
        Statement statement = connection.createStatement()){

      createTablesMysql(statement);
      migrateMysqlSchema(connection, statement);
    }

    try{
      seedInitialData();
    }catch(Exception e){
      System.err.println("[inmobiliaria] No se pudo cargar el dato inicial: " + e.getMessage());
    }
  }

  private static void createTablesMysql(Statement statement) throws SQLException{

    statement.execute("""
      create table if not exists properties (
        id bigint auto_increment primary key,
        slug varchar(255) not null unique,
        title varchar(255) not null,
        type varchar(80),
        category varchar(80),
        operation varchar(80),
        price double,
        currency varchar(16),
        location varchar(255),
        address varchar(255),
        city varchar(120),
        area varchar(120),
        total_area double,
        covered_area double,
        bedrooms int,
        bathrooms int,
        rooms int,
        state varchar(120),
        description text,
        features text,
        reference_code varchar(120),
        status varchar(80),
        is_published boolean,
        is_featured boolean,
        highlighted_messages text,
        payment_options text,
        services text,
        contact_phone varchar(120),
        contact_email varchar(255),
        observations text,
        youtube_url varchar(500),
        created_at varchar(40) not null,
        updated_at varchar(40) not null
      )
    """);

    statement.execute("""
      create table if not exists property_images (
        id bigint auto_increment primary key,
        property_id bigint not null,
        url mediumtext not null,
        display_order int not null,
        is_primary boolean not null,
        foreign key (property_id) references properties(id) on delete cascade
      )
    """);

    statement.execute("""
      create table if not exists admin_users (
        id bigint auto_increment primary key,
        email varchar(255) not null unique,
        password_hash varchar(255) not null,
        created_at varchar(40) not null
      )
    """);

    statement.execute("""
      create table if not exists password_reset_tokens (
        token varchar(255) primary key,
        admin_email varchar(255) not null,
        expires_at varchar(40) not null,
        used boolean not null default false
      )
    """);

    statement.execute("""
      create table if not exists login_attempts (
        identifier varchar(512) primary key,
        attempt_count int not null default 0,
        locked_until varchar(40)
      )
    """);
  }

  public static Connection getConnection() throws SQLException{

    if(jdbcUrl == null){
      throw new IllegalStateException("La base no fue inicializada");
    }

    return DriverManager.getConnection(jdbcUrl,dbUser,dbPassword);
  }

  private static void seedInitialData() throws SQLException{

    try(Connection connection = getConnection();
        PreparedStatement countStmt =
          connection.prepareStatement("select count(*) from properties");
        ResultSet rs = countStmt.executeQuery()){

      rs.next();

      if(rs.getInt(1) > 0){
        return;
      }
    }

    Property sample = new Property();

    sample.title = "Casa modelo cargada desde Tomcat";
    sample.slug = SlugUtils.slugify(sample.title);
    sample.type = "casa";
    sample.category = "inmueble";
    sample.operation = "venta";
    sample.price = 185000d;
    sample.currency = "USD";
    sample.location = "Centro";
    sample.address = "Av. Principal 123";
    sample.city = "San Isidro";
    sample.area = "Centro";
    sample.totalArea = 180d;
    sample.coveredArea = 120d;
    sample.bedrooms = 3;
    sample.bathrooms = 2;
    sample.rooms = 4;
    sample.state = "Excelente";
    sample.description = "Propiedad inicial para validar la integracion entre Vue y Tomcat.";
    sample.features = List.of("Jardin","Parrilla","Cochera");
    sample.referenceCode = "TOMCAT-001";
    sample.status = "available";
    sample.isPublished = true;
    sample.isFeatured = true;
    sample.contactPhone = "+54 11 1234-5678";
    sample.contactEmail = "admin@inmobiliaria.com";
    sample.observations = "";
    sample.images = List.of(primaryImage(
      "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800"
    ));

    sample.createdAt = Instant.now().toString();
    sample.updatedAt = sample.createdAt;

    new PropertyRepository().create(sample);
  }

  private static PropertyImage primaryImage(String url){

    PropertyImage image = new PropertyImage();
    image.url = url;
    image.order = 0;
    image.isPrimary = true;

    return image;
  }

  private static void migrateMysqlSchema(Connection connection, Statement statement) throws SQLException{
    // Asegura compatibilidad con instalaciones previas donde url podía haber quedado en VARCHAR/TEXT.
    // El admin actual guarda imágenes como data URLs, que superan fácilmente 255 caracteres.
    statement.execute("alter table property_images modify column url mediumtext not null");
    ensureIndexExists(
      connection,
      "properties",
      "idx_properties_public_listing",
      "create index idx_properties_public_listing on properties (is_published, status, is_featured, id)"
    );
    ensureIndexExists(
      connection,
      "properties",
      "idx_properties_operation_type",
      "create index idx_properties_operation_type on properties (operation, type)"
    );
    ensureIndexExists(
      connection,
      "property_images",
      "idx_property_images_property_order",
      "create index idx_property_images_property_order on property_images (property_id, is_primary, display_order)"
    );
    ensureColumnExists(
      connection,
      "properties",
      "youtube_url",
      "alter table properties add column youtube_url varchar(500)"
    );
    ensureColumnExists(
      connection,
      "properties",
      "highlighted_messages",
      "alter table properties add column highlighted_messages text"
    );
    ensureColumnExists(
      connection,
      "properties",
      "payment_options",
      "alter table properties add column payment_options text"
    );
    ensureColumnExists(
      connection,
      "properties",
      "services",
      "alter table properties add column services text"
    );
  }

  private static void ensureIndexExists(Connection connection, String tableName, String indexName, String createSql)
    throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("""
      select count(*)
        from information_schema.statistics
       where table_schema = database()
         and table_name = ?
         and index_name = ?
      """)) {
      statement.setString(1, tableName);
      statement.setString(2, indexName);
      try (ResultSet rs = statement.executeQuery()) {
        rs.next();
        if (rs.getInt(1) > 0) {
          return;
        }
      }
    }

    try (Statement statement = connection.createStatement()) {
      statement.execute(createSql);
    }
  }

  private static void ensureColumnExists(Connection connection, String tableName, String columnName, String alterSql)
    throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("""
      select count(*)
        from information_schema.columns
       where table_schema = database()
         and table_name = ?
         and column_name = ?
      """)) {
      statement.setString(1, tableName);
      statement.setString(2, columnName);

      try (ResultSet rs = statement.executeQuery()) {
        rs.next();
        if (rs.getInt(1) == 0) {
          try (Statement alter = connection.createStatement()) {
            alter.execute(alterSql);
          }
        }
      }
    }
  }

  private static void configureFromMysqlUrl(String mysqlUrl) {
    try {
      URI uri = new URI(mysqlUrl);
      String scheme = uri.getScheme();
      if (scheme == null || !scheme.startsWith("mysql")) {
        throw new IllegalArgumentException("MYSQL_URL debe usar esquema mysql://");
      }

      String host = uri.getHost();
      int port = uri.getPort() > 0 ? uri.getPort() : 3306;
      String path = uri.getPath();
      String db = path != null ? path.replaceFirst("^/+", "") : "";
      String userInfo = uri.getUserInfo();

      if (host == null || host.isBlank() || db.isBlank() || userInfo == null || userInfo.isBlank()) {
        throw new IllegalArgumentException("MYSQL_URL no tiene host, base o credenciales válidas");
      }

      String[] credentials = userInfo.split(":", 2);
      if (credentials.length != 2 || credentials[0].isBlank()) {
        throw new IllegalArgumentException("MYSQL_URL no tiene usuario/contraseña válidos");
      }

      jdbcUrl = buildJdbcUrl(host, Integer.toString(port), db);
      dbUser = credentials[0];
      dbPassword = credentials[1];
    } catch (URISyntaxException e) {
      throw new IllegalStateException("MYSQL_URL no tiene un formato válido", e);
    }
  }

  private static String buildJdbcUrl(String host, String port, String db) {
    return "jdbc:mysql://" + host + ":" + port + "/" + db
      + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
