package com.sonicle.webtop.tasks.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Carry task folder data during add operation.
 **/
@ApiModel(description = "Carry task folder data during add operation.")
@JsonTypeName("DavFolderNew")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-21T12:01:13.376+02:00[Europe/Berlin]")
public class ApiDavFolderNew   {
  private @Valid String name;
  private @Valid String description;
  private @Valid String color;

  /**
   * Category&#39;s name
   **/
  public ApiDavFolderNew name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category's name")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Category&#39;s description
   **/
  public ApiDavFolderNew description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Category's description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Associated color in HEX format (like #FFFFFF)
   **/
  public ApiDavFolderNew color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(value = "Associated color in HEX format (like #FFFFFF)")
  @JsonProperty("color")
  public String getColor() {
    return color;
  }

  @JsonProperty("color")
  public void setColor(String color) {
    this.color = color;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavFolderNew davFolderNew = (ApiDavFolderNew) o;
    return Objects.equals(this.name, davFolderNew.name) &&
        Objects.equals(this.description, davFolderNew.description) &&
        Objects.equals(this.color, davFolderNew.color);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, color);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavFolderNew {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

