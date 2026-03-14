package com.inmobiliaria.api.model;

import java.util.ArrayList;
import java.util.List;

public class Property {
  public String id;
  public String slug;
  public String title;
  public String type;
  public String category;
  public String operation;
  public Double price;
  public String currency;
  public String location;
  public String address;
  public String city;
  public String area;
  public Double totalArea;
  public Double coveredArea;
  public Integer bedrooms;
  public Integer bathrooms;
  public Integer rooms;
  public String state;
  public String description;
  public List<String> features = new ArrayList<>();
  public String referenceCode;
  public String status;
  public Boolean isPublished;
  public Boolean isFeatured;
  public List<String> highlightedMessages = new ArrayList<>();
  public List<String> paymentOptions = new ArrayList<>();
  public List<PropertyImage> images = new ArrayList<>();
  public String contactPhone;
  public String contactEmail;
  public String observations;
  public String youtubeUrl;
  public String createdAt;
  public String updatedAt;
}
