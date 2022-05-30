/*
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.rest.v1;

import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.model.SharePerms;
import com.sonicle.webtop.core.model.SharePermsElements;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.tasks.ManagerUtils;
import com.sonicle.webtop.tasks.TaskObjectOutputType;
import com.sonicle.webtop.tasks.TasksManager;
import com.sonicle.webtop.tasks.TasksServiceSettings;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.ShareFolderCategory;
import com.sonicle.webtop.tasks.model.ShareRootCategory;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskObjectChanged;
import com.sonicle.webtop.tasks.model.TaskObjectWithBean;
import com.sonicle.webtop.tasks.model.TaskObjectWithICalendar;
import com.sonicle.webtop.tasks.swagger.v1.api.DavApi;
import com.sonicle.webtop.tasks.swagger.v1.model.ApiError;
import com.sonicle.webtop.tasks.swagger.v1.model.DavFolder;
import com.sonicle.webtop.tasks.swagger.v1.model.DavFolderNew;
import com.sonicle.webtop.tasks.swagger.v1.model.DavFolderUpdate;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObject;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObjectChanged;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObjectPayload;
import com.sonicle.webtop.tasks.swagger.v1.model.DavObjectsChanges;
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
		List<DavFolder> items = new ArrayList<>();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] getDavFolders()", currentProfileId);
		}
		
		try {
			Map<Integer, Category> categories = manager.listMyCategories();
			Map<Integer, DateTime> revisions = manager.getCategoriesLastRevision(categories.keySet());
			for (Category category : categories.values()) {
				//if (cal.isProviderRemote()) continue;
				items.add(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), null, null));
			}
			
			List<ShareRootCategory> shareRoots = manager.listIncomingCategoryRoots();
			for (ShareRootCategory shareRoot : shareRoots) {
				Map<Integer, ShareFolderCategory> folders = manager.listIncomingCategoryFolders(shareRoot.getShareId());
				revisions = manager.getCategoriesLastRevision(folders.keySet());
				//Map<Integer, CategoryPropSet> props = manager.getCategoryCustomProps(folders.keySet());
				for (ShareFolderCategory folder : folders.values()) {
					Category category = folder.getCategory();
					//if (cal.isProviderRemote()) continue;
					items.add(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), folder.getPerms(), folder.getElementsPerms()));
				}
			}
			
			return respOk(items);
			
		} catch(Throwable t) {
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
			
			String rootShareId = manager.getIncomingCategoryShareRootId(categoryId);
			if (rootShareId != null) {
				Map<Integer, ShareFolderCategory> folders = manager.listIncomingCategoryFolders(rootShareId);
				ShareFolderCategory folder = folders.get(categoryId);
				return respOk(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), folder.getPerms(), folder.getElementsPerms()));
				
			} else {
				return respOk(createDavFolder(currentProfileId, category, revisions.get(category.getCategoryId()), null, null));
			}
			
		} catch(Throwable t) {
			LOGGER.error("[{}] getDavFolder({})", currentProfileId, folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response addDavFolder(DavFolderNew body) {
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
			return respOkCreated(createDavFolder(currentProfileId, category, null, null, null));
			
		} catch(Throwable t) {
			LOGGER.error("[{}] addDavFolder(...)", currentProfileId, t);
			return respError(t);
		}
	}

	@Override
	public Response updateDavFolder(String folderUid, DavFolderUpdate body) {
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
			manager.updateCategory(category);
			return respOk();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
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
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
			LOGGER.error("[{}] deleteDavFolder({})", RunContext.getRunProfileId(), folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response getDavObjects(String folderUid, List<String> hrefs, String format) {
		TasksManager manager = getManager();
		List<DavObject> items = new ArrayList<>();
		
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
			
		} catch(Throwable t) {
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
			
		} catch(Throwable t) {
			LOGGER.error("[{}] getDavObject({}, {})", RunContext.getRunProfileId(), folderUid, href, t);
			return respError(t);
		}
	}

	@Override
	public Response addDavObject(String folderUid, DavObjectPayload body) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] addDavObject({}, ...)", RunContext.getRunProfileId(), folderUid);
			LOGGER.debug("{}", body);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			// Manager's call is already ro protected for remoteProviders
			if (DavObjectPayload.FormatEnum.JSON.equals(body.getFormat())) {
				return respErrorBadRequest("Sorry but JSON format is not supported yet!");
			} else {
				net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body.getData());
				manager.addTaskObject(categoryId, body.getHref(), iCalendar);
			}
			return respOkCreated();
			
		} catch(Throwable t) {
			LOGGER.error("[{}] addDavObject({}, ...)", RunContext.getRunProfileId(), folderUid, t);
			return respError(t);
		}
	}

	@Override
	public Response updateDavObject(String folderUid, String href, DavObjectPayload body) {
		TasksManager manager = getManager();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] updateDavObject({}, {}, ...)", RunContext.getRunProfileId(), folderUid, href);
			LOGGER.debug("{}", body);
		}
		
		try {
			int categoryId = ManagerUtils.decodeAsTaskFolderId(folderUid);
			// Manager's call is already ro protected for remoteProviders
			if (DavObjectPayload.FormatEnum.JSON.equals(body.getFormat())) {
				return respErrorBadRequest("Sorry but JSON format is not supported yet!");
			} else {
				net.fortuna.ical4j.model.Calendar iCalendar = parseICalendar(body.getData());
				manager.updateTaskObject(categoryId, href, iCalendar);
			}
			return respOkNoContent();
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
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
			
		} catch(WTNotFoundException ex) {
			return respErrorNotFound();
		} catch(Throwable t) {
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
			
			LangUtils.CollectionChangeSet<TaskObjectChanged> changes = manager.listTaskObjectsChanges(categoryId, since, limit);
			return respOk(createDavObjectsChanges(revisions.get(categoryId), changes));
			
		} catch(Throwable t) {
			LOGGER.error("[{}] getDavObjectsChanges({}, {}, {})", RunContext.getRunProfileId(), folderUid, syncToken, limit, t);
			return respError(t);
		}
	}
	
	private DavFolder createDavFolder(UserProfileId currentProfileId, Category cat, DateTime lastRevisionTimestamp, SharePerms folderPerms, SharePerms elementPerms) {
		UserProfile.Data owud = WT.getUserData(cat.getProfileId());
		
		String displayName = cat.getName();
		if (!currentProfileId.equals(cat.getProfileId())) {
			//String apn = LangUtils.abbreviatePersonalName(false, owud.getDisplayName());
			displayName = "[" + owud.getDisplayName() + "] " + displayName;
		}
		String ownerUsername = owud.getProfileEmailAddress();
		
		return new DavFolder()
				.id(cat.getCategoryId())
				.uid(ManagerUtils.encodeAsTaskFolderUid(cat.getCategoryId()))
				.name(cat.getName())
				.description(cat.getDescription())
				.color(cat.getColor())
				.syncToken(buildEtag(lastRevisionTimestamp))
				.aclFol((folderPerms == null) ? SharePermsFolder.full().toString() : folderPerms.toString())
				.aclEle((elementPerms == null) ? SharePermsElements.full().toString() : elementPerms.toString())
				.ownerUsername(ownerUsername)
				.displayName(displayName);
	}
	
	private DavObject createDavObject(TaskObject obj) {
		DavObject ret = new DavObject()
				.id(obj.getTaskId())
				.uid(obj.getPublicUid())
				.href(obj.getHref())
				.lastModified(obj.getRevisionTimestamp().withZone(DateTimeZone.UTC).getMillis()/1000)
				.etag(buildEtag(obj.getRevisionTimestamp()));
		
		if (obj instanceof TaskObjectWithICalendar) {
			TaskObjectWithICalendar objwi = (TaskObjectWithICalendar)obj;
			return ret.size(objwi.getSize())
				.data(objwi.getIcalendar())
				.format(DavObject.FormatEnum.ICALENDAR);
		} else if (obj instanceof TaskObjectWithBean) {
			return ret;
		} else {
			return ret;
		}
	}
	
	private DavObjectChanged createDavObjectChanged(TaskObjectChanged obj) {
		return new DavObjectChanged()
				.id(obj.getTaskId())
				.href(obj.getHref())
				.etag(buildEtag(obj.getRevisionTimestamp()));
	}
	
	private DavObjectsChanges createDavObjectsChanges(DateTime lastRevisionTimestamp, LangUtils.CollectionChangeSet<TaskObjectChanged> changes) {
		ArrayList<DavObjectChanged> inserted = new ArrayList<>();
		for (TaskObjectChanged calObj : changes.inserted) {
			inserted.add(createDavObjectChanged(calObj));
		}
		
		ArrayList<DavObjectChanged> updated = new ArrayList<>();
		for (TaskObjectChanged calObj : changes.updated) {
			updated.add(createDavObjectChanged(calObj));
		}
		
		ArrayList<DavObjectChanged> deleted = new ArrayList<>();
		for (TaskObjectChanged calObj : changes.deleted) {
			deleted.add(createDavObjectChanged(calObj));
		}
		
		return new DavObjectsChanges()
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
	
	private DavObjectPayload.FormatEnum parseFormat(String format, DavObjectPayload.FormatEnum defaultFormat) {
		DavObjectPayload.FormatEnum fe = DavObjectPayload.FormatEnum.fromValue(format);
		return fe == null ? defaultFormat : fe;
	}
	
	private TaskObjectOutputType formatToOutputType(DavObjectPayload.FormatEnum format, TaskObjectOutputType defaultOutputType) {
		if (DavObjectPayload.FormatEnum.ICALENDAR.equals(format)) {
			return TaskObjectOutputType.ICALENDAR;
		} else if (DavObjectPayload.FormatEnum.JSON.equals(format)) {
			return TaskObjectOutputType.BEAN;
		} else {
			return defaultOutputType;
		}
	}
	
	private TaskObjectOutputType formatToOutputType(String format, TaskObjectOutputType defaultOutputType) {
		return formatToOutputType(DavObjectPayload.FormatEnum.fromValue(format), defaultOutputType);
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
