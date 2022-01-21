package com.sonicle.webtop.tasks.swagger.v1.model;

import javax.validation.constraints.*;
import javax.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;


public class DavFolder   {
  
  private @Valid Integer id = null;
  private @Valid String uid = null;
  private @Valid String name = null;
  private @Valid String description = null;
  private @Valid String color = null;
  private @Valid String syncToken = null;
  private @Valid String aclFol = null;
  private @Valid String aclEle = null;
  private @Valid String ownerUsername = null;
  private @Valid String displayName = null;

  /**
   * Category ID (internal)
   **/
  public DavFolder id(Integer id) {
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
   * Category UID (public)
   **/
  public DavFolder uid(String uid) {
    this.uid = uid;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Category UID (public)")
  @JsonProperty("uid")
  @NotNull
  public String getUid() {
    return uid;
  }
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   * Category&#39;s name
   **/
  public DavFolder name(String name) {
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
  public DavFolder description(String description) {
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
  public DavFolder color(String color) {
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
   * Current sync-token
   **/
  public DavFolder syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Current sync-token")
  @JsonProperty("syncToken")
  @NotNull
  public String getSyncToken() {
    return syncToken;
  }
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

  /**
   * ACL info for folder itself
   **/
  public DavFolder aclFol(String aclFol) {
    this.aclFol = aclFol;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder itself")
  @JsonProperty("aclFol")
  @NotNull
  public String getAclFol() {
    return aclFol;
  }
  public void setAclFol(String aclFol) {
    this.aclFol = aclFol;
  }

  /**
   * ACL info for folder elements
   **/
  public DavFolder aclEle(String aclEle) {
    this.aclEle = aclEle;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "ACL info for folder elements")
  @JsonProperty("aclEle")
  @NotNull
  public String getAclEle() {
    return aclEle;
  }
  public void setAclEle(String aclEle) {
    this.aclEle = aclEle;
  }

  /**
   * The owner profile&#39;s username
   **/
  public DavFolder ownerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "The owner profile's username")
  @JsonProperty("ownerUsername")
  @NotNull
  public String getOwnerUsername() {
    return ownerUsername;
  }
  public void setOwnerUsername(String ownerUsername) {
    this.ownerUsername = ownerUsername;
  }

  /**
   * Suitable display name
   **/
  public DavFolder displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Suitable display name")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DavFolder davFolder = (DavFolder) o;
    return Objects.equals(id, davFolder.id) &&
        Objects.equals(uid, davFolder.uid) &&
        Objects.equals(name, davFolder.name) &&
        Objects.equals(description, davFolder.description) &&
        Objects.equals(color, davFolder.color) &&
        Objects.equals(syncToken, davFolder.syncToken) &&
        Objects.equals(aclFol, davFolder.aclFol) &&
        Objects.equals(aclEle, davFolder.aclEle) &&
        Objects.equals(ownerUsername, davFolder.ownerUsername) &&
        Objects.equals(displayName, davFolder.displayName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uid, name, description, color, syncToken, aclFol, aclEle, ownerUsername, displayName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavFolder {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

