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
package com.sonicle.webtop.tasks;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.util.EmailNotification;
import com.sonicle.webtop.core.model.ProfileI18n;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.model.TaskBase;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import jakarta.mail.internet.AddressException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class TplHelper {
	private static final String SERVICE_ID = "com.sonicle.webtop.tasks";
	
	public static String buildTaskReminderSubject(ProfileI18n profileI18n, TaskBase task) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.abbreviate(task.getSubject(), 30));
		
		return MessageFormat.format(WT.lookupResource(SERVICE_ID, profileI18n.getLocale(), TasksLocale.EMAIL_REMINDER_SUBJECT), sb.toString());
	}
	
	public static String buildTplTaskReminderBody(ProfileI18n profileI18n, TaskBase task) throws IOException, TemplateException, AddressException {
		MapItem i18n = new MapItem();
		i18n.put("whenStart", WT.lookupResource(SERVICE_ID, profileI18n.getLocale(), TasksLocale.TPL_EMAIL_TASK_BODY_WHENSTART));
		i18n.put("whenDue", WT.lookupResource(SERVICE_ID,  profileI18n.getLocale(), TasksLocale.TPL_EMAIL_TASK_BODY_WHENDUE));
		i18n.put("whenCompleted", WT.lookupResource(SERVICE_ID,  profileI18n.getLocale(), TasksLocale.TPL_EMAIL_TASK_BODY_WHENCOMPLETED));
		i18n.put("status", WT.lookupResource(SERVICE_ID,  profileI18n.getLocale(), TasksLocale.TPL_EMAIL_TASK_BODY_STATUS));
		i18n.put("completion", WT.lookupResource(SERVICE_ID,  profileI18n.getLocale(), TasksLocale.TPL_EMAIL_TASK_BODY_COMPLETION));
		
		DateTimeFormatter dateFmt = DateTimeUtils.createFormatter(profileI18n.getDateFormat(), DateTimeZone.UTC);
		MapItem item = new MapItem();
		item.put("subject", StringUtils.defaultIfBlank(task.getSubject(), ""));
		item.put("description", StringUtils.defaultIfBlank(task.getDescription(), null));
		item.put("startDate", formatAsDate(task.getStart(), dateFmt));
		item.put("dueDate", formatAsDate(task.getDue(), dateFmt));
		//item.put("completedDate", dateFmt.print(task.getCompletedOn()));
		item.put("status", statusToString(profileI18n.getLocale(), task.getStatus()));
		item.put("completion", toCompletionPercentage(task.getProgress())); 
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("task", item);
		
		return WT.buildTemplate(SERVICE_ID, "tpl/email/task-body.html", vars);
	}
	
	public static String buildTaskReminderHtml(Locale locale, String bodyHeader, String customBodyHtml, String source, String because, String recipientEmail) throws IOException, TemplateException {
		EmailNotification.BecauseBuilder builder = new EmailNotification.BecauseBuilder()
				.withCustomBody(bodyHeader, customBodyHtml);
		
		return builder.build(locale, source, because, recipientEmail).write();
	}
	
	private static String formatAsDate(DateTime dt, DateTimeFormatter fmt) {
		return (dt == null) ? "" : fmt.print(dt);
	}
	
	private static String toCompletionPercentage(Short completion) {
		return (completion == null) ? "" : completion + "%";
	}
	
	private static String statusToString(Locale locale, TaskBase.Status status) {
		return (status == null) ? "" : WT.lookupResource(SERVICE_ID, locale, MessageFormat.format(TasksLocale.TPL_EMAIL_TASK_BODY_STATUS_X, EnumUtils.toSerializedName(status)));
	}
}
