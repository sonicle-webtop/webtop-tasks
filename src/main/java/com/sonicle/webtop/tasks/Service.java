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

import com.google.gson.annotations.SerializedName;
import com.sonicle.commons.BitFlag;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.PathUtils;
import com.sonicle.commons.cache.AbstractPassiveExpiringBulkMap;
import com.sonicle.commons.qbuilders.conditions.Condition;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.time.DateTimeRange;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.ServletUtils.IntegerArray;
import com.sonicle.commons.web.ServletUtils.StringArray;
import com.sonicle.commons.web.json.CompositeId;
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
import com.sonicle.webtop.tasks.bol.js.JsFolderNode;
import com.sonicle.webtop.tasks.bol.js.JsSharing;
import com.sonicle.webtop.tasks.model.ShareFolderCategory;
import com.sonicle.webtop.tasks.model.ShareRootCategory;
import com.sonicle.webtop.tasks.bol.model.MyShareFolderCategory;
import com.sonicle.webtop.tasks.bol.model.MyShareRootCategory;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.app.WebTopSession.UploadedFile;
import com.sonicle.webtop.core.app.util.log.LogEntry;
import com.sonicle.webtop.core.app.util.log.LogHandler;
import com.sonicle.webtop.core.bol.js.JsCustomFieldDefsData;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.js.JsWizardData;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldDefs;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
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
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.LogEntries;
import com.sonicle.webtop.core.util.RRuleStringify;
import com.sonicle.webtop.tasks.bol.js.JsCategory;
import com.sonicle.webtop.tasks.bol.js.JsCategoryLkp;
import com.sonicle.webtop.tasks.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.tasks.bol.js.JsGridTask;
import com.sonicle.webtop.tasks.bol.js.JsPletTasks;
import com.sonicle.webtop.tasks.bol.js.JsTask;
import com.sonicle.webtop.tasks.bol.js.JsTaskPreview;
import com.sonicle.webtop.tasks.bol.model.RBTaskDetail;
import com.sonicle.webtop.tasks.bol.model.RBTaskList;
import com.sonicle.webtop.tasks.io.ICalendarInput;
import com.sonicle.webtop.tasks.io.ICalendarOutput;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.ListTasksResult;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithBytes;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithStream;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskEx;
import com.sonicle.webtop.tasks.model.TaskInstance;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import com.sonicle.webtop.tasks.model.TaskLookup;
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
import java.util.LinkedHashMap;
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
	
	private TasksManager manager;
	private TasksServiceSettings ss;
	private TasksUserSettings us;
	
	private final SearchableCustomFieldTypeCache cacheSearchableCustomFieldType = new SearchableCustomFieldTypeCache(5, TimeUnit.SECONDS);
	private final LinkedHashMap<String, ShareRootCategory> roots = new LinkedHashMap<>();
	private final LinkedHashMap<Integer, ShareFolderCategory> folders = new LinkedHashMap<>();
	private final HashMap<Integer, CategoryPropSet> folderProps = new HashMap<>();
	private final HashMap<String, ArrayList<ShareFolderCategory>> foldersByRoot = new HashMap<>();
	private final HashMap<Integer, ShareRootCategory> rootByFolder = new HashMap<>();
	
	private StringSet inactiveRoots = null;
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
		inactiveFolders.clear();
		inactiveFolders = null;
		inactiveRoots.clear();
		inactiveRoots = null;
		rootByFolder.clear();
		foldersByRoot.clear();
		folderProps.clear();
		folders.clear();
		roots.clear();
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
		return co;
	}
	
	private ObjCustomFieldDefs.FieldsList getSearchableCustomFieldDefs() {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			ObjCustomFieldDefs.FieldsList scfields = new ObjCustomFieldDefs.FieldsList();
			for (CustomFieldEx cfield : coreMgr.listCustomFields(SERVICE_ID, true, null).values()) {
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
		synchronized(roots) {
			updateRootFoldersCache();
			updateFoldersCache();
			
			// HANDLE TRANSITION: cleanup code when process is completed!
			StringSet checkedRoots = us.getCheckedCategoryRoots();
			if (checkedRoots != null) { // Migration code... (remove after migration)
				List<String> toInactive = roots.keySet().stream()
						.filter(shareId -> !checkedRoots.contains(shareId))
						.collect(Collectors.toList());
				inactiveRoots = new StringSet(toInactive);
				us.setInactiveCategoryRoots(inactiveRoots);
				us.clearCheckedCategoryRoots();
				
			} else { // New code... (keep after migrarion)
				inactiveRoots = us.getInactiveCategoryRoots();
				// Clean-up orphans
				if (inactiveRoots.removeIf(shareId -> !roots.containsKey(shareId))) {
					us.setInactiveCategoryRoots(inactiveRoots);
				}
			}
			
			IntegerSet checkedFolders = us.getCheckedCategoryFolders();
			if (checkedFolders != null) { // Migration code... (remove after migration)
				List<Integer> toInactive = folders.keySet().stream()
						.filter(categoryId -> !checkedFolders.contains(categoryId))
						.collect(Collectors.toList());
				inactiveFolders = new IntegerSet(toInactive);
				us.setInactiveCategoryFolders(inactiveFolders);
				us.clearCheckedCategoryFolders();
				
			} else { // New code... (keep after migrarion)
				inactiveFolders = us.getInactiveCategoryFolders();
				// Clean-up orphans
				if (inactiveFolders.removeIf(categoryId -> !folders.containsKey(categoryId))) {
					us.setInactiveCategoryFolders(inactiveFolders);
				}
			}
		}
	}
	
	private void updateRootFoldersCache() throws WTException {
		UserProfileId pid = getEnv().getProfile().getId();
		synchronized(roots) {
			roots.clear();
			roots.put(MyShareRootCategory.SHARE_ID, new MyShareRootCategory(pid));
			for (ShareRootCategory root : manager.listIncomingCategoryRoots()) {
				roots.put(root.getShareId(), root);
			}
		}
	}
	
	private void updateFoldersCache() throws WTException {
		synchronized(roots) {
			foldersByRoot.clear();
			folders.clear();
			rootByFolder.clear();
			for (ShareRootCategory root : roots.values()) {
				foldersByRoot.put(root.getShareId(), new ArrayList<ShareFolderCategory>());
				if (root instanceof MyShareRootCategory) {
					for (Category cat : manager.listMyCategories().values()) {
						final MyShareFolderCategory fold = new MyShareFolderCategory(root.getShareId(), cat);
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(cat.getCategoryId(), fold);
						rootByFolder.put(cat.getCategoryId(), root);
					}
				} else {
					for (ShareFolderCategory fold : manager.listIncomingCategoryFolders(root.getShareId()).values()) {
						final int catId = fold.getCategory().getCategoryId();
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(catId, fold);
						folderProps.put(catId, manager.getCategoryCustomProps(catId));
						rootByFolder.put(catId, root);
					}
				}
			}
		}
	}
	
	public void processManageFoldersTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				boolean chooser = ServletUtils.getBooleanParameter(request, "chooser", false);
				
				if (node.equals("root")) { // Node: root -> list roots
					for (ShareRootCategory root : roots.values()) {
						children.add(createRootNode(chooser, root));
					}
				} else { // Node: folder -> list folders (categories)
					boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", false);
					ShareRootCategory root = roots.get(node);
					
					Integer defltCategoryId = manager.getDefaultCategoryId();
					if (root instanceof MyShareRootCategory) {
						for (Category cat : manager.listMyCategories().values()) {
							MyShareFolderCategory folder = new MyShareFolderCategory(node, cat);
							if (writableOnly && !folder.getElementsPerms().implies("CREATE")) continue;
							
							final boolean isDefault = folder.getCategory().getCategoryId().equals(defltCategoryId);
							children.add(createFolderNode(chooser, folder, null, root.getPerms(), isDefault));
						}
					} else {
						if (foldersByRoot.containsKey(root.getShareId())) {
							for (ShareFolderCategory folder : foldersByRoot.get(root.getShareId())) {
								if (writableOnly && !folder.getElementsPerms().implies("CREATE")) continue;
								
								final boolean isDefault = folder.getCategory().getCategoryId().equals(defltCategoryId);
								final CategoryPropSet pset = folderProps.get(folder.getCategory().getCategoryId());
								final ExtTreeNode etn = createFolderNode(chooser, folder, pset, root.getPerms(), isDefault);
								if (etn != null) children.add(etn);
							}
						}
					}
				}
				new JsonResult("children", children).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for (JsFolderNode node : pl.data) {
					if (node._type.equals(JsFolderNode.TYPE_ROOT)) {
						toggleActiveRoot(node.id, node._active);
						
					} else if (node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						toggleActiveFolder(Integer.valueOf(cid.getToken(1)), node._active);
					}
				}
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for (JsFolderNode node : pl.data) {
					if (node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						manager.deleteCategory(Integer.valueOf(cid.getToken(1)));
					}
				}
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageFoldersTree", ex);
		}
	}
	
	public void processLookupCategoryRoots(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", true);
			
			for (ShareRootCategory root : roots.values()) {
				if (root instanceof MyShareRootCategory) {
					UserProfile up = getEnv().getProfile();
					items.add(new JsSimple(up.getStringId(), up.getDisplayName()));
				} else {
					//TODO: se writableOnly verificare che il gruppo condiviso sia scrivibile
					items.add(new JsSimple(root.getOwnerProfileId().toString(), root.getDescription()));
				}
			}
			
			new JsonResult("roots", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupCategoryRoots", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCategoryFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCategoryLkp> items = new ArrayList<>();
		
		try {
			Integer defltCategoryId = manager.getDefaultCategoryId();
			synchronized(roots) {
				for (ShareRootCategory root : roots.values()) {
					if (foldersByRoot.containsKey(root.getShareId())) {
						for (ShareFolderCategory folder : foldersByRoot.get(root.getShareId())) {
							final boolean isDefault = folder.getCategory().getCategoryId().equals(defltCategoryId);
							items.add(new JsCategoryLkp(root, folder, folderProps.get(folder.getCategory().getCategoryId()), isDefault, items.size()));
						}
					}
				}
			}
			new JsonResult("folders", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in LookupCategoryFolders", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				Sharing sharing = manager.getSharing(id);
				String description = buildSharingPath(sharing);
				new JsonResult(new JsSharing(sharing, description)).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Sharing> pl = ServletUtils.getPayload(request, Sharing.class);
				
				manager.updateSharing(pl.data);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageCategory(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Category item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", true);
				
				item = manager.getCategory(id);
				new JsonResult(new JsCategory(item)).printTo(out);
				
			} else if (crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				item = manager.addCategory(JsCategory.createFolder(pl.data));
				updateFoldersCache();
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.updateCategory(JsCategory.createFolder(pl.data));
				updateFoldersCache();
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.deleteCategory(pl.data.categoryId);
				updateFoldersCache();
				toggleActiveFolder(pl.data.categoryId, true); // forgets it by simply activating it
				new JsonResult().printTo(out);
				
			} else if (crud.equals("updateTag")) {
				int id = ServletUtils.getIntParameter(request, "id", true);
				UpdateTagsOperation op = ServletUtils.getEnumParameter(request, "op", true, UpdateTagsOperation.class);
				ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
				
				manager.updateTaskCategoryTags(op, id, new HashSet<>(tags));
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCategory", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processManageHiddenCategories(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if (crud.equals(Crud.READ)) {
				String rootId = ServletUtils.getStringParameter(request, "rootId", true);
				if (rootId.equals(MyShareRootCategory.SHARE_ID)) throw new WTException("Personal root is not supported");
				
				ArrayList<JsSimple> items = new ArrayList<>();
				synchronized(roots) {
					for (ShareFolderCategory folder : foldersByRoot.get(rootId)) {
						CategoryPropSet pset = folderProps.get(folder.getCategory().getCategoryId());
						if ((pset != null) && pset.getHiddenOrDefault(false)) {
							items.add(new JsSimple(folder.getCategory().getCategoryId(), folder.getCategory().getName()));
						}
					}
				}
				new JsonResult(items).printTo(out);
				
			} else if (crud.equals(Crud.UPDATE)) {
				Integer categoryId = ServletUtils.getIntParameter(request, "categoryId", true);
				Boolean hidden = ServletUtils.getBooleanParameter(request, "hidden", false);
				
				updateCategoryFolderVisibility(categoryId, hidden);
				new JsonResult().printTo(out);
				
			} else if (crud.equals(Crud.DELETE)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				HashSet<String> pids = new HashSet<>();
				synchronized(roots) {
					for (String id : ids) {
						int categoryId = Integer.valueOf(id);
						ShareFolderCategory fold = folders.get(categoryId);
						if (fold != null) {
							updateCategoryFolderVisibility(categoryId, null);
							pids.add(fold.getCategory().getProfileId().toString());
						}
					}
				}
				new JsonResult(pids).printTo(out);
			}
			
		} catch(Exception ex) {
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processSetDefaultCategory(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			
			us.setDefaultCategoryFolder(id);
			Integer defltCategoryId = manager.getDefaultCategoryId();
			new JsonResult(String.valueOf(defltCategoryId)).printTo(out);
				
		} catch(Throwable t) {
			logger.error("Error in SetDefaultCategory", t);
			new JsonResult(t).printTo(out);
		}
	}
	
	public void processSetCategoryColor(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String color = ServletUtils.getStringParameter(request, "color", null);
			
			updateCategoryFolderColor(id, color);
			new JsonResult().printTo(out);
				
		} catch(Throwable t) {
			logger.error("Error in SetCategoryColor", t);
			new JsonResult(t).printTo(out);
		}
	}
				
	public void processSetCategorySync(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String sync = ServletUtils.getStringParameter(request, "sync", null);
			
			updateCategoryFolderSync(id, EnumUtils.forSerializedName(sync, Category.Sync.class));
			new JsonResult().printTo(out);
				
		} catch(Throwable t) {
			logger.error("Error in SetCategorySync", t);
			new JsonResult(t).printTo(out);
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
			for (TaskLookupInstance instance : manager.listTaskInstances(categoryIds, pred, null, userTimeZone)) {
				items.add(new JsSimple(instance.getIdAsString(), instance.getSubject()));
			}
			new JsonResult(items).printTo(out);
			
		} catch(Throwable t) {
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
				QueryObj queryObj = ServletUtils.getObjectParameter(request, "query", new QueryObj(), QueryObj.class);
				if (view == null) queryObj = applyTasksQueryObjDefaults(queryObj);
				SortMeta.List sortMeta = ServletUtils.getObjectParameter(request, "sort", new SortMeta.List(), SortMeta.List.class);
				
				ArrayList<JsGridTask> items = new ArrayList<>();
				Set<String> parents = new HashSet<>();
				Map<String, CustomField.Type> map = cacheSearchableCustomFieldType.shallowCopy();
				List<Integer> visibleCategoryIds = getActiveFolderIds();
				SortInfo sortInfo = !sortMeta.isEmpty() ? sortMeta.get(0).toSortInfo() : SortInfo.asc("start");
				for (TaskLookupInstance instance : manager.listTaskInstances(visibleCategoryIds, view, null, TaskQuery.toCondition(queryObj, map, userTimeZone), sortInfo, userTimeZone)) {
					final ShareRootCategory root = rootByFolder.get(instance.getCategoryId());
					if (root == null) continue;
					final ShareFolderCategory fold = folders.get(instance.getCategoryId());
					if (fold == null) continue;
					
					CategoryPropSet pset = folderProps.get(instance.getCategoryId());
					
					JsGridTask js = null;
					if (!instance.getHasChildren() && instance.getParentInstanceId() == null) { // simple task
						js = new JsGridTask(fold, pset, instance, null, 0, userTimeZone);
					} else if (instance.getHasChildren()) { // parent task
						parents.add(instance.getTaskId());
						js = new JsGridTask(fold, pset, instance, JsGridTask.Hierarchy.PARENT, 0, userTimeZone);
					} else if (instance.getParentInstanceId() != null) { // child task
						boolean parentInResultset = parents.contains(instance.getParentInstanceId().getTaskId());
						js = new JsGridTask(fold, pset, instance, JsGridTask.Hierarchy.CHILD, parentInResultset ? 1 : 0, userTimeZone);
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
		
		} catch(Throwable t) {
			logger.error("Error in ManageGridTasks", t);
			new JsonResult(t).printTo(out);
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
						final ShareRootCategory root = rootByFolder.get(instance.getCategoryId());
						if (root == null) continue;
						final ShareFolderCategory fold = folders.get(instance.getCategoryId());
						if (fold == null) continue;
						final CategoryPropSet pset = folderProps.get(instance.getCategoryId());
						
						boolean showNested = false;
						if (instance.isParent()) {
							lastParent = instance.getTaskId();
						} else if (instance.isChild()) {
							showNested = StringUtils.equals(instance.getParentInstanceId().getTaskId(), lastParent);
						}
						items.add(new RBTaskList(root, fold.getCategory(), pset, instance, userTimeZone, showNested));
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
					for (TaskLookupInstance instance : manager.listTaskInstances(visibleCategoryIds, view, null, TaskQuery.toCondition(queryObj, map, userTimeZone), sortInfo, userTimeZone)) {
						final ShareRootCategory root = rootByFolder.get(instance.getCategoryId());
						if (root == null) continue;
						final ShareFolderCategory fold = folders.get(instance.getCategoryId());
						if (fold == null) continue;
						final CategoryPropSet pset = folderProps.get(instance.getCategoryId());
						
						boolean showNested = false;
						if (instance.isParent()) {
							lastParent = instance.getTaskId();
						} else if (instance.isChild()) {
							showNested = StringUtils.equals(instance.getParentInstanceId().getTaskId(), lastParent);
						}
						items.add(new RBTaskList(root, fold.getCategory(), pset, instance, userTimeZone, showNested));
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
					final ShareRootCategory root = rootByFolder.get(instance.getCategoryId());
					if (root == null) continue;
					final ShareFolderCategory fold = folders.get(instance.getCategoryId());
					if (fold == null) continue;
					final CategoryPropSet pset = folderProps.get(instance.getCategoryId());
					
					items.add(new RBTaskDetail(rrs, root, fold.getCategory(), pset, instance, userTimeZone));
				}
				rpt.setDataSource(items);
			}
			
			ServletUtils.setFileStreamHeaders(response, filename + ".pdf");
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, response.getOutputStream());
			
		} catch(Throwable t) {
			logger.error("Error in action PrintTasks", t);
			ServletUtils.writeErrorHandlingJs(response, t.getMessage());
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
				
				ShareRootCategory root = rootByFolder.get(task.getCategoryId());
				if (root == null) throw new WTException("Root not found [{}]", task.getCategoryId());
				ShareFolderCategory folder = folders.get(task.getCategoryId());
				if (folder == null) throw new WTException("Folder not found [{}]", task.getCategoryId());
				CategoryPropSet pset = folderProps.get(task.getCategoryId());

				Set<String> pvwfields = coreMgr.listCustomFieldIds(SERVICE_ID, null, true);
				Map<String, CustomPanel> cpanels = coreMgr.listCustomPanelsUsedBy(SERVICE_ID, task.getTags());
				Map<String, CustomField> cfields = new HashMap<>();
				for (CustomPanel cpanel : cpanels.values()) {
					for (String fieldId : cpanel.getFields()) {
						if (!pvwfields.contains(fieldId)) continue;
						CustomField cfield = coreMgr.getCustomField(SERVICE_ID, fieldId);
						if (cfield != null) cfields.put(fieldId, cfield);
					}
				}

				new JsonResult(new JsTaskPreview(root, folder, pset, task, cpanels.values(), cfields, up.getLanguageTag(), up.getTimeZone())).printTo(out);
			}
			
		} catch(Throwable t) {
			logger.error("Error in GetTaskPreview", t);
			new JsonResult(t).printTo(out);	
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
			
		} catch(Throwable t) {
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
							instanceIds.add(TaskInstanceId.build(id.getTaskId(), TaskInstanceId.MASTER_INSTANCE_ID));
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
				boolean copy = ServletUtils.getBooleanParameter(request, "copy", false);
				
				//TaskInstanceId instanceId = TaskInstanceId.parse(iid);
				//manager.moveTaskInstance(copy, instanceId, categoryId);
				manager.moveTaskInstance(copy, iids, categoryId);
				new JsonResult().printTo(out);
			}
			
		} catch(Throwable t) {
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
			
		} catch(Throwable t) {
			logger.error("Error in DownloadTaskAttachment", t);
			ServletUtils.writeErrorHandlingJs(response, t.getMessage());
		}
	}
	
	public void processGetCustomFieldsDefsData(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		CoreManager coreMgr = WT.getCoreManager();
		UserProfile up = getEnv().getProfile();
		
		try {
			ServletUtils.StringArray tags = ServletUtils.getObjectParameter(request, "tags", ServletUtils.StringArray.class, true);
			String iid = ServletUtils.getStringParameter(request, "iid", false);
			
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
			new JsonResult(new JsCustomFieldDefsData(cpanels.values(), cfields, cvalues, up.getLanguageTag(), up.getTimeZone())).printTo(out);
			
		} catch(Throwable t) {
			logger.error("Error in GetCustomFieldsDefsData", t);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processImportTasksFromICal(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		UserProfile up = getEnv().getProfile();
		
		try {
			String oid = ServletUtils.getStringParameter(request, "oid", true);
			String op = ServletUtils.getStringParameter(request, "op", true);
			String uploadId = ServletUtils.getStringParameter(request, "uploadId", true);
			
			UploadedFile upl = getUploadedFile(uploadId);
			if (upl == null) throw new WTException("Uploaded file not found [{0}]", uploadId);
			File file = new File(WT.getTempFolder(), upl.getUploadId());
			
			if (op.equals("do")) {
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
			
		} catch(Exception ex) {
			logger.error("Error in ImportContactsFromICal", ex);
			new JsonResult(false, ex.getMessage()).printTo(out);
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
			UserProfile userProfile = getEnv().getProfile();
			DateTimeZone userTimeZone = userProfile.getTimeZone();
			
			if (query == null) {
				final ShareRootCategory root = roots.get(MyShareRootCategory.SHARE_ID);
				final Set<Integer> ids = manager.listMyCategoryIds();
				List<TaskLookupInstance> instances = manager.listTaskInstances(ids, ITasksManager.TaskListView.UPCOMING, userTimeZone);
				//TODO: sort
				
				for (TaskLookupInstance instance : instances) {
					final ShareFolderCategory fold = folders.get(instance.getCategoryId());
					if (fold == null) continue;
					
					CategoryPropSet pset = folderProps.get(instance.getCategoryId());
					items.add(new JsPletTasks(root, fold, pset, instance, userTimeZone));
				}
				
			} else {
				String pattern = LangUtils.patternizeWords(query);
				for (TaskLookupInstance instance : manager.listTaskInstances(folders.keySet(), TaskQuery.toCondition(pattern), null, userTimeZone)) {
					final ShareRootCategory root = rootByFolder.get(instance.getCategoryId());
					if (root == null) continue;
					final ShareFolderCategory folder = folders.get(instance.getCategoryId());
					if (folder == null) continue;
					
					CategoryPropSet pset = folderProps.get(instance.getCategoryId());
					items.add(new JsPletTasks(root, folder, pset, instance, userTimeZone));
				}
			}
			
			new JsonResult(items).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in PortletTasks", ex);
			new JsonResult(false, "Error").printTo(out);	
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
	
	private String buildSharingPath(Sharing sharing) throws WTException {
		StringBuilder sb = new StringBuilder();
		
		// Root description part
		CompositeId cid = new CompositeId().parse(sharing.getId());
		if(roots.containsKey(cid.getToken(0))) {
			ShareRootCategory root = roots.get(cid.getToken(0));
			if(root instanceof MyShareRootCategory) {
				sb.append(lookupResource(TasksLocale.CATEGORIES_MY));
			} else {
				sb.append(root.getDescription());
			}
		}
		
		// Folder description part
		if(sharing.getLevel() == 1) {
			int catId = Integer.valueOf(cid.getToken(1));
			Category category = manager.getCategory(catId);
			sb.append("/");
			sb.append((category != null) ? category.getName() : cid.getToken(1));
		}
		
		return sb.toString();
	}
	
	private ArrayList<Integer> getActiveFolderIds() {
		ArrayList<Integer> ids = new ArrayList<>();
		synchronized(roots) {
			for (ShareRootCategory root : getActiveRoots()) {
				for (ShareFolderCategory folder : foldersByRoot.get(root.getShareId())) {
					if (inactiveFolders.contains(folder.getCategory().getCategoryId())) continue;
					ids.add(folder.getCategory().getCategoryId());
				}
			}
		}
		return ids;
	}
	
	private List<ShareRootCategory> getActiveRoots() {
		return roots.values().stream()
				.filter(root -> !inactiveRoots.contains(root.getShareId()))
				.collect(Collectors.toList());
	}
	
	private void toggleActiveRoot(String shareId, boolean active) {
		toggleActiveRoots(new String[]{shareId}, active);
	}
	
	private void toggleActiveRoots(String[] shareIds, boolean active) {
		synchronized(roots) {
			for (String shareId : shareIds) {
				if (active) {
					inactiveRoots.remove(shareId);
				} else {
					inactiveRoots.add(shareId);
				}
			}	
			us.setInactiveCategoryRoots(inactiveRoots);
		}
	}
	
	private void toggleActiveFolder(Integer folderId, boolean active) {
		toggleActiveFolders(new Integer[]{folderId}, active);
	}
	
	private void toggleActiveFolders(Integer[] folderIds, boolean active) {
		synchronized(roots) {
			for (int folderId : folderIds) {
				if (active) {
					inactiveFolders.remove(folderId);
				} else {
					inactiveFolders.add(folderId);
				}
			}
			us.setInactiveCategoryFolders(inactiveFolders);
		}
	}
	
	private void updateCategoryFolderVisibility(int categoryId, Boolean hidden) {
		synchronized(roots) {
			try {
				CategoryPropSet pset = manager.getCategoryCustomProps(categoryId);
				pset.setHidden(hidden);
				manager.updateCategoryCustomProps(categoryId, pset);
				
				// Update internal cache
				ShareFolderCategory folder = folders.get(categoryId);
				if (!(folder instanceof MyShareFolderCategory)) {
					folderProps.put(categoryId, pset);
				}
			} catch(WTException ex) {
				logger.error("Error saving custom category props", ex);
			}
		}
	}
	
	private void updateCategoryFolderColor(int categoryId, String color) throws WTException {
		synchronized(roots) {
			if (folders.get(categoryId) instanceof MyShareFolderCategory) {
				Category cat = manager.getCategory(categoryId);
				cat.setColor(color);
				manager.updateCategory(cat);
				updateFoldersCache();
			} else {
				CategoryPropSet pset = manager.getCategoryCustomProps(categoryId);
				pset.setColor(color);
				manager.updateCategoryCustomProps(categoryId, pset);
				folderProps.put(categoryId, pset);
			}
		}
	}
	
	private void updateCategoryFolderSync(int categoryId, Category.Sync sync) throws WTException {
		synchronized(roots) {
			if (folders.get(categoryId) instanceof MyShareFolderCategory) {
				Category cat = manager.getCategory(categoryId);
				cat.setSync(sync);
				manager.updateCategory(cat);
				updateFoldersCache();
			} else {
				CategoryPropSet pset = manager.getCategoryCustomProps(categoryId);
				pset.setSync(sync);
				manager.updateCategoryCustomProps(categoryId, pset);
				folderProps.put(categoryId, pset);
			}
		}
	}
	
	private ExtTreeNode createRootNode(boolean chooser, ShareRootCategory root) {
		if(root instanceof MyShareRootCategory) {
			return createRootNode(chooser, root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), lookupResource(TasksLocale.CATEGORIES_MY), false, "wttasks-icon-categoryMy")
					.setExpanded(true);
		} else {
			return createRootNode(chooser, root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), root.getDescription(), false, "wttasks-icon-categoryIncoming")
					.setExpanded(true);
		}
	}
	
	private ExtTreeNode createRootNode(boolean chooser, String id, String pid, String rights, String text, boolean leaf, String iconClass) {
		boolean active = !inactiveRoots.contains(id);
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", JsFolderNode.TYPE_ROOT);
		node.put("_pid", pid);
		node.put("_rrights", rights);
		node.put("_active", active);
		node.setIconClass(iconClass);
		if (!chooser) node.setChecked(active);
		node.put("expandable", false);
		
		return node;
	}
	
	private ExtTreeNode createFolderNode(boolean chooser, ShareFolderCategory folder, CategoryPropSet folderProps, SharePermsRoot rootPerms, boolean isDefault) {
		Category cat = folder.getCategory();
		String id = new CompositeId().setTokens(folder.getShareId(), cat.getCategoryId()).toString();
		String color = cat.getColor();
		Category.Sync sync = Category.Sync.OFF;
		boolean active = !inactiveFolders.contains(cat.getCategoryId());
		
		if (folderProps != null) { // Props are not null only for incoming folders
			if (folderProps.getHiddenOrDefault(false)) return null;
			color = folderProps.getColorOrDefault(color);
			sync = folderProps.getSyncOrDefault(sync);
		} else {
			sync = cat.getSync();
		}
		
		ExtTreeNode node = new ExtTreeNode(id, cat.getName(), true);
		node.put("_type", JsFolderNode.TYPE_FOLDER);
		node.put("_pid", cat.getProfileId().toString());
		node.put("_rrights", rootPerms.toString());
		node.put("_frights", folder.getPerms().toString());
		node.put("_erights", folder.getElementsPerms().toString());
		node.put("_catId", cat.getCategoryId());
		node.put("_builtIn", cat.getBuiltIn());
		node.put("_color", Category.getHexColor(color));
		node.put("_sync", EnumUtils.toSerializedName(sync));
		node.put("_default", isDefault);
		node.put("_active", active);
		if (!chooser) node.setChecked(active);
		
		return node;
	}
	
	private class SearchableCustomFieldTypeCache extends AbstractPassiveExpiringBulkMap<String, CustomField.Type> {
		
		public SearchableCustomFieldTypeCache(final long timeToLive, final TimeUnit timeUnit) {
			super(timeToLive, timeUnit);
		}
		
		@Override
		protected Map<String, CustomField.Type> internalGetMap() {
			try {
				CoreManager coreMgr = WT.getCoreManager();
				return coreMgr.listCustomFieldTypesById(SERVICE_ID, true);
				
			} catch(Throwable t) {
				logger.error("[SearchableCustomFieldTypeCache] Unable to build cache", t);
				throw new UnsupportedOperationException();
			}
		}
	}
}
