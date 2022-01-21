package com.sonicle.webtop.tasks.swagger.v1.model;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DavFolderNew   {
  
  private @Valid String name = null;
  private @Valid String description = null;
  private @Valid String color = null;

  /**
   * Category&#39;s name
   **/
  public DavFolderNew name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category's name")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Category&#39;s description
   **/
  public DavFolderNew description(String description) {
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
  public DavFolderNew color(String color) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DavFolderNew davFolderNew = (DavFolderNew) o;
    return Objects.equals(name, davFolderNew.name) &&
        Objects.equals(description, davFolderNew.description) &&
        Objects.equals(color, davFolderNew.color);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, color);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavFolderNew {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

