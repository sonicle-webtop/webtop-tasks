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

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.LangUtils.CollectionChangeSet;
import com.sonicle.commons.db.DbUtils;
import com.sonicle.commons.web.json.CompositeId;
import com.sonicle.webtop.core.CoreManager;
import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.core.bol.OShare;
import com.sonicle.webtop.core.bol.Owner;
import com.sonicle.webtop.core.model.IncomingShareRoot;
import com.sonicle.webtop.core.model.SharePermsElements;
import com.sonicle.webtop.core.model.SharePermsFolder;
import com.sonicle.webtop.core.model.SharePermsRoot;
import com.sonicle.webtop.core.bol.model.Sharing;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.core.dal.DAOIntegrityViolationException;
import com.sonicle.webtop.core.model.ProfileI18n;
import com.sonicle.webtop.core.sdk.AbstractMapCache;
import com.sonicle.webtop.core.sdk.AbstractShareCache;
import com.sonicle.webtop.core.sdk.AuthException;
import com.sonicle.webtop.core.sdk.BaseManager;
import com.sonicle.webtop.core.sdk.BaseReminder;
import com.sonicle.webtop.core.sdk.ReminderEmail;
import com.sonicle.webtop.core.sdk.ReminderInApp;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.core.sdk.WTRuntimeException;
import com.sonicle.webtop.core.util.NotificationHelper;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.OCategoryPropSet;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.OTaskAttachment;
import com.sonicle.webtop.tasks.bol.OTaskAttachmentData;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.bol.VTaskObject;
import com.sonicle.webtop.tasks.bol.VTaskObjectChanged;
import com.sonicle.webtop.tasks.bol.VTaskLookup;
import com.sonicle.webtop.tasks.bol.model.MyShareRootCategory;
import com.sonicle.webtop.tasks.model.ShareFolderCategory;
import com.sonicle.webtop.tasks.model.ShareRootCategory;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.dal.CategoryDAO;
import com.sonicle.webtop.tasks.dal.CategoryPropsDAO;
import com.sonicle.webtop.tasks.dal.TaskAttachmentDAO;
import com.sonicle.webtop.tasks.dal.TaskDAO;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.ListTasksResult;
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithBytes;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithStream;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskObjectChanged;
import com.sonicle.webtop.tasks.model.TaskObjectWithBean;
import com.sonicle.webtop.tasks.model.TaskObjectWithICalendar;
import com.sonicle.webtop.tasks.model.TaskLookup;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.mail.internet.AddressException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import com.sonicle.commons.qbuilders.conditions.Condition;
import com.sonicle.commons.web.json.JsonResult;
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.tasks.bol.OTaskCustomValue;
import com.sonicle.webtop.tasks.dal.TaskCustomValueDAO;
import com.sonicle.webtop.tasks.dal.TaskPredicateVisitor;
import com.sonicle.webtop.tasks.dal.TaskTagDAO;
import com.sonicle.webtop.tasks.model.TaskQuery;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;

/**
 *
 * @author malbinola
 */
public class TasksManager extends BaseManager implements ITasksManager {
	public static final Logger logger = WT.getLogger(TasksManager.class);
	public static final String GROUPNAME_CATEGORY = "CATEGORY";
	public static final String SUGGESTION_TASK_SUBJECT = "tasksubject";
	
	private final OwnerCache ownerCache = new OwnerCache();
	private final ShareCache shareCache = new ShareCache();
	
	public TasksManager(boolean fastInit, UserProfileId targetProfileId) {
		super(fastInit, targetProfileId);
		if (!fastInit) {
			shareCache.init();
		}
	}
	
	private CoreManager getCoreManager() {
		return WT.getCoreManager(getTargetProfileId());
	}
	
	private TasksServiceSettings getServiceSettings() {
		return new TasksServiceSettings(SERVICE_ID, getTargetProfileId().getDomainId());
	}
	
	private List<ShareRootCategory> internalListIncomingCategoryShareRoots() throws WTException {
		CoreManager coreMgr = getCoreManager();
		List<ShareRootCategory> roots = new ArrayList();
		HashSet<String> hs = new HashSet<>();
		for (IncomingShareRoot share : coreMgr.listIncomingShareRoots(SERVICE_ID, GROUPNAME_CATEGORY)) {
			final SharePermsRoot perms = coreMgr.getShareRootPermissions(share.getShareId());
			ShareRootCategory root = new ShareRootCategory(share, perms);
			if (hs.contains(root.getShareId())) continue; // Avoid duplicates ??????????????????????
			hs.add(root.getShareId());
			roots.add(root);
		}
		return roots;
	}
	
	public String buildSharingId(int categoryId) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		// Skip rights check if running user is resource's owner
		UserProfileId owner = ownerCache.get(categoryId);
		if (owner == null) throw new WTException("owner({0}) -> null", categoryId);
		
