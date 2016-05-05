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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.ServiceContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.Owner;
import com.sonicle.webtop.core.bol.model.IncomingShareRoot;
import com.sonicle.webtop.core.bol.model.SharePermsElements;
import com.sonicle.webtop.core.bol.model.SharePermsFolder;
import com.sonicle.webtop.core.bol.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTOperationException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.bol.model.CategoryFolder;
import com.sonicle.webtop.tasks.bol.model.CategoryRoot;
import com.sonicle.webtop.tasks.bol.model.Task;
import com.sonicle.webtop.tasks.dal.CategoryDAO;
import com.sonicle.webtop.tasks.dal.TaskDAO;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

/**
 *
 * @author rfullone
 */
public class TasksManager extends BaseManager {
	public static final Logger logger = WT.getLogger(TasksManager.class);
	private static final String RESOURCE_CATEGORY = "CATEGORY";
	
	private final HashMap<Integer, UserProfile.Id> cacheCategoryToOwner = new HashMap<>();
	private final Object shareCacheLock = new Object();
	private final HashMap<UserProfile.Id, String> cacheOwnerToRootShare = new HashMap<>();
	private final HashMap<UserProfile.Id, String> cacheOwnerToWildcardFolderShare = new HashMap<>();
	private final HashMap<Integer, String> cacheCategoryToFolderShare = new HashMap<>();

	public TasksManager(ServiceContext context) {
		super(context);
	}
	
	public TasksManager(ServiceContext context, UserProfile.Id targetProfileId) {
		super(context, targetProfileId);
	}
    
    private void writeLog(String action, String data) {
		CoreManager core = WT.getCoreManager(getServiceContext());
		core.setSoftwareName(getSoftwareName());
		core.writeLog(action, data);
	}
	
