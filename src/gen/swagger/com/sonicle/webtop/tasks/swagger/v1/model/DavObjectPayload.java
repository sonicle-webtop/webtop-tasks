package com.sonicle.webtop.tasks.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Transports task-object data.
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Transports task-object data.")

public class DavObjectPayload   {
  
  private @Valid String href = null;
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
   * The URI where this object is filed (for updates is the same as path param)
   **/
  public DavObjectPayload href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The URI where this object is filed (for updates is the same as path param)")
  @JsonProperty("href")
  @NotNull
  public String getHref() {
    return href;
  }
  public void setHref(String href) {
    this.href = href;
  }

  /**
   * Task data payload
   **/
  public DavObjectPayload data(String data) {
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
  public DavObjectPayload format(FormatEnum format) {
    this.format = format;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies the format of data payload")
  @JsonProperty("format")
  @NotNull
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
    DavObjectPayload davObjectPayload = (DavObjectPayload) o;
    return Objects.equals(href, davObjectPayload.href) &&
        Objects.equals(data, davObjectPayload.data) &&
        Objects.equals(format, davObjectPayload.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(href, data, format);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavObjectPayload {\n");
    
    sb.append("    href: ").append(toIndentedString(href)).append("\n");
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

