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
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldDefs;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldValue;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.webtop.tasks.model.TaskAssignee;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskEx;
import com.sonicle.webtop.tasks.model.TaskInstance;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import com.sonicle.webtop.tasks.model.TaskRecurrence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import net.fortuna.ical4j.model.Recur;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsTask {
	public String id;
	public String parentId;
	public Integer categoryId;
	//public String publicUid;
	public String subject;
	public String location;
	public String description;
	public String timezone;
	public String start;
	public String due;
	public Short progress;
	public String completedOn;
	public String status;
	public Short importance;
	public Boolean isPrivate;
	public String docRef;
	public Integer reminder;
	public String contact;
	public String contactId;
	public String rrule;
	public String tags;
	public ArrayList<Assignee> assignees;
	public ArrayList<Attachment> attachments;
	public ArrayList<ObjCustomFieldValue> cvalues;
	public Integer _childTotalCount; // Read-only
	public Integer _childComplCount; // Read-only
	public String _parentSubject; // Read-only
	public String _ownerId; // Read-only
	public String _cfdefs; // Read-only

	public JsTask() {}
	
	public JsTask(UserProfileId ownerId, TaskInstance task, Collection<CustomPanel> customPanels, Map<String, CustomField> customFields, String parentTaskSubject, String profileLanguageTag, DateTimeZone profileTz) {
		DateTimeFormatter fmtYmdHms = DateTimeUtils.createYmdHmsFormatter(profileTz);
		
		this.id = task.getId().toString();
		this.parentId = task.getParentInstanceId() != null ? task.getParentInstanceId().toString() : null;
		this.categoryId = task.getCategoryId();
		this.subject = task.getSubject();
		this.location = task.getLocation();
		this.description = task.getDescription();
		this.timezone = task.getTimezone();
		this.start = DateTimeUtils.print(fmtYmdHms, task.getStart());
		this.due = DateTimeUtils.print(fmtYmdHms, task.getDue());
		this.progress = task.getProgress();
		this.completedOn = DateTimeUtils.print(fmtYmdHms, task.getCompletedOn());
		this.status = EnumUtils.toSerializedName(task.getStatus());
		this.importance = task.getImportance();
		this.isPrivate = task.getIsPrivate();
		this.docRef = task.getDocumentRef();
		this.reminder = task.getReminder();
		this.contact = task.getContact();
		this.contactId = task.getContactId();
		if (task.getRecurrence() != null) {
			this.rrule = task.getRecurrence().getRule();
		}
		
		this.assignees = new ArrayList<>();
		for (TaskAssignee ass : task.getAssignees()) {
			Assignee js = new Assignee();
			js.id = ass.getAssigneeId();
			js.recipient = ass.getRecipient();
			js.recipientUserId = ass.getRecipientUserId();
			this.assignees.add(js);
		}
		
		this.tags = CId.build(task.getTags()).toString();
		
		this.attachments = new ArrayList<>();
		for (TaskAttachment att : task.getAttachments()) {
			Attachment js = new Attachment();
			js.id = att.getAttachmentId();
			//jsatt.lastModified = DateTimeUtils.printYmdHmsWithZone(att.getRevisionTimestamp(), profileTz);
			js.name = att.getFilename();
			js.size = att.getSize();
			this.attachments.add(js);
		}
		
		cvalues = new ArrayList<>();
		ArrayList<ObjCustomFieldDefs.Panel> panels = new ArrayList<>();
		for (CustomPanel panel : customPanels) {
			panels.add(new ObjCustomFieldDefs.Panel(panel, profileLanguageTag));
		}
		ArrayList<ObjCustomFieldDefs.Field> fields = new ArrayList<>();
		for (CustomField field : customFields.values()) {
			CustomFieldValue cvalue = null;
			if (task.hasCustomValues()) {
				cvalue = task.getCustomValues().get(field.getFieldId());
			}
			cvalues.add(cvalue != null ? new ObjCustomFieldValue(field.getType(), cvalue, profileTz) : new ObjCustomFieldValue(field.getType(), field.getFieldId()));
			fields.add(new ObjCustomFieldDefs.Field(field, profileLanguageTag));
		}
		
		_childTotalCount = task.getChildrenTotalCount();
		_childComplCount = task.getChildrenCompletedCount();
		_parentSubject = task.getParentInstanceId() != null ? parentTaskSubject : null;
		_ownerId = ownerId.toString();
		_cfdefs = LangUtils.serialize(new ObjCustomFieldDefs(panels, fields), ObjCustomFieldDefs.class);
	}
	
	public TaskEx createTaskForAdd(DateTimeZone profileTz) {
		DateTimeZone tz = LangUtils.coalesce(DateTimeUtils.parseDateTimeZone(timezone), profileTz);
		DateTimeFormatter fmtYmdHms = DateTimeUtils.createYmdHmsFormatter(tz);
		
		TaskEx item = new TaskEx();
		item.setParentInstanceId(TaskInstanceId.parse(parentId));
		item.setCategoryId(categoryId);
		item.setSubject(subject);
		item.setLocation(location);
		item.setDescription(description);
		item.setDescriptionType(TaskBase.BodyType.TEXT);
		item.setTimezone(tz.getID());
		item.setStart(DateTimeUtils.parseDateTime(fmtYmdHms, start));
		item.setDue(DateTimeUtils.parseDateTime(fmtYmdHms, due));
		item.setProgress(progress);
		item.setStatus(EnumUtils.forSerializedName(status, TaskBase.Status.class));
		item.setImportance(importance);
		item.setIsPrivate(isPrivate);
		item.setDocumentRef(docRef);
		item.setReminder(reminder);
		item.setContact(contact);
		item.setContactId(contactId);
		
		if (!StringUtils.isBlank(rrule)) {
			LocalTime lt = item.getStart().toLocalTime();
			// Fix recur until-date timezone: due to we do not have tz-db in client
			// we cannot move until-date into the right zone directly in browser, 
			// so we need to change it here if necessary.
			Recur recur = ICal4jUtils.parseRRule(rrule);
			if (ICal4jUtils.adjustRecurUntilDate(recur, lt, tz)) {
				rrule = recur.toString();
			}
			//item.setRecurrence(new TaskRecurrence(rrule, DateTimeUtils.parseLocalDate(fmtYmd, rstart)));
			item.setRecurrence(new TaskRecurrence(rrule, item.getStart()));
		}
		
		item.setAssignees(new ArrayList<>());
		for (JsTask.Assignee js : assignees) {
			TaskAssignee assignee = new TaskAssignee();
			assignee.setAssigneeId(js.id);
			assignee.setRecipient(js.recipient);
			assignee.setRecipientUserId(js.recipientUserId);
			assignee.setResponseStatus(EnumUtils.forSerializedName(js.respStatus, TaskAssignee.ResponseStatus.NEEDS_ACTION, TaskAssignee.ResponseStatus.class));
			//assignee.setNotify(js.notify);
			item.getAssignees().add(assignee);
		}
		
		item.setTags(new LinkedHashSet<>(new CompositeId().parse(tags).getTokens()));
		
		ArrayList<CustomFieldValue> customValues = new ArrayList<>();
		for (ObjCustomFieldValue jscfv : cvalues) {
			customValues.add(jscfv.toCustomFieldValue(profileTz));
		}
		item.setCustomValues(customValues);
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		return item;
	}
	
	public TaskEx createTaskForUpdate(DateTimeZone profileTz) {
		DateTimeZone tz = LangUtils.coalesce(DateTimeUtils.parseDateTimeZone(timezone), profileTz);
		DateTimeFormatter fmtYmdHms = DateTimeUtils.createYmdHmsFormatter(tz);
		
		TaskEx item = new TaskEx();
		item.setCategoryId(categoryId);
		item.setSubject(subject);
		item.setLocation(location);
		item.setDescription(description);
		item.setDescriptionType(TaskBase.BodyType.TEXT);
		item.setStart(DateTimeUtils.parseDateTime(fmtYmdHms, start));
		item.setDue(DateTimeUtils.parseDateTime(fmtYmdHms, due));
		item.setProgress(progress);
		item.setStatus(EnumUtils.forSerializedName(status, TaskBase.Status.class));
		item.setImportance(importance);
		item.setIsPrivate(isPrivate);
		item.setDocumentRef(docRef);
		item.setReminder(reminder);
		item.setContact(contact);
		item.setContactId(contactId);
		
		if (!StringUtils.isBlank(rrule)) {
			LocalTime lt = item.getStart().toLocalTime();
			// Fix recur until-date timezone: due to we do not have tz-db in client
			// we cannot move until-date into the right zone directly in browser, 
			// so we need to change it here if necessary.
			Recur recur = ICal4jUtils.parseRRule(rrule);
			if (ICal4jUtils.adjustRecurUntilDate(recur, lt, tz)) {
				rrule = recur.toString();
			}
			//item.setRecurrence(new TaskRecurrence(rrule, DateTimeUtils.parseLocalDate(fmtYmd, rstart)));
			item.setRecurrence(new TaskRecurrence(rrule, item.getStart()));
		}
		
		item.setAssignees(new ArrayList<>());
		for (JsTask.Assignee js : assignees) {
			TaskAssignee assignee = new TaskAssignee();
			assignee.setAssigneeId(js.id);
			assignee.setRecipient(js.recipient);
			assignee.setRecipientUserId(js.recipientUserId);
			assignee.setResponseStatus(EnumUtils.forSerializedName(js.respStatus, TaskAssignee.ResponseStatus.NEEDS_ACTION, TaskAssignee.ResponseStatus.class));
			//assignee.setNotify(js.notify);
			item.getAssignees().add(assignee);
		}
		
		item.setTags(new LinkedHashSet<>(new CompositeId().parse(tags).getTokens()));
		
		ArrayList<CustomFieldValue> customValues = new ArrayList<>();
		for (ObjCustomFieldValue jscfv : cvalues) {
			customValues.add(jscfv.toCustomFieldValue(profileTz));
		}
		item.setCustomValues(customValues);
		// Attachment needs to be treated outside this class in order to have complete access to their streams
		return item;
	}
	
	public static class Assignee {
		public String id;
		public String recipient;
		public String recipientUserId;
		public String respStatus;
	}
	
	public static class Attachment {
		public String id;
		public String name;
		public Long size;
		public String _uplId;
	}
}
