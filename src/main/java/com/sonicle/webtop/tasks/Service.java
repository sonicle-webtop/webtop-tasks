/*
 * webtop-tasks is a WebTop Service developed by Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
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
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.js.JsFolderNode;
import com.sonicle.webtop.tasks.bol.js.JsSharing;
import com.sonicle.webtop.tasks.bol.model.CategoryFolder;
import com.sonicle.webtop.tasks.bol.model.CategoryRoot;
import com.sonicle.webtop.tasks.bol.model.MyCategoryFolder;
import com.sonicle.webtop.tasks.bol.model.MyCategoryRoot;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.js.JsSimple;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import com.sonicle.webtop.core.sdk.BaseService;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.bol.js.JsCategory;
import com.sonicle.webtop.tasks.bol.js.JsCategoryLkp;
import com.sonicle.webtop.tasks.bol.js.JsFolderNode.JsFolderNodeList;
import com.sonicle.webtop.tasks.bol.js.JsGridTask;
import com.sonicle.webtop.tasks.bol.js.JsTask;
import com.sonicle.webtop.tasks.bol.model.RBTaskDetail;
import com.sonicle.webtop.tasks.bol.model.Task;
import com.sonicle.webtop.tasks.rpt.RptTasksDetail;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
	private TasksUserSettings us;
	
	private final LinkedHashMap<String, CategoryRoot> roots = new LinkedHashMap<>();
	private final HashMap<String, ArrayList<CategoryFolder>> foldersByRoot = new HashMap<>();
	private final LinkedHashMap<Integer, CategoryFolder> folders = new LinkedHashMap<>();
	
	private CheckedRoots checkedRoots = null;
	private CheckedFolders checkedFolders = null;
	
	@Override
	public void initialize() throws Exception {
		UserProfile up = getEnv().getProfile();
		manager = (TasksManager)WT.getServiceManager(SERVICE_ID, up.getId());
		us = new TasksUserSettings(SERVICE_ID, up.getId());
		initFolders();
	}
	
	@Override
	public void cleanup() throws Exception {
		checkedFolders.clear();
		checkedFolders = null;
		checkedRoots.clear();
		checkedRoots = null;
		folders.clear();
		foldersByRoot.clear();
		roots.clear();
		us = null;
		manager = null;
	}
	
	@Override
	public ServiceVars returnServiceVars() {
		ServiceVars co = new ServiceVars();
		return co;
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
		UserProfile.Id pid = getEnv().getProfile().getId();
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
			for(CategoryRoot root : roots.values()) {
				foldersByRoot.put(root.getShareId(), new ArrayList<CategoryFolder>());
				if(root instanceof MyCategoryRoot) {
					for(OCategory cat : manager.listCategories()) {
						MyCategoryFolder fold = new MyCategoryFolder(root.getShareId(), cat);
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(cat.getCategoryId(), fold);
					}
				} else {
					for(CategoryFolder fold : manager.listIncomingCategoryFolders(root.getShareId()).values()) {
						foldersByRoot.get(root.getShareId()).add(fold);
						folders.put(fold.getCategory().getCategoryId(), fold);
					}
				}
			}
		}
	}
	
	public void processManageFoldersTree(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<ExtTreeNode> children = new ArrayList<>();
		ExtTreeNode child = null;
		
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
						for(OCategory cal : manager.listCategories()) {
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
								children.add(createFolderNode(fold, root.getPerms()));
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
					for(OCategory cal : manager.listCategories()) {
						items.add(new JsCategoryLkp(cal));
					}
				} else {
					if(foldersByRoot.containsKey(root.getShareId())) {
						for(CategoryFolder fold : foldersByRoot.get(root.getShareId())) {
							if(!fold.getElementsPerms().implies("CREATE")) continue;
							items.add(new JsCategoryLkp(fold.getCategory()));
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
			logger.error("Error in action ManageSharing", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}
	
	public void processManageCategories(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		OCategory item = null;
		
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
				new JsonResult().printTo(out);
				
			} else if(crud.equals(Crud.DELETE)) {
				Payload<MapItem, JsCategory> pl = ServletUtils.getPayload(request, JsCategory.class);
				
				manager.deleteCategory(pl.data.categoryId);
				updateFoldersCache();
				new JsonResult().printTo(out);
			}
			
		} catch(Exception ex) {
			logger.error("Error in action ManageCategories", ex);
			new JsonResult(false, "Error").printTo(out);
		}
	}

	public void processManageGridTasks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
		ArrayList<JsGridTask> items = new ArrayList<>();
		
		try {
			UserProfile up = getEnv().getProfile();
			//DateTimeZone utz = up.getTimeZone();
			
			String crud = ServletUtils.getStringParameter(request, "crud", true);
			if(crud.equals(Crud.READ)) {
				String query = ServletUtils.getStringParameter(request, "query", null);
				String pattern = (query == null) ? "%" : ("%" + query.toLowerCase() + "%");
				
				List<TasksManager.CategoryTasks> foldTasks = null;
				Integer[] checked = getCheckedFolders();
				for (CategoryRoot root : getCheckedRoots()) {
					foldTasks = manager.listTasks(root, checked, pattern);
					// Iterates over category->tasks
					for (TasksManager.CategoryTasks foldTask : foldTasks) {
						CategoryFolder fold = folders.get(foldTask.folder.getCategoryId());
                        if (fold == null) continue;
                        for (VTask vt : foldTask.tasks) {
                            items.add(new JsGridTask(fold,vt,DateTimeZone.UTC));
                        }
					}
				}
				new JsonResult("tasks", items).printTo(out);
			}
		
		} catch(Exception ex) {
			logger.error("Error in action ManageGridTasks", ex);
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
				UserProfile.Id ownerId = manager.getCategoryOwner(task.getCategoryId());
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
			logger.error("Error in action ManageTasks", ex);
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
			OCategory category = null;
			for(Integer id : ids) {
				task = manager.getTask(id);
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
			OCategory category = manager.getCategory(catId);
			sb.append("/");
			sb.append((category != null) ? category.getName() : cid.getToken(1));
		}
		
		return sb.toString();
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
		synchronized(roots) {
			if(checked) {
				checkedRoots.add(shareId);
			} else {
				checkedRoots.remove(shareId);
			}
			us.setCheckedCategoryRoots(checkedRoots);
		}
	}
	
	private void toggleCheckedFolder(int folderId, boolean checked) {
		synchronized(roots) {
			if(checked) {
				checkedFolders.add(folderId);
			} else {
				checkedFolders.remove(folderId);
			}
			us.setCheckedCategoryFolders(checkedFolders);
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
		OCategory cat = folder.getCategory();
		String id = new CompositeId().setTokens(folder.getShareId(), cat.getCategoryId()).toString();
		boolean visible = checkedFolders.contains(cat.getCategoryId());
		ExtTreeNode node = new ExtTreeNode(id, cat.getName(), true);
		node.put("_type", JsFolderNode.TYPE_FOLDER);
		node.put("_pid", cat.getProfileId().toString());
		node.put("_rrights", rootPerms.toString());
		node.put("_frights", folder.getPerms().toString());
		node.put("_erights", folder.getElementsPerms().toString());
		node.put("_catId", cat.getCategoryId());
		node.put("_builtIn", cat.getBuiltIn());
		node.put("_default", cat.getIsDefault());
		node.put("_color", cat.getColor());
		node.put("_visible", visible);
		
		List<String> classes = new ArrayList<>();
		if(cat.getIsDefault()) classes.add("wttasks-tree-default");
		if(!folder.getElementsPerms().implies("CREATE") 
				&& !folder.getElementsPerms().implies("UPDATE")
				&& !folder.getElementsPerms().implies("DELETE")) classes.add("wttasks-tree-readonly");
		node.setCls(StringUtils.join(classes, " "));
		
		node.setIconClass("wt-palette-" + cat.getHexColor());
		node.setChecked(visible);
		return node;
	}
	
}