		String rootShareId = null;
		if (owner.equals(targetPid)) {
			rootShareId = MyShareRootCategory.SHARE_ID;
		} else {
			rootShareId = shareCache.getShareRootIdByFolderId(categoryId);
		}
		if (rootShareId == null) throw new WTException("Unable to find a root share [{0}]", categoryId);
		return new CompositeId().setTokens(rootShareId, categoryId).toString();
	}
	
	public Sharing getSharing(String shareId) throws WTException {
		CoreManager coreMgr = getCoreManager();
		return coreMgr.getSharing(SERVICE_ID, GROUPNAME_CATEGORY, shareId);
	}
	
	public void updateSharing(Sharing sharing) throws WTException {
		CoreManager coreMgr = getCoreManager();
		coreMgr.updateSharing(SERVICE_ID, GROUPNAME_CATEGORY, sharing);
	}
	
	public UserProfileId getCategoryOwner(int categoryId) throws WTException {
		return ownerCache.get(categoryId);
	}
	
	@Override
	public List<ShareRootCategory> listIncomingCategoryRoots() {
		return shareCache.getShareRoots();
	}
	
	@Override
	public Map<Integer, ShareFolderCategory> listIncomingCategoryFolders(String rootShareId) throws WTException {
		CoreManager coreMgr = getCoreManager();
		LinkedHashMap<Integer, ShareFolderCategory> folders = new LinkedHashMap<>();
		
		for (Integer folderId : shareCache.getFolderIdsByShareRoot(rootShareId)) {
			final String shareFolderId = shareCache.getShareFolderIdByFolderId(folderId);
			if (StringUtils.isBlank(shareFolderId)) continue;
			SharePermsFolder fperms = coreMgr.getShareFolderPermissions(shareFolderId);
			SharePermsElements eperms = coreMgr.getShareElementsPermissions(shareFolderId);
			if (folders.containsKey(folderId)) {
				final ShareFolderCategory shareFolder = folders.get(folderId);
				if (shareFolder == null) continue;
				shareFolder.getPerms().merge(fperms);
				shareFolder.getElementsPerms().merge(eperms);
			} else {
				final Category category = getCategory(folderId);
				if (category == null) continue;
				folders.put(folderId, new ShareFolderCategory(shareFolderId, fperms, eperms, category));
			}
		}
		return folders;
	}
	
	@Override
	public Set<Integer> listCategoryIds() throws WTException {
		return listCategoryIds(getTargetProfileId());
	}
	
	@Override
	public Set<Integer> listIncomingCategoryIds() throws WTException {
		return shareCache.getFolderIds();
	}
	
	@Override
	public Set<Integer> listAllCategoryIds() throws WTException {
		return Stream.concat(listCategoryIds().stream(), listIncomingCategoryIds().stream())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	private Set<Integer> listCategoryIds(UserProfileId pid) throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			return catDao.selectIdsByProfile(con, pid.getDomainId(), pid.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<Integer, Category> listCategories() throws WTException {
		return listCategories(getTargetProfileId());
	}
	
	private Map<Integer, Category> listCategories(UserProfileId pid) throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		LinkedHashMap<Integer, Category> items = new LinkedHashMap<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for (OCategory ocat : catDao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
				items.put(ocat.getCategoryId(), ManagerUtils.createCategory(ocat));
			}
			return items;
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<Integer, DateTime> getCategoriesLastRevision(Collection<Integer> categoryIds) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCategoryIds = categoryIds.stream()
					.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ"))
					.collect(Collectors.toList());
			
			con = WT.getConnection(SERVICE_ID);
			return tasDao.selectMaxRevTimestampByCategories(con, okCategoryIds);
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Category getCategory(int categoryId) throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			
			con = WT.getConnection(SERVICE_ID);
			OCategory ocat = catDao.selectById(con, categoryId);
			return ManagerUtils.createCategory(ocat);
			
		} catch(SQLException | DAOException | WTException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Category getBuiltInCategory() throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OCategory ocat = catDao.selectBuiltInByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (ocat == null) return null;
			
			checkRightsOnCategory(ocat.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
			
			return ManagerUtils.createCategory(ocat);
			
		} catch(SQLException | DAOException | WTException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Category addCategory(Category category) throws WTException {
		Connection con = null;
		
		try {
			checkRightsOnCategoryRoot(category.getProfileId(), "MANAGE");
			
			con = WT.getConnection(SERVICE_ID, false);
			category.setBuiltIn(false);
			category = doCategoryInsert(con, category);
			
			DbUtils.commitQuietly(con);
			onAfterCategoryAction(category.getCategoryId(), category.getProfileId());
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.CATEGORY, AuditAction.CREATE, category.getCategoryId(), null);
			}
			
			return category;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
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
			cat.setBuiltIn(true);
			cat.setName(WT.getPlatformName());
			cat.setDescription("");
			cat.setIsDefault(true);
			cat = doCategoryInsert(con, cat);
			
			DbUtils.commitQuietly(con);
			onAfterCategoryAction(cat.getCategoryId(), cat.getProfileId());
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.CATEGORY, AuditAction.CREATE, cat.getCategoryId(), null);
			}
			
			return cat;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateCategory(Category cat) throws WTException {
		Connection con = null;
		
		try {
			int categoryId = cat.getCategoryId();
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "UPDATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			boolean ret = doCategoryUpdate(con, cat);
			if (!ret) throw new WTNotFoundException("Category not found [{}]", categoryId);
			
			DbUtils.commitQuietly(con);
			onAfterCategoryAction(categoryId, cat.getProfileId());
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.CATEGORY, AuditAction.UPDATE, categoryId, null);
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public boolean deleteCategory(int categoryId) throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		CategoryPropsDAO psetDao = CategoryPropsDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "DELETE");
				
			// Retrieve sharing status (for later)
			String sharingId = buildSharingId(categoryId);
			Sharing sharing = getSharing(sharingId);
			
			con = WT.getConnection(SERVICE_ID, false);
			Category cat = ManagerUtils.createCategory(catDao.selectById(con, categoryId));
			if (cat == null) throw new WTNotFoundException("Category not found [{}]", categoryId);
			
			int ret = catDao.deleteById(con, categoryId);
			psetDao.deleteByCategory(con, categoryId);
			doTaskDeleteByCategory(con, categoryId);
			
			// Cleanup sharing, if necessary
			if ((sharing != null) && !sharing.getRights().isEmpty()) {
				logger.debug("Removing {} active sharing [{}]", sharing.getRights().size(), sharing.getId());
				sharing.getRights().clear();
				updateSharing(sharing);
			}
			
			DbUtils.commitQuietly(con);
			onAfterCategoryAction(categoryId, cat.getProfileId());
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.CATEGORY, AuditAction.DELETE, categoryId, null);
				writeAuditLog(AuditContext.CATEGORY, AuditAction.DELETE, "*", categoryId);
			}
			
			return ret == 1;
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CategoryPropSet getCategoryCustomProps(int categoryId) throws WTException {
		return getCategoriesCustomProps(getTargetProfileId(), Arrays.asList(categoryId)).get(categoryId);
	}
	
	@Override
	public Map<Integer, CategoryPropSet> getCategoriesCustomProps(Collection<Integer> categoryIds) throws WTException {
		return getCategoriesCustomProps(getTargetProfileId(), categoryIds);
	}
	
	private Map<Integer, CategoryPropSet> getCategoriesCustomProps(UserProfileId profileId, Collection<Integer> categoryIds) throws WTException {
		CategoryPropsDAO psetDao = CategoryPropsDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			LinkedHashMap<Integer, CategoryPropSet> psets = new LinkedHashMap<>(categoryIds.size());
			Map<Integer, OCategoryPropSet> map = psetDao.selectByProfileCategoryIn(con, profileId.getDomainId(), profileId.getUserId(), categoryIds);
			for (Integer categoryId : categoryIds) {
				OCategoryPropSet opset = map.get(categoryId);
				psets.put(categoryId, (opset == null) ? new CategoryPropSet() : ManagerUtils.createCategoryPropSet(opset));
			}
			return psets;
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CategoryPropSet updateCategoryCustomProps(int categoryId, CategoryPropSet propertySet) throws WTException {
		ensureUser();
		return updateCategoryCustomProps(getTargetProfileId(), categoryId, propertySet);
	}
	
	private CategoryPropSet updateCategoryCustomProps(UserProfileId profileId, int categoryId, CategoryPropSet propertySet) throws WTException {
		CategoryPropsDAO psetDao = CategoryPropsDAO.getInstance();
		Connection con = null;
		
		try {
			OCategoryPropSet opset = ManagerUtils.createOCategoryPropSet(propertySet);
			opset.setDomainId(profileId.getDomainId());
			opset.setUserId(profileId.getUserId());
			opset.setCategoryId(categoryId);
			
			con = WT.getConnection(SERVICE_ID);
			try {
				psetDao.insert(con, opset);
			} catch(DAOIntegrityViolationException ex1) {
				psetDao.update(con, opset);
			}
			return propertySet;
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<TaskObject> listTaskObjects(int categoryId, TaskObjectOutputType outputType) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			
			ArrayList<TaskObject> items = new ArrayList<>();
			Map<String, List<VTaskObject>> map = tasDao.viewTaskObjectsByCategory(con, categoryId);
			for (List<VTaskObject> vtasks : map.values()) {
				if (vtasks.isEmpty()) continue;
				VTaskObject vtask = vtasks.get(vtasks.size()-1);
				if (vtasks.size() > 1) {
					logger.trace("Many tasks ({}) found for same href [{} -> {}]", vtasks.size(), vtask.getHref(), vtask.getTaskId());
				}
				
				items.add(doTaskObjectPrepare(con, vtask, outputType));
			}
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public CollectionChangeSet<TaskObjectChanged> listTaskObjectsChanges(int categoryId, DateTime since, Integer limit) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			
			ArrayList<TaskObjectChanged> inserted = new ArrayList<>();
			ArrayList<TaskObjectChanged> updated = new ArrayList<>();
			ArrayList<TaskObjectChanged> deleted = new ArrayList<>();
			
			if (limit == null) limit = Integer.MAX_VALUE;
			if (since == null) {
				List<VTaskObjectChanged> vtasks = tasDao.viewLiveTaskObjectsChangedByCategory(con, categoryId, limit);
				for (VTaskObjectChanged vtask : vtasks) {
					inserted.add(new TaskObjectChanged(vtask.getTaskId(), vtask.getRevisionTimestamp(), vtask.getHref()));
				}
			} else {
				List<VTaskObjectChanged> vtasks = tasDao.viewTaskObjectsChangedByCategorySince(con, categoryId, since, limit);
				for (VTaskObjectChanged vtask : vtasks) {
					Task.RevisionStatus revStatus = EnumUtils.forSerializedName(vtask.getRevisionStatus(), Task.RevisionStatus.class);
					if (Task.RevisionStatus.DELETED.equals(revStatus)) {
						deleted.add(new TaskObjectChanged(vtask.getTaskId(), vtask.getRevisionTimestamp(), vtask.getHref()));
					} else {
						if (Task.RevisionStatus.NEW.equals(revStatus) || (vtask.getCreationTimestamp().compareTo(since) >= 0)) {
							inserted.add(new TaskObjectChanged(vtask.getTaskId(), vtask.getRevisionTimestamp(), vtask.getHref()));
						} else {
							updated.add(new TaskObjectChanged(vtask.getTaskId(), vtask.getRevisionTimestamp(), vtask.getHref()));
						}
					}
				}
			}
			
			return new CollectionChangeSet<>(inserted, updated, deleted);
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public TaskObjectWithICalendar getTaskObjectWithICalendar(int categoryId, String href) throws WTException {
		List<TaskObjectWithICalendar> ccs = getTaskObjectsWithICalendar(categoryId, Arrays.asList(href));
		return ccs.isEmpty() ? null : ccs.get(0);
	}
	
	@Override
	public List<TaskObjectWithICalendar> getTaskObjectsWithICalendar(int categoryId, Collection<String> hrefs) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			
			ArrayList<TaskObjectWithICalendar> items = new ArrayList<>();
			Map<String, List<VTaskObject>> map = tasDao.viewTaskObjectsByCategoryHrefs(con, categoryId, hrefs);
			for (String href : hrefs) {
				List<VTaskObject> vtasks = map.get(href);
				if (vtasks == null) continue;
				if (vtasks.isEmpty()) continue;
				VTaskObject vtask = vtasks.get(vtasks.size()-1);
				if (vtasks.size() > 1) {
					logger.trace("Many tasks ({}) found for same href [{} -> {}]", vtasks.size(), vtask.getHref(), vtask.getTaskId());
				}
				
				items.add((TaskObjectWithICalendar)doTaskObjectPrepare(con, vtask, TaskObjectOutputType.ICALENDAR));
			}
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public TaskObject getTaskObject(int categoryId, int taskId, TaskObjectOutputType outputType) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			
			VTaskObject vtask = tasDao.viewTaskObjectById(con, categoryId, taskId);
			return (vtask == null) ? null : doTaskObjectPrepare(con, vtask, outputType);
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public ListTasksResult listTasks(Collection<Integer> categoryIds, Condition<TaskQuery> conditionPredicate) throws WTException {
		return listTasks(categoryIds, conditionPredicate, 1, Integer.MAX_VALUE, false);
	}
	
	@Override
	public ListTasksResult listTasks(Collection<Integer> categoryIds, Condition<TaskQuery> conditionPredicate, int page, int limit, boolean returnFullCount) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			int offset = ManagerUtils.toOffset(page, limit);
			List<Integer> okCategoryIds = categoryIds.stream()
					.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ"))
					.collect(Collectors.toList());
			
			con = WT.getConnection(SERVICE_ID);
			
			Integer fullCount = null;
			org.jooq.Condition condition = BaseDAO.createCondition(conditionPredicate, new TaskPredicateVisitor()
					.withIgnoreCase(true)
					.withForceStringLikeComparison(true)
			);
			if (returnFullCount) fullCount = tasDao.countByCategoryPattern(con, okCategoryIds, condition);
			ArrayList<TaskLookup> items = new ArrayList<>();
			for (VTaskLookup vcont : tasDao.viewByCategoryCondition(con, okCategoryIds, condition, limit, offset)) {
				items.add(ManagerUtils.fillTaskLookup(new TaskLookup(), vcont));
			}
			
			return new ListTasksResult(items, fullCount);
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public List<TaskLookup> listUpcomingTasks(Collection<Integer> categoryIds) throws WTException {
		return listUpcomingTasks(categoryIds, null);
	}
	
	@Override
	public List<TaskLookup> listUpcomingTasks(Collection<Integer> categoryIds, String pattern) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCategoryIds = categoryIds.stream()
					.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ"))
					.collect(Collectors.toList());
			
			con = WT.getConnection(SERVICE_ID);
			
			ArrayList<TaskLookup> items = new ArrayList<>();
			for (VTaskLookup vcont : tasDao.viewUpcomingByCategoryPattern(con, okCategoryIds, pattern)) {
				items.add(ManagerUtils.fillTaskLookup(new TaskLookup(), vcont));
			}
			
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
    
	@Override
	public Task getTask(int taskId) throws WTException {
		return getTask(taskId, true, true, true);
	}
	
	@Override
	public Task getTask(int taskId, boolean processAttachments, boolean processTags, boolean processCustomValues) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Task task = doTaskGet(con, taskId, processAttachments, processTags, processCustomValues);
			if (task == null) return null;
			checkRightsOnCategory(task.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
			
			return task;
		
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public TaskAttachmentWithBytes getTaskAttachment(int taskId, String attachmentId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			OTask otask = tasDao.selectById(con, taskId);
			if (otask == null) return null;
			checkRightsOnCategory(otask.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
			
			OTaskAttachment oatt = attDao.selectByIdTask(con, attachmentId, taskId);
			if (oatt == null) return null;
			
			OTaskAttachmentData oattData = attDao.selectBytesById(con, attachmentId);
			return ManagerUtils.fillTaskAttachment(new TaskAttachmentWithBytes(oattData.getBytes()), oatt);
		
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Map<String, CustomFieldValue> getTaskCustomValues(int taskId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Integer catId = tasDao.selectCategoryId(con, taskId);
			if (catId == null) return null;
			checkRightsOnCategory(catId, CheckRightsTarget.FOLDER, "READ");
			
			List<OTaskCustomValue> ovals = cvalDao.selectByTask(con, taskId);
			return ManagerUtils.createCustomValuesMap(ovals);
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public Task addTask(Task task) throws WTException {
		CoreManager coreMgr = getCoreManager();
		Connection con = null;
		
		try {
			checkRightsOnCategory(task.getCategoryId(), CheckRightsTarget.ELEMENTS, "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			Set<String> validTags = coreMgr.listTagIds();
			TaskInsertResult result = doTaskInsert(con, task, true, true, true, validTags);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.CREATE, result.otask.getTaskId(), null);
			}
			storeAsSuggestion(coreMgr, SUGGESTION_TASK_SUBJECT, result.otask.getSubject());
			
			Task newTask = ManagerUtils.createTask(result.otask);
			newTask.setAttachments(ManagerUtils.createTaskAttachmentList(result.oattachments));
			return newTask;
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateTask(Task task) throws WTException {
		updateTask(task, true);
	}
	
	@Override
	public void updateTask(Task task, boolean processAttachments) throws WTException {
		updateTask(task, true, true, true);
	}
	
	public void updateTask(Task task, boolean processAttachments, boolean processTags, boolean processCustomValues) throws WTException {
		CoreManager coreMgr = getCoreManager();
		Connection con = null;
		
		try {
			checkRightsOnCategory(task.getCategoryId(), CheckRightsTarget.ELEMENTS, "UPDATE");
			Set<String> validTags = processTags ? coreMgr.listTagIds() : null;
			con = WT.getConnection(SERVICE_ID, false);
			
			boolean ret = doTaskUpdate(con, task, processAttachments, processTags, processCustomValues, validTags);
			if (!ret) throw new WTNotFoundException("Task not found [{}]", task.getTaskId());
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, task.getTaskId(), null);
			}
			
			//TODO: handle subject suggestions

		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteTask(int taskId) throws WTException {
		deleteTask(Arrays.asList(taskId));
	}
	
	@Override
	public void deleteTask(Collection<Integer> taskIds) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			Set<Integer> deleteOkCache = new HashSet<>();
			Map<Integer, Integer> map = tasDao.selectCategoriesByIds(con, taskIds);
			ArrayList<AuditReferenceDataEntry> deleted = new ArrayList<>();
			for (Integer taskId : taskIds) {
				if (taskId == null) continue;
				if (!map.containsKey(taskId)) throw new WTNotFoundException("Task not found [{}]", taskId);
				checkRightsOnCategory(deleteOkCache, map.get(taskId), CheckRightsTarget.ELEMENTS, "DELETE");
				
				if (doTaskDelete(con, taskId)) {
					deleted.add(new AuditTaskObj(taskId));
				} else {
					throw new WTNotFoundException("Task not found [{}]", taskId);
				}
			}
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.DELETE, deleted);
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void moveTask(boolean copy, int taskId, int targetCategoryId) throws WTException {
		moveTask(copy, Arrays.asList(taskId), targetCategoryId);
	}
	
	@Override
	public void moveTask(boolean copy, Collection<Integer> taskIds, int targetCategoryId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(targetCategoryId, CheckRightsTarget.ELEMENTS, "CREATE");
			
			con = WT.getConnection(SERVICE_ID, false);
			Set<Integer> readOkCache = new HashSet<>();
			Set<Integer> deleteOkCache = new HashSet<>();
			Map<Integer, Integer> map = tasDao.selectCategoriesByIds(con, taskIds);
			ArrayList<AuditReferenceDataEntry> copied = new ArrayList<>();
			ArrayList<AuditReferenceDataEntry> moved = new ArrayList<>();
			for (Integer taskId : taskIds) {
				if (taskId == null) continue;
				if (!map.containsKey(taskId)) throw new WTNotFoundException("Task not found [{}]", taskId);
				int categoryId = map.get(taskId);
				checkRightsOnCategory(readOkCache, categoryId, CheckRightsTarget.FOLDER, "READ");
				
				if (copy || (targetCategoryId != categoryId)) {
					if (copy) {
						Task origTask = doTaskGet(con, taskId, false, true, true);
						if (origTask == null) throw new WTNotFoundException("Task not found [{}]", taskId);
						TaskInsertResult result = doTaskCopy(con, origTask, targetCategoryId);
						
						copied.add(new AuditTaskCopyObj(result.otask.getTaskId(), origTask.getTaskId()));
						
					} else {
						checkRightsOnCategory(deleteOkCache, categoryId, CheckRightsTarget.ELEMENTS, "DELETE");
						boolean ret = doTaskMove(con, taskId, targetCategoryId);
						if (!ret) throw new WTNotFoundException("Task not found [{}]", taskId);
						
						moved.add(new AuditTaskMoveObj(taskId, categoryId));
					}	
				}
			}
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				if (copy) {
					writeAuditLog(AuditContext.TASK, AuditAction.CREATE, copied);
				} else {
					writeAuditLog(AuditContext.TASK, AuditAction.MOVE, moved);
				}
			}
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	@Override
	public void moveTask(boolean copy, int taskId, int targetCategoryId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			OTask otask = tasDao.selectById(con, taskId);
			if (otask == null) throw new WTNotFoundException("Task not found [{}]", taskId);
			checkRightsOnCategory(otask.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
			
			if (copy || (targetCategoryId != otask.getCategoryId())) {
				checkRightsOnCategory(targetCategoryId, CheckRightsTarget.ELEMENTS, "CREATE");
				if (!copy) checkRightsOnCategory(otask.getCategoryId(), CheckRightsTarget.ELEMENTS, "DELETE");
				
				Task task = ManagerUtils.createTask(otask);
				boolean ret = doTaskMove(con, copy, task, targetCategoryId);
				if (!ret) throw new WTNotFoundException("Task not found [{}]", taskId);
				
				//FIXME move log below to avoid log in case of errors
				writeLog("TASK_UPDATE", String.valueOf(task.getTaskId()));
			}
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	@Override
	public void updateTaskCategoryTags(final UpdateTagsOperation operation, final int categoryId, final Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		TaskTagDAO ttagDao = TaskTagDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "UPDATE");
			
			if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
				Set<String> validTags = coreMgr.listTagIds();
				List<String> okTagIds = tagIds.stream()
						.filter(tagId -> validTags.contains(tagId))
						.collect(Collectors.toList());
				
				con = WT.getConnection(SERVICE_ID, false);
				if (UpdateTagsOperation.RESET.equals(operation)) ttagDao.deleteByCategory(con, categoryId);
				for (String tagId : okTagIds) {
					ttagDao.insertByCategory(con, categoryId, tagId);
				}
				
			} else if (UpdateTagsOperation.UNSET.equals(operation)) {
				con = WT.getConnection(SERVICE_ID, false);
				ttagDao.deleteByCategoryTags(con, categoryId, tagIds);
			}
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, "*", categoryId);
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateTaskTags(final UpdateTagsOperation operation, final Collection<Integer> taskIds, final Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		TaskTagDAO ttagDao = TaskTagDAO.getInstance();
		Connection con = null;
		
		try {
			List<Integer> okCategoryIds = listAllCategoryIds().stream()
					.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "UPDATE"))
					.collect(Collectors.toList());
			
			if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
				Set<String> validTags = coreMgr.listTagIds();
				List<String> okTagIds = tagIds.stream()
						.filter(tagId -> validTags.contains(tagId))
						.collect(Collectors.toList());
				
				con = WT.getConnection(SERVICE_ID, false);
				if (UpdateTagsOperation.RESET.equals(operation)) ttagDao.deleteByCategoriesTasks(con, okCategoryIds, taskIds);
				for (String tagId : okTagIds) {
					ttagDao.insertByCategoriesTasks(con, okCategoryIds, taskIds, tagId);
				}
				
			} else if (UpdateTagsOperation.UNSET.equals(operation)) {
				con = WT.getConnection(SERVICE_ID, false);
				ttagDao.deleteByCategoriesTasksTags(con, okCategoryIds, taskIds, tagIds);
			}
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				ArrayList<AuditReferenceDataEntry> updated = new ArrayList<>();
				Iterator it = taskIds.iterator();
				while (it.hasNext()) updated.add(new AuditTaskObj((Integer)it.next()));
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, updated);
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void eraseData(boolean deep) throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		CategoryPropsDAO psetDao = CategoryPropsDAO.getInstance();
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		//TODO: controllo permessi
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			UserProfileId pid = getTargetProfileId();
			
			// Erase tasks
			if (deep) {
				for (OCategory ocat : catDao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					tasDao.deleteByCategoryId(con, ocat.getCategoryId());
				}
			} else {
				DateTime revTs = BaseDAO.createRevisionTimestamp();
				for (OCategory ocat : catDao.selectByProfile(con, pid.getDomainId(), pid.getUserId())) {
					tasDao.logicDeleteByCategoryId(con, ocat.getCategoryId(), revTs);
				}
			}
			
			// Erase categories
			psetDao.deleteByProfile(con, pid.getDomainId(), pid.getUserId());
			catDao.deleteByProfile(con, pid.getDomainId(), pid.getUserId());
			
			DbUtils.commitQuietly(con);
			
		} catch(SQLException | DAOException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}

	public List<BaseReminder> getRemindersToBeNotified(DateTime now) {
		ArrayList<BaseReminder> alerts = new ArrayList<>();
		TaskDAO dao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			final boolean shouldLog = logger.isDebugEnabled();
			con = WT.getConnection(SERVICE_ID, false);
			
			DateTime now12 = now.plusHours(14);
			if (shouldLog) logger.debug("Retrieving expired tasks... [ ->| {}]", now12);
			
			List<VTask> tasks = dao.viewExpridedForUpdateByUntil(con, now12);
			HashMap<UserProfileId, Boolean> byEmailCache = new HashMap<>();
			
			int i = 0;
			if (shouldLog) logger.debug("Found {} expired tasks", tasks.size());
			for (VTask task : tasks) {
				i++;
				try {
					if (shouldLog) logger.debug("[{}] Working on task... [{}]", i, task.getTaskId());
					
					UserProfile.Data ud = WT.getUserData(task.getCategoryProfileId());
					if (ud == null) throw new WTException("UserData is null [{}]", task.getCategoryProfileId());
					
					final DateTime profileNow = now.withZone(ud.getTimeZone());
					final DateTime profileReminderDate = task.getReminderDate().withZone(DateTimeZone.UTC).withZoneRetainFields(ud.getTimeZone());
					if (profileReminderDate.isAfter(profileNow)) continue;

					if (!byEmailCache.containsKey(task.getCategoryProfileId())) {
						TasksUserSettings us = new TasksUserSettings(SERVICE_ID, task.getCategoryProfileId());
						boolean bool = us.getTaskReminderDelivery().equals(TasksSettings.TASK_REMINDER_DELIVERY_EMAIL);
						byEmailCache.put(task.getCategoryProfileId(), bool);
					}
					
					if (shouldLog) logger.debug("[{}] Creating alert... [{}]", i, task.getTaskId());
					BaseReminder alert = null;
					if (byEmailCache.get(task.getCategoryProfileId())) {
						alert = createTaskReminderAlertEmail(ud.toProfileI18n(), task, ud.getPersonalEmailAddress());
						
					} else {
						alert = createTaskReminderAlertWeb(ud.toProfileI18n(), task, profileReminderDate);
					}
					
					if (shouldLog) logger.debug("[{}] Updating task record... [{}]", i, task.getTaskId());
					int ret = dao.updateRemindedOn(con, task.getTaskId(), now);
					if (ret != 1) continue;
					
					alerts.add(alert);
					if (shouldLog) logger.debug("[{}] Alert collected [{}]", i, task.getTaskId());
					
				} catch (Throwable t1) {
					logger.warn("[{}] Unable to manage reminder. Task skipped! [{}]", t1, i, task.getTaskId());
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			logger.error("Error handling tasks' reminder alerts", t);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return alerts;
	}
	
	private Category doCategoryInsert(Connection con, Category cat) throws DAOException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		
		OCategory ocat = ManagerUtils.createOCategory(cat);
		ocat.setCategoryId(catDao.getSequence(con).intValue());
		ManagerUtils.fillOCategoryWithDefaults(ocat, getTargetProfileId(), getServiceSettings());
		if (ocat.getIsDefault()) catDao.resetIsDefaultByProfile(con, ocat.getDomainId(), ocat.getUserId());
		
		catDao.insert(con, ocat);
		return ManagerUtils.createCategory(ocat);
	}
	
	private boolean doCategoryUpdate(Connection con, Category cat) throws DAOException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		
		OCategory ocat = ManagerUtils.createOCategory(cat);
		ManagerUtils.fillOCategoryWithDefaults(ocat, getTargetProfileId(), getServiceSettings());
		if (ocat.getIsDefault()) catDao.resetIsDefaultByProfile(con, ocat.getDomainId(), ocat.getUserId());
		
		return catDao.update(con, ocat) == 1;
	}
	
	private TaskObject doTaskObjectPrepare(Connection con, VTaskObject vtask, TaskObjectOutputType outputType) throws WTException {
		if (TaskObjectOutputType.STAT.equals(outputType)) {
			return ManagerUtils.fillTaskObject(new TaskObject(), vtask);
			
		} else {
			Task tas = ManagerUtils.fillTask(new Task(), vtask);
			
			if (TaskObjectOutputType.ICALENDAR.equals(outputType)) {
				throw new WTRuntimeException("ICalendar output not supported yet!");
				/*
				TaskCalObjectWithICalendar co = ManagerUtils.fillTaskCalObject(new TaskCalObjectWithICalendar(), vtask);
				
				//ICalendarOutput out = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()));
				//ICalendar iCalendar = out.toICalendar(tas);
				if (vtask.getHasIcalendar()) {
					//TODO: in order to be fully compliant, merge generated vcard with the original one in db table!
				}
				co.setIcalendar(out.write(iCalendar));
				return co;
				*/
				
			} else {
				TaskObjectWithBean co = ManagerUtils.fillTaskObject(new TaskObjectWithBean(), vtask);
				co.setTask(tas);
				return co;
			}
		}
	}
	
	private Task doTaskGet(Connection con, int taskId, boolean processAttachments, boolean processTags, boolean processCustomValues) throws DAOException, WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskTagDAO tagDao = TaskTagDAO.getInstance();
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		
		OTask otask = tasDao.selectById(con, taskId);
		if (otask == null) return null;
		
		Task task = ManagerUtils.createTask(otask);
		if (processTags) {
			task.setTags(tagDao.selectTagsByTask(con, taskId));
		}
		if (processAttachments) {
			List<OTaskAttachment> oatts = attDao.selectByTask(con, taskId);
			task.setAttachments(ManagerUtils.createTaskAttachmentList(oatts));
		}
		if (processCustomValues) {
			List<OTaskCustomValue> ovals = cvalDao.selectByTask(con, taskId);
			task.setCustomValues(ManagerUtils.createCustomValuesMap(ovals));
		}
		return task;
	}
	
	private TaskInsertResult doTaskInsert(Connection con, Task task, boolean processAttachments, boolean processTags, boolean processCustomValues, Set<String> validTags) throws DAOException, IOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskTagDAO tagDao = TaskTagDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		
		OTask otask = ManagerUtils.createOTask(task);
		otask.setTaskId(tasDao.getSequence(con).intValue());
		ManagerUtils.fillOTaskWithDefaults(otask, getTargetProfileId());
				
		tasDao.insert(con, otask, BaseDAO.createRevisionTimestamp());
		
		Set<String> otags = null;
		if (processTags && task.hasTags()) {
			otags = new LinkedHashSet<>();
			for (String tag : task.getTags()) {
				if (validTags != null && !validTags.contains(tag)) continue;
				//TODO: optimize insertion using multivalue insert
				tagDao.insert(con, otask.getTaskId(), tag);
			}
		}
		
		ArrayList<OTaskAttachment> oatts = null;
		if (processAttachments && task.hasAttachments()) {
			oatts = new ArrayList<>();
			for (TaskAttachment att : task.getAttachments()) {
				if (!(att instanceof TaskAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				oatts.add(doTaskAttachmentInsert(con, otask.getTaskId(), (TaskAttachmentWithStream)att));
			}
		}
		
		ArrayList<OTaskCustomValue> ocvals = null;
		if (processCustomValues && task.hasCustomValues()) {
				ocvals = new ArrayList<>(task.getCustomValues().size());
				for (CustomFieldValue cfv : task.getCustomValues().values()) {
					OTaskCustomValue ocv = ManagerUtils.createOTaskCustomValue(cfv);
					ocv.setTaskId(otask.getTaskId());
					ocvals.add(ocv);
				}
				cvalDao.batchInsert(con, ocvals);
			}
		
		return new TaskInsertResult(otask, otags, oatts, ocvals);
	}
	
	private boolean doTaskUpdate(Connection con, Task task, boolean processAttachments, boolean processTags, boolean processCustomValues, Set<String> validTags) throws DAOException, IOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskTagDAO tagDao = TaskTagDAO.getInstance();
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		
		OTask otask = ManagerUtils.createOTask(task);
		ManagerUtils.fillOTaskWithDefaults(otask, getTargetProfileId());
		boolean ret = tasDao.update(con, otask, BaseDAO.createRevisionTimestamp()) == 1;
		
		if (processTags && task.hasTags()) {
			Set<String> oldTags = tagDao.selectTagsByTask(con, task.getTaskId());
			CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(oldTags, task.getTags());
			for (String tag : changeSet.inserted) {
				if (validTags != null && !validTags.contains(tag)) continue;
				tagDao.insert(con, task.getTaskId(), tag);
			}
			for (String tag : changeSet.deleted) {
				tagDao.delete(con, task.getTaskId(), tag);
			}
		}
		
		if (processAttachments && task.hasAttachments()) {
			List<TaskAttachment> oldAtts = ManagerUtils.createTaskAttachmentList(attDao.selectByTask(con, task.getTaskId()));
			CollectionChangeSet<TaskAttachment> changeSet = LangUtils.getCollectionChanges(oldAtts, task.getAttachments());

			for (TaskAttachment att : changeSet.inserted) {
				if (!(att instanceof TaskAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				doTaskAttachmentInsert(con, otask.getTaskId(), (TaskAttachmentWithStream)att);
			}
			for (TaskAttachment att : changeSet.updated) {
				if (!(att instanceof TaskAttachmentWithStream)) continue;
				doTaskAttachmentUpdate(con, (TaskAttachmentWithStream)att);
			}
			for (TaskAttachment att : changeSet.deleted) {
				attDao.delete(con, att.getAttachmentId());
			}
		}
		
		if (processCustomValues && task.hasCustomValues()) {
			ArrayList<String> customFieldIds = new ArrayList<>();
			ArrayList<OTaskCustomValue> ocvals = new ArrayList<>(task.getCustomValues().size());
			for (CustomFieldValue cfv : task.getCustomValues().values()) {
				OTaskCustomValue ocv = ManagerUtils.createOTaskCustomValue(cfv);
				ocv.setTaskId(otask.getTaskId());
				ocvals.add(ocv);
				customFieldIds.add(ocv.getCustomFieldId());
			}
			//TODO: use upsert when available
			cvalDao.deleteByTaskFields(con, otask.getTaskId(), customFieldIds);
			cvalDao.batchInsert(con, ocvals);
		}
		
		return ret;
	}
	
	private boolean doTaskDelete(Connection con, int taskId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		return tasDao.logicDeleteById(con, taskId, BaseDAO.createRevisionTimestamp()) == 1;
	}
	
	private int doTaskDeleteByCategory(Connection con, int categoryId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		return tasDao.logicDeleteByCategoryId(con, categoryId, BaseDAO.createRevisionTimestamp());
	}
	
	private TaskInsertResult doTaskCopy(Connection con, Task task, int targetCategoryId) throws DAOException, IOException {
		task.setCategoryId(targetCategoryId);
		//TODO: maybe add support to attachments copy
		
		return doTaskInsert(con, task, false, true, true, null);
	}
	
	private boolean doTaskMove(Connection con, int taskId, int targetCategoryId) throws DAOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		return tasDao.updateCategory(con, taskId, targetCategoryId, BaseDAO.createRevisionTimestamp()) == 1;
	}
	
	private OTaskAttachment doTaskAttachmentInsert(Connection con, int taskId, TaskAttachmentWithStream attachment) throws DAOException, IOException {
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		
		OTaskAttachment oatt = ManagerUtils.createOTaskAttachment(attachment);
		oatt.setTaskAttachmentId(IdentifierUtils.getUUIDTimeBased());
		oatt.setTaskId(taskId);
		attDao.insert(con, oatt, BaseDAO.createRevisionTimestamp());
		
		InputStream is = attachment.getStream();
		try {
			attDao.insertBytes(con, oatt.getTaskAttachmentId(), IOUtils.toByteArray(is));
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		return oatt;
	}
	
	private boolean doTaskAttachmentUpdate(Connection con, TaskAttachmentWithStream attachment) throws DAOException, IOException {
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		
		OTaskAttachment oatt = ManagerUtils.createOTaskAttachment(attachment);
		attDao.update(con, oatt, BaseDAO.createRevisionTimestamp());
		
		InputStream is = attachment.getStream();
		try {
			attDao.deleteBytesById(con, oatt.getTaskAttachmentId());
			return attDao.insertBytes(con, oatt.getTaskAttachmentId(), IOUtils.toByteArray(is)) == 1;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private UserProfileId findCategoryOwner(int categoryId) throws WTException {
		CategoryDAO dao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Owner owner = dao.selectOwnerById(con, categoryId);
			return (owner == null) ? null : new UserProfileId(owner.getDomainId(), owner.getUserId());
			
		} catch(SQLException | DAOException ex) {
			throw new WTException(ex, "DB error");
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private void onAfterCategoryAction(int categoryId, UserProfileId owner) {
		if (!owner.equals(getTargetProfileId())) shareCache.init();
	}
	
	private void checkRightsOnCategoryRoot(UserProfileId owner, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		
		if (RunContext.isWebTopAdmin()) return;
		if (owner.equals(targetPid)) return;
		
		String shareId = shareCache.getShareRootIdByOwner(owner);
		if (shareId == null) throw new WTException("ownerToRootShareId({0}) -> null", owner);
		CoreManager core = WT.getCoreManager(targetPid);
		if (core.isShareRootPermitted(shareId, action)) return;
		//if(core.isShareRootPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, shareId)) return;
		
		throw new AuthException("Action not allowed on root share [{0}, {1}, {2}, {3}]", shareId, action, GROUPNAME_CATEGORY, targetPid.toString());
	}
	
	private boolean quietlyCheckRightsOnCategory(int categoryId, CheckRightsTarget target, String action) {
		try {
			checkRightsOnCategory(categoryId, target, action);
			return true;
		} catch(AuthException ex1) {
			return false;
		} catch(WTException ex1) {
			logger.warn("Unable to check rights [{}]", categoryId);
			return false;
		}
	}
	
	private void checkRightsOnCategory(Set<Integer> okCache, int categoryId, CheckRightsTarget target, String action) throws WTException {
		if (!okCache.contains(categoryId)) {
			checkRightsOnCategory(categoryId, target, action);
			okCache.add(categoryId);
		}
	}
	
	private void checkRightsOnCategory(int categoryId, CheckRightsTarget target, String action) throws WTException {
		UserProfileId targetPid = getTargetProfileId();
		Subject subject = RunContext.getSubject();
		UserProfileId runPid = RunContext.getRunProfileId(subject);
		UserProfileId owner = ownerCache.get(categoryId);
		if (owner == null) throw new WTException("categoryToOwner({0}) -> null", categoryId);
		
		if (RunContext.isWebTopAdmin(subject)) {
			// Skip checks for running wtAdmin and sysAdmin target
			if (targetPid.equals(RunContext.getSysAdminProfileId())) return;
			
			// Skip checks if target is the resource owner
			if (owner.equals(targetPid)) return;
			
			// Skip checks if resource is a valid incoming folder
			if (shareCache.getFolderIds().contains(categoryId)) return;
			
			String exMsg = null;
			if (CheckRightsTarget.FOLDER.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on folder '{}' [{}, {}]";
			} else if (CheckRightsTarget.ELEMENTS.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on elements of folder '{}' [{}, {}]";
			}
			throw new AuthException(exMsg, action, runPid, categoryId, GROUPNAME_CATEGORY, targetPid.toString());
			
		} else {
			// Skip checks if target is the resource owner and it's the running profile
			if (owner.equals(targetPid) && targetPid.equals(runPid)) return;
			
			// Checks rights on the wildcard instance (if present)
			CoreManager core = WT.getCoreManager(targetPid);
			String wildcardShareId = shareCache.getWildcardShareFolderIdByOwner(owner);
			if (wildcardShareId != null) {
				if (CheckRightsTarget.FOLDER.equals(target)) {
					if (core.isShareFolderPermitted(wildcardShareId, action)) return;
				} else if (CheckRightsTarget.ELEMENTS.equals(target)) {
					if (core.isShareElementsPermitted(wildcardShareId, action)) return;
					//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, wildcardShareId)) return;
				}
			}
			
			// Checks rights on category instance
			String shareId = shareCache.getShareFolderIdByFolderId(categoryId);
			if (shareId == null) throw new WTException("categoryToLeafShareId({0}) -> null", categoryId);
			if (CheckRightsTarget.FOLDER.equals(target)) {
				if (core.isShareFolderPermitted(shareId, action)) return;
			} else if (CheckRightsTarget.ELEMENTS.equals(target)) {
				if (core.isShareElementsPermitted(shareId, action)) return;
				//if(core.isShareElementsPermitted(SERVICE_ID, RESOURCE_CATEGORY, action, wildcardShareId)) return;
			}
			
			String exMsg = null;
			if (CheckRightsTarget.FOLDER.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on folder '{}' [{}, {}, {}]";
			} else if (CheckRightsTarget.ELEMENTS.equals(target)) {
				exMsg = "Action '{}' not allowed for '{}' on elements of folder '{}' [{}, {}, {}]";
			}
			throw new AuthException(exMsg, action, runPid, categoryId, shareId, GROUPNAME_CATEGORY, targetPid.toString());
		}
	}
	
	private enum CheckRightsTarget {
		FOLDER, ELEMENTS
	}
	
	private ReminderInApp createTaskReminderAlertWeb(ProfileI18n profileI18n, VTask task, DateTime reminderDate) {
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, task.getCategoryProfileId(), "task", String.valueOf(task.getTaskId()));
		alert.setTitle(task.getSubject());
		alert.setDate(reminderDate);
		alert.setTimezone(profileI18n.getTimezone().getID());
		return alert;
	}
	
	private ReminderEmail createTaskReminderAlertEmail(ProfileI18n profileI18n, VTask task, String recipientEmail) throws WTException {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, task.getCategoryProfileId(), "task", String.valueOf(task.getTaskId()));
		
		try {
			String source = NotificationHelper.buildSource(profileI18n.getLocale(), SERVICE_ID);
			String because = lookupResource(profileI18n.getLocale(), TasksLocale.EMAIL_REMINDER_FOOTER_BECAUSE);
			String customBodyHtml = TplHelper.buildTplTaskReminderBody(profileI18n, task);
			
			String subject = TplHelper.buildTaskReminderSubject(profileI18n, task);
			String html = TplHelper.buildTaskReminderHtml(profileI18n.getLocale(), task.getSubject(), customBodyHtml, source, because, recipientEmail);
			
			alert.setSubject(EmailNotification.buildSubject(profileI18n.getLocale(), SERVICE_ID, subject));
			alert.setBody(html);
			
		} catch(IOException | TemplateException | AddressException ex) {
			throw new WTException(ex);
		}
		
		return alert;
	}
	
	private void storeAsSuggestion(CoreManager coreMgr, String context, String value) {
		if (StringUtils.isBlank(value)) return;
		coreMgr.addServiceStoreEntry(SERVICE_ID, context, value.toUpperCase(), value);
	}
	
	private static class TaskInsertResult {
		public final OTask otask;
		public final Set<String> otags;
		public final List<OTaskAttachment> oattachments;
		public final List<OTaskCustomValue> ocustomvalues;
		
		public TaskInsertResult(OTask otask, Set<String> otags, List<OTaskAttachment> oattachments, ArrayList<OTaskCustomValue> ocustomvalues) {
			this.otask = otask;
			this.otags = otags;
			this.oattachments = oattachments;
			this.ocustomvalues = ocustomvalues;
		}
	}
	
	private class OwnerCache extends AbstractMapCache<Integer, UserProfileId> {

		@Override
		protected void internalInitCache(Map<Integer, UserProfileId> mapObject) {}

		@Override
		protected void internalMissKey(Map<Integer, UserProfileId> mapObject, Integer key) {
			try {
				UserProfileId owner = findCategoryOwner(key);
				if (owner == null) throw new WTException("Owner not found [{0}]", key);
				mapObject.put(key, owner);
			} catch(WTException ex) {
				logger.trace("OwnerCache miss", ex);
			}
		}
	}
	
	private class ShareCache extends AbstractShareCache<Integer, ShareRootCategory> {

		@Override
		protected void internalInitCache() {
			final CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
			try {
				for (ShareRootCategory root : internalListIncomingCategoryShareRoots()) {
					shareRoots.add(root);
					ownerToShareRoot.put(root.getOwnerProfileId(), root);
					for (OShare folder : coreMgr.listIncomingShareFolders(root.getShareId(), GROUPNAME_CATEGORY)) {
						if (folder.hasWildcard()) {
							final UserProfileId ownerPid = coreMgr.userUidToProfileId(folder.getUserUid());
							ownerToWildcardShareFolder.put(ownerPid, folder.getShareId().toString());
							for (Category category : listCategories(ownerPid).values()) {
								folderTo.add(category.getCategoryId());
								rootShareToFolderShare.put(root.getShareId(), category.getCategoryId());
								folderToWildcardShareFolder.put(category.getCategoryId(), folder.getShareId().toString());
							}
						} else {
							int categoryId = Integer.valueOf(folder.getInstance());
							folderTo.add(categoryId);
							rootShareToFolderShare.put(root.getShareId(), categoryId);
							folderToShareFolder.put(categoryId, folder.getShareId().toString());
						}
					}
				}
				ready = true;
			} catch(WTException ex) {
				throw new WTRuntimeException(ex.getMessage());
			}
		}
	}
	
	private enum AuditContext {
		CATEGORY, TASK
	}
	
	private enum AuditAction {
		CREATE, UPDATE, DELETE, MOVE
	}
	
	private void writeAuditLog(AuditContext context, AuditAction action, Object reference, Object data) {
		writeAuditLog(EnumUtils.getName(context), EnumUtils.getName(action), (reference != null) ? String.valueOf(reference) : null, (data != null) ? String.valueOf(data) : null);
	}
	
	private void writeAuditLog(AuditContext context, AuditAction action, Collection<AuditReferenceDataEntry> entries) {
		writeAuditLog(EnumUtils.getName(context), EnumUtils.getName(action), entries);
	}
	
	private class AuditTaskObj implements AuditReferenceDataEntry {
		public final int contactId;
		
		public AuditTaskObj(int contactId) {
			this.contactId = contactId;
		}

		@Override
		public String getReference() {
			return String.valueOf(contactId);
		}

		@Override
		public String getData() {
			return null;
		}
	}
	
	private class AuditTaskMoveObj implements AuditReferenceDataEntry {
		public final int taskId;
		public final int origCategoryId;
		
		public AuditTaskMoveObj(int taskId, int origCategoryId) {
			this.taskId = taskId;
			this.origCategoryId = origCategoryId;
		}

		@Override
		public String getReference() {
			return String.valueOf(taskId);
		}

		@Override
		public String getData() {
			return String.valueOf(origCategoryId);
		}
	}
	
	private class AuditTaskCopyObj implements AuditReferenceDataEntry {
		public final int taskId;
		public final int origTaskId;
		
		public AuditTaskCopyObj(int taskId, int origTaskId) {
			this.taskId = taskId;
			this.origTaskId = origTaskId;
		}

		@Override
		public String getReference() {
			return String.valueOf(taskId);
		}

		@Override
		public String getData() {
			return String.valueOf(origTaskId);
		}
	}
}
