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
package com.sonicle.webtop.tasks.dal;

import static com.sonicle.webtop.tasks.jooq.Sequences.SEQ_TASKS;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.VTask;
import static com.sonicle.webtop.tasks.jooq.Tables.CATEGORIES;
import com.sonicle.webtop.tasks.jooq.tables.records.TasksRecord;
import java.sql.Connection;
import java.util.List;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.DSLContext;

/**
 *
 * @author rfullone
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

	public List<VTask> viewByCategoryPattern(Connection con, int categoryId, String pattern) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		Condition searchCndt = null;
		searchCndt = TASKS.SUBJECT.likeIgnoreCase(pattern)
				.or(TASKS.DESCRIPTION.likeIgnoreCase(pattern));
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.CATEGORY_ID,
				TASKS.PUBLIC_UID,
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
				CATEGORIES.DOMAIN_ID.as("category_domain_id"),
				CATEGORIES.USER_ID.as("category_user_id")
			)
			.from(TASKS)
			.join(CATEGORIES).on(TASKS.CATEGORY_ID.equal(CATEGORIES.CATEGORY_ID))
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
				.and(
					TASKS.REVISION_STATUS.equal(OTask.REV_STATUS_NEW)
					.or(TASKS.REVISION_STATUS.equal(OTask.REV_STATUS_MODIFIED))
				)
				.and(
					searchCndt
				)
			)
			.orderBy(
				TASKS.SUBJECT.asc()
			)
			.fetchInto(VTask.class);
	}

	public List<VTask> viewExpridedForUpdateByUntil(Connection con, DateTime until) throws DAOException {
		DSLContext dsl = getDSL(con);
		
		return dsl
			.select(
				TASKS.TASK_ID,
				TASKS.CATEGORY_ID,
				TASKS.SUBJECT,
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
					TASKS.REVISION_STATUS.equal(OTask.REV_STATUS_NEW)
					.or(TASKS.REVISION_STATUS.equal(OTask.REV_STATUS_MODIFIED))
				)
				.and(TASKS.REMINDER_DATE.lessThan(until))
			)
			.orderBy(
				TASKS.REMINDER_DATE.asc(),TASKS.SUBJECT.asc()
			)
			.forUpdate()
			.fetchInto(VTask.class);
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
	
	public int insert(Connection con, OTask item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(OTask.REV_STATUS_NEW);
		item.setRevisionTimestamp(revisionTimestamp);
		TasksRecord record = dsl.newRecord(TASKS, item);
		return dsl
			.insertInto(TASKS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OTask item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionStatus(OTask.REV_STATUS_MODIFIED);
		item.setRevisionTimestamp(revisionTimestamp);
		return dsl
			.update(TASKS)
			.set(TASKS.CATEGORY_ID,item.getCategoryId())
			.set(TASKS.REVISION_STATUS,item.getRevisionStatus())
			.set(TASKS.REVISION_TIMESTAMP,item.getRevisionTimestamp())
			.set(TASKS.SUBJECT,item.getSubject())
			.set(TASKS.DESCRIPTION,item.getDescription())
			.set(TASKS.START_DATE,item.getStartDate())
			.set(TASKS.DUE_DATE,item.getDueDate())
			.set(TASKS.COMPLETED_DATE,item.getCompletedDate())
			.set(TASKS.IMPORTANCE,item.getImportance())
			.set(TASKS.IS_PRIVATE,item.getIsPrivate())
			.set(TASKS.STATUS,item.getStatus())
			.set(TASKS.COMPLETION_PERCENTAGE,item.getCompletionPercentage())
			.set(TASKS.REMINDER_DATE,item.getReminderDate())
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
			.set(TASKS.REVISION_STATUS, OTask.REV_STATUS_MODIFIED)
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
	
	public int updateRevisionStatus(Connection con, int taskId, String revisionStatus, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, revisionStatus)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int logicDeleteById(Connection con, int taskId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, OTask.REV_STATUS_DELETED)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.TASK_ID.equal(taskId)
				.and(TASKS.REVISION_STATUS.notEqual(OTask.REV_STATUS_DELETED))
			)
			.execute();
	}
	
	public int logicDeleteByCategoryId(Connection con, int categoryId, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS)
			.set(TASKS.REVISION_STATUS, OTask.REV_STATUS_DELETED)
			.set(TASKS.REVISION_TIMESTAMP, revisionTimestamp)
			.where(
				TASKS.CATEGORY_ID.equal(categoryId)
				.and(TASKS.REVISION_STATUS.notEqual(OTask.REV_STATUS_DELETED))
			)
			.execute();
	}
}
