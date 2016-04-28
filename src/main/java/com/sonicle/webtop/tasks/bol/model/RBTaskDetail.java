/*
 * webtop-tasks is a WebTop Service developed by Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.tasks.bol.model;

import com.sonicle.webtop.core.util.JRHelper;
import com.sonicle.webtop.tasks.bol.OCategory;
import java.awt.Image;
import java.util.Date;
import org.joda.time.DateTime;

/**
 *
 * @author rfullone
 */
public class RBTaskDetail {
	protected java.lang.Integer         taskId;
	protected java.lang.Integer         categoryId;
	protected java.lang.String          categoryName;
	protected java.lang.String          categoryColor;
	protected java.awt.Image            categoryColorImage;
	protected java.lang.String          subject;
	protected java.lang.String          description;
	protected Date startDate;
	protected Date dueDate;
	protected Date completedDate;
	protected java.lang.Short           importance;
	protected java.lang.Boolean         isPrivate;
	protected java.lang.String          status;
	protected java.lang.Short           completionPercentage;
	protected Date reminderDate;
   
    public RBTaskDetail() {}

    public RBTaskDetail(OCategory category, Task task) {
        this.taskId = task.getCategoryId();
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
        this.status = task.getStatus();
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