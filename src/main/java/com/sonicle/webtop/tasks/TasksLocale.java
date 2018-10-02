/* 
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks;

/**
 *
 * @author rfullone
 */
public class TasksLocale {
	public static final String SERVICE_NAME = "service.name";
	public static final String SERVICE_DESCRIPTION = "service.description";
	public static final String CATEGORIES_MY = "categories.my";
	
	public static final String EMAIL_REMINDER_SUBJECT = "email.task.reminder.subject";
	public static final String EMAIL_REMINDER_FOOTER_BECAUSE = "email.task.reminder.footer.because";
	
	public static final String TPL_EMAIL_TASK_BODY_WHENSTART = "tpl.email.task.body.whenstart";
	public static final String TPL_EMAIL_TASK_BODY_WHENDUE = "tpl.email.task.body.whendue";
	public static final String TPL_EMAIL_TASK_BODY_WHENCOMPLETED = "tpl.email.task.body.whencompleted";
	public static final String TPL_EMAIL_TASK_BODY_STATUS = "tpl.email.task.body.status";
	public static final String TPL_EMAIL_TASK_BODY_STATUS_X = "tpl.email.task.body.status.{0}";
	//public static final String TPL_EMAIL_TASK_BODY_STATUS_NOTSTARTED = "tpl.email.task.body.status.notstarted";
	//public static final String TPL_EMAIL_TASK_BODY_STATUS_INPROGRESS = "tpl.email.task.body.status.inprogress";
	//public static final String TPL_EMAIL_TASK_BODY_STATUS_COMPLETED = "tpl.email.task.body.status.completed";
	//public static final String TPL_EMAIL_TASK_BODY_STATUS_WAITING = "tpl.email.task.body.status.waiting";
	//public static final String TPL_EMAIL_TASK_BODY_STATUS_DEFERRED = "tpl.email.task.body.status.deferred";
	public static final String TPL_EMAIL_TASK_BODY_COMPLETION = "tpl.email.task.body.completion";
}
