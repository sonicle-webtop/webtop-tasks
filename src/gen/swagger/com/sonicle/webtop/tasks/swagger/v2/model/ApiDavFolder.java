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
 * Represent a single folder that contains tasks.
 **/
@ApiModel(description = "Represent a single folder that contains tasks.")
@JsonTypeName("DavFolder")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-21T12:01:13.376+02:00[Europe/Berlin]")
public class ApiDavFolder   {
  private @Valid String id;
  private @Valid String uid;
  private @Valid String name;
  private @Valid String description;
  private @Valid String color;
  private @Valid String syncToken;
  private @Valid String aclFol;
  private @Valid String aclEle;
  private @Valid String ownerUsername;
  private @Valid String displayName;

  /**
   * Category ID (internal)
   **/
  public ApiDavFolder id(String id) {
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
   * Category UID (public)
   **/
  public ApiDavFolder uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category UID (public)")
  @JsonProperty("uid")
  @NotNull
  public String getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Category&#39;s name
   **/
  public ApiDavFolder name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category's name")
  @JsonProperty("name")
  @NotNull
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
  public ApiDavFolder description(String description) {
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
  public ApiDavFolder color(String color) {
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
   * Current sync-token
   **/
  public ApiDavFolder syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Current sync-token")
  @JsonProperty("syncToken")
  @NotNull
  public String getSyncToken() {
    return syncToken;
  }

  @JsonProperty("syncToken")
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

  /**
   * ACL info for folder itself
   **/
  public ApiDavFolder aclFol(String aclFol) {
    this.aclFol = aclFol;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder itself")
  @JsonProperty("aclFol")
  @NotNull
  public String getAclFol() {
    return aclFol;
  }

  @JsonProperty("aclFol")
  public void setAclFol(String aclFol) {
    this.aclFol = aclFol;
  }

  /**
   * ACL info for folder elements
   **/
  public ApiDavFolder aclEle(String aclEle) {
    this.aclEle = aclEle;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder elements")
  @JsonProperty("aclEle")
  @NotNull
  public String getAclEle() {
    return aclEle;
  }

  @JsonProperty("aclEle")
  public void setAclEle(String aclEle) {
    this.aclEle = aclEle;
  }

  /**
   * The owner profile&#39;s username
   **/
  public ApiDavFolder ownerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The owner profile's username")
  @JsonProperty("ownerUsername")
  @NotNull
  public String getOwnerUsername() {
    return ownerUsername;
  }

  @JsonProperty("ownerUsername")
  public void setOwnerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
  }

  /**
   * Suitable display name
   **/
  public ApiDavFolder displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Suitable display name")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavFolder davFolder = (ApiDavFolder) o;
    return Objects.equals(this.id, davFolder.id) &&
        Objects.equals(this.uid, davFolder.uid) &&
        Objects.equals(this.name, davFolder.name) &&
        Objects.equals(this.description, davFolder.description) &&
        Objects.equals(this.color, davFolder.color) &&
        Objects.equals(this.syncToken, davFolder.syncToken) &&
        Objects.equals(this.aclFol, davFolder.aclFol) &&
        Objects.equals(this.aclEle, davFolder.aclEle) &&
        Objects.equals(this.ownerUsername, davFolder.ownerUsername) &&
        Objects.equals(this.displayName, davFolder.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, name, description, color, syncToken, aclFol, aclEle, ownerUsername, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavFolder {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    syncToken: ").append(toIndentedString(syncToken)).append("\n");
    sb.append("    aclFol: ").append(toIndentedString(aclFol)).append("\n");
    sb.append("    aclEle: ").append(toIndentedString(aclEle)).append("\n");
    sb.append("    ownerUsername: ").append(toIndentedString(ownerUsername)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
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

