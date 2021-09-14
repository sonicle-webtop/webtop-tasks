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
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.tasks.ITasksManager;
import com.sonicle.webtop.tasks.bol.OChildrenCount;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.OTaskInstanceInfo;
import com.sonicle.webtop.tasks.bol.VTaskObject;
import com.sonicle.webtop.tasks.bol.VTaskObjectChanged;
import com.sonicle.webtop.tasks.bol.VTaskLookup;
import static com.sonicle.webtop.tasks.jooq.Tables.CATEGORIES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_ICALENDARS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_RECURRENCES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_TAGS;
import com.sonicle.webtop.tasks.jooq.tables.Tasks;
import com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskInstance;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Param;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author malbinola
 */
public class TaskDAO extends BaseDAO {
	private final static TaskDAO INSTANCE = new TaskDAO();
	public static TaskDAO getInstance() {
		return INSTANCE;
	}
	
	public OTaskInstanceInfo selectInstanceInfo(Connection con, TaskInstanceId instanceId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl.select(
				field(
					select(
						nvl2(TASKS.PARENT_TASK_ID, true, false)
					)
					.from(TASKS)
					.where(
						TASKS.TASK_ID.equal(instanceId.getTaskId())
					)
				).as("has_parent"),
				field(exists(
					selectOne()
					.from(TASKS_RECURRENCES)
					.where(
						TASKS_RECURRENCES.TASK_ID.equal(instanceId.getTaskId())
					)
				)).as("has_recurrence"),
				field(
					select(
						TASKS.TASK_ID
					)
					.from(TASKS)
					.where(
						TASKS.SERIES_TASK_ID.equal(instanceId.getTaskId())
						.and(TASKS.SERIES_INSTANCE_ID.equal(instanceId.getInstance()))
					)
				).as("task_id_by_instance"),
				/*
				field(exists(
					selectOne()
					.from(TASKS)
					.where(
						TASKS.PARENT_TASK_ID.equal(instanceId.getTaskId())
					)
				)).as("has_children"),
				*/
				coalesce(
					field(
						select(
							TASKS.TIMEZONE
						)
						.from(TASKS)
						.where(
							TASKS.SERIES_TASK_ID.equal(instanceId.getTaskId())
							.and(TASKS.SERIES_INSTANCE_ID.equal(instanceId.getInstance()))
						)
					),
					field(
						select(
							TASKS.TIMEZONE
						)
						.from(TASKS)
						.where(
							TASKS.TASK_ID.equal(instanceId.getTaskId())
						)
					)
				).as("timezone")
			).fetchOneInto(OTaskInstanceInfo.class);
	}
	
