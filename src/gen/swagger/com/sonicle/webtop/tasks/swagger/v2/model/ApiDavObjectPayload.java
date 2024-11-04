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
 * Transports task object data.
 **/
@ApiModel(description = "Transports task object data.")
@JsonTypeName("DavObjectPayload")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:40:25.131+01:00[Europe/Berlin]")
public class ApiDavObjectPayload   {
  private @Valid String href;
  private @Valid String data;
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
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
	public static FormatEnum fromString(String s) {
        for (FormatEnum b : FormatEnum.values()) {
            // using Objects.toString() to be safe if value type non-object type
            // because types like 'int' etc. will be auto-boxed
            if (java.util.Objects.toString(b.value).equals(s)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected string value '" + s + "'");
	}
	
    @JsonCreator
    public static FormatEnum fromValue(String value) {
        for (FormatEnum b : FormatEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

  private @Valid FormatEnum format;

  /**
   * The URI where this object is filed (for updates is the same as path param)
   **/
  public ApiDavObjectPayload href(String href) {
    this.href = href;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The URI where this object is filed (for updates is the same as path param)")
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
   * Task data payload
   **/
  public ApiDavObjectPayload data(String data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(value = "Task data payload")
  @JsonProperty("data")
  public String getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(String data) {
    this.data = data;
  }

  /**
   * Specifies the format of data payload
   **/
  public ApiDavObjectPayload format(FormatEnum format) {
    this.format = format;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies the format of data payload")
  @JsonProperty("format")
  @NotNull
  public FormatEnum getFormat() {
    return format;
  }

  @JsonProperty("format")
  public void setFormat(FormatEnum format) {
    this.format = format;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavObjectPayload davObjectPayload = (ApiDavObjectPayload) o;
    return Objects.equals(this.href, davObjectPayload.href) &&
        Objects.equals(this.data, davObjectPayload.data) &&
        Objects.equals(this.format, davObjectPayload.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(href, data, format);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavObjectPayload {\n");
    
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


}

