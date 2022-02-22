/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks;

import com.sonicle.commons.Base58;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.OCategoryPropSet;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.OTaskAssignee;
import com.sonicle.webtop.tasks.bol.OTaskAttachment;
import com.sonicle.webtop.tasks.bol.OTaskCustomValue;
import com.sonicle.webtop.tasks.bol.VTaskLookup;
import com.sonicle.webtop.tasks.bol.VTaskObject;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskAssignee;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskLookup;
import com.sonicle.webtop.tasks.model.TaskAlertLookup;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public class ManagerUtils {
	
	public static String getProductName() {
		return WT.getPlatformName() + " Tasks";
	}
	
	public static int decodeAsTaskFolderId(String categoryPublicUid) throws WTException {
		try {
			return Integer.valueOf(new String(Base58.decode(categoryPublicUid)));
		} catch(RuntimeException ex) { // Not a Base58 input
			throw new WTException(ex, "Invalid calendar UID encoding");
		}
	}
	
	public static String encodeAsTaskFolderUid(int categoryId) {
		return Base58.encode(StringUtils.leftPad(String.valueOf(categoryId), 10, "0").getBytes());
	}
	
	static int toOffset(int page, int limit) {
		return limit * (page-1);
	}
	
	static Category createCategory(OCategory src) {
		if (src == null) return null;
		return fillCategory(new Category(), src);
	}
	
	static Category fillCategory(Category tgt, OCategory src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCategoryId(src.getCategoryId());
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.forSerializedName(src.getSync(), Category.Sync.class));
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setDefaultReminder(src.getReminder());
		}
		return tgt;
	}
	
	static OCategory createOCategory(Category src) {
		if (src == null) return null;
		return fillOCategory(new OCategory(), src);
	}
	
	static OCategory fillOCategory(OCategory tgt, Category src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCategoryId(src.getCategoryId());
			tgt.setDomainId(src.getDomainId());
			tgt.setUserId(src.getUserId());
			tgt.setBuiltIn(src.getBuiltIn());
			tgt.setName(src.getName());
			tgt.setDescription(src.getDescription());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.toSerializedName(src.getSync()));
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setReminder(src.getDefaultReminder());
		}
		return tgt;
	}
	
	static OCategory fillOCategoryWithDefaults(OCategory tgt, UserProfileId targetProfile, TasksServiceSettings ss) {
		if (tgt != null) {
			if (tgt.getDomainId() == null) tgt.setDomainId(targetProfile.getDomainId());
			if (tgt.getUserId() == null) tgt.setUserId(targetProfile.getUserId());
			if (tgt.getBuiltIn() == null) tgt.setBuiltIn(false);
			if (StringUtils.isBlank(tgt.getColor())) tgt.setColor("#FFFFFF");
			if (StringUtils.isBlank(tgt.getSync())) tgt.setSync(EnumUtils.toSerializedName(ss.getDefaultCategorySync()));
			if (tgt.getIsPrivate() == null) tgt.setIsPrivate(false);
			if (tgt.getIsDefault() == null) tgt.setIsDefault(false); // Deprecated: remove when DB field will be deleted!
		}
		return tgt;
	}
	
	static CategoryPropSet createCategoryPropSet(OCategoryPropSet src) {
		if (src == null) return null;
		return fillCategoryPropSet(new CategoryPropSet(), src);
	}
	
	static CategoryPropSet fillCategoryPropSet(CategoryPropSet tgt, OCategoryPropSet src) {
		if ((tgt != null) && (src != null)) {
			tgt.setHidden(src.getHidden());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.forSerializedName(src.getSync(), Category.Sync.class));
		}
		return tgt;
	}
	
	static OCategoryPropSet createOCategoryPropSet(CategoryPropSet src) {
		if (src == null) return null;
		return fillOCategoryPropSet(new OCategoryPropSet(), src);
	}
	
	static OCategoryPropSet fillOCategoryPropSet(OCategoryPropSet tgt, CategoryPropSet src) {
		if ((tgt != null) && (src != null)) {
			tgt.setHidden(src.getHidden());
			tgt.setColor(src.getColor());
			tgt.setSync(EnumUtils.toSerializedName(src.getSync()));
		}
		return tgt;
	}
	
	static <T extends TaskObject> T fillTaskObject(T tgt, VTaskObject src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTaskId(src.getTaskId());
			tgt.setCategoryId(src.getCategoryId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Task.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setHref(src.getHref());
			tgt.setObjectName(LangUtils.coalesceStrings(src.getSubject(), src.getTaskId()));
		}
		return tgt;
	}
	
	static <T extends TaskLookup> T fillTaskLookup(T tgt, VTaskLookup src) {
		fillTask((TaskBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setParentInstanceId(toParentInstanceId(src.getParentTaskId()));
			tgt.setTags(src.getTags());
			tgt.setHasRecurrence(src.getHasRecurrence());
			tgt.setHasChildren(src.getHasChildren());
			tgt.setCategoryName(src.getCategoryName());
			tgt.setCategoryDomainId(src.getCategoryDomainId());
			tgt.setCategoryUserId(src.getCategoryUserId());
		}
		return tgt;
	}
	
	static <T extends TaskAlertLookup> T fillTaskAlertLookup(T tgt, VTaskLookup src) {
		fillTask((TaskBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setRemindedOn(src.getRemindedOn());
			tgt.setHasRecurrence(src.getHasRecurrence());
			tgt.setCategoryName(src.getCategoryName());
			tgt.setCategoryDomainId(src.getCategoryDomainId());
			tgt.setCategoryUserId(src.getCategoryUserId());
		}
		return tgt;
	}
	
	static <T extends Task> T fillTask(T tgt, OTask src) {
		fillTask((TaskBase)tgt, src);
		if ((tgt != null) && (src != null)) {
			tgt.setTaskId(src.getTaskId());
			tgt.setSeriesTaskId(src.getSeriesTaskId());
			tgt.setSeriesInstanceId(src.getSeriesInstanceId());
		}
		return tgt;
	}
	
	static <T extends TaskBase> T fillTask(T tgt, OTask src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCategoryId(src.getCategoryId());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), TaskBase.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setOrganizer(src.getOrganizer());
			tgt.setOrganizerId(src.getOrganizerId());
			tgt.setSubject(src.getSubject());
			tgt.setLocation(src.getLocation());
			tgt.setDescription(src.getDescription());
			tgt.setDescriptionType(EnumUtils.forSerializedName(src.getDescriptionType(), TaskBase.BodyType.class));
			tgt.setTimezone(src.getTimezone());
			tgt.setStart(src.getStart());
			tgt.setDue(src.getDue());
			tgt.setCompletedOn(src.getCompletedOn());
			tgt.setProgress(src.getProgress());
			tgt.setStatus(EnumUtils.forSerializedName(src.getStatus(), TaskBase.Status.class));
			tgt.setImportance(src.getImportance());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setDocumentRef(src.getDocumentRef());
			tgt.setHref(src.getHref());
			tgt.setEtag(src.getEtag());
			tgt.setReminder(TaskBase.Reminder.valueOf(src.getReminder()));
			tgt.setContact(src.getContact());
			tgt.setContactId(src.getContactId());
		}
		return tgt;
	}
	
	static OTask fillOTask(OTask tgt, TaskBase src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCategoryId(src.getCategoryId());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setRevisionStatus(EnumUtils.toSerializedName(src.getRevisionStatus()));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setOrganizer(src.getOrganizer());
			tgt.setOrganizerId(src.getOrganizerId());
			tgt.setSubject(src.getSubject());
			tgt.setLocation(src.getLocation());
			tgt.setDescription(src.getDescription());
			tgt.setDescriptionType(EnumUtils.toSerializedName(src.getDescriptionType()));
			tgt.setTimezone(src.getTimezone());
			tgt.setStart(src.getStart());
			tgt.setDue(src.getDue());
			tgt.setCompletedOn(src.getCompletedOn());
			tgt.setProgress(src.getProgress());
			tgt.setStatus(EnumUtils.toSerializedName(src.getStatus()));
			tgt.setImportance(src.getImportance());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setDocumentRef(src.getDocumentRef());
			tgt.setHref(src.getHref());
			tgt.setEtag(src.getEtag());
			tgt.setReminder(TaskBase.Reminder.getMinutesValue(src.getReminder()));
			tgt.setContact(src.getContact());
			tgt.setContactId(src.getContactId());
		}
		return tgt;
	}
	
	public static String buildOrganizer(UserProfileId profileId) {
		UserProfile.Data ud = WT.getUserData(profileId);
		InternetAddress ia = InternetAddressUtils.toInternetAddress(ud.getEmail().getAddress(), ud.getDisplayName());
		return ia.toString();
	}
	
	static OTask fillOTaskWithDefaultsForInsert(OTask tgt, UserProfileId targetProfile, DateTime defaultTimestamp) {
		if (tgt != null) {
			if (StringUtils.isBlank(tgt.getPublicUid())) {
				tgt.setPublicUid(TasksUtils.buildTaskUid(tgt.getTaskId(), WT.getDomainInternetName(targetProfile.getDomainId())));
			}
			if (tgt.getRevisionTimestamp()== null) tgt.setRevisionTimestamp(defaultTimestamp);
			if (tgt.getRevisionSequence() == null) tgt.setRevisionSequence(0);
			if (tgt.getCreationTimestamp() == null) tgt.setCreationTimestamp(defaultTimestamp);
			if (StringUtils.isBlank(tgt.getOrganizer())) {
				tgt.setOrganizer(buildOrganizer(targetProfile));
				tgt.setOrganizerId(targetProfile.getUserId());
			}
			if (tgt.getTimezone() == null) {
				UserProfile.Data ud = WT.getUserData(targetProfile);
				if (ud != null) tgt.setTimezone(ud.getTimeZoneId());
			}
			if (tgt.getDescriptionType() == null) tgt.setDescriptionType(EnumUtils.toSerializedName(TaskBase.BodyType.TEXT));
			if (tgt.getProgress() == null) tgt.setProgress((short)0);
			if (tgt.getStatus() == null) {
				tgt.setStatus(EnumUtils.toSerializedName(TaskBase.Status.NEEDS_ACTION));
				tgt.setCompletedOn(null);
			}
			if (tgt.getImportance() == null) tgt.setImportance((short)0);
			if (tgt.getIsPrivate() == null) tgt.setIsPrivate(false);
			if (tgt.getStart() == null) tgt.setReminder(null);
			
			if (tgt.getProgress() == 100) {
				tgt.setStatus(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
				if (tgt.getCompletedOn() == null) tgt.setCompletedOn(defaultTimestamp);
			} else if (EnumUtils.toSerializedName(TaskBase.Status.COMPLETED).equals(tgt.getStatus())) {
				if (tgt.getCompletedOn() == null) tgt.setCompletedOn(defaultTimestamp);
			}
			if (StringUtils.isBlank(tgt.getHref())) tgt.setHref(TasksUtils.buildHref(tgt.getPublicUid()));
		}
		return tgt;
	}
	
	static OTask fillOTaskWithDefaultsForUpdate(OTask tgt, DateTime defaultTimestamp) {
		if (tgt != null) {
			if (tgt.getDescriptionType()== null) tgt.setDescriptionType(EnumUtils.toSerializedName(TaskBase.BodyType.TEXT));
			if (tgt.getProgress()== null) tgt.setProgress((short)0);
			if (tgt.getStatus() == null) {
				tgt.setStatus(EnumUtils.toSerializedName(TaskBase.Status.NEEDS_ACTION));
				tgt.setCompletedOn(null);
			}
			if (tgt.getImportance() == null) tgt.setImportance((short)0);
			if (tgt.getIsPrivate() == null) tgt.setIsPrivate(false);
			if (tgt.getStart() == null) tgt.setReminder(null);
			
			if (tgt.getProgress() == 100) {
				tgt.setStatus(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
				if (tgt.getCompletedOn() == null) tgt.setCompletedOn(defaultTimestamp);
			} else if (EnumUtils.toSerializedName(TaskBase.Status.COMPLETED).equals(tgt.getStatus())) {
				if (tgt.getCompletedOn() == null) tgt.setCompletedOn(defaultTimestamp);
			}
		}
		return tgt;
	}
	
	static List<TaskAssignee> createTaskAssigneeList(List<OTaskAssignee> assignees) {
		ArrayList<TaskAssignee> items = new ArrayList<>();
		for (OTaskAssignee assignee : assignees) {
			items.add(fillTaskAssignee(new TaskAssignee(), assignee));
		}
		return items;
	}
	
	static <T extends TaskAssignee> T fillTaskAssignee(T tgt, OTaskAssignee src) {
		if ((tgt != null) && (src != null)) {
			tgt.setAssigneeId(src.getAssigneeId());
			tgt.setRecipient(src.getRecipient());
			tgt.setRecipientUserId(src.getRecipientUserId());
			tgt.setResponseStatus(EnumUtils.forSerializedName(src.getResponseStatus(), TaskAssignee.ResponseStatus.class));
		}
		return tgt;
	}
	
	static OTaskAssignee fillOTaskAssignee(OTaskAssignee tgt, TaskAssignee src) {
		if ((tgt != null) && (src != null)) {
			tgt.setAssigneeId(src.getAssigneeId());
			tgt.setRecipient(src.getRecipient());
			tgt.setRecipientUserId(src.getRecipientUserId());
			tgt.setResponseStatus(EnumUtils.toSerializedName(src.getResponseStatus()));
		}
		return tgt;
	}
	
	static boolean validateForInsert(TaskAssignee src) {
		if (StringUtils.isBlank(src.getRecipientUserId()) && StringUtils.isBlank(src.getRecipient())) return false;
		if (src.getResponseStatus() == null) return false;
		return true;
	}
	
	static boolean validateForUpdate(TaskAssignee src) {
		if (StringUtils.isBlank(src.getRecipientUserId()) && StringUtils.isBlank(src.getRecipient())) return false;
		if (src.getResponseStatus() == null) return false;
		return true;
	}
	
	static List<TaskAttachment> createTaskAttachmentList(List<OTaskAttachment> items) {
		ArrayList<TaskAttachment> list = new ArrayList<>(items.size());
		for (OTaskAttachment item : items) {
			list.add(fillTaskAttachment(new TaskAttachment(), item));
		}
		return list;
	}
	
	static <T extends TaskAttachment> T fillTaskAttachment(T tgt, OTaskAttachment src) {
		if ((tgt != null) && (src != null)) {
			tgt.setAttachmentId(src.getTaskAttachmentId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setFilename(src.getFilename());
			tgt.setSize(src.getSize());
			tgt.setMediaType(src.getMediaType());
		}
		return tgt;
	}
	
	static <T extends OTaskAttachment> T fillOTaskAttachment(T tgt, TaskAttachment src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTaskAttachmentId(src.getAttachmentId());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setRevisionSequence(src.getRevisionSequence());
			tgt.setFilename(src.getFilename());
			tgt.setSize(src.getSize());
			tgt.setMediaType(src.getMediaType());
		}
		return tgt;
	}
	
	static OTask fillOTaskAttachmentWithDefaults(OTask tgt, UserProfileId targetProfile) {
		if (tgt != null) {
			//if (fill.getRevisionTimestamp()== null) fill.setRevisionTimestamp();
			if (tgt.getRevisionSequence() == null) tgt.setRevisionSequence(0);
		}
		return tgt;
	}
	
	static Map<String, CustomFieldValue> createCustomValuesMap(List<OTaskCustomValue> items) {
		LinkedHashMap<String, CustomFieldValue> map = new LinkedHashMap<>(items.size());
		for (OTaskCustomValue item : items) {
			map.put(item.getCustomFieldId(), createCustomValue(item));
		}
		return map;
	}
	
	static CustomFieldValue createCustomValue(OTaskCustomValue src) {
		if (src == null) return null;
		return fillCustomFieldValue(new CustomFieldValue(), src);
	}
	
	static <T extends CustomFieldValue> T fillCustomFieldValue(T tgt, OTaskCustomValue src) {
		if ((tgt != null) && (src != null)) {
			tgt.setFieldId(src.getCustomFieldId());
			tgt.setStringValue(src.getStringValue());
			tgt.setNumberValue(src.getNumberValue());
			tgt.setBooleanValue(src.getBooleanValue());
			tgt.setDateValue(src.getDateValue());
			tgt.setTextValue(src.getTextValue());
		}
		return tgt;
	}
	
	static OTaskCustomValue createOTaskCustomValue(CustomFieldValue src) {
		if (src == null) return null;
		return fillOTaskCustomValue(new OTaskCustomValue(), src);
	}
	
	static <T extends OTaskCustomValue> T fillOTaskCustomValue(T tgt, CustomFieldValue src) {
		if ((tgt != null) && (src != null)) {
			tgt.setCustomFieldId(src.getFieldId());
			tgt.setStringValue(src.getStringValue());
			tgt.setNumberValue(src.getNumberValue());
			tgt.setBooleanValue(src.getBooleanValue());
			tgt.setDateValue(src.getDateValue());
			tgt.setTextValue(src.getTextValue());
		}
		return tgt;
	}
	
	/**
	 * Construct a date-time using date from instanceDate parameter and time 
	 * part from targetDateTime object, appropriately moved to desired timezone 
	 * (the timezone of the task).
	 * @param instanceDate Instance local date.
	 * @param targetDateTime Target date-time from which extract the time part.
	 * @param targetTimezone Target timezone
	 * @return The built date-time object
	 */
	static DateTime instanceDateToDateTime(LocalDate instanceDate, DateTime targetDateTime, DateTimeZone targetTimezone) {
		return instanceDate.toDateTime(targetDateTime.withZone(targetTimezone).toLocalTime(), targetTimezone);
	}
	
	static TaskInstanceId toParentInstanceId(String parentTaskId) {
		return StringUtils.isBlank(parentTaskId) ? null : TaskInstanceId.buildMaster(parentTaskId);
	}
}
