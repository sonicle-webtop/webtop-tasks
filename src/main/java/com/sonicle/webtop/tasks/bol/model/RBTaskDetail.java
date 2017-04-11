/* 
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.bol.model;

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.core.util.JRHelper;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.model.Category;
import java.awt.Image;
import java.util.Date;

/**
 *
 * @author rfullone
 */
public class RBTaskDetail {
	public Integer taskId;
	public Integer categoryId;
	public String categoryName;
	public String categoryColor;
	public Image categoryColorImage;
	public String subject;
	public String description;
	public Date startDate;
	public Date dueDate;
	public Date completedDate;
	public Short importance;
	public Boolean isPrivate;
	public String status;
	public Short completionPercentage;
	public Date reminderDate;
   
	public RBTaskDetail() {}

	public RBTaskDetail(Category category, Task task) {
		this.taskId = task.getTaskId();
		this.categoryId = category.getCategoryId();
		this.categoryName = category.getName();
		this.categoryColor = category.getColor();
		this.categoryColorImage = JRHelper.colorAsImage(category.getHexColor());
		this.subject = task.getSubject();
		this.description = task.getDescription();
		this.startDate = (task.getStartDate() == null) ? null : task.getStartDate().toDate();
		this.dueDate = (task.getDueDate() == null) ? null : task.getDueDate().toDate();
		this.completedDate = (task.getCompletedDate() == null) ? null : task.getCompletedDate().toDate();
		this.importance = task.getImportance();
		this.isPrivate = task.getIsPrivate();
		this.status = EnumUtils.getValue(task.getStatus());
		this.completionPercentage = task.getCompletionPercentage();
		this.reminderDate = (task.getReminderDate() == null) ? null : task.getReminderDate().toDate();
	}

	public Integer getTaskId() {
		return taskId;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getCategoryColor() {
		return categoryColor;
	}

	public Image getCategoryColorImage() {
		return categoryColorImage;
	}

	public String getSubject() {
		return subject;
	}

	public String getDescription() {
		return description;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public Date getCompletedDate() {
		return completedDate;
	}

	public Short getImportance() {
		return importance;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}

	public String getStatus() {
		return status;
	}

	public Short getCompletionPercentage() {
		return completionPercentage;
	}

	public Date getReminderDate() {
		return reminderDate;
	}
}
