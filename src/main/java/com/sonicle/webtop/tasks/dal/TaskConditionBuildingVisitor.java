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

import com.sonicle.commons.rsql.parser.Operator;
import com.sonicle.webtop.core.app.sdk.JOOQConditionBuildingVisitorWithCFields;
import com.sonicle.webtop.core.model.CustomFieldBase;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_CUSTOM_VALUES;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_TAGS;
import com.sonicle.webtop.tasks.jooq.tables.TasksCustomValues;
import com.sonicle.webtop.tasks.jooq.tables.TasksTags;
import com.sonicle.webtop.tasks.model.TaskInstanceId;
import com.sonicle.webtop.tasks.model.TaskQuery;
import java.util.Collection;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.TableLike;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.selectOne;

/**
 *
 * @author malbinola
 */
public class TaskConditionBuildingVisitor extends JOOQConditionBuildingVisitorWithCFields {
	private final TasksCustomValues PV_TASKS_CUSTOM_VALUES = TASKS_CUSTOM_VALUES.as("pvis_cv");
	private final TasksTags PV_TASKS_TAGS = TASKS_TAGS.as("pvis_ct");
	protected InstanceIdDecoder instanceIdDecoder;
	
	public TaskConditionBuildingVisitor() {
		super(false);
	}
	
	public TaskConditionBuildingVisitor withInstanceIdDecoder(InstanceIdDecoder instanceIdDecoder) {
		this.instanceIdDecoder = instanceIdDecoder;
		return this;
	}
	
	@Override
	protected Condition buildCondition(String fieldName, Operator operator, Collection<?> values) {
		if (TaskQuery.ID.equals(fieldName)) {
			return defaultCondition(TASKS_.TASK_ID, operator, values);
			
		} else if (TaskQuery.CREATED_ON.equals(fieldName)) {
			return defaultCondition(TASKS_.CREATION_TIMESTAMP, operator, values);
			
		} else if (TaskQuery.UPDATED_ON.equals(fieldName)) {
			return defaultCondition(TASKS_.REVISION_TIMESTAMP, operator, values);
			
		} else if (TaskQuery.PRIVATE.equals(fieldName)) {
			return defaultCondition(TASKS_.IS_PRIVATE, operator, values);
			
		} else if (TaskQuery.SUBJECT.equals(fieldName)) {
			return defaultCondition(TASKS_.SUBJECT, operator, values);
			
		} else if (TaskQuery.LOCATION.equals(fieldName)) {
			return defaultCondition(TASKS_.LOCATION, operator, values);
			
		} else if (TaskQuery.DESCRIPTION.equals(fieldName)) {
			return defaultCondition(TASKS_.DESCRIPTION, operator, values);
			
		} else if (TaskQuery.TIMEZONE.equals(fieldName)) {
			return defaultCondition(TASKS_.TIMEZONE, operator, values);
			
		} else if (TaskQuery.START.equals(fieldName)) {
			return defaultCondition(TASKS_.START, operator, values);
			
		} else if (TaskQuery.DUE.equals(fieldName)) {
			return defaultCondition(TASKS_.DUE, operator, values);
			
		} else if (TaskQuery.COMPLETED_ON.equals(fieldName)) {
			return defaultCondition(TASKS_.COMPLETED_ON, operator, values);
			
		} else if (TaskQuery.PROGRESS.equals(fieldName)) {
			return defaultCondition(TASKS_.PROGRESS, operator, values);
			
		} else if (TaskQuery.COMPLETED_ON.equals(fieldName)) {
			return defaultCondition(TASKS_.COMPLETED_ON, operator, values);
			
		} else if (TaskQuery.STATUS.equals(fieldName)) {
			return defaultCondition(TASKS_.STATUS, operator, values);
			
		} else if (TaskQuery.IMPORTANCE.equals(fieldName)) {
			return defaultCondition(TASKS_.IMPORTANCE, operator, values);
			
		} else if (TaskQuery.REFERENCE.equals(fieldName)) {
			return defaultCondition(TASKS_.DOCUMENT_REF, operator, values);
			
		} else if (TaskQuery.REMINDER.equals(fieldName)) {
			return defaultCondition(TASKS_.REMINDER, operator, values);
			
		} else if (TaskQuery.ORGANIZER.equals(fieldName)) {
			return defaultCondition(TASKS_.ORGANIZER, operator, values);
			
		} else if (TaskQuery.ORGANIZER_ID.equals(fieldName)) {
			return defaultCondition(TASKS_.ORGANIZER_ID, operator, values);
			
		} else if (TaskQuery.PARENT_ID.equals(fieldName)) {
			String taskId = singleValueAsString(values);
			TaskInstanceId iid = TaskInstanceId.parse(taskId);
			if (iid != null && instanceIdDecoder != null) {
				String realTaskId = instanceIdDecoder.realTaskId(iid);
				if (realTaskId != null) taskId = realTaskId;
			}
			return TASKS_.PARENT_TASK_ID.equal(taskId);
			
		}/* else if (TaskQueryNEW.COMPANY.equals(fieldName)) {
			return defaultCondition(TASKS_.COMPANY, operator, values);
			
		} else if (TaskQueryNEW.COMPANY_ID.equals(fieldName)) {
			return defaultCondition(TASKS_.COMPANY_ID, operator, values);
			
		}*/ else if (TaskQuery.TAG_ID.equals(fieldName)) {
			return exists(
				selectOne()
				.from(TASKS_TAGS)
				.where(
					TASKS_TAGS.TASK_ID.equal(TASKS_.TASK_ID)
					.and(TASKS_TAGS.TAG_ID.equal(singleValueAsString(values)))
				)
			);
			
		} else if (isCFieldPlainNotation(fieldName) || isCFieldWithRawValueTypeNotation(fieldName)) {
			return evalFieldNameAndGenerateCFieldCondition(fieldName, operator, values);
		}
		
		return null;
	}
	
