package com.sonicle.webtop.tasks.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Carry task folder data during update operation.
 **/
@ApiModel(description = "Carry task folder data during update operation.")
@JsonTypeName("DavFolderUpdate")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-14T18:05:51.753+02:00[Europe/Berlin]")
public class ApiDavFolderUpdate   {
  private @Valid String name;
  private @Valid String description;
  private @Valid String color;
  private @Valid List<String> updatedFields = null;

  /**
   * Category&#39;s name
   **/
  public ApiDavFolderUpdate name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Category's name")
  @JsonProperty("name")
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
  public ApiDavFolderUpdate description(String description) {
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
  public ApiDavFolderUpdate color(String color) {
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

  /**
   * List of field names (above) updated by the operation
   **/
  public ApiDavFolderUpdate updatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
    return this;
  }

  
  @ApiModelProperty(value = "List of field names (above) updated by the operation")
  @JsonProperty("updatedFields")
  public List<String> getUpdatedFields() {
    return updatedFields;
  }

  @JsonProperty("updatedFields")
  public void setUpdatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
  }

  public ApiDavFolderUpdate addUpdatedFieldsItem(String updatedFieldsItem) {
    if (this.updatedFields == null) {
      this.updatedFields = new ArrayList<>();
    }

    this.updatedFields.add(updatedFieldsItem);
    return this;
  }

  public ApiDavFolderUpdate removeUpdatedFieldsItem(String updatedFieldsItem) {
    if (updatedFieldsItem != null && this.updatedFields != null) {
      this.updatedFields.remove(updatedFieldsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavFolderUpdate davFolderUpdate = (ApiDavFolderUpdate) o;
    return Objects.equals(this.name, davFolderUpdate.name) &&
        Objects.equals(this.description, davFolderUpdate.description) &&
        Objects.equals(this.color, davFolderUpdate.color) &&
        Objects.equals(this.updatedFields, davFolderUpdate.updatedFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, color, updatedFields);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavFolderUpdate {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    updatedFields: ").append(toIndentedString(updatedFields)).append("\n");
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

