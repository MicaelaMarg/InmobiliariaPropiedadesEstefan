package com.inmobiliaria.api.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.inmobiliaria.api.model.PropertyImage;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CloudinaryImageService {
  private static final String CLOUDINARY_URL_ENV = "CLOUDINARY_URL";
  private static final String PROPERTY_UPLOAD_FOLDER = "inmobiliaria/properties";
  private static final int THUMBNAIL_WIDTH = 320;
  private static final int MEDIUM_WIDTH = 960;
  private static final int LARGE_WIDTH = 1440;
  private static final int PLACEHOLDER_WIDTH = 24;

  private static volatile Cloudinary cloudinary;
  private static volatile CloudinaryCredentials credentials;

  private CloudinaryImageService() {
  }

  public static PropertyImage uploadPropertyImage(Part filePart, PropertyImage metadata) throws IOException {
    if (filePart == null || filePart.getSize() <= 0) {
      throw new IOException("No se recibió ningún archivo de imagen");
    }

    String contentType = filePart.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IOException("El archivo subido no es una imagen válida");
    }

    String originalName = firstNotBlank(
      metadata != null ? metadata.originalName : null,
      filePart.getSubmittedFileName()
    );

    try {
      byte[] bytes = filePart.getInputStream().readAllBytes();
      Map<?, ?> uploadResult = getCloudinary().uploader().upload(
        bytes,
        ObjectUtils.asMap(
          "resource_type", "image",
          "folder", PROPERTY_UPLOAD_FOLDER,
          "use_filename", true,
          "unique_filename", true,
          "overwrite", false
        )
      );

      return buildPropertyImage(uploadResult, metadata, contentType, originalName);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("Error al subir imagen a Cloudinary: " + resolveErrorMessage(e), e);
    }
  }

  private static PropertyImage buildPropertyImage(
    Map<?, ?> uploadResult,
    PropertyImage metadata,
    String contentType,
    String originalName
  ) {
    String secureUrl = stringValue(uploadResult.get("secure_url"));
    if (secureUrl == null || secureUrl.isBlank()) {
      throw new IllegalStateException("Cloudinary no devolvió una URL segura para la imagen");
    }

    Integer originalWidth = integerValue(uploadResult.get("width"));
    Integer originalHeight = integerValue(uploadResult.get("height"));

    PropertyImage image = new PropertyImage();
    image.publicId = stringValue(uploadResult.get("public_id"));
    image.url = secureUrl;
    image.thumbnailUrl = applyCloudinaryTransformation(secureUrl, THUMBNAIL_WIDTH);
    image.mediumUrl = applyCloudinaryTransformation(secureUrl, MEDIUM_WIDTH);
    image.largeUrl = applyCloudinaryTransformation(secureUrl, LARGE_WIDTH);
    image.placeholderUrl = applyCloudinaryTransformation(secureUrl, PLACEHOLDER_WIDTH);
    image.width = originalWidth;
    image.height = originalHeight;
    image.thumbnailWidth = clampWidth(originalWidth, THUMBNAIL_WIDTH);
    image.mediumWidth = clampWidth(originalWidth, MEDIUM_WIDTH);
    image.largeWidth = clampWidth(originalWidth, LARGE_WIDTH);
    image.mimeType = firstNotBlank(contentType, stringValue(uploadResult.get("format")));
    image.originalName = originalName;
    image.order = metadata != null ? metadata.order : null;
    image.isPrimary = metadata != null ? metadata.isPrimary : null;
    return image;
  }

  public static Map<String, Object> createSignedUploadRequest() {
    CloudinaryCredentials currentCredentials = getCredentials();
    long timestamp = Instant.now().getEpochSecond();
    Map<String, String> signatureParams = new LinkedHashMap<>();
    signatureParams.put("folder", PROPERTY_UPLOAD_FOLDER);
    signatureParams.put("overwrite", "false");
    signatureParams.put("timestamp", String.valueOf(timestamp));
    signatureParams.put("unique_filename", "true");
    signatureParams.put("use_filename", "true");

    return Map.of(
      "cloudName", currentCredentials.cloudName(),
      "apiKey", currentCredentials.apiKey(),
      "folder", PROPERTY_UPLOAD_FOLDER,
      "overwrite", "false",
      "uniqueFilename", "true",
      "useFilename", "true",
      "timestamp", timestamp,
      "signature", sign(signatureParams, currentCredentials.apiSecret()),
      "uploadUrl", "https://api.cloudinary.com/v1_1/" + currentCredentials.cloudName() + "/image/upload"
    );
  }

  private static Cloudinary getCloudinary() {
    if (cloudinary != null) {
      return cloudinary;
    }

    synchronized (CloudinaryImageService.class) {
      if (cloudinary != null) {
        return cloudinary;
      }

      cloudinary = new Cloudinary(getCredentials().cloudinaryUrl());
      return cloudinary;
    }
  }

  private static CloudinaryCredentials getCredentials() {
    if (credentials != null) {
      return credentials;
    }

    synchronized (CloudinaryImageService.class) {
      if (credentials != null) {
        return credentials;
      }

      String cloudinaryUrl = System.getenv(CLOUDINARY_URL_ENV);
      if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
        throw new IllegalStateException("CLOUDINARY_URL no esta configurado");
      }

      credentials = parseCredentials(cloudinaryUrl);
      return credentials;
    }
  }

  private static CloudinaryCredentials parseCredentials(String cloudinaryUrl) {
    try {
      URI uri = new URI(cloudinaryUrl);
      String userInfo = uri.getUserInfo();
      if (userInfo == null || !userInfo.contains(":")) {
        throw new IllegalStateException("CLOUDINARY_URL no tiene api_key y api_secret válidas");
      }

      String[] parts = userInfo.split(":", 2);
      String apiKey = parts[0];
      String apiSecret = parts[1];
      String cloudName = uri.getHost();

      if (isBlank(apiKey) || isBlank(apiSecret) || isBlank(cloudName)) {
        throw new IllegalStateException("CLOUDINARY_URL está incompleta");
      }

      return new CloudinaryCredentials(cloudinaryUrl, cloudName, apiKey, apiSecret);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("CLOUDINARY_URL no tiene un formato válido", e);
    }
  }

  private static String applyCloudinaryTransformation(String secureUrl, int width) {
    int uploadIndex = secureUrl.indexOf("/upload/");
    if (uploadIndex < 0) {
      return secureUrl;
    }

    String transformation = width <= PLACEHOLDER_WIDTH
      ? "f_auto,q_auto,c_limit,w_" + width
      : "f_auto,q_auto,c_limit,w_" + width;

    return secureUrl.substring(0, uploadIndex + "/upload".length())
      + "/" + transformation
      + secureUrl.substring(uploadIndex + "/upload".length());
  }

  private static Integer clampWidth(Integer originalWidth, int targetWidth) {
    if (originalWidth == null) {
      return targetWidth;
    }
    return Math.min(originalWidth, targetWidth);
  }

  private static Integer integerValue(Object value) {
    if (value instanceof Number number) {
      return number.intValue();
    }
    if (value == null) {
      return null;
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static String stringValue(Object value) {
    return value != null ? String.valueOf(value) : null;
  }

  private static String firstNotBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private static String sign(Map<String, String> params, String apiSecret) {
    List<Map.Entry<String, String>> entries = new ArrayList<>(params.entrySet());
    entries.sort(Comparator.comparing(Map.Entry::getKey));

    StringBuilder toSign = new StringBuilder();
    for (Map.Entry<String, String> entry : entries) {
      if (isBlank(entry.getValue())) {
        continue;
      }
      if (toSign.length() > 0) {
        toSign.append('&');
      }
      toSign.append(entry.getKey()).append('=').append(entry.getValue());
    }
    toSign.append(apiSecret);

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] hash = digest.digest(toSign.toString().getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        hex.append(String.format("%02x", value));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("No se pudo firmar la subida a Cloudinary", e);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String resolveErrorMessage(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && !message.isBlank()) {
        return message;
      }
      current = current.getCause();
    }
    return throwable != null ? throwable.getClass().getSimpleName() : "Error desconocido";
  }

  private record CloudinaryCredentials(String cloudinaryUrl, String cloudName, String apiKey, String apiSecret) {
  }
}
