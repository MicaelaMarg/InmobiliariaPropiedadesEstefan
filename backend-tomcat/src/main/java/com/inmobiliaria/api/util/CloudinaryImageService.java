package com.inmobiliaria.api.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.inmobiliaria.api.model.PropertyImage;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Map;

public final class CloudinaryImageService {
  private static final String CLOUDINARY_URL_ENV = "CLOUDINARY_URL";
  private static final int THUMBNAIL_WIDTH = 320;
  private static final int MEDIUM_WIDTH = 960;
  private static final int LARGE_WIDTH = 1440;
  private static final int PLACEHOLDER_WIDTH = 24;

  private static volatile Cloudinary cloudinary;

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
          "folder", "inmobiliaria/properties",
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

  private static Cloudinary getCloudinary() {
    if (cloudinary != null) {
      return cloudinary;
    }

    synchronized (CloudinaryImageService.class) {
      if (cloudinary != null) {
        return cloudinary;
      }

      String cloudinaryUrl = System.getenv(CLOUDINARY_URL_ENV);
      if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
        throw new IllegalStateException("CLOUDINARY_URL no esta configurado");
      }

      cloudinary = new Cloudinary(cloudinaryUrl);
      return cloudinary;
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
}
