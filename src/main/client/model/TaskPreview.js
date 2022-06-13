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
Ext.define('Sonicle.webtop.tasks.model.TaskPreview', {
	extend: 'WTA.ux.data.EmptyModel',
	proxy: WTF.apiProxy('com.sonicle.webtop.tasks', 'GetTaskPreview'),
	
	idProperty: 'id',
	fields: [
		WTF.roField('id', 'string'),
		WTF.roField('taskId', 'string'),
		WTF.roField('subject', 'string'),
		WTF.roField('location', 'string'),
		WTF.roField('start', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('due', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('completedOn', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('status', 'string'),
		WTF.roField('progress', 'int'),
		WTF.roField('importance', 'int'),
		WTF.roField('isPrivate', 'boolean'),
		WTF.roField('docRef', 'string'),
		WTF.roField('reminder', 'int'),
		WTF.roField('contactEmail', 'string'),
		WTF.roField('tags', 'string'),
		WTF.roField('hasRecur', 'boolean'),
		WTF.roField('categoryId', 'int'),
		WTF.roField('categoryName', 'string'),
		WTF.roField('categoryColor', 'string'),
		WTF.roField('ownerId', 'string'),
		WTF.roField('ownerDN', 'string'),
		WTF.roField('_frights', 'string'),
		WTF.roField('_erights', 'string'),
		WTF.roField('_cfdefs', 'string')
	],
	hasMany: [
		WTF.hasMany('cvalues', 'Sonicle.webtop.core.ux.data.CustomFieldValueModel')
	]
});
