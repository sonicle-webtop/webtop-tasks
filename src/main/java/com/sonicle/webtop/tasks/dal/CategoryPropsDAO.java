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
import com.sonicle.webtop.tasks.bol.OCategoryPropSet;
import static com.sonicle.webtop.tasks.jooq.Tables.CATEGORY_PROPS;
import com.sonicle.webtop.tasks.jooq.tables.records.CategoryPropsRecord;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import org.jooq.DSLContext;

/**
 *
 * @author malbinola
 */
public class CategoryPropsDAO extends BaseDAO {
	private final static CategoryPropsDAO INSTANCE = new CategoryPropsDAO();
	public static CategoryPropsDAO getInstance() {
		return INSTANCE;
	}
	
	public Map<Integer, OCategoryPropSet> selectByProfileCategoryIn(Connection con, String domainId, String userId, Collection<Integer> categoryIds) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CATEGORY_PROPS)
			.where(
				CATEGORY_PROPS.DOMAIN_ID.equal(domainId)
				.and(CATEGORY_PROPS.USER_ID.equal(userId))
				.and(CATEGORY_PROPS.CATEGORY_ID.in(categoryIds))
			)
			.fetchMap(CATEGORY_PROPS.CATEGORY_ID, OCategoryPropSet.class);
	}
	
	public OCategoryPropSet selectByProfileCategory(Connection con, String domainId, String userId, int categoryId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.select()
			.from(CATEGORY_PROPS)
			.where(
				CATEGORY_PROPS.DOMAIN_ID.equal(domainId)
				.and(CATEGORY_PROPS.USER_ID.equal(userId))
				.and(CATEGORY_PROPS.CATEGORY_ID.equal(categoryId))
			)
			.fetchOneInto(OCategoryPropSet.class);
	}
	
	public int insert(Connection con, OCategoryPropSet item) throws DAOException {
		DSLContext dsl = getDSL(con);
		CategoryPropsRecord record = dsl.newRecord(CATEGORY_PROPS, item);
		return dsl
			.insertInto(CATEGORY_PROPS)
			.set(record)
			.execute();
	}
	
	public int update(Connection con, OCategoryPropSet item) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.update(CATEGORY_PROPS)
			.set(CATEGORY_PROPS.HIDDEN, item.getHidden())
			.set(CATEGORY_PROPS.COLOR, item.getColor())
			.set(CATEGORY_PROPS.SYNC, item.getSync())
			.where(
				CATEGORY_PROPS.DOMAIN_ID.equal(item.getDomainId())
				.and(CATEGORY_PROPS.USER_ID.equal(item.getUserId()))
				.and(CATEGORY_PROPS.CATEGORY_ID.equal(item.getCategoryId()))
			)
			.execute();
	}
	
	public int deleteByCategory(Connection con, int categoryId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CATEGORY_PROPS)
			.where(
				CATEGORY_PROPS.CATEGORY_ID.equal(categoryId)
			)
			.execute();
	}
	
	public int deleteByDomain(Connection con, String domainId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CATEGORY_PROPS)
			.where(
				CATEGORY_PROPS.DOMAIN_ID.equal(domainId)
			)
			.execute();
	}
	
	public int deleteByProfile(Connection con, String domainId, String userId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CATEGORY_PROPS)
			.where(
				CATEGORY_PROPS.DOMAIN_ID.equal(domainId)
				.and(CATEGORY_PROPS.USER_ID.equal(userId))
			)
			.execute();
	}
	
	public int deleteByProfileCategory(Connection con, String domainId, String userId, int categoryId) throws DAOException {
		DSLContext dsl = getDSL(con);
		return dsl
			.delete(CATEGORY_PROPS)
			.where(
				CATEGORY_PROPS.DOMAIN_ID.equal(domainId)
				.and(CATEGORY_PROPS.USER_ID.equal(userId))
				.and(CATEGORY_PROPS.CATEGORY_ID.equal(categoryId))
			)
			.execute();
	}
}
