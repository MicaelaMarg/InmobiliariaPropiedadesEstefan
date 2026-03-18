-- Índices recomendados para acelerar filtros comunes
ALTER TABLE properties
  ADD UNIQUE KEY uq_properties_slug (slug),
  ADD INDEX idx_pub_op_type_price (is_published, operation, type, price),
  ADD INDEX idx_pub_featured (is_published, is_featured, status),
  ADD INDEX idx_city (city),
  ADD INDEX idx_location (location);

-- Opcional: fulltext para búsqueda libre
-- CREATE FULLTEXT INDEX ft_properties_search ON properties (title, description, location, city);
