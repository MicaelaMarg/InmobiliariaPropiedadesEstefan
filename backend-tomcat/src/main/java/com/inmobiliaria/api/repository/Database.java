package com.inmobiliaria.api.repository;

import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.model.PropertyImage;
import com.inmobiliaria.api.util.SlugUtils;
import jakarta.servlet.ServletContext;

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

    String host = System.getenv("MYSQLHOST");
    String port = System.getenv("MYSQLPORT");
    String db = System.getenv("MYSQLDATABASE");
    String user = System.getenv("MYSQLUSER");
    String password = System.getenv("MYSQLPASSWORD");

    if(db == null || db.isBlank()){
      throw new IllegalStateException("MYSQLDATABASE no está configurado en Railway");
    }

    try{
      Class.forName("com.mysql.cj.jdbc.Driver");
    }catch(ClassNotFoundException e){
      throw new IllegalStateException("No se pudo cargar el driver MySQL", e);
    }

    jdbcUrl =
      "jdbc:mysql://" + host + ":" + port + "/" + db +
      "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    dbUser = user;
    dbPassword = password;

    System.out.println("[inmobiliaria] Conectado a MySQL: " + jdbcUrl);

    try(Connection connection = getConnection();
        Statement statement = connection.createStatement()){

      createTablesMysql(statement);
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
        contact_phone varchar(120),
        contact_email varchar(255),
        observations text,
        created_at varchar(40) not null,
        updated_at varchar(40) not null
      )
    """);

    statement.execute("""
      create table if not exists property_images (
        id bigint auto_increment primary key,
        property_id bigint not null,
        url text not null,
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
}
