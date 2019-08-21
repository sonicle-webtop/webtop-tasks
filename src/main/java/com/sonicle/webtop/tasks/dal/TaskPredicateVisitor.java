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
import java.util.Collection;
import org.jooq.Condition;
import static com.sonicle.webtop.tasks.jooq.Tables.TASKS;
import com.sonicle.webtop.core.app.sdk.BaseJOOQVisitor;
import com.sonicle.webtop.tasks.model.BaseTask;
import org.joda.time.DateTime;

/**
 *
 * @author Inis
 */
public class TaskPredicateVisitor extends BaseJOOQVisitor {

	public TaskPredicateVisitor() {
		super();
	}

	public TaskPredicateVisitor(boolean ignoreCase) {
		super(ignoreCase);
	}

	@Override
	protected Condition toCondition(String fieldName, ComparisonOperator operator, Collection<?> values, ComparisonNode node) {
		switch (fieldName) {
			case "subject":
				return defaultCondition(TASKS.SUBJECT, operator, values);
			case "description":
				return defaultCondition(TASKS.DESCRIPTION, operator, values);
			case "after":
				DateTime after = (DateTime)single(values);
				return TASKS.START_DATE.greaterOrEqual(after);
			case "before":
				DateTime before = (DateTime)single(values);
				return TASKS.START_DATE.lessThan(before);
			case "done":
				return TASKS.STATUS.equal(EnumUtils.toSerializedName(BaseTask.Status.COMPLETED));
			case "private":
				return defaultCondition(TASKS.IS_PRIVATE, operator, values);
			case "any":
				return TASKS.SUBJECT.likeIgnoreCase(valueToSmartLikePattern(singleAsString(values)))
						.or(TASKS.DESCRIPTION.likeIgnoreCase(valueToSmartLikePattern(singleAsString(values))));
			default:
				throw new UnsupportedOperationException("Field not supported: " + fieldName);
		}
	}
}
