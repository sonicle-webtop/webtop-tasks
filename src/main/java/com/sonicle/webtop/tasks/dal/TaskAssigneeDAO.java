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
import com.sonicle.webtop.tasks.bol.OTaskAssignee;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_ASSIGNEES;
import com.sonicle.webtop.tasks.jooq.tables.records.TasksAssigneesRecord;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class TaskAssigneeDAO extends BaseDAO {
	private final static TaskAssigneeDAO INSTANCE = new TaskAssigneeDAO();
	public static TaskAssigneeDAO getInstance() {
		return INSTANCE;
	}
	
	public List<OTaskAssignee> selectByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS_ASSIGNEES)
			.where(
				TASKS_ASSIGNEES.TASK_ID.equal(taskId)
			)
			.orderBy(
				TASKS_ASSIGNEES.RECIPIENT.asc()
			)
			.fetchInto(OTaskAssignee.class);
	}
	
	public int insert(Connection con, OTaskAssignee item) throws DAOException {
		DSLContext dsl = getDSL(con);
		TasksAssigneesRecord record = dsl.newRecord(TASKS_ASSIGNEES, item);
		return dsl
			.insertInto(TASKS_ASSIGNEES)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OTaskAssignee item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_ASSIGNEES)
			.set(TASKS_ASSIGNEES.RECIPIENT, item.getRecipient())
			.set(TASKS_ASSIGNEES.RECIPIENT_USER_ID, item.getRecipientUserId())
			.set(TASKS_ASSIGNEES.RESPONSE_STATUS, item.getResponseStatus())
			.where(
				TASKS_ASSIGNEES.ASSIGNEE_ID.equal(item.getAssigneeId())
				.and(TASKS_ASSIGNEES.TASK_ID.equal(item.getTaskId()))
			)
			.execute();
	}
	
	public int updateAssigneeResponseByTask(Connection con, String responseStatus, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(TASKS_ASSIGNEES)
			.set(TASKS_ASSIGNEES.RESPONSE_STATUS, responseStatus)
			.where(
				TASKS_ASSIGNEES.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int deleteByIdTask(Connection con, String assigneeId, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_ASSIGNEES)
			.where(
				TASKS_ASSIGNEES.ASSIGNEE_ID.equal(assigneeId)
				.and(TASKS_ASSIGNEES.TASK_ID.equal(taskId))
			)
			.execute();
	}
	
	public int deleteByIdsTask(Connection con, Collection<String> assigneeIds, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_ASSIGNEES)
			.where(
				TASKS_ASSIGNEES.ASSIGNEE_ID.in(assigneeIds)
				.and(TASKS_ASSIGNEES.TASK_ID.equal(taskId))
			)
			.execute();
	}
}
