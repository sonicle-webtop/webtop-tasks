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
import java.sql.Connection;
import java.util.List;
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
				TASKS.SUBJECT,
				TASKS.DESCRIPTION,
				TASKS.START_DATE,
				TASKS.DUE_DATE,
				TASKS.IMPORTANCE,
				TASKS.IS_PRIVATE,
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
	
}
