package com.sonicle.webtop.tasks.swagger.v1.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;


/**
 * Bean for carry task&#39;s updateable fields
 **/
import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
@ApiModel(description = "Bean for carry task's updateable fields")

public class SyncTaskUpdate   {
  
  private @Valid String subject = null;
  private @Valid String start = null;
  private @Valid String due = null;
  private @Valid String complOn = null;
  private @Valid Integer impo = null;
  private @Valid Boolean prvt = null;
  private @Valid String notes = null;

  /**
   * Subject
   **/
  public SyncTaskUpdate subject(String subject) {
    this.subject = subject;
    return this;
  }

  
  @ApiModelProperty(value = "Subject")
  @JsonProperty("subject")
  public String getSubject() {
    return subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Start date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public SyncTaskUpdate start(String start) {
    this.start = start;
    return this;
  }

  
  @ApiModelProperty(value = "Start date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("start")
  public String getStart() {
    return start;
  }
  public void setStart(String start) {
    this.start = start;
  }

  /**
   * Due date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public SyncTaskUpdate due(String due) {
    this.due = due;
    return this;
  }

  
  @ApiModelProperty(value = "Due date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("due")
  public String getDue() {
    return due;
  }
  public void setDue(String due) {
    this.due = due;
  }

  /**
   * Completed date/time (ISO date/time YYYYMMDD&#39;T&#39;HHMMSS&#39;Z&#39;)
   **/
  public SyncTaskUpdate complOn(String complOn) {
    this.complOn = complOn;
    return this;
  }

  
  @ApiModelProperty(value = "Completed date/time (ISO date/time YYYYMMDD'T'HHMMSS'Z')")
  @JsonProperty("complOn")
  public String getComplOn() {
    return complOn;
  }
  public void setComplOn(String complOn) {
    this.complOn = complOn;
  }

  /**
   * Priority flag (0&#x3D;low, 1&#x3D;normal, 2&#x3D;high)
   **/
  public SyncTaskUpdate impo(Integer impo) {
    this.impo = impo;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Priority flag (0=low, 1=normal, 2=high)")
  @JsonProperty("impo")
  @NotNull
  public Integer getImpo() {
    return impo;
  }
  public void setImpo(Integer impo) {
    this.impo = impo;
  }

  /**
   * Private flag
   **/
  public SyncTaskUpdate prvt(Boolean prvt) {
    this.prvt = prvt;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Private flag")
  @JsonProperty("prvt")
  @NotNull
  public Boolean isPrvt() {
    return prvt;
  }
  public void setPrvt(Boolean prvt) {
    this.prvt = prvt;
  }

  /**
   * Description
   **/
  public SyncTaskUpdate notes(String notes) {
    this.notes = notes;
    return this;
  }

  
  @ApiModelProperty(value = "Description")
  @JsonProperty("notes")
  public String getNotes() {
    return notes;
  }
  public void setNotes(String notes) {
    this.notes = notes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SyncTaskUpdate syncTaskUpdate = (SyncTaskUpdate) o;
    return Objects.equals(subject, syncTaskUpdate.subject) &&
        Objects.equals(start, syncTaskUpdate.start) &&
        Objects.equals(due, syncTaskUpdate.due) &&
        Objects.equals(complOn, syncTaskUpdate.complOn) &&
        Objects.equals(impo, syncTaskUpdate.impo) &&
        Objects.equals(prvt, syncTaskUpdate.prvt) &&
        Objects.equals(notes, syncTaskUpdate.notes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, start, due, complOn, impo, prvt, notes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SyncTaskUpdate {\n");
    
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
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

