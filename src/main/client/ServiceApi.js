/*
 * webtop-calendar is a WebTop Service developed by Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.tasks.ServiceApi', {
	extend: 'WTA.sdk.ServiceApi',
	uses: [
		'Sonicle.String'
	],
	
	/**
	 * Create an instance of the events portlet body.
	 */
	createTasksPortletBody: function(cfg) {
		return Ext.create('Sonicle.webtop.tasks.portlet.TasksBody', Ext.apply(cfg||{},{ mys: this.service }));
	},
	
	openReminder: function(type, id) {
		if (Sonicle.String.isIn(type, ['task', 'task-recurring'])) {
			this.service.openTaskUI(true, id, false);
		} else {
			Ext.raise('Reminder type not supported [' + type + ']');
		}
	},
	
	/**
	 * Builds a Task instance ID from passed parameters.
	 * @param {String} taskId The task ID.
	 * @param {String} [yyyymmdd] The instance Data in format 'yyyymmdd'.
	 * @returns {String}
	 */
	createTaskInstanceId: function(taskId, yyyymmdd) {
		return Sonicle.webtop.tasks.Service.createTaskInstanceId(taskId, yyyymmdd);
	},
	
	/**
	 * Opens a task using the choosen editing mode, defaults to edit.
	 * @param {String} id The task instance ID.
	 * @param {Object} opts An object containing configuration.
	 * @param {edit|view} [opts.mode="edit"] Opening mode.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 * @param {Boolean} [opts.dirty] The dirty state of the model.
	 * @param {Boolean} [opts.uploadTag] A custom upload tag.
	 * @returns {WTA.sdk.ModelView}
	 */
	openTask: function(id, opts) {
		opts = opts || {};
		return this.service.openTask(opts.mode === 'view' ? false : true, id, {
			callback: opts.callback,
			scope: opts.scope,
			dirty: opts.dirty,
			uploadTag: opts.uploadTag
		});
	},
	
	/**
	 * Starts adding a new task opening editing view.
	 * @param {Object} data data An object containing entity data.
	 * @param {String} [data.categoryId] The category ID in which to add the item.
	 * @param {String} [data.parentId] The ID of parent task in order to add this as sub-task.
	 * @param {String} [data.parentSubject] The subject of parent task in order to add this as sub-task. Optional, only for better UI management.
	 * @param {String} [data.subject] The subject.
	 * @param {String} [data.location] The location.
	 * @param {String} [data.description] The extended description.
	 * @param {Date} [data.start] The start date.
	 * @param {Date} [data.due] The due date.
	 * @param {Number} [data.progress] The progress percentage (integer value between 0-100).
	 * @param {NA|CO|IP|CA|WA} [data.status] The completion status.
	 * @param {9|5|1} [data.importance] The assigned importance: 9 = Low, 5 = Normal/Medium, 1 = High.
	 * @param {0|5|10|15|30|45|60|120|180|240|300|360|420|480|540|600|660|720|1080|1440|2880|10080|20160|43200} [data.reminder] Minutes before start at which set reminder.
	 * @param {default|private} [data.visibility] Visibility value.
	 * @param {String} [data.docRef] The reference document.
	 * @param {String} [data.tags] Pipe-separated list of WebTop's tag IDs.
	 * @param {Object} opts An object containing configuration.
	 * @param {Function} [opts.callback] Callback method for 'viewsave' event.
	 * @param {Object} [opts.scope] The callback method scope.
	 * @param {Boolean} [opts.dirty] The dirty state of the model.
	 * @param {Boolean} [opts.uploadTag] A custom upload tag.
	 */
	addTask: function(data, opts) {
		opts = opts || {};
		this.service.addTaskWithData(data, {
			callback: opts.callback,
			scope: opts.scope,
			dirty: opts.dirty,
			uploadTag: opts.uploadTag
		});
	}
});
