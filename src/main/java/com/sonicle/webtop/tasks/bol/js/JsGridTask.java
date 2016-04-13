/* * webtop-task is a WebTop Service developed by Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.tasks.bol.js;

import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.core.sdk.UserProfile;
import com.sonicle.webtop.tasks.bol.OCategory;
import com.sonicle.webtop.tasks.bol.OTask;
import com.sonicle.webtop.tasks.bol.VTask;
import com.sonicle.webtop.tasks.bol.model.CategoryFolder;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsGridTask {
	public Integer taskId;
	public String subject;
	public String description;
	public String startDate;
	public String dueDate;
	public String completedDate;
	public Short importance;
	public Boolean isPrivate;
	public String status;
	public Short percentage;
	public String reminderDate;
	//public String publicUid;
    public Integer categoryId;
	public String categoryName;
    public String categoryColor;
	public String _frights;
	public String _erights;
	public String _profileId;

	
	public JsGridTask() {}
	
	public JsGridTask(CategoryFolder folder, VTask task, DateTimeZone profileTz) {
		DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter(profileTz);
		
		taskId = task.getTaskId();
        subject = task.getSubject();
        description = task.getDescription();
        if (task.getStartDate()!=null)
            startDate = ymdhmsZoneFmt.print(task.getStartDate());
        if (task.getDueDate()!=null)
            dueDate = ymdhmsZoneFmt.print(task.getDueDate());
        if (task.getCompletedDate()!=null)
            completedDate = ymdhmsZoneFmt.print(task.getCompletedDate());
        importance = task.getImportance();
        isPrivate = task.getIsPrivate();
        status = task.getStatus();
        percentage = task.getCompletionPercentage();
        reminderDate = ymdhmsZoneFmt.print(task.getReminderDate());
        //publicUid;
        categoryId = task.getCategoryId();
        categoryName = folder.getCategory().getName();
        categoryColor = folder.getCategory().getColor();
        _frights = folder.getPerms().toString();
        _erights = folder.getElementsPerms().toString();
        _profileId = folder.getCategory().getProfileId().toString();
	}
	
}
