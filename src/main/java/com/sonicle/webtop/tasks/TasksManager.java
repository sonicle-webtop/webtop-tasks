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

import com.sonicle.commons.BitFlag;
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
import com.sonicle.webtop.tasks.model.TaskAttachment;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithBytes;
import com.sonicle.webtop.tasks.model.TaskAttachmentWithStream;
import com.sonicle.webtop.tasks.model.TaskObject;
import com.sonicle.webtop.tasks.model.TaskObjectChanged;
import com.sonicle.webtop.tasks.model.TaskObjectWithBean;
import com.sonicle.webtop.tasks.model.TaskObjectWithICalendar;
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
import com.sonicle.webtop.core.app.sdk.AuditReferenceDataEntry;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.util.ExceptionUtils;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.webtop.tasks.bol.OTaskCustomValue;
import com.sonicle.webtop.tasks.bol.OTaskRecurrence;
import com.sonicle.webtop.tasks.dal.TaskCustomValueDAO;
import com.sonicle.webtop.tasks.dal.TaskICalendarsDAO;
import com.sonicle.webtop.tasks.dal.TaskPredicateVisitor;
import com.sonicle.webtop.tasks.dal.TaskRecurrenceDAO;
import com.sonicle.webtop.tasks.dal.TaskTagDAO;
import com.sonicle.webtop.tasks.model.TaskInstance;
import com.sonicle.webtop.tasks.model.TaskQuery;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.fortuna.ical4j.model.Recur;
import org.apache.shiro.subject.Subject;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import com.sonicle.commons.BitFlagEnum;
import com.sonicle.commons.beans.SortInfo;
import com.sonicle.commons.concurrent.KeyedReentrantLocks;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.time.InstantRange;
import com.sonicle.commons.web.json.CId;
import com.sonicle.webtop.core.app.util.log.LogHandler;
import com.sonicle.webtop.core.app.util.log.LogMessage;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.tasks.bol.OChildrenCount;
import com.sonicle.webtop.tasks.bol.OTaskAssignee;
import com.sonicle.webtop.tasks.bol.OTaskInstanceInfo;
import com.sonicle.webtop.tasks.dal.TaskAssigneeDAO;
import com.sonicle.webtop.tasks.io.ICalendarInput;
import com.sonicle.webtop.tasks.io.ICalendarOutput;
import com.sonicle.webtop.tasks.io.TaskInput;
import com.sonicle.webtop.tasks.io.TasksStreamReader;
import com.sonicle.webtop.tasks.model.TaskAlertLookup;
import com.sonicle.webtop.tasks.model.TaskAlertLookupInstance;
import com.sonicle.webtop.tasks.model.TaskAssignee;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskEx;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import com.sonicle.webtop.tasks.model.TaskLookupInstance;
import com.sonicle.webtop.tasks.model.TaskRecurrence;
import jakarta.mail.internet.InternetAddress;
import java.util.Collections;
import java.util.Comparator;
import net.fortuna.ical4j.data.ParserException;
import net.sf.qualitycheck.Check;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.joda.time.DateTimeComparator;
import org.joda.time.Days;

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
	private final KeyedReentrantLocks locks = new KeyedReentrantLocks<String>();
	
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
	public Set<Integer> listMyCategoryIds() throws WTException {
		return listCategoryIds(getTargetProfileId());
	}
	
	@Override
	public Set<Integer> listIncomingCategoryIds() throws WTException {
		return shareCache.getFolderIds();
	}
	
	@Override
	public Set<Integer> listIncomingCategoryIds(final UserProfileId owner) throws WTException {
		if (owner == null) {
			return listIncomingCategoryIds();
		} else {
			String rootId = shareCache.getShareRootIdByOwner(owner);
			if (rootId == null) return null;
			return shareCache.getFolderIds().stream()
				.filter(categoryId -> rootId.equals(shareCache.getShareRootIdByFolderId(categoryId)))
				.collect(Collectors.toCollection(LinkedHashSet::new));
		}
	}
	
	@Override
	public Set<Integer> listAllCategoryIds() throws WTException {
		return Stream.concat(listMyCategoryIds().stream(), listIncomingCategoryIds().stream())
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
	public Map<Integer, Category> listMyCategories() throws WTException {
		return listCategories(getTargetProfileId());
	}
	
	@Override
	public Map<Integer, Category> listIncomingCategories() throws WTException {
		return listIncomingCategories(null);
	}
	
	@Override
	public Map<Integer, Category> listIncomingCategories(final UserProfileId owner) throws WTException {
		Set<Integer> ids = listIncomingCategoryIds(owner);
		if (ids == null) return null;
		
		CategoryDAO catDao = CategoryDAO.getInstance();
		LinkedHashMap<Integer, Category> items = new LinkedHashMap<>();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			for (OCategory ocat : catDao.selectByDomainIn(con, getTargetProfileId().getDomainId(), ids)) {
				items.put(ocat.getCategoryId(), ManagerUtils.createCategory(ocat));
			}
			return items;
			
		} catch(Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
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
	public Integer getDefaultCategoryId() throws WTException {
		TasksUserSettings us = new TasksUserSettings(SERVICE_ID, getTargetProfileId());
		
		Integer categoryId = null;
		try (KeyedReentrantLocks.KeyedLock lock = locks.tryAcquire("getDefaultCategoryId", 60 * 1000)) {
			if (lock != null) {
				categoryId = us.getDefaultCategoryFolder();
				if (categoryId == null || !quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "CREATE")) {
					try {
						categoryId = getBuiltInCategoryId();
						if (categoryId == null) throw new WTException("Built-in category is null");
						us.setDefaultCategoryFolder(categoryId);
					} catch (Throwable t) {
						logger.error("Unable to get built-in category", t);
					}
				}
			}
		}
		return categoryId;
	}
	
	@Override
	public Integer getBuiltInCategoryId() throws WTException {
		CategoryDAO catDao = CategoryDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Integer catId = catDao.selectBuiltInIdByProfile(con, getTargetProfileId().getDomainId(), getTargetProfileId().getUserId());
			if (catId == null) return null;
			
			checkRightsOnCategory(catId, CheckRightsTarget.FOLDER, "READ");
			
			return catId;
			
		} catch(SQLException | DAOException | WTException ex) {
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
			cat = doCategoryInsert(con, cat);
			
			DbUtils.commitQuietly(con);
			onAfterCategoryAction(cat.getCategoryId(), cat.getProfileId());
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.CATEGORY, AuditAction.CREATE, cat.getCategoryId(), null);
			}
			
			// Sets category as default
			TasksUserSettings us = new TasksUserSettings(SERVICE_ID, cat.getProfileId());
			us.setDefaultCategoryFolder(cat.getCategoryId());
			
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
			doTaskDeleteByCategory(con, categoryId, true);
			
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
		CoreManager coreMgr = getCoreManager();
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			con = WT.getConnection(SERVICE_ID);
			
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			ArrayList<TaskObject> items = new ArrayList<>();
			Map<String, List<VTaskObject>> map = tasDao.viewTaskObjectsByCategory(con, TaskObjectOutputType.STAT.equals(outputType), categoryId);
			for (List<VTaskObject> vtasks : map.values()) {
				if (vtasks.isEmpty()) continue;
				VTaskObject vtask = vtasks.get(vtasks.size()-1);
				if (vtasks.size() > 1) {
					logger.trace("Many tasks ({}) found for same href [{} -> {}]", vtasks.size(), vtask.getHref(), vtask.getTaskId());
				}
				items.add(doTaskObjectPrepare(con, vtask, outputType, tagNamesByIdMap));
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
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			con = WT.getConnection(SERVICE_ID);
			
			ArrayList<TaskObjectChanged> inserted = new ArrayList<>();
			ArrayList<TaskObjectChanged> updated = new ArrayList<>();
			ArrayList<TaskObjectChanged> deleted = new ArrayList<>();
			
			if (limit == null) limit = Integer.MAX_VALUE;
			if (since == null) {
				List<VTaskObjectChanged> vtasks = tasDao.viewOnlineTaskObjectsChangedByCategory(con, categoryId, limit);
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
		CoreManager coreMgr = getCoreManager();
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			con = WT.getConnection(SERVICE_ID);
			
			Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
			ArrayList<TaskObjectWithICalendar> items = new ArrayList<>();
			Map<String, List<VTaskObject>> map = tasDao.viewTaskObjectsByCategoryHrefs(con, false, categoryId, hrefs);
			for (String href : hrefs) {
				List<VTaskObject> vtasks = map.get(href);
				if (vtasks == null) continue;
				if (vtasks.isEmpty()) continue;
				VTaskObject vtask = vtasks.get(vtasks.size()-1);
				if (vtasks.size() > 1) {
					logger.trace("Many tasks ({}) found for same href [{} -> {}]", vtasks.size(), vtask.getHref(), vtask.getTaskId());
				}
				
				items.add((TaskObjectWithICalendar)doTaskObjectPrepare(con, vtask, TaskObjectOutputType.ICALENDAR, tagNamesByIdMap));
			}
			return items;
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public TaskObject getTaskObject(final TaskInstanceId instanceId, final TaskObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			String realTaskId = doTaskGetInstanceTaskId(con, instanceId);
			
			VTaskObject vtask = tasDao.viewTaskObjectById(con, (realTaskId != null) ? realTaskId : instanceId.getTaskId());
			if (vtask == null) {
				return null;
			} else {
				checkRightsOnCategory(vtask.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
				
				Map<String, String> tagNamesByIdMap = coreMgr.listTagNamesById();
				return doTaskObjectPrepare(con, vtask, outputType, tagNamesByIdMap);
			}
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	public TaskObject getTaskObject(int categoryId, String taskId, TaskObjectOutputType outputType) throws WTException {
		CoreManager coreMgr = getCoreManager();
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ");
			con = WT.getConnection(SERVICE_ID);
			
			VTaskObject vtask = tasDao.viewTaskObjectById(con, taskId);
			if (vtask == null) {
				return null;
			} else {
				Map<String, List<String>> tagIdsByNameMap = coreMgr.listTagIdsByName();
				return doTaskObjectPrepare(con, vtask, outputType, tagIdsByNameMap);
			}
			
		} catch (SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	/*
	public List<TaskLookupInstance> listTaskInstances_(Collection<Integer> categoryIds, Condition<TaskQuery> conditionPredicate, DateTimeZone targetTimezone) throws WTException {
		return listTaskInstances_(categoryIds, null, (DateTimeRange)null, conditionPredicate, targetTimezone, true);
	}
	
	public List<TaskLookupInstance> listTaskInstances_(Collection<Integer> categoryIds, TaskListView view, DateTimeRange range, Condition<TaskQuery> conditionPredicate, DateTimeZone targetTimezone, boolean sort) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			UserProfileId runProfile = RunContext.getRunProfileId();
			List<Integer> okCategoryIds = categoryIds.stream()
				.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ"))
				.collect(Collectors.toList());
			
			DateTime queryFrom = null;
			DateTime queryTo = null;
			org.jooq.Condition queryCondition = null;
			org.jooq.Condition queryRecCondition = null;
			DateTime instFrom = null;
			DateTime instTo = null;
			int noOfRecurringInst = 368;
			if (view != null) {
				if (TaskListView.TODAY.equals(view)) {
					queryTo = instTo = DateTimeUtils.now().withZone(targetTimezone).toLocalDate().plusDays(1).toDateTimeAtStartOfDay(targetTimezone);
				} else if (TaskListView.NEXT_7.equals(view)) {
					queryTo = instTo = DateTimeUtils.now().withZone(targetTimezone).toLocalDate().plusDays(8).toDateTimeAtStartOfDay(targetTimezone);
				}
				queryCondition = queryRecCondition = tasDao.toCondition(view, DateTimeUtils.now());
				
			} else {
				TaskPredicateVisitor tpv = new TaskPredicateVisitor(TaskPredicateVisitor.Target.NORMAL)
					.withIgnoreCase(true)
					.withForceStringLikeComparison(true);
				if (conditionPredicate != null) {
					queryCondition = BaseDAO.createCondition(conditionPredicate, tpv);
					queryRecCondition = BaseDAO.createCondition(conditionPredicate, new TaskPredicateVisitor(TaskPredicateVisitor.Target.RECURRING)
						.withIgnoreCase(true)
						.withForceStringLikeComparison(true)
					);
				}
				
				queryFrom = (range != null) ? range.from : null;
				queryTo = (range != null) ? range.to : null;
				instFrom = tpv.hasFromRange() ? tpv.getFromRange() : queryFrom;
				instTo = tpv.hasToRange() ? tpv.getToRange() : queryTo;
				Days days = DateTimeUtils.daysBetween(queryFrom, queryTo);
				if (days != null) noOfRecurringInst = days.getDays() + 2;
			}
			
			con = WT.getConnection(SERVICE_ID);
			ArrayList<TaskLookupInstance> instances = new ArrayList<>();
			for (VTaskLookup vtas : tasDao.viewByCategoryRangeCondition(false, con, okCategoryIds, queryFrom, queryTo, queryCondition)) {
				boolean keepPrivate = treatTaskAsPrivate(runProfile, vtas.getCategoryProfileId(), vtas.getIsPrivate());
				
				TaskLookupInstance item = ManagerUtils.fillTaskLookup(new TaskLookupInstance(), vtas);
				item.setId(TaskInstanceId.build(vtas.getTaskId(), vtas.getSeriesTaskId(), vtas.getSeriesInstanceId()));
				if (keepPrivate) item.censorize();
				instances.add(item);
			}
			for (VTaskLookup vtas : tasDao.viewByCategoryRangeCondition(true, con, okCategoryIds, queryFrom, queryTo, queryRecCondition)) {
				boolean keepPrivate = treatTaskAsPrivate(runProfile, vtas.getCategoryProfileId(), vtas.getIsPrivate());
				
				if (TaskListView.ALL.equals(view)) {
					TaskLookupInstance item = ManagerUtils.fillTaskLookup(new TaskLookupInstance(), vtas);
					item.setId(TaskInstanceId.build(vtas.getTaskId(), vtas.getSeriesTaskId(), vtas.getSeriesInstanceId()));
					if (keepPrivate) item.censorize();
					instances.add(item);
				} else {
					instances.addAll(calculateRecurringInstances(con, new TaskLookupInstanceMapper(vtas, keepPrivate), instFrom, instTo, noOfRecurringInst));
				}
			}
			
			//TODO: transform to an ordered insert
			if (sort) {
				Comparator<TaskLookupInstance> compStartDate = Comparator.comparing(TaskLookupInstance::getStart, (s1, s2) -> {
					return (s1 != null && s2 != null) ? s1.compareTo(s2) : (s1 == null ? -1 : 1);
				});
				Comparator<TaskLookupInstance> compSubject = Comparator.comparing(TaskLookupInstance::getSubject, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
				Collections.sort(instances, compStartDate.thenComparing(compSubject));
			}
			
			return instances;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	*/
	
	@Override
	public List<TaskLookupInstance> listTaskInstances(final Collection<Integer> categoryIds, final TaskListView view, final DateTimeZone targetTimezone) throws WTException {
		return listTaskInstances(categoryIds, view, (InstantRange)null, null, null, targetTimezone);
	}
	
	@Override
	public List<TaskLookupInstance> listTaskInstances(final Collection<Integer> categoryIds, final Condition<TaskQuery> conditionPredicate, final SortInfo sortInfo, final DateTimeZone targetTimezone) throws WTException {
		return listTaskInstances(categoryIds, null, (InstantRange)null, conditionPredicate, sortInfo, targetTimezone);
	}
	
	@Override
	public List<TaskLookupInstance> listTaskInstances(final Collection<Integer> categoryIds, final TaskListView view, final InstantRange viewRange, final Condition<TaskQuery> conditionPredicate, final SortInfo sortInfo, final DateTimeZone targetTimezone) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			boolean nestResults = true;
			UserProfileId runProfile = RunContext.getRunProfileId();
			List<Integer> okCategoryIds = categoryIds.stream()
				.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.FOLDER, "READ"))
				.collect(Collectors.toList());
			
			org.jooq.Condition queryCondition = null;
			DateTime from = null;
			DateTime to = null;
			int noOfRecurringInst = 367;
			if (view != null) {
				DateTime now = DateTimeUtils.now().withZone(targetTimezone);
				queryCondition = tasDao.toCondition(view, now);
				if (TaskListView.TODAY.equals(view) || TaskListView.NOT_STARTED.equals(view) || TaskListView.LATE.equals(view) || TaskListView.COMPLETED.equals(view)) {
					to = now.toLocalDate().plusDays(1).toDateTimeAtStartOfDay(targetTimezone);
				} else if (TaskListView.NEXT_7.equals(view) || TaskListView.UPCOMING.equals(view)) {
					to = now.toLocalDate().plusDays(8).toDateTimeAtStartOfDay(targetTimezone);
				} else if (TaskListView.NOT_COMPLETED.equals(view)) {
					to = now.toLocalDate().plusDays(367).toDateTimeAtStartOfDay(targetTimezone);
					noOfRecurringInst = 1;
				}
				if (TaskListView.UPCOMING.equals(view)) nestResults = false;
				
			} else {
				TaskPredicateVisitor tpv = new TaskPredicateVisitor(null)
					.withInstanceIdDecoder((instanceId) -> {
						Connection con1 = null;
						try {
							con1 = WT.getConnection(SERVICE_ID);
							return doGetInstanceInfo(con1, instanceId).taskId;

						} catch(SQLException | DAOException ex) {
							return null;
						} finally {
							DbUtils.closeQuietly(con1);
						}
					})
					.withIgnoreCase(true)
					.withForceStringLikeComparison(true);
				if (conditionPredicate != null) {
					queryCondition = BaseDAO.createCondition(conditionPredicate, tpv);
					nestResults = false;
				}
				
				from = (viewRange != null) ? viewRange.getStart() : null;
				to = (viewRange != null) ? viewRange.getEnd() : null;
				from = tpv.getRangeStartOrDefault(from);
				to = tpv.getRangeEndOrDefault(to);
				
				Days daysInRange = DateTimeUtils.daysBetween(from, to);
				if (daysInRange != null) noOfRecurringInst = daysInRange.getDays() + 2;
			}
			
			con = WT.getConnection(SERVICE_ID);
			
			final String nullParent = "00000000000000000000000000000000";
			Map<String, TaskLookupInstance> parents = new HashMap<>();
			MultiValuedMap<String, TaskLookupInstance> instancesByParent = new ArrayListValuedHashMap<>();
			ArrayList<TaskLookupInstance> instances = new ArrayList<>();
			for (VTaskLookup vtas : tasDao.viewOnlineByCategoryRangeCondition(con, okCategoryIds, from, to, queryCondition)) {
				final boolean keepPrivate = treatTaskAsPrivate(runProfile, vtas.getCategoryProfileId(), vtas.getIsPrivate());
				final String parentId = vtas.getParentTaskId();
				
				if (!vtas.getHasRecurrence() || TaskListView.ALL.equals(view)) {
					TaskLookupInstance item = ManagerUtils.fillTaskLookup(new TaskLookupInstance(), vtas);
					item.setId(TaskInstanceId.build(vtas.getTaskId(), vtas.getSeriesTaskId(), vtas.getSeriesInstanceId()));
					item.setTaskId(vtas.getTaskId());
					if (keepPrivate) item.censorize();
					
					if (nestResults) {
						if (vtas.getHasChildren()) parents.put(item.getTaskId(), item);
						instancesByParent.put(parentId == null ? nullParent : parentId, item);
					} else {
						instances.add(item);
					}
					
				} else {
					final List<TaskLookupInstance> items = calculateRecurringInstances(con, new TaskLookupInstanceMapper(vtas, keepPrivate), from, to, noOfRecurringInst);
					
					if (nestResults) {
						TaskLookupInstance item = ManagerUtils.fillTaskLookup(new TaskLookupInstance(), vtas);
						item.setId(TaskInstanceId.build(vtas.getTaskId(), vtas.getSeriesTaskId(), vtas.getSeriesInstanceId()));
						item.setTaskId(vtas.getTaskId());
						
						if (vtas.getHasChildren()) parents.put(item.getTaskId(), item);
						instancesByParent.putAll(parentId == null ? nullParent : parentId, items);
					} else {
						instances.addAll(items);
					}
				}
			}
			
			if (TaskListView.UPCOMING.equals(view)) {
				Collections.sort(instances, getComparator("due", SortInfo.Direction.ASC)
					.thenComparing(getComparator("subject", SortInfo.Direction.ASC)));
				
			} else if (sortInfo != null) {
				if (nestResults) {
					Comparator mainComp = getComparator(sortInfo.getField(), sortInfo.getDirection());
					Comparator subjComp = getComparator("subject", SortInfo.Direction.ASC);
					List<TaskLookupInstance> topLevel = new ArrayList<>(instancesByParent.get(nullParent));
					Map<String, List<TaskLookupInstance>> byParent = new HashMap<>();
					
					// ---------->
					//logger.debug("Dump for topLevel");
					//for (TaskLookupInstance instance : topLevel) {
					//	logger.debug("topLevel: '{}' [{}]", instance.getSubject(), instance.getIdAsString());
					//}
					// <----------

					// 1 - Sort each parents' items and populate top-level list to be sorted adding a reference item (the 1st of the resulting list).
					for (String taskId : instancesByParent.keySet()) {
						if (nullParent.equals(taskId)) continue;
						
						List<TaskLookupInstance> list = new ArrayList<>(instancesByParent.get(taskId));
						if (list.size() > 1) Collections.sort(list, mainComp.thenComparing(subjComp));
						
						TaskLookupInstance parentInstance = parents.get(taskId);
						//if (parentInstance != null) {
						//	logger.debug("Sorting items for '{}' [{}]", parentInstance.getSubject(), parentInstance.getIdAsString());
						//} else {
						//	logger.debug("Parent for '{}' missing", taskId);
						//}
						byParent.put(taskId, list);
						if (!list.isEmpty() && parentInstance != null) {
							// Replace topLevel parent with its first children (reference item) to allow sorting on it (see step 2).
							// Then in step 3, reference item will be replaced by the real parent!
							topLevel.remove(parentInstance);
							topLevel.add(list.get(0));
						} else if (parentInstance == null) {
							topLevel.addAll(list);
						}
					}

					// 2 - Sort top-level items
					Collections.sort(topLevel, mainComp.thenComparing(subjComp));
					
					// ---------->
					//logger.debug("Dump for topLevel");
					//for (TaskLookupInstance instance : topLevel) {
					//	logger.debug("topLevel: '{}' [{}]", instance.getSubject(), instance.getIdAsString());
					//}
					// <----------

					// 3 - Loop over sorted top-level items and move them into target collection, adding group items in case of reference items.
					for (TaskLookupInstance instance : topLevel) {
						//if (instance.getParentTaskId() != null) {
						if (instance.getParentInstanceId() != null) {
							// ---------->
							//logger.debug("Found topLevel placeholder for parent '{}' [{}]", instance.getParentTaskId(), instance.getIdAsString());
							// <----------
							//TaskLookupInstance pinstance = parents.get(instance.getParentTaskId());
							TaskLookupInstance pinstance = parents.get(instance.getParentInstanceId().getTaskId());
							if (pinstance != null) instances.add(pinstance);
							//logger.debug("Adding parent items");
							//instances.addAll(byParent.get(instance.getParentTaskId()));
							instances.addAll(byParent.get(instance.getParentInstanceId().getTaskId()));
						} else {
							instances.add(instance);
						}
					}
					
				} else {
					Collections.sort(instances, getComparator("start", SortInfo.Direction.ASC)
						.thenComparing(getComparator("subject", SortInfo.Direction.ASC)));
				}
			}			
			return instances;
			
		} catch (Throwable t) {
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private Comparator getComparator(String field, SortInfo.Direction direction) {
		Comparator comp = null;
		if ("subject".equals(field)) {
			comp = Comparator.comparing(TaskLookupInstance::getSubject, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
		} else if ("start".equals(field)) {
			comp = Comparator.comparing(TaskLookupInstance::getStart, Comparator.nullsFirst(DateTimeComparator.getInstance()));
		} else if ("due".equals(field)) {
			comp = Comparator.comparing(TaskLookupInstance::getDue, Comparator.nullsFirst(DateTimeComparator.getInstance()));
		} else {
			throw new IllegalArgumentException("Field not supported");
		}
		return SortInfo.Direction.ASC.equals(direction) ? comp : comp.reversed();
	}
	
	@Override
	public TaskInstance getTaskInstance(final TaskInstanceId instanceId) throws WTException {
		return getTaskInstance(instanceId, BitFlag.of(TaskGetOptions.ATTACHMENTS, TaskGetOptions.TAGS, TaskGetOptions.CUSTOM_VALUES));
	}
	
	@Override
	public TaskInstance getTaskInstance(final TaskInstanceId instanceId, final BitFlag<TaskGetOptions> options) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			String realTaskId = doTaskGetInstanceTaskId(con, instanceId);
			BitFlag<TaskProcessOpts> options2 = TaskProcessOpts.parseTaskGetOptions(options).set(TaskProcessOpts.RECUR).set(TaskProcessOpts.RECUR_EX);
			Task task = null;
			if (realTaskId != null) {
				task = doTaskGet(con, realTaskId, options2);
			} else {
				task = doTaskGet(con, instanceId.getTaskId(), options2);
			}
			if (task == null) return null;
			
			if (!instanceId.hasNoInstance()) {
				if (task.getStart() == null) throw new WTException("Start date cannot be null");
				
				Integer dueDays = (task.getDue() != null) ? Math.abs(Days.daysBetween(task.getStart().toLocalDate(), task.getDue().toLocalDate()).getDays()) : null;
				task.setStart(instanceDateToDateTime(instanceId.getInstanceAsDate(), task.getStart(), task.getTimezoneObject()));
				if (dueDays != null) {
					task.setDue(instanceDateToDateTime(instanceId.getInstanceAsDate().plusDays(dueDays), task.getDue(), task.getTimezoneObject()));
				}
			}
			
			checkRightsOnCategory(task.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
			return new TaskInstance(instanceId, task);
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	private DateTime instanceDateToDateTime(LocalDate instanceDate, DateTime targetDateTime, DateTimeZone targetTimezone) {
		// In order to rebuild a valid DateTime from an instanceDate, since that 
		// instanceDate is conventionally in UTC, we need to extract the time from
		// a target DateTime and converts it into UTC. Secondly, we can build 
		// a new DateTime starting from the instanceDate (always in UTC timezone),
		// and finally set the target timezone, transporting the entire DateTime 
		// to the right zone.
		return instanceDate.toDateTime(targetDateTime.withZone(DateTimeZone.UTC).toLocalTime(), DateTimeZone.UTC).withZone(targetTimezone);
	}
	
	private InstanceInfo doGetInstanceInfo(Connection con, TaskInstanceId instanceId) {
		TaskDAO tasDao = TaskDAO.getInstance();
		return new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
	}
	
	@Override
	public TaskAttachmentWithBytes getTaskInstanceAttachment(final TaskInstanceId instanceId, final String attachmentId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			InstanceInfo info = doGetInstanceInfo(con, instanceId);
			String taskId = info.realTaskId();
			Integer catId = tasDao.selectCategoryId(con, taskId);
			checkRightsOnCategory(catId, CheckRightsTarget.FOLDER, "READ");
			
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
	public Map<String, CustomFieldValue> getTaskInstanceCustomValues(final TaskInstanceId instanceId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
			String taskId = info.realTaskId();
			Integer catId = tasDao.selectCategoryId(con, taskId);
			if (catId == null) return null;
			checkRightsOnCategory(catId, CheckRightsTarget.FOLDER, "READ");
			
			List<OTaskCustomValue> ovals = cvalDao.selectByTask(con, taskId);
			return ManagerUtils.createCustomValuesMap(ovals);
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateTaskInstance(final TaskInstanceId instanceId, final TaskEx task) throws WTException {
		updateTaskInstance(instanceId, task, BitFlag.of(TaskUpdateOptions.ASSIGNEES, TaskUpdateOptions.ATTACHMENTS, TaskUpdateOptions.TAGS, TaskUpdateOptions.CUSTOM_VALUES, TaskUpdateOptions.CONTACT_REF, TaskUpdateOptions.DOCUMENT_REF));
	}
	
	@Override
	public void updateTaskInstance(final TaskInstanceId instanceId, final TaskEx task, final BitFlag<TaskUpdateOptions> options) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID, false);
			InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
			Integer catId = tasDao.selectCategoryId(con, info.realTaskId());
			if (catId == null) throw new WTNotFoundException("Task category not found [{}]", info.taskId);
			
			checkRightsOnCategory(catId, CheckRightsTarget.ELEMENTS, "UPDATE");
			//TODO: throw AuthException on private tasks...
			
			doTaskInstanceUpdateAndCommit(con, info, task, TaskProcessOpts.parseTaskUpdateOptions(options).set(TaskProcessOpts.RECUR));
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void updateQuickTaskInstance(final TaskInstanceId instanceId, final Boolean completed, final Short progress, final Short importance) throws WTException {
		updateQuickTaskInstance(Arrays.asList(instanceId), completed, progress, importance);
	}
	
	@Override
	public void updateQuickTaskInstance(final Collection<TaskInstanceId> instanceIds, final Boolean completed, final Short progress, final Short importance) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			if (instanceIds.size() == 1) {
				TaskInstanceId instanceId = instanceIds.iterator().next();
				con = WT.getConnection(SERVICE_ID, false);
				InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
				Integer catId = tasDao.selectCategoryId(con, info.realTaskId());
				if (catId == null) throw new WTNotFoundException("Task category not found [{}]", instanceId);

				checkRightsOnCategory(catId, CheckRightsTarget.ELEMENTS, "UPDATE");

				Task origTask = doTaskGet(con, info.realTaskId(), BitFlag.none());
				if (origTask == null) throw new WTException("Task not found [{}]", info.realTaskId());
				if (completed != null) {
					origTask.setStatus(completed ? TaskBase.Status.COMPLETED : TaskBase.Status.NEEDS_ACTION);
				} else if (progress != null) {
					origTask.setProgress(progress);
				} else if (importance != null) {
					origTask.setImportance(importance);
				}
				try {
					this.doTaskInstanceUpdateAndCommit(con, info, origTask, BitFlag.none());
				} catch (IOException ex1) {/* Due configuration here this will never happen! */}
				
			} else {
				Set<Integer> okCategoryIds = listAllCategoryIds().stream()
					.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "UPDATE"))
					.collect(Collectors.toSet());

				con = WT.getConnection(SERVICE_ID, false);

				// Collect necessary data
				InstancesDataResult idr = collectInstancesData(con, instanceIds);
				Map<String, Integer> catMap = tasDao.selectCategoriesByIds(con, idr.involvedTaskIds);

				for (Map.Entry<TaskInstanceId, InstanceInfo> entry : idr.infoMap.entrySet()) {
					InstanceInfo info = entry.getValue();
					String taskId = info.realTaskId();
					if (taskId == null) {
						logger.debug("Task ID is null");
						continue;
					}
					if (!catMap.containsKey(taskId)) {
						logger.warn("Task not found [{}]", entry.getKey());
						continue;
					}
					int categoryId = catMap.get(taskId);
					if (!okCategoryIds.contains(categoryId)) {
						logger.warn("Operation not allowed on folder [{}]", categoryId);
						continue;
					}

					Task origTask = doTaskGet(con, taskId, BitFlag.none());
					if (origTask == null) {
						logger.warn("Task not found [{}]", taskId);
						continue;
					}
					if (completed != null) {
						origTask.setStatus(completed ? TaskBase.Status.COMPLETED : TaskBase.Status.NEEDS_ACTION);
					} else if (progress != null) {
						origTask.setProgress(progress);
					} else if (importance != null) {
						origTask.setImportance(importance);
					}
					try {
						this.doTaskInstanceUpdateAndCommit(con, info, origTask, BitFlag.none());
					} catch (IOException ex1) {/* Due configuration here this will never happen! */}
				}
			}
			
		} catch (SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void deleteTaskInstance(final TaskInstanceId instanceId) throws WTException {
		deleteTaskInstance(Arrays.asList(instanceId));
	}
	
	@Override
	public void deleteTaskInstance(final Collection<TaskInstanceId> instanceIds) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			if (instanceIds.size() == 1) {
				TaskInstanceId instanceId = instanceIds.iterator().next();
				con = WT.getConnection(SERVICE_ID, false);
				InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
				Integer catId = tasDao.selectCategoryId(con, info.realTaskId());
				if (catId == null) throw new WTNotFoundException("Task category not found [{}]", instanceId);
				
				checkRightsOnCategory(catId, CheckRightsTarget.ELEMENTS, "DELETE");
				//TODO: throw AuthException on private taks...

				if (!doTaskInstanceDeleteAndCommit(con, info)) {
					throw new WTNotFoundException("Task not found [{}]", instanceId);
				}
				
			} else {
				con = WT.getConnection(SERVICE_ID, false);
			
				// Collect necessary data
				InstancesDataResult idr = collectInstancesData(con, instanceIds);
				Map<String, Integer> catMap = tasDao.selectCategoriesByIds(con, idr.involvedTaskIds);
				
				// Perform delete operation on instances
				Set<Integer> deleteOkCache = new HashSet<>();
				ArrayList<AuditReferenceDataEntry> deleted = new ArrayList<>();
				for (Map.Entry<TaskInstanceId, InstanceInfo> entry : idr.infoMap.entrySet()) {
					InstanceInfo info = entry.getValue();
					String taskId = info.realTaskId();
					if (taskId == null) continue;
					if (!catMap.containsKey(taskId)) throw new WTNotFoundException("Task not found [{}]", entry.getKey());
					checkRightsOnCategory(deleteOkCache, catMap.get(taskId), CheckRightsTarget.ELEMENTS, "DELETE");

					if (doTaskInstanceDeleteAndCommit(con, info)) {
						deleted.add(new AuditTaskObj(info.taskId));
					} else {
						throw new WTNotFoundException("Task not found [{}]", entry.getKey());
					}
				}
				
				if (isAuditEnabled()) {
					writeAuditLog(AuditContext.TASK, AuditAction.DELETE, deleted);
				}
			}
			
		} catch(SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	@Override
	public void moveTaskInstance(final boolean copy, final TaskInstanceId instanceId, final int targetCategoryId) throws WTException {
		moveTaskInstance(copy, Arrays.asList(instanceId), targetCategoryId);
	}
	
	@Override
	public void moveTaskInstance(final boolean copy, final Collection<TaskInstanceId> instanceIds, final int targetCategoryId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			checkRightsOnCategory(targetCategoryId, CheckRightsTarget.ELEMENTS, "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			// Collect necessary data
			InstancesDataResult idr = collectInstancesData(con, instanceIds);
			Map<String, Integer> catMap = tasDao.selectCategoriesByIds(con, idr.involvedTaskIds);
			
			// Perform delete operation
			Set<Integer> readOkCache = new HashSet<>();
			Set<Integer> deleteOkCache = new HashSet<>();
			ArrayList<AuditReferenceDataEntry> copied = new ArrayList<>();
			ArrayList<AuditReferenceDataEntry> moved = new ArrayList<>();
			for (Map.Entry<TaskInstanceId, InstanceInfo> entry : idr.infoMap.entrySet()) {
				InstanceInfo info = entry.getValue();
				String taskId = info.realTaskId();
				if (taskId == null) continue;
				if (!catMap.containsKey(taskId)) throw new WTNotFoundException("Task not found [{}]", entry.getKey());
				int categoryId = catMap.get(taskId);
				checkRightsOnCategory(readOkCache, categoryId, CheckRightsTarget.FOLDER, "READ");
				
				if (copy || (targetCategoryId != categoryId)) {
					if (copy) {
						BitFlag<TaskProcessOpts> options = BitFlag.of(TaskProcessOpts.RECUR, TaskProcessOpts.RECUR_EX, TaskGetOptions.ATTACHMENTS, TaskGetOptions.TAGS, TaskGetOptions.CUSTOM_VALUES);
						Task origTask = doTaskGet(con, taskId, options);
						if (origTask == null) throw new WTNotFoundException("Task not found [{}]", taskId);
						
						TaskInsertResult result = doTaskInstanceCopy(con, info, origTask, targetCategoryId);
						copied.add(new AuditTaskCopyObj(result.otask.getTaskId(), origTask.getTaskId()));
						
					} else {
						if (info.hasParent) throw new WTException("Child task cannot be moved alone. Move its parent instead.");
						checkRightsOnCategory(deleteOkCache, categoryId, CheckRightsTarget.ELEMENTS, "DELETE");
						
						List<String> taskIds = doTaskInstanceMove(con, info, targetCategoryId);
						for (String id : taskIds) {
							moved.add(new AuditTaskMoveObj(id, categoryId));
						}
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
	
	@Override
	public void updateTaskInstanceTags(final UpdateTagsOperation operation, final Collection<TaskInstanceId> instanceIds, final Set<String> tagIds) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		try {
			List<String> okTagIds = null;
			if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
				Set<String> validTags = coreMgr.listTagIds();
				okTagIds = tagIds.stream()
					.filter(tagId -> validTags.contains(tagId))
					.collect(Collectors.toList());
			}
			
			if (instanceIds.size() == 1) {
				TaskInstanceId instanceId = instanceIds.iterator().next();
				con = WT.getConnection(SERVICE_ID, false);
				InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
				Integer catId = tasDao.selectCategoryId(con, info.realTaskId());
				if (catId == null) throw new WTNotFoundException("Task category not found [{}]", instanceId);

				checkRightsOnCategory(catId, CheckRightsTarget.ELEMENTS, "UPDATE");
				
				Task origTask = doTaskGet(con, info.realTaskId(), BitFlag.of(TaskProcessOpts.TAGS));
				if (origTask == null) throw new WTException("Task not found [{}]", info.realTaskId());

				if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
					if (UpdateTagsOperation.RESET.equals(operation)) origTask.getTags().clear();
					origTask.getTags().addAll(okTagIds);
				} else if (UpdateTagsOperation.UNSET.equals(operation)) {
					origTask.getTags().removeAll(tagIds);
				}
				
				try {
					this.doTaskInstanceUpdateAndCommit(con, info, origTask, BitFlag.of(TaskProcessOpts.TAGS));
				} catch (IOException ex1) {/* Due configuration here this will never happen! */}
				
			} else {
				Set<Integer> okCategoryIds = listAllCategoryIds().stream()
					.filter(categoryId -> quietlyCheckRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "UPDATE"))
					.collect(Collectors.toSet());
				con = WT.getConnection(SERVICE_ID, false);
				
				// Collect necessary data
				InstancesDataResult idr = collectInstancesData(con, instanceIds);
				Map<String, Integer> catMap = tasDao.selectCategoriesByIds(con, idr.involvedTaskIds);

				for (Map.Entry<TaskInstanceId, InstanceInfo> entry : idr.infoMap.entrySet()) {
					InstanceInfo info = entry.getValue();
					String taskId = info.realTaskId();
					if (taskId == null) {
						logger.debug("Task ID is null");
						continue;
					}
					if (!catMap.containsKey(taskId)) {
						logger.warn("Task not found [{}]", entry.getKey());
						continue;
					}
					int categoryId = catMap.get(taskId);
					if (!okCategoryIds.contains(categoryId)) {
						logger.warn("Operation not allowed on folder [{}]", categoryId);
						continue;
					}

					Task origTask = doTaskGet(con, taskId, BitFlag.of(TaskProcessOpts.TAGS));
					if (origTask == null) {
						logger.warn("Task not found [{}]", taskId);
						continue;
					}
					
					if (UpdateTagsOperation.SET.equals(operation) || UpdateTagsOperation.RESET.equals(operation)) {
						if (UpdateTagsOperation.RESET.equals(operation)) origTask.getTags().clear();
						origTask.getTags().addAll(okTagIds);
					} else if (UpdateTagsOperation.UNSET.equals(operation)) {
						origTask.getTags().removeAll(tagIds);
					}
					
					try {
						this.doTaskInstanceUpdateAndCommit(con, info, origTask, BitFlag.of(TaskProcessOpts.TAGS));
					} catch (IOException ex1) {/* Due configuration here this will never happen! */}
				}
			}
			
		} catch (SQLException | DAOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	/*
	@Deprecated
	@Override
	public Task getTask(String taskId) throws WTException {
		return getTask(taskId, BitFlag.of(TaskGetOptions.ATTACHMENTS, TaskGetOptions.TAGS, TaskGetOptions.CUSTOM_VALUES));
	}
	
	@Deprecated
	@Override
	public Task getTask(String taskId, BitFlag<TaskGetOptions> options) throws WTException {
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Task task = doTaskGet(con, taskId, TaskProcessOpts.parseTaskGetOptions(options));
			if (task == null) return null;
			checkRightsOnCategory(task.getCategoryId(), CheckRightsTarget.FOLDER, "READ");
			
		} catch(SQLException | DAOException ex) {
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
		return null;
	}
	
	@Deprecated
	@Override
	public TaskAttachmentWithBytes getTaskAttachment(String taskId, String attachmentId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		Connection con = null;
		
		try {
			con = WT.getConnection(SERVICE_ID);
			Integer catId = tasDao.selectCategoryId(con, taskId);
			if (catId == null) return null;
			checkRightsOnCategory(catId, CheckRightsTarget.FOLDER, "READ");
			
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
	
	@Deprecated
	@Override
	public Map<String, CustomFieldValue> getTaskCustomValues(String taskId) throws WTException {
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
	*/
	
	
	
	@Override
	public Task addTask(final TaskEx task) throws WTException {
		return addTask(task, null);
	}
	
	private String doGetOrCreateBrokenInstance(Connection con, TaskInstanceId instanceId, int categoryId) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
		
		if (info.belongsToSeries && info.taskId == null) {
			checkRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "UPDATE");
			
			Task origTask = doTaskGet(con, info.realTaskId(), BitFlag.none());
			if (origTask == null) throw new WTException("Task not found [{}]", info.realTaskId());
			
			TaskInsertResult result = null;
			try {
				result = doTaskInstanceUpdateAndCommit(con, info, origTask, BitFlag.none());
			} catch (IOException ex1) {/* Due configuration here this will never happen! */}
			
			return result.otask.getTaskId();
			
		} else if (info.belongsToSeries && info.isBroken) {
			return info.taskId;
			
		} else {
			return info.taskId;
		}
	}
	
	private Task addTask(TaskEx task, String rawICalendar) throws WTException {
		CoreManager coreMgr = getCoreManager();
		Connection con = null;
		
		try {
			checkRightsOnCategory(task.getCategoryId(), CheckRightsTarget.ELEMENTS, "CREATE");
			con = WT.getConnection(SERVICE_ID, false);
			
			if (task.getParentInstanceId() != null) {
				String newParentTaskId = doGetOrCreateBrokenInstance(con, task.getParentInstanceId(), task.getCategoryId());
				task.setParentInstanceId(ManagerUtils.toParentInstanceId(newParentTaskId));
			}
			
			BitFlag<TaskProcessOpts> processOpts = BitFlag.of(TaskProcessOpts.RECUR, TaskProcessOpts.RECUR_EX, TaskProcessOpts.ATTACHMENTS, TaskProcessOpts.TAGS, TaskProcessOpts.CUSTOM_VALUES, TaskProcessOpts.CONTACT_REF, TaskProcessOpts.DOCUMENT_REF);
			TaskInsertResult result = doTaskInsert(con, task, null, null, rawICalendar, processOpts, BitFlag.none());
			String newTaskId = result.otask.getTaskId();
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.CREATE, newTaskId, null);
			}
			storeAsSuggestion(coreMgr, SUGGESTION_TASK_SUBJECT, result.otask.getSubject());
			
			return doTaskGet(con, newTaskId, BitFlag.of(TaskProcessOpts.ATTACHMENTS, TaskProcessOpts.TAGS, TaskProcessOpts.CUSTOM_VALUES));
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void addTaskObject(final int categoryId, final String href, final net.fortuna.ical4j.model.Calendar iCalendar) throws WTException {
		CoreManager coreMgr = getCoreManager();
		UserProfile.Data udata = WT.getUserData(getTargetProfileId());
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "CREATE");
			
			ICalendarInput in = new ICalendarInput(udata.getTimeZone());
			List<TaskInput> tis = in.parseCalendar(iCalendar);
			if (tis.isEmpty()) throw new WTException("iCalendar object does not contain any VTODO");
			if (tis.size() > 1) throw new WTException("iCalendar object should contain one VTODO");
			TaskInput input = tis.get(0);
			input.task.setCategoryId(categoryId);
			input.task.setHref(href);
			
			con = WT.getConnection(SERVICE_ID, false);
			
			Map<String, List<String>> tagIdsByNameMap = coreMgr.listTagIdsByName();
			TaskInsertResult result = doTaskInputInsert(con, tagIdsByNameMap, new HashMap<>(), input, BitFlag.of(TaskReminderOpts.IGNORE));
			String newTaskId = result.otask.getTaskId();
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.CREATE, newTaskId, null);
			}
			
		} catch(SQLException | DAOException | IOException | WTException ex) {
			DbUtils.rollbackQuietly(con);
			throw wrapException(ex);
		} finally {
			DbUtils.closeQuietly(con);
		}
	}
	
	public void importTasks(final int categoryId, final TasksStreamReader reader, final InputStream is, final ImportMode mode, LogHandler logHandler) throws WTException {
		CoreManager coreMgr = WT.getCoreManager(getTargetProfileId());
		HashMap<String, String> publicIdToMap = new HashMap<>();
		Connection con = null;
		
		try {
			checkRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "CREATE");
			if (ImportMode.COPY.equals(mode)) checkRightsOnCategory(categoryId, CheckRightsTarget.ELEMENTS, "DELETE");
			
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "Started at {}", new DateTime()));
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "Reading source file..."));
			
			List<TaskInput> items = null;
			try {
				items = reader.read(is);
			} catch (IOException ex) {
				logHandler.handle(new LogMessage(0, LogMessage.Level.ERROR, "Unable to read. Reason: {}", ex.getMessage()));
				throw new WTException(ex);
			}
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "{} event/s found!", items.size()));
			
			Map<String, List<String>> tagIdsByNameMap = coreMgr.listTagIdsByName();
			con = WT.getConnection(SERVICE_ID, false);
			
			if (ImportMode.COPY.equals(mode)) {
				logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "Cleaning previous events..."));
				int del = doTaskDeleteByCategory(con, categoryId, false);
				//TODO: audit delete * operation
				logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "{} event/s deleted!", del));
				DbUtils.commitQuietly(con);
			}
			
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "Importing..."));
			BitFlag<TaskReminderOpts> reminderOpts = BitFlag.of(TaskReminderOpts.DISARM_PAST);
			int count = 0;
			for (TaskInput item : items) {
				item.task.setCategoryId(categoryId);
				try {
					doTaskInputInsert(con, tagIdsByNameMap, publicIdToMap, item, reminderOpts);
					DbUtils.commitQuietly(con);
					count++;
					
				} catch (Throwable t1) {
					logger.trace("Error inserting task", t1);
					DbUtils.rollbackQuietly(con);
					logHandler.handle(new LogMessage(0, LogMessage.Level.ERROR, "Unable to import [{}, {}]. Reason: {}", item.task.getSubject(), item.task.getPublicUid(), LangUtils.getDeepestCauseMessage(t1)));
				}
			}
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "{} task/s imported!", count));
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			throw ExceptionUtils.wrapThrowable(t);
		} finally {
			DbUtils.closeQuietly(con);
			logHandler.handle(new LogMessage(0, LogMessage.Level.INFO, "Ended at {}", new DateTime()));
		}
	}
	
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
		TaskDAO tasDao = TaskDAO.getInstance();
		Connection con = null;
		
		ensureSysAdmin();
		final boolean shouldLog = logger.isDebugEnabled();
		try {
			con = WT.getConnection(SERVICE_ID, false);
			
			final DateTime from = now.withTimeAtStartOfDay();
			final DateTime to = from.plusDays(7*2+1);
			if (shouldLog) logger.debug("[reminders] Retrieving expiring instances... [{} -> {}]", from, to);
			
			int noOfRecurringInst = 7*2+1;
			ArrayList<TaskAlertLookupInstance> instances = new ArrayList<>();
			for (VTaskLookup vtas : tasDao.viewOnlineExpiredByRangeForUpdate(con, from, to)) {
				if (!vtas.getHasRecurrence()) {
					TaskAlertLookupInstance item = ManagerUtils.fillTaskAlertLookup(new TaskAlertLookupInstance(), vtas);
					item.setId(TaskInstanceId.build(vtas.getTaskId(), vtas.getSeriesTaskId(), vtas.getSeriesInstanceId()));
					item.setTaskId(vtas.getTaskId());
					instances.add(item);
				} else {
					instances.addAll(calculateRecurringInstances(con, new TaskAlertLookupInstanceMapper(vtas), from, to, noOfRecurringInst));
				}
			}
			
			HashMap<UserProfileId, Boolean> byEmailCache = new HashMap<>();
			
			int i = 0;
			if (shouldLog) logger.debug("[reminders] Found {} candidates", instances.size());
			for (TaskAlertLookupInstance instance : instances) {
				i++;
				try {
					if (shouldLog) logger.debug("[reminders][{}] Working on instance [{}]", i, instance.getId().toString());
					
					UserProfile.Data ud = WT.getUserData(instance.getCategoryProfileId());
					if (ud == null) throw new WTException("UserData is null [{}]", instance.getCategoryProfileId());
					final DateTime profileNow = now.withZone(ud.getTimeZone());
					final DateTime remindOn = instance.getStart().withZone(ud.getTimeZone()).minusMinutes(instance.getReminder());
					if (profileNow.compareTo(remindOn) < 0) {
						if (shouldLog) logger.debug("[reminders][{}] Skipped: remind instant '{}' not yet reached [{}]", i, remindOn, instance.getId().toString());
						continue; // Skip if now is not after remindOn
					}
					
					if (instance.getRemindedOn() != null) { // Instance could have been reminded in the past (only for series)
						final DateTime lastRemindedOn = instance.getRemindedOn().withZone(ud.getTimeZone());
						if (remindOn.compareTo(lastRemindedOn) <= 0) {
							if (shouldLog) logger.debug("[reminders][{}] Skipped: remind instant '{}' is in past [{}]", i, remindOn, lastRemindedOn, instance.getId().toString());
							continue; // Skip if remindOn is not after last remindOn
						}
					}
					
					if (!byEmailCache.containsKey(instance.getCategoryProfileId())) {
						TasksUserSettings us = new TasksUserSettings(SERVICE_ID, instance.getCategoryProfileId());
						boolean bool = us.getTaskReminderDelivery().equals(TasksSettings.TASK_REMINDER_DELIVERY_EMAIL);
						byEmailCache.put(instance.getCategoryProfileId(), bool);
					}
					
					if (shouldLog) logger.debug("[reminders][{}] Building alert [{}]", i, instance.getId().toString());
					BaseReminder alert = null;
					if (byEmailCache.get(instance.getCategoryProfileId())) {
						alert = createTaskReminderAlertEmail(ud.toProfileI18n(), instance.getId().toString(), instance, ud.getPersonalEmailAddress());
					} else {
						alert = createTaskReminderAlertWeb(ud.toProfileI18n(), instance.getId().toString(), instance, remindOn);
					}
					
					if (shouldLog) logger.debug("[reminders][{}] Updating task record [{}]", i, instance.getId().toString());
					int ret = tasDao.updateRemindedOn(con, instance.getTaskId(), now);
					if (ret != 1) continue;
					
					alerts.add(alert);
					if (shouldLog) logger.debug("[reminders][{}] Alert queued [{}]", i, instance.getId().toString());
					
				} catch (Throwable t1) {
					logger.warn("[reminders][{}] Unable to prepare alert [{}]", i, instance.getId().toString(), t1);
				}
			}
			DbUtils.commitQuietly(con);
			
		} catch (Throwable t) {
			DbUtils.rollbackQuietly(con);
			logger.error("[reminders] Error collecting alerts", t);
			alerts.clear();
		} finally {
			DbUtils.closeQuietly(con);
		}
		return alerts;
	}
	
	
	
	
	private boolean needsTreatAsPrivate(UserProfileId runningProfile, boolean taskIsPrivate, int taskCategoryId) {
		if (!taskIsPrivate) return false;
		UserProfileId ownerProfile = ownerCache.get(taskCategoryId);
		return treatTaskAsPrivate(runningProfile, ownerProfile, taskIsPrivate);
	}
	
	private boolean treatTaskAsPrivate(UserProfileId runningProfile, UserProfileId taskOwner, boolean taskIsPrivate) {
		if (!taskIsPrivate) return false;
		if (RunContext.isWebTopAdmin(runningProfile)) return false;
		return !taskOwner.equals(runningProfile);
	}
	
	private <T> List<T> calculateRecurringInstances(Connection con, RecurringInstanceMapper<T> instanceMapper, DateTime rangeFrom, DateTime rangeTo, int limit) throws WTException {
		TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
		ArrayList<T> instances = new ArrayList<>();
		
		DateTimeZone timezone = instanceMapper.getTimezone();
		String taskId = instanceMapper.getTaskId();
		DateTime taskStart = instanceMapper.getTaskStart();
		DateTime taskDue = instanceMapper.getTaskDue();
		
		if (taskStart == null) {
			logger.warn("Task has no valid start [{}]", taskId);
		} else {
			Integer dueDays = (taskDue != null) ? Math.abs(Days.daysBetween(taskStart.toLocalDate(), taskDue.toLocalDate()).getDays()) : null;
			
			try {
				OTaskRecurrence orec = recDao.selectRecurrenceByTask(con, taskId);
				if (orec == null) {
					logger.warn("Unable to retrieve recurrence for task [{}]", taskId);
				} else {
					if (rangeFrom == null) rangeFrom = orec.getStart();
					if (rangeTo == null) rangeTo = orec.getStart().plusYears(1);

					Recur recur = orec.getRecur();
					if (recur == null) throw new WTException("Unable to parse rrule [{}]", orec.getRule());

					Set<LocalDate> exclDates = recDao.selectRecurrenceExByTask(con, taskId);
					List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(recur, orec.getStart(), exclDates, false, taskStart, taskStart.plusDays(1), timezone, rangeFrom, rangeTo, limit);
					for (LocalDate date : dates) {
						DateTime start = instanceDateToDateTime(date, taskStart, timezone);
						DateTime due = (taskDue != null) ? instanceDateToDateTime(date.plusDays(dueDays), taskDue, timezone) : null;
						//DateTime start = date.toDateTime(taskStartTime, timezone);
						//DateTime due = (taskDueTime != null) ? date.plusDays(dueDays).toDateTime(taskDueTime, timezone) : null;
						TaskInstanceId id = TaskInstanceId.build(taskId, start);
						//DateTime start = date.toDateTime(taskStartTime, timezone).withZone(userTimezone);
						//TaskInstanceId id = TaskInstanceId.build(taskId, date);

						instances.add(instanceMapper.createInstance(id, start, due));
					}
				}
			} catch (Throwable t) {
				throw ExceptionUtils.wrapThrowable(t);
			}
		}
		return instances;
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
	
	private TaskObject doTaskObjectPrepare(Connection con, VTaskObject vtask, TaskObjectOutputType outputType, Map<String, String> tagNamesByIdMap) throws WTException {
		if (TaskObjectOutputType.STAT.equals(outputType)) {
			return ManagerUtils.fillTaskObject(new TaskObject(), vtask);
			
		} else {
			TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
			
			TaskEx task = ManagerUtils.fillTask(new TaskEx(), vtask);
			if (vtask.getHasRecurrence()) {
				OTaskRecurrence orec = recDao.selectRecurrenceByTask(con, vtask.getTaskId());
				if (orec != null) {
					TaskRecurrence rec = new TaskRecurrence(orec.getRule(), orec.getStart());
					rec.setExcludedDates(recDao.selectRecurrenceExByTask(con, vtask.getTaskId()));
					task.setRecurrence(rec);
				}
			}
			if (!StringUtils.isBlank(vtask.getTags())) {
				task.setTags(new LinkedHashSet(new CId(vtask.getTags()).getTokens()));
			}
			
			//TODO: add support to Assignee, Attachments and CustomValues. See doTaskGet...
			
			if (TaskObjectOutputType.ICALENDAR.equals(outputType)) {
				TaskICalendarsDAO icalDao = TaskICalendarsDAO.getInstance();
				
				net.fortuna.ical4j.model.PropertyList extraProps = null;
				if (vtask.getHasIcalendar()) {
					String rawICalendar = icalDao.selectRawDataById(con, vtask.getTaskId());
					if (rawICalendar != null) {
						try {
							extraProps = ICalendarUtils.extractProperties(ICalendarUtils.parse(rawICalendar), net.fortuna.ical4j.model.component.VToDo.class);
						} catch (IOException | ParserException ex) {
							logger.debug("ICalendarUtils.extractProperties", ex);
						}
					}
				}
				TaskObjectWithICalendar ret = ManagerUtils.fillTaskObject(new TaskObjectWithICalendar(), vtask);
				ICalendarOutput out = new ICalendarOutput(ICalendarUtils.buildProdId(ManagerUtils.getProductName()))
						.withTagNamesByIdMap(tagNamesByIdMap);
				try {
					//TODO: maybe add support to relatedTo (last null param here below)
					ret.setIcalendar(out.printICalendar(task, extraProps, null));
				} catch (IOException ex) {
					logger.debug("printICalendar", ex);
				}
				return ret;
				
			} else {
				TaskObjectWithBean ret = ManagerUtils.fillTaskObject(new TaskObjectWithBean(), vtask);
				ret.setTask(task);
				return ret;
			}
		}
	}
	
	private static class InstancesDataResult {
		public final Map<TaskInstanceId, InstanceInfo> infoMap;
		public final Set<String> involvedTaskIds;
		
		public InstancesDataResult(Map<TaskInstanceId, InstanceInfo> infoMap, Set<String> involvedTaskIds) {
			this.infoMap = infoMap;
			this.involvedTaskIds = involvedTaskIds;
		}
	}
	
	private InstancesDataResult collectInstancesData(Connection con, Collection<TaskInstanceId> instanceIds) {
		TaskDAO tasDao = TaskDAO.getInstance();
		
		LinkedHashMap<TaskInstanceId, InstanceInfo> infoMap = new LinkedHashMap<>(instanceIds.size());
		LinkedHashSet<String> involvedTaskIds = new LinkedHashSet<>();
		for (TaskInstanceId instanceId : instanceIds) {
			InstanceInfo info = new InstanceInfo(instanceId, tasDao.selectInstanceInfo(con, instanceId));
			infoMap.put(instanceId, info);
			// Fill involvedTaskIds collection only if we have a valid task ID!
			String taskId = info.realTaskId();
			if (taskId != null) involvedTaskIds.add(taskId);
		}
		return new InstancesDataResult(infoMap, involvedTaskIds);
	}
	
	private static class InstanceInfo {
		
		/**
		 * Reports if the instance has a parent task.
		 */
		public final boolean hasParent;
		
		/**
		 * Reports if the instance has a parent task.
		 */
		//public final boolean hasChildren;
		
		/**
		 * Reports if the instance is attributable to a series, whether it is 
		 * a master instance (series generator) or other instances (series templates or broken).
		 */
		public final boolean belongsToSeries;
		
		/**
		 * Reports if the instance is a broken element of the series.
		 * After being edited, a series instance template become a broken instance.
		 */
		public final boolean isBroken;
		
		/**
		 * Reports the underlying task ID of the instance: valued for master, broken and single
		 * instances, null for series templates. This will be null too for unexistent instances.
		 */
		public final String taskId;
		
		/**
		 * Reports the timezone of the instance: taken from the master element in 
		 * case of instance template (or master element itself), or from the task 
		 * in case of broken or single instances
		 */
		public final String taskTimezone;
		
		/**
		 * Reports the taskId of the master series, only if this instance clearly
		 * belongs to a series (for brokens too). This will be null for single instances.
		 */
		public final String masterTaskId;
		
		/**
		 * Reports the instance key: not null for instances different from 00000000
		 */
		public final String seriesInstance;
		
		/**
		 * Reports the instance key as date: not null for instances different from 00000000
		 */
		public final LocalDate seriesInstanceDate;
		
		public InstanceInfo(TaskInstanceId instanceId, OTaskInstanceInfo otii) {
			// TABLE tasks
			// | taskId                           | series_task_id                   | series_instance
			//   6e5070066a5d11eb9c2d0d2374d38e09   (Null)                             (Null)
			//   d62a21876a5d11eb9c2d4d84360a8aaa   (Null)                             (Null)
			//   89fa4e3a899f11eb86b139828f248cb5   d62a21876a5d11eb9c2d4d84360a8aaa   20210402
			//
			// TABLE tasks_recurrences
			// | taskId                           | start_date              | until_date              | rule
			//   d62a21876a5d11eb9c2d4d84360a8aaa   2021-04-01 15:00:00+02    2021-04-05 15:00:00+02    RRULE:FREQ=DAILY;COUNT=5
			
			// Thesea are query results for above tables:
			
			// 6e5070066a5d11eb9c2d0d2374d38e09.20210401 (series instance not existing)
			// belongsToSeries (existsRecurrenceByTask): false
			// taskIdByInstance (selectIdBySeriesInstance): null
			
			// 6e5070066a5d11eb9c2d0d2374d38e09.00000000 (single instance)
			// belongsToSeries (existsRecurrenceByTask): false
			// taskIdByInstance (selectIdBySeriesInstance): null
			
			// d62a21876a5d11eb9c2d4d84360a8aaa.00000000 (series master)
			// belongsToSeries (existsRecurrenceByTask): true
			// taskIdByInstance (selectIdBySeriesInstance): null
			
			// d62a21876a5d11eb9c2d4d84360a8aaa.20210401 (series instance)
			// belongsToSeries (existsRecurrenceByTask): true
			// taskIdByInstance (selectIdBySeriesInstance): null
			
			// d62a21876a5d11eb9c2d4d84360a8aaa.20210402 (series broken instance)
			// belongsToSeries (existsRecurrenceByTask): true
			// taskIdByInstance (selectIdBySeriesInstance): 89fa4e3a899f11eb86b139828f248cb5
			
			if (instanceId.hasNoInstance()) {
				this.hasParent = otii.getHasParent();
				this.belongsToSeries = otii.getHasRecurrence();
				this.masterTaskId = belongsToSeries ? instanceId.getTaskId() : null;
				this.taskId = instanceId.getTaskId();
				this.taskTimezone = otii.getTimezone();
				this.isBroken = false;
				this.seriesInstance = null;
				this.seriesInstanceDate = null;
				
			} else {
				this.hasParent = otii.getHasParent();
				this.belongsToSeries = otii.getHasRecurrence();
				this.masterTaskId = belongsToSeries ? instanceId.getTaskId() : null;
				this.taskId = otii.getTaskIdByInstance();
				this.taskTimezone = otii.getTimezone();
				this.isBroken = (taskId != null);
				this.seriesInstance = instanceId.getInstance();
				this.seriesInstanceDate = instanceId.getInstanceAsDate();
			}
		}
		
		public String realTaskId() {
			return LangUtils.coalesceStrings(taskId, masterTaskId);
		}
		
		public DateTimeZone taskTimezoneAsObject() {
			return DateTimeUtils.parseDateTimeZone(this.taskTimezone);
		}
	}
	
	private enum TaskProcessOpts implements BitFlagEnum {
		RECUR(1), RECUR_EX(2), ASSIGNEES(4), ATTACHMENTS(8), TAGS(16), CUSTOM_VALUES(32), CONTACT_REF(64), DOCUMENT_REF(128);
		
		private int value = 0;
		private TaskProcessOpts(int value) { this.value = value; }
		@Override
		public int value() { return this.value; }
		
		public static BitFlag<TaskProcessOpts> parseTaskGetOptions(BitFlag<TaskGetOptions> flags) {
			BitFlag<TaskProcessOpts> ret = new BitFlag<>();
			
			if (flags.has(TaskGetOptions.ATTACHMENTS)) ret.set(TaskProcessOpts.ATTACHMENTS);
			if (flags.has(TaskGetOptions.TAGS)) ret.set(TaskProcessOpts.TAGS);
			if (flags.has(TaskGetOptions.CUSTOM_VALUES)) ret.set(TaskProcessOpts.CUSTOM_VALUES);
			return ret;
		}
		
		public static BitFlag<TaskProcessOpts> parseTaskUpdateOptions(BitFlag<TaskUpdateOptions> flags) {
			BitFlag<TaskProcessOpts> ret = new BitFlag<>();
			if (flags.has(TaskUpdateOptions.ASSIGNEES)) ret.set(TaskProcessOpts.ASSIGNEES);
			if (flags.has(TaskUpdateOptions.ATTACHMENTS)) ret.set(TaskProcessOpts.ATTACHMENTS);
			if (flags.has(TaskUpdateOptions.TAGS)) ret.set(TaskProcessOpts.TAGS);
			if (flags.has(TaskUpdateOptions.CUSTOM_VALUES)) ret.set(TaskProcessOpts.CUSTOM_VALUES);
			if (flags.has(TaskUpdateOptions.CONTACT_REF)) ret.set(TaskProcessOpts.CONTACT_REF);
			if (flags.has(TaskUpdateOptions.DOCUMENT_REF)) ret.set(TaskProcessOpts.DOCUMENT_REF);
			return ret;
		}
	}
	
	private enum TaskReminderOpts implements BitFlagEnum {
		IGNORE(1), DISARM_PAST(2);
		
		private int value = 0;
		private TaskReminderOpts(int value) { this.value = value; }
		@Override
		public int value() { return this.value; }
	}
	
	private String doTaskGetInstanceTaskId(Connection con, TaskInstanceId instanceId) throws DAOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		return tasDao.selectIdBySeriesInstance(con, instanceId.getTaskId(), instanceId.getInstance());
	}
	
	private Task doTaskGet(Connection con, String taskId, BitFlag<TaskProcessOpts> processOptions) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
		TaskAssigneeDAO assDao = TaskAssigneeDAO.getInstance();
		TaskTagDAO tagDao = TaskTagDAO.getInstance();
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		
		OTask otask = tasDao.selectOnlineById(con, taskId);
		if (otask == null) return null;
		OChildrenCount childrenCount = tasDao.selectCountByParent(con, taskId);
		Task task = ManagerUtils.fillTask(new Task(childrenCount.getTotalCount(), childrenCount.getCompletedCount()), otask);
		task.setParentInstanceId(ManagerUtils.toParentInstanceId(otask.getParentTaskId()));
		
		List<OTaskAssignee> oassignees = assDao.selectByTask(con, taskId);
		task.setAssignees(ManagerUtils.createTaskAssigneeList(oassignees));
		
		if (processOptions.has(TaskProcessOpts.RECUR)) {
			OTaskRecurrence orec = recDao.selectRecurrenceByTask(con, taskId);
			if (orec != null) {
				TaskRecurrence rec = new TaskRecurrence(orec.getRule(), orec.getStart());
				//TaskRecurrence rec = new TaskRecurrence(orec.getRule(), orec.getLocalStartDate(otask.getDateTimezone()));
				if (processOptions.has(TaskProcessOpts.RECUR_EX)) {
					rec.setExcludedDates(recDao.selectRecurrenceExByTask(con, taskId));
				}
				task.setRecurrence(rec);
			}
		}
		if (processOptions.has(TaskProcessOpts.TAGS)) {
			task.setTags(tagDao.selectTagsByTask(con, taskId));
		}
		if (processOptions.has(TaskProcessOpts.ATTACHMENTS)) {
			List<OTaskAttachment> oatts = attDao.selectByTask(con, taskId);
			task.setAttachments(ManagerUtils.createTaskAttachmentList(oatts));
		}
		if (processOptions.has(TaskProcessOpts.CUSTOM_VALUES)) {
			List<OTaskCustomValue> ovals = cvalDao.selectByTask(con, taskId);
			task.setCustomValues(ManagerUtils.createCustomValuesMap(ovals));
		}
		return task;
	}
	
	private TaskInsertResult doTaskInputInsert(Connection con, Map<String, List<String>> tagIdsByNameMap, Map<String, String> taskIdByPublicIdMap, TaskInput input, BitFlag<TaskReminderOpts> reminderOpts) throws IOException {
		TaskEx task = new TaskEx(input.task);
		if (input.taskRecurrence != null) {
			task.setRecurrence(input.taskRecurrence);
		}
		if (input.tagNames != null) {
			Set<String> tagIds = new HashSet<>();
			for (String tagName : input.tagNames) {
				if (tagIdsByNameMap.containsKey(tagName)) {
					tagIds.addAll(tagIdsByNameMap.get(tagName));
				}
			}
			task.setTags(tagIds);
		}
		if (input.relatedToUid != null) {
			//TODO: find task with publicId = relatedToUid
		}
		String rawICalendar = null;
		if (input.extraProps != null) {
			rawICalendar = ICalendarUtils.printProperties(input.extraProps, "VTODO");
		}
		
		BitFlag<TaskProcessOpts> processOpts = BitFlag.of(TaskProcessOpts.RECUR).set(TaskProcessOpts.RECUR_EX);
		TaskInsertResult ret = doTaskInsert(con, task, null, null, rawICalendar, processOpts, reminderOpts);
		
		if (input.taskRecurrence != null) {
			if (ret.otask.getPublicUid() != null) {
				taskIdByPublicIdMap.put(ret.otask.getPublicUid(), ret.otask.getTaskId());
			} else {
				//TODO: warn no publicid
			}
		} else {
			if (input.recurringRefs != null && taskIdByPublicIdMap.containsKey(input.recurringRefs.exRefersToMasterUid)) {
				TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
				String taskId = taskIdByPublicIdMap.get(input.recurringRefs.exRefersToMasterUid);
				recDao.insertRecurrenceEx(con, taskId, input.recurringRefs.exRefersToDate);
			}
		}
		
		return ret;
	}
	
	private TaskInsertResult doTaskInsert(Connection con, TaskEx task, String seriesTaskId, String seriesInstance, String rawICalendar, BitFlag<TaskProcessOpts> processOpts, BitFlag<TaskReminderOpts> reminderOpts) throws DAOException, IOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
		TaskAssigneeDAO assDao = TaskAssigneeDAO.getInstance();
		TaskTagDAO tagDao = TaskTagDAO.getInstance();
		TaskICalendarsDAO icalDao = TaskICalendarsDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		DateTime revisionTimestamp = BaseDAO.createRevisionTimestamp();
		
		String newTaskId = IdentifierUtils.getUUIDTimeBased(true);
		OTask otask = ManagerUtils.fillOTask(new OTask(), task);
		otask.setTaskId(newTaskId);
		if (task.getParentInstanceId() != null) {
			if (!task.getParentInstanceId().hasNoInstance()) throw new IOException("NOT SUPPORTED");
			otask.setParentTaskId(task.getParentInstanceId().getTaskId());
		} else {
			otask.setParentTaskId(null);
		}
		otask.setSeriesTaskId(seriesTaskId);
		otask.setSeriesInstanceId(seriesInstance);
		ManagerUtils.fillOTaskWithDefaultsForInsert(otask, getTargetProfileId(), revisionTimestamp);
		
		if (otask.getReminder() != null) {
			if (reminderOpts.has(TaskReminderOpts.IGNORE)) {
				otask.setReminder(null);
			} else if (reminderOpts.has(TaskReminderOpts.DISARM_PAST) && otask.getStart().isBeforeNow()) {
				otask.setRemindedOn(revisionTimestamp);
			}
		}
		
		boolean ret = tasDao.insert(con, otask, processOpts.has(TaskProcessOpts.CONTACT_REF), processOpts.has(TaskProcessOpts.DOCUMENT_REF)) == 1;
		if (!ret) return null;
		
		OTaskRecurrence orec = null;
		if (processOpts.has(TaskProcessOpts.RECUR) && task.hasRecurrence()) {
			Recur recur = task.getRecurrence().getRuleRecur();
			
			orec = new OTaskRecurrence();
			orec.setTaskId(newTaskId);
			orec.set(recur, task.getRecurrence().getStart(), task.getStart(), task.getTimezoneObject());
			recDao.insertRecurrence(con, orec);
			
			if (processOpts.has(TaskProcessOpts.RECUR_EX) && (task.getRecurrence().getExcludedDates() != null)) {
				recDao.batchInsertRecurrenceEx(con, newTaskId, task.getRecurrence().getExcludedDates());
			}
		}
		
		if (!StringUtils.isBlank(rawICalendar)) {
			icalDao.insert(con, newTaskId, rawICalendar);
		}
		
		ArrayList<OTaskAssignee> oassignees = null;
		if (processOpts.has(TaskProcessOpts.ASSIGNEES) && task.hasAssignees()) {
			oassignees = new ArrayList<>();
			for (TaskAssignee assignee : task.getAssignees()) {
				if (!ManagerUtils.validateForInsert(assignee)) continue;
				OTaskAssignee oassignee = ManagerUtils.fillOTaskAssignee(new OTaskAssignee(), assignee);
				oassignee.setAssigneeId(IdentifierUtils.getUUIDTimeBased(true));
				oassignee.setTaskId(newTaskId);
				assDao.insert(con, oassignee);
				oassignees.add(oassignee);
			}
		}
		
		Set<String> otags = null;
		if (processOpts.has(TaskProcessOpts.TAGS) && task.hasTags()) {
			otags = new LinkedHashSet<>(task.getTags());
			tagDao.batchInsert(con, newTaskId, task.getTags());
		}
		
		ArrayList<OTaskAttachment> oattchs = null;
		if (processOpts.has(TaskProcessOpts.ATTACHMENTS) && task.hasAttachments()) {
			oattchs = new ArrayList<>();
			for (TaskAttachment att : task.getAttachments()) {
				if (!(att instanceof TaskAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				oattchs.add(doTaskAttachmentInsert(con, newTaskId, (TaskAttachmentWithStream)att));
			}
		}
		
		ArrayList<OTaskCustomValue> ocvals = null;
		if (processOpts.has(TaskProcessOpts.CUSTOM_VALUES) && task.hasCustomValues()) {
			ocvals = new ArrayList<>(task.getCustomValues().size());
			for (CustomFieldValue cfv : task.getCustomValues().values()) {
				OTaskCustomValue ocv = ManagerUtils.fillOTaskCustomValue(new OTaskCustomValue(), cfv);
				ocv.setTaskId(newTaskId);
				ocvals.add(ocv);
			}
			cvalDao.batchInsert(con, ocvals);
		}
		
		return new TaskInsertResult(otask, oassignees, otags, oattchs, ocvals);
	}
	
	private OTask doTaskUpdate(Connection con, String taskId, TaskEx task, BitFlag<TaskProcessOpts> processOpts) throws DAOException, IOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
		TaskAssigneeDAO assDao = TaskAssigneeDAO.getInstance();
		TaskTagDAO tagDao = TaskTagDAO.getInstance();
		TaskAttachmentDAO attcDao = TaskAttachmentDAO.getInstance();
		TaskCustomValueDAO cvalDao = TaskCustomValueDAO.getInstance();
		DateTime revisionTimestamp = BaseDAO.createRevisionTimestamp();
		
		Check.notNull(task.getTimezone(), "task.getTimezone() must not be null!");
		OTask otask = ManagerUtils.fillOTask(new OTask(), task);
		otask.setTaskId(taskId);
		ManagerUtils.fillOTaskWithDefaultsForUpdate(otask, revisionTimestamp);
		
		boolean clearRemindedOn = (task.getStart() != null) ? task.getStart().isAfterNow() : false;
		boolean ret = tasDao.update(con, otask, processOpts.has(TaskProcessOpts.CONTACT_REF), processOpts.has(TaskProcessOpts.DOCUMENT_REF), clearRemindedOn, revisionTimestamp) == 1;
		if (!ret) return null;
		
		if (processOpts.has(TaskProcessOpts.RECUR)) {
			OTaskRecurrence orec = recDao.selectRecurrenceByTask(con, taskId);
			if ((orec != null) && task.hasRecurrence()) { // New task has recurrence and the old too
				Recur recur = task.getRecurrence().getRuleRecur();
				boolean recurChanged = !ICal4jUtils.equals(recur, orec.getRecur());
				
				orec.set(recur, task.getRecurrence().getStart(), task.getStart(), task.getTimezoneObject());
				recDao.updateRecurrence(con, orec);
				
				if (processOpts.has(TaskProcessOpts.RECUR_EX)) {
					// If rule is changed, cleanup stored exceptions (we lose any broken events restore information)
					if (recurChanged) recDao.deleteRecurrenceExByTask(con, taskId);
					
					Set<LocalDate> newDates = (task.getRecurrence().getExcludedDates() != null) ? task.getRecurrence().getExcludedDates() : new LinkedHashSet<>();
					Set<LocalDate> oldDates = recDao.selectRecurrenceExByTask(con, taskId);
					CollectionChangeSet<LocalDate> changeSet = LangUtils.getCollectionChanges(oldDates, newDates);
					recDao.batchInsertRecurrenceEx(con, taskId, changeSet.inserted);
					recDao.deleteRecurrenceExByTaskDates(con, taskId, changeSet.deleted);
				}
				
			} else if ((orec == null) &&  task.hasRecurrence()) { // New task has recurrence but the old doesn't
				Recur recur = task.getRecurrence().getRuleRecur();
				
				orec = new OTaskRecurrence();
				orec.setTaskId(taskId);
				orec.set(recur, task.getRecurrence().getStart(), task.getStart(), task.getTimezoneObject());
				recDao.insertRecurrence(con, orec);

				if (processOpts.has(TaskProcessOpts.RECUR_EX) && (task.getRecurrence().getExcludedDates() != null)) {
					recDao.batchInsertRecurrenceEx(con, taskId, task.getRecurrence().getExcludedDates());
				}
				
			} else if ((orec != null) && !task.hasRecurrence()) { // New task doesn't have recurrence but the old does
				recDao.deleteRecurrenceByTask(con, taskId);
				recDao.deleteRecurrenceExByTask(con, taskId);
			}
		}
		
		if (processOpts.has(TaskProcessOpts.ASSIGNEES) && task.hasAssignees()) {
			List<TaskAssignee> oldAssignees = ManagerUtils.createTaskAssigneeList(assDao.selectByTask(con, taskId));
			CollectionChangeSet<TaskAssignee> changeSet = LangUtils.getCollectionChanges(oldAssignees, task.getAssignees());
			
			for (TaskAssignee assignee : changeSet.inserted) {
				if (!ManagerUtils.validateForInsert(assignee)) continue;
				final OTaskAssignee oassignee = ManagerUtils.fillOTaskAssignee(new OTaskAssignee(), assignee);
				oassignee.setAssigneeId(IdentifierUtils.getUUIDTimeBased(true));
				oassignee.setTaskId(taskId);
				assDao.insert(con, oassignee);
			}
			for (TaskAssignee assignee : changeSet.updated) {
				if (!ManagerUtils.validateForUpdate(assignee)) continue;
				final OTaskAssignee oassignee = ManagerUtils.fillOTaskAssignee(new OTaskAssignee(), assignee);
				assDao.update(con, oassignee);
			}
			assDao.deleteByIdsTask(con, changeSet.deleted.stream().map(att -> att.getAssigneeId()).collect(Collectors.toList()), taskId);
		}
		
		if (processOpts.has(TaskProcessOpts.TAGS) && task.hasTags()) {
			Set<String> oldTags = tagDao.selectTagsByTask(con, taskId);
			CollectionChangeSet<String> changeSet = LangUtils.getCollectionChanges(oldTags, task.getTags());
			tagDao.batchInsert(con, taskId, changeSet.inserted);
			tagDao.deleteByIdTags(con, taskId, changeSet.deleted);
		}
		
		if (processOpts.has(TaskProcessOpts.ATTACHMENTS) && task.hasAttachments()) {
			List<TaskAttachment> oldAttchs = ManagerUtils.createTaskAttachmentList(attcDao.selectByTask(con, taskId));
			CollectionChangeSet<TaskAttachment> changeSet = LangUtils.getCollectionChanges(oldAttchs, task.getAttachments());

			for (TaskAttachment att : changeSet.inserted) {		
				if (!(att instanceof TaskAttachmentWithStream)) throw new IOException("Attachment stream not available [" + att.getAttachmentId() + "]");
				doTaskAttachmentInsert(con, taskId, (TaskAttachmentWithStream)att);
			}
			for (TaskAttachment att : changeSet.updated) {
				if (!(att instanceof TaskAttachmentWithStream)) continue;
				doTaskAttachmentUpdate(con, (TaskAttachmentWithStream)att);
			}
			attcDao.deleteByIdsTask(con, changeSet.deleted.stream().map(att -> att.getAttachmentId()).collect(Collectors.toList()), taskId);
		}
		
		if (processOpts.has(TaskProcessOpts.CUSTOM_VALUES) && task.hasCustomValues()) {
			ArrayList<String> customFieldIds = new ArrayList<>();
			ArrayList<OTaskCustomValue> ocvals = new ArrayList<>(task.getCustomValues().size());
			for (CustomFieldValue cfv : task.getCustomValues().values()) {
				OTaskCustomValue ocv = ManagerUtils.fillOTaskCustomValue(new OTaskCustomValue(), cfv);
				ocv.setTaskId(taskId);
				ocvals.add(ocv);
				customFieldIds.add(ocv.getCustomFieldId());
			}
			cvalDao.deleteByTaskFields(con, taskId, customFieldIds);
			cvalDao.batchInsert(con, ocvals);
		}
		
		return otask;
	}
	
	private Set<String> doTaskDelete(Connection con, String taskId, boolean logicDelete) throws DAOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		if (logicDelete) {
			DateTime revision = BaseDAO.createRevisionTimestamp();
			LinkedHashSet<String> deleted = new LinkedHashSet<>();
			if (tasDao.logicDeleteById(con, taskId, revision) == 1) {
				deleted.add(taskId);
			}
			deleted.addAll(tasDao.logicDeleteByParent(con, taskId, revision));
			return deleted;
		} else {
			LinkedHashSet<String> deleted = new LinkedHashSet<>();
			if (tasDao.deleteById(con, taskId) == 1) {
				deleted.add(taskId);
			}
			// Due to integrity check, when a parent is deleted each child task
			// is updated setting parentTaskId to NULL. This avoids collecting
			// updated childs list in the aftermath.
			return deleted;
		}
	}
	
	private int doTaskDeleteByCategory(Connection con, int categoryId, boolean logicDelete) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		if (logicDelete) {
			return tasDao.logicDeleteByCategoryId(con, categoryId, BaseDAO.createRevisionTimestamp());
		} else {
			return tasDao.deleteByCategoryId(con, categoryId);
		}
	}
	
	private TaskInsertResult doTaskCopy(Connection con, String sourceTaskId, TaskEx task, int targetCategoryId, BitFlag<TaskProcessOpts> processOptions) throws DAOException, IOException {
		TaskICalendarsDAO icalDao = TaskICalendarsDAO.getInstance();
		
		task.setParentInstanceId(null);
		task.setCategoryId(targetCategoryId);
		task.setPublicUid(null); // Reset value in order to make inner function generate new one!
		task.setOrganizer(null); // Reset value in order to make inner function generate new one!
		task.setOrganizerId(null); // Reset value in order to make inner function generate new one!
		task.setHref(null); // Reset value in order to make inner function generate new one!
		task.setEtag(null); // Reset value in order to make inner function generate new one!
		
		String rawICalendar = icalDao.selectRawDataById(con, sourceTaskId);
		//TODO: maybe add support to attachments copy
		
		return doTaskInsert(con, task, null, null, rawICalendar, processOptions, BitFlag.none());
	}
	
	private OTaskAttachment doTaskAttachmentInsert(Connection con, String taskId, TaskAttachmentWithStream attachment) throws DAOException, IOException {
		TaskAttachmentDAO attDao = TaskAttachmentDAO.getInstance();
		
		OTaskAttachment oatt = ManagerUtils.fillOTaskAttachment(new OTaskAttachment(), attachment);
		oatt.setTaskAttachmentId(IdentifierUtils.getUUIDTimeBased(true));
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
		
		OTaskAttachment oatt = ManagerUtils.fillOTaskAttachment(new OTaskAttachment(), attachment);
		attDao.update(con, oatt, BaseDAO.createRevisionTimestamp());
		
		InputStream is = attachment.getStream();
		try {
			attDao.deleteBytesById(con, oatt.getTaskAttachmentId());
			return attDao.insertBytes(con, oatt.getTaskAttachmentId(), IOUtils.toByteArray(is)) == 1;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private TaskInsertResult doTaskInstanceUpdateAndCommit(Connection con, InstanceInfo info, TaskEx task, BitFlag<TaskProcessOpts> processOpts) throws DAOException, IOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
		TaskInsertResult result = null;
		
		task.setTimezone(info.taskTimezone);
		if (info.belongsToSeries && info.isBroken) { // -> BROKEN INSTANCE
			// 1 - Updates the broken item with new data
			OTask updateResult = doTaskUpdate(con, info.taskId, task, processOpts.copy().unset(TaskProcessOpts.RECUR));
			
			DateTime revision = BaseDAO.createRevisionTimestamp();
			Set<String> updatedTaskIds = tasDao.updateCategoryByParent(con, info.taskId, updateResult.getCategoryId(), revision);
			if (EnumUtils.toSerializedName(TaskBase.Status.COMPLETED).equals(updateResult.getStatus())) {
				updatedTaskIds.addAll(tasDao.updateCompletedByParent(con, info.taskId, revision));
			}
			boolean parentProgressUpdated = tasDao.updateParentProgressByChild(con, info.taskId, revision) == 1;
			if (parentProgressUpdated) updatedTaskIds.add(info.taskId);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, info.taskId, null);
				for (String updatedTaskId : updatedTaskIds) {
					writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, updatedTaskId, null);
				}
			}
			
		} else if (info.belongsToSeries && (info.seriesInstanceDate != null)) { // -> SERIES TEMPLATE INSTANCE
			// 1 - Inserts new broken item (rr is not supported here)
			task.setStart(instanceDateToDateTime(info.seriesInstanceDate, task.getStart(), task.getTimezoneObject()));
			BitFlag<TaskProcessOpts> processOpts2 = processOpts.copy().unset(TaskProcessOpts.RECUR, TaskProcessOpts.RECUR_EX, TaskProcessOpts.ATTACHMENTS);
			TaskInsertResult insert = doTaskInsert(con, task, info.masterTaskId, info.seriesInstance, null, processOpts2, BitFlag.none());
			
			Set<String> updatedTaskIds = new LinkedHashSet<>();
			boolean parentProgressUpdated = tasDao.updateParentProgressByChild(con, info.taskId, BaseDAO.createRevisionTimestamp()) == 1;
			if (parentProgressUpdated) updatedTaskIds.add(info.taskId);
			
			// 2 - Inserts an exception on modified date
			recDao.insertRecurrenceEx(con, info.masterTaskId, info.seriesInstanceDate);

			// 3 - Updates revision of master event
			tasDao.updateRevision(con, info.masterTaskId, BaseDAO.createRevisionTimestamp());

			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, info.masterTaskId, null);
				writeAuditLog(AuditContext.TASK, AuditAction.CREATE, insert.otask.getTaskId(), null);
				for (String updatedTaskId : updatedTaskIds) {
					writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, updatedTaskId, null);
				}
			}
			result = insert;
			
		} else { // -> SINGLE INSTANCE or MASTER INSTANCE
			// 1 - Updates this item with new data
			OTask updateResult = doTaskUpdate(con, info.taskId, task, processOpts.copy().unset(TaskProcessOpts.RECUR_EX));
			
			DateTime revision = BaseDAO.createRevisionTimestamp();
			Set<String> updatedTaskIds = tasDao.updateCategoryByParent(con, info.taskId, updateResult.getCategoryId(), revision);
			if (EnumUtils.toSerializedName(TaskBase.Status.COMPLETED).equals(updateResult.getStatus())) {
				updatedTaskIds.addAll(tasDao.updateCompletedByParent(con, info.taskId, revision));
			}
			boolean parentProgressUpdated = tasDao.updateParentProgressByChild(con, info.taskId, revision) == 1;
			if (parentProgressUpdated) updatedTaskIds.add(info.taskId);
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, info.taskId, null);
				for (String updatedTaskId : updatedTaskIds) {
					writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, updatedTaskId, null);
				}
			}
		}
		
		// Notify assignee
		if (true) {
			//TODOOOOOOOOOOOOOOOOOOOOOOO
		}
		return result;
	}
	
	/*
	private List<RecipientTuple> computeAssignationRecipients(UserProfileId ownerProfile, Task task, String crud) {
		ArrayList<RecipientTuple> rcpts = new ArrayList<>();
		
		if (!task.getAssignees().isEmpty()) {
			for (TaskAssignee assignee : task.getAssignees()) {
				InternetAddress ia = WT.getUserPersonalEmail(assignee.getRecipientProfileId(ownerProfile.getDomainId()));
				if (ia == null) ia = assignee.getRecipientInternetAddress();
				if (ia == null) continue;
				
				
				//TODOOOOOOOOOOOOOOOOOOOOOOO
			}
		}
		return rcpts;
	}
	*/
	
	private static class RecipientTuple {
		public final InternetAddress recipient;
		public final UserProfileId refProfileId;
		
		public RecipientTuple(InternetAddress recipient, UserProfileId profileId) {
			this.recipient = recipient;
			this.refProfileId = profileId;
		}
	}
			
	private boolean doTaskInstanceDeleteAndCommit(Connection con, InstanceInfo info) throws DAOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		TaskRecurrenceDAO recDao = TaskRecurrenceDAO.getInstance();
		
		if (info.belongsToSeries && info.isBroken) { // -> BROKEN INSTANCE
			// 1 - Deletes (logically) this event, the broken
			Set<String> deletedTaskIds = doTaskDelete(con, info.taskId, true);
			if (deletedTaskIds.isEmpty()) return false;

			// 2 - Updates revision of master item
			if (tasDao.updateRevision(con, info.masterTaskId, BaseDAO.createRevisionTimestamp()) == 0) {
				return false;
			}

			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.DELETE, info.taskId, null);
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, info.masterTaskId, null);
				for (String deletedTaskId : deletedTaskIds) {
					writeAuditLog(AuditContext.TASK, AuditAction.DELETE, deletedTaskId, null);
				}
			}
			
		} else if (info.belongsToSeries && (info.seriesInstanceDate != null)) { // -> SERIES TEMPLATE INSTANCE
			// 1 - Inserts an exception on deleted date (no broken item is created)
			if (recDao.insertRecurrenceEx(con, info.masterTaskId, info.seriesInstanceDate) == 0) {
				return false;
			}

			// 2 - Updates revision of master item
			if (tasDao.updateRevision(con, info.masterTaskId, BaseDAO.createRevisionTimestamp()) == 0) {
				return false;
			}

			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.UPDATE, info.masterTaskId, null);
			}
			
		} else { // -> SINGLE INSTANCE or MASTER INSTANCE
			// 1 - Deletes (logically) this event
			Set<String> deletedTaskIds = doTaskDelete(con, info.taskId, true);
			if (deletedTaskIds.isEmpty()) return false;
			
			DbUtils.commitQuietly(con);
			if (isAuditEnabled()) {
				writeAuditLog(AuditContext.TASK, AuditAction.DELETE, info.taskId, null);
				for (String deletedTaskId : deletedTaskIds) {
					writeAuditLog(AuditContext.TASK, AuditAction.DELETE, deletedTaskId, null);
				}
			}
		}
		return true;
	}
	
	private TaskInsertResult doTaskInstanceCopy(Connection con, InstanceInfo info, TaskEx task, int targetCategoryId) throws DAOException, IOException {
		String taskId = info.realTaskId();
		if (info.belongsToSeries && info.isBroken) { // -> BROKEN INSTANCE
			return doTaskCopy(con, taskId, task, targetCategoryId, BitFlag.of(TaskProcessOpts.TAGS, TaskProcessOpts.CUSTOM_VALUES));
			
		} else if (info.belongsToSeries && (info.seriesInstanceDate != null)) { // -> SERIES TEMPLATE INSTANCE
			task.setStart(instanceDateToDateTime(info.seriesInstanceDate, task.getStart(), task.getTimezoneObject()));
			return doTaskCopy(con, taskId, task, targetCategoryId, BitFlag.of(TaskProcessOpts.TAGS, TaskProcessOpts.CUSTOM_VALUES));
			
		} else { // -> SINGLE INSTANCE or MASTER INSTANCE
			return doTaskCopy(con, taskId, task, targetCategoryId, BitFlag.of(TaskProcessOpts.RECUR, TaskProcessOpts.TAGS, TaskProcessOpts.CUSTOM_VALUES));
		}
	}
	
	private List<String> doTaskInstanceMove(Connection con, InstanceInfo info, int targetCategoryId) throws DAOException {
		TaskDAO tasDao = TaskDAO.getInstance();
		DateTime revision = BaseDAO.createRevisionTimestamp();
		
		if (info.belongsToSeries) { // -> MASTER INSTANCE, BROKEN INSTANCE or SERIES TEMPLATE INSTANCE
			ArrayList<String> ret = new ArrayList<>();
			ret.addAll(tasDao.updateCategoryBySeries(con, info.masterTaskId, targetCategoryId, revision));
			if (info.taskId != null) ret.addAll(tasDao.updateCategoryByParent(con, info.taskId, targetCategoryId, revision));
			return ret;
			//return tasDao.updateCategoryBySeries(con, info.masterTaskId, targetCategoryId, revision);
			
		} else { // -> SINGLE INSTANCE
			if (tasDao.updateCategory(con, info.taskId, targetCategoryId, revision) == 1) {
				ArrayList<String> ret = new ArrayList<>();
				ret.add(info.taskId);
				ret.addAll(tasDao.updateCategoryByParent(con, info.taskId, targetCategoryId, revision));
				return ret;
				//return Arrays.asList(info.taskId);
			} else {
				return new ArrayList<>(0);
			}
		}
	}
	
	/*
	private List<TaskAlertLookupInstance> doTaskInstanceListExpiredForUpdate(Connection con, DateTime from, DateTime to, int noOfRecurringInst) throws WTException {
		TaskDAO tasDao = TaskDAO.getInstance();
		ArrayList<TaskAlertLookupInstance> instances = new ArrayList<>();
		
		for (VTaskLookup vtas : tasDao.viewExpiredForUpdateByRange(false, con, from, to)) {
				TaskAlertLookupInstance item = ManagerUtils.fillTaskAlertLookup(new TaskAlertLookupInstance(), vtas);
				item.setId(TaskInstanceId.build(vtas.getTaskId(), vtas.getSeriesTaskId(), vtas.getSeriesInstanceId()));
				item.setTaskId(vtas.getTaskId());
				instances.add(item);
		}
		for (VTaskLookup vtas : tasDao.viewExpiredForUpdateByRange(true, con, from, to)) {
				instances.addAll(calculateRecurringInstances(con, new TaskAlertLookupInstanceMapper(vtas), from, to, noOfRecurringInst));
			}
		return instances;
	}
	*/
	
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
	
	private ReminderInApp createTaskReminderAlertWeb(ProfileI18n profileI18n, String taskInstanceId, TaskAlertLookup task, DateTime reminderDate) {
		String type = task.getHasRecurrence() ? "task-recurring" : "task";
		ReminderInApp alert = new ReminderInApp(SERVICE_ID, task.getCategoryProfileId(), type, taskInstanceId);
		alert.setTitle(task.getSubject());
		alert.setDate(reminderDate);
		alert.setTimezone(profileI18n.getTimezone().getID());
		return alert;
	}
	
	private ReminderEmail createTaskReminderAlertEmail(ProfileI18n profileI18n, String taskInstanceId, TaskAlertLookup task, String recipientEmail) throws WTException {
		ReminderEmail alert = new ReminderEmail(SERVICE_ID, task.getCategoryProfileId(), "task", taskInstanceId);
		
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
		public final List<OTaskAssignee> oassignees;
		public final Set<String> otags;
		public final List<OTaskAttachment> oattachments;
		public final List<OTaskCustomValue> ocustomvalues;
		
		public TaskInsertResult(OTask otask, List<OTaskAssignee> oassignees, Set<String> otags, List<OTaskAttachment> oattachments, ArrayList<OTaskCustomValue> ocustomvalues) {
			this.otask = otask;
			this.oassignees = oassignees;
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
	
	private class AuditTaskResult {
		public final AuditContext context;
		public final AuditAction action;
		public final Object reference;
		public final Object data;
		
		public AuditTaskResult(AuditContext context, AuditAction action, Object reference, Object data) {
			this.context = context;
			this.action = action;
			this.reference = reference;
			this.data = data;
		}
	}
	
	private class AuditTaskObj implements AuditReferenceDataEntry {
		public final String taskId;
		
		public AuditTaskObj(String taskId) {
			this.taskId = taskId;
		}

		@Override
		public String getReference() {
			return String.valueOf(taskId);
		}

		@Override
		public String getData() {
			return null;
		}
	}
	
	private class AuditTaskMoveObj implements AuditReferenceDataEntry {
		public final String taskId;
		public final int origCategoryId;
		
		public AuditTaskMoveObj(String taskId, int origCategoryId) {
			this.taskId = taskId;
			this.origCategoryId = origCategoryId;
		}

		@Override
		public String getReference() {
			return taskId;
		}

		@Override
		public String getData() {
			return String.valueOf(origCategoryId);
		}
	}
	
	private class AuditTaskCopyObj implements AuditReferenceDataEntry {
		public final String taskId;
		public final String origTaskId;
		
		public AuditTaskCopyObj(String taskId, String origTaskId) {
			this.taskId = taskId;
			this.origTaskId = origTaskId;
		}

		@Override
		public String getReference() {
			return taskId;
		}

		@Override
		public String getData() {
			return origTaskId;
		}
	}
	
	private class TaskLookupInstanceMapper implements RecurringInstanceMapper<TaskLookupInstance> {
		private final VTaskLookup task;
		private final boolean censorize;
		
		public TaskLookupInstanceMapper(VTaskLookup task, boolean censorize) {
			this.task = task;
			this.censorize = censorize;
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return task.getTimezoneObject();
		}
		
		@Override
		public String getTaskId() {
			return task.getTaskId();
		}

		@Override
		public DateTime getTaskStart() {
			return task.getStart();
		}
		
		@Override
		public DateTime getTaskDue() {
			return task.getDue();
		}
		
		@Override
		public TaskLookupInstance createInstance(TaskInstanceId id, DateTime start, DateTime due) {
			TaskLookupInstance item = ManagerUtils.fillTaskLookup(new TaskLookupInstance(), task);
			item.setId(id);
			item.setTaskId(task.getTaskId());
			item.setStart(start);
			item.setDue(due);
			if (censorize) item.censorize();
			return item;
		}
	}
	
	private class TaskAlertLookupInstanceMapper implements RecurringInstanceMapper<TaskAlertLookupInstance> {
		private final VTaskLookup task;
		
		public TaskAlertLookupInstanceMapper(VTaskLookup task) {
			this.task = task;
		}
		
		@Override
		public DateTimeZone getTimezone() {
			return task.getTimezoneObject();
		}
		
		@Override
		public String getTaskId() {
			return task.getTaskId();
		}

		@Override
		public DateTime getTaskStart() {
			return task.getStart();
		}
		
		@Override
		public DateTime getTaskDue() {
			return task.getDue();
		}
		
		@Override
		public TaskAlertLookupInstance createInstance(TaskInstanceId id, DateTime start, DateTime due) {
			TaskAlertLookupInstance item = ManagerUtils.fillTaskAlertLookup(new TaskAlertLookupInstance(), task);
			item.setId(id);
			item.setTaskId(task.getTaskId());
			item.setStart(start);
			item.setDue(due);
			return item;
		}
	}
	
	private interface RecurringInstanceMapper<T> {
		public DateTimeZone getTimezone();
		public String getTaskId();
		public DateTime getTaskStart();
		public DateTime getTaskDue();
		public T createInstance(TaskInstanceId id, DateTime start, DateTime due);
	}
	
	
	/*
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
	*/
	
	/*
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
	*/
	
	/*
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
				doTaskAttachmentInsert(con, task.getTaskId(), (TaskAttachmentWithStream)att);
			}
			for (TaskAttachment att : changeSet.updated) {
				if (!(att instanceof TaskAttachmentWithStream)) continue;
				doTaskAttachmentUpdate(con, (TaskAttachmentWithStream)att);
			}
			for (TaskAttachment att : changeSet.deleted) {
				attDao.deleteById(con, att.getAttachmentId());
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
	*/
}
