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

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.OCategoryPropSet;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.OTaskAttachment;
import com.sonicle.webtop.tasks.bol.VTaskLookup;
import com.sonicle.webtop.tasks.bol.VTaskObject;
import com.sonicle.webtop.tasks.model.BaseTask;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskLookup;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author malbinola
 */
public class ManagerUtils {
	
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
			tgt.setIsDefault(src.getIsDefault());
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
			tgt.setIsDefault(src.getIsDefault());
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
			if (tgt.getIsDefault() == null) tgt.setIsDefault(false);
			if (tgt.getIsPrivate() == null) tgt.setIsPrivate(false);
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
	
	static TaskObject fillTaskObject(TaskObject tgt, Task src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTaskId(src.getTaskId());
			tgt.setCategoryId(src.getCategoryId());
			tgt.setRevisionStatus(src.getRevisionStatus());
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setHref(src.getHref());
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
		}
		return tgt;
	}
	
	static <T extends TaskLookup, S extends VTaskLookup> T fillTaskLookup(T tgt, S src) {
		if ((tgt != null) && (src != null)) {
			fillBaseTask(tgt, src);
			tgt.setCategoryName(src.getCategoryName());
			tgt.setCategoryDomainId(src.getCategoryDomainId());
			tgt.setCategoryUserId(src.getCategoryUserId());
			tgt.setDescription(src.getDescription());
		}
		return tgt;
	}
	
	static <T extends BaseTask, S extends OTask> T fillBaseTask(T tgt, S src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTaskId(src.getTaskId());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setCategoryId(src.getCategoryId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), BaseTask.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setCreationTimestamp(src.getCreationTimestamp());
			tgt.setSubject(src.getSubject());
			tgt.setStartDate(src.getStartDate());
			tgt.setDueDate(src.getDueDate());
			tgt.setCompletedDate(src.getCompletedDate());
			tgt.setImportance(src.getImportance());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setStatus(EnumUtils.forSerializedName(src.getStatus(), BaseTask.Status.class));
			tgt.setCompletionPercentage(src.getCompletionPercentage());
			tgt.setReminderDate(src.getReminderDate());
		}
		return tgt;
	}
	
	
	
	static Task createTask(OTask src) {
		if (src == null) return null;
		return fillTask(new Task(), src);
	}
	
	static Task fillTask(Task tgt, OTask src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTaskId(src.getTaskId());
			tgt.setCategoryId(src.getCategoryId());
			tgt.setRevisionStatus(EnumUtils.forSerializedName(src.getRevisionStatus(), Task.RevisionStatus.class));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setSubject(src.getSubject());
			tgt.setDescription(src.getDescription());
			tgt.setStartDate(src.getStartDate());
			tgt.setDueDate(src.getDueDate());
			tgt.setCompletedDate(src.getCompletedDate());
			tgt.setImportance(src.getImportance());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setStatus(EnumUtils.forSerializedName(src.getStatus(), Task.Status.class));
			tgt.setCompletionPercentage(src.getCompletionPercentage());
			tgt.setReminderDate(src.getReminderDate());
		}
		return tgt;
	}
	
	static OTask createOTask(Task src) {
		if (src == null) return null;
		return fillOTask(new OTask(), src);
	}
	
	static OTask fillOTask(OTask tgt, Task src) {
		if ((tgt != null) && (src != null)) {
			tgt.setTaskId(src.getTaskId());
			tgt.setCategoryId(src.getCategoryId());
			tgt.setRevisionStatus(EnumUtils.toSerializedName(src.getRevisionStatus()));
			tgt.setRevisionTimestamp(src.getRevisionTimestamp());
			tgt.setPublicUid(src.getPublicUid());
			tgt.setSubject(src.getSubject());
			tgt.setDescription(src.getDescription());
			tgt.setStartDate(src.getStartDate());
			tgt.setDueDate(src.getDueDate());
			tgt.setCompletedDate(src.getCompletedDate());
			tgt.setImportance(src.getImportance());
			tgt.setIsPrivate(src.getIsPrivate());
			tgt.setStatus(EnumUtils.toSerializedName(src.getStatus()));
			tgt.setCompletionPercentage(src.getCompletionPercentage());
			tgt.setReminderDate(src.getReminderDate());
		}
		return tgt;
	}
	
	static OTask fillOTaskWithDefaults(OTask tgt, UserProfileId targetProfile) {
		if (tgt != null) {
			if (StringUtils.isBlank(tgt.getPublicUid())) {
				tgt.setPublicUid(TasksUtils.buildTaskUid(tgt.getTaskId(), WT.getDomainInternetName(targetProfile.getDomainId())));
			}
			if (tgt.getImportance() == null) tgt.setImportance((short)0);
			if (tgt.getIsPrivate() == null) tgt.setIsPrivate(false);
			if (tgt.getStatus() == null) tgt.setStatus(EnumUtils.toSerializedName(Task.Status.NOT_STARTED));
			if (tgt.getCompletionPercentage() == null) tgt.setCompletionPercentage((short)0);
		}
		return tgt;
	}
	
	static List<TaskAttachment> createTaskAttachmentList(List<OTaskAttachment> items) {
		ArrayList<TaskAttachment> list = new ArrayList<>(items.size());
		for (OTaskAttachment item : items) {
			list.add(createTaskAttachment(item));
		}
		return list;
	}
	
	static TaskAttachment createTaskAttachment(OTaskAttachment src) {
		if (src == null) return null;
		return fillTaskAttachment(new TaskAttachment(), src);
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
	
	static OTaskAttachment createOTaskAttachment(TaskAttachment src) {
		if (src == null) return null;
		return fillOTaskAttachment(new OTaskAttachment(), src);
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
}
