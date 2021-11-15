package com.sonicle.webtop.tasks.swagger.v1.model;

import com.sonicle.webtop.tasks.swagger.v1.model.DavObjectChanged;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Represent a collection of changes made since an instant defined by the sync-token
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Represent a collection of changes made since an instant defined by the sync-token")

public class DavObjectsChanges   {
  
  private @Valid String syncToken = null;
  private @Valid List<DavObjectChanged> inserted = new ArrayList<DavObjectChanged>();
  private @Valid List<DavObjectChanged> updated = new ArrayList<DavObjectChanged>();
  private @Valid List<DavObjectChanged> deleted = new ArrayList<DavObjectChanged>();

  /**
   **/
  public DavObjectsChanges syncToken(String syncToken) {
    this.syncToken = syncToken;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("syncToken")
  @NotNull
  public String getSyncToken() {
    return syncToken;
  }
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

  /**
   **/
  public DavObjectsChanges inserted(List<DavObjectChanged> inserted) {
    this.inserted = inserted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("inserted")
  @NotNull
  public List<DavObjectChanged> getInserted() {
    return inserted;
  }
  public void setInserted(List<DavObjectChanged> inserted) {
    this.inserted = inserted;
  }

  /**
   **/
  public DavObjectsChanges updated(List<DavObjectChanged> updated) {
    this.updated = updated;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("updated")
  @NotNull
  public List<DavObjectChanged> getUpdated() {
    return updated;
  }
  public void setUpdated(List<DavObjectChanged> updated) {
    this.updated = updated;
  }

  /**
   **/
  public DavObjectsChanges deleted(List<DavObjectChanged> deleted) {
    this.deleted = deleted;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("deleted")
  @NotNull
  public List<DavObjectChanged> getDeleted() {
    return deleted;
  }
  public void setDeleted(List<DavObjectChanged> deleted) {
    this.deleted = deleted;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DavObjectsChanges davObjectsChanges = (DavObjectsChanges) o;
    return Objects.equals(syncToken, davObjectsChanges.syncToken) &&
        Objects.equals(inserted, davObjectsChanges.inserted) &&
        Objects.equals(updated, davObjectsChanges.updated) &&
        Objects.equals(deleted, davObjectsChanges.deleted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(syncToken, inserted, updated, deleted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DavObjectsChanges {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

