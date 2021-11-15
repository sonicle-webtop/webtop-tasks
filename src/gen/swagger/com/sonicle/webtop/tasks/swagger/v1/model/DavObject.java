package com.sonicle.webtop.tasks.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Represent a single task-object
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Represent a single task-object")

public class DavObject   {
  
  private @Valid String id = null;
  private @Valid String uid = null;
  private @Valid String href = null;
  private @Valid Long lastModified = null;
  private @Valid String etag = null;
  private @Valid Integer size = null;
  private @Valid String data = null;

public enum FormatEnum {

    ICALENDAR(String.valueOf("icalendar")), JSON(String.valueOf("json"));


    private String value;

    FormatEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static FormatEnum fromValue(String v) {
        for (FormatEnum b : FormatEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

  private @Valid FormatEnum format = null;

  /**
   * Task instance ID (internal)
   **/
  public DavObject id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "Task instance ID (internal)")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Task object ID (public)
   **/
  public DavObject uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(value = "Task object ID (public)")
  @JsonProperty("uid")
  public String getUid() {
    return uid;
  }
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * The URI where this object is filed
   **/
  public DavObject href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(value = "The URI where this object is filed")
  @JsonProperty("href")
  public String getHref() {
    return href;
  }
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Last modification time (unix timestamp)
   **/
  public DavObject lastModified(Long lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  
  @ApiModelProperty(value = "Last modification time (unix timestamp)")
  @JsonProperty("lastModified")
  public Long getLastModified() {
    return lastModified;
  }
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Revision tag of the object
   **/
  public DavObject etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(value = "Revision tag of the object")
  @JsonProperty("etag")
  public String getEtag() {
    return etag;
  }
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Size (in bytes) of data payload
   **/
  public DavObject size(Integer size) {
    this.size = size;
    return this;
  }

  
  @ApiModelProperty(value = "Size (in bytes) of data payload")
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Task data payload
   **/
  public DavObject data(String data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(value = "Task data payload")
  @JsonProperty("data")
  public String getData() {
    return data;
  }
  public void setData(String data) {
    this.data = data;
  }

  /**
   * Specifies the format of data payload
   **/
  public DavObject format(FormatEnum format) {
    this.format = format;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies the format of data payload")
  @JsonProperty("format")
  public FormatEnum getFormat() {
    return format;
  }
  public void setFormat(FormatEnum format) {
    this.format = format;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DavObject davObject = (DavObject) o;
    return Objects.equals(id, davObject.id) &&
        Objects.equals(uid, davObject.uid) &&
        Objects.equals(href, davObject.href) &&
        Objects.equals(lastModified, davObject.lastModified) &&
        Objects.equals(etag, davObject.etag) &&
        Objects.equals(size, davObject.size) &&
        Objects.equals(data, davObject.data) &&
        Objects.equals(format, davObject.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, href, lastModified, etag, size, data, format);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavObject {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    href: ").append(toIndentedString(href)).append("\n");
    sb.append("    lastModified: ").append(toIndentedString(lastModified)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
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

