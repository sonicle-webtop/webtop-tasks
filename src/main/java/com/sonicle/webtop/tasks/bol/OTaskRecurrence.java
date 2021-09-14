/*
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks.bol;

import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.webtop.tasks.jooq.tables.pojos.TasksRecurrences;
import net.fortuna.ical4j.model.Recur;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public class OTaskRecurrence extends TasksRecurrences {
	
	public void set(Recur recur, DateTime recurStartDate, DateTime taskStartDate, DateTimeZone taskTimezone) {
		DateTime newStart = (recurStartDate != null) ? recurStartDate : taskStartDate;
		DateTime newUntil = null;
		if (ICal4jUtils.recurHasCount(recur)) {
			DateTime untilDate = ICal4jUtils.calculateRecurrenceEnd(recur, newStart, taskStartDate, null, taskTimezone);
			newUntil = untilDate.withTimeAtStartOfDay().plusDays(1);
		} else if (ICal4jUtils.recurHasUntilDate(recur)) {
			DateTime untilDate = ICal4jUtils.toJodaDateTime(recur.getUntil(), DateTimeZone.UTC).withZone(taskTimezone);
			newUntil = untilDate.withTimeAtStartOfDay().plusDays(1);
		} else {
			newUntil = ICal4jUtils.ifiniteDate(taskTimezone);
		}
		this.setStart(newStart);
		this.setUntil(newUntil);
		this.setRule(recur.toString());
	}
	
	public void set(Recur recur, LocalDate recurStartDate, DateTime taskStartDate, DateTimeZone timezone) {
		DateTime newStart = (recurStartDate != null) ? recurStartDate.toDateTimeAtStartOfDay(timezone) : taskStartDate;
		DateTime newUntil = null;
		if (ICal4jUtils.recurHasCount(recur)) {
			DateTime untilDate = ICal4jUtils.calculateRecurrenceEnd(recur, newStart, taskStartDate, null, timezone);
			newUntil = untilDate.withTimeAtStartOfDay().plusDays(1);
		} else if (ICal4jUtils.recurHasUntilDate(recur)) {
			DateTime untilDate = ICal4jUtils.toJodaDateTime(recur.getUntil(), DateTimeZone.UTC).withZone(timezone);
			newUntil = untilDate.withTimeAtStartOfDay().plusDays(1);
		} else {
			newUntil = ICal4jUtils.ifiniteDate(timezone);
		}
		this.setStart(newStart);
		this.setUntil(newUntil);
		this.setRule(recur.toString());
	}
	
	public Recur getRecur() {
		return ICal4jUtils.parseRRule(getRule());
	}
	
	public LocalDate getStartLocalDate(DateTimeZone timezone) {
		return getStart().withZone(timezone).toLocalDate();
	}
}
