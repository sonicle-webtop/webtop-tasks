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
package com.sonicle.webtop.tasks;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sonicle.commons.BitFlag;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.cache.AbstractPassiveExpiringBulkMap;
import com.sonicle.commons.qbuilders.conditions.Condition;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.ServletUtils.StringArray;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.bean.IntegerSet;
import com.sonicle.commons.web.json.bean.QueryObj;
import com.sonicle.commons.web.json.bean.StringSet;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.commons.web.json.extjs.GridMetadata;
import com.sonicle.commons.web.json.extjs.SortMeta;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.core.app.CoreManifest;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.tasks.bol.js.JsFolderNode;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.app.model.FolderShare;
import com.sonicle.webtop.core.app.model.FolderSharing;
import com.sonicle.webtop.core.app.sdk.AbstractFolderTreeCache;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.app.util.log.LogEntry;
import com.sonicle.webtop.core.app.util.log.LogHandler;
import com.sonicle.webtop.core.bol.js.JsCustomFieldDefsData;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsWizardData;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldDefs;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldEx;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.ServiceMessage;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.util.RRuleStringify;
import com.sonicle.webtop.tasks.bol.js.JsCategory;
import com.sonicle.webtop.tasks.bol.js.JsCategoryLkp;
import com.sonicle.webtop.tasks.bol.js.JsCategorySharing;
import com.sonicle.webtop.tasks.bol.js.JsGridTask;
import com.sonicle.webtop.tasks.bol.js.JsPletTasks;
import com.sonicle.webtop.tasks.bol.js.JsTask;
import com.sonicle.webtop.tasks.bol.js.JsTaskPreview;
import com.sonicle.webtop.tasks.bol.model.CategoryNodeId;
import com.sonicle.webtop.tasks.bol.model.MyCategoryFSFolder;
import com.sonicle.webtop.tasks.bol.model.MyCategoryFSOrigin;
import com.sonicle.webtop.tasks.bol.model.RBTaskDetail;
import com.sonicle.webtop.tasks.bol.model.RBTaskList;
import com.sonicle.webtop.tasks.io.ICalendarInput;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFSFolder;
import com.sonicle.webtop.tasks.model.CategoryFSOrigin;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithBytes;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithStream;
import com.sonicle.webtop.tasks.model.TaskEx;
import com.sonicle.webtop.tasks.model.TaskInstance;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import com.sonicle.webtop.tasks.model.TaskLookupInstance;
import com.sonicle.webtop.tasks.model.TaskObjectWithICalendar;
import com.sonicle.webtop.tasks.model.TaskQuery;
import com.sonicle.webtop.tasks.msg.TaskImportLogSM;
import com.sonicle.webtop.tasks.rpt.RptTaskList;
import com.sonicle.webtop.tasks.rpt.RptTasksDetail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class Service extends BaseService {
	public static final Logger logger = WT.getLogger(Service.class);
	public static final String META_CONTEXT_SEARCH = "mainsearch";
	
	private TasksManager manager;
	private TasksServiceSettings ss;
	private TasksUserSettings us;
	
	private final KeyedReentrantLocks<String> locks = new KeyedReentrantLocks<>();
	private final SearchableCustomFieldTypeCache cacheSearchableCustomFieldType = new SearchableCustomFieldTypeCache(5, TimeUnit.SECONDS);
	private final FoldersTreeCache foldersTreeCache = new FoldersTreeCache();
	private final LoadingCache<Integer, Optional<CategoryPropSet>> foldersPropsCache = Caffeine.newBuilder().build(new FoldersPropsCacheLoader());
	private StringSet inactiveOrigins = null;
	private IntegerSet inactiveFolders = null;	
	
	@Override
	public void initialize() throws Exception {
		UserProfile up = getEnv().getProfile();
		manager = (TasksManager)WT.getServiceManager(SERVICE_ID);
		ss = new TasksServiceSettings(SERVICE_ID, up.getDomainId());
		us = new TasksUserSettings(SERVICE_ID, up.getId());
		initFolders();
		
		// Default lookup: if not yet configured this will implicitly set built-in folder as default!
		manager.getDefaultCategoryId();
	}
	
	@Override
	public void cleanup() throws Exception {
		if (inactiveFolders != null) inactiveFolders.clear();
		if (foldersTreeCache != null) foldersTreeCache.clear();
		us = null;
		ss = null;
		manager = null;
	}
	
	@Override
	public ServiceVars returnServiceVars() {
		ServiceVars co = new ServiceVars();
		co.put("gridView", EnumUtils.toSerializedName(us.getGridView()));
		co.put("defaultCategorySync", EnumUtils.toSerializedName(ss.getDefaultCategorySync()));
		co.put("cfieldsSearchable", LangUtils.serialize(getSearchableCustomFieldDefs(), ObjCustomFieldDefs.FieldsList.class));
		co.put("hasAudit", manager.isAuditEnabled() && (RunContext.isImpersonated() || RunContext.isPermitted(true, CoreManifest.ID, "AUDIT")));
		return co;
	}
	
	private ObjCustomFieldDefs.FieldsList getSearchableCustomFieldDefs() {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			ObjCustomFieldDefs.FieldsList scfields = new ObjCustomFieldDefs.FieldsList();
			for (CustomFieldEx cfield : coreMgr.listCustomFields(SERVICE_ID, BitFlag.of(CoreManager.CustomFieldListOptions.SEARCHABLE)).values()) {
				scfields.add(new ObjCustomFieldDefs.Field(cfield, up.getLanguageTag()));
			}
			return scfields;
			
		} catch(Throwable t) {
			return null;
		}
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	private void initFolders() throws WTException {
		foldersTreeCache.init();
		// Retrieves inactive origins set
		inactiveOrigins = us.getInactiveCategoryOrigins();
		if (inactiveOrigins.removeIf(key -> shouldCleanupInactiveOriginKey(key))) { // Clean-up orphans
			us.setInactiveCategoryOrigins(inactiveOrigins);
		}
		// Retrieves inactive folders set
		inactiveFolders = us.getInactiveCategoryFolders();
		if (inactiveFolders.removeIf(categoryId -> !foldersTreeCache.existsFolder(categoryId))) { // Clean-up orphans
			us.setInactiveCategoryFolders(inactiveFolders);
		}
	}
	
	private void appendOriginFolderNodes(final ArrayList<ExtTreeNode> children, final CategoryFSOrigin origin, final Integer defaultCalendarId, final boolean writableOnly, final boolean chooser) {
		for (CategoryFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
			if (writableOnly && !folder.getPermissions().getItemsPermissions().has(FolderShare.ItemsRight.CREATE)) continue;

			final boolean isDefault = folder.getFolderId().equals(defaultCalendarId);
			final ExtTreeNode xnode = createCategoryFolderNode(chooser, origin, folder, isDefault);
			if (xnode != null) children.add(xnode);
		}
	}
	
	public void processManageFoldersTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				boolean chooser = ServletUtils.getBooleanParameter(request, "chooser", false);
				boolean bulk = ServletUtils.getBooleanParameter(request, "bulk", false);
				
				if (bulk && node.equals("root")) {
					boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", false);
					final Integer defaultCategoryId = manager.getDefaultCategoryId();
					boolean hasOthers = false;
					
					// Classic root nodes
					for (CategoryFSOrigin origin : foldersTreeCache.getOrigins()) {
						if (!chooser && !(origin instanceof MyCategoryFSOrigin)) {
							hasOthers = true;
							continue;
						}
						final ExtTreeNode onode = createOriginFolderNode(chooser, origin);
						if (onode != null) {
							ArrayList<ExtTreeNode> ochildren = new ArrayList<>();
							// Tree node -> append folders of specified origin
							appendOriginFolderNodes(ochildren, origin, defaultCategoryId, writableOnly, chooser);
							if (!ochildren.isEmpty()) {
								onode.setChildren(ochildren);
								if (chooser) onode.setExpanded(true);
								children.add(onode.setLoaded(true));
							}
						}
					}
					// Others root node
					if (!chooser && hasOthers) {
						final ExtTreeNode gnode = createOthersFolderNode(chooser);
						if (gnode != null) {
							ArrayList<ExtTreeNode> gchildren = new ArrayList<>();
							for (CategoryFSOrigin origin : foldersTreeCache.getOrigins()) {
								if (origin instanceof MyCategoryFSOrigin) continue;
								
								final ExtTreeNode onode = createOriginFolderNode(chooser, origin);
								if (onode != null) {
									ArrayList<ExtTreeNode> ochildren = new ArrayList<>();
									// Tree node -> append folders of specified incoming origin
									appendOriginFolderNodes(ochildren, origin, defaultCategoryId, writableOnly, chooser);
									onode.setChildren(ochildren);
									gchildren.add(onode.setLoaded(true));
								}
							}
							gnode.setChildren(gchildren);
							children.add(gnode.setLoaded(true));
						}
					}
					new JsonResult("children", children).printTo(out);
					
				} else {
					if (node.equals("root")) { // Tree ROOT node -> list folder origins (incoming origins will be grouped)
						boolean hasOthers = false;
						
						// Classic root nodes
						for (CategoryFSOrigin origin : foldersTreeCache.getOrigins()) {
							if (!chooser && !(origin instanceof MyCategoryFSOrigin)) {
								hasOthers = true;
								continue;
							}
							final ExtTreeNode xnode = createOriginFolderNode(chooser, origin);
							if (xnode != null) children.add(xnode);
						}
						// Others root node
						if (!chooser && hasOthers) {
							final ExtTreeNode xnode = createOthersFolderNode(chooser);
							if (xnode != null) children.add(xnode);
						}
						
					} else {
						boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", false);
						CategoryNodeId nodeId = new CategoryNodeId(node);
						if (nodeId.isGrouperOther() && !chooser) { // Tree node (others grouper) -> list all incoming origins
							for (CategoryFSOrigin origin : foldersTreeCache.getOrigins()) {
								if (origin instanceof MyCategoryFSOrigin) continue;

								// Will pass here incoming origins only (resources' origins excluded)
								final ExtTreeNode xnode = createOriginFolderNode(chooser, origin);
								children.add(xnode);
							}

						} else if (CategoryNodeId.Type.ORIGIN.equals(nodeId.getType())) { // Tree node -> list folder of specified origin
							final Integer defaultCategoryId = manager.getDefaultCategoryId();
							final CategoryFSOrigin origin = foldersTreeCache.getOriginByProfile(nodeId.getOriginAsProfileId());
							for (CategoryFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
								if (writableOnly && !folder.getPermissions().getItemsPermissions().has(FolderShare.ItemsRight.CREATE)) continue;

								final boolean isDefault = folder.getFolderId().equals(defaultCategoryId);
								final ExtTreeNode xnode = createCategoryFolderNode(chooser, origin, folder, isDefault);
								if (xnode != null) children.add(xnode);
							}	

						} else {
							throw new WTParseException("Unable to parse '{}' as node ID", node);
						}
					}
					new JsonResult("children", children).printTo(out);
				}
				
			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsFolderNode.List> pl = ServletUtils.getPayloadAsList(request, JsFolderNode.List.class);
				
				for (JsFolderNode node : pl.data) {
					CategoryNodeId nodeId = new CategoryNodeId(node.id);
					if (CategoryNodeId.Type.ORIGIN.equals(nodeId.getType()) || CategoryNodeId.Type.GROUPER.equals(nodeId.getType())) {
						toggleActiveOrigin(nodeId.getOrigin(), node._active);
					} else if (CategoryNodeId.Type.FOLDER.equals(nodeId.getType())) {
						toggleActiveFolder(nodeId.getFolderId(), node._active);
					}
				}
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<JsFolderNode.List> pl = ServletUtils.getPayloadAsList(request, JsFolderNode.List.class);
				
				for (JsFolderNode node : pl.data) {
					CategoryNodeId nodeId = new CategoryNodeId(node.id);
					if (CategoryNodeId.Type.FOLDER.equals(nodeId.getType())) {
						manager.deleteCategory(nodeId.getFolderId());
						toggleActiveFolder(nodeId.getFolderId(), true); // forgets it by simply activating it
					}
				}
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in action ManageFoldersTree", ex);
		}
	}
	
	private ExtTreeNode createOriginFolderNode(boolean chooser, CategoryFSOrigin origin) {
		CategoryNodeId nodeId = CategoryNodeId.build(CategoryNodeId.Type.ORIGIN, origin.getProfileId());
		boolean checked = isOriginActive(toInactiveOriginKey(origin));
		if (origin instanceof MyCategoryFSOrigin) {
			return createOriginFolderNode(chooser, nodeId, "{trfolders.origin.my}", "wttasks-icon-categoryMy", origin.getWildcardPermissions(), checked);
		} else {
			return createOriginFolderNode(chooser, nodeId, origin.getDisplayName(), "wttasks-icon-categoryIncoming", origin.getWildcardPermissions(), checked);
		}
	}
	
	private ExtTreeNode createOriginFolderNode(boolean chooser, CategoryNodeId nodeId, String text, String iconClass, FolderShare.Permissions originPermissions, boolean isActive) {
		ExtTreeNode node = new ExtTreeNode(nodeId.toString(), text, false);
		node.put("_orPerms", originPermissions.getFolderPermissions().toString(true));
		node.put("_active", isActive);
		node.setIconClass(iconClass);
		if (!chooser) node.setChecked(isActive);
		node.put("expandable", true);
		return node;
	}
	
	private ExtTreeNode createOthersFolderNode(boolean chooser) {
		CategoryNodeId nodeId = CategoryNodeId.build(CategoryNodeId.Type.GROUPER, CategoryNodeId.GROUPER_OTHERS_ORIGIN);
		boolean checked = isOriginActive(toInactiveOriginKey(nodeId));
		return createOthersFolderNode(chooser, nodeId, checked);
	}
	
	private ExtTreeNode createOthersFolderNode(boolean chooser, CategoryNodeId nodeId, boolean isActive) {
		ExtTreeNode node = new ExtTreeNode(nodeId.toString(), "{trfolders.origin.others}", false);
		node.put("_active", isActive);
		if (!chooser) node.setChecked(isActive);
		node.put("expandable", true);
		node.setIconClass("wttas-icon-categoryOthers");
		return node;
	}
	
	private ExtTreeNode createCategoryFolderNode(boolean chooser, CategoryFSOrigin origin, CategoryFSFolder folder, boolean isDefault) {
		CategoryNodeId.Type type = CategoryNodeId.Type.FOLDER;
		final CategoryNodeId nodeId = CategoryNodeId.build(type, origin.getProfileId(), folder.getFolderId());
		final String name = folder.getDisplayName();
		final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
		final boolean active = !inactiveFolders.contains(folder.getFolderId());
		return createCategoryFolderNode(chooser, nodeId, name, folder.getPermissions(), folder.getCategory(), props, isDefault, active);
	}
	
	private ExtTreeNode createCategoryFolderNode(boolean chooser, CategoryNodeId nodeId, String name, FolderShare.Permissions folderPermissions, Category category, CategoryPropSet folderProps, boolean isDefault, boolean isActive) {
		String color = category.getColor();
		Category.Sync sync = Category.Sync.OFF;
		
		if (folderProps != null) { // Props are not null only for incoming folders
			if (folderProps.getHiddenOrDefault(false)) return null;
			color = folderProps.getColorOrDefault(color);
			sync = folderProps.getSyncOrDefault(sync);
		} else {
			sync = category.getSync();
		}
		
		ExtTreeNode node = new ExtTreeNode(nodeId.toString(), name, true);
		node.put("_foPerms", folderPermissions.getFolderPermissions().toString());
		node.put("_itPerms", folderPermissions.getItemsPermissions().toString());
		node.put("_builtIn", category.getBuiltIn());
		node.put("_color", Category.getHexColor(color));
		node.put("_sync", EnumUtils.toSerializedName(sync));
		node.put("_default", isDefault);
		node.put("_active", isActive);
		node.put("_defPrivate", category.getIsPrivate());
		node.put("_defReminder", category.getDefaultReminder());
		if (!chooser) node.setChecked(isActive);
		return node;
	}
	
	public void processLookupCategoryFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCategoryLkp> items = new ArrayList<>();
		
		try {
			Integer defltCategoryId = manager.getDefaultCategoryId();
			for (CategoryFSOrigin origin : foldersTreeCache.getOrigins()) {
				for (CategoryFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
					final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					final boolean isDefault = folder.getFolderId().equals(defltCategoryId);
					items.add(new JsCategoryLkp(origin, folder, props, isDefault, items.size()));
				}
			}
			new JsonResult("folders", items, items.size()).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in LookupCategoryFolders", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String node = ServletUtils.getStringParameter(request, "id", true);
				
				CategoryNodeId nodeId = new CategoryNodeId(node);
				FolderSharing.Scope scope = JsCategorySharing.toFolderSharingScope(nodeId);
				Set<FolderSharing.SubjectConfiguration> configurations = manager.getFolderShareConfigurations(nodeId.getOriginAsProfileId(), scope);
				String[] sdn = buildSharingDisplayNames(nodeId);
				new JsonResult(new JsCategorySharing(nodeId, sdn[0], sdn[1], configurations)).printTo(out);
				
			} else if (Crud.UPDATE.equals(crud)) {
				Payload<MapItem, JsCategorySharing> pl = ServletUtils.getPayload(request, JsCategorySharing.class);
				
				CategoryNodeId nodeId = new CategoryNodeId(pl.data.id);
				FolderSharing.Scope scope = JsCategorySharing.toFolderSharingScope(nodeId);
				manager.updateFolderShareConfigurations(nodeId.getOriginAsProfileId(), scope, pl.data.toSubjectConfigurations());
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageSharing", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private String[] buildSharingDisplayNames(CategoryNodeId nodeId) throws WTException {
		String originDn = null, folderDn = null;
	
		CategoryFSOrigin origin = foldersTreeCache.getOrigin(nodeId.getOriginAsProfileId());
		if (origin instanceof MyCategoryFSOrigin) {
			originDn = lookupResource(TasksLocale.CATEGORIES_MY);
		} else if (origin instanceof CategoryFSOrigin) {
			originDn = origin.getDisplayName();
		}
		
		if (CategoryNodeId.Type.FOLDER.equals(nodeId.getType())) {
			CategoryFSFolder folder = foldersTreeCache.getFolder(nodeId.getFolderId());
			folderDn = (folder != null) ? folder.getCategory().getName() : String.valueOf(nodeId.getFolderId());
		}
		
		return new String[]{originDn, folderDn};
	}
	
	public void processManageCategory(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Category item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				
				item = manager.getCategory(id);
				new JsonResult(new JsCategory(item)).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				item = manager.addCategory(JsCategory.createCategory(pl.data));
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.updateCategory(JsCategory.createCategory(pl.data));
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.deleteCategory(pl.data.categoryId);
				foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
				toggleActiveFolder(pl.data.categoryId, true); // forgets it by simply activating it
				new JsonResult().printTo(out);
				
			} else if (crud.equals("updateTag")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				UpdateTagsOperation op = ServletUtils.getEnumParameter(request, "op", true, UpdateTagsOperation.class);
				ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
				
				manager.updateTaskCategoryTags(op, id, new HashSet<>(tags));
				new JsonResult().printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in ManageCategory", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageHiddenCategories(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (Crud.READ.equals(crud)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				
				CategoryNodeId nodeId = new CategoryNodeId(node);
				if (CategoryNodeId.Type.ORIGIN.equals(nodeId.getType())) {
					final CategoryFSOrigin origin = foldersTreeCache.getOrigin(nodeId.getOriginAsProfileId());
					if (origin instanceof MyCategoryFSOrigin) throw new WTException("Unsupported for personal origin");
					
					ArrayList<JsSimple> items = new ArrayList<>();
					for (CategoryFSFolder folder : foldersTreeCache.getFoldersByOrigin(origin)) {
						final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
						if ((props != null) && props.getHiddenOrDefault(false)) {
							items.add(new JsSimple(folder.getFolderId(), folder.getCategory().getName()));
						}
					}
					new JsonResult(items).printTo(out);
					
				} else {
					throw new WTException("Invalid node [{}]", node);
				}
				
			} else if (Crud.UPDATE.equals(crud)) {
				Integer categoryId = ServletUtils.getIntParameter(request, "categoryId", true);
				Boolean hidden = ServletUtils.getBooleanParameter(request, "hidden", false);
				
				updateCategoryFolderVisibility(categoryId, hidden);
				new JsonResult().printTo(out);
				
			} else if (Crud.DELETE.equals(crud)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				HashSet<String> originProfileIds = new HashSet<>();
				for (String folderId : ids) {
					int categoryId = Integer.valueOf(folderId);
					CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(categoryId);
					if (origin == null) continue;
					originProfileIds.add(origin.getProfileId().toString());
					updateCategoryFolderVisibility(categoryId, null);
				}
				new JsonResult(originProfileIds).printTo(out);
			}
			
		} catch (Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetDefaultCategory(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			
			us.setDefaultCategoryFolder(id);
			Integer defltCategoryId = manager.getDefaultCategoryId();
			new JsonResult(String.valueOf(defltCategoryId)).printTo(out);
				
		} catch (Exception ex) {
			logger.error("Error in SetDefaultCategory", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetCategoryColor(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String color = ServletUtils.getStringParameter(request, "color", null);
			
			updateCategoryFolderColor(id, color);
			new JsonResult().printTo(out);
				
		} catch (Exception ex) {
			logger.error("Error in SetCategoryColor", ex);
			new JsonResult(ex).printTo(out);
		}
	}
				
	public void processSetCategorySync(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String sync = ServletUtils.getStringParameter(request, "sync", null);
			
			updateCategoryFolderSync(id, EnumUtils.forSerializedName(sync, Category.Sync.class));
			new JsonResult().printTo(out);
				
		} catch (Exception ex) {
			logger.error("Error in SetCategorySync", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processGetTaskChildren(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile userProfile = getEnv().getProfile();
		DateTimeZone userTimeZone = userProfile.getTimeZone();
		
		try {
			String parentId = ServletUtils.getStringParameter(request, "parentId", true);
			
			List<JsSimple> items = new ArrayList<>();
			Condition<TaskQuery> pred = new TaskQuery().parent().eq(parentId);
			Set<Integer> categoryIds = manager.listAllCategoryIds();
			for (TaskLookupInstance instance : manager.listTaskInstances(categoryIds, pred, SortInfo.asc("subject"), userTimeZone)) {
				items.add(new JsSimple(instance.getIdAsString(), instance.getSubject()));
			}
			new JsonResult(items).printTo(out);
			
		} catch (Throwable t) {
			logger.error("Error in GetTaskChildren", t);
			new JsonResult(t).printTo(out);
		}
	}

	public void processManageGridTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile userProfile = getEnv().getProfile();
		DateTimeZone userTimeZone = userProfile.getTimeZone();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				ITasksManager.TaskListView view = ServletUtils.getEnumParameter(request, "view", null, ITasksManager.TaskListView.class);
				String queryText = ServletUtils.getStringParameter(request, "queryText", null);
				if (!StringUtils.isBlank(queryText)) {
					CoreManager core = WT.getCoreManager();
					core.saveMetaEntry(SERVICE_ID, META_CONTEXT_SEARCH, queryText, queryText, false);
				}
				QueryObj queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
				if (view == null) queryObj = applyTasksQueryObjDefaults(queryObj);
				SortMeta.List sortMeta = ServletUtils.getObjectParameter(request, "sort", new SortMeta.List(), SortMeta.List.class);
				
				ArrayList<JsGridTask> items = new ArrayList<>();
				Set<String> parents = new HashSet<>();
				Map<String, CustomField.Type> map = cacheSearchableCustomFieldType.shallowCopy();
				List<Integer> visibleCategoryIds = getActiveFolderIds();
				SortInfo sortInfo = !sortMeta.isEmpty() ? sortMeta.get(0).toSortInfo() : SortInfo.asc("start");
				for (TaskLookupInstance instance : manager.listTaskInstances(visibleCategoryIds, view, null, TaskQuery.createCondition(queryObj, map, userTimeZone), sortInfo, userTimeZone)) {
					final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCategoryId());
					if (origin == null) continue;
					final CategoryFSFolder folder = foldersTreeCache.getFolder(instance.getCategoryId());
					if (folder == null) continue;
					
					final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					JsGridTask js = null;
					if (!instance.getHasChildren() && instance.getParentInstanceId() == null) { // simple task
						js = new JsGridTask(folder, props, instance, null, 0, userTimeZone);
					} else if (instance.getHasChildren()) { // parent task
						parents.add(instance.getTaskId());
						js = new JsGridTask(folder, props, instance, JsGridTask.Hierarchy.PARENT, 0, userTimeZone);
					} else if (instance.getParentInstanceId() != null) { // child task
						boolean parentInResultset = parents.contains(instance.getParentInstanceId().getTaskId());
						js = new JsGridTask(folder, props, instance, JsGridTask.Hierarchy.CHILD, parentInResultset ? 1 : 0, userTimeZone);
					}
					items.add(js);
				}
				new JsonResult(items, items.size())
					.setMetaData(new GridMetadata()
						.setSortInfo(sortInfo)
					)
					.printTo(out);
				
			} else if (crud.equals("updateTag")) {
				StringArray ids = ServletUtils.getObjectParameter(request, "ids", StringArray.class, true);
				UpdateTagsOperation op = ServletUtils.getEnumParameter(request, "op", true, UpdateTagsOperation.class);
				ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
				List<TaskInstanceId> iids = ids.stream()
					.map(id -> TaskInstanceId.parse(id))
					.filter(id -> id != null)
					.collect(Collectors.toList());
				
				manager.updateTaskInstanceTags(op, iids, new HashSet<>(tags));
				new JsonResult().printTo(out);
				
			} else if (crud.equals("complete")) {
				StringArray ids = ServletUtils.getObjectParameter(request, "ids", StringArray.class, true);
				List<TaskInstanceId> iids = ids.stream()
					.map(id -> TaskInstanceId.parse(id))
					.filter(id -> id != null)
					.collect(Collectors.toList());
				
				manager.updateQuickTaskInstance(iids, true, null, null);
				new JsonResult().printTo(out);
				
			} else if (crud.equals("setProgress")) {
				StringArray ids = ServletUtils.getObjectParameter(request, "ids", StringArray.class, true);
				Short progress = ServletUtils.getShortParameter(request, "progress", true);
				List<TaskInstanceId> iids = ids.stream()
					.map(id -> TaskInstanceId.parse(id))
					.filter(id -> id != null)
					.collect(Collectors.toList());
				
				manager.updateQuickTaskInstance(iids, null, progress, null);
				new JsonResult().printTo(out);
				
			} else if (crud.equals("setImportance")) {
				StringArray ids = ServletUtils.getObjectParameter(request, "ids", StringArray.class, true);
				Short importance = ServletUtils.getShortParameter(request, "importance", true);
				List<TaskInstanceId> iids = ids.stream()
					.map(id -> TaskInstanceId.parse(id))
					.filter(id -> id != null)
					.collect(Collectors.toList());
				
				manager.updateQuickTaskInstance(iids, null, null, importance);
				new JsonResult().printTo(out);
			}
		
		} catch (Exception ex) {
			logger.error("Error in ManageGridTasks", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	private QueryObj applyTasksQueryObjDefaults(QueryObj queryObj) {
		// If status is not used in query, filter-out completed tasks by default!
		if (!queryObj.hasCondition("is", "done") && !queryObj.hasCondition("status")) {
			queryObj.addCondition("is", "done", true);
			//FIXME: using many conditions seems that provides unexpected search results. I think that there is a problem on how precedences are translated using QBuilder object.
			//queryObj.addCondition("status", EnumUtils.toSerializedName(TaskBase.Status.NEEDS_ACTION), false);
			//queryObj.addCondition("status", EnumUtils.toSerializedName(TaskBase.Status.IN_PROGRESS), false);
			//queryObj.addCondition("status", EnumUtils.toSerializedName(TaskBase.Status.CANCELLED), false);
			//queryObj.addCondition("status", EnumUtils.toSerializedName(TaskBase.Status.WAITING), false);
		}
		return queryObj;
	}
	
	public void processPrintTasks(HttpServletRequest request, HttpServletResponse response) {
		UserProfile userProfile = getEnv().getProfile();
		DateTimeZone userTimeZone = userProfile.getTimeZone();
		AbstractReport rpt = null;
		ByteArrayOutputStream baos = null;
		
		try {
			String filename = ServletUtils.getStringParameter(request, "filename", false);
			String type = ServletUtils.getStringParameter(request, "type", true);
			
			if ("list".equals(type)) {
				if (StringUtils.isBlank(filename)) filename = "tasklist";
				ReportConfig.Builder builder = reportConfigBuilder();
				rpt = new RptTaskList(builder.build());
				ArrayList<RBTaskList> items = new ArrayList<>();
				
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, false);
				if (ids != null && !ids.isEmpty()) {
					List<TaskInstanceId> iids = ids.stream()
						.map(id -> TaskInstanceId.parse(id))
						.filter(id -> id != null)
						.collect(Collectors.toList());
					
					String lastParent = null;
					for (TaskInstanceId iid : iids) {
						TaskInstance instance = manager.getTaskInstance(iid);
						if (instance == null) continue;
						final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCategoryId());
						if (origin == null) continue;
						final CategoryFSFolder folder = foldersTreeCache.getFolder(instance.getCategoryId());
						if (folder == null) continue;
						
						final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
						boolean showNested = false;
						if (instance.isParent()) {
							lastParent = instance.getTaskId();
						} else if (instance.isChild()) {
							showNested = StringUtils.equals(instance.getParentInstanceId().getTaskId(), lastParent);
						}
						items.add(new RBTaskList(origin, folder, props, instance, userTimeZone, showNested));
					}
					
				} else {
					ITasksManager.TaskListView view = ServletUtils.getEnumParameter(request, "view", null, ITasksManager.TaskListView.class);
					QueryObj queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
					if (view == null) queryObj = applyTasksQueryObjDefaults(queryObj);
					SortMeta.List sortMeta = ServletUtils.getObjectParameter(request, "sort", new SortMeta.List(), SortMeta.List.class);
					
					String lastParent = null;
					Map<String, CustomField.Type> map = cacheSearchableCustomFieldType.shallowCopy();
					List<Integer> visibleCategoryIds = getActiveFolderIds();
					SortInfo sortInfo = !sortMeta.isEmpty() ? sortMeta.get(0).toSortInfo() : SortInfo.asc("start");
					for (TaskLookupInstance instance : manager.listTaskInstances(visibleCategoryIds, view, null, TaskQuery.createCondition(queryObj, map, userTimeZone), sortInfo, userTimeZone)) {
						final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCategoryId());
						if (origin == null) continue;
						final CategoryFSFolder folder = foldersTreeCache.getFolder(instance.getCategoryId());
						if (folder == null) continue;
						
						final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
						boolean showNested = false;
						if (instance.isParent()) {
							lastParent = instance.getTaskId();
						} else if (instance.isChild()) {
							showNested = StringUtils.equals(instance.getParentInstanceId().getTaskId(), lastParent);
						}
						items.add(new RBTaskList(origin, folder, props, instance, userTimeZone, showNested));
					}
				}
				rpt.setDataSource(items);
				
			} else if ("detail".equals(type)) {
				if (StringUtils.isBlank(filename)) filename = "taskdetail";
				ReportConfig.Builder builder = reportConfigBuilder();
				rpt = new RptTasksDetail(builder.build());
				ArrayList<RBTaskDetail> items = new ArrayList<>();
				
				RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(userProfile.getLocale());
				RRuleStringify rrs = new RRuleStringify(strings, userTimeZone);
				
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, false);
				List<TaskInstanceId> iids = ids.stream()
					.map(id -> TaskInstanceId.parse(id))
					.filter(id -> id != null)
					.collect(Collectors.toList());

				for (TaskInstanceId iid : iids) {
					TaskInstance instance = manager.getTaskInstance(iid, BitFlag.none());
					if (instance == null) continue;
					final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCategoryId());
					if (origin == null) continue;
					final CategoryFSFolder folder = foldersTreeCache.getFolder(instance.getCategoryId());
					if (folder == null) continue;

					final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					items.add(new RBTaskDetail(rrs, origin, folder, props, instance, userTimeZone));
				}
				rpt.setDataSource(items);
			}
			
			ServletUtils.setFileStreamHeaders(response, filename + ".pdf");
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, response.getOutputStream());
			
		} catch (Exception ex) {
			logger.error("Error in action PrintTasks", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
	
	public void processGetTaskPreview(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				UserProfile up = getEnv().getProfile();
				TaskInstanceId instanceId = TaskInstanceId.parse(id);
				BitFlag<ITasksManager.TaskGetOptions> options = BitFlag.of(ITasksManager.TaskGetOptions.TAGS, ITasksManager.TaskGetOptions.CUSTOM_VALUES);
				TaskInstance task = manager.getTaskInstance(instanceId, options);
				if (task == null) throw new WTException("Task not found [{}]", instanceId);
				
				final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(task.getCategoryId());
				if (origin == null) throw new WTException("Origin not found [{}]", task.getCategoryId());
				final CategoryFSFolder folder = foldersTreeCache.getFolder(task.getCategoryId());
				if (folder == null) throw new WTException("Folder not found [{}]", task.getCategoryId());
				final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
				
				Set<String> pvwfields = coreMgr.listCustomFieldIds(SERVICE_ID, BitFlag.of(CoreManager.CustomFieldListOptions.PREVIEWABLE));
				Map<String, CustomPanel> cpanels = coreMgr.listCustomPanelsUsedBy(SERVICE_ID, task.getTags());
				Map<String, CustomField> cfields = new HashMap<>();
				for (CustomPanel cpanel : cpanels.values()) {
					for (String fieldId : cpanel.getFields()) {
						if (!pvwfields.contains(fieldId)) continue;
						CustomField cfield = coreMgr.getCustomField(SERVICE_ID, fieldId);
						if (cfield != null) cfields.put(fieldId, cfield);
					}
				}

				new JsonResult(new JsTaskPreview(origin, folder, props, task, cpanels.values(), cfields, up.getLanguageTag(), up.getTimeZone())).printTo(out);
			}
			
		} catch (Exception ex) {
			logger.error("Error in GetTaskPreview", ex);
			new JsonResult(ex).printTo(out);	
		}
	}
	
	public void processPrepareSendTaskByEmail(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<MapItem> items = new ArrayList<>();
		
		try {
			String tag = ServletUtils.getStringParameter(request, "uploadTag", true);
			StringArray ids = ServletUtils.getObjectParameter(request, "ids", StringArray.class, true);
			List<TaskInstanceId> iids = ids.stream()
				.map(id -> TaskInstanceId.parse(id))
				.filter(id -> id != null)
				.collect(Collectors.toList());
			
			HashSet<String> processed = new HashSet();
			for (TaskInstanceId iid : iids) {
				TaskObjectWithICalendar taskObj = (TaskObjectWithICalendar)manager.getTaskObject(iid, TaskObjectOutputType.ICALENDAR);
				if (taskObj == null) continue;
				if (processed.contains(taskObj.getTaskId())) continue; // Skip iid from same series
				
				final String filename = PathUtils.sanitizeFileName(taskObj.getObjectName()) + ".ics";
				UploadedFile upfile = addAsUploadedFile(tag, filename, "text/icalendar", IOUtils.toInputStream(taskObj.getIcalendar()));
				items.add(new MapItem()
					.add("uploadId", upfile.getUploadId())
					.add("fileName", filename)
					.add("fileSize", upfile.getSize())
				);
				processed.add(taskObj.getTaskId());
			}
			new JsonResult(items).printTo(out);
			
		} catch (Throwable t) {
			logger.error("Error in PrepareSendTaskByEmail", t);
			new JsonResult(t).printTo(out);
		}
	}
	    
	public void processManageTask(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		JsTask item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				TaskInstanceId instanceId = TaskInstanceId.parse(id);
				TaskInstance task = manager.getTaskInstance(instanceId);
				if (task == null) throw new WTException("Task not found [{}]", instanceId);
				UserProfileId ownerId = manager.getCategoryOwner(task.getCategoryId());
				
				Map<String, CustomPanel> cpanels = coreMgr.listCustomPanelsUsedBy(SERVICE_ID, task.getTags());
				Map<String, CustomField> cfields = new HashMap<>();
				for (CustomPanel cpanel : cpanels.values()) {
					for (String fieldId : cpanel.getFields()) {
						CustomField cfield = coreMgr.getCustomField(SERVICE_ID, fieldId);
						if (cfield != null) cfields.put(fieldId, cfield);
					}
				}
				String parentTaskSubject = null;
				//if (!StringUtils.isBlank(task.getParentTaskId())) {
				if (task.getParentInstanceId() != null) {
					//TaskInstanceId piid = TaskInstanceId.build(task.getParentTaskId(), TaskInstanceId.MASTER_INSTANCE_ID);
					//TaskInstance ptask = manager.getTaskInstance(piid, BitFlag.none());
					TaskInstance ptask = manager.getTaskInstance(task.getParentInstanceId(), BitFlag.none());
					if (ptask != null) {
						parentTaskSubject = ptask.getSubject();
					} else {
						//logger.warn("Referenced parent task not found [{}]", piid);
						logger.warn("Referenced parent task not found [{}]", task.getParentInstanceId());
					}
				}
				item = new JsTask(ownerId, task, cpanels.values(), cfields, parentTaskSubject, up.getLanguageTag(), up.getTimeZone());
				new JsonResult(item).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsTask> pl = ServletUtils.getPayload(request, JsTask.class);
				
				TaskEx task = pl.data.createTaskForAdd(up.getTimeZone());
				for (JsTask.Attachment jsatt : pl.data.attachments) {
					UploadedFile upFile = getUploadedFileOrThrow(jsatt._uplId);
					TaskAttachmentWithStream att = new TaskAttachmentWithStream(upFile.getFile());
					att.setAttachmentId(jsatt.id);
					att.setFilename(upFile.getFilename());
					att.setSize(upFile.getSize());
					att.setMediaType(upFile.getMediaType());
					task.addAttachment(att);
				}
                manager.addTask(task);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsTask> pl = ServletUtils.getPayload(request, JsTask.class);
				
				TaskEx task = pl.data.createTaskForUpdate(up.getTimeZone());
				for (JsTask.Attachment jsatt : pl.data.attachments) {
					if (!StringUtils.isBlank(jsatt._uplId)) {
						UploadedFile upFile = getUploadedFileOrThrow(jsatt._uplId);
						TaskAttachmentWithStream att = new TaskAttachmentWithStream(upFile.getFile());
						att.setAttachmentId(jsatt.id);
						att.setFilename(upFile.getFilename());
						att.setSize(upFile.getSize());
						att.setMediaType(upFile.getMediaType());
						task.addAttachment(att);
					} else {
						TaskAttachment att = new TaskAttachment();
						att.setAttachmentId(jsatt.id);
						att.setFilename(jsatt.name);
						att.setSize(jsatt.size);
						task.addAttachment(att);
					}
				}
				
				TaskInstanceId iid = TaskInstanceId.parse(pl.data.id);
				manager.updateTaskInstance(iid, task);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				StringArray iids = ServletUtils.getObjectParameter(request, "iids", StringArray.class, true);
				String target = ServletUtils.getStringParameter(request, "target", false);
				
				boolean single = iids.size() == 1;
				List<TaskInstanceId> instanceIds = new ArrayList<>();
				for (String iid : iids) {
					TaskInstanceId id = TaskInstanceId.parse(iid);
					if (id != null) {
						if (single && "all".equals(target)) {
							// Control over target is performed by tweaking the instance ID: all series tasks
							// will be deleted by referring to the series (00000000 as instance).
							instanceIds.add(TaskInstanceId.buildMaster(id.getTaskId()));
						} else {
							instanceIds.add(id);
						}
					}
				}
				
				manager.deleteTaskInstance(instanceIds);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.MOVE)) {
				StringArray ids = ServletUtils.getObjectParameter(request, "iids", StringArray.class, true);
				List<TaskInstanceId> iids = ids.stream()
					.map(id -> TaskInstanceId.parse(id))
					.filter(id -> id != null)
					.collect(Collectors.toList());
				//String iid = ServletUtils.getStringParameter(request, "iid", true);
				Integer categoryId = ServletUtils.getIntParameter(request, "targetCategoryId", true);
				ITasksManager.MoveCopyMode copyMode = ServletUtils.getEnumParameter(request, "copyMode", ITasksManager.MoveCopyMode.NONE, ITasksManager.MoveCopyMode.class);
				
				//TaskInstanceId instanceId = TaskInstanceId.parse(iid);
				//manager.moveTaskInstance(copy, instanceId, categoryId);
				manager.moveTaskInstance(copyMode, iids, categoryId);
				new JsonResult().printTo(out);
			}
			
		} catch (Throwable t) {
			logger.error("Error in ManageTasks", t);
			new JsonResult(t).printTo(out);	
		}
	}
	
	public void processDownloadTaskAttachment(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			boolean inline = ServletUtils.getBooleanParameter(request, "inline", false);
			String attachmentId = ServletUtils.getStringParameter(request, "attachmentId", null);
			
			if (!StringUtils.isBlank(attachmentId)) {
				String iid = ServletUtils.getStringParameter(request, "iid", true);
				
				TaskInstanceId instanceId = TaskInstanceId.parse(iid);
				TaskAttachmentWithBytes attData = manager.getTaskInstanceAttachment(instanceId, attachmentId);
				InputStream is = null;
				try {
					is = new ByteArrayInputStream(attData.getBytes());
					ServletUtils.writeFileResponse(response, inline, attData.getFilename(), null, attData.getSize(), is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			} else {
				String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
				
				UploadedFile uplFile = getUploadedFileOrThrow(uploadId);
				InputStream is = null;
				try {
					is = new FileInputStream(uplFile.getFile());
					ServletUtils.writeFileResponse(response, inline, uplFile.getFilename(), null, uplFile.getSize(), is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
			
		} catch (Throwable t) {
			logger.error("Error in DownloadTaskAttachment", t);
			ServletUtils.writeErrorHandlingJs(response, t.getMessage());
		}
	}
	
	public void processGetCustomFieldsDefsData(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
			String iid = ServletUtils.getStringParameter(request, "id", false);
			
			TaskInstanceId instanceId = TaskInstanceId.parse(iid);
			Map<String, CustomPanel> cpanels = coreMgr.listCustomPanelsUsedBy(SERVICE_ID, tags);
			Map<String, CustomFieldValue> cvalues = (instanceId != null) ? manager.getTaskInstanceCustomValues(instanceId) : null;
			Map<String, CustomField> cfields = new HashMap<>();
			for (CustomPanel cpanel : cpanels.values()) {
				for (String fieldId : cpanel.getFields()) {
					CustomField cfield = coreMgr.getCustomField(SERVICE_ID, fieldId);
					if (cfield != null) cfields.put(fieldId, cfield);
				}
			}
			new JsonResult(new JsCustomFieldDefsData(cpanels.values(), cfields, cvalues, up.getLanguageTag(), up.getTimeZone())).setTotal(cfields.size()).printTo(out);
			
		} catch (Throwable t) {
			logger.error("Error in GetCustomFieldsDefsData", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processImportTasksFromICal(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String op = ServletUtils.getStringParameter(request, "op", true);
			String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
			
			UploadedFile upl = getUploadedFile(uploadId);
			if (upl == null) throw new WTException("Uploaded file not found [{0}]", uploadId);
			File file = new File(WT.getTempFolder(), upl.getUploadId());
			
			if (op.equals("do")) {
				String oid = ServletUtils.getStringParameter(request, "oid", true);
				Integer categoryId = ServletUtils.getIntParameter(request, "categoryId", true);
				ITasksManager.ImportMode mode = ServletUtils.getEnumParameter(request, "importMode", ITasksManager.ImportMode.COPY, ITasksManager.ImportMode.class);
				
				WebTopSession wts = getWts();
				
				ICalendarInput in = new ICalendarInput(up.getTimeZone())
					.withLogHandler(new LogHandler() {
						@Override
						public void handle(Collection<LogEntry> entries) {
							if (entries != null) wts.notify(toTaskImportLogSMs(oid, true, entries));
						}
					});
				FileInputStream fis = null;
				try {	
					fis = new FileInputStream(file);
					manager.importTasks(categoryId, in, fis, mode, new LogHandler() {
						@Override
						public void handle(Collection<LogEntry> entries) {
							if (entries != null) wts.notify(toTaskImportLogSMs(oid, false, entries));
						}
					});
				} finally {
					IOUtils.closeQuietly(fis);
				}
				removeUploadedFile(uploadId);
				new JsonResult(new JsWizardData(null)).printTo(out);
			}
			
		} catch (Throwable t) {
			logger.error("Error in ImportContactsFromICal", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	private ServiceMessage toTaskImportLogSMs(String operationId, boolean pushDown, Collection<LogEntry> entries) {
		StringJoiner sj = new StringJoiner("\n");
		for (LogEntry entry : entries) {
			if (pushDown) entry.pushDown();
			sj.add(entry.toString());
		}
		return new TaskImportLogSM(SERVICE_ID, operationId, sj.toString());
	}
	
	public void processPortletTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsPletTasks> items = new ArrayList<>();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", null);
			UserProfile up = getEnv().getProfile();
			DateTimeZone utz = up.getTimeZone();
			
			if (query == null) {
				final CategoryFSOrigin origin = foldersTreeCache.getOrigin(up.getId());
				//TODO: evaluate to add a user-setting to specify categories in which look for
				final Set<Integer> ids = manager.listMyCategoryIds();
				List<TaskLookupInstance> instances = manager.listTaskInstances(ids, ITasksManager.TaskListView.UPCOMING, utz);
				for (TaskLookupInstance instance : instances) {
					final CategoryFSFolder folder = foldersTreeCache.getFolder(instance.getCategoryId());
					if (folder == null) continue;
					
					final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					items.add(new JsPletTasks(origin, folder, props, instance, utz));
				}
				
			} else {
				final Set<Integer> ids = foldersTreeCache.getFolderIDs();
				final String pattern = LangUtils.patternizeWords(query);
				for (TaskLookupInstance instance : manager.listTaskInstances(ids, TaskQuery.createCondition(pattern), null, utz)) {
					final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(instance.getCategoryId());
					if (origin == null) continue;
					final CategoryFSFolder folder = foldersTreeCache.getFolder(instance.getCategoryId());
					if (folder == null) continue;
					
					final CategoryPropSet props = foldersPropsCache.get(folder.getFolderId()).orElse(null);
					items.add(new JsPletTasks(origin, folder, props, instance, utz));
				}
			}
			
			new JsonResult(items).printTo(out);
			
		} catch (Exception ex) {
			logger.error("Error in PortletTasks", ex);
			new JsonResult(ex).printTo(out);	
		}
	}
	
	private ReportConfig.Builder reportConfigBuilder() {
		UserProfile.Data ud = getEnv().getProfile().getData();
		CoreUserSettings cus = getEnv().getCoreUserSettings();
		return new ReportConfig.Builder()
			.useLocale(ud.getLocale())
			.useTimeZone(ud.getTimeZone().toTimeZone())
			.dateFormatShort(cus.getShortDateFormat())
			.dateFormatLong(cus.getLongDateFormat())
			.timeFormatShort(cus.getShortTimeFormat())
			.timeFormatLong(cus.getLongTimeFormat())
			.generatedBy(WT.getPlatformName() + " " + lookupResource(TasksLocale.SERVICE_NAME))
			.printedBy(ud.getDisplayName());
	}
	
	private ArrayList<Integer> getActiveFolderIds() {
		ArrayList<Integer> ids = new ArrayList<>();
		for (CategoryFSOrigin origin : getActiveOrigins()) {
			boolean isOthersChildren = !(origin instanceof MyCategoryFSOrigin);
			for (CategoryFSFolder folder: foldersTreeCache.getFoldersByOrigin(origin)) {
				if ((isOthersChildren && inactiveOrigins.contains(CategoryNodeId.GROUPER_OTHERS_ORIGIN))
					|| (inactiveFolders.contains(folder.getFolderId()))
				) continue;
				ids.add(folder.getFolderId());
			}
		}
		return ids;
	}
	
	private List<CategoryFSOrigin> getActiveOrigins() {
		return foldersTreeCache.getOrigins().stream()
			.filter(origin -> !inactiveOrigins.contains(toInactiveOriginKey(origin)))
			.collect(Collectors.toList());
	}
	
	private boolean shouldCleanupInactiveOriginKey(String originKey) {
		return !foldersTreeCache.existsOrigin(UserProfileId.parseQuielty(originKey));
	}
	
	private boolean isOriginActive(String originKey) {
		return !inactiveOrigins.contains(originKey);
	}
	
	private String toInactiveOriginKey(CategoryNodeId nodeId) {
		return nodeId.getOrigin();
	}
	
	private String toInactiveOriginKey(CategoryFSOrigin origin) {
		return origin.getProfileId().toString();
	}
	
	private void toggleActiveOrigin(String originKey, boolean active) {
		toggleActiveOrigins(new String[]{originKey}, active);
	}
	
	private void toggleActiveOrigins(String[] originKeys, boolean active) {	
		try {
			locks.tryLock("inactiveOrigins", 60, TimeUnit.SECONDS);
			for (String originId : originKeys) {
				if (active) {
					inactiveOrigins.remove(originId);
				} else {
					inactiveOrigins.add(originId);
				}
			}
			us.setInactiveCategoryOrigins(inactiveOrigins);
			
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("inactiveOrigins");
		}
	}
	
	private void toggleActiveFolder(Integer folderId, boolean active) {
		toggleActiveFolders(new Integer[]{folderId}, active);
	}
	
	private void toggleActiveFolders(Integer[] folderIds, boolean active) {
		try {
			locks.tryLock("inactiveFolders", 60, TimeUnit.SECONDS);
			for (int folderId : folderIds) {
				if (active) {
					inactiveFolders.remove(folderId);
				} else {
					inactiveFolders.add(folderId);
				}
			}
			us.setInactiveCategoryFolders(inactiveFolders);
			
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("inactiveFolders");
		}
	}
	
	private void updateCategoryFolderVisibility(int categoryId, Boolean hidden) {
		try {
			locks.tryLock("folderVisibility-"+categoryId, 60, TimeUnit.SECONDS);
			CategoryPropSet pset = manager.getCategoryCustomProps(categoryId);
			pset.setHidden(hidden);
			manager.updateCategoryCustomProps(categoryId, pset);
			
			final CategoryFSFolder folder = foldersTreeCache.getFolder(categoryId);
			if (!(folder instanceof MyCategoryFSFolder)) {
				foldersPropsCache.put(categoryId, Optional.of(pset));
			}
		
		} catch (WTException ex) {
			logger.error("Error saving custom category props", ex);
		} catch (InterruptedException ex) {
			// Do nothing...
		} finally {
			locks.unlock("folderVisibility-"+categoryId);
		}
	}
	
	private void updateCategoryFolderColor(int categoryId, String color) throws WTException {
		final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(categoryId);
		if (origin instanceof MyCategoryFSOrigin) {
			Category category = manager.getCategory(categoryId);
			category.setColor(color);
			manager.updateCategory(category);
			foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
			
		} else if (origin instanceof CategoryFSOrigin) {
			CategoryPropSet props = manager.getCategoryCustomProps(categoryId);
			props.setColor(color);
			manager.updateCategoryCustomProps(categoryId, props);
			foldersPropsCache.put(categoryId, Optional.of(props));
		}
	}
	
	private void updateCategoryFolderSync(int categoryId, Category.Sync sync) throws WTException {
		final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(categoryId);
		if (origin instanceof MyCategoryFSOrigin) {
			Category category = manager.getCategory(categoryId);
			category.setSync(sync);
			manager.updateCategory(category);
			foldersTreeCache.init(AbstractFolderTreeCache.Target.FOLDERS);
			
		} else if (origin instanceof CategoryFSOrigin) {
			CategoryPropSet props = manager.getCategoryCustomProps(categoryId);
			props.setSync(sync);
			manager.updateCategoryCustomProps(categoryId, props);
			foldersPropsCache.put(categoryId, Optional.of(props));
		}
	}
	
	private class SearchableCustomFieldTypeCache extends AbstractPassiveExpiringBulkMap<String, CustomField.Type> {
		
		public SearchableCustomFieldTypeCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit);
		}
		
		@Override
		protected Map<String, CustomField.Type> internalGetMap() {
			try {
				CoreManager coreMgr = WT.getCoreManager();
				return coreMgr.listCustomFieldTypesById(SERVICE_ID, BitFlag.of(CoreManager.CustomFieldListOptions.SEARCHABLE));
				
			} catch(Throwable t) {
				logger.error("[SearchableCustomFieldTypeCache] Unable to build cache", t);
				throw new UnsupportedOperationException();
			}
		}
	}
	
	private class FoldersPropsCacheLoader implements CacheLoader<Integer, Optional<CategoryPropSet>> {
		@Override
		public Optional<CategoryPropSet> load(Integer k) throws Exception {
			try {
				logger.trace("[FoldersPropsCache] Loading... [{}]", k);
				final CategoryFSOrigin origin = foldersTreeCache.getOriginByFolder(k);
				if (origin == null) return Optional.empty(); // Disable lookup for unknown folder IDs
				if (origin instanceof MyCategoryFSOrigin) return Optional.empty(); // Disable lookup for personal folder IDs
				return Optional.ofNullable(manager.getCategoryCustomProps(k));
				
			} catch (Exception ex) {
				logger.error("[FoldersPropsCache] Unable to load [{}]", k);
				return null;
			}
		}
	}
	
	private class FoldersTreeCache extends AbstractFolderTreeCache<Integer, CategoryFSOrigin, CategoryFSFolder, Object> {
		
		@Override
		protected void internalBuildCache(AbstractFolderTreeCache.Target options) {
			UserProfileId pid = getEnv().getProfile().getId();
				
			if (AbstractFolderTreeCache.Target.ALL.equals(options) || AbstractFolderTreeCache.Target.ORIGINS.equals(options)) {
				try {
					this.internalClear(AbstractFolderTreeCache.Target.ORIGINS);
					this.origins.put(pid, new MyCategoryFSOrigin(pid));
					for (CategoryFSOrigin origin : manager.listIncomingCategoryOrigins().values()) {
						this.origins.put(origin.getProfileId(), origin);
					}
					
				} catch (WTException ex) {
					logger.error("[FoldersTreeCache] Error updating Origins", ex);
				}
			}	
			if (AbstractFolderTreeCache.Target.ALL.equals(options) || AbstractFolderTreeCache.Target.FOLDERS.equals(options)) {
				try {
					this.internalClear(AbstractFolderTreeCache.Target.FOLDERS);
					for (CategoryFSOrigin origin : this.origins.values()) {
						if (origin instanceof MyCategoryFSOrigin) {
							for (Category category : manager.listMyCategories().values()) {
								final MyCategoryFSFolder folder = new MyCategoryFSFolder(category.getCategoryId(), category);
								this.folders.put(folder.getFolderId(), folder);
								this.foldersByOrigin.put(origin.getProfileId(), folder);
								this.originsByFolder.put(folder.getFolderId(), origin);
							}
						} else if (origin instanceof CategoryFSOrigin) {
							for (CategoryFSFolder folder : manager.listIncomingCategoryFolders(origin.getProfileId()).values()) {
								// Make sure to track only folders with at least READ premission: 
								// it is ugly having in UI empty folder nodes for just manage update/delete/sharing operations.
								if (!folder.getPermissions().getFolderPermissions().has(FolderShare.FolderRight.READ)) continue;
								
								this.folders.put(folder.getFolderId(), folder);
								this.foldersByOrigin.put(origin.getProfileId(), folder);
								this.originsByFolder.put(folder.getFolderId(), origin);
							}
						}
					}
					
				} catch (WTException ex) {
					logger.error("[FoldersTreeCache] Error updating Folders", ex);
				}
			}
		}
	}
}
