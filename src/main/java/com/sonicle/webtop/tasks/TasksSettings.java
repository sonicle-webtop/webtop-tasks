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
 * @author malbinola
 */
public class TasksSettings {
	
	/**
	 * [system+domain]
	 * [boolean](false)
	 * Enable/Disable category deletions through DAV rest-api interface. Defaults to `false`.
	 */
	public static final String DAV_CATEGORY_DELETE_ENABLED = "dav.category.delete.enabled";
	
	/**
	 * [system+domain][default-only]
	 * [enum]
	 * The default value of the sync field for new categories.
	 */
	public static final String CATEGORY_SYNC = "category.sync";
	
	/**
	 * [user][default]
	 * [enum {all:ALL, today:TODAY, next7:NEXT7, notStarted:NOT_STARTED, late:LATE, completed:COMPLETED, notCompleted:NOT_COMPLETED](today)
	 * Tasks grid default view
	 */
	public static final String GRID_VIEW = "grid.view";
	
	/**
	 * [user][default]
	 * [string]
	 * Set reminder delivery mode
	 */
	public static final String TASK_REMINDER_DELIVERY = "task.reminder.delivery";
	public static final String TASK_REMINDER_DELIVERY_APP = "app";
	public static final String TASK_REMINDER_DELIVERY_EMAIL = "email";
	
	/**
	 * [user]
	 * [string[]]
	 * List of deactivated origin nodes.
	 */
	public static final String INACTIVE_CATEGORY_ORIGINS = "category.origins.inactive";
	
	/**
	 * [user]
	 * [int[]]
	 * List of deactivated folders (groups).
	 */
	public static final String INACTIVE_CATEGORY_FOLDERS = "category.folders.inactive";
	
	/**
	 * [user]
	 * [int]
	 * ID of the folder choosen as default.
	 */
	public static final String DEFAULT_CATEGORY_FOLDER = "category.folder.default";
}
