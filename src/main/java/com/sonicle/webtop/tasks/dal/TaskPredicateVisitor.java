/*
 * Copyright (C) 2019 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2019 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.dal;

import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode;
import com.github.rutledgepaulv.qbuilders.operators.ComparisonOperator;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.json.CompId;
import java.util.Collection;
import org.jooq.Condition;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import com.sonicle.webtop.core.app.sdk.JOOQPredicateVisitorWithCValues;
import com.sonicle.webtop.core.app.sdk.QueryBuilderWithCValues;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_CUSTOM_VALUES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_TAGS;
import com.sonicle.webtop.tasks.model.BaseTask;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author malbinola
 */
public class TaskPredicateVisitor extends JOOQPredicateVisitorWithCValues {

	public TaskPredicateVisitor() {
		super(false);
	}

	@Override
	protected Condition toCondition(String fieldName, ComparisonOperator operator, Collection<?> values, ComparisonNode node) {
		if ("subject".equals(fieldName)) {
			return defaultCondition(TASKS.SUBJECT, operator, values);
			
		} else if ("description".equals(fieldName)) {
			return defaultCondition(TASKS.DESCRIPTION, operator, values);
			
		} else if ("after".equals(fieldName)) {
			DateTime after = (DateTime)single(values);
			return TASKS.START_DATE.greaterOrEqual(after);
			
		} else if ("before".equals(fieldName)) {
			DateTime before = (DateTime)single(values);
			return TASKS.START_DATE.lessThan(before);
			
		} else if ("done".equals(fieldName)) {
			return TASKS.STATUS.equal(EnumUtils.toSerializedName(BaseTask.Status.COMPLETED));
			
		} else if ("private".equals(fieldName)) {
			return defaultCondition(TASKS.IS_PRIVATE, operator, values);
			
		} else if ("tag".equals(fieldName)) {
			return exists(
					selectOne()
					.from(TASKS_TAGS)
					.where(
						TASKS_TAGS.TASK_ID.equal(TASKS.TASK_ID)
						.and(TASKS_TAGS.TAG_ID.equal(singleAsString(values)))
					)
				);
			
		} else if ("any".equals(fieldName)) {
			String singleAsString = valueToLikePattern(singleAsString(values));
			return TASKS.SUBJECT.likeIgnoreCase(singleAsString)
				.or(TASKS.DESCRIPTION.likeIgnoreCase(singleAsString));
			
		} else if (StringUtils.startsWith(fieldName, "CV")) {
			CompId fn = new CompId(2).parse(fieldName, false);
			if (fn.isTokenEmpty(1)) throw new UnsupportedOperationException("Field name invalid: " + fieldName);
			
			JOOQPredicateVisitorWithCValues.CValueCondition cvCondition = getCustomFieldCondition(fn, operator, values);
			if (cvCondition.negated) {
				return notExists(
					selectOne()
					.from(TASKS_CUSTOM_VALUES)
					.where(
						TASKS_CUSTOM_VALUES.TASK_ID.equal(TASKS.TASK_ID)
						.and(TASKS_CUSTOM_VALUES.CUSTOM_FIELD_ID.equal(fn.getToken(1)))
						.and(cvCondition.condition)
					)
				);
				
			} else {
				return exists(
					selectOne()
					.from(TASKS_CUSTOM_VALUES)
					.where(
						TASKS_CUSTOM_VALUES.TASK_ID.equal(TASKS.TASK_ID)
						.and(TASKS_CUSTOM_VALUES.CUSTOM_FIELD_ID.equal(fn.getToken(1)))
						.and(cvCondition.condition)
					)
				);
			}
			
		} else {
			throw new UnsupportedOperationException("Field not supported: " + fieldName);
		}
	}

	@Override
	protected Condition cvalueCondition(QueryBuilderWithCValues.Type cvalueType, ComparisonOperator operator, Collection<?> values) {
		if (QueryBuilderWithCValues.Type.CVSTRING.equals(cvalueType)) {
			return defaultCondition(TASKS_CUSTOM_VALUES.STRING_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVNUMBER.equals(cvalueType)) {
			return defaultCondition(TASKS_CUSTOM_VALUES.NUMBER_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVBOOL.equals(cvalueType)) {
			return defaultCondition(TASKS_CUSTOM_VALUES.BOOLEAN_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVDATE.equals(cvalueType)) {
			return defaultCondition(TASKS_CUSTOM_VALUES.DATE_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVTEXT.equals(cvalueType)) {
			return defaultCondition(TASKS_CUSTOM_VALUES.TEXT_VALUE, operator, values);
			
		} else {
			return null;
		}
	}
}
