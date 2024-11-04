package com.sonicle.webtop.tasks.swagger.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObjectChanged;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represent a collection of changes made since an instant defined by the sync-token.
 **/
@ApiModel(description = "Represent a collection of changes made since an instant defined by the sync-token.")
@JsonTypeName("DavObjectsChanges")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-11-04T12:40:25.131+01:00[Europe/Berlin]")
public class ApiDavObjectsChanges   {
  private @Valid String syncToken;
  private @Valid List<ApiDavObjectChanged> inserted = new ArrayList<>();
  private @Valid List<ApiDavObjectChanged> updated = new ArrayList<>();
  private @Valid List<ApiDavObjectChanged> deleted = new ArrayList<>();

  /**
   **/
  public ApiDavObjectsChanges syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
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
   **/
  public ApiDavObjectsChanges inserted(List<ApiDavObjectChanged> inserted) {
    this.inserted = inserted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("inserted")
  @NotNull
  public List<ApiDavObjectChanged> getInserted() {
    return inserted;
  }

  @JsonProperty("inserted")
  public void setInserted(List<ApiDavObjectChanged> inserted) {
    this.inserted = inserted;
  }

  public ApiDavObjectsChanges addInsertedItem(ApiDavObjectChanged insertedItem) {
    if (this.inserted == null) {
      this.inserted = new ArrayList<>();
    }

    this.inserted.add(insertedItem);
    return this;
  }

  public ApiDavObjectsChanges removeInsertedItem(ApiDavObjectChanged insertedItem) {
    if (insertedItem != null && this.inserted != null) {
      this.inserted.remove(insertedItem);
    }

    return this;
  }
  /**
   **/
  public ApiDavObjectsChanges updated(List<ApiDavObjectChanged> updated) {
    this.updated = updated;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("updated")
  @NotNull
  public List<ApiDavObjectChanged> getUpdated() {
    return updated;
  }

  @JsonProperty("updated")
  public void setUpdated(List<ApiDavObjectChanged> updated) {
    this.updated = updated;
  }

  public ApiDavObjectsChanges addUpdatedItem(ApiDavObjectChanged updatedItem) {
    if (this.updated == null) {
      this.updated = new ArrayList<>();
    }

    this.updated.add(updatedItem);
    return this;
  }

  public ApiDavObjectsChanges removeUpdatedItem(ApiDavObjectChanged updatedItem) {
    if (updatedItem != null && this.updated != null) {
      this.updated.remove(updatedItem);
    }

    return this;
  }
  /**
   **/
  public ApiDavObjectsChanges deleted(List<ApiDavObjectChanged> deleted) {
    this.deleted = deleted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("deleted")
  @NotNull
  public List<ApiDavObjectChanged> getDeleted() {
    return deleted;
  }

  @JsonProperty("deleted")
  public void setDeleted(List<ApiDavObjectChanged> deleted) {
    this.deleted = deleted;
  }

  public ApiDavObjectsChanges addDeletedItem(ApiDavObjectChanged deletedItem) {
    if (this.deleted == null) {
      this.deleted = new ArrayList<>();
    }

    this.deleted.add(deletedItem);
    return this;
  }

  public ApiDavObjectsChanges removeDeletedItem(ApiDavObjectChanged deletedItem) {
    if (deletedItem != null && this.deleted != null) {
      this.deleted.remove(deletedItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiDavObjectsChanges davObjectsChanges = (ApiDavObjectsChanges) o;
    return Objects.equals(this.syncToken, davObjectsChanges.syncToken) &&
        Objects.equals(this.inserted, davObjectsChanges.inserted) &&
        Objects.equals(this.updated, davObjectsChanges.updated) &&
        Objects.equals(this.deleted, davObjectsChanges.deleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(syncToken, inserted, updated, deleted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDavObjectsChanges {\n");
    
    sb.append("    syncToken: ").append(toIndentedString(syncToken)).append("\n");
    sb.append("    inserted: ").append(toIndentedString(inserted)).append("\n");
    sb.append("    updated: ").append(toIndentedString(updated)).append("\n");
    sb.append("    deleted: ").append(toIndentedString(deleted)).append("\n");
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

