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
 * Carry task category’s fields.
 **/
@ApiModel(description = "Carry task category’s fields.")
@JsonTypeName("EasSyncFolder")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:40:25.131+01:00[Europe/Berlin]")
public class ApiEasSyncFolder   {
  private @Valid String id;
  private @Valid String displayName;
  private @Valid String etag;
  private @Valid Boolean deflt;
  private @Valid String foAcl;
  private @Valid String elAcl;
  private @Valid String ownerId;

  /**
   * Category ID (internal)
   **/
  public ApiEasSyncFolder id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category ID (internal)")
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
   * Display name
   **/
  public ApiEasSyncFolder displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Display name")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Revision tag
   **/
  public ApiEasSyncFolder etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Revision tag")
  @JsonProperty("etag")
  @NotNull
  public String getEtag() {
    return etag;
  }

  @JsonProperty("etag")
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Specifies if marked as predefined folder
   **/
  public ApiEasSyncFolder deflt(Boolean deflt) {
    this.deflt = deflt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if marked as predefined folder")
  @JsonProperty("deflt")
  @NotNull
  public Boolean getDeflt() {
    return deflt;
  }

  @JsonProperty("deflt")
  public void setDeflt(Boolean deflt) {
    this.deflt = deflt;
  }

  /**
   * ACL info for folder itself
   **/
  public ApiEasSyncFolder foAcl(String foAcl) {
    this.foAcl = foAcl;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder itself")
  @JsonProperty("foAcl")
  @NotNull
  public String getFoAcl() {
    return foAcl;
  }

  @JsonProperty("foAcl")
  public void setFoAcl(String foAcl) {
    this.foAcl = foAcl;
  }

  /**
   * ACL info for folder elements
   **/
  public ApiEasSyncFolder elAcl(String elAcl) {
    this.elAcl = elAcl;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder elements")
  @JsonProperty("elAcl")
  @NotNull
  public String getElAcl() {
    return elAcl;
  }

  @JsonProperty("elAcl")
  public void setElAcl(String elAcl) {
    this.elAcl = elAcl;
  }

  /**
   * The owner profile ID
   **/
  public ApiEasSyncFolder ownerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The owner profile ID")
  @JsonProperty("ownerId")
  @NotNull
  public String getOwnerId() {
    return ownerId;
  }

  @JsonProperty("ownerId")
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEasSyncFolder easSyncFolder = (ApiEasSyncFolder) o;
    return Objects.equals(this.id, easSyncFolder.id) &&
        Objects.equals(this.displayName, easSyncFolder.displayName) &&
        Objects.equals(this.etag, easSyncFolder.etag) &&
        Objects.equals(this.deflt, easSyncFolder.deflt) &&
        Objects.equals(this.foAcl, easSyncFolder.foAcl) &&
        Objects.equals(this.elAcl, easSyncFolder.elAcl) &&
        Objects.equals(this.ownerId, easSyncFolder.ownerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, displayName, etag, deflt, foAcl, elAcl, ownerId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEasSyncFolder {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
    sb.append("    deflt: ").append(toIndentedString(deflt)).append("\n");
    sb.append("    foAcl: ").append(toIndentedString(foAcl)).append("\n");
    sb.append("    elAcl: ").append(toIndentedString(elAcl)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
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

