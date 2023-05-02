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
package com.sonicle.webtop.tasks.bol.js;

import com.google.gson.annotations.SerializedName;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFSFolder;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskLookupInstance;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsGridTask {
	public String id;
	public String taskId;
	public String parentId;
	public String subject;
	public String description;
	public String start;
	public String due;
	public String completedOn;
	public String status;
	public Short progress;
	public Short importance;
	public Boolean isPrivate;
	public String docRef;
	public Integer reminder;
	//public String publicUid;
	public String tags;
	public Boolean hasRecur;
    public Integer categoryId;
	public String categoryName;
    public String categoryColor;
	public String _owPid;
	public String _foPerms;
	public String _itPerms;
	public Hierarchy _hierarchy;
	public Integer _depth;
	
	public JsGridTask() {}
	
	public JsGridTask(CategoryFSFolder folder, CategoryPropSet folderProps, TaskLookupInstance task, Hierarchy hierarchy, Integer depth, DateTimeZone profileTz) {
		DateTimeFormatter fmtYmdHms = DateTimeUtils.createYmdHmsFormatter(profileTz);
		Category category = folder.getCategory();
		
		this.id = task.getId().toString();
		this.taskId = task.getTaskId();
		this.parentId = Hierarchy.CHILD.equals(hierarchy) ? task.getParentInstanceId().toString() : null;
        this.subject = task.getSubject();
        this.description = task.getDescription();
		this.start = DateTimeUtils.print(fmtYmdHms, task.getStart());
		this.due = DateTimeUtils.print(fmtYmdHms, task.getDue());
		this.completedOn = DateTimeUtils.print(fmtYmdHms, task.getCompletedOn());
		this.status = EnumUtils.toSerializedName(task.getStatus());
        this.progress = task.getProgress();
        this.importance = task.getImportance();
        this.isPrivate = task.getIsPrivate();
		this.docRef = task.getDocumentRef();
		this.reminder =	TaskBase.Reminder.getMinutesValue(task.getReminder());
		this.tags = task.getTags();
		this.hasRecur = task.getHasRecurrence();
        this.categoryId = task.getCategoryId();
        this.categoryName = category.getName();
		this.categoryColor = category.getColor();
		if (folderProps != null) this.categoryColor = folderProps.getColorOrDefault(categoryColor);
		this._owPid = category.getProfileId().toString();
        this._foPerms = folder.getPermissions().getFolderPermissions().toString();
		this._itPerms = folder.getPermissions().getItemsPermissions().toString();
		this._hierarchy = hierarchy;
		this._depth = depth;
	}
	
	public static enum Hierarchy {
		@SerializedName("parent") PARENT,
		@SerializedName("child") CHILD
	}
}
