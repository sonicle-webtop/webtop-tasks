/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.rest.v1;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.model.SharePerms;
import com.sonicle.webtop.core.model.SharePermsElements;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.tasks.TaskObjectOutputType;
import com.sonicle.webtop.tasks.TasksManager;
import com.sonicle.webtop.tasks.model.BaseTask;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.ShareFolderCategory;
import com.sonicle.webtop.tasks.model.ShareRootCategory;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskObjectWithBean;
import com.sonicle.webtop.tasks.swagger.v1.api.EasApi;
import com.sonicle.webtop.tasks.swagger.v1.model.ApiError;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncFolder;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncTask;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncTaskStat;
import com.sonicle.webtop.tasks.swagger.v1.model.SyncTaskUpdate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Eas extends EasApi {
	private static final Logger logger = LoggerFactory.getLogger(Eas.class);
	private static final String DEFAULT_ETAG = "19700101000000000";
	private static final DateTimeFormatter ETAG_FMT = DateTimeUtils.createFormatter("yyyyMMddHHmmssSSS", DateTimeZone.UTC);
	private static final DateTimeFormatter ISO_DATE_FMT = DateTimeUtils.createFormatter("yyyyMMdd", DateTimeZone.UTC);
	private static final DateTimeFormatter ISO_DATETIME_FMT = DateTimeUtils.createFormatter("yyyyMMdd'T'HHmmss'Z'", DateTimeZone.UTC);
	
	@Override
	public Response getFolders() {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		TasksManager manager = getManager();
		List<SyncFolder> items = new ArrayList<>();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getFolders()", currentProfileId);
		}
		
		try {
			Map<Integer, Category> cats = manager.listCategories();
			Map<Integer, DateTime> revisions = manager.getCategoriesLastRevision(cats.keySet());
			for (Category cat : cats.values()) {
				if (Category.Sync.OFF.equals(cat.getSync())) continue;
				
				items.add(createSyncFolder(currentProfileId, cat, revisions.get(cat.getCategoryId()), null, ShareFolderCategory.realElementsPerms(cat.getSync())));
			}
			
			List<ShareRootCategory> shareRoots = manager.listIncomingCategoryRoots();
			for (ShareRootCategory shareRoot : shareRoots) {
				Map<Integer, ShareFolderCategory> folders = manager.listIncomingCategoryFolders(shareRoot.getShareId());
				revisions = manager.getCategoriesLastRevision(folders.keySet());
				Map<Integer, CategoryPropSet> props = manager.getCategoriesCustomProps(folders.keySet());
				
				for (ShareFolderCategory folder : folders.values()) {
					Category cat = folder.getCategory();
					CategoryPropSet catProps = props.get(cat.getCategoryId());
					if (Category.Sync.OFF.equals(catProps.getSyncOrDefault(Category.Sync.OFF))) continue;
					
					items.add(createSyncFolder(currentProfileId, cat, revisions.get(cat.getCategoryId()), folder.getPerms(), folder.getRealElementsPerms(catProps.getSync())));
				}
			}
			
			return respOk(items);
			
		} catch(Throwable t) {
			logger.error("[{}] getFolders()", currentProfileId, t);
			return respError(t);
		}
	}

	@Override
	public Response getMessagesStats(Integer folderId, String cutoffDate) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getMessagesStats({}, {})", RunContext.getRunProfileId(), folderId, cutoffDate);
		}
		
		try {
			Category cat = manager.getCategory(folderId);
			if (cat == null) return respErrorBadRequest();
			//TODO: maybe check if passed folder is set to OFF
			
			//DateTime since = DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, cutoffDate);
			//if (since == null) DateTimeUtils.now().minusDays(30).withTimeAtStartOfDay();
			
			List<SyncTaskStat> items = new ArrayList<>();
			List<TaskObject> objs = manager.listTaskObjects(folderId, TaskObjectOutputType.STAT);
			for (TaskObject obj : objs) {
				items.add(createSyncTaskStat(obj));
			}
			return respOk(items);
			
		} catch(Throwable t) {
			logger.error("[{}] getMessagesStats({})", RunContext.getRunProfileId(), folderId, t);
			return respError(t);
		}
	}
	
	@Override
	public Response getMessage(Integer folderId, Integer id) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getMessage({}, {})", RunContext.getRunProfileId(), folderId, id);
		}
		
		try {
			Category cal = manager.getCategory(folderId);
			if (cal == null) return respErrorBadRequest();
			///TODO: maybe check if passed folder is set to OFF
			
			TaskObjectWithBean obj = (TaskObjectWithBean)manager.getTaskObject(folderId, id, TaskObjectOutputType.BEAN);
			if (obj != null) {
				return respOk(createSyncTask(obj));
			} else {
				return respErrorNotFound();
			}
			
		} catch(Throwable t) {
			logger.error("[{}] getMessage({}, {})", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}

	@Override
	public Response addMessage(Integer folderId, SyncTaskUpdate body) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] addMessage({}, ...)", RunContext.getRunProfileId(), folderId);
			logger.debug("{}", body);
		}
		
		try {
			//TODO: maybe check if passed folder is set to OFF
			Task newTask = mergeTask(new Task(), body);
			newTask.setCategoryId(folderId);
			
			Task task = manager.addTask(newTask);
			TaskObject obj = manager.getTaskObject(folderId, task.getTaskId(), TaskObjectOutputType.STAT);
			if (obj == null) return respErrorNotFound();
			
			return respOkCreated(createSyncTaskStat(obj));
			
		} catch(Throwable t) {
			logger.error("[{}] addMessage({}, ...)", RunContext.getRunProfileId(), folderId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateMessage(Integer folderId, Integer id, SyncTaskUpdate body) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] updateMessage({}, {}, ...)", RunContext.getRunProfileId(), folderId, id);
			logger.debug("{}", body);
		}
		
		try {
			//TODO: maybe check if passed folder is set to OFF
			Task task = manager.getTask(id);
			if (task == null) return respErrorNotFound();
			
			mergeTask(task, body);
			manager.updateTask(task, false);
			TaskObject obj = manager.getTaskObject(folderId, id, TaskObjectOutputType.STAT);
			if (obj == null) return respErrorNotFound();
			
			return respOk(createSyncTaskStats(Arrays.asList(obj)));
			
		} catch(Throwable t) {
			logger.error("[{}] updateMessage({}, {}, ...)", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteMessage(Integer folderId, Integer id) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] deleteMessage({}, {})", RunContext.getRunProfileId(), folderId, id);
		}
		
		try {
			//TODO: maybe check if passed folder is set to OFF
			manager.deleteTask(id);
			return respOkNoContent();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
			logger.error("[{}] deleteMessage({}, {})", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}
	
	private SyncFolder createSyncFolder(UserProfileId currentProfileId, Category cat, DateTime lastRevisionTimestamp, SharePerms folderPerms, SharePerms elementPerms) {
		String displayName = cat.getName();
		if (!currentProfileId.equals(cat.getProfileId())) {
			UserProfile.Data owud = WT.getUserData(cat.getProfileId());
			//String apn = LangUtils.abbreviatePersonalName(false, owud.getDisplayName());
			displayName = "[" + owud.getDisplayName() + "] " + displayName;
		}
		//String ownerUsername = owud.getProfileEmailAddress();
		
		return new SyncFolder()
				.id(cat.getCategoryId())
				.displayName(displayName)
				.etag(buildEtag(lastRevisionTimestamp))
				.deflt(cat.getIsDefault())
				.foAcl((folderPerms == null) ? SharePermsFolder.full().toString() : folderPerms.toString())
				.elAcl((elementPerms == null) ? SharePermsElements.full().toString() : elementPerms.toString())
				.ownerId(cat.getProfileId().toString());
	}
	
	private List<SyncTaskStat> createSyncTaskStats(Collection<TaskObject> objs) {
		ArrayList<SyncTaskStat> stats = new ArrayList<>(objs.size());
		objs.forEach(evtobj -> stats.add(createSyncTaskStat(evtobj)));
		return stats;
	}
	
	private SyncTaskStat createSyncTaskStat(TaskObject obj) {
		return new SyncTaskStat()
				.id(obj.getTaskId())
				.etag(buildEtag(obj.getRevisionTimestamp()));
	}
	
	private SyncTask createSyncTask(TaskObjectWithBean obj) {
		Task task = obj.getTask();
		
		return new SyncTask()
				.id(obj.getTaskId())
				.etag(buildEtag(obj.getRevisionTimestamp()))
				.subject(task.getSubject())
				.start(DateTimeUtils.print(ISO_DATETIME_FMT, task.getStartDate()))
				.due(DateTimeUtils.print(ISO_DATETIME_FMT, task.getDueDate()))
				.status(EnumUtils.toSerializedName(task.getStatus()))
				.complOn(DateTimeUtils.print(ISO_DATETIME_FMT, task.getCompletedDate()))
				.impo((int)task.getImportance())
				.prvt(task.getIsPrivate())
				//.reminder(event.getReminder())
				.notes(task.getDescription());
	}
	
	private <T extends Task> T mergeTask(T tgt, SyncTaskUpdate src) {
		boolean isNew = tgt.getTaskId() == null;
		
		tgt.setSubject(src.getSubject());
		tgt.setStartDate(DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getStart()));
		tgt.setDueDate(DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getDue()));
		DateTime complOn = DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getComplOn());
		if (complOn != null) {
			tgt.setCompletedDate(complOn);
			tgt.setStatus(BaseTask.Status.COMPLETED);
		}
		tgt.setImportance(src.getImpo().shortValue());
		tgt.setIsPrivate(src.isPrvt());
		//tgt.setReminder(src.getReminder());
		tgt.setDescription(src.getNotes());
		
		return tgt;
	}
	
	private String buildEtag(DateTime revisionTimestamp) {
		if (revisionTimestamp != null) {
			return ETAG_FMT.print(revisionTimestamp);
		} else {
			return DEFAULT_ETAG;
		}
	}
	
	private TasksManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private TasksManager getManager(UserProfileId targetProfileId) {
		return (TasksManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
