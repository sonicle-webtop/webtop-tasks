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
package com.sonicle.webtop.tasks.bol.js;

import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.webtop.tasks.bol.model.MyCategoryFSOrigin;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFSFolder;
import com.sonicle.webtop.tasks.model.CategoryFSOrigin;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.TaskLookupInstance;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class JsPletTasks {
	public String id;
	public Integer categoryId;
	public String categoryName;
    public String categoryColor;
	public String subject;
	public String body;
	public String due;
	public String _orDN;
	public String _owPid;
	public String _foPerms;
	public String _itPerms;
	
	public JsPletTasks(CategoryFSOrigin origin, CategoryFSFolder folder, CategoryPropSet props, TaskLookupInstance task, DateTimeZone profileTz) {
		final Category category = folder.getCategory();
		
		this.id = task.getId().toString();
		this.categoryId = task.getCategoryId();
		this.categoryName = category.getName();
		this.categoryColor = category.getColor();
		this.subject = task.getSubject();
		this.body = StringUtils.left(task.getDescription(), 250);
		this.due = JodaTimeUtils.printYMDHMS(profileTz, task.getDue());
		
		this._orDN = (origin instanceof MyCategoryFSOrigin) ? null : origin.getDisplayName();
		this._owPid = category.getProfileId().toString();
		this._foPerms = folder.getPermissions().getFolderPermissions().toString();
		this._itPerms = folder.getPermissions().getItemsPermissions().toString();
	}
}
