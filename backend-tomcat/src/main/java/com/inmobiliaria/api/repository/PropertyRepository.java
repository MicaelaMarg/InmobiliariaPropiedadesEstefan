package com.inmobiliaria.api.repository;

import com.google.gson.reflect.TypeToken;
import com.inmobiliaria.api.model.Property;
import com.inmobiliaria.api.model.PropertyImage;
import com.inmobiliaria.api.util.JsonUtil;
import com.inmobiliaria.api.util.SlugUtils;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyRepository {
  private static final Type FEATURES_TYPE = new TypeToken<List<String>>() { }.getType();
  private static final int DEFAULT_PUBLIC_PAGE_SIZE = 12;
  private static final int MAX_PUBLIC_PAGE_SIZE = 48;
  private static final String BASE_SELECT = """
    select id, slug, title, type, category, operation, price, currency, location, address, city, area,
           total_area, covered_area, bedrooms, bathrooms, rooms, state, description, features,
           reference_code, status, is_published, is_featured, contact_phone, contact_email,
           observations, created_at, updated_at
    from properties
    """;

  public List<Property> findPublic(String operation, String type, Double minPrice, Double maxPrice, String location, Boolean featured)
    throws SQLException {
    return findPublicPage(operation, type, minPrice, maxPrice, location, featured, 1, DEFAULT_PUBLIC_PAGE_SIZE).items;
  }

  public PublicPropertyPage findPublicPage(
    String operation,
    String type,
    Double minPrice,
    Double maxPrice,
    String location,
    Boolean featured,
    Integer page,
    Integer limit
  ) throws SQLException {
    StringBuilder sql = new StringBuilder(BASE_SELECT)
      .append(" where is_published = true and status = 'available'");
    StringBuilder countSql = new StringBuilder("select count(*) from properties where is_published = true and status = 'available'");
    List<Object> params = new ArrayList<>();

    if (notBlank(operation)) {
      sql.append(" and operation = ?");
      countSql.append(" and operation = ?");
      params.add(operation);
    }
    if (notBlank(type)) {
      sql.append(" and type = ?");
      countSql.append(" and type = ?");
      params.add(type);
    }
    if (minPrice != null) {
      sql.append(" and price >= ?");
      countSql.append(" and price >= ?");
      params.add(minPrice);
    }
    if (maxPrice != null) {
      sql.append(" and price <= ?");
      countSql.append(" and price <= ?");
      params.add(maxPrice);
    }
    if (notBlank(location)) {
      sql.append(" and lower(concat(coalesce(location,''),' ',coalesce(city,''))) like ?");
      countSql.append(" and lower(concat(coalesce(location,''),' ',coalesce(city,''))) like ?");
      params.add("%" + location.trim().toLowerCase() + "%");
    }
    if (Boolean.TRUE.equals(featured)) {
      sql.append(" and is_featured = true");
      countSql.append(" and is_featured = true");
    }

    int normalizedLimit = normalizeLimit(limit);
    int normalizedPage = Math.max(page != null ? page : 1, 1);
    long total = count(countSql.toString(), params);
    int totalPages = Math.max((int) Math.ceil(total / (double) normalizedLimit), 1);
    if (normalizedPage > totalPages) {
      normalizedPage = totalPages;
    }
    int offset = (normalizedPage - 1) * normalizedLimit;

    sql.append(" order by is_featured desc, id desc");
    sql.append(" limit ? offset ?");

    List<Object> pagedParams = new ArrayList<>(params);
    pagedParams.add(normalizedLimit);
    pagedParams.add(offset);

    PublicPropertyPage result = new PublicPropertyPage();
    result.items = query(sql.toString(), pagedParams, false);
    result.total = total;
    result.page = normalizedPage;
    result.limit = normalizedLimit;
    result.totalPages = totalPages;
    return result;
  }

  public List<Property> findAdmin(String search, String status, Boolean isPublished) throws SQLException {
    StringBuilder sql = new StringBuilder(BASE_SELECT).append(" where 1 = 1");
    List<Object> params = new ArrayList<>();

    if (notBlank(search)) {
      sql.append("""
         and (
           lower(coalesce(title, '')) like ?
           or lower(coalesce(reference_code, '')) like ?
           or lower(coalesce(location, '')) like ?
         )
        """);
      String term = "%" + search.trim().toLowerCase() + "%";
      params.add(term);
      params.add(term);
      params.add(term);
    }
    if (notBlank(status)) {
      sql.append(" and status = ?");
      params.add(status);
    }
    if (isPublished != null) {
      sql.append(" and is_published = ?");
      params.add(isPublished);
    }

    sql.append(" order by id desc");
    return query(sql.toString(), params, false);
  }

  public Property findById(long id) throws SQLException {
    List<Property> properties = query(BASE_SELECT + " where id = ?", List.of(id), true);
    return properties.isEmpty() ? null : properties.get(0);
  }

  public Property findBySlugPublic(String slug) throws SQLException {
    List<Property> properties = query(
      BASE_SELECT + " where slug = ? and is_published = true and status = 'available'",
      List.of(slug),
      true
    );
    return properties.isEmpty() ? null : properties.get(0);
  }

  public Property create(Property input) throws SQLException {
    Property property = normalizeForCreate(input);

    try (Connection connection = Database.getConnection();
         PreparedStatement statement = connection.prepareStatement("""
           insert into properties (
             slug, title, type, category, operation, price, currency, location, address, city, area,
             total_area, covered_area, bedrooms, bathrooms, rooms, state, description, features,
             reference_code, status, is_published, is_featured, contact_phone, contact_email,
             observations, created_at, updated_at
           ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           """, Statement.RETURN_GENERATED_KEYS)) {
      bindProperty(statement, property);
      statement.executeUpdate();

      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) {
          property.id = String.valueOf(keys.getLong(1));
        }
      }

      saveImages(connection, Long.parseLong(property.id), property.images);
      return findById(Long.parseLong(property.id));
    }
  }

  public Property update(long id, Property changes) throws SQLException {
    Property existing = findById(id);
    if (existing == null) {
      return null;
    }

    Property merged = merge(existing, changes);
    merged.id = String.valueOf(id);
    merged.slug = ensureUniqueSlug(merged.slug, id);
    merged.updatedAt = Instant.now().toString();

    try (Connection connection = Database.getConnection();
         PreparedStatement statement = connection.prepareStatement("""
           update properties
              set slug = ?, title = ?, type = ?, category = ?, operation = ?, price = ?, currency = ?,
                  location = ?, address = ?, city = ?, area = ?, total_area = ?, covered_area = ?,
                  bedrooms = ?, bathrooms = ?, rooms = ?, state = ?, description = ?, features = ?,
                  reference_code = ?, status = ?, is_published = ?, is_featured = ?, contact_phone = ?,
                  contact_email = ?, observations = ?, created_at = ?, updated_at = ?
            where id = ?
           """)) {
      bindProperty(statement, merged);
      statement.setLong(29, id);
      statement.executeUpdate();

      saveImages(connection, id, merged.images);
      return findById(id);
    }
  }

  public boolean delete(long id) throws SQLException {
    try (Connection connection = Database.getConnection();
         PreparedStatement statement = connection.prepareStatement("delete from properties where id = ?")) {
      statement.setLong(1, id);
      return statement.executeUpdate() > 0;
    }
  }

  private List<Property> query(String sql, List<Object> params, boolean includeAllImages) throws SQLException {
    try (Connection connection = Database.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      bindParams(statement, params);
      try (ResultSet rs = statement.executeQuery()) {
        List<Property> result = new ArrayList<>();
        while (rs.next()) {
          Property property = mapProperty(rs);
          result.add(property);
        }
        attachImages(connection, result, includeAllImages);
        return result;
      }
    }
  }

  private Property mapProperty(ResultSet rs) throws SQLException {
    Property property = new Property();
    property.id = rs.getString("id");
    property.slug = rs.getString("slug");
    property.title = rs.getString("title");
    property.type = rs.getString("type");
    property.category = rs.getString("category");
    property.operation = rs.getString("operation");
    property.price = getNullableDouble(rs, "price");
    property.currency = rs.getString("currency");
    property.location = rs.getString("location");
    property.address = rs.getString("address");
    property.city = rs.getString("city");
    property.area = rs.getString("area");
    property.totalArea = getNullableDouble(rs, "total_area");
    property.coveredArea = getNullableDouble(rs, "covered_area");
    property.bedrooms = getNullableInt(rs, "bedrooms");
    property.bathrooms = getNullableInt(rs, "bathrooms");
    property.rooms = getNullableInt(rs, "rooms");
    property.state = rs.getString("state");
    property.description = rs.getString("description");
    property.features = parseFeatures(rs.getString("features"));
    property.referenceCode = rs.getString("reference_code");
    property.status = rs.getString("status");
    property.isPublished = rs.getBoolean("is_published");
    property.isFeatured = rs.getBoolean("is_featured");
    property.contactPhone = rs.getString("contact_phone");
    property.contactEmail = rs.getString("contact_email");
    property.observations = rs.getString("observations");
    property.createdAt = rs.getString("created_at");
    property.updatedAt = rs.getString("updated_at");
    return property;
  }

  private List<PropertyImage> findImages(Connection connection, long propertyId) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement("""
      select url, display_order, is_primary
        from property_images
       where property_id = ?
       order by display_order asc, id asc
      """)) {
      statement.setLong(1, propertyId);
      try (ResultSet rs = statement.executeQuery()) {
        List<PropertyImage> images = new ArrayList<>();
        while (rs.next()) {
          PropertyImage image = new PropertyImage();
          image.url = rs.getString("url");
          image.order = getNullableInt(rs, "display_order");
          image.isPrimary = rs.getBoolean("is_primary");
          images.add(image);
        }
        return images;
      }
    }
  }

  private void attachImages(Connection connection, List<Property> properties, boolean includeAllImages) throws SQLException {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    if (includeAllImages) {
      for (Property property : properties) {
        property.images = findImages(connection, Long.parseLong(property.id));
      }
      return;
    }

    attachPrimaryImages(connection, properties);
  }

  private void attachPrimaryImages(Connection connection, List<Property> properties) throws SQLException {
    String placeholders = String.join(", ", Collections.nCopies(properties.size(), "?"));
    Map<Long, Property> propertiesById = new HashMap<>();
    for (Property property : properties) {
      property.images = new ArrayList<>();
      propertiesById.put(Long.parseLong(property.id), property);
    }

    String sql = """
      select property_id, url, display_order, is_primary
        from property_images
       where property_id in (%s)
       order by property_id asc, is_primary desc, display_order asc, id asc
      """.formatted(placeholders);

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      for (int i = 0; i < properties.size(); i++) {
        statement.setLong(i + 1, Long.parseLong(properties.get(i).id));
      }

      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          long propertyId = rs.getLong("property_id");
          Property property = propertiesById.get(propertyId);
          if (property == null || !property.images.isEmpty()) {
            continue;
          }

          PropertyImage image = new PropertyImage();
          image.url = rs.getString("url");
          image.order = getNullableInt(rs, "display_order");
          image.isPrimary = rs.getBoolean("is_primary");
          property.images.add(image);
        }
      }
    }
  }

  private void saveImages(Connection connection, long propertyId, List<PropertyImage> images) throws SQLException {
    try (PreparedStatement deleteStmt = connection.prepareStatement("delete from property_images where property_id = ?")) {
      deleteStmt.setLong(1, propertyId);
      deleteStmt.executeUpdate();
    }

    if (images == null || images.isEmpty()) {
      return;
    }

    try (PreparedStatement insertStmt = connection.prepareStatement("""
      insert into property_images (property_id, url, display_order, is_primary)
      values (?, ?, ?, ?)
      """)) {
      for (int i = 0; i < images.size(); i++) {
        PropertyImage image = images.get(i);
        if (image == null || !notBlank(image.url)) {
          continue;
        }
        insertStmt.setLong(1, propertyId);
        insertStmt.setString(2, image.url);
        insertStmt.setInt(3, image.order != null ? image.order : i);
        insertStmt.setBoolean(4, Boolean.TRUE.equals(image.isPrimary) || i == 0);
        insertStmt.addBatch();
      }
      insertStmt.executeBatch();
    }
  }

  private void bindProperty(PreparedStatement statement, Property property) throws SQLException {
    statement.setString(1, property.slug);
    statement.setString(2, property.title);
    statement.setString(3, property.type);
    statement.setString(4, property.category);
    statement.setString(5, property.operation);
    statement.setObject(6, property.price);
    statement.setString(7, property.currency);
    statement.setString(8, property.location);
    statement.setString(9, property.address);
    statement.setString(10, property.city);
    statement.setString(11, property.area);
    statement.setObject(12, property.totalArea);
    statement.setObject(13, property.coveredArea);
    statement.setObject(14, property.bedrooms);
    statement.setObject(15, property.bathrooms);
    statement.setObject(16, property.rooms);
    statement.setString(17, property.state);
    statement.setString(18, property.description);
    statement.setString(19, JsonUtil.gson().toJson(property.features));
    statement.setString(20, property.referenceCode);
    statement.setString(21, property.status);
    statement.setBoolean(22, Boolean.TRUE.equals(property.isPublished));
    statement.setBoolean(23, Boolean.TRUE.equals(property.isFeatured));
    statement.setString(24, property.contactPhone);
    statement.setString(25, property.contactEmail);
    statement.setString(26, property.observations);
    statement.setString(27, property.createdAt);
    statement.setString(28, property.updatedAt);
  }

  private void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      statement.setObject(i + 1, params.get(i));
    }
  }

  private long count(String sql, List<Object> params) throws SQLException {
    try (Connection connection = Database.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      bindParams(statement, params);
      try (ResultSet rs = statement.executeQuery()) {
        rs.next();
        return rs.getLong(1);
      }
    }
  }

  private Property normalizeForCreate(Property input) throws SQLException {
    Property property = input == null ? new Property() : input;
    property.title = defaultIfBlank(property.title, "Nueva propiedad");
    property.slug = ensureUniqueSlug(property.slug, null);
    property.category = defaultIfBlank(property.category, "inmueble");
    property.operation = defaultIfBlank(property.operation, "venta");
    property.currency = defaultIfBlank(property.currency, "USD");
    property.status = defaultIfBlank(property.status, "available");
    property.isPublished = property.isPublished != null ? property.isPublished : Boolean.TRUE;
    property.isFeatured = property.isFeatured != null ? property.isFeatured : Boolean.FALSE;
    property.features = property.features != null ? property.features : new ArrayList<>();
    property.images = property.images != null ? property.images : new ArrayList<>();

    String now = Instant.now().toString();
    property.createdAt = defaultIfBlank(property.createdAt, now);
    property.updatedAt = defaultIfBlank(property.updatedAt, now);
    return property;
  }

  private Property merge(Property existing, Property changes) throws SQLException {
    if (changes == null) {
      return existing;
    }

    Property merged = new Property();
    merged.id = existing.id;
    merged.title = pick(changes.title, existing.title);
    merged.slug = ensureUniqueSlug(pick(changes.slug, existing.slug, SlugUtils.slugify(merged.title)), Long.parseLong(existing.id));
    merged.type = pick(changes.type, existing.type);
    merged.category = pick(changes.category, existing.category);
    merged.operation = pick(changes.operation, existing.operation);
    merged.price = changes.price != null ? changes.price : existing.price;
    merged.currency = pick(changes.currency, existing.currency);
    merged.location = pick(changes.location, existing.location);
    merged.address = pick(changes.address, existing.address);
    merged.city = pick(changes.city, existing.city);
    merged.area = pick(changes.area, existing.area);
    merged.totalArea = changes.totalArea != null ? changes.totalArea : existing.totalArea;
    merged.coveredArea = changes.coveredArea != null ? changes.coveredArea : existing.coveredArea;
    merged.bedrooms = changes.bedrooms != null ? changes.bedrooms : existing.bedrooms;
    merged.bathrooms = changes.bathrooms != null ? changes.bathrooms : existing.bathrooms;
    merged.rooms = changes.rooms != null ? changes.rooms : existing.rooms;
    merged.state = pick(changes.state, existing.state);
    merged.description = pick(changes.description, existing.description);
    merged.features = changes.features != null ? changes.features : existing.features;
    merged.referenceCode = pick(changes.referenceCode, existing.referenceCode);
    merged.status = pick(changes.status, existing.status);
    merged.isPublished = changes.isPublished != null ? changes.isPublished : existing.isPublished;
    merged.isFeatured = changes.isFeatured != null ? changes.isFeatured : existing.isFeatured;
    merged.images = changes.images != null ? changes.images : existing.images;
    merged.contactPhone = pick(changes.contactPhone, existing.contactPhone);
    merged.contactEmail = pick(changes.contactEmail, existing.contactEmail);
    merged.observations = pick(changes.observations, existing.observations);
    merged.createdAt = existing.createdAt;
    merged.updatedAt = Instant.now().toString();
    return merged;
  }

  private String ensureUniqueSlug(String slugCandidate, Long excludedId) throws SQLException {
    String baseSlug = SlugUtils.slugify(slugCandidate);
    String slug = baseSlug;
    int suffix = 2;

    while (slugExists(slug, excludedId)) {
      slug = baseSlug + "-" + suffix++;
    }

    return slug;
  }

  private boolean slugExists(String slug, Long excludedId) throws SQLException {
    String sql = excludedId == null
      ? "select count(*) from properties where slug = ?"
      : "select count(*) from properties where slug = ? and id <> ?";

    try (Connection connection = Database.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, slug);
      if (excludedId != null) {
        statement.setLong(2, excludedId);
      }
      try (ResultSet rs = statement.executeQuery()) {
        rs.next();
        return rs.getInt(1) > 0;
      }
    }
  }

  private List<String> parseFeatures(String json) {
    if (!notBlank(json)) {
      return new ArrayList<>();
    }

    List<String> parsed = JsonUtil.gson().fromJson(json, FEATURES_TYPE);
    return parsed != null ? parsed : new ArrayList<>();
  }

  private static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
    double value = rs.getDouble(column);
    return rs.wasNull() ? null : value;
  }

  private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
    int value = rs.getInt(column);
    return rs.wasNull() ? null : value;
  }

  private static boolean notBlank(String value) {
    return value != null && !value.isBlank();
  }

  private static String defaultIfBlank(String value, String fallback) {
    return notBlank(value) ? value : fallback;
  }

  private static int normalizeLimit(Integer limit) {
    if (limit == null || limit <= 0) {
      return DEFAULT_PUBLIC_PAGE_SIZE;
    }
    return Math.min(limit, MAX_PUBLIC_PAGE_SIZE);
  }

  private static String pick(String... values) {
    for (String value : values) {
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  public static class PublicPropertyPage {
    public List<Property> items = new ArrayList<>();
    public long total;
    public int page;
    public int limit;
    public int totalPages;
  }
}