	public String selectIdBySeriesInstance(Connection con, String seriesTaskId, String seriesInstance) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS.TASK_ID
			)
			.from(TASKS)
			.where(
				TASKS.SERIES_TASK_ID.in(seriesTaskId)
				.and(TASKS.SERIES_INSTANCE_ID.in(seriesInstance))
			)
			.fetchOne(0, String.class);
	}
	
	public OTask selectById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS)
			.where(TASKS.TASK_ID.equal(taskId))
			.fetchOneInto(OTask.class);
	}
	
	public OChildrenCount selectCountByParent(Connection con, String parentTaskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl.select(
				field(
					DSL.selectCount()
					.from(TASKS)
					.where(
						TASKS.PARENT_TASK_ID.equal(parentTaskId)
					)
				).as("total_count"),
				field(
					DSL.selectCount()
					.from(TASKS)
					.where(
						TASKS.PARENT_TASK_ID.equal(parentTaskId)
						.and(TASKS.STATUS.equal(EnumUtils.toSerializedName(Task.Status.COMPLETED)))
					)
				).as("completed_count")
			).fetchOneInto(OChildrenCount.class);
	}
	
	public OTask selectOnlineById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS)
			.where(
				TASKS.TASK_ID.equal(taskId)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(TaskBase.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(TaskBase.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(OTask.class);
	}
	
	public Integer selectCategoryId(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS.CATEGORY_ID
			)
			.from(TASKS)
			.where(TASKS.TASK_ID.equal(taskId))
			.fetchOneInto(Integer.class);
	}
	
	public Map<String, Integer> selectCategoriesByIds(Connection con, Collection<String> taskIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.CATEGORY_ID
			)
			.from(TASKS)
			.where(
				TASKS.TASK_ID.in(taskIds)
			)
			.fetchMap(TASKS.TASK_ID, TASKS.CATEGORY_ID);
	}
	
	@Deprecated
	public List<VTaskLookup> viewExpiredForUpdateByRange(boolean recurring, Connection con, DateTime rangeFrom, DateTime rangeTo) throws DAOException {
		DSLContext dsl = getDSL(con);
		//https://gitlab.com/davical-project/davical/-/blob/master/dba/rrule_functions.sql
		//https://github.com/volkanunsal/postgres-rrule
		
		Condition recurCndt = null;
		if (recurring) {
			recurCndt = TASKS_RECURRENCES.TASK_ID.isNotNull();
		} else {
			recurCndt = TASKS_RECURRENCES.TASK_ID.isNull();
		}
		
		Condition rangeCndt = DSL.trueCondition();
		if (recurring) {
			rangeCndt = TASKS_RECURRENCES.START.between(rangeFrom, rangeTo) // Recurrences that start in current range
				.or(TASKS_RECURRENCES.UNTIL.between(rangeFrom, rangeTo)) // Recurrences that end in current range
				.or(TASKS_RECURRENCES.START.lessThan(rangeFrom).and(TASKS_RECURRENCES.UNTIL.greaterThan(rangeTo))); // Recurrences that start before and end after
		} else {
			rangeCndt = TASKS.START.between(rangeFrom, rangeTo); // Events that start in current range
		}
		
		// New field: has children
		Tasks TT1 = TASKS.as("ta1");
		Field<Boolean> hasChildren = field(exists(
				selectOne()
				.from(TT1)
				.where(
					TT1.PARENT_TASK_ID.equal(TASKS.TASK_ID)
				)
			)).as("has_children");
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.CATEGORY_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.ORGANIZER,
				TASKS.ORGANIZER_ID,
				TASKS.PUBLIC_UID,
				TASKS.SUBJECT,
				TASKS.TIMEZONE,
				TASKS.START,
				TASKS.DUE,
				TASKS.REMINDER,
				TASKS.REMINDED_ON
			)
			.select(
				hasChildren,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
				.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				.and(
					TASKS.REMINDER.isNotNull().and(TASKS.REMINDED_ON.isNull()) // Note this NULL test on REMINDED_ON field!!!
				)
				.and(recurCndt)
				.and(rangeCndt)
			)
			.orderBy(
				TASKS.START.asc()
			)
			.forUpdate()
			.fetchInto(VTaskLookup.class);
	}
	
	public VTaskObject viewTaskObjectById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
			).asField("tags");
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(TASKS_ICALENDARS.TASK_ID, true, false).as("has_icalendar");
		
		return dsl
			.select(
				getVTaskObjectFields(false)
			)
			.select(
				tags,
				hasRecurrence,
				hasICalendar
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(TASKS_ICALENDARS).on(TASKS.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID))
			.where(
				TASKS.TASK_ID.equal(taskId)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VTaskObject.class);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategory(Connection con, boolean stat, int categoryId) throws DAOException {
		return viewTaskObjectsByCategoryHrefsSince(con, stat, categoryId, null, null);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategoryHrefs(Connection con, boolean stat, int categoryId, Collection<String> hrefs) throws DAOException {
		return viewTaskObjectsByCategoryHrefsSince(con, stat, categoryId, hrefs, null);
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategorySince(Connection con, boolean stat, int categoryId, DateTime since) throws DAOException {
		return viewTaskObjectsByCategoryHrefsSince(con, stat, categoryId, null, since);
	}
	
	private Field[] getVTaskObjectFields(boolean stat) {
		if (stat) {
			return new Field[]{
				TASKS.TASK_ID,
				TASKS.CATEGORY_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.PUBLIC_UID,
				TASKS.HREF
			};
		} else {
			return new Field[]{
				TASKS.TASK_ID,
				TASKS.CATEGORY_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.REVISION_SEQUENCE,
				TASKS.CREATION_TIMESTAMP,
				TASKS.ORGANIZER,
				TASKS.ORGANIZER_ID,
				TASKS.PUBLIC_UID,
				TASKS.SUBJECT,
				TASKS.DESCRIPTION,
				TASKS.DESCRIPTION_TYPE,
				TASKS.TIMEZONE,
				TASKS.START,
				TASKS.DUE,
				TASKS.PROGRESS,
				TASKS.STATUS,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.REMINDER,
				TASKS.HREF
			};
		}
	}
	
	public Map<String, List<VTaskObject>> viewTaskObjectsByCategoryHrefsSince(Connection con, boolean stat, int categoryId, Collection<String> hrefs, DateTime since) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition inHrefsCndt = DSL.trueCondition();
		if (hrefs != null) {
			inHrefsCndt = TASKS.HREF.in(hrefs);
		}
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(TASKS.START, null, TASKS_RECURRENCES.RULE, rangeFromPar, null);
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: has icalendar
		Field<Boolean> hasICalendar = DSL.nvl2(TASKS_ICALENDARS.TASK_ID, true, false).as("has_icalendar");
		
		return dsl
			.select(
				getVTaskObjectFields(stat)
			)
			.select(
				hasRecurrence,
				hasICalendar
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(TASKS_ICALENDARS).on(TASKS.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID))
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(inHrefsCndt)
				.and(overlaps)
			)
			.orderBy(
				TASKS.TASK_ID.asc()
			)
			.fetchGroups(TASKS.HREF, VTaskObject.class);
	}
	
	public List<VTaskObjectChanged> viewOnlineTaskObjectsChangedByCategory(Connection con, int categoryId, int limit) throws DAOException {
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
			.fetchInto(VTaskObjectChanged.class);
	}
	
	public List<VTaskObjectChanged> viewTaskObjectsChangedByCategorySince(Connection con, int categoryId, DateTime since, int limit) throws DAOException {
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
			.fetchInto(VTaskObjectChanged.class);
	}
	
	public int countByCategoryPattern(Connection con, Collection<Integer> categoryIds, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
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
					filterCndt
				)
			)
			.fetchOne(0, Integer.class);
	}
	
	public Condition toCondition(ITasksManager.TaskListView view, DateTime today) {
		if (ITasksManager.TaskListView.ALL.equals(view)) {
			return TASKS.SERIES_TASK_ID.isNull()
				.and(TASKS.SERIES_INSTANCE_ID.isNull());
			
		} else if (ITasksManager.TaskListView.TODAY.equals(view)) {
			DateTime todayAtBeginning = today.withTimeAtStartOfDay();
			return TASKS.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
				.or(
					TASKS.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
					.and(TASKS.COMPLETED_ON.greaterOrEqual(todayAtBeginning))
					.and(TASKS.COMPLETED_ON.lessThan(todayAtBeginning.plusDays(1)))
				);
			
		} else if (ITasksManager.TaskListView.NEXT_7.equals(view)) {
			DateTime todayAtBeginning = today.withTimeAtStartOfDay();
			return TASKS.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
				.or(
					TASKS.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
					.and(TASKS.COMPLETED_ON.greaterOrEqual(todayAtBeginning))
					.and(TASKS.COMPLETED_ON.lessThan(todayAtBeginning.plusDays(1)))
				);
			
		} else if (ITasksManager.TaskListView.NOT_STARTED.equals(view)) {
			return TASKS.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.NEEDS_ACTION));
			
		} else if (ITasksManager.TaskListView.LATE.equals(view)) {
			return TASKS.COMPLETED_ON.isNull()
				.and(TASKS.DUE.lessThan(DateTime.now()));
			
		} else if (ITasksManager.TaskListView.COMPLETED.equals(view)) {
			return TASKS.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
			
		} else if (ITasksManager.TaskListView.NOT_COMPLETED.equals(view)) {
			return TASKS.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
			
		} else if (ITasksManager.TaskListView.UPCOMING.equals(view)) {
			return TASKS.DUE.isNotNull()
				.and(TASKS.STATUS.notIn(EnumUtils.toSerializedName(Task.Status.COMPLETED), EnumUtils.toSerializedName(Task.Status.CANCELLED)));
			
		} else {
			return null;
		}
	}
	
	public List<VTaskLookup> viewOnlineByCategoryRangeCondition(Connection con, Collection<Integer> categoryIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		//https://gitlab.com/davical-project/davical/-/blob/master/dba/rrule_functions.sql
		//https://github.com/volkanunsal/postgres-rrule
		//SELECT * FROM rrule_event_instances_range('2021-04-20 18:16:24+02'::timestamptz, 'FREQ=DAILY;COUNT=5', '2021-04-21 00:00:00+02'::timestamptz, '2021-04-29 00:00:00+02'::timestamptz, 100)
		
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (rangeFrom != null) ? DSL.value(rangeFrom) : null;
		Param<DateTime> rangeToPar = (rangeTo != null) ? DSL.value(rangeTo) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(TASKS.START, null, TASKS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: has children
		Tasks TT1 = TASKS.as("ta1");
		Field<Boolean> hasChildren = field(exists(
				selectOne()
				.from(TT1)
				.where(
					TT1.PARENT_TASK_ID.equal(TASKS.TASK_ID)
				)
			)).as("has_children");
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
			).asField("tags");
		
		// New field: parent is not null
		Tasks PTT1 = TASKS.as("pta1");
		Field<Boolean> parentNotNull = DSL.field(PTT1.TASK_ID.isNotNull());
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.CATEGORY_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.ORGANIZER,
				TASKS.ORGANIZER_ID,
				TASKS.PUBLIC_UID,
				TASKS.SUBJECT,
				TASKS.DESCRIPTION,
				TASKS.DESCRIPTION_TYPE,
				TASKS.TIMEZONE,
				TASKS.START,
				TASKS.DUE,
				TASKS.PROGRESS,
				TASKS.STATUS,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.DOCUMENT_REF,
				TASKS.REMINDER				
			)
			.select(
				hasRecurrence,
				hasChildren,
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(PTT1).on(TASKS.PARENT_TASK_ID.equal(PTT1.TASK_ID))
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					TASKS.START.isNull()
					.or(overlaps)
				)
				.and(filterCndt)
			)
			.orderBy(
				DSL.coalesce(PTT1.TASK_ID, TASKS.TASK_ID),
				parentNotNull,
				TASKS.TASK_ID
			)
			.fetchInto(VTaskLookup.class);
	}
	
	public List<VTaskLookup> viewOnlineExpiredByRangeForUpdate(Connection con, DateTime rangeFrom, DateTime rangeTo) {
		DSLContext dsl = getDSL(con);
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (rangeFrom != null) ? DSL.value(rangeFrom) : null;
		Param<DateTime> rangeToPar = (rangeTo != null) ? DSL.value(rangeTo) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(TASKS.START, null, TASKS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: has children
		Tasks TT1 = TASKS.as("ta1");
		Field<Boolean> hasChildren = field(exists(
				selectOne()
				.from(TT1)
				.where(
					TT1.PARENT_TASK_ID.equal(TASKS.TASK_ID)
				)
			)).as("has_children");
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.CATEGORY_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.ORGANIZER,
				TASKS.ORGANIZER_ID,
				TASKS.PUBLIC_UID,
				TASKS.SUBJECT,
				TASKS.DESCRIPTION,
				TASKS.DESCRIPTION_TYPE,
				TASKS.TIMEZONE,
				TASKS.START,
				TASKS.DUE,
				TASKS.PROGRESS,
				TASKS.STATUS,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.REMINDER,
				TASKS.REMINDED_ON // Important for recurring tasks, see WHERE part below!
			)
			.select(
				hasRecurrence,
				hasChildren,
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				.and(TASKS.START.isNotNull())
				.and(
					TASKS.REMINDER.isNotNull().and(
						TASKS_RECURRENCES.TASK_ID.isNull().and(TASKS.REMINDED_ON.isNull()) // Normal tasks: REMINDED_ON must be null!
						.or(TASKS_RECURRENCES.TASK_ID.isNotNull()) // Recurring tasks: REMINDED_ON can be null or not, calling code will check it!
					)
				)
				.and(overlaps)
			)
			.fetchInto(VTaskLookup.class);
	}
	
	/*
	public List<VTaskLookup> viewByCategoryRangeCondition(Connection con, Collection<Integer> categoryIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		//https://gitlab.com/davical-project/davical/-/blob/master/dba/rrule_functions.sql
		//https://github.com/volkanunsal/postgres-rrule
		
		Condition rangeCndt = DSL.trueCondition();
		if ((rangeFrom != null) && (rangeTo != null)) {
			rangeCndt = TASKS.START_DATE.between(rangeFrom, rangeTo); // Events that start in current range
		}
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.CATEGORY_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.OWNER_ID,
				TASKS.OWNER,
				TASKS.PUBLIC_UID,
				TASKS.SUBJECT,
				TASKS.BODY,
				TASKS.BODY_TYPE,
				TASKS.START_DATE,
				TASKS.DUE_DATE,
				TASKS.COMPLETION_PERC,
				TASKS.STATUS,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.REMINDER
			)
			.select(
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS_RECURRENCES.TASK_ID.isNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				TASKS.START_DATE.asc(),
				TASKS.SUBJECT.asc()
			)
			.fetchInto(VTaskLookup.class);
	}
	*/
	
	/*
	public List<VTaskLookup> viewRecurringByCategoryRangeCondition(Connection con, Collection<Integer> categoryIds, DateTime rangeFrom, DateTime rangeTo, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition rangeCndt = DSL.trueCondition();
		if ((rangeFrom != null) && (rangeTo != null)) {
			rangeCndt = TASKS_RECURRENCES.START_DATE.between(rangeFrom, rangeTo) // Recurrences that start in current range
				.or(TASKS_RECURRENCES.UNTIL_DATE.between(rangeFrom, rangeTo)) // Recurrences that end in current range
				.or(TASKS_RECURRENCES.START_DATE.lessThan(rangeFrom).and(TASKS_RECURRENCES.UNTIL_DATE.greaterThan(rangeTo))); // Recurrences that start before and end after
		}
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.SERIES_TASK_ID,
				TASKS.SERIES_INSTANCE_ID,
				TASKS.CATEGORY_ID,
				TASKS.PARENT_TASK_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.OWNER_ID,
				TASKS.OWNER,
				TASKS.PUBLIC_UID,
				TASKS.SUBJECT,
				TASKS.BODY,
				TASKS.BODY_TYPE,
				TASKS.START_DATE,
				TASKS.DUE_DATE,
				TASKS.COMPLETION_PERC,
				TASKS.STATUS,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.REMINDER
			)
			.select(
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS_RECURRENCES.TASK_ID.isNotNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				TASKS.START_DATE.asc(),
				TASKS.SUBJECT.asc()
			)
			.fetchInto(VTaskLookup.class);
	}
	*/
	
	/*
	public List<VTaskLookup> viewByCategoryCondition(Connection con, Collection<Integer> categoryIds, Condition condition, int limit, int offset) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.PUBLIC_UID,
				TASKS.CATEGORY_ID,
				TASKS.REVISION_STATUS,
				TASKS.REVISION_TIMESTAMP,
				TASKS.CREATION_TIMESTAMP,
				TASKS.SUBJECT,
				TASKS.BODY,
				TASKS.BODY_TYPE,
				//TASKS.TIMEZONE, // Timezone can't be changed, it's set at creation!
				TASKS.START,
				TASKS.DUE,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.STATUS,
				TASKS.COMPLETION_PERC
				//TASKS.REMINDER_DATE
			)
			.select(
				tags,
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
					filterCndt
				)
			)
			.orderBy(
				TASKS.SUBJECT.asc()
			)
			.limit(limit)
			.offset(offset)
			.fetchInto(VTaskLookup.class);
	}
	*/
	
	@Deprecated
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
				TASKS.DESCRIPTION_TYPE,
				TASKS.START,
				TASKS.DUE,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
				TASKS.STATUS,
				TASKS.PROGRESS
				//TASKS.REMINDER_DATE
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
					TASKS.DUE.isNotNull()
					.and(TASKS.STATUS.notIn(EnumUtils.toSerializedName(Task.Status.COMPLETED), EnumUtils.toSerializedName(Task.Status.CANCELLED)))
				)
				.and(
					patternCndt
				)
			)
			.orderBy(
				TASKS.DUE.asc()
			)
			.fetchInto(VTaskLookup.class);
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
	
	public int insert(Connection con, OTask item, boolean setContactRef, boolean setDocRef) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(EnumUtils.toSerializedName(Task.RevisionStatus.NEW));
		if (!setContactRef) {
			item.setContact(null);
			item.setContactId(null);
		}
		if (!setDocRef) {
			item.setDocumentRef(null);
		}
		TasksRecord record = dsl.newRecord(TASKS, item);
		return dsl
			.insertInto(TASKS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OTask item, boolean setContactRef, boolean setDocRef, boolean clearRemindedOn, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED));
		item.setRevisionTimestamp(revisionTimestamp);
		
		UpdateSetMoreStep update = dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID, item.getCategoryId())
			.set(TASKS.REVISION_STATUS, item.getRevisionStatus())
			.set(TASKS.REVISION_TIMESTAMP, item.getRevisionTimestamp())
			.set(TASKS.SUBJECT, item.getSubject())
			.set(TASKS.LOCATION, item.getLocation())
			.set(TASKS.DESCRIPTION, item.getDescription())
			.set(TASKS.DESCRIPTION_TYPE, item.getDescriptionType())
			.set(TASKS.START, item.getStart())
			.set(TASKS.DUE, item.getDue())
			.set(TASKS.COMPLETED_ON, item.getCompletedOn())
			.set(TASKS.PROGRESS, item.getProgress())
			.set(TASKS.STATUS, item.getStatus())
			.set(TASKS.IMPORTANCE, item.getImportance())
			.set(TASKS.IS_PRIVATE, item.getIsPrivate())
			.set(TASKS.REMINDER, item.getReminder());
		
		if (setDocRef) {
			update = update
				.set(TASKS.DOCUMENT_REF, item.getDocumentRef());
		} else {
			item.setDocumentRef(null);
		}
		if (setContactRef) {
			update = update
				.set(TASKS.CONTACT, item.getContact())
				.set(TASKS.CONTACT_ID, item.getContactId());
		} else {
			item.setContact(null);
			item.setContactId(null);
		}
		if (clearRemindedOn) {
			update = update
				.set(TASKS.REMINDED_ON, (DateTime)null);
		}
		
		return update
			.where(
				TASKS.TASK_ID.equal(item.getTaskId())
			)
			.execute();
	}
	
	public int updateCategory(Connection con, String taskId, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID, categoryId)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int updateRemindedOn(Connection con, String taskId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REMINDED_ON, remindedOn)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public List<String> updateOnlineSubjectsBySeries(Connection con, String seriesTaskId, String oldSubject, String newSubject, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS)
			.set(TASKS.SUBJECT, newSubject)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.SERIES_TASK_ID.equal(seriesTaskId)
				.and(
					TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS.SUBJECT.equal(oldSubject))
			)
			.returning(
				TASKS.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toList());
	}
	
	public List<String> updateCategoryBySeries(Connection con, String seriesTaskId, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID, categoryId)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(seriesTaskId)
					.or(TASKS.SERIES_TASK_ID.equal(seriesTaskId))
				.and(TASKS.CATEGORY_ID.notEqual(categoryId))
			)
			.returning(
				TASKS.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toList());
	}
	
	public int updateParentProgressByChild(Connection con, String childTaskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Tasks TASKS_1 = TASKS.as("t1");
		Tasks TASKS_2 = TASKS.as("t2");
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.set(TASKS.PROGRESS, 
				DSL.select(
					avg(TASKS_1.PROGRESS)
				)
				.from(TASKS_1)
				.where(
					TASKS_1.PARENT_TASK_ID.equal(TASKS.TASK_ID)
				).asField()
			)
			.where(
				TASKS.TASK_ID.equal(
					select(TASKS_2.PARENT_TASK_ID)
					.from(TASKS_2)
					.where(
						TASKS_2.TASK_ID.equal(childTaskId)
					)
				)
			)
			.execute();
	}
	
	public Set<String> updateCategoryByParent(Connection con, String parentTaskId, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID, categoryId)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.PARENT_TASK_ID.equal(parentTaskId)
				.and(TASKS.CATEGORY_ID.notEqual(categoryId))
			)
			.returning(
				TASKS.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toSet());
	}
	
	public Set<String> updateCompletedByParent(Connection con, String parentTaskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS)
			.set(TASKS.STATUS, EnumUtils.toSerializedName(Task.Status.COMPLETED))
			.set(TASKS.COMPLETED_ON, revisionTimestamp)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(	
				TASKS.PARENT_TASK_ID.equal(parentTaskId)
				.and(TASKS.STATUS.notEqual(EnumUtils.toSerializedName(Task.Status.COMPLETED)))
			)
			.returning(
				TASKS.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toSet());
	}
	
	public int updateParentProgressByParent(Connection con, String parentTaskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Tasks TASKS_1 = TASKS.as("t1");
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.set(TASKS.PROGRESS, 
				DSL.select(
					avg(TASKS_1.PROGRESS)
				)
				.from(TASKS_1)
				.where(
					TASKS_1.PARENT_TASK_ID.equal(TASKS.TASK_ID)
				).asField()
					
				//sum.divide(count).cast(Short.class)
				/*
				DSL.select(nvl(sum(TASKS_1.PROGRESS), 0))
				.from(TASKS_1)
				.where(
					TASKS_1.PARENT_TASK_ID.equal(TASKS.TASK_ID)
				)
				.asField()
				.divide(
					DSL.selectCount()
					.from(TASKS_2)
					.where(
						TASKS_2.PARENT_TASK_ID.equal(TASKS.TASK_ID)
					)
					.asField()
				).cast(Short.class)
				*/
			)
			.where(
				TASKS.TASK_ID.equal(parentTaskId)
			)
			.execute();
	}
	
	public int updateRevision(Connection con, String taskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int updateRevisionStatus(Connection con, String taskId, Task.RevisionStatus revisionStatus, DateTime revisionTimestamp) throws DAOException {
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
	
	public int deleteById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS)
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
	
	public int logicDeleteById(Connection con, String taskId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.PARENT_TASK_ID, (String)null)
			.set(TASKS.REVISION_STATUS, DELETED)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
				.and(TASKS.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	public Set<String> logicDeleteByParent(Connection con, String parentTaskId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS)
			.set(TASKS.PARENT_TASK_ID, (String)null)
			.set(TASKS.REVISION_STATUS, DELETED)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(	
				TASKS.PARENT_TASK_ID.equal(parentTaskId)
				.and(TASKS.REVISION_STATUS.notEqual(DELETED))
			)
			.returning(
				TASKS.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toSet());
	}
	
	public int logicDeleteByCategoryId(Connection con, int categoryId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.PARENT_TASK_ID, (String)null)
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
