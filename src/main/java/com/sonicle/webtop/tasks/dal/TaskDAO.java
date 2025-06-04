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
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.tasks.ITasksManager;
import com.sonicle.webtop.tasks.bol.OChildrenCount;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.OTaskInstanceInfo;
import com.sonicle.webtop.tasks.bol.VTaskObject;
import com.sonicle.webtop.tasks.bol.VTaskObjectChanged;
import com.sonicle.webtop.tasks.bol.VTaskLookup;
import static com.sonicle.webtop.tasks.jooq.Tables.CATEGORIES;
import static com.sonicle.webtop.tasks.jooq.Tables.HISTORY_TASKS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_ATTACHMENTS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_CUSTOM_VALUES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_ICALENDARS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_RECURRENCES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_TAGS;
import com.sonicle.webtop.tasks.jooq.tables.Tasks;
import com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord;
import com.sonicle.webtop.tasks.model.Task;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Param;
import org.jooq.Record;
import org.jooq.Result;
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
						nvl2(TASKS_.PARENT_TASK_ID, true, false)
					)
					.from(TASKS_)
					.where(
						TASKS_.TASK_ID.equal(instanceId.getTaskId())
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
						TASKS_.TASK_ID
					)
					.from(TASKS_)
					.where(
						TASKS_.SERIES_TASK_ID.equal(instanceId.getTaskId())
						.and(TASKS_.SERIES_INSTANCE_ID.equal(instanceId.getInstance()))
					)
				).as("task_id_by_instance"),
				field(exists(
					selectOne()
					.from(TASKS_)
					.where(
						TASKS_.PARENT_TASK_ID.equal(instanceId.getTaskId())
					)
				)).as("has_children"),
				coalesce(
					field(
						select(
							TASKS_.TIMEZONE
						)
						.from(TASKS_)
						.where(
							TASKS_.SERIES_TASK_ID.equal(instanceId.getTaskId())
							.and(TASKS_.SERIES_INSTANCE_ID.equal(instanceId.getInstance()))
						)
					),
					field(
						select(
							TASKS_.TIMEZONE
						)
						.from(TASKS_)
						.where(
							TASKS_.TASK_ID.equal(instanceId.getTaskId())
						)
					)
				).as("timezone")
			).fetchOneInto(OTaskInstanceInfo.class);
	}
	
	public Set<String> selectOnlineIdsByParent(Connection con, String parentTaskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_.TASK_ID
			)
			.from(TASKS_)
			.where(
				TASKS_.PARENT_TASK_ID.in(parentTaskId)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(TaskBase.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(TaskBase.RevisionStatus.MODIFIED)))
				)
			)
			.fetchSet(TASKS_.TASK_ID);
	}
	
	public String selectIdBySeriesInstance(Connection con, String seriesTaskId, String seriesInstance) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_.TASK_ID
			)
			.from(TASKS_)
			.where(
				TASKS_.SERIES_TASK_ID.in(seriesTaskId)
				.and(TASKS_.SERIES_INSTANCE_ID.in(seriesInstance))
			)
			.fetchOne(0, String.class);
	}
	
	public OTask selectById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS_)
			.where(TASKS_.TASK_ID.equal(taskId))
			.fetchOneInto(OTask.class);
	}
	
	public OChildrenCount selectCountByParent(Connection con, String parentTaskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl.select(
				field(
					DSL.selectCount()
					.from(TASKS_)
					.where(
						TASKS_.PARENT_TASK_ID.equal(parentTaskId)
					)
				).as("total_count"),
				field(
					DSL.selectCount()
					.from(TASKS_)
					.where(
						TASKS_.PARENT_TASK_ID.equal(parentTaskId)
						.and(TASKS_.STATUS.equal(EnumUtils.toSerializedName(Task.Status.COMPLETED)))
					)
				).as("completed_count")
			).fetchOneInto(OChildrenCount.class);
	}
	
	public OTask selectOnlineById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS_)
			.where(
				TASKS_.TASK_ID.equal(taskId)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(TaskBase.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(TaskBase.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(OTask.class);
	}
	
	public Integer selectCategoryId(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_.CATEGORY_ID
			)
			.from(TASKS_)
			.where(TASKS_.TASK_ID.equal(taskId))
			.fetchOneInto(Integer.class);
	}
	
	public Map<String, Integer> selectCategoriesByIds(Connection con, Collection<String> taskIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.CATEGORY_ID
			)
			.from(TASKS_)
			.where(
				TASKS_.TASK_ID.in(taskIds)
			)
			.fetchMap(TASKS_.TASK_ID, TASKS_.CATEGORY_ID);
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
			rangeCndt = TASKS_.START.between(rangeFrom, rangeTo); // Events that start in current range
		}
		
		// New field: has children
		Tasks TT1 = TASKS_.as("ta1");
		Field<Boolean> hasChildren = field(exists(
				selectOne()
				.from(TT1)
				.where(
					TT1.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
				)
			)).as("has_children");
		
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.ORGANIZER,
				TASKS_.ORGANIZER_ID,
				TASKS_.PUBLIC_UID,
				TASKS_.SUBJECT,
				TASKS_.TIMEZONE,
				TASKS_.START,
				TASKS_.DUE,
				TASKS_.REMINDER,
				TASKS_.REMINDED_ON
			)
			.select(
				hasChildren,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
				.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				.and(
					TASKS_.REMINDER.isNotNull().and(TASKS_.REMINDED_ON.isNull()) // Note this NULL test on REMINDED_ON field!!!
				)
				.and(recurCndt)
				.and(rangeCndt)
			)
			.orderBy(
				TASKS_.START.asc()
			)
			.forUpdate()
			.fetchInto(VTaskLookup.class);
	}
	
	public VTaskObject viewTaskObjectById(Connection con, Collection<Integer> categoryIds, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition catCndt = DSL.trueCondition();
		if (categoryIds != null && categoryIds.size() > 0) {
			catCndt = TASKS_.CATEGORY_ID.in(categoryIds);
		}
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
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
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(TASKS_ICALENDARS).on(TASKS_.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID))
			.where(
				TASKS_.TASK_ID.equal(taskId)
				.and(catCndt)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
			)
			.fetchOneInto(VTaskObject.class);
	}
	
	public Map<String, List<VTaskObject>> viewOnlineTaskObjectsByCategory(Connection con, boolean stat, int categoryId) throws DAOException {
		return viewOnlineTaskObjectsByCategoryHrefsSince(con, stat, categoryId, null, null);
	}
	
	public Map<String, List<VTaskObject>> viewOnlineTaskObjectsByCategoryHrefs(Connection con, boolean stat, int categoryId, Collection<String> hrefs) throws DAOException {
		return viewOnlineTaskObjectsByCategoryHrefsSince(con, stat, categoryId, hrefs, null);
	}
	
	public Map<String, List<VTaskObject>> viewOnlineTaskObjectsByCategorySince(Connection con, boolean stat, int categoryId, DateTime since) throws DAOException {
		return viewOnlineTaskObjectsByCategoryHrefsSince(con, stat, categoryId, null, since);
	}
	
	public Map<String, List<VTaskObject>> viewOnlineTaskObjectsByCategoryHrefsSince(Connection con, boolean stat, int categoryId, Collection<String> hrefs, DateTime since) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition inHrefsCndt = DSL.trueCondition();
		if (hrefs != null) {
			inHrefsCndt = TASKS_.HREF.in(hrefs);
		}
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (since != null) ? DSL.value(since) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(TASKS_.START, null, TASKS_RECURRENCES.RULE, rangeFromPar, null);
		
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
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(TASKS_ICALENDARS).on(TASKS_.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID))
			.where(
				TASKS_.CATEGORY_ID.equal(categoryId)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(inHrefsCndt)
				.and(
					TASKS_.START.isNull().or(overlaps)
				)
			)
			.orderBy(
				TASKS_.TASK_ID.asc()
			)
			.fetchGroups(TASKS_.HREF, VTaskObject.class);
	}
	
	public void lazy_viewChangedTaskObjects(Connection con, Collection<Integer> categoryIds, Condition condition, boolean statFields, int limit, int offset, VTaskObjectChanged.Consumer consumer) throws DAOException, WTException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS_.CONTACT_ID)
			).asField("tags");
		
		// New field: has attachments
		Field<Boolean> hasAttachments = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(TASKS_ATTACHMENTS)
				.where(TASKS_ATTACHMENTS.TASK_ID.equal(TASKS_.TASK_ID))
			)).as("has_attachments");
		
		// New field: has custom values
		Field<Boolean> hasCustomValues = DSL.field(DSL.exists(
			DSL.selectOne()
				.from(TASKS_CUSTOM_VALUES)
				.where(TASKS_CUSTOM_VALUES.TASK_ID.equal(TASKS_.TASK_ID))
			)).as("has_custom_values");
		
		// New field: has vcard
		Field<Boolean> hasICalendar = DSL.nvl2(TASKS_ICALENDARS.TASK_ID, true, false).as("has_icalendar");
		
		Cursor<Record> cursor = dsl
			.select(
				HISTORY_TASKS.CHANGE_TIMESTAMP,
				HISTORY_TASKS.CHANGE_TYPE
			)
			.select(
				getVTaskObjectFields(statFields)
			)
			.select(
				tags,
				hasRecurrence,
				hasAttachments,
				hasCustomValues,
				hasICalendar
			)
			.distinctOn(HISTORY_TASKS.TASK_ID)
			.from(HISTORY_TASKS)
			.leftOuterJoin(TASKS_).on(HISTORY_TASKS.TASK_ID.equal(TASKS_.TASK_ID))
			.leftOuterJoin(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(TASKS_ICALENDARS).on(TASKS_.TASK_ID.equal(TASKS_ICALENDARS.TASK_ID))
			.where(
				HISTORY_TASKS.CATEGORY_ID.in(categoryIds)
				.and(filterCndt)
			)
			.orderBy(
				HISTORY_TASKS.TASK_ID.asc(),
				HISTORY_TASKS.ID.desc()
			)
			.limit(limit)
			.offset(offset)
			.fetchLazy();
		
		try {
			for(;;) {
				VTaskObjectChanged vtoc = cursor.fetchNextInto(VTaskObjectChanged.class);
				if (vtoc == null) break;
				consumer.consume(vtoc, con);
			}
		} finally {
			cursor.close();
		}
	}
	
	public int countByCategoryCondition(Connection con, Collection<Integer> categoryIds, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		return dsl
			.selectCount()
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					filterCndt
				)
			)
			.fetchOne(0, Integer.class);
	}
	
	public boolean existByCategoryCondition(Connection con, Collection<Integer> categoryIds, Condition condition) throws DAOException {
		DSLContext dsl = getDSL(con);
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		return dsl.fetchExists(
			dsl.selectOne()
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					filterCndt
				)
			)
		);
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
			.rruleEventOverlaps(TASKS_.START, null, TASKS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: has children
		Tasks TT1 = TASKS_.as("ta1");
		Field<Boolean> hasChildren = field(exists(
				selectOne()
				.from(TT1)
				.where(
					TT1.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
				)
			)).as("has_children");
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
			).asField("tags");
		
		// New field: parent is not null
		Tasks PTT1 = TASKS_.as("pta1");
		Field<Boolean> parentNotNull = DSL.field(PTT1.TASK_ID.isNotNull());
		
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.ORGANIZER,
				TASKS_.ORGANIZER_ID,
				TASKS_.PUBLIC_UID,
				TASKS_.SUBJECT,
				TASKS_.DESCRIPTION,
				TASKS_.DESCRIPTION_TYPE,
				TASKS_.TIMEZONE,
				TASKS_.START,
				TASKS_.DUE,
				TASKS_.COMPLETED_ON,
				TASKS_.PROGRESS,
				TASKS_.STATUS,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.DOCUMENT_REF,
				TASKS_.REMINDER				
			)
			.select(
				hasRecurrence,
				hasChildren,
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.leftOuterJoin(PTT1).on(TASKS_.PARENT_TASK_ID.equal(PTT1.TASK_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					TASKS_.START.isNull()
					.or(overlaps)
				)
				.and(filterCndt)
			)
			.orderBy(
				DSL.coalesce(PTT1.TASK_ID, TASKS_.TASK_ID),
				parentNotNull,
				TASKS_.TASK_ID
			)
			.fetchInto(VTaskLookup.class);
	}
	
	public List<VTaskLookup> viewOnlineExpiredByRangeForUpdate(Connection con, DateTime rangeFrom, DateTime rangeTo) {
		DSLContext dsl = getDSL(con);
		
		// New field: overlaps
		Param<DateTime> rangeFromPar = (rangeFrom != null) ? DSL.value(rangeFrom) : null;
		Param<DateTime> rangeToPar = (rangeTo != null) ? DSL.value(rangeTo) : null;
		Field<Boolean> overlaps = com.sonicle.webtop.core.jooq.public_.Routines
			.rruleEventOverlaps(TASKS_.START, null, TASKS_RECURRENCES.RULE, rangeFromPar, rangeToPar);
		
		// New field: has recurrence reference
		Field<Boolean> hasRecurrence = DSL.nvl2(TASKS_RECURRENCES.TASK_ID, true, false).as("has_recurrence");
		
		// New field: has children
		Tasks TT1 = TASKS_.as("ta1");
		Field<Boolean> hasChildren = field(exists(
				selectOne()
				.from(TT1)
				.where(
					TT1.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
				)
			)).as("has_children");
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.ORGANIZER,
				TASKS_.ORGANIZER_ID,
				TASKS_.PUBLIC_UID,
				TASKS_.SUBJECT,
				TASKS_.DESCRIPTION,
				TASKS_.DESCRIPTION_TYPE,
				TASKS_.TIMEZONE,
				TASKS_.START,
				TASKS_.DUE,
				TASKS_.PROGRESS,
				TASKS_.STATUS,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.REMINDER,
				TASKS_.REMINDED_ON // Important for recurring tasks, see WHERE part below!
			)
			.select(
				hasRecurrence,
				hasChildren,
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				.and(TASKS_.START.isNotNull())
				.and(
					TASKS_.REMINDER.isNotNull().and(
						TASKS_RECURRENCES.TASK_ID.isNull().and(TASKS_.REMINDED_ON.isNull()) // Normal tasks: REMINDED_ON must be null!
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
			rangeCndt = TASKS_.START_DATE.between(rangeFrom, rangeTo); // Events that start in current range
		}
		Condition filterCndt = (condition != null) ? condition : DSL.trueCondition();
		
		// New field: tags list
		Field<String> tags = DSL
			.select(DSL.groupConcat(TASKS_TAGS.TAG_ID, "|"))
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.OWNER_ID,
				TASKS_.OWNER,
				TASKS_.PUBLIC_UID,
				TASKS_.SUBJECT,
				TASKS_.BODY,
				TASKS_.BODY_TYPE,
				TASKS_.START_DATE,
				TASKS_.DUE_DATE,
				TASKS_.COMPLETION_PERC,
				TASKS_.STATUS,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.REMINDER
			)
			.select(
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS_RECURRENCES.TASK_ID.isNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				TASKS_.START_DATE.asc(),
				TASKS_.SUBJECT.asc()
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
				TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.OWNER_ID,
				TASKS_.OWNER,
				TASKS_.PUBLIC_UID,
				TASKS_.SUBJECT,
				TASKS_.BODY,
				TASKS_.BODY_TYPE,
				TASKS_.START_DATE,
				TASKS_.DUE_DATE,
				TASKS_.COMPLETION_PERC,
				TASKS_.STATUS,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.REMINDER
			)
			.select(
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.leftOuterJoin(TASKS_RECURRENCES).on(TASKS_.TASK_ID.equal(TASKS_RECURRENCES.TASK_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS_RECURRENCES.TASK_ID.isNotNull())
				.and(rangeCndt)
				.and(filterCndt)
			)
			.orderBy(
				TASKS_.START_DATE.asc(),
				TASKS_.SUBJECT.asc()
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
				TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
			).asField("tags");
		
		return dsl
			.select(
				TASKS_.TASK_ID,
				TASKS_.PUBLIC_UID,
				TASKS_.CATEGORY_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.SUBJECT,
				TASKS_.BODY,
				TASKS_.BODY_TYPE,
				//TASKS_.TIMEZONE, // Timezone can't be changed, it's set at creation!
				TASKS_.START,
				TASKS_.DUE,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.STATUS,
				TASKS_.COMPLETION_PERC
				//TASKS_.REMINDER_DATE
			)
			.select(
				tags,
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					filterCndt
				)
			)
			.orderBy(
				TASKS_.SUBJECT.asc()
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
				TASKS_.TASK_ID,
				TASKS_.PUBLIC_UID,
				TASKS_.CATEGORY_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.SUBJECT,
				TASKS_.DESCRIPTION,
				TASKS_.DESCRIPTION_TYPE,
				TASKS_.START,
				TASKS_.DUE,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.STATUS,
				TASKS_.PROGRESS
				//TASKS_.REMINDER_DATE
			)
			.select(
				CATEGORIES.NAME.as("category_name"),
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS_.CATEGORY_ID.in(categoryIds)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(
					TASKS_.DUE.isNotNull()
					.and(TASKS_.STATUS.notIn(EnumUtils.toSerializedName(Task.Status.COMPLETED), EnumUtils.toSerializedName(Task.Status.CANCELLED)))
				)
				.and(
					patternCndt
				)
			)
			.orderBy(
				TASKS_.DUE.asc()
			)
			.fetchInto(VTaskLookup.class);
	}	
	
	public List<String> selectOnlineIdsByCategoryHrefs(Connection con, int categoryId, String href) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_.TASK_ID
			)
			.from(TASKS_)
			.join(CATEGORIES).on(TASKS_.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS_.CATEGORY_ID.equal(categoryId)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS_.HREF.equal(href))
			)
			.orderBy(
				TASKS_.TASK_ID.asc()
			)
			.fetchInto(String.class);
	}
	
	public Map<Integer, DateTime> selectMaxRevTimestampByCategories(Connection con, Collection<Integer> categoryIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				HISTORY_TASKS.CATEGORY_ID,
				DSL.max(HISTORY_TASKS.CHANGE_TIMESTAMP)
			)
			.from(HISTORY_TASKS)
			.where(
				HISTORY_TASKS.CATEGORY_ID.in(categoryIds)
			)
			.groupBy(
				HISTORY_TASKS.CATEGORY_ID
			)
			.fetchMap(HISTORY_TASKS.CATEGORY_ID, DSL.max(HISTORY_TASKS.CHANGE_TIMESTAMP));
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
		TasksRecord record = dsl.newRecord(TASKS_, item);
		return dsl
			.insertInto(TASKS_)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OTask item, boolean setContactRef, boolean setDocRef, boolean clearRemindedOn, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED));
		item.setRevisionTimestamp(revisionTimestamp);
		
		UpdateSetMoreStep update = dsl
			.update(TASKS_)
			.set(TASKS_.CATEGORY_ID, item.getCategoryId())
			.set(TASKS_.REVISION_STATUS, item.getRevisionStatus())
			.set(TASKS_.REVISION_TIMESTAMP, item.getRevisionTimestamp())
			.set(TASKS_.SUBJECT, item.getSubject())
			.set(TASKS_.LOCATION, item.getLocation())
			.set(TASKS_.DESCRIPTION, item.getDescription())
			.set(TASKS_.DESCRIPTION_TYPE, item.getDescriptionType())
			.set(TASKS_.START, item.getStart())
			.set(TASKS_.DUE, item.getDue())
			.set(TASKS_.COMPLETED_ON, item.getCompletedOn())
			.set(TASKS_.PROGRESS, item.getProgress())
			.set(TASKS_.STATUS, item.getStatus())
			.set(TASKS_.IMPORTANCE, item.getImportance())
			.set(TASKS_.IS_PRIVATE, item.getIsPrivate())
			.set(TASKS_.REMINDER, item.getReminder());
		
		if (setDocRef) {
			update = update
				.set(TASKS_.DOCUMENT_REF, item.getDocumentRef());
		} else {
			item.setDocumentRef(null);
		}
		if (setContactRef) {
			update = update
				.set(TASKS_.CONTACT, item.getContact())
				.set(TASKS_.CONTACT_ID, item.getContactId());
		} else {
			item.setContact(null);
			item.setContactId(null);
		}
		if (clearRemindedOn) {
			update = update
				.set(TASKS_.REMINDED_ON, (DateTime)null);
		}
		
		return update
			.where(
				TASKS_.TASK_ID.equal(item.getTaskId())
			)
			.execute();
	}
	
	public int updateCategory(Connection con, String taskId, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_)
			.set(TASKS_.CATEGORY_ID, categoryId)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int updateRemindedOn(Connection con, String taskId, DateTime remindedOn) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_)
			.set(TASKS_.REMINDED_ON, remindedOn)
			.where(
				TASKS_.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public List<String> updateOnlineSubjectsBySeries(Connection con, String seriesTaskId, String oldSubject, String newSubject, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS_)
			.set(TASKS_.SUBJECT, newSubject)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.SERIES_TASK_ID.equal(seriesTaskId)
				.and(
					TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.NEW))
					.or(TASKS_.REVISION_STATUS.equal(EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED)))
				)
				.and(TASKS_.SUBJECT.equal(oldSubject))
			)
			.returning(
				TASKS_.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toList());
	}
	
	public List<String> updateCategoryBySeries(Connection con, String seriesTaskId, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS_)
			.set(TASKS_.CATEGORY_ID, categoryId)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.TASK_ID.equal(seriesTaskId)
					.or(TASKS_.SERIES_TASK_ID.equal(seriesTaskId))
				.and(TASKS_.CATEGORY_ID.notEqual(categoryId))
			)
			.returning(
				TASKS_.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toList());
	}
	
	public int updateParentProgressByChild(Connection con, String childTaskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Tasks TASKS_1 = TASKS_.as("t1");
		Tasks TASKS_2 = TASKS_.as("t2");
		return dsl
			.update(TASKS_)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.set(TASKS_.PROGRESS, 
				DSL.select(
					avg(TASKS_1.PROGRESS)
				)
				.from(TASKS_1)
				.where(
					TASKS_1.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
				).asField()
			)
			.where(
				TASKS_.TASK_ID.equal(
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
			.update(TASKS_)
			.set(TASKS_.CATEGORY_ID, categoryId)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.PARENT_TASK_ID.equal(parentTaskId)
				.and(TASKS_.CATEGORY_ID.notEqual(categoryId))
			)
			.returning(
				TASKS_.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toSet());
	}
	
	public Set<String> updateCompletedByParent(Connection con, String parentTaskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS_)
			.set(TASKS_.STATUS, EnumUtils.toSerializedName(Task.Status.COMPLETED))
			.set(TASKS_.COMPLETED_ON, revisionTimestamp)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(	
				TASKS_.PARENT_TASK_ID.equal(parentTaskId)
				.and(TASKS_.STATUS.notEqual(EnumUtils.toSerializedName(Task.Status.COMPLETED)))
			)
			.returning(
				TASKS_.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toSet());
	}
	
	public int updateParentProgressByParent(Connection con, String parentTaskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Tasks TASKS_1 = TASKS_.as("t1");
		return dsl
			.update(TASKS_)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(Task.RevisionStatus.MODIFIED))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.set(TASKS_.PROGRESS, 
				DSL.select(
					avg(TASKS_1.PROGRESS)
				)
				.from(TASKS_1)
				.where(
					TASKS_1.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
				).asField()
					
				//sum.divide(count).cast(Short.class)
				/*
				DSL.select(nvl(sum(TASKS_1.PROGRESS), 0))
				.from(TASKS_1)
				.where(
					TASKS_1.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
				)
				.asField()
				.divide(
					DSL.selectCount()
					.from(TASKS_2)
					.where(
						TASKS_2.PARENT_TASK_ID.equal(TASKS_.TASK_ID)
					)
					.asField()
				).cast(Short.class)
				*/
			)
			.where(
				TASKS_.TASK_ID.equal(parentTaskId)
			)
			.execute();
	}
	
	public int updateRevision(Connection con, String taskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_)
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int updateRevisionStatus(Connection con, String taskId, Task.RevisionStatus revisionStatus, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_)
			.set(TASKS_.REVISION_STATUS, EnumUtils.toSerializedName(revisionStatus))
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int deleteById(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_)
			.where(
				TASKS_.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int deleteByCategoryId(Connection con, int categoryId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_)
			.where(
				TASKS_.CATEGORY_ID.equal(categoryId)
			)
			.execute();
	}
	
	public int logicDeleteById(Connection con, String taskId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_)
			.set(TASKS_.PARENT_TASK_ID, (String)null)
			.set(TASKS_.REVISION_STATUS, DELETED)
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.TASK_ID.equal(taskId)
				.and(TASKS_.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	public Set<String> logicDeleteBySeries(Connection con, String seriesTaskId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS_)
			.set(TASKS_.PARENT_TASK_ID, (String)null)
			.set(TASKS_.REVISION_STATUS, DELETED)
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(	
				TASKS_.SERIES_TASK_ID.equal(seriesTaskId)
				.and(TASKS_.REVISION_STATUS.notEqual(DELETED))
			)
			.returning(
				TASKS_.TASK_ID
			)
			.fetch();
		
		return result.stream()
			.map(rec -> rec.getTaskId())
			.collect(Collectors.toSet());
	}
	
	public Set<String> logicDeleteByParent(Connection con, String parentTaskId, DateTime revisionTimestamp) throws DAOException {
		final String DELETED = EnumUtils.toSerializedName(Task.RevisionStatus.DELETED);
		DSLContext dsl = getDSL(con);
		Result<TasksRecord> result = dsl
			.update(TASKS_)
			.set(TASKS_.PARENT_TASK_ID, (String)null)
			.set(TASKS_.REVISION_STATUS, DELETED)
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(	
				TASKS_.PARENT_TASK_ID.equal(parentTaskId)
				.and(TASKS_.REVISION_STATUS.notEqual(DELETED))
			)
			.returning(
				TASKS_.TASK_ID
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
			.update(TASKS_)
			.set(TASKS_.PARENT_TASK_ID, (String)null)
			.set(TASKS_.REVISION_STATUS, DELETED)
			.set(TASKS_.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS_.CATEGORY_ID.equal(categoryId)
				.and(TASKS_.REVISION_STATUS.notEqual(DELETED))
			)
			.execute();
	}
	
	private Condition toSearchPatternCondition(String pattern) {
		Condition cndt = DSL.trueCondition();
		if (!StringUtils.isBlank(pattern)) {
			return TASKS_.SUBJECT.likeIgnoreCase(pattern)
					.or(TASKS_.DESCRIPTION.likeIgnoreCase(pattern));
		}
		return cndt;
	}
	
	private Field[] getVTaskObjectFields(boolean stat) {
		if (stat) {
			return new Field[]{
				TASKS_.TASK_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.PUBLIC_UID,
				TASKS_.HREF
			};
		} else {
			return new Field[]{
				TASKS_.TASK_ID,
				TASKS_.CATEGORY_ID,
				TASKS_.SERIES_TASK_ID,
				TASKS_.SERIES_INSTANCE_ID,
				TASKS_.PARENT_TASK_ID,
				TASKS_.REVISION_STATUS,
				TASKS_.REVISION_TIMESTAMP,
				TASKS_.REVISION_SEQUENCE,
				TASKS_.CREATION_TIMESTAMP,
				TASKS_.ORGANIZER,
				TASKS_.ORGANIZER_ID,
				TASKS_.PUBLIC_UID,
				TASKS_.SUBJECT,
				TASKS_.DESCRIPTION,
				TASKS_.DESCRIPTION_TYPE,
				TASKS_.TIMEZONE,
				TASKS_.START,
				TASKS_.DUE,
				TASKS_.PROGRESS,
				TASKS_.COMPLETED_ON,
				TASKS_.STATUS,
				TASKS_.IMPORTANCE,
				TASKS_.IS_PRIVATE,
				TASKS_.REMINDER,
				TASKS_.HREF
			};
		}
	}
	
	public static Condition createChangedTasksNewOrModifiedCondition() {
		return HISTORY_TASKS.CHANGE_TYPE.equal(BaseDAO.CHANGE_TYPE_CREATION)
			.or(HISTORY_TASKS.CHANGE_TYPE.equal(BaseDAO.CHANGE_TYPE_UPDATE));
	}
	
	public static Condition createChangedTasksSinceUntilCondition(DateTime since, DateTime until) {
		return HISTORY_TASKS.CHANGE_TIMESTAMP.greaterThan(since)
			.and(HISTORY_TASKS.CHANGE_TIMESTAMP.lessThan(until));
	}
	
	public static Condition createCondition(ITasksManager.TaskListView view, DateTime today) {
		if (ITasksManager.TaskListView.ALL.equals(view)) {
			return TASKS_.SERIES_TASK_ID.isNull()
				.and(TASKS_.SERIES_INSTANCE_ID.isNull());
			
		} else if (ITasksManager.TaskListView.TODAY.equals(view)) {
			DateTime todayAtBeginning = today.withTimeAtStartOfDay();
			return TASKS_.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
				.or(
					TASKS_.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
					.and(TASKS_.COMPLETED_ON.greaterOrEqual(todayAtBeginning))
					.and(TASKS_.COMPLETED_ON.lessThan(todayAtBeginning.plusDays(1)))
				);
			
		} else if (ITasksManager.TaskListView.NEXT_7.equals(view)) {
			DateTime todayAtBeginning = today.withTimeAtStartOfDay();
			return TASKS_.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
				.or(
					TASKS_.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED))
					.and(TASKS_.COMPLETED_ON.greaterOrEqual(todayAtBeginning))
					.and(TASKS_.COMPLETED_ON.lessThan(todayAtBeginning.plusDays(1)))
				);
			
		} else if (ITasksManager.TaskListView.NOT_STARTED.equals(view)) {
			return TASKS_.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.NEEDS_ACTION));
			
		} else if (ITasksManager.TaskListView.LATE.equals(view)) {
			return TASKS_.COMPLETED_ON.isNull()
				.and(TASKS_.DUE.lessThan(DateTime.now()));
			
		} else if (ITasksManager.TaskListView.COMPLETED.equals(view)) {
			return TASKS_.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
			
		} else if (ITasksManager.TaskListView.NOT_COMPLETED.equals(view)) {
			return TASKS_.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
			
		} else if (ITasksManager.TaskListView.UPCOMING.equals(view)) {
			return TASKS_.DUE.isNotNull()
				.and(TASKS_.STATUS.notIn(EnumUtils.toSerializedName(Task.Status.COMPLETED), EnumUtils.toSerializedName(Task.Status.CANCELLED)));
			
		} else {
			return null;
		}
	}
}
