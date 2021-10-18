/*
 * Copyright (C) 2020 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2020 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.dal;

import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import com.sonicle.webtop.tasks.jooq.tables.TasksTags;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_TAGS;
import java.sql.Connection;
import java.util.Collection;
import java.util.Set;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author malbinola
 */
public class TaskTagDAO extends BaseDAO {
	private final static TaskTagDAO INSTANCE = new TaskTagDAO();
	public static TaskTagDAO getInstance() {
		return INSTANCE;
	}
	
	public Set<String> selectTagsByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_TAGS.TAG_ID
			)
			.from(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(taskId)
			)
			.orderBy(
				TASKS_TAGS.TAG_ID.asc()
			)
			.fetchSet(TASKS_TAGS.TAG_ID);
	}
	
	public int insert(Connection con, String taskId, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(TASKS_TAGS)
			.set(TASKS_TAGS.TASK_ID, taskId)
			.set(TASKS_TAGS.TAG_ID, tagId)
			.execute();
	}
	
	public int[] batchInsert(Connection con, String taskId, Collection<String> tagIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		BatchBindStep batch = dsl.batch(
			dsl.insertInto(TASKS_TAGS, 
				TASKS_TAGS.TASK_ID, 
				TASKS_TAGS.TAG_ID
			)
			.values((String)null, null)
		);
		for (String tagId : tagIds) {
			batch.bind(
				taskId,
				tagId
			);
		}
		return batch.execute();
	}
	
	public int insertByCategory(Connection con, int categoryId, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		TasksTags tt1 = TASKS_TAGS.as("tt1");
		return dsl
			.insertInto(TASKS_TAGS)
			.select(
				select(
					TASKS.TASK_ID,
					val(tagId, String.class).as("tag_id")
				)
				.from(TASKS)
				.where(
					TASKS.CATEGORY_ID.equal(categoryId)
					.and(TASKS.TASK_ID.notIn(
						select(
							tt1.TASK_ID
						)
						.from(tt1)
						.where(
							tt1.TASK_ID.equal(TASKS.TASK_ID)
							.and(tt1.TAG_ID.equal(tagId))
						)
					))
				)
			)
			.execute();
	}
	
	public int insertByCategoriesTasks(Connection con, Collection<Integer> categoryIds, Collection<String> taskIds, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		TasksTags tt1 = TASKS_TAGS.as("tt1");
		return dsl
			.insertInto(TASKS_TAGS)
			.select(
				select(
					TASKS.TASK_ID,
					val(tagId, String.class).as("tag_id")
				)
				.from(TASKS)
				.where(
					TASKS.CATEGORY_ID.in(categoryIds)
					.and(TASKS.TASK_ID.in(taskIds))
					.and(TASKS.TASK_ID.notIn(
						select(
							tt1.TASK_ID
						)
						.from(tt1)
						.where(
							tt1.TASK_ID.in(taskIds)
							.and(tt1.TAG_ID.equal(tagId))
						)
					))
				)
			)
			.execute();
	}
	
	public int delete(Connection con, String taskId, String tagId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(taskId)
				.and(TASKS_TAGS.TAG_ID.equal(tagId))
			)
			.execute();
	}
	
	public int deleteByIdTags(Connection con, String taskId, Collection<String> tagIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(taskId)
				.and(TASKS_TAGS.TAG_ID.in(tagIds))
			)
			.execute();
	}
	
	public int deleteByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.equal(taskId)
			)
			.execute();
	}
	
	public int deleteByCategory(Connection con, int categoryId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.in(
					select(
						TASKS.TASK_ID
					)
					.from(TASKS)
					.where(
						TASKS.CATEGORY_ID.equal(categoryId)
					)
				)
			)
			.execute();
	}
	
	public int deleteByCategoryTags(Connection con, int categoryId, Collection<String> tagIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.in(
					select(
						TASKS.TASK_ID
					)
					.from(TASKS)
					.where(
						TASKS.CATEGORY_ID.equal(categoryId)
					)
				)
				.and(TASKS_TAGS.TAG_ID.in(tagIds))
			)
			.execute();
	}
	
	public int deleteByCategoriesTasks(Connection con, Collection<Integer> categoryIds, Collection<String> taskIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.in(
					select(
						TASKS.TASK_ID
					)
					.from(TASKS)
					.where(
						TASKS.TASK_ID.in(taskIds)
						.and(TASKS.CATEGORY_ID.in(categoryIds))
					)
				)
			)
			.execute();
	}
	
	public int deleteByCategoriesTasksTags(Connection con, Collection<Integer> categoryIds, Collection<String> taskIds, Collection<String> tagIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_TAGS)
			.where(
				TASKS_TAGS.TASK_ID.in(
					select(
						TASKS.TASK_ID
					)
					.from(TASKS)
					.where(
						TASKS.TASK_ID.in(taskIds)
						.and(TASKS.CATEGORY_ID.in(categoryIds))
					)
				)
				.and(TASKS_TAGS.TAG_ID.in(tagIds))
			)
			.execute();
	}
}
