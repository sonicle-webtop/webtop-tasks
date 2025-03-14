/*
 * Copyright (C) 2025 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2025 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.dal;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.rsql.parser.Operator;
import com.sonicle.webtop.core.app.sdk.JOOQConditionBuildingVisitor;
import static com.sonicle.webtop.tasks.jooq.Tables.CATEGORIES;
import com.sonicle.webtop.tasks.model.CategoryBase;
import com.sonicle.webtop.tasks.model.CategoryQuery;
import java.util.ArrayList;
import java.util.Collection;
import org.jooq.Condition;

/**
 *
 * @author malbinola
 */
public class CategoryConditionBuildingVisitor extends JOOQConditionBuildingVisitor {

	@Override
	protected Condition buildCondition(String fieldName, Operator operator, Collection<?> values) {
		if (CategoryQuery.ID.equals(fieldName)) {
			return defaultCondition(CATEGORIES.CATEGORY_ID, operator, values);
			
		} else if (CategoryQuery.CREATED_ON.equals(fieldName)) {
			return defaultCondition(CATEGORIES.CREATION_TIMESTAMP, operator, values);
			
		} else if (CategoryQuery.UPDATED_ON.equals(fieldName)) {
			return defaultCondition(CATEGORIES.REVISION_TIMESTAMP, operator, values);
			
		} else if (CategoryQuery.USER_ID.equals(fieldName)) {
			return defaultCondition(CATEGORIES.USER_ID, operator, values);
			
		} else if (CategoryQuery.BUILT_IN.equals(fieldName)) {
			return defaultCondition(CATEGORIES.BUILT_IN, operator, values);
			
		} else if (CategoryQuery.NAME.equals(fieldName)) {
			return defaultCondition(CATEGORIES.NAME, operator, values);
		
		} else if (CategoryQuery.DESCRIPTION.equals(fieldName)) {
			return defaultCondition(CATEGORIES.DESCRIPTION, operator, values, DefaultConditionOption.stringICaseComparison());
		
		} else if (CategoryQuery.COLOR.equals(fieldName)) {
			return defaultCondition(CATEGORIES.COLOR, operator, values, DefaultConditionOption.stringICaseComparison());
		
		} else if (CategoryQuery.SYNC.equals(fieldName)) {
			return defaultCondition(CATEGORIES.SYNC, operator, parseSyncValues(values), DefaultConditionOption.stringICaseComparison());
		
		} else if (CategoryQuery.IS_OWNER_DEFAULT.equals(fieldName)) {
			return defaultCondition(CATEGORIES.IS_DEFAULT, operator, values);
			
		} else if (CategoryQuery.DEFAULT_ISPRIVATE.equals(fieldName)) {
			return defaultCondition(CATEGORIES.IS_PRIVATE, operator, values);
		
		} else if (CategoryQuery.DEFAULT_VISIBILITY.equals(fieldName)) {
			return defaultCondition(CATEGORIES.IS_PRIVATE, operator, parseVisibilityValuesAsIsPrivate(values));
		
		} else if (CategoryQuery.DEFAULT_REMINDER.equals(fieldName)) {
			return defaultCondition(CATEGORIES.REMINDER, operator, values);
		}
		
		throw new UnsupportedOperationException("Field not supported: " + fieldName);
	}
	
	private Collection<String> parseSyncValues(Collection<?> values) {
		ArrayList<String> newValues = new ArrayList<>(values.size());
		for (Object value : values) {
			final String svalue = String.valueOf(value);
			final CategoryBase.Sync sync = EnumUtils.forString(svalue, CategoryBase.Sync.class, true);
			if (sync != null) newValues.add(svalue);
		}
		return newValues;
	}
	
	private Collection<Boolean> parseVisibilityValuesAsIsPrivate(Collection<?> values) {
		ArrayList<Boolean> newValues = new ArrayList<>(values.size());
		for (Object value : values) {
			final String svalue = String.valueOf(value);
			final CategoryBase.Visibility visibility = EnumUtils.forString(svalue, CategoryBase.Visibility.class, true);
			if (visibility != null) newValues.add(CategoryBase.Visibility.PRIVATE.equals(visibility));
		}
		return newValues;
	}
}
