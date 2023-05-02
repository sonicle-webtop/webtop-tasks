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
import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.util.JRHelper;
import com.sonicle.webtop.core.util.RRuleStringify;
import com.sonicle.webtop.tasks.ITaskInstanceStatable;
import com.sonicle.webtop.tasks.TasksUtils;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFSFolder;
import com.sonicle.webtop.tasks.model.CategoryFSOrigin;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskInstance;
import java.util.Date;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class RBTaskDetail {
	private String taskType;
	private String subject;
	private String location;
	private String description;
	private java.util.Date startDate;
	private java.util.Date dueDate;
	private java.util.Date completedOnDate;
	private Float progressPerc;
	private String status;
	private String importance;
	private Boolean isPrivate;
	private Integer reminder;
	private String documentRef;
	private String recurrenceHRFreqText;
	private String recurrenceHRRuleText;
	private String categoryOwner;
	//private Integer categoryId;
	private String categoryName;
	private String categoryColor;
	
	public RBTaskDetail(RRuleStringify rrStringify, CategoryFSOrigin origin, CategoryFSFolder folder, CategoryPropSet folderProps, TaskInstance task, DateTimeZone userTimezone) {
		this(rrStringify, origin instanceof MyCategoryFSOrigin ? null : LangUtils.abbreviatePersonalName(true, origin.getDisplayName()), folder.getCategory(), folderProps, task, userTimezone);
	}

	public RBTaskDetail(RRuleStringify rrStringify, String categoryOwner, Category category, CategoryPropSet folderProps, TaskInstance task, DateTimeZone userTimezone) {
		this.taskType = toTaskType(task);
		this.subject = JRHelper.saniString(task.getSubject());
		this.location = JRHelper.saniString(task.getLocation());
		this.description = JRHelper.saniString(task.getDescription());
		this.startDate = JRHelper.dateTimeAsDate(task.getStart(), userTimezone);
		this.dueDate = JRHelper.dateTimeAsDate(task.getDue(), userTimezone);
		this.completedOnDate = JRHelper.dateTimeAsDate(task.getCompletedOn(), userTimezone);
		this.progressPerc = task.getProgressPercentage();
		this.status = EnumUtils.toSerializedName(task.getStatus());
		this.importance = String.valueOf(task.getImportance());
		this.isPrivate = task.getIsPrivate();
		this.reminder =	TaskBase.Reminder.getMinutesValue(task.getReminder());
		this.documentRef = task.getDocumentRef();
		if (task.hasRecurrence()) {
			this.recurrenceHRFreqText = rrStringify.toHumanReadableFrequencyQuietly(task.getRecurrence().getRule());
			this.recurrenceHRRuleText = rrStringify.toHumanReadableTextQuietly(task.getRecurrence().getRule());
		}
		this.categoryOwner = categoryOwner;
		//this.categoryId = category.getCategoryId();
		this.categoryName = JRHelper.saniString(category.getName());
		this.categoryColor = Category.getHexColor(category.getColor());
		if (folderProps != null) this.categoryColor = Category.getHexColor(folderProps.getColorOrDefault(categoryColor));
	}
	
	public String getTaskType() {
		return taskType;
	}

	public String getSubject() {
		return subject;
	}
	
	public String getLocation() {
		return location;
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

	public Date getCompletedOnDate() {
		return completedOnDate;
	}
	
	public Float getProgressPerc() {
		return progressPerc;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getImportance() {
		return importance;
	}

	public Boolean getIsPrivate() {
		return isPrivate;
	}
	
	public Integer getReminder() {
		return reminder;
	}
	
	public String getDocumentRef() {
		return documentRef;
	}
	
	public String getRecurrenceHRFreqText() {
		return recurrenceHRFreqText;
	}
	
	public String getRecurrenceHRRuleText() {
		return recurrenceHRRuleText;
	}
	
	public String getCategoryOwner() {
		return categoryOwner;
	}

	public String getCategoryName() {
		return categoryName;
	}
	
	public String getCategoryColor() {
		return categoryColor;
	}
	
	private String toTaskType(ITaskInstanceStatable task) {
		if (TasksUtils.isTaskSeriesMaster(task)) return "SERIES_MASTER";
		else if (TasksUtils.isTaskSeriesItem(task)) return "SERIES_ITEM";
		else if (TasksUtils.isTaskSeriesBroken(task)) return "SERIES_BROKEN";
		else return "DEFAULT";
	}
}
