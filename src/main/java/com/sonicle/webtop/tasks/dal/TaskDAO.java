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
package com.sonicle.webtop.tasks.dal;

import com.sonicle.commons.EnumUtils;
import static com.sonicle.webtop.tasks.jooq.Sequences.SEQ_TASKS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.bol.VTaskObject;
import com.sonicle.webtop.tasks.bol.VTaskCalObjectChanged;
import com.sonicle.webtop.tasks.bol.VTaskLookup;
import static com.sonicle.webtop.tasks.jooq.Tables.CATEGORIES;
import static com.sonicle.webtop.tasks.jooq.tables.TasksIcalendars.TASKS_ICALENDARS;
import com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord;
import com.sonicle.webtop.tasks.model.Task;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 *
 * @author malbinola
 */
public class TaskDAO extends BaseDAO {
	private final static TaskDAO INSTANCE = new TaskDAO();
	public static TaskDAO getInstance() {
		return INSTANCE;
	}

	public Long getSequence(Connection con) throws DAOException {
		DSLContext dsl = getDSL(con);
		Long nextID = dsl.nextval(SEQ_TASKS);
		return nextID;
	}

	public List<VTask> viewExpridedForUpdateByUntil(Connection con, DateTime until) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.CATEGORY_ID,
				TASKS.SUBJECT,
				TASKS.START_DATE,
				TASKS.DUE_DATE,
				TASKS.COMPLETED_DATE,
				TASKS.STATUS,
				TASKS.COMPLETION_PERCENTAGE,
				TASKS.REMINDER_DATE,
				TASKS.PUBLIC_UID
			)
			.select(
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS.REMINDER_DATE.isNotNull()
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS.REMINDER_DATE.lessThan(until))
			)
			.orderBy(
				TASKS.REMINDER_DATE.asc(),TASKS.SUBJECT.asc()
			)
			.forUpdate()
			.fetchInto(VTask.class);
	}
	
	public VTaskObject viewTaskObjectById(Connection con, int categoryId, int taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(TASKS_ICALENDARS.TASK_ID, true, false).as("has_icalendar");
		
		return dsl
			.select(
				TASKS.fields()
			)
			.select(
				hasICalendar
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_ICALENDARS).on(
				TASKS.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID)
			)
			.where(
				TASKS.TASK_ID.equal(taskId)
				.and(TASKS.CATEGORY_ID.equal(categoryId))
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VTaskObject.class);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategory(Connection con, int categoryId) throws DAOException {
		return viewTaskObjectsByCategoryHrefsSince(con, categoryId, null, null);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategoryHrefs(Connection con, int categoryId, Collection<String> hrefs) throws DAOException {
		return viewTaskObjectsByCategoryHrefsSince(con, categoryId, hrefs, null);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategorySince(Connection con, int categoryId, DateTime since) throws DAOException {
		return viewTaskObjectsByCategoryHrefsSince(con, categoryId, null, since);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategoryHrefsSince(Connection con, int categoryId, Collection<String> hrefs, DateTime since) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(TASKS_ICALENDARS.TASK_ID, true, false).as("has_icalendar");
	
		Condition inHrefsCndt = DSL.trueCondition();
		if (hrefs != null) {
			inHrefsCndt = TASKS.HREF.in(hrefs);
		}
		
		Condition rangeCndt = null;
		/*
		if (since != null) {
			rangeCndt = TASKS.END_DATE.greaterOrEqual(since)
				.or(RECURRENCES.RECURRENCE_ID.isNotNull()
					.and(RECURRENCES.UNTIL_DATE.greaterOrEqual(since).or(RECURRENCES.UNTIL_DATE.isNull()))	
				);
		}
		*/
		
		if (rangeCndt == null) {
			return dsl
				.select(
					TASKS.fields()
				)
				.select(
					hasICalendar
				)
				.from(TASKS)
				.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
				.leftOuterJoin(TASKS_ICALENDARS).on(
					TASKS.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID)
				)
				.where(
					TASKS.CATEGORY_ID.equal(categoryId)
					.and(
						TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
						.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
					)
					.and(inHrefsCndt)
				)
				.orderBy(
					TASKS.TASK_ID.asc()
				)
				.fetchGroups(TASKS.HREF, VTaskObject.class);
		} else {
			return dsl
				.select(
					TASKS.fields()
				)
				.select(
					hasICalendar
				)
				.from(TASKS)
				.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
				//.leftOuterJoin(RECURRENCES).on(EVENTS.RECURRENCE_ID.equal(RECURRENCES.RECURRENCE_ID))
				.leftOuterJoin(TASKS_ICALENDARS).on(
					TASKS.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID)
				)
				.where(
					TASKS.CATEGORY_ID.equal(categoryId)
					.and(
						TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
						.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
					)
					.and(inHrefsCndt)
					.and(rangeCndt)
				)
				.orderBy(
					TASKS.TASK_ID.asc()
				)
				.fetchGroups(TASKS.HREF, VTaskObject.class);
		}
	}
	
	public List<VTaskCalObjectChanged> viewChangedLiveTaskObjectsByCategory(Connection con, int categoryId, int limit) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.HREF
			)
			.from(TASKS)
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
			)
			.orderBy(
				TASKS.TASK_ID.asc()
			)
			.limit(limit)
			.fetchInto(VTaskCalObjectChanged.class);
	}
	
	public List<VTaskCalObjectChanged> viewChangedTaskObjectsByCategorySince(Connection con, int categoryId, DateTime since, int limit) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.HREF
			)
			.from(TASKS)
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
				.and(TASKS.REVISION_TIMESTAMP.greaterThan(since))
			)
			.orderBy(
				TASKS.CREATION_TIMESTAMP.asc()
			)
			.limit(limit)
			.fetchInto(VTaskCalObjectChanged.class);
	}
	
	public int countByCategoryPattern(Connection con, Collection<Integer> categoryIds, String pattern) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition patternCndt = toSearchPatternCondition(pattern);
		
		return dsl
			.selectCount()
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					patternCndt
				)
			)
			.fetchOne(0, Integer.class);
	}
	
	public List<VTaskLookup> viewByCategoryPattern(Connection con, Collection<Integer> categoryIds, String pattern, int limit, int offset) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition patternCndt = toSearchPatternCondition(pattern);
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.PUBLIC_UID,
				TASKS.CATEGORY_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.SUBJECT,
				TASKS.DESCRIPTION,
				TASKS.START_DATE,
				TASKS.DUE_DATE,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.STATUS,
				TASKS.COMPLETION_PERCENTAGE,
				TASKS.REMINDER_DATE
			)
			.select(
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					patternCndt
				)
			)
			.orderBy(
				TASKS.SUBJECT.asc()
			)
			.limit(limit)
			.offset(offset)
			.fetchInto(VTaskLookup.class);
	}
	
	public List<VTaskLookup> viewUpcomingByCategoryPattern(Connection con, Collection<Integer> categoryIds, String pattern) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition patternCndt = toSearchPatternCondition(pattern);
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.PUBLIC_UID,
				TASKS.CATEGORY_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.SUBJECT,
				TASKS.DESCRIPTION,
				TASKS.START_DATE,
				TASKS.DUE_DATE,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.STATUS,
				TASKS.COMPLETION_PERCENTAGE,
				TASKS.REMINDER_DATE
			)
			.select(
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					TASKS.DUE_DATE.isNotNull()
					.and(TASKS.STATUS.notIn(EnumUtils.toSerializedName(Task.Status.COMPLETED), EnumUtils.toSerializedName(Task.Status.DEFERRED)))
				)
				.and(
					patternCndt
				)
			)
			.orderBy(
				TASKS.DUE_DATE.asc()
			)
			.fetchInto(VTaskLookup.class);
	}
	
	public int updateRemindedOn(Connection con, int taskId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REMINDED_ON, remindedOn)
			.set(TASKS.REMINDER_DATE, (DateTime) null)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}	
	
	public OTask selectById(Connection con, int taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS)
			.where(TASKS.TASK_ID.equal(taskId))
			.fetchOneInto(OTask.class);
	}
	
	public Map<Integer, DateTime> selectMaxRevTimestampByCategories(Connection con, Collection<Integer> categoryIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS.CATEGORY_ID,
				DSL.max(TASKS.REVISION_TIMESTAMP)
			)
			.from(TASKS)
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
			)
			.groupBy(
				TASKS.CATEGORY_ID
			)
			.fetchMap(TASKS.CATEGORY_ID, DSL.max(TASKS.REVISION_TIMESTAMP));
	}
	
	public int insert(Connection con, OTask item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(EnumUtils.toSerializedName(Task.RevisionStatus.NEW));
		item.setRevisionTimestamp(revisionTimestamp);
		item.setRevisionSequence(0);
		TasksRecord record = dsl.newRecord(TASKS, item);
		return dsl
			.insertInto(TASKS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OTask item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED));
		item.setRevisionTimestamp(revisionTimestamp);
		return dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID, item.getCategoryId())
			.set(TASKS.REVISION_STATUS, item.getRevisionStatus())
			.set(TASKS.REVISION_TIMESTAMP, item.getRevisionTimestamp())
			.set(TASKS.SUBJECT, item.getSubject())
			.set(TASKS.DESCRIPTION, item.getDescription())
			.set(TASKS.START_DATE, item.getStartDate())
			.set(TASKS.DUE_DATE, item.getDueDate())
			.set(TASKS.COMPLETED_DATE, item.getCompletedDate())
			.set(TASKS.IMPORTANCE, item.getImportance())
			.set(TASKS.IS_PRIVATE, item.getIsPrivate())
			.set(TASKS.STATUS, item.getStatus())
			.set(TASKS.COMPLETION_PERCENTAGE, item.getCompletionPercentage())
			.set(TASKS.REMINDER_DATE, item.getReminderDate())
			.where(
				TASKS.TASK_ID.equal(item.getTaskId())
			)
			.execute();
	}
	
	public int updateCategory(Connection con, int contactId, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID, categoryId)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(contactId)
			)
			.execute();
	}
	
	public int updateRevision(Connection con, int taskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int updateRevisionStatus(Connection con, int taskId, Task.RevisionStatus revisionStatus, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(revisionStatus))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int deleteByCategoryId(Connection con, int categoryId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS)
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
			)
			.execute();
	}
	
	public int logicDeleteById(Connection con, int taskId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, DELETED)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
				.and(TASKS.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	public int logicDeleteByCategoryId(Connection con, int categoryId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, DELETED)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
				.and(TASKS.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	private Condition toSearchPatternCondition(String pattern) {
		Condition cndt = DSL.trueCondition();
		if (!StringUtils.isBlank(pattern)) {
			return TASKS.SUBJECT.likeIgnoreCase(pattern)
					.or(TASKS.DESCRIPTION.likeIgnoreCase(pattern));
		}
		return cndt;
	}
}
