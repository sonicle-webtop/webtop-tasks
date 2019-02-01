package com.sonicle.webtop.tasks.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry category’s fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry category’s fields")

public class SyncFolder   {
  
  private @Valid Integer id = null;
  private @Valid String displayName = null;
  private @Valid String etag = null;
  private @Valid Boolean deflt = null;
  private @Valid String foAcl = null;
  private @Valid String elAcl = null;
  private @Valid String ownerId = null;

  /**
   * Category ID (internal)
   **/
  public SyncFolder id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category ID (internal)")
  @JsonProperty("id")
  @NotNull
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Display name
   **/
  public SyncFolder displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Display name")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Revision tag
   **/
  public SyncFolder etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Revision tag")
  @JsonProperty("etag")
  @NotNull
  public String getEtag() {
    return etag;
  }
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Specifies if marked as predefined folder
   **/
  public SyncFolder deflt(Boolean deflt) {
    this.deflt = deflt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Specifies if marked as predefined folder")
  @JsonProperty("deflt")
  @NotNull
  public Boolean isDeflt() {
    return deflt;
  }
  public void setDeflt(Boolean deflt) {
    this.deflt = deflt;
  }

  /**
   * ACL info for folder itself
   **/
  public SyncFolder foAcl(String foAcl) {
    this.foAcl = foAcl;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder itself")
  @JsonProperty("foAcl")
  @NotNull
  public String getFoAcl() {
    return foAcl;
  }
  public void setFoAcl(String foAcl) {
    this.foAcl = foAcl;
  }

  /**
   * ACL info for folder elements
   **/
  public SyncFolder elAcl(String elAcl) {
    this.elAcl = elAcl;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder elements")
  @JsonProperty("elAcl")
  @NotNull
  public String getElAcl() {
    return elAcl;
  }
  public void setElAcl(String elAcl) {
    this.elAcl = elAcl;
  }

  /**
   * The owner profile ID
   **/
  public SyncFolder ownerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The owner profile ID")
  @JsonProperty("ownerId")
  @NotNull
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SyncFolder syncFolder = (SyncFolder) o;
    return Objects.equals(id, syncFolder.id) &&
        Objects.equals(displayName, syncFolder.displayName) &&
        Objects.equals(etag, syncFolder.etag) &&
        Objects.equals(deflt, syncFolder.deflt) &&
        Objects.equals(foAcl, syncFolder.foAcl) &&
        Objects.equals(elAcl, syncFolder.elAcl) &&
        Objects.equals(ownerId, syncFolder.ownerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, displayName, etag, deflt, foAcl, elAcl, ownerId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SyncFolder {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

