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

import com.sonicle.commons.EnumUtils;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryBase;

/**
 *
 * @author malbinola
 */
public class JsCategory {
	public Integer categoryId;
	public String domainId;
	public String userId;
	public Boolean builtIn;
	public String name;
	public String description;
	public String color;
	public String sync;
	public Boolean isPrivate;
	public Integer reminder;
	
	public JsCategory(Category cat) {
		categoryId = cat.getCategoryId();
		domainId = cat.getDomainId();
		userId = cat.getUserId();
		builtIn = cat.getBuiltIn();
		name = cat.getName();
		description = cat.getDescription();
		color = cat.getColor();
		sync = EnumUtils.toSerializedName(cat.getSync());
		isPrivate = cat.getIsPrivate();
		reminder = cat.getDefaultReminder();
	}
	
	public CategoryBase createCategoryForInsert() {
		return createCategoryForUpdate();
	}
	
	public CategoryBase createCategoryForUpdate() {
		CategoryBase item = new CategoryBase();
		item.setDomainId(domainId);
		item.setUserId(userId);
		item.setBuiltIn(builtIn);
		item.setName(name);
		item.setDescription(description);
		item.setColor(color);
		item.setSync(EnumUtils.forSerializedName(sync, Category.Sync.class));
		item.setIsPrivate(isPrivate);
		item.setDefaultReminder(reminder);
		return item;
	}
}
