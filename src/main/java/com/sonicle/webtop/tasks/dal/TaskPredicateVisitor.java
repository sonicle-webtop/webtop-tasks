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

import com.sonicle.commons.qbuilders.nodes.ComparisonNode;
import com.sonicle.commons.qbuilders.operators.ComparisonOperator;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.json.CId;
import java.util.Collection;
import org.jooq.Condition;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import com.sonicle.webtop.core.app.sdk.JOOQPredicateVisitorWithCValues;
import com.sonicle.webtop.core.app.sdk.QueryBuilderWithCValues;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_CUSTOM_VALUES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_RECURRENCES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_TAGS;
import com.sonicle.webtop.tasks.jooq.tables.TasksCustomValues;
import com.sonicle.webtop.tasks.jooq.tables.TasksTags;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jooq.Field;
import org.jooq.TableLike;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author malbinola
 */
public class TaskPredicateVisitor extends JOOQPredicateVisitorWithCValues {
	private final TasksCustomValues PV_TASKS_CUSTOM_VALUES = TASKS_CUSTOM_VALUES.as("pvis_cv");
	private final TasksTags PV_TASKS_TAGS = TASKS_TAGS.as("pvis_ct");
	protected final Target target;
	protected InstanceIdDecoder instanceIdDecoder;
	protected DateTime rangeStart = null;
	protected DateTime rangeEnd = null;
	
	public TaskPredicateVisitor(Target target) {
		super(false);
		this.target = target;
	}
	
	public TaskPredicateVisitor withInstanceIdDecoder(InstanceIdDecoder instanceIdDecoder) {
		this.instanceIdDecoder = instanceIdDecoder;
		return this;
	}
	
	public DateTime getRangeStart() {
		return rangeStart;
	}
	
	public DateTime getRangeStartOfDefault(DateTime deflt) {
		return (rangeStart != null) ? rangeStart : deflt;
	}
	
	public DateTime getRangeEnd() {
		return rangeEnd;
	}
	
	public DateTime getRangeEndOfDefault(DateTime deflt) {
		return (rangeEnd != null) ? rangeEnd : deflt;
	}
	
	@Deprecated
	public boolean hasFromRange() {
		return rangeStart != null;
	}
	
	@Deprecated
	public boolean hasToRange() {
		return rangeEnd != null;
	}
	
	@Deprecated
	public DateTime getFromRange() {
		return rangeStart;
	}
	
	@Deprecated
	public DateTime getToRange() {
		return rangeEnd;
	}

	@Override
	protected Condition toCondition(String fieldName, ComparisonOperator operator, Collection<?> values, ComparisonNode node) {
		if ("subject".equals(fieldName)) {
			return defaultCondition(TASKS.SUBJECT, operator, values);
			
		} else if ("location".equals(fieldName)) {
			return defaultCondition(TASKS.LOCATION, operator, values);
			
		} else if ("description".equals(fieldName)) {
			return defaultCondition(TASKS.DESCRIPTION, operator, values);
			
		} else if ("after".equals(fieldName)) {
			rangeStart = (DateTime)single(values);
			if (target == null) {
				return null;
			} else {
				if (Target.RECURRING.equals(target)) {
					return TASKS_RECURRENCES.START.greaterOrEqual(rangeStart)
						.or(TASKS_RECURRENCES.UNTIL.greaterOrEqual(rangeStart));
				} else {
					return TASKS.START.greaterOrEqual(rangeStart);
				}
			}
			
		} else if ("before".equals(fieldName)) {
			rangeEnd = (DateTime)single(values);
			if (rangeEnd != null) rangeEnd = rangeEnd.plusDays(1);
			if (target == null) {
				return null;
			} else {
				if (Target.RECURRING.equals(target)) {
					return TASKS_RECURRENCES.START.lessThan(rangeEnd)
							.or(TASKS_RECURRENCES.UNTIL.lessThan(rangeEnd));
				} else {
					return TASKS.START.lessThan(rangeEnd);
				}
			}
			
		} else if ("status".equals(fieldName)) {
			return defaultCondition(TASKS.STATUS, operator, values);
			
		} else if ("done".equals(fieldName)) {
			Boolean value = singleAsBoolean(values);
			return false == value ? TASKS.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED)) : TASKS.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
			
		} else if ("private".equals(fieldName)) {
			return defaultCondition(TASKS.IS_PRIVATE, operator, values);
			
		} else if ("document".equals(fieldName)) {
			return defaultCondition(TASKS.DOCUMENT_REF, operator, values);
			
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
			
		} else if ("parent".equals(fieldName)) {
			String taskId = singleAsString(values);
			TaskInstanceId iid = TaskInstanceId.parse(taskId);
			if (iid != null && instanceIdDecoder != null) {
				String realTaskId = instanceIdDecoder.realTaskId(iid);
				if (realTaskId != null) taskId = realTaskId;
			}
			return TASKS.PARENT_TASK_ID.equal(taskId);
			
		} else if (StringUtils.startsWith(fieldName, "CV")) {
			CId fn = new CId(fieldName, 2);
			if (fn.isTokenEmpty(1)) throw new UnsupportedOperationException("Field name invalid: " + fieldName);
			return generateCValueCondition(fn, operator, values);
			
		} else {
			throw new UnsupportedOperationException("Field not supported: " + fieldName);
		}
	}
	
	@Override
	protected Field<?> getFieldEntityIdOfEntityTable() {
		return TASKS.TASK_ID;
	}
	
	@Override
	protected TableLike<?> getTableTags() {
		return PV_TASKS_TAGS;
	}
	
	@Override
	protected Field<String> getFieldTagIdOfTableTags() {
		return PV_TASKS_TAGS.TAG_ID;
	}
	
	@Override
	protected Condition getConditionTagsForCurrentEntity() {
		return PV_TASKS_TAGS.TASK_ID.eq(TASKS.TASK_ID);
	}
	
	@Override
	protected TableLike<?> getTableCustomValues() {
		return PV_TASKS_CUSTOM_VALUES;
	}
	
	@Override
	protected Condition getConditionCustomValuesForCurrentEntityAndField(String fieldId) {
		return PV_TASKS_CUSTOM_VALUES.TASK_ID.eq(TASKS.TASK_ID)
				.and(PV_TASKS_CUSTOM_VALUES.CUSTOM_FIELD_ID.eq(fieldId));
	}
	
	@Override
	protected Condition getConditionCustomValuesForFieldValue(QueryBuilderWithCValues.Type cvalueType, ComparisonOperator operator, Collection<?> values) {
		if (QueryBuilderWithCValues.Type.CVSTRING.equals(cvalueType)) {
			return defaultCondition(PV_TASKS_CUSTOM_VALUES.STRING_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVNUMBER.equals(cvalueType)) {
			return defaultCondition(PV_TASKS_CUSTOM_VALUES.NUMBER_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVBOOL.equals(cvalueType)) {
			return defaultCondition(PV_TASKS_CUSTOM_VALUES.BOOLEAN_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVDATE.equals(cvalueType)) {
			return defaultCondition(PV_TASKS_CUSTOM_VALUES.DATE_VALUE, operator, values);
			
		} else if (QueryBuilderWithCValues.Type.CVTEXT.equals(cvalueType)) {
			return defaultCondition(PV_TASKS_CUSTOM_VALUES.TEXT_VALUE, operator, values);
			
		} else {
			return null;
		}
	}
	
	public static enum Target {
		NORMAL, RECURRING;
	}
	
	public static interface InstanceIdDecoder {
		public String realTaskId(TaskInstanceId instanceId);
	}
}
