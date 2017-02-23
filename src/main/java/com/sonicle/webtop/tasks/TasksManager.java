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

import com.sonicle.commons.db.DbUtils;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
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
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.IdentifierUtils;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.model.CategoryFolder;
import com.sonicle.webtop.tasks.model.CategoryRoot;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.dal.CategoryDAO;
import com.sonicle.webtop.tasks.dal.TaskDAO;
import com.sonicle.webtop.tasks.model.Category;
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
 * @author malbinola
 */
public class TasksManager extends BaseManager implements ITasksManager {
	public static final Logger logger = WT.getLogger(TasksManager.class);
	public static final String GROUPNAME_CATEGORY = "CATEGORY";
	public static final String SUGGESTION_TASK_SUBJECT = "tasksubject";
	
	private final HashMap<Integer, UserProfile.Id> cacheCategoryToOwner = new HashMap<>();
	private final Object shareCacheLock = new Object();
	private final HashMap<UserProfile.Id, String> cacheOwnerToRootShare = new HashMap<>();
	private final HashMap<UserProfile.Id, String> cacheOwnerToWildcardFolderShare = new HashMap<>();
	private final HashMap<Integer, String> cacheCategoryToFolderShare = new HashMap<>();
	
	public TasksManager(boolean fastInit, UserProfile.Id targetProfileId) {
		super(fastInit, targetProfileId);
	}
	
