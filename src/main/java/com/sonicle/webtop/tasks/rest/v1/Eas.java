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
import com.sonicle.commons.flags.BitFlags;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.tasks.ITasksManager.TaskUpdateOption;
import com.sonicle.webtop.tasks.TaskObjectOutputType;
import com.sonicle.webtop.tasks.TasksManager;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFSFolder;
import com.sonicle.webtop.tasks.model.CategoryFSOrigin;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskEx;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
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
			Integer defltCategoryId = manager.getDefaultCategoryId();
			Map<Integer, Category> cats = manager.listMyCategories();
			Map<Integer, DateTime> revisions = manager.getCategoriesLastRevision(cats.keySet());
			for (Category category : cats.values()) {
				if (Category.Sync.OFF.equals(category.getSync())) continue;
				
				final boolean isDefault = category.getCategoryId().equals(defltCategoryId);
				final FolderShare.Permissions permissions = Category.Sync.READ.equals(category.getSync()) ? FolderShare.Permissions.fullFolderOnly() : FolderShare.Permissions.full();
				items.add(createSyncFolder(currentProfileId, category, revisions.get(category.getCategoryId()), permissions, isDefault));
			}
			
			for (CategoryFSOrigin origin : manager.listIncomingCategoryOrigins().values()) {
				Map<Integer, CategoryFSFolder> folders = manager.listIncomingCategoryFolders(origin);
				Map<Integer, CategoryPropSet> folderProps = manager.getCategoriesCustomProps(folders.keySet());
				revisions = manager.getCategoriesLastRevision(folders.keySet());
				for (CategoryFSFolder folder : folders.values()) {
					Category category = folder.getCategory();
					CategoryPropSet props = folderProps.get(category.getCategoryId());
					if (Category.Sync.OFF.equals(props.getSyncOrDefault(Category.Sync.OFF))) continue;
					
					final boolean isDefault = category.getCategoryId().equals(defltCategoryId);
					final FolderShare.Permissions permissions = Category.Sync.READ.equals(props.getSync()) ? FolderShare.Permissions.withFolderPermissionsOnly(folder.getPermissions()) : folder.getPermissions();
					items.add(createSyncFolder(currentProfileId, category, revisions.get(category.getCategoryId()), permissions, isDefault));
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
	public Response getMessage(Integer folderId, String id) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] getMessage({}, {})", RunContext.getRunProfileId(), folderId, id);
		}
		
		try {
			Category cal = manager.getCategory(folderId);
			if (cal == null) return respErrorBadRequest();
			///TODO: maybe check if passed folder is set to OFF
			
			TaskInstanceId iid = TaskInstanceId.buildMaster(id);
			TaskObjectWithBean obj = (TaskObjectWithBean)manager.getTaskObject(iid, TaskObjectOutputType.BEAN);
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
			TaskEx newTask = mergeTask(true, new TaskEx(), body);
			newTask.setCategoryId(folderId);
			
			Task task = manager.addTask(newTask);
			TaskInstanceId iid = TaskInstanceId.buildMaster(task.getTaskId());
			TaskObject obj = manager.getTaskObject(iid, TaskObjectOutputType.STAT);
			if (obj == null) return respErrorNotFound();
			
			return respOkCreated(createSyncTaskStat(obj));
			
		} catch(Throwable t) {
			logger.error("[{}] addMessage({}, ...)", RunContext.getRunProfileId(), folderId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateMessage(Integer folderId, String id, SyncTaskUpdate body) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] updateMessage({}, {}, ...)", RunContext.getRunProfileId(), folderId, id);
			logger.debug("{}", body);
		}
		
		try {
			//TODO: maybe check if passed folder is set to OFF
			TaskInstanceId iid = TaskInstanceId.buildMaster(id);
			TaskObjectWithBean objwb = (TaskObjectWithBean)manager.getTaskObject(iid, TaskObjectOutputType.BEAN);
			if (objwb == null) return respErrorNotFound();
			
			TaskEx task = mergeTask(false, objwb.getTask(), body);
			BitFlags<TaskUpdateOption> options = BitFlags.noneOf(TaskUpdateOption.class);
			manager.updateTaskInstance(iid, task, options);
			
			TaskObject obj = manager.getTaskObject(iid, TaskObjectOutputType.STAT);
			if (obj == null) return respErrorNotFound();
			
			return respOk(createSyncTaskStats(Arrays.asList(obj)));
			
		} catch(Throwable t) {
			logger.error("[{}] updateMessage({}, {}, ...)", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteMessage(Integer folderId, String id) {
		TasksManager manager = getManager();
		
		if (logger.isDebugEnabled()) {
			logger.debug("[{}] deleteMessage({}, {})", RunContext.getRunProfileId(), folderId, id);
		}
		
		try {
			//TODO: maybe check if passed folder is set to OFF
			TaskInstanceId iid = TaskInstanceId.buildMaster(id);
			manager.deleteTaskInstance(iid);
			return respOkNoContent();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
			logger.error("[{}] deleteMessage({}, {})", RunContext.getRunProfileId(), folderId, id, t);
			return respError(t);
		}
	}
	
	private SyncFolder createSyncFolder(UserProfileId currentProfileId, Category cat, DateTime lastRevisionTimestamp, FolderShare.Permissions permissions, boolean isDefault) {
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
				.deflt(isDefault)
				.foAcl(permissions.getFolderPermissions().toString())
				.elAcl(permissions.getItemsPermissions().toString())
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
		TaskEx task = obj.getTask();
		
		return new SyncTask()
				.id(obj.getTaskId())
				.etag(buildEtag(obj.getRevisionTimestamp()))
				.subject(task.getSubject())
				.start(DateTimeUtils.print(ISO_DATETIME_FMT, task.getStart()))
				.due(DateTimeUtils.print(ISO_DATETIME_FMT, task.getDue()))
				.status(EnumUtils.toSerializedName(task.getStatus()))
				.complOn(DateTimeUtils.print(ISO_DATETIME_FMT, task.getCompletedOn()))
				.impo((int)task.getImportance())
				.prvt(task.getIsPrivate())
				//.reminder(task.getReminder())
				.notes(task.getDescription());
	}
	
	private <T extends TaskEx> T mergeTask(boolean isNew, T tgt, SyncTaskUpdate src) {
		tgt.setSubject(src.getSubject());
		tgt.setStart(DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getStart()));
		tgt.setDue(DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getDue()));
		DateTime complOn = DateTimeUtils.parseDateTime(ISO_DATETIME_FMT, src.getComplOn());
		if (complOn != null) {
			tgt.setCompletedOn(complOn);
			tgt.setStatus(TaskBase.Status.COMPLETED);
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
		TasksManager manager = (TasksManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
		manager.setSoftwareName("rest-eas");
		return manager;
	}
	
	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
				.code(status.getStatusCode())
				.description(message);
	}
}