	public List<CategoryRoot> listIncomingCategoryRoots() throws WTException {
		CoreManager core = WT.getCoreManager(getServiceContext());
		ArrayList<CategoryRoot> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		
		List<IncomingShareRoot> shares = core.listIncomingShareRoots(SERVICE_ID, RESOURCE_CATEGORY);
		for(IncomingShareRoot share : shares) {
			SharePermsRoot perms = core.getShareRootPermissions(SERVICE_ID, RESOURCE_CATEGORY, share.getShareId());
			CategoryRoot root = new CategoryRoot(share, perms);
			if(hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public HashMap<Integer, CategoryFolder> listIncomingCategoryFolders(String rootShareId) throws WTException {
		CoreManager core = WT.getCoreManager(getServiceContext(), getTargetProfileId());
		LinkedHashMap<Integer, CategoryFolder> folders = new LinkedHashMap<>();
		
		// Retrieves incoming folders (from sharing). This lookup already 
		// returns readable shares (we don't need to test READ permission)
		List<OShare> shares = core.listIncomingShareFolders(rootShareId, SERVICE_ID, RESOURCE_CATEGORY);
		for(OShare share : shares) {
			
			List<OCategory> cats = null;
			if(share.hasWildcard()) {
				UserProfile.Id ownerId = core.userUidToProfileId(share.getUserUid());
				cats = listCategories(ownerId);
			} else {
				cats = Arrays.asList(getCategory(Integer.valueOf(share.getInstance())));
			}
			
			for(OCategory cat : cats) {
				SharePermsFolder fperms = core.getShareFolderPermissions(SERVICE_ID, RESOURCE_CATEGORY, share.getShareId().toString());
				SharePermsElements eperms = core.getShareElementsPermissions(SERVICE_ID, RESOURCE_CATEGORY, share.getShareId().toString());
				
				if(folders.containsKey(cat.getCategoryId())) {
					CategoryFolder folder = folders.get(cat.getCategoryId());
					folder.getPerms().merge(fperms);
					folder.getElementsPerms().merge(eperms);
				} else {
					folders.put(cat.getCategoryId(), new CategoryFolder(share.getShareId().toString(), fperms, eperms, cat));
				}
			}
		}
		return folders;
	}
	
	public Sharing getSharing(String shareId) throws WTException {
		CoreManager core = WT.getCoreManager(getServiceContext());
		return core.getSharing(getTargetProfileId(), SERVICE_ID, RESOURCE_CATEGORY, shareId);
	}
	
	public void updateSharing(Sharing sharing) throws WTException {
		CoreManager core = WT.getCoreManager(getServiceContext());
		core.updateSharing(getTargetProfileId(), SERVICE_ID, RESOURCE_CATEGORY, sharing);
	}
	
	public UserProfile.Id getCategoryOwner(int categoryId) throws WTException {
		return categoryToOwner(categoryId);
	}
	
	public List<OCategory> listCategories() throws WTException {
		return listCategories(getTargetProfileId());
	}
	
	private List<OCategory> listCategories(UserProfile.Id pid) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			CategoryDAO dao = CategoryDAO.getInstance();
			return dao.selectByDomainUser(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCategory getCategory(int categoryId) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryFolder(categoryId, "READ");
			con = WT.getConnection(getManifest());
			CategoryDAO dao = CategoryDAO.getInstance();
			return dao.selectById(con, categoryId);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCategory getBuiltInCategory() throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			CategoryDAO dao = CategoryDAO.getInstance();
			OCategory cat = dao.selectBuiltInByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if(cat == null) return null;
			checkRightsOnCategoryFolder(cat.getCategoryId(), "READ");
			return cat;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCategory addCategory(OCategory item) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryRoot(item.getProfileId(), "MANAGE");
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			
			item.setBuiltIn(false);
			item = doInsertCategory(con, item);
			DbUtils.commitQuietly(con);
			return item;
			
			/*
			CategoryDAO dao = CategoryDAO.getInstance();
			
			item.setCategoryId(dao.getSequence(con).intValue());
			item.setBuiltIn(false);
			if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
			dao.insert(con, item, createUpdateInfo());
			DbUtils.commitQuietly(con);
			return item;
			*/
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCategory addBuiltInCategory() throws WTException {
		Connection con = null;
		OCategory item = null;
		
		try {
			checkRightsOnCategoryRoot(getTargetProfileId(), "MANAGE");
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			CategoryDAO dao = CategoryDAO.getInstance();
			
			item = dao.selectBuiltInByDomainUser(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if(item != null) throw new WTOperationException("Built-in category already present");
			
			item = new OCategory();
			item.setDomainId(getTargetProfileId().getDomainId());
			item.setUserId(getTargetProfileId().getUserId());
			item.setBuiltIn(true);
			item.setName(WT.getPlatformName());
			item.setDescription("");
			item.setColor("#FFFFFF");
			item.setSync(true);
			item.setIsDefault(true);
			item = doInsertCategory(con, item);
			DbUtils.commitQuietly(con);
			return item;
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public OCategory updateCategory(OCategory item) throws Exception {
		Connection con = null;
		
		try {
			checkRightsOnCategoryFolder(item.getCategoryId(), "UPDATE");
			con = WT.getConnection(getManifest());
			con.setAutoCommit(false);
			CategoryDAO dao = CategoryDAO.getInstance();
			
			if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
			dao.update(con, item);
			DbUtils.commitQuietly(con);
			return item;
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteCategory(int categoryId) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryFolder(categoryId, "DELETE");
			con = WT.getConnection(getManifest());
			
			CategoryDAO dao = CategoryDAO.getInstance();
			dao.deleteById(con, categoryId);
			//TODO: cancellare contatti collegati
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	public List<CategoryTasks> listTasks(CategoryRoot root, Integer[] categoryFolders, String pattern) throws Exception {
		return listTasks(root.getOwnerProfileId(), categoryFolders, pattern);
	}
	
	public List<CategoryTasks> listTasks(UserProfile.Id pid, Integer[] categoryFolders, String pattern) throws Exception {
		CategoryDAO catdao = CategoryDAO.getInstance();
		TaskDAO tasdao = TaskDAO.getInstance();
		ArrayList<CategoryTasks> catTasks = new ArrayList<>();
		Connection con = null;
		
		try {
            // TODO: implementare filtro task privati
			con = WT.getConnection(getManifest());
			
			// Lists desired groups (tipically visibles) coming from passed list
			// Passed ids should belong to referenced folder(group), 
			// this is ensured using domainId and userId parameters in below query.
			List<OCategory> cats = catdao.selectByDomainUserIn(con, pid.getDomainId(), pid.getUserId(), categoryFolders);
			List<VTask> vts = null;
			for(OCategory cat : cats) {
				checkRightsOnCategoryFolder(cat.getCategoryId(), "READ");
				vts = tasdao.viewByCategoryPattern(con, cat.getCategoryId(), pattern);
				catTasks.add(new CategoryTasks(cat, vts));
			}
			return catTasks;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

    private Task createTask (OTask otask) throws WTException {
		try {
			Task task = new Task();
			BeanUtils.copyProperties(task, otask);
			return task;
		} catch (Exception ex) {
			throw new WTException(ex, "Error creating bean");
		}
    }
    
	public Task getTask(int taskId) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OTask otask = tdao.selectById(con, taskId);
			if(otask == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
			checkRightsOnCategoryFolder(otask.getCategoryId(), "READ"); // Rights check!
			
			return createTask(otask);
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} catch(WTException ex) {
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addTask(Task task) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryElements(task.getCategoryId(), "CREATE"); // Rights check!
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			OTask result = doInsertTask(con, task);
			DbUtils.commitQuietly(con);
			writeLog("TASK_INSERT", String.valueOf(result.getTaskId()));
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void updateTask(Task task) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryElements(task.getCategoryId(), "UPDATE"); // Rights check!
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			doUpdateTask(con, task);
			DbUtils.commitQuietly(con);
			writeLog("TASK_UPDATE", String.valueOf(task.getTaskId()));			
            
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
    
	public void deleteTasksByCategory(int categoryId) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryElements(categoryId, "DELETE"); // Rights check!
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			doDeleteTasksByCategory(con, categoryId);
			DbUtils.commitQuietly(con);
			writeLog("TASK_DELETE", "*");
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteTask(int taskId) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OTask cont = tdao.selectById(con, taskId);
			if(cont == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
			checkRightsOnCategoryElements(cont.getCategoryId(), "DELETE"); // Rights check!
			
			con.setAutoCommit(false);
			doDeleteTask(con, taskId);
			DbUtils.commitQuietly(con);
			writeLog("TASK_DELETE", String.valueOf(taskId));
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void deleteTask(ArrayList<Integer> taskIds) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			for(Integer taskId : taskIds) {
				if(taskId == null) continue;
				OTask task = tdao.selectById(con, taskId);
				if(task == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
				checkRightsOnCategoryElements(task.getCategoryId(), "DELETE"); // Rights check!
				
				doDeleteTask(con, taskId);
			}
			
			DbUtils.commitQuietly(con);
			writeLog("TASK_DELETE", "*");
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void moveTask(boolean copy, int taskId, int targetCategoryId) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OTask otask = tdao.selectById(con, taskId);
			if(otask == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
			checkRightsOnCategoryFolder(otask.getCategoryId(), "READ"); // Rights check!
			
			if(copy || (targetCategoryId != otask.getCategoryId())) {
				checkRightsOnCategoryElements(targetCategoryId, "CREATE"); // Rights check!
				
				Task task = createTask(otask);

				con.setAutoCommit(false);
				doMoveTask(con, copy, task, targetCategoryId);
				DbUtils.commitQuietly(con);
				writeLog("TASK_UPDATE", String.valueOf(task.getTaskId()));
			}
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch(Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	public List<BaseReminder> getRemindersToBeNotified(DateTime now) {
		ArrayList<BaseReminder> alerts = new ArrayList<>();
		HashMap<UserProfile.Id, Boolean> byEmailCache = new HashMap<>();
		TaskDAO dao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			DateTime now12 = now.plusHours(14);
			List<VTask> tasks = dao.viewExpridedForUpdateByUntil(con, now12);
			DateTime profileNow = null, profileReminderDate = null;
			for(VTask task : tasks) {
				UserProfile.Data ud = WT.getUserData(task.getCategoryProfileId());
				profileNow = now.withZone(ud.getTimeZone());
				profileReminderDate = task.getReminderDate().withZone(DateTimeZone.UTC).withZoneRetainFields(ud.getTimeZone());
				if(profileReminderDate.isAfter(profileNow)) continue;
				
				if(!byEmailCache.containsKey(task.getCategoryProfileId())) {
					TasksUserSettings us = new TasksUserSettings(SERVICE_ID, task.getCategoryProfileId());
					boolean bool = us.getTaskReminderDelivery().equals(TasksUserSettings.TASK_REMINDER_DELIVERY_EMAIL);
					byEmailCache.put(task.getCategoryProfileId(), bool);
				}

				int ret = dao.updateRemindedOn(con, task.getTaskId(), now);
				if(ret != 1) continue;
				
				if(byEmailCache.get(task.getCategoryProfileId())) {
					//UserProfile.Data ud = WT.getUserData(task.getCategoryProfileId());
					alerts.add(createTaskReminderAlertEmail(ud.getLocale(), task));
				} else {
					alerts.add(createTaskReminderAlertWeb(task, profileReminderDate, ud.getTimeZone()));
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch(Exception ex) {
			logger.error("Error collecting reminder alerts", ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return alerts;
	}
	
	private ReminderInApp createTaskReminderAlertWeb(VTask task, DateTime profileReminderDate, DateTimeZone profileTz) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, task.getCategoryProfileId(), "task", String.valueOf(task.getTaskId()));
		alert.setTitle(task.getSubject());
		alert.setDate(profileReminderDate);
		alert.setTimezone(profileTz.getID());
		return alert;
	}
	
	private ReminderEmail createTaskReminderAlertEmail(Locale locale, VTask task) {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, task.getCategoryProfileId(), "task", String.valueOf(task.getTaskId()));
		//TODO: completare email
		return alert;
	}
	
	private OTask doInsertTask(Connection con, Task task) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		OTask item = new OTask();
		try {
			BeanUtils.copyProperties(item, task);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new WTException(ex,"Error creating bean");
		}
		if(StringUtils.isEmpty(item.getPublicUid())) item.setPublicUid(IdentifierUtils.getUUID());
        item.setTaskId(tdao.getSequence(con).intValue());
        tdao.insert(con, item, createRevisionTimestamp());
        return item;
	}
	
	private void doUpdateTask(Connection con, Task task) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		OTask item = new OTask();
		try {
			BeanUtils.copyProperties(item, task);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new WTException(ex,"Error creating bean");
		}
        tdao.update(con, item, createRevisionTimestamp());
	}
	
	private int doDeleteTask(Connection con, int taskId) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		return tdao.logicDeleteById(con, taskId, createRevisionTimestamp());
	}
	
	private int doDeleteTasksByCategory(Connection con, int categoryId) throws WTException {
		TaskDAO tdao = TaskDAO.getInstance();
		return tdao.logicDeleteByCategoryId(con, categoryId, createRevisionTimestamp());
	}
	
	private void doMoveTask(Connection con, boolean copy, Task task, int targetCategoryId) throws WTException {
		if(copy) {
			task.setCategoryId(targetCategoryId);
            doInsertTask(con, task);
		} else {
			TaskDAO tdao = TaskDAO.getInstance();
			tdao.updateCategory(con, task.getTaskId(), targetCategoryId, createRevisionTimestamp());
		}
	}
	
    private OCategory doInsertCategory(Connection con, OCategory item) throws WTException {
		CategoryDAO dao = CategoryDAO.getInstance();
		item.setCategoryId(dao.getSequence(con).intValue());
		if(item.getIsDefault()) dao.resetIsDefaultByDomainUser(con, item.getDomainId(), item.getUserId());
		dao.insert(con, item);
		return item;
	}
	
	private void buildShareCache() {
		CoreManager core = WT.getCoreManager(getServiceContext());
		
		try {
			cacheOwnerToRootShare.clear();
			cacheOwnerToWildcardFolderShare.clear();
			cacheCategoryToFolderShare.clear();
			for(CategoryRoot root : listIncomingCategoryRoots()) {
				cacheOwnerToRootShare.put(root.getOwnerProfileId(), root.getShareId());
				for(OShare folder : core.listIncomingShareFolders(root.getShareId(), SERVICE_ID, RESOURCE_CATEGORY)) {
					if(folder.hasWildcard()) {
						UserProfile.Id ownerId = core.userUidToProfileId(folder.getUserUid());
						cacheOwnerToWildcardFolderShare.put(ownerId, folder.getShareId().toString());
					} else {
						cacheCategoryToFolderShare.put(Integer.valueOf(folder.getInstance()), folder.getShareId().toString());
					}
				}
			}
		} catch(WTException ex) {
			throw new WTRuntimeException(ex.getMessage());
		}
	}
	
	private String ownerToRootShareId(UserProfile.Id owner) {
		synchronized(shareCacheLock) {
			if(!cacheOwnerToRootShare.containsKey(owner)) buildShareCache();
			return cacheOwnerToRootShare.get(owner);
		}
	}
	
	private String ownerToWildcardFolderShareId(UserProfile.Id ownerPid) {
		synchronized(shareCacheLock) {
			if(!cacheOwnerToWildcardFolderShare.containsKey(ownerPid) && cacheOwnerToRootShare.isEmpty()) buildShareCache();
			return cacheOwnerToWildcardFolderShare.get(ownerPid);
		}
	}
	
	private String categoryToFolderShareId(int category) {
		synchronized(shareCacheLock) {
			if(!cacheCategoryToFolderShare.containsKey(category)) buildShareCache();
			return cacheCategoryToFolderShare.get(category);
		}
	}
	
	private UserProfile.Id categoryToOwner(int categoryId) {
		synchronized(cacheCategoryToOwner) {
			if(cacheCategoryToOwner.containsKey(categoryId)) {
				return cacheCategoryToOwner.get(categoryId);
			} else {
				try {
					UserProfile.Id owner = findCategoryOwner(categoryId);
					cacheCategoryToOwner.put(categoryId, owner);
					return owner;
				} catch(WTException ex) {
					throw new WTRuntimeException(ex.getMessage());
				}
			}
		}
	}
	
	private UserProfile.Id findCategoryOwner(int categoryId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(getManifest());
			CategoryDAO dao = CategoryDAO.getInstance();
			Owner owner = dao.selectOwnerById(con, categoryId);
			if(owner == null) throw new WTException("Category not found [{0}]", categoryId);
			return new UserProfile.Id(owner.getDomainId(), owner.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void checkRightsOnCategoryRoot(UserProfile.Id ownerPid, String action) throws WTException {
		UserProfile.Id targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		if(ownerPid.equals(targetPid)) return;
		
		String shareId = ownerToRootShareId(ownerPid);
		if(shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", ownerPid);
		CoreManager core = WT.getCoreManager(getServiceContext(), targetPid);
		if(core.isShareRootPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on root share [{0}, {1}, {2}, {3}]", shareId, action, RESOURCE_CATEGORY, targetPid.toString());
	}
	
	private void checkRightsOnCategoryFolder(int categoryId, String action) throws WTException {
		UserProfile.Id targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = categoryToOwner(categoryId);
		if(ownerPid.equals(targetPid)) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(getServiceContext(), targetPid);
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, wildcardShareId)) return;
		}
		
		// Checks rights on category instance
		String shareId = categoryToFolderShareId(categoryId);
		if(shareId == null) throw new WTException("categoryToLeafShareId({0}) -> null", categoryId);
		if(core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folder share [{0}, {1}, {2}, {3}]", shareId, action, RESOURCE_CATEGORY, targetPid.toString());
	}
	
	private void checkRightsOnCategoryElements(int categoryId, String action) throws WTException {
		UserProfile.Id targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = categoryToOwner(categoryId);
		if(ownerPid.equals(getTargetProfileId())) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(getServiceContext(), targetPid);
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = categoryToFolderShareId(categoryId);
		if(shareId == null) throw new WTException("categoryToLeafShareId({0}) -> null", categoryId);
		if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folderEls share [{0}, {1}, {2}, {3}]", shareId, action, RESOURCE_CATEGORY, targetPid.toString());
	}
    
	private DateTime createRevisionTimestamp() {
		return DateTime.now(DateTimeZone.UTC);
	}
	
    public static class CategoryTasks {
		public final OCategory folder;
		public final List<VTask> tasks;
		
		public CategoryTasks(OCategory folder, List<VTask> tasks) {
			this.folder = folder;
			this.tasks = tasks;
		}
	}
}
