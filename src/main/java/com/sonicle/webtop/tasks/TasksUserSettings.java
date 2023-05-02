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

import com.sonicle.webtop.tasks.bol.model.GridView;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.web.json.bean.IntegerSet;
import com.sonicle.commons.web.json.bean.StringSet;
import com.sonicle.webtop.core.sdk.BaseUserSettings;
import com.sonicle.webtop.core.sdk.UserProfileId;
import static com.sonicle.webtop.tasks.TasksSettings.*;

/**
 *
 * @author malbinola
 */
public class TasksUserSettings extends BaseUserSettings {
	private TasksServiceSettings ss;
	
	public TasksUserSettings(String serviceId, UserProfileId profileId) {
		super(serviceId, profileId);
		ss = new TasksServiceSettings(serviceId, profileId.getDomainId());
	}
	
	public GridView getGridView() {
		GridView value = getEnum(GRID_VIEW, null, GridView.class);
		if (value != null) return value;
		return ss.getDefaultGridView();
	}
	
	public boolean setGridView(GridView value) {
		return setEnum(GRID_VIEW, value);
	}
	
	public boolean setGridView(String value) {
		return setGridView(EnumUtils.forSerializedName(value, GridView.class));
	}
	
	public String getTaskReminderDelivery() {
		String value = getString(TASK_REMINDER_DELIVERY, null);
		if (value != null) return value;
		return ss.getDefaultTaskReminderDelivery();
	}
	
	public boolean setTaskReminderDelivery(String value) {
		return setString(TASK_REMINDER_DELIVERY, value);
	}
	
	public StringSet getInactiveCategoryOrigins() {
		return getObject(INACTIVE_CATEGORY_ORIGINS, new StringSet(), StringSet.class);
	}
	
	public boolean setInactiveCategoryOrigins(StringSet value) {
		return setObject(INACTIVE_CATEGORY_ORIGINS, value, StringSet.class);
	}
	
	public IntegerSet getInactiveCategoryFolders() {
		return getObject(INACTIVE_CATEGORY_FOLDERS, new IntegerSet(), IntegerSet.class);
	}
	
	public boolean setInactiveCategoryFolders(IntegerSet value) {
		return setObject(INACTIVE_CATEGORY_FOLDERS, value, IntegerSet.class);
	}
	
	public Integer getDefaultCategoryFolder() {
		return getInteger(DEFAULT_CATEGORY_FOLDER, null);
	}
	
	public boolean setDefaultCategoryFolder(Integer categoryId) {
		return setInteger(DEFAULT_CATEGORY_FOLDER, categoryId);
	}
}
