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
 * Defines useful data of a changed task object.
 **/
@ApiModel(description = "Defines useful data of a changed task object.")
@JsonTypeName("DavObjectChanged")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:40:25.131+01:00[Europe/Berlin]")
public class ApiDavObjectChanged   {
  private @Valid String id;
  private @Valid String href;
  private @Valid String etag;

  /**
   * Task instance ID
   **/
  public ApiDavObjectChanged id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Task instance ID")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Reference URI where this object is filed
   **/
  public ApiDavObjectChanged href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Reference URI where this object is filed")
  @JsonProperty("href")
  @NotNull
  public String getHref() {
    return href;
  }

  @JsonProperty("href")
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Revision tag of the object
   **/
  public ApiDavObjectChanged etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Revision tag of the object")
  @JsonProperty("etag")
  @NotNull
  public String getEtag() {
    return etag;
  }

  @JsonProperty("etag")
  public void setEtag(String etag) {
    this.etag = etag;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavObjectChanged davObjectChanged = (ApiDavObjectChanged) o;
    return Objects.equals(this.id, davObjectChanged.id) &&
        Objects.equals(this.href, davObjectChanged.href) &&
        Objects.equals(this.etag, davObjectChanged.etag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, href, etag);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavObjectChanged {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    href: ").append(toIndentedString(href)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
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

