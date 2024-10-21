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
 * Represent task&#39;s updateable fields
 **/
@ApiModel(description = "Represent task's updateable fields")
@JsonTypeName("EasSyncTaskUpdate")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2024-10-21T12:01:13.376+02:00[Europe/Berlin]")
public class ApiEasSyncTaskUpdate   {
  private @Valid String subject;
  private @Valid String start;
  private @Valid String due;
  private @Valid String complOn;
  private @Valid Integer impo;
  private @Valid Boolean prvt;
  private @Valid String notes;

  /**
   * Subject
   **/
  public ApiEasSyncTaskUpdate subject(String subject) {
    this.subject = subject;
    return this;
  }

  
  @ApiModelProperty(value = "Subject")
  @JsonProperty("subject")
  public String getSubject() {
    return subject;
  }

  @JsonProperty("subject")
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Start date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public ApiEasSyncTaskUpdate start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(value = "Start date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("start")
  public String getStart() {
    return start;
  }

  @JsonProperty("start")
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * Due date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public ApiEasSyncTaskUpdate due(String due) {
    this.due = due;
    return this;
  }

  
  @ApiModelProperty(value = "Due date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("due")
  public String getDue() {
    return due;
  }

  @JsonProperty("due")
  public void setDue(String due) {
    this.due = due;
  }

  /**
   * Completed date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public ApiEasSyncTaskUpdate complOn(String complOn) {
    this.complOn = complOn;
    return this;
  }

  
  @ApiModelProperty(value = "Completed date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("complOn")
  public String getComplOn() {
    return complOn;
  }

  @JsonProperty("complOn")
  public void setComplOn(String complOn) {
    this.complOn = complOn;
  }

  /**
   * Priority flag (0&#x3D;low, 1&#x3D;normal, 2&#x3D;high)
   **/
  public ApiEasSyncTaskUpdate impo(Integer impo) {
    this.impo = impo;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Priority flag (0=low, 1=normal, 2=high)")
  @JsonProperty("impo")
  @NotNull
  public Integer getImpo() {
    return impo;
  }

  @JsonProperty("impo")
  public void setImpo(Integer impo) {
    this.impo = impo;
  }

  /**
   * Private flag
   **/
  public ApiEasSyncTaskUpdate prvt(Boolean prvt) {
    this.prvt = prvt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Private flag")
  @JsonProperty("prvt")
  @NotNull
  public Boolean getPrvt() {
    return prvt;
  }

  @JsonProperty("prvt")
  public void setPrvt(Boolean prvt) {
    this.prvt = prvt;
  }

  /**
   * Description
   **/
  public ApiEasSyncTaskUpdate notes(String notes) {
    this.notes = notes;
    return this;
  }

  
  @ApiModelProperty(value = "Description")
  @JsonProperty("notes")
  public String getNotes() {
    return notes;
  }

  @JsonProperty("notes")
  public void setNotes(String notes) {
    this.notes = notes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiEasSyncTaskUpdate easSyncTaskUpdate = (ApiEasSyncTaskUpdate) o;
    return Objects.equals(this.subject, easSyncTaskUpdate.subject) &&
        Objects.equals(this.start, easSyncTaskUpdate.start) &&
        Objects.equals(this.due, easSyncTaskUpdate.due) &&
        Objects.equals(this.complOn, easSyncTaskUpdate.complOn) &&
        Objects.equals(this.impo, easSyncTaskUpdate.impo) &&
        Objects.equals(this.prvt, easSyncTaskUpdate.prvt) &&
        Objects.equals(this.notes, easSyncTaskUpdate.notes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, due, complOn, impo, prvt, notes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiEasSyncTaskUpdate {\n");
    
    sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    due: ").append(toIndentedString(due)).append("\n");
    sb.append("    complOn: ").append(toIndentedString(complOn)).append("\n");
    sb.append("    impo: ").append(toIndentedString(impo)).append("\n");
    sb.append("    prvt: ").append(toIndentedString(prvt)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
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

