package com.inmobiliaria.api.repository;

import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.model.PropertyImage;
import com.inmobiliaria.api.util.SlugUtils;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
  private static volatile boolean useMysql;

  private Database(){}

  public static boolean isUsingMysql(){
    return useMysql;
  }

  public static synchronized void init(ServletContext context) throws SQLException, IOException {

    if(jdbcUrl != null){
      return;
    }

    String mysqlDatabase = firstNonBlank(
      context.getInitParameter("inmobiliaria.mysql.database"),
      System.getenv("MYSQLDATABASE"),
      System.getenv("MYSQL_DATABASE")
    );

    if(mysqlDatabase != null && !mysqlDatabase.isBlank()){

      useMysql = true;

      try{
        Class.forName("com.mysql.cj.jdbc.Driver");
      }catch(ClassNotFoundException e){
        throw new IllegalStateException("No se pudo cargar el driver MySQL", e);
      }

      String host = firstNonBlank(
        System.getenv("MYSQLHOST"),
        System.getenv("MYSQL_HOST"),
        "localhost"
      );

      String port = firstNonBlank(
        System.getenv("MYSQLPORT"),
        System.getenv("MYSQL_PORT"),
        "3306"
      );

      String user = firstNonBlank(
        System.getenv("MYSQLUSER"),
        System.getenv("MYSQL_USER"),
        "root"
      );

      String password = firstNonBlank(
        System.getenv("MYSQLPASSWORD"),
        System.getenv("MYSQL_PASSWORD"),
        ""
      );

      jdbcUrl =
        "jdbc:mysql://" + host + ":" + port + "/" + mysqlDatabase +
        "?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true";

      dbUser = user;
      dbPassword = password;

      System.out.println("[inmobiliaria] Usando MySQL: " + jdbcUrl);

    }else{

      useMysql = false;

      try{
        Class.forName("org.h2.Driver");
      }catch(ClassNotFoundException e){
        throw new IllegalStateException("No se pudo cargar el driver H2", e);
      }

      String configuredPath = firstNonBlank(
        context.getInitParameter("inmobiliaria.db.path"),
        System.getProperty("inmobiliaria.db.path"),
        System.getenv("INMOBILIARIA_DB_PATH")
      );

      String basePath = configuredPath;

      if(basePath == null){

        String catalinaBase = System.getProperty("catalina.base");

        if(catalinaBase == null || catalinaBase.isBlank()){
          catalinaBase = System.getProperty("user.home");
        }

        basePath = Paths.get(catalinaBase,"data","inmobiliaria-db","inmobiliaria").toString();
      }

      Path dbFile = Paths.get(basePath).toAbsolutePath().normalize();
      Files.createDirectories(dbFile.getParent());

      jdbcUrl =
        "jdbc:h2:file:" +
        dbFile.toString().replace("\\","/") +
        ";AUTO_SERVER=TRUE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";

      dbUser = "sa";
      dbPassword = "";

      System.out.println("[inmobiliaria] Usando H2 local");
    }

    try(Connection connection = getConnection();
        Statement statement = connection.createStatement()){

      if(useMysql){
        createTablesMysql(statement);
      }else{
        createTablesH2(statement);
      }
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

  private static void createTablesH2(Statement statement) throws SQLException{

    statement.execute("""
      create table if not exists properties (
        id bigint generated by default as identity primary key,
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
        bedrooms integer,
        bathrooms integer,
        rooms integer,
        state varchar(120),
        description clob,
        features clob,
        reference_code varchar(120),
        status varchar(80),
        is_published boolean,
        is_featured boolean,
        contact_phone varchar(120),
        contact_email varchar(255),
        observations clob,
        created_at varchar(40) not null,
        updated_at varchar(40) not null
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

  private static String firstNonBlank(String...values){

    for(String value : values){

      if(value != null && !value.isBlank()){
        return value;
      }
    }

    return null;
  }
}
