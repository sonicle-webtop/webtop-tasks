/*
 * Copyright (C) 2025 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.bol;

/**
 *
 * @author malbinola
 */
public class VTaskBase extends OTask {
	// We extend OTask for simplicity, not all fields are needed!
	protected Boolean hasRecurrence;
	
	public Boolean getHasRecurrence() {
		return hasRecurrence;
	}

	public void setHasRecurrence(Boolean hasRecurrence) {
		this.hasRecurrence = hasRecurrence;
	}
	
	/*
	protected Integer taskId;
	protected String publicUid;
	protected Integer categoryId;
	protected String subject;
	protected String description;
	protected DateTime startDate;
	protected DateTime dueDate;
	protected DateTime completedDate;
	protected Short importance;
	protected Boolean isPrivate;
	protected String status;
	protected Short completionPercentage;
	protected DateTime reminderDate;

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	
	public String getPublicUid() {
		return publicUid;
	}

	public void setPublicUid(String publicUid) {
		this.publicUid = publicUid;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getDueDate() {
		return dueDate;
	}

	public void setDueDate(DateTime dueDate) {
		this.dueDate = dueDate;
	}

	public DateTime getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(DateTime completedDate) {
		this.completedDate = completedDate;
	}

	public Short getImportance() {
		return importance;
	}

	public void setImportance(Short importance) {
		this.importance = importance;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(Boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Short getCompletionPercentage() {
		return completionPercentage;
	}

	public void setCompletionPercentage(Short completionPercentage) {
		this.completionPercentage = completionPercentage;
	}

	public DateTime getReminderDate() {
		return reminderDate;
	}

	public void setReminderDate(DateTime reminderDate) {
		this.reminderDate = reminderDate;
	}
	*/
}
