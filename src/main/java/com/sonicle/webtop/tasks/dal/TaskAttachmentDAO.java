/*
 * Copyright (C) 2018 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.dal;

import com.sonicle.webtop.core.dal.BaseDAO;
import com.sonicle.webtop.core.dal.DAOException;
import com.sonicle.webtop.tasks.bol.OTaskAttachment;
import com.sonicle.webtop.tasks.bol.OTaskAttachmentData;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_ATTACHMENTS_DATA;
import static com.sonicle.webtop.tasks.jooq.tables.TasksAttachments.TASKS_ATTACHMENTS;
import com.sonicle.webtop.tasks.jooq.tables.records.TasksAttachmentsRecord;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import org.joda.time.DateTime;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class TaskAttachmentDAO extends BaseDAO {
	private final static TaskAttachmentDAO INSTANCE = new TaskAttachmentDAO();
	public static TaskAttachmentDAO getInstance() {
		return INSTANCE;
	}
	
	public OTaskAttachment selectByIdTask(Connection con, String attachmentId, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS_ATTACHMENTS)
			.where(
				TASKS_ATTACHMENTS.TASK_ATTACHMENT_ID.equal(attachmentId)
				.and(TASKS_ATTACHMENTS.TASK_ID.equal(taskId))
			)
			.fetchOneInto(OTaskAttachment.class);
	}
	
	public List<OTaskAttachment> selectByTask(Connection con, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(TASKS_ATTACHMENTS)
			.where(
				TASKS_ATTACHMENTS.TASK_ID.equal(taskId)
			)
			.orderBy(
				TASKS_ATTACHMENTS.FILENAME.asc()
			)
			.fetchInto(OTaskAttachment.class);
	}
	
	public int insert(Connection con, OTaskAttachment item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionTimestamp(revisionTimestamp);
		item.setRevisionSequence((short)0);
		TasksAttachmentsRecord record = dsl.newRecord(TASKS_ATTACHMENTS, item);
		return dsl
			.insertInto(TASKS_ATTACHMENTS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OTaskAttachment item, DateTime revisionTimestamp) throws DAOException {
		DSLContext dsl = getDSL(con);
		item.setRevisionTimestamp(revisionTimestamp);
		return dsl
			.update(TASKS_ATTACHMENTS)
			.set(TASKS_ATTACHMENTS.FILENAME, item.getFilename())
			.set(TASKS_ATTACHMENTS.SIZE, item.getSize())
			.set(TASKS_ATTACHMENTS.MEDIA_TYPE, item.getMediaType())
			.where(
				TASKS_ATTACHMENTS.TASK_ATTACHMENT_ID.equal(item.getTaskAttachmentId())
			)
			.execute();
	}
	
	public int deleteByIdTask(Connection con, String attachmentId, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_ATTACHMENTS)
			.where(
				TASKS_ATTACHMENTS.TASK_ATTACHMENT_ID.equal(attachmentId)
				.and(TASKS_ATTACHMENTS.TASK_ID.equal(taskId))
			)
			.execute();
	}
	
	public int deleteByIdsTask(Connection con, Collection<String> attachmentIds, String taskId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_ATTACHMENTS)
			.where(
				TASKS_ATTACHMENTS.TASK_ATTACHMENT_ID.in(attachmentIds)
				.and(TASKS_ATTACHMENTS.TASK_ID.equal(taskId))
			)
			.execute();
	}
	
	public OTaskAttachmentData selectBytesById(Connection con, String attachmentId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select(
				TASKS_ATTACHMENTS_DATA.BYTES
			)
			.from(TASKS_ATTACHMENTS_DATA)
			.where(TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID.equal(attachmentId))
			.fetchOneInto(OTaskAttachmentData.class);
	}
	
	/*
	https://github.com/jOOQ/jOOQ/issues/7319
	
	public int upsertBytes(Connection con, String attachmentId, byte[] bytes) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(TASKS_ATTACHMENTS_DATA)
			.set(TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID, attachmentId)
			.set(TASKS_ATTACHMENTS_DATA.BYTES, bytes)
			.onDuplicateKeyUpdate()
			.set(TASKS_ATTACHMENTS_DATA.BYTES, bytes)
			.execute();
	}
	*/
	
	public int insertBytes(Connection con, String attachmentId, byte[] bytes) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.insertInto(TASKS_ATTACHMENTS_DATA)
			.set(TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID, attachmentId)
			.set(TASKS_ATTACHMENTS_DATA.BYTES, bytes)
			.execute();
	}
	
	public int deleteBytesById(Connection con, String attachmentId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(TASKS_ATTACHMENTS_DATA)
			.where(TASKS_ATTACHMENTS_DATA.TASK_ATTACHMENT_ID.equal(attachmentId))
			.execute();
	}
}