	@Override
	protected Field<?> getFieldEntityIdOfEntityTable() {
		return TASKS_.TASK_ID;
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
		return PV_TASKS_TAGS.TASK_ID.eq(TASKS_.TASK_ID);
	}

	@Override
	protected TableLike<?> getTableCustomValues() {
		return PV_TASKS_CUSTOM_VALUES;
	}
	
	@Override
	protected Condition getConditionCustomValuesForCurrentEntityAndField(String fieldId) {
		return PV_TASKS_CUSTOM_VALUES.TASK_ID.eq(TASKS_.TASK_ID)
			.and(PV_TASKS_CUSTOM_VALUES.CUSTOM_FIELD_ID.eq(fieldId));
	}

	@Override
	protected Field<?> getTableCustomValuesTypeTableField(CustomFieldBase.RawValueType cvalueType) {
		if (CustomFieldBase.RawValueType.CVSTRING.equals(cvalueType)) {
			return PV_TASKS_CUSTOM_VALUES.STRING_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVSTRINGARRAY.equals(cvalueType)) {
			return PV_TASKS_CUSTOM_VALUES.STRING_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVNUMBER.equals(cvalueType)) {
			return PV_TASKS_CUSTOM_VALUES.NUMBER_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVBOOL.equals(cvalueType)) {
			return PV_TASKS_CUSTOM_VALUES.BOOLEAN_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVDATE.equals(cvalueType)) {
			return PV_TASKS_CUSTOM_VALUES.DATE_VALUE;
			
		} else if (CustomFieldBase.RawValueType.CVTEXT.equals(cvalueType)) {
			return PV_TASKS_CUSTOM_VALUES.TEXT_VALUE;
			
		} else {
			return null;
		}
	}
	
	public static interface InstanceIdDecoder {
		public String realTaskId(TaskInstanceId instanceId);
	}
}
