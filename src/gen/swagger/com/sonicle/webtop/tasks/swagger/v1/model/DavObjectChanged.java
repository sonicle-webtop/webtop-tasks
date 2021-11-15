package com.sonicle.webtop.tasks.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Defines useful data of a changed task-object
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Defines useful data of a changed task-object")

public class DavObjectChanged   {
  
  private @Valid String id = null;
  private @Valid String href = null;
  private @Valid String etag = null;

  /**
   * Task instance ID
   **/
  public DavObjectChanged id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Task instance ID")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Reference URI where this object is filed
   **/
  public DavObjectChanged href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Reference URI where this object is filed")
  @JsonProperty("href")
  @NotNull
  public String getHref() {
    return href;
  }
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Revision tag of the object
   **/
  public DavObjectChanged etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Revision tag of the object")
  @JsonProperty("etag")
  @NotNull
  public String getEtag() {
    return etag;
  }
  public void setEtag(String etag) {
    this.etag = etag;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DavObjectChanged davObjectChanged = (DavObjectChanged) o;
    return Objects.equals(id, davObjectChanged.id) &&
        Objects.equals(href, davObjectChanged.href) &&
        Objects.equals(etag, davObjectChanged.etag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, href, etag);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavObjectChanged {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

