package com.sonicle.webtop.tasks.swagger.v1.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DavFolderUpdate   {
  
  private @Valid String name = null;
  private @Valid String description = null;
  private @Valid String color = null;
  private @Valid List<String> updatedFields = new ArrayList<String>();

  /**
   * Category&#39;s name
   **/
  public DavFolderUpdate name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Category's name")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Category&#39;s description
   **/
  public DavFolderUpdate description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Category's description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Associated color in HEX format (like #FFFFFF)
   **/
  public DavFolderUpdate color(String color) {
    this.color = color;
    return this;
  }

  
  @ApiModelProperty(value = "Associated color in HEX format (like #FFFFFF)")
  @JsonProperty("color")
  public String getColor() {
    return color;
  }
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * List of field names (above) updated by the operation
   **/
  public DavFolderUpdate updatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
    return this;
  }

  
  @ApiModelProperty(value = "List of field names (above) updated by the operation")
  @JsonProperty("updatedFields")
  public List<String> getUpdatedFields() {
    return updatedFields;
  }
  public void setUpdatedFields(List<String> updatedFields) {
    this.updatedFields = updatedFields;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DavFolderUpdate davFolderUpdate = (DavFolderUpdate) o;
    return Objects.equals(name, davFolderUpdate.name) &&
        Objects.equals(description, davFolderUpdate.description) &&
        Objects.equals(color, davFolderUpdate.color) &&
        Objects.equals(updatedFields, davFolderUpdate.updatedFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, color, updatedFields);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavFolderUpdate {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

