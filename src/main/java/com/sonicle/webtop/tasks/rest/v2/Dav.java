/*
 * Copyright (C) 2024 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2024 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.rest.v2;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.model.ChangedItem;
import com.sonicle.webtop.core.model.Delta;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.tasks.ManagerUtils;
import com.sonicle.webtop.tasks.TaskObjectOutputType;
import com.sonicle.webtop.tasks.TasksManager;
import com.sonicle.webtop.tasks.TasksServiceSettings;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFSFolder;
import com.sonicle.webtop.tasks.model.CategoryFSOrigin;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskObjectWithBean;
import com.sonicle.webtop.tasks.model.TaskObjectWithICalendar;
import com.sonicle.webtop.tasks.swagger.v2.api.DavApi;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavFolder;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavFolderNew;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavFolderUpdate;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObject;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObjectChanged;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObjectPayload;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiDavObjectsChanges;
import com.sonicle.webtop.tasks.swagger.v2.model.ApiError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import net.fortuna.ical4j.data.ParserException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author malbinola
 */
public class Dav extends DavApi {
	private static final Logger LOGGER = LoggerFactory.getLogger(Dav.class);
	private static final String DEFAULT_ETAG = "19700101000000000";
	private static final DateTimeFormatter ETAG_FORMATTER = DateTimeUtils.createFormatter("yyyyMMddHHmmssSSS", DateTimeZone.UTC);

	@Override
	public Response getDavFolders() {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		TasksManager manager = getManager();
		List<ApiDavFolder> items = new ArrayList<>();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDavFolders()", currentProfileId);
		}
		
