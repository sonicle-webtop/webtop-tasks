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
package com.sonicle.webtop.tasks.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.InternetAddressUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.commons.web.json.CId;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldDefs;
import com.sonicle.webtop.core.bol.js.ObjCustomFieldValue;
import com.sonicle.webtop.core.model.CustomField;
import com.sonicle.webtop.core.model.CustomFieldValue;
import com.sonicle.webtop.core.model.CustomPanel;
import com.sonicle.webtop.core.sdk.UserProfileId;
import com.sonicle.webtop.tasks.bol.model.MyShareRootCategory;
import com.sonicle.webtop.tasks.model.Category;
import com.sonicle.webtop.tasks.model.CategoryPropSet;
import com.sonicle.webtop.tasks.model.ShareFolderCategory;
import com.sonicle.webtop.tasks.model.ShareRootCategory;
import com.sonicle.webtop.tasks.model.TaskBase;
import com.sonicle.webtop.tasks.model.TaskInstance;
import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

/**
 *
 * @author malbinola
 */
public class JsTaskPreview {
	public String id;
	public String subject;
	public String location;
	public String description;
	public String start;
	public String due;
	public String completedOn;
	public String status;
	public Short progress;
	public Short importance;
	public Boolean isPrivate;
	public String docRef;
	public Integer reminder;
	public String contactEmail;
	public String tags;
	public ArrayList<ObjCustomFieldValue> cvalues;
	public Integer categoryId;
	public String categoryName;
	public String categoryColor;
	public String ownerId;
	public String ownerDN;
	public String _frights;
	public String _erights;
	public String _cfdefs;
	
	public JsTaskPreview(ShareRootCategory root, ShareFolderCategory folder, CategoryPropSet folderProps, TaskInstance item, Collection<CustomPanel> customPanels, Map<String, CustomField> customFields, String profileLanguageTag, DateTimeZone profileTz) {
		Category category = folder.getCategory();
		boolean isSeriesMaster = StringUtils.endsWith(item.getId().toString(), ".00000000") && item.getRecurrence() != null;
		
		this.id = item.getId().toString();
        this.subject = item.getSubject();
		this.location = item.getLocation();
        this.description = item.getDescription();
		this.start = !isSeriesMaster ? DateTimeUtils.printYmdHmsWithZone(item.getStart(), profileTz) : null;
		this.due = !isSeriesMaster ? DateTimeUtils.printYmdHmsWithZone(item.getDue(), profileTz) : null;
		this.completedOn = DateTimeUtils.printYmdHmsWithZone(item.getCompletedOn(), profileTz);
		this.status = EnumUtils.toSerializedName(item.getStatus());
        this.progress = item.getProgress();
        this.importance = item.getImportance();
        this.isPrivate = item.getIsPrivate();
		this.docRef = item.getDocumentRef();
		this.reminder =	TaskBase.Reminder.getMinutesValue(item.getReminder());
		InternetAddress cia = InternetAddressUtils.toInternetAddress(item.getContact());
		if (cia != null) this.contactEmail = cia.getAddress();
		this.tags = CId.build(item.getTags()).toString();
		
		this.cvalues = new ArrayList<>();
		ArrayList<ObjCustomFieldDefs.Panel> panels = new ArrayList<>();
		for (CustomPanel panel : customPanels) {
			panels.add(new ObjCustomFieldDefs.Panel(panel, profileLanguageTag));
		}
		ArrayList<ObjCustomFieldDefs.Field> fields = new ArrayList<>();
		for (CustomField field : customFields.values()) {
			CustomFieldValue cvalue = null;
			if (item.hasCustomValues()) {
				cvalue = item.getCustomValues().get(field.getFieldId());
			}
			cvalues.add(cvalue != null ? new ObjCustomFieldValue(field.getType(), cvalue, profileTz) : new ObjCustomFieldValue(field.getType(), field.getFieldId()));
			fields.add(new ObjCustomFieldDefs.Field(field, profileLanguageTag));
		}
		
		this.categoryId = category.getCategoryId();
		this.categoryName = category.getName();
		this.categoryColor = (folderProps != null) ? folderProps.getColorOrDefault(category.getColor()) : folder.getCategory().getColor();
		this.ownerId = new UserProfileId(category.getDomainId(), category.getUserId()).toString();
		this.ownerDN = (root instanceof MyShareRootCategory) ? null : root.getDescription();
		this._frights = folder.getPerms().toString();
		this._erights = folder.getElementsPerms().toString();
		_cfdefs = LangUtils.serialize(new ObjCustomFieldDefs(panels, fields), ObjCustomFieldDefs.class);
	}
}
