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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import org.joda.time.DateTimeZone;

/**
 *
 * @author rfullone
 */
public class JsTask {
	public Integer taskId;
	public Integer categoryId;
	//public String publicUid;
	public String subject;
	public String description;
	public String startDate;
	public String dueDate;
	public String completedDate;
	public Short importance;
	public Boolean isPrivate;
	public String status;
	public Short percentage;
	public String reminderDate;
	public String tags;
	public ArrayList<Attachment> attachments;
	// Read-only fields
	public String _profileId;

	public JsTask() {}

	public JsTask(UserProfileId ownerId, Task task, DateTimeZone profileTz) {
		taskId = task.getTaskId();
		categoryId = task.getCategoryId();
		//publicUid;
		subject = task.getSubject();
		description = task.getDescription();
		startDate = DateTimeUtils.printYmdHmsWithZone(task.getStartDate(), profileTz);
		dueDate = DateTimeUtils.printYmdHmsWithZone(task.getDueDate(), profileTz);
		completedDate = DateTimeUtils.printYmdHmsWithZone(task.getCompletedDate(), profileTz);
		importance = task.getImportance();
		isPrivate = task.getIsPrivate();
		status = EnumUtils.toSerializedName(task.getStatus());
		percentage = task.getCompletionPercentage();
		reminderDate = DateTimeUtils.printYmdHmsWithZone(task.getReminderDate(), profileTz);
		tags = new CompositeId(task.getTags()).toString();
		
		attachments = new ArrayList<>();
		for (TaskAttachment att : task.getAttachments()) {
			Attachment jsatt = new Attachment();
			jsatt.id = att.getAttachmentId();
			//jsatt.lastModified = DateTimeUtils.printYmdHmsWithZone(att.getRevisionTimestamp(), profileTz);
			jsatt.name = att.getFilename();
			jsatt.size = att.getSize();
			attachments.add(jsatt);
		}
		
		_profileId = ownerId.toString();
	}

	public static Task createTask(JsTask js, DateTimeZone profileTz) {
		Task item = new Task();
		item.setTaskId(js.taskId);
		item.setCategoryId(js.categoryId);
		item.setSubject(js.subject);
		item.setDescription(js.description);
		item.setStartDate(DateTimeUtils.parseYmdHmsWithZone(js.startDate, DateTimeZone.UTC));
		item.setDueDate(DateTimeUtils.parseYmdHmsWithZone(js.dueDate, DateTimeZone.UTC));
		item.setCompletedDate(DateTimeUtils.parseYmdHmsWithZone(js.completedDate, DateTimeZone.UTC));
		item.setImportance(js.importance);
		item.setIsPrivate(js.isPrivate);
		item.setStatus(EnumUtils.forSerializedName(js.status, Task.Status.class));
		item.setCompletionPercentage(js.percentage);
		item.setReminderDate(DateTimeUtils.parseYmdHmsWithZone(js.reminderDate, DateTimeZone.UTC));
		item.setTags(new LinkedHashSet<>(new CompositeId().parse(js.tags).getTokens()));
		
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		return item;
	}
	
	public static class Attachment {
		public String id;
		public String name;
		public Long size;
		public String _uplId;
	}
}
