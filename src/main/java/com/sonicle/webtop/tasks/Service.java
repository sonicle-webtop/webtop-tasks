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

import com.sonicle.commons.web.Crud;
import com.sonicle.commons.web.ServletUtils;
import com.sonicle.commons.web.ServletUtils.IntegerArray;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.commons.web.json.PayloadAsList;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.Payload;
import com.sonicle.commons.web.json.extjs.ExtTreeNode;
import com.sonicle.webtop.core.CoreUserSettings;
import com.sonicle.webtop.tasks.TasksUserSettings.CheckedFolders;
import com.sonicle.webtop.tasks.TasksUserSettings.CheckedRoots;
import com.sonicle.webtop.tasks.bol.js.JsFolderNode;
import com.sonicle.webtop.tasks.bol.js.JsSharing;
import com.sonicle.webtop.tasks.model.CategoryFolder;
import com.sonicle.webtop.tasks.model.CategoryRoot;
import com.sonicle.webtop.tasks.bol.model.MyCategoryFolder;
import com.sonicle.webtop.tasks.bol.model.MyCategoryRoot;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.WebTopSession;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.tasks.bol.js.JsCategory;
import com.sonicle.webtop.tasks.bol.js.JsCategoryLkp;
import com.sonicle.webtop.tasks.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.tasks.bol.js.JsGridTask;
import com.sonicle.webtop.tasks.bol.js.JsPletTasks;
import com.sonicle.webtop.tasks.bol.js.JsTask;
import com.sonicle.webtop.tasks.bol.model.CategoryFolderData;
import com.sonicle.webtop.tasks.bol.model.RBTaskDetail;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.FolderTasks;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskEx;
import com.sonicle.webtop.tasks.rpt.RptTasksDetail;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
	public static final String WORK_VIEW = "w";
	public static final String HOME_VIEW = "h";
	
	private TasksManager manager;
	private TasksServiceSettings ss;
	private TasksUserSettings us;
	
	private final LinkedHashMap<String, CategoryRoot> roots = new LinkedHashMap<>();
	private final LinkedHashMap<Integer, CategoryFolder> folders = new LinkedHashMap<>();
	private final HashMap<String, ArrayList<CategoryFolder>> foldersByRoot = new HashMap<>();
	private final HashMap<Integer, CategoryRoot> rootByFolder = new HashMap<>();
	
	private CheckedRoots checkedRoots = null;
	private CheckedFolders checkedFolders = null;
	
	@Override
	public void initialize() throws Exception {
		UserProfile up = getEnv().getProfile();
		manager = (TasksManager)WT.getServiceManager(SERVICE_ID);
		ss = new TasksServiceSettings(SERVICE_ID, up.getDomainId());
		us = new TasksUserSettings(SERVICE_ID, up.getId());
		initFolders();
	}
	
	@Override
	public void cleanup() throws Exception {
		checkedFolders.clear();
		checkedFolders = null;
		checkedRoots.clear();
		checkedRoots = null;
		rootByFolder.clear();
		foldersByRoot.clear();
		folders.clear();
		roots.clear();
		us = null;
		ss = null;
		manager = null;
	}
	
	@Override
	public ServiceVars returnServiceVars() {
		ServiceVars co = new ServiceVars();
		co.put("defaultCategorySync", ss.getDefaultCategorySync().toString());
		return co;
	}
	
	private WebTopSession getWts() {
		return getEnv().getWebTopSession();
	}
	
	private void initFolders() throws WTException {
		synchronized(roots) {
			updateRootFoldersCache();
			updateFoldersCache();
			
			checkedRoots = us.getCheckedCategoryRoots();
			// If empty, adds MyNode checked by default!
			if(checkedRoots.isEmpty()) {
				checkedRoots.add(MyCategoryRoot.SHARE_ID);
				us.setCheckedCategoryRoots(checkedRoots);
			}
			checkedFolders = us.getCheckedCategoryFolders();
		}
	}
	
	private void updateRootFoldersCache() throws WTException {
		UserProfileId pid = getEnv().getProfile().getId();
		synchronized(roots) {
			roots.clear();
			roots.put(MyCategoryRoot.SHARE_ID, new MyCategoryRoot(pid));
			for(CategoryRoot root : manager.listIncomingCategoryRoots()) {
				roots.put(root.getShareId(), root);
			}
		}
	}
	
	private void updateFoldersCache() throws WTException {
		synchronized(roots) {
			foldersByRoot.clear();
			folders.clear();
			rootByFolder.clear();
			for(CategoryRoot root : roots.values()) {
				foldersByRoot.put(root.getShareId(), new ArrayList<CategoryFolder>());
				if(root instanceof MyCategoryRoot) {
					for(Category cat : manager.listCategories()) {
						final MyCategoryFolder fold = new MyCategoryFolder(root.getShareId(), cat);
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(cat.getCategoryId(), fold);
						rootByFolder.put(cat.getCategoryId(), root);
					}
				} else {
					for(CategoryFolder fold : manager.listIncomingCategoryFolders(root.getShareId()).values()) {
						final int catId = fold.getCategory().getCategoryId();
						fold.setData(us.getCategoryFolderData(catId));
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(catId, fold);
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
			if(crud.equals(Crud.READ)) {
				String node = ServletUtils.getStringParameter(request, "node", true);
				
				if(node.equals("root")) { // Node: root -> list roots
					for(CategoryRoot root : roots.values()) {
						children.add(createRootNode(root));
					}
				} else { // Node: folder -> list folders (categories)
					CategoryRoot root = roots.get(node);
					
					if(root instanceof MyCategoryRoot) {
						for(Category cal : manager.listCategories()) {
							MyCategoryFolder folder = new MyCategoryFolder(node, cal);
							children.add(createFolderNode(folder, root.getPerms()));
						}
					} else {
						/*
						HashMap<Integer, CategoryFolder> folds = manager.listIncomingCategoryFolders(root.getShareId());
						for(CategoryFolder fold : folds.values()) {
							children.add(createFolderNode(fold, root.getPerms()));
						}
						*/
						if(foldersByRoot.containsKey(root.getShareId())) {
							for(CategoryFolder fold : foldersByRoot.get(root.getShareId())) {
								final ExtTreeNode etn = createFolderNode(fold, root.getPerms());
								if (etn != null) children.add(etn);
							}
						}
					}
				}
				new JsonResult("children", children).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for(JsFolderNode node : pl.data) {
					if(node._type.equals(JsFolderNode.TYPE_ROOT)) {
						toggleCheckedRoot(node.id, node._visible);
						
					} else if(node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						toggleCheckedFolder(Integer.valueOf(cid.getToken(1)), node._visible);
					}
				}
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				PayloadAsList<JsFolderNodeList> pl = ServletUtils.getPayloadAsList(request, JsFolderNodeList.class);
				
				for(JsFolderNode node : pl.data) {
					if(node._type.equals(JsFolderNode.TYPE_FOLDER)) {
						CompositeId cid = new CompositeId().parse(node.id);
						manager.deleteCategory(Integer.valueOf(cid.getToken(1)));
					}
				}
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageFoldersTree", ex);
		}
	}
	
	public void processUpdateCheckedFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String rootId = ServletUtils.getStringParameter(request, "rootId", true);
			Boolean checked = ServletUtils.getBooleanParameter(request, "checked", true);
			
			synchronized(roots) {
				ArrayList<Integer> catIds = new ArrayList<>();
				for(CategoryFolder fold : foldersByRoot.get(rootId)) {
					catIds.add(fold.getCategory().getCategoryId());
				}
				toggleCheckedFolders(catIds.toArray(new Integer[catIds.size()]), checked);
			}
			
			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in UpdateCheckedFolders", ex);
			new JsonResult(ex).printTo(out);
		}
	}
	
	public void processLookupCategoryRoots(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsSimple> items = new ArrayList<>();
		
		try {
			Boolean writableOnly = ServletUtils.getBooleanParameter(request, "writableOnly", true);
			
			for(CategoryRoot root : roots.values()) {
				if(root instanceof MyCategoryRoot) {
					UserProfile up = getEnv().getProfile();
					items.add(new JsSimple(up.getStringId(), up.getDisplayName()));
				} else {
					//TODO: se writableOnly verificare che il gruppo condiviso sia scrivibile
					items.add(new JsSimple(root.getOwnerProfileId().toString(), root.getDescription()));
				}
			}
			
			new JsonResult("roots", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action LookupCategoryRoots", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processLookupCategoryFolders(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		List<JsCategoryLkp> items = new ArrayList<>();
		
		try {
			for(CategoryRoot root : roots.values()) {
				if(root instanceof MyCategoryRoot) {
					for(Category cal : manager.listCategories()) {
						items.add(new JsCategoryLkp(cal));
					}
				} else {
					if(foldersByRoot.containsKey(root.getShareId())) {
						for(CategoryFolder fold : foldersByRoot.get(root.getShareId())) {
							if(!fold.getElementsPerms().implies("CREATE")) continue;
							items.add(new JsCategoryLkp(fold));
						}
					}
				}
			}
			new JsonResult("folders", items, items.size()).printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in action LookupCategoryFolders", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageSharing(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				Sharing sharing = manager.getSharing(id);
				String description = buildSharingPath(sharing);
				new JsonResult(new JsSharing(sharing, description)).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, Sharing> pl = ServletUtils.getPayload(request, Sharing.class);
				
				manager.updateSharing(pl.data);
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageHiddenCategories(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String rootId = ServletUtils.getStringParameter(request, "rootId", true);
				if (rootId.equals(MyCategoryRoot.SHARE_ID)) throw new WTException();
				
				ArrayList<JsSimple> items = new ArrayList<>();
				synchronized(roots) {
					for(CategoryFolder fold : foldersByRoot.get(rootId)) {
						CategoryFolderData data = (CategoryFolderData)fold.getData();
						if (data != null) {
							if ((data.hidden != null) && data.hidden) {
								items.add(new JsSimple(fold.getCategory().getCategoryId(), fold.getCategory().getName()));
							}
						}
					}
				}
				new JsonResult(items).printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Integer categoryId = ServletUtils.getIntParameter(request, "categoryId", true);
				Boolean hidden = ServletUtils.getBooleanParameter(request, "hidden", false);
				
				updateCategoryFolderVisibility(categoryId, hidden);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				ServletUtils.StringArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.StringArray.class, true);
				
				HashSet<String> pids = new HashSet<>();
				synchronized(roots) {
					for(String id : ids) {
						int categoryId = Integer.valueOf(id);
						CategoryFolder fold = folders.get(categoryId);
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
	
	public void processManageCategories(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		Category item = null;
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				Integer id = ServletUtils.getIntParameter(request, "id", true);
				
				item = manager.getCategory(id);
				new JsonResult(new JsCategory(item)).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				item = manager.addCategory(JsCategory.createFolder(pl.data));
				updateFoldersCache();
				toggleCheckedFolder(item.getCategoryId(), true);
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.updateCategory(JsCategory.createFolder(pl.data));
				updateFoldersCache();
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.deleteCategory(pl.data.categoryId);
				updateFoldersCache();
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageCategories", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processSetCategoryColor(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		try {
			Integer id = ServletUtils.getIntParameter(request, "id", true);
			String color = ServletUtils.getStringParameter(request, "color", null);
			
			updateCategoryFolderColor(id, color);

			new JsonResult().printTo(out);
			
		} catch(Exception ex) {
			logger.error("Error in SetCategoryColor", ex);
			new JsonResult(ex).printTo(out);
		}
	}

	public void processManageGridTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsGridTask> items = new ArrayList<>();
		
		try {
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", null);
				String pattern = (query == null) ? null : ("%" + query.toLowerCase() + "%");
				
				List<Integer> visibleCategoryIds = getVisibleFolderIds(true);
				List<FolderTasks> foTasksObjs = manager.listFolderTasks(visibleCategoryIds, pattern);
				for (FolderTasks foTasksObj : foTasksObjs) {
					final CategoryFolder fold = folders.get(foTasksObj.folder.getCategoryId());
					if (fold == null) continue;
					
					for (TaskEx te : foTasksObj.tasks) {
						items.add(new JsGridTask(fold, te, DateTimeZone.UTC));
					}
				}
				new JsonResult("tasks", items).printTo(out);
			}
		
		} catch(Exception ex) {
			logger.error("Error in ManageGridTasks", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	    
	public void processManageTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		JsTask item = null;
		
		try {
			DateTimeZone ptz = getEnv().getProfile().getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				
				int taskId = Integer.parseInt(id);
				Task task = manager.getTask(taskId);
				UserProfileId ownerId = manager.getCategoryOwner(task.getCategoryId());
				item = new JsTask(ownerId, task, DateTimeZone.UTC);
				
				new JsonResult(item).printTo(out);
				
			} else if(crud.equals(Crud.CREATE)) {
				Payload<MapItem, JsTask> pl = ServletUtils.getPayload(request, JsTask.class);
				
				Task task = JsTask.createTask(pl.data, ptz);
                manager.addTask(task);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.UPDATE)) {
				Payload<MapItem, JsTask> pl = ServletUtils.getPayload(request, JsTask.class);
				
				Task task = JsTask.createTask(pl.data, ptz);
                manager.updateTask(task);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				IntegerArray ids = ServletUtils.getObjectParameter(request, "ids", IntegerArray.class, true);
				
				manager.deleteTask(ids);
				
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.MOVE)) {
				String id = ServletUtils.getStringParameter(request, "id", true);
				Integer categoryId = ServletUtils.getIntParameter(request, "targetCategoryId", true);
				boolean copy = ServletUtils.getBooleanParameter(request, "copy", false);
				
				int taskId = Integer.parseInt(id);
				manager.moveTask(copy, taskId, categoryId);
				
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in ManageTasks", ex);
			new JsonResult(false, "Error").printTo(out);	
		}
	}
	
	public void processPrintTasksDetail(HttpServletRequest request, HttpServletResponse response) {
		ArrayList<RBTaskDetail> items = new ArrayList<>();
		ByteArrayOutputStream baos = null;
		
		try {
			String filename = ServletUtils.getStringParameter(request, "filename", "print");
			ServletUtils.IntegerArray ids = ServletUtils.getObjectParameter(request, "ids", ServletUtils.IntegerArray.class, true);
			
			Task task = null;
			Category category = null;
			for(Integer id : ids) {
				task = manager.getTask(id);
				if (task == null) continue;
				
				category = manager.getCategory(task.getCategoryId());
				items.add(new RBTaskDetail(category, task));
			}
			
			ReportConfig.Builder builder = reportConfigBuilder();
			RptTasksDetail rpt = new RptTasksDetail(builder.build());
			rpt.setDataSource(items);
			
			baos = new ByteArrayOutputStream();
			WT.generateReportToStream(rpt, AbstractReport.OutputType.PDF, baos);
			ServletUtils.setContentDispositionHeader(response, "inline", filename + ".pdf");
			ServletUtils.writeContent(response, baos, "application/pdf");
			
		} catch(Exception ex) {
			logger.error("Error in action PrintTasksDetail", ex);
			ServletUtils.writeErrorHandlingJs(response, ex.getMessage());
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
	
	public void processPortletTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsPletTasks> items = new ArrayList<>();
		
		try {
			String query = ServletUtils.getStringParameter(request, "query", null);
			
			if (query == null) {
				List<Integer> ids = manager.listCategoryIds();
				List<TaskEx> tasks = manager.listUpcomingTasks(ids, "%");
				for(TaskEx task : tasks) {
					final CategoryFolder folder = folders.get(task.getCategoryId());
					if (folder == null) continue;
					items.add(new JsPletTasks(folder, task, DateTimeZone.UTC));
				}
				
			} else {
				String pattern = "%" + query.toLowerCase() + "%";
				//TODO: implement this fork
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
			CategoryRoot root = roots.get(cid.getToken(0));
			if(root instanceof MyCategoryRoot) {
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
	
	private ArrayList<Integer> getVisibleFolderIds(boolean cleanupOrphans) {
		ArrayList<Integer> ids = new ArrayList<>();
		ArrayList<Integer> orphans = new ArrayList<>();
		
		Integer[] checked = getCheckedFolders();
		for (CategoryRoot root : getCheckedRoots()) {
			for (Integer folderId : checked) {
				final CategoryRoot folderRoot = rootByFolder.get(folderId);
				if (folderRoot == null) {
					if (cleanupOrphans) orphans.add(folderId);
					continue;
				}
				
				if (root.getShareId().equals(folderRoot.getShareId())) {
					ids.add(folderId);
				}
			}
		}
		if (cleanupOrphans) toggleCheckedFolders(orphans.toArray(new Integer[orphans.size()]), false);
		return ids;
	}
	
	private List<CategoryRoot> getCheckedRoots() {
		ArrayList<CategoryRoot> checked = new ArrayList<>();
		for(CategoryRoot root : roots.values()) {
			if(!checkedRoots.contains(root.getShareId())) continue; // Skip folder if not visible
			checked.add(root);
		}
		return checked;
	}
	
	private Integer[] getCheckedFolders() {
		return checkedFolders.toArray(new Integer[checkedFolders.size()]);
	}
	
	private void toggleCheckedRoot(String shareId, boolean checked) {
		toggleCheckedRoots(new String[]{shareId}, checked);
	}
	
	private void toggleCheckedRoots(String[] shareIds, boolean checked) {
		synchronized(roots) {
			for(String shareId : shareIds) {
				if(checked) {
					checkedRoots.add(shareId);
				} else {
					checkedRoots.remove(shareId);
				}
			}	
			us.setCheckedCategoryRoots(checkedRoots);
		}
	}
	
	private void toggleCheckedFolder(Integer folderId, boolean checked) {
		toggleCheckedFolders(new Integer[]{folderId}, checked);
	}
	
	private void toggleCheckedFolders(Integer[] folderIds, boolean checked) {
		synchronized(roots) {
			for(int folderId : folderIds) {
				if(checked) {
					checkedFolders.add(folderId);
				} else {
					checkedFolders.remove(folderId);
				}
			}
			us.setCheckedCategoryFolders(checkedFolders);
		}
	}
	
	private void updateCategoryFolderColor(int categoryId, String color) {
		synchronized(roots) {
			CategoryFolderData data = us.getCategoryFolderData(categoryId);
			data.color = color;
			if (!data.isNull()) {
				us.setCategoryFolderData(categoryId, data);
			} else {
				us.clearCategoryFolderData(categoryId);
			}
			
			// Update internal cache
			CategoryFolder folder = folders.get(categoryId);
			if (!(folder instanceof MyCategoryFolder)) {
				((CategoryFolderData)folder.getData()).update(data);
			}
		}
	}
	
	private void updateCategoryFolderVisibility(int categoryId, Boolean hidden) {
		synchronized(roots) {
			CategoryFolderData data = us.getCategoryFolderData(categoryId);
			data.hidden = hidden;
			if (!data.isNull()) {
				us.setCategoryFolderData(categoryId, data);
			} else {
				us.clearCategoryFolderData(categoryId);
			}
			
			// Update internal cache
			CategoryFolder folder = folders.get(categoryId);
			if (!(folder instanceof MyCategoryFolder)) {
				((CategoryFolderData)folder.getData()).update(data);
			}
		}
	}
	
	private ExtTreeNode createRootNode(CategoryRoot root) {
		if(root instanceof MyCategoryRoot) {
			return createRootNode(root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), lookupResource(TasksLocale.CATEGORIES_MY), false, "wttasks-icon-root-my-xs").setExpanded(true);
		} else {
			return createRootNode(root.getShareId(), root.getOwnerProfileId().toString(), root.getPerms().toString(), root.getDescription(), false, "wttasks-icon-root-incoming-xs");
		}
	}
	
	private ExtTreeNode createRootNode(String id, String pid, String rights, String text, boolean leaf, String iconClass) {
		boolean visible = checkedRoots.contains(id);
		ExtTreeNode node = new ExtTreeNode(id, text, leaf);
		node.put("_type", JsFolderNode.TYPE_ROOT);
		node.put("_pid", pid);
		node.put("_rrights", rights);
		node.put("_visible", visible);
		node.setIconClass(iconClass);
		node.setChecked(visible);
		return node;
	}
	
	private ExtTreeNode createFolderNode(CategoryFolder folder, SharePermsRoot rootPerms) {
		Category cat = folder.getCategory();
		String id = new CompositeId().setTokens(folder.getShareId(), cat.getCategoryId()).toString();
		String color = cat.getColor();
		boolean visible = checkedFolders.contains(cat.getCategoryId());
		
		if (folder.getData() != null) {
			CategoryFolderData data = (CategoryFolderData)folder.getData();
			if ((data.hidden != null) && data.hidden) return null;
			if (!StringUtils.isBlank(data.color)) color = data.color;
		}
		
		ExtTreeNode node = new ExtTreeNode(id, cat.getName(), true);
		node.put("_type", JsFolderNode.TYPE_FOLDER);
		node.put("_pid", cat.getProfileId().toString());
		node.put("_rrights", rootPerms.toString());
		node.put("_frights", folder.getPerms().toString());
		node.put("_erights", folder.getElementsPerms().toString());
		node.put("_catId", cat.getCategoryId());
		node.put("_builtIn", cat.getBuiltIn());
		node.put("_default", cat.getIsDefault());
		node.put("_color", Category.getHexColor(color));
		node.put("_visible", visible);
		
		List<String> classes = new ArrayList<>();
		if(cat.getIsDefault()) classes.add("wttasks-tree-default");
		if(!folder.getElementsPerms().implies("CREATE") 
				&& !folder.getElementsPerms().implies("UPDATE")
				&& !folder.getElementsPerms().implies("DELETE")) classes.add("wttasks-tree-readonly");
		node.setCls(StringUtils.join(classes, " "));
		
		node.setIconClass("wt-palette-" + Category.getHexColor(color));
		node.setChecked(visible);
		return node;
	}
}
