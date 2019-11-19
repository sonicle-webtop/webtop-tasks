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
Ext.define('Sonicle.webtop.tasks.model.CategoryLkp', {
	extend: 'WTA.ux.data.BaseModel',
	
	idProperty: 'categoryId',
	fields: [
		WTF.field('categoryId', 'int', false),
		// For backward compatibility
		WTF.calcField('domainId', 'string', ['_profileId'], function(v, rec) {
			return Sonicle.String.substrAfterLast(rec.get('_profileId'), '@');
		}),
		// For backward compatibility
		WTF.calcField('userId', 'string', ['_profileId'], function(v, rec) {
			return Sonicle.String.substrBeforeLast(rec.get('_profileId'), '@');
		}),
		WTF.field('name', 'string', false),
		WTF.field('color', 'string', false, {defaultValue: '#FFFFFF'}),
		WTF.field('isDefault', 'boolean', false, {defaultValue: false}),
		WTF.field('tasPrivate', 'boolean', false, {defaultValue: false}),
		WTF.roField('_profileId', 'string'),
		WTF.roField('_profileDescription', 'string'),
		WTF.calcField('_label', 'string', ['name', '_profileDescription'], function(v, rec) {
			return Ext.String.format('[{0}] {1}', rec.get('_profileDescription'), rec.get('name'));
		}),
		WTF.roField('_writable', 'boolean'),
		WTF.roField('_order', 'int')
	]
});
