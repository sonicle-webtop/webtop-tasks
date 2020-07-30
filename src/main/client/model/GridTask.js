/*
 * webtop-tasks is a WebTop Service developed by Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.tasks.model.GridTask', {
	extend: 'WTA.ux.data.BaseModel',
	
	idProperty: 'taskId',
	fields: [
		WTF.roField('taskId', 'int'),
		WTF.roField('subject', 'string'),
		WTF.roField('description', 'string'),
		WTF.roField('startDate', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('dueDate', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.roField('importance', 'string'),
		WTF.roField('isPrivate', 'boolean'),
		WTF.roField('status', 'string'),
		WTF.roField('percentage', 'int'),
		WTF.calcField('progress', 'number','percentage', function (v, rec) {
			return Ext.isEmpty(rec.get('percentage'))?0:rec.get('percentage')/100;
		}),
		WTF.roField('reminderDate', 'date', {dateFormat: 'Y-m-d H:i:s'}),
		WTF.calcField('hasReminder', 'boolean', 'reminderDate', function (v, rec) {
			return Ext.isEmpty(rec.get('reminderDate'));
		}),
		//WTF.roField('publicUid', 'string'),
		WTF.roField('tags', 'string'),
		WTF.roField('categoryId', 'int'),
		WTF.roField('categoryName', 'string'),
		WTF.roField('categoryColor', 'string')
	]
});