		try {
			Map<Integer, Category> categories = manager.listMyCategories();
			Map<Integer, DateTime> revisions = manager.getCategoriesLastRevision(categories.keySet());
			for (Category category : categories.values()) {
				//if (cal.isProviderRemote()) continue;
				items.add(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), FolderShare.Permissions.full()));
			}
			for (CategoryFSOrigin origin : manager.listIncomingCategoryOrigins().values()) {
				Map<Integer, CategoryFSFolder> folders = manager.listIncomingCategoryFolders(origin);
				revisions = manager.getCategoriesLastRevision(folders.keySet());
				for (CategoryFSFolder folder : folders.values()) {
					Category category = folder.getCategory();
					//if (category.isProviderRemote()) continue;
					items.add(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), folder.getPermissions()));
				}
			}
			
			return respOk(items);
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getDavFolders()", currentProfileId, t);
			return respError(t);
		}
	}

	@Override
	public Response getDavFolder(String folderUid) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDavFolder({})", currentProfileId, folderUid);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			Category category = manager.getCategory(categoryId);
			if (category == null) return respErrorNotFound();
			//if (cal.isProviderRemote()) return respErrorBadRequest();
			
			Map<Integer, DateTime> revisions = manager.getCategoriesLastRevision(Arrays.asList(category.getCategoryId()));
			CategoryFSOrigin origin = manager.getIncomingCategoryOriginByFolderId(categoryId);
			if (origin != null) {
				Map<Integer, CategoryFSFolder> folders = manager.listIncomingCategoryFolders(origin);
				return respOk(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), folders.get(categoryId).getPermissions()));
			} else {
				return respOk(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), FolderShare.Permissions.full()));
			}
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getDavFolder({})", currentProfileId, folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response addDavFolder(ApiDavFolderNew body) {
		UserProfileId currentProfileId = RunContext.getRunProfileId();
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addDavFolder(...)", currentProfileId);
			LOGGER.debug("{}", body);
		}
		
		try {
			Category category = new Category();
			category.setName(body.getName());
			category.setDescription(body.getDescription());
			category = manager.addCategory(category);
			// Calendars are always added in currentProfile so we do not handle perms here (passing null = full rights)
			return respOkCreated(createDavFolder(currentProfileId, category, null, FolderShare.Permissions.full()));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] addDavFolder(...)", currentProfileId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateDavFolder(String folderUid, ApiDavFolderUpdate body) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateDavFolder({}, ...)", RunContext.getRunProfileId(), folderUid);
			LOGGER.debug("{}", body);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			Category category = manager.getCategory(categoryId);
			if (category == null) return respErrorNotFound();
			//if (cal.isProviderRemote()) return respErrorBadRequest();
			
			if (body.getUpdatedFields().contains("name")) {
				category.setName(body.getName());
			}
			if (body.getUpdatedFields().contains("description")) {
				category.setDescription(body.getDescription());
			}
			if (body.getUpdatedFields().contains("color")) {
				category.setColor(body.getColor());
			}
			manager.updateCategory(categoryId, category);
			return respOk();
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Throwable t) {
			LOGGER.error("[{}] updateDavFolder({}, ...)", RunContext.getRunProfileId(), folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteDavFolder(String folderUid) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteDavFolder({})", RunContext.getRunProfileId(), folderUid);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			TasksServiceSettings css = new TasksServiceSettings(SERVICE_ID, RunContext.getRunProfileId().getDomainId());
			if (css.getDavCategoryDeleteEnabled()) {
				manager.deleteCategory(categoryId);
				return respOkNoContent();
			} else {
				return respErrorNotAllowed();
			}
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Throwable t) {
			LOGGER.error("[{}] deleteDavFolder({})", RunContext.getRunProfileId(), folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response getDavObjects(String folderUid, List<String> hrefs, String format) {
		TasksManager manager = getManager();
		List<ApiDavObject> items = new ArrayList<>();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDavObjects({})", RunContext.getRunProfileId(), folderUid);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			Category category = manager.getCategory(categoryId);
			if (category == null) return respErrorBadRequest();
			//if (category.isProviderRemote()) return respErrorBadRequest();
			
			TaskObjectOutputType outputType = formatToOutputType(format, TaskObjectOutputType.ICALENDAR);
			if (TaskObjectOutputType.BEAN.equals(outputType)) {
				return respErrorBadRequest("Sorry but JSON format is not supported yet!");
			}
			
			if ((hrefs == null) || hrefs.isEmpty()) {
				List<TaskObject> objs = manager.listTaskObjects(categoryId, outputType);
				for (TaskObject obj : objs) {
					items.add(createDavObject(obj));
				}
				return respOk(items);
				
			} else {
				List<TaskObject> objs = manager.getTaskObjects(categoryId, hrefs, outputType);
				for (TaskObject obj : objs) {
					items.add(createDavObject(obj));
				}
				return respOk(items);
			}
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getDavObjects({})", RunContext.getRunProfileId(), folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response getDavObject(String folderUid, String href, String format) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDavObject({}, {})", RunContext.getRunProfileId(), folderUid, href);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			Category category = manager.getCategory(categoryId);
			if (category == null) return respErrorBadRequest();
			//if (category.isProviderRemote()) return respErrorBadRequest();
			
			TaskObjectOutputType outputType = formatToOutputType(format, TaskObjectOutputType.ICALENDAR);
			if (TaskObjectOutputType.BEAN.equals(outputType)) {
				return respErrorBadRequest("Sorry but JSON format is not supported yet!");
			}
			
			TaskObject object = manager.getTaskObject(categoryId, href, outputType);
			if (object != null) return respOk(createDavObject(object));
			return respErrorNotFound();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getDavObject({}, {})", RunContext.getRunProfileId(), folderUid, href, t);
			return respError(t);
		}
	}

	@Override
	public Response addDavObject(String folderUid, ApiDavObjectPayload body) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addDavObject({}, ...)", RunContext.getRunProfileId(), folderUid);
			LOGGER.debug("{}", body);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			// Manager's call is already ro protected for remoteProviders
			if (ApiDavObjectPayload.FormatEnum.JSON.equals(body.getFormat())) {
				return respErrorBadRequest("Sorry but JSON format is not supported yet!");
			} else {
				net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body.getData());
				manager.addTaskObject(categoryId, body.getHref(), iCalendar);
			}
			return respOkCreated();
			
		} catch (Throwable t) {
			LOGGER.error("[{}] addDavObject({}, ...)", RunContext.getRunProfileId(), folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response updateDavObject(String folderUid, String href, ApiDavObjectPayload body) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateDavObject({}, {}, ...)", RunContext.getRunProfileId(), folderUid, href);
			LOGGER.debug("{}", body);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			// Manager's call is already ro protected for remoteProviders
			if (ApiDavObjectPayload.FormatEnum.JSON.equals(body.getFormat())) {
				return respErrorBadRequest("Sorry but JSON format is not supported yet!");
			} else {
				net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body.getData());
				manager.updateTaskObject(categoryId, href, iCalendar);
			}
			return respOkNoContent();
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Throwable t) {
			LOGGER.error("[{}] updateDavObject({}, {}, ...)", RunContext.getRunProfileId(), folderUid, href, t);
			return respError(t);
		}
	}

	@Override
	public Response deleteDavObject(String folderUid, String href) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] deleteDavObject({}, {})", RunContext.getRunProfileId(), folderUid, href);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			manager.deleteTaskObject(categoryId, href);
			return respOkNoContent();
			
		} catch (WTNotFoundException ex) {
			return respErrorNotFound();
		} catch (Throwable t) {
			LOGGER.error("[{}] deleteDavObject({}, {})", RunContext.getRunProfileId(), folderUid, href, t);
			return respError(t);
		}
	}

	@Override
	public Response getDavObjectsChanges(String folderUid, String syncToken, Integer limit) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDavObjectsChanges({}, {}, {})", RunContext.getRunProfileId(), folderUid, syncToken, limit);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			com.sonicle.webtop.tasks.model.Category cal = manager.getCategory(categoryId);
			if (cal == null) return respErrorNotFound();
			//if (cal.isProviderRemote()) return respErrorBadRequest();
			
			Map<Integer, DateTime> revisions = manager.getCategoriesLastRevision(Arrays.asList(categoryId));
			
			DateTime since = null;
			if (!StringUtils.isBlank(syncToken)) {
				since = ETAG_FORMATTER.parseDateTime(syncToken);
				if (since == null) return respErrorBadRequest();
			}
			
			Delta<TaskObject> delta = manager.listTasksDelta(categoryId, since, TaskObjectOutputType.STAT);
			return respOk(createDavObjectsChanges(revisions.get(categoryId), delta));
			
		} catch (Throwable t) {
			LOGGER.error("[{}] getDavObjectsChanges({}, {}, {})", RunContext.getRunProfileId(), folderUid, syncToken, limit, t);
			return respError(t);
		}
	}
	
	private ApiDavFolder createDavFolder(UserProfileId currentProfileId, Category cat, DateTime lastRevisionTimestamp, FolderShare.Permissions permissions) {
		UserProfile.Data owud = WT.getUserData(cat.getProfileId());
		
		String displayName = cat.getName();
		if (!currentProfileId.equals(cat.getProfileId())) {
			//String apn = LangUtils.abbreviatePersonalName(false, owud.getDisplayName());
			displayName = "[" + owud.getDisplayName() + "] " + displayName;
		}
		String ownerUsername = owud.getProfileEmailAddress();
		
		return new ApiDavFolder()
			.id(String.valueOf(cat.getCategoryId()))
			.uid(ManagerUtils.encodeAsTaskFolderUid(cat.getCategoryId()))
			.name(cat.getName())
			.description(cat.getDescription())
			.color(cat.getColor())
			.syncToken(buildEtag(lastRevisionTimestamp))
			.aclFol(permissions.getFolderPermissions().toString())
			.aclEle(permissions.getItemsPermissions().toString())
			.ownerUsername(ownerUsername)
			.displayName(displayName);
	}
	
	private ApiDavObject createDavObject(TaskObject obj) {
		ApiDavObject ret = new ApiDavObject()
			.id(obj.getTaskId())
			.uid(obj.getPublicUid())
			.href(obj.getHref())
			.lastModified(obj.getRevisionTimestamp().withZone(DateTimeZone.UTC).getMillis()/1000)
			.etag(buildEtag(obj.getRevisionTimestamp()));
		
		if (obj instanceof TaskObjectWithICalendar) {
			TaskObjectWithICalendar objwi = (TaskObjectWithICalendar)obj;
			return ret.size(objwi.getSize())
				.data(objwi.getIcalendar())
				.format(ApiDavObject.FormatEnum.ICALENDAR);
		} else if (obj instanceof TaskObjectWithBean) {
			return ret;
		} else {
			return ret;
		}
	}
	
	private ApiDavObjectChanged createDavObjectChanged(TaskObject taskObject) {
		return new ApiDavObjectChanged()
			.id(taskObject.getTaskId())
			.href(taskObject.getHref())
			.etag(buildEtag(taskObject.getRevisionTimestamp()));
	}
	
	private ApiDavObjectsChanges createDavObjectsChanges(DateTime lastRevisionTimestamp, Delta<TaskObject> delta) {
		ArrayList<ApiDavObjectChanged> inserted = new ArrayList<>();
		ArrayList<ApiDavObjectChanged> updated = new ArrayList<>();
		ArrayList<ApiDavObjectChanged> deleted = new ArrayList<>();
		
		for (ChangedItem<TaskObject> item : delta.getItems()) {
			if (ChangedItem.ChangeType.ADDED.equals(item.getChangeType())) {
				inserted.add(createDavObjectChanged(item.getObject()));
			} else if (ChangedItem.ChangeType.DELETED.equals(item.getChangeType())) {
				deleted.add(createDavObjectChanged(item.getObject()));
			} else {
				updated.add(createDavObjectChanged(item.getObject()));
			}
		}
		
		return new ApiDavObjectsChanges()
			.syncToken(buildEtag(lastRevisionTimestamp))
			.inserted(inserted)
			.updated(updated)
			.deleted(deleted);
	}
	
	private net.fortuna.ical4j.model.Calendar parseICalendar(String s) throws WTException {
		try {
			return ICalendarUtils.parse(s);
		} catch(IOException | ParserException ex) {
			throw new WTException(ex, "Unable to parse icalendar data");
		}
	}
	
	private String buildEtag(DateTime revisionTimestamp) {
		if (revisionTimestamp != null) {
			return ETAG_FORMATTER.print(revisionTimestamp);
		} else {
			return DEFAULT_ETAG;
		}
	}
	
	private ApiDavObjectPayload.FormatEnum parseFormat(String format, ApiDavObjectPayload.FormatEnum defaultFormat) {
		ApiDavObjectPayload.FormatEnum fe = ApiDavObjectPayload.FormatEnum.fromValue(format);
		return fe == null ? defaultFormat : fe;
	}
	
	private TaskObjectOutputType formatToOutputType(ApiDavObjectPayload.FormatEnum format, TaskObjectOutputType defaultOutputType) {
		if (ApiDavObjectPayload.FormatEnum.ICALENDAR.equals(format)) {
			return TaskObjectOutputType.ICALENDAR;
		} else if (ApiDavObjectPayload.FormatEnum.JSON.equals(format)) {
			return TaskObjectOutputType.BEAN;
		} else {
			return defaultOutputType;
		}
	}
	
	private TaskObjectOutputType formatToOutputType(String format, TaskObjectOutputType defaultOutputType) {
		return formatToOutputType(ApiDavObjectPayload.FormatEnum.fromValue(format), defaultOutputType);
	}
	
	private TasksManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private TasksManager getManager(UserProfileId targetProfileId) {
		TasksManager manager = (TasksManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
		manager.setSoftwareName("rest-dav");
		return manager;
	}

	@Override
	protected Object createErrorEntity(Response.Status status, String message) {
		return new ApiError()
			.code(status.getStatusCode())
			.description(message);
	}
}
