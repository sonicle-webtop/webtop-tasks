/*
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.dal;

import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.tasks.bol.OTaskRecurrence;
import com.sonicle.webtop.tasks.bol.OTaskRecurrenceEx;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_RECURRENCES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_RECURRENCES_EX;
import java.sql.Connection;
import java.util.Collection;
import java.util.Set;
import org.joda.time.LocalDate;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class TaskRecurrenceDAO extends BaseDAO {
	private final static TaskRecurrenceDAO INSTANCE = new TaskRecurrenceDAO();
	public static TaskRecurrenceDAO getInstance() {
		return INSTANCE;
	}
	
	public boolean existsRecurrenceByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(TASKS_RECURRENCES)
			.where(
				TASKS_RECURRENCES.TASK_ID.equal(taskId)
			)
			.fetchOne(0, int.class) == 1;
	}
	
	public OTaskRecurrence selectRecurrenceByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS_RECURRENCES)
			.where(
				TASKS_RECURRENCES.TASK_ID.equal(taskId)
			)
			.fetchOneInto(OTaskRecurrence.class);
	}
	
	public int insertRecurrence(Connection con, OTaskRecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(TASKS_RECURRENCES)
			.set(TASKS_RECURRENCES.TASK_ID, item.getTaskId())
			.set(TASKS_RECURRENCES.START, item.getStart())
			.set(TASKS_RECURRENCES.UNTIL, item.getUntil())
			.set(TASKS_RECURRENCES.RULE, item.getRule())
			.execute();
	}
	
	public int updateRecurrence(Connection con, OTaskRecurrence item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_RECURRENCES)
			.set(TASKS_RECURRENCES.START, item.getStart())
			.set(TASKS_RECURRENCES.UNTIL, item.getUntil())
			.set(TASKS_RECURRENCES.RULE, item.getRule())
			.where(TASKS_RECURRENCES.TASK_ID.equal(item.getTaskId()))
			.execute();
	}
	
	public int deleteRecurrenceByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_RECURRENCES)
			.where(
				TASKS_RECURRENCES.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public boolean existsRecurrenceExByTaskDate(Connection con, String taskId, LocalDate date) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.selectCount()
			.from(TASKS_RECURRENCES_EX)
			.where(
				TASKS_RECURRENCES_EX.TASK_ID.equal(taskId)
				.and(TASKS_RECURRENCES_EX.DATE.equal(date))
			)
			.fetchOne(0, int.class) == 1;
	}
	
	public Set<LocalDate> selectRecurrenceExByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_RECURRENCES_EX.DATE
			)
			.from(TASKS_RECURRENCES_EX)
			.where(
				TASKS_RECURRENCES_EX.TASK_ID.equal(taskId)
			)
			.fetchSet(TASKS_RECURRENCES_EX.DATE);
	}
	
	public int insertRecurrenceEx(Connection con, OTaskRecurrenceEx item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(TASKS_RECURRENCES_EX)
			.set(TASKS_RECURRENCES_EX.TASK_ID, item.getTaskId())
			.set(TASKS_RECURRENCES_EX.DATE, item.getDate())
			.execute();
	}
	
	public int insertRecurrenceEx(Connection con, String taskId, LocalDate date) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(TASKS_RECURRENCES_EX)
			.set(TASKS_RECURRENCES_EX.TASK_ID, taskId)
			.set(TASKS_RECURRENCES_EX.DATE, date)
			.execute();
	}
	
	public int[] batchInsertRecurrenceEx(Connection con, String taskId, Collection<LocalDate> dates) throws DAOException {
		if (dates.isEmpty()) return new int[0];
		DSLContext dsl = getDSL(con);
		BatchBindStep batch = dsl.batch(
			dsl.insertInto(TASKS_RECURRENCES_EX, 
				TASKS_RECURRENCES_EX.TASK_ID, 
				TASKS_RECURRENCES_EX.DATE
			)
			.values((String)null, null)
		);
		for (LocalDate date : dates) {
			batch.bind(
				taskId,
				date
			);
		}
		return batch.execute();
	}
	
	public int deleteRecurrenceExByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_RECURRENCES_EX)
			.where(
				TASKS_RECURRENCES_EX.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int deleteRecurrenceExByTaskDates(Connection con, String taskId, Collection<LocalDate> dates) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_RECURRENCES_EX)
			.where(
				TASKS_RECURRENCES_EX.TASK_ID.equal(taskId)
				.and(TASKS_RECURRENCES_EX.DATE.in(dates))
			)
			.execute();
	}
}
