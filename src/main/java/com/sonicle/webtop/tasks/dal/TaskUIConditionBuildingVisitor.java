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
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS_;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskQueryUI;
import java.util.Collection;
import org.joda.time.DateTime;
import org.jooq.Condition;
import static org.jooq.impl.DSL.trueCondition;

/**
 *
 * @author malbinola
 */
public class TaskUIConditionBuildingVisitor extends TaskConditionBuildingVisitor {
	protected DateTime rangeStart = null;
	protected DateTime rangeEnd = null;
	
	public TaskUIConditionBuildingVisitor(InstanceIdDecoder instanceIdDecoder) {
		super();
		withInstanceIdDecoder(instanceIdDecoder);
	}
	
	public DateTime getRangeStart() {
		return rangeStart;
	}
	
	public DateTime getRangeStartOrDefault(DateTime deflt) {
		return (rangeStart != null) ? rangeStart : deflt;
	}
	
	public DateTime getRangeEnd() {
		return rangeEnd;
	}
	
	public DateTime getRangeEndOrDefault(DateTime deflt) {
		return (rangeEnd != null) ? rangeEnd : deflt;
	}
	
	@Override
	protected Condition buildCondition(String fieldName, Operator operator, Collection<?> values) {
		if (TaskQueryUI.AFTER.equals(fieldName)) {
			rangeStart = singleValueAsDateTime(values);
			return trueCondition();
			
		} else if (TaskQueryUI.BEFORE.equals(fieldName)) {
			rangeEnd = singleValueAsDateTime(values);
			if (rangeEnd != null) rangeEnd = rangeEnd.plusDays(1);
			return trueCondition();
			
		} else if (TaskQueryUI.DONE.equals(fieldName)) {
			Boolean value = singleValueAsBoolean(values);
			return false == value ? TASKS_.STATUS.notEqual(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED)) : TASKS_.STATUS.equal(EnumUtils.toSerializedName(TaskBase.Status.COMPLETED));
			
		} else if (TaskQueryUI.ANY.equals(fieldName)) {
			return defaultCondition(TASKS_.SUBJECT, operator, values)
				.or(defaultCondition(TASKS_.DESCRIPTION, operator, values));
			
		} else {
			return super.buildCondition(fieldName, operator, values);
		}
	}
}
