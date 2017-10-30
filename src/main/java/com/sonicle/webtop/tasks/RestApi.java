/*
 * Copyright (C) 2017 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2017 Sonicle S.r.l.".
 */
package com.sonicle.webtop.tasks;

import com.sonicle.webtop.core.app.RunContext;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.sdk.BaseRestApiEndpoint;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.core.sdk.WTException;
import com.sonicle.webtop.tasks.bol.js.rest.JsIncomingCategory;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryFolder;
import com.sonicle.webtop.tasks.model.CategoryRoot;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author malbinola
 */
public class RestApi extends BaseRestApiEndpoint {
	
	@GET
	@Path("/categories/incoming")
	@Produces({MediaType.APPLICATION_JSON})
	public Response listIncomingCategories() throws WTException {
		TasksManager manager = getManager();
		
		ArrayList<JsIncomingCategory> items = new ArrayList<>();
		for (CategoryRoot root : manager.listIncomingCategoryRoots()) {
			for (CategoryFolder fold : manager.listIncomingCategoryFolders(root.getShareId()).values()) {
				if (Category.Sync.OFF.equals(fold.getCategory().getSync())) continue;
				
				items.add(new JsIncomingCategory(root, fold));
			}
		}
		return ok(items);
	}
	
	private TasksManager getManager() {
		return getManager(RunContext.getRunProfileId());
	}
	
	private TasksManager getManager(UserProfileId targetProfileId) {
		return (TasksManager)WT.getServiceManager(SERVICE_ID, targetProfileId);
	}
}
