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
Ext.define('Sonicle.webtop.tasks.model.FolderNode', {
	extend: 'Ext.data.Model',
	mixins: [
		'WTA.sdk.mixin.FolderNodeInterface'
	],
	
	colorField: '_color',
	defaultField: '_default',
	builtInField: '_builtIn',
	activeField: '_active',
	originPermsField: '_orPerms',
	folderPermsField: '_foPerms',
	itemsPermsField: '_itPerms',
	
	fields: [
		WTF.field('_active', 'boolean', true), // Same as checked
		WTF.roField('_default', 'boolean'),
		WTF.roField('_builtIn', 'boolean'),
		//WTF.roField('_provider', 'string'),
		WTF.roField('_color', 'string'),
		WTF.roField('_sync', 'string'),
		WTF.roField('_defPrivate', 'boolean'),
		WTF.roField('_defReminder', 'int'),
		WTF.roField('_orPerms', 'string'),
		WTF.roField('_foPerms', 'string'),
		WTF.roField('_itPerms', 'string')
	]
});