	private void buildShareCache() {
		CoreManager core = WT.getCoreManager();
		
		try {
			cacheOwnerToRootShare.clear();
			cacheOwnerToWildcardFolderShare.clear();
			cacheCategoryToFolderShare.clear();
			for(CategoryRoot root : listIncomingCategoryRoots()) {
				cacheOwnerToRootShare.put(root.getOwnerProfileId(), root.getShareId());
				for(OShare folder : core.listIncomingShareFolders(root.getShareId(), GROUPNAME_CATEGORY)) {
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
	
	public List<CategoryRoot> listIncomingCategoryRoots() throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		ArrayList<CategoryRoot> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		
		List<IncomingShareRoot> shares = core.listIncomingShareRoots(SERVICE_ID, GROUPNAME_CATEGORY);
		for(IncomingShareRoot share : shares) {
			SharePermsRoot perms = core.getShareRootPermissions(share.getShareId());
			CategoryRoot root = new CategoryRoot(share, perms);
			if(hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public HashMap<Integer, CategoryFolder> listIncomingCategoryFolders(String rootShareId) throws WTException {
		CoreManager core = WT.getCoreManager(getTargetProfileId());
		LinkedHashMap<Integer, CategoryFolder> folders = new LinkedHashMap<>();
		
		// Retrieves incoming folders (from sharing). This lookup already 
		// returns readable shares (we don't need to test READ permission)
		List<OShare> shares = core.listIncomingShareFolders(rootShareId, GROUPNAME_CATEGORY);
		for(OShare share : shares) {
			
			List<Category> cats = null;
			if(share.hasWildcard()) {
				UserProfile.Id ownerId = core.userUidToProfileId(share.getUserUid());
				cats = listCategories(ownerId);
			} else {
				cats = Arrays.asList(getCategory(Integer.valueOf(share.getInstance())));
			}
			
			for(Category cat : cats) {
				SharePermsFolder fperms = core.getShareFolderPermissions(share.getShareId().toString());
				SharePermsElements eperms = core.getShareElementsPermissions(share.getShareId().toString());
				
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
		CoreManager core = WT.getCoreManager();
		return core.getSharing(SERVICE_ID, GROUPNAME_CATEGORY, shareId);
	}
	
	public void updateSharing(Sharing sharing) throws WTException {
		CoreManager core = WT.getCoreManager();
		core.updateSharing(SERVICE_ID, GROUPNAME_CATEGORY, sharing);
	}
	
	public UserProfile.Id getCategoryOwner(int categoryId) throws WTException {
		return categoryToOwner(categoryId);
	}
	
	@Override
	public List<Category> listCategories() throws WTException {
		return listCategories(getTargetProfileId());
	}
	
	private List<Category> listCategories(UserProfile.Id pid) throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		ArrayList<Category> items = new ArrayList<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for (OCategory ocat : catdao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
				items.add(createCategory(ocat));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Category getCategory(int categoryId) throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategoryFolder(categoryId, "READ");
			
			con = WT.getConnection(SERVICE_ID);
			OCategory ocat = catdao.selectById(con, categoryId);
			
			return createCategory(ocat);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Category getBuiltInCategory() throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OCategory ocat = catdao.selectBuiltInByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if(ocat == null) return null;
			
			checkRightsOnCategoryFolder(ocat.getCategoryId(), "READ");
			
			return createCategory(ocat);
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Category addCategory(Category cat) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryRoot(cat.getProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			cat.setBuiltIn(false);
			cat = doCategoryUpdate(true, con, cat);
			DbUtils.commitQuietly(con);
			writeLog("CATEGORY_INSERT", String.valueOf(cat.getCategoryId()));
			
			return cat;
			
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
	
	@Override
	public Category addBuiltInCategory() throws WTException {
		CategoryDAO dao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategoryRoot(getTargetProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			OCategory ocat = dao.selectBuiltInByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (ocat != null) {
				logger.debug("Built-in category already present");
				return null;
			}
			
			Category cat = new Category();
			cat.setDomainId(getTargetProfileId().getDomainId());
			cat.setUserId(getTargetProfileId().getUserId());
			cat.setBuiltIn(true);
			cat.setName(WT.getPlatformName());
			cat.setDescription("");
			cat.setColor("#FFFFFF");
			cat.setSync(Category.SYNC_OFF);
			cat.setIsPrivate(false);
			cat.setIsDefault(true);
			cat = doCategoryUpdate(true, con, cat);
			DbUtils.commitQuietly(con);
			writeLog("CATEGORY_INSERT", String.valueOf(cat.getCategoryId()));
			
			return cat;
			
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
	
	@Override
	public Category updateCategory(Category cat) throws Exception {
		Connection con = null;
		
		try {
			checkRightsOnCategoryFolder(cat.getCategoryId(), "UPDATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			cat = doCategoryUpdate(false, con, cat);
			DbUtils.commitQuietly(con);
			writeLog("CATEGORY_UPDATE", String.valueOf(cat.getCategoryId()));
			
			return cat;
			
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
	
	@Override
	public boolean deleteCategory(int categoryId) throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategoryFolder(categoryId, "DELETE");

			con = WT.getConnection(SERVICE_ID, false);
			int ret = catdao.deleteById(con, categoryId);
			doDeleteTasksByCategory(con, categoryId);
			DbUtils.commitQuietly(con);
			writeLog("CATEGORY_DELETE", String.valueOf(categoryId));
			writeLog("TASK_DELETE", "*");
			
			return ret == 1;
			
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

	public List<CategoryTasks> listTasks(CategoryRoot root, Integer[] categoryFolders, String pattern) throws WTException {
		return listTasks(root.getOwnerProfileId(), categoryFolders, pattern);
	}
	
	public List<CategoryTasks> listTasks(UserProfile.Id pid, Integer[] categoryFolders, String pattern) throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		TaskDAO tasdao = TaskDAO.getInstance();
		ArrayList<CategoryTasks> catTasks = new ArrayList<>();
		Connection con = null;
		
		try {
            // TODO: implementare filtro task privati
			con = WT.getConnection(SERVICE_ID);
			
			// Lists desired groups (tipically visibles) coming from passed list
			// Passed ids should belong to referenced folder(group), 
			// this is ensured using domainId and userId parameters in below query.
			List<OCategory> ocats = catdao.selectByProfileIn(con, pid.getDomainId(), pid.getUserId(), categoryFolders);
			List<VTask> vts = null;
			for(OCategory ocat : ocats) {
				checkRightsOnCategoryFolder(ocat.getCategoryId(), "READ");
				vts = tasdao.viewByCategoryPattern(con, ocat.getCategoryId(), pattern);
				catTasks.add(new CategoryTasks(createCategory(ocat), vts));
			}
			return catTasks;
		
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
    
	@Override
	public Task getTask(int taskId) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			OTask otask = tasdao.selectById(con, taskId);
			if (otask == null) return null;
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
	
	@Override
	public void addTask(Task task) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		Connection con = null;
		
		try {
			checkRightsOnCategoryElements(task.getCategoryId(), "CREATE"); // Rights check!
			
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			OTask result = doInsertTask(con, task);
			DbUtils.commitQuietly(con);
			writeLog("TASK_INSERT", String.valueOf(result.getTaskId()));
			
			storeAsSuggestion(coreMgr, SUGGESTION_TASK_SUBJECT, task.getSubject());
			
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
	
	@Override
	public void updateTask(Task task) throws WTException {
		Connection con = null;
		
		//TODO: gestire i suggerimenti (soggetto)

		try {
			checkRightsOnCategoryElements(task.getCategoryId(), "UPDATE"); // Rights check!

			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			doUpdateTask(con, task);
			DbUtils.commitQuietly(con);
			writeLog("TASK_UPDATE", String.valueOf(task.getTaskId()));

		} catch (SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteTask(int taskId) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		Connection con = null;

		try {
			con = WT.getConnection(SERVICE_ID);

			OTask cont = tasdao.selectById(con, taskId);
			if (cont == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
			checkRightsOnCategoryElements(cont.getCategoryId(), "DELETE"); // Rights check!

			con.setAutoCommit(false);
			doDeleteTask(con, taskId);
			DbUtils.commitQuietly(con);
			writeLog("TASK_DELETE", String.valueOf(taskId));

		} catch (SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteTask(ArrayList<Integer> taskIds) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			con.setAutoCommit(false);
			
			for(Integer taskId : taskIds) {
				if (taskId == null) continue;
				OTask task = tasdao.selectById(con, taskId);
				if (task == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
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
	
	@Override
	public int deleteAllTasks(int categoryId) throws WTException {
		Connection con = null;

		try {
			checkRightsOnCategoryElements(categoryId, "DELETE");

			con = WT.getConnection(SERVICE_ID, false);
			int ret = doDeleteTasksByCategory(con, categoryId);
			DbUtils.commitQuietly(con);
			writeLog("TASK_DELETE", "*");
			
			return ret;

		} catch (SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw new WTException(ex, "DB error");
		} catch (Exception ex) {
			DbUtils.rollbackQuietly(con);
			throw ex;
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void moveTask(boolean copy, int taskId, int targetCategoryId) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OTask otask = tasdao.selectById(con, taskId);
			if (otask == null) throw new WTException("Unable to retrieve task [{0}]", taskId);
			checkRightsOnCategoryFolder(otask.getCategoryId(), "READ"); // Rights check!
			
			if (copy || (targetCategoryId != otask.getCategoryId())) {
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
	
	public void eraseData(boolean deep) throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		TaskDAO tasdao = TaskDAO.getInstance();
		Connection con = null;
		
		//TODO: controllo permessi
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			UserProfile.Id pid = getTargetProfileId();
			
			// Erase tasks
			if (deep) {
				for (OCategory ocat : catdao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					tasdao.deleteByCategoryId(con, ocat.getCategoryId());
				}
			} else {
				DateTime revTs = createRevisionTimestamp();
				for (OCategory ocat : catdao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					tasdao.logicDeleteByCategoryId(con, ocat.getCategoryId(), revTs);
				}
			}
			
			// Erase categories
			catdao.deleteByProfile(con, pid.getDomainId(), pid.getUserId());
			
			DbUtils.commitQuietly(con);
			
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
					boolean bool = us.getTaskReminderDelivery().equals(TasksSettings.TASK_REMINDER_DELIVERY_EMAIL);
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
	
	
	
	private Category doCategoryUpdate(boolean insert, Connection con, Category cat) throws WTException {
		CategoryDAO catdao = CategoryDAO.getInstance();
		
		OCategory ocat = createOCategory(cat);
		if (ocat.getDomainId() == null) ocat.setDomainId(getTargetProfileId().getDomainId());
		if (ocat.getUserId() == null) ocat.setUserId(getTargetProfileId().getUserId());
		
		if(ocat.getIsDefault()) catdao.resetIsDefaultByProfile(con, ocat.getDomainId(), ocat.getUserId());
		if (insert) {
			ocat.setCategoryId(catdao.getSequence(con).intValue());
			catdao.insert(con, ocat);
		} else {
			catdao.update(con, ocat);
		}
		
		return createCategory(ocat);
	}
	
	private OTask doInsertTask(Connection con, Task task) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		OTask item = new OTask();
		try {
			BeanUtils.copyProperties(item, task);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new WTException(ex, "Error creating bean");
		}
		if(StringUtils.isEmpty(item.getPublicUid())) item.setPublicUid(IdentifierUtils.getUUID());
        item.setTaskId(tasdao.getSequence(con).intValue());
        tasdao.insert(con, item, createRevisionTimestamp());
        return item;
	}
	
	private void doUpdateTask(Connection con, Task task) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		OTask item = new OTask();
		try {
			BeanUtils.copyProperties(item, task);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			throw new WTException(ex, "Error creating bean");
		}
        tasdao.update(con, item, createRevisionTimestamp());
	}
	
	private int doDeleteTask(Connection con, int taskId) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		return tasdao.logicDeleteById(con, taskId, createRevisionTimestamp());
	}
	
	private int doDeleteTasksByCategory(Connection con, int categoryId) throws WTException {
		TaskDAO tasdao = TaskDAO.getInstance();
		return tasdao.logicDeleteByCategoryId(con, categoryId, createRevisionTimestamp());
	}
	
	private void doMoveTask(Connection con, boolean copy, Task task, int targetCategoryId) throws WTException {
		if(copy) {
			task.setCategoryId(targetCategoryId);
            doInsertTask(con, task);
		} else {
			TaskDAO tasdao = TaskDAO.getInstance();
			tasdao.updateCategory(con, task.getTaskId(), targetCategoryId, createRevisionTimestamp());
		}
	}
	
	private UserProfile.Id findCategoryOwner(int categoryId) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
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
		CoreManager core = WT.getCoreManager(targetPid);
		if(core.isShareRootPermitted(shareId, action)) return;
		//if(core.isShareRootPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on root share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CATEGORY, targetPid.toString());
	}
	
	private void checkRightsOnCategoryFolder(int categoryId, String action) throws WTException {
		UserProfile.Id targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = categoryToOwner(categoryId);
		if(ownerPid.equals(targetPid)) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(targetPid);
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareFolderPermitted(wildcardShareId, action)) return;
			//if(core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, wildcardShareId)) return;
		}
		
		// Checks rights on category instance
		String shareId = categoryToFolderShareId(categoryId);
		if(shareId == null) throw new WTException("categoryToLeafShareId({0}) -> null", categoryId);
		if(core.isShareFolderPermitted(shareId, action)) return;
		//if(core.isShareFolderPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folder share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CATEGORY, targetPid.toString());
	}
	
	private void checkRightsOnCategoryElements(int categoryId, String action) throws WTException {
		UserProfile.Id targetPid = getTargetProfileId();
		
		if(RunContext.isWebTopAdmin()) return;
		// Skip rights check if running user is resource's owner
		UserProfile.Id ownerPid = categoryToOwner(categoryId);
		if(ownerPid.equals(getTargetProfileId())) return;
		
		// Checks rights on the wildcard instance (if present)
		CoreManager core = WT.getCoreManager(targetPid);
		String wildcardShareId = ownerToWildcardFolderShareId(ownerPid);
		if(wildcardShareId != null) {
			if(core.isShareElementsPermitted(wildcardShareId, action)) return;
			//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, wildcardShareId)) return;
		}
		
		// Checks rights on calendar instance
		String shareId = categoryToFolderShareId(categoryId);
		if(shareId == null) throw new WTException("categoryToLeafShareId({0}) -> null", categoryId);
		if(core.isShareElementsPermitted(shareId, action)) return;
		//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on folderEls share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CATEGORY, targetPid.toString());
	}
	
	private Category createCategory(OCategory ocat) {
		if (ocat == null) return null;
		Category cat = new Category();
		cat.setCategoryId(ocat.getCategoryId());
		cat.setDomainId(ocat.getDomainId());
		cat.setUserId(ocat.getUserId());
		cat.setBuiltIn(ocat.getBuiltIn());
		cat.setName(ocat.getName());
		cat.setDescription(ocat.getDescription());
		cat.setColor(ocat.getColor());
		cat.setSync(ocat.getSync());
		cat.setIsPrivate(ocat.getIsPrivate());
		cat.setIsDefault(ocat.getIsDefault());
		return cat;
	}
	
	private OCategory createOCategory(Category cat) {
		if (cat == null) return null;
		OCategory ocat = new OCategory();
		ocat.setCategoryId(cat.getCategoryId());
		ocat.setDomainId(cat.getDomainId());
		ocat.setUserId(cat.getUserId());
		ocat.setBuiltIn(cat.getBuiltIn());
		ocat.setName(cat.getName());
		ocat.setDescription(cat.getDescription());
		ocat.setColor(cat.getColor());
		ocat.setSync(cat.getSync());
		ocat.setIsPrivate(cat.getIsPrivate());
		ocat.setIsDefault(cat.getIsDefault());
		return ocat;
	}
	
	private Task createTask(OTask otask) throws WTException {
		if (otask == null) return null;
		Task task = new Task();
		task.setTaskId(otask.getTaskId());
		task.setCategoryId(otask.getCategoryId());
		task.setRevisionStatus(otask.getRevisionStatus());
		task.setRevisionTimestamp(otask.getRevisionTimestamp());
		task.setPublicUid(otask.getPublicUid());
		task.setSubject(otask.getSubject());
		task.setDescription(otask.getDescription());
		task.setStartDate(otask.getStartDate());
		task.setDueDate(otask.getDueDate());
		task.setCompletedDate(otask.getCompletedDate());
		task.setImportance(otask.getImportance());
		task.setIsPrivate(otask.getIsPrivate());
		task.setStatus(otask.getStatus());
		task.setCompletionPercentage(otask.getCompletionPercentage());
		task.setReminderDate(otask.getReminderDate());
		task.setRemindedOn(otask.getRemindedOn());
		return task;
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
	
	private void storeAsSuggestion(CoreManager coreMgr, String context, String value) {
		if (StringUtils.isBlank(value)) return;
		coreMgr.addServiceStoreEntry(SERVICE_ID, context, value.toUpperCase(), value);
	}
    
	private DateTime createRevisionTimestamp() {
		return DateTime.now(DateTimeZone.UTC);
	}
	
    public static class CategoryTasks {
		public final Category folder;
		public final List<VTask> tasks;
		
		public CategoryTasks(Category folder, List<VTask> tasks) {
			this.folder = folder;
			this.tasks = tasks;
		}
	}
}
