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
Ext.define('Sonicle.webtop.tasks.model.Task', {
	extend: 'WTA.ux.data.BaseModel',
	requires: [
		'Sonicle.Date',
		'Sonicle.Object',
		'Sonicle.data.validator.Custom',
		'Sonicle.webtop.core.ux.data.CustomFieldValueModel',
		'Sonicle.webtop.tasks.model.TaskAssignee',
		'Sonicle.webtop.tasks.model.TaskAttachment'
	],
	proxy: WTF.apiProxy('com.sonicle.webtop.tasks', 'ManageTask', 'data', {
		writer: {
			type: 'sojson',
			writeAssociations: true
		}
	}),
	
	identifier: 'negativestring',
	idProperty: 'id',
	fields: [
		WTF.field('id', 'string', false),
		WTF.field('parentId', 'string', true),
		WTF.field('categoryId', 'int', false),
		WTF.field('subject', 'string', false),
		WTF.field('location', 'string', true),
		WTF.field('description', 'string', true),
		WTF.field('start', 'date', true, {dateFormat: 'Y-m-d H:i:s'}),
		WTF.field('due', 'date', true, {dateFormat: 'Y-m-d H:i:s'}),
		WTF.field('progress', 'int', true, {defaultValue: 0}),
		WTF.field('status', 'string', true, {defaultValue: 'NA'}),
		WTF.field('importance', 'string', false, {defaultValue: 1}),
		WTF.field('isPrivate', 'boolean', false, {defaultValue: false}),
		WTF.field('docRef', 'string', true),
		WTF.field('reminder', 'int', true),
		WTF.field('contact', 'string', true),
		WTF.field('contactId', 'string', true),
		WTF.field('rrule', 'string', true, {
			validators: [
				{
					type: 'socustom',
					fn: function(v, rec) {
						return Ext.isEmpty(v) || Sonicle.Object.coalesce(rec.get('_childrenCount'), 0) === 0;
					}
				}
			]
		}),
		WTF.field('rstart', 'date', true, {dateFormat: 'Y-m-d'}),
		WTF.field('tags', 'string', true),
		WTF.roField('_childTotalCount', 'int'),
		WTF.roField('_childComplCount', 'int'),
		WTF.roField('_parentSubject', 'string'),
		WTF.roField('_ownerId', 'string'),
		WTF.roField('_cfdefs', 'string')
	],
	hasMany: [
		WTF.hasMany('assignees', 'Sonicle.webtop.tasks.model.TaskAssignee'),
		WTF.hasMany('attachments', 'Sonicle.webtop.tasks.model.TaskAttachment'),
		WTF.hasMany('cvalues', 'Sonicle.webtop.core.ux.data.CustomFieldValueModel')
	],
	
	isSeriesMaster: function() {
		var id = this.getId();
		return !Ext.isEmpty(id) && Sonicle.String.endsWith(id, '.00000000') && !Ext.isEmpty(this.get('rrule'));
	},
	
	isSeriesInstance: function() {
		var id = this.getId();
		return !Ext.isEmpty(id) && !Sonicle.String.endsWith(id, '.00000000');
	},
	
	setStartDate: function(date) {
		var me = this,
				due = me.get('due'), v;
		
		if (!Ext.isEmpty(me.get('rrule')) && !Ext.isDate(date)) return;
		v = me.setDatePart('start', date);
		if (Ext.isDate(v) && Ext.isDate(due) && v > due) me.set('due', v);
	},
	
	setStartTime: function(date) {
		var me = this,
				due = me.get('due'), v;
		
		if (!Ext.isEmpty(me.get('rrule')) && !Ext.isDate(date)) return;
		v = me.setTimePart('start', date);
		if (Ext.isDate(v) && Ext.isDate(due) && v > due) me.set('due', v);
	},
	
	setDueDate: function(date) {
		var me = this,
				start = me.get('start'), v;
		
		v = me.setDatePart('due', date);
		if (Ext.isDate(v) && Ext.isDate(start) && v < start) me.set('due', start);
	},
	
	setDueTime: function(date) {
		var me = this,
				start = me.get('start'), v;
				
		v = me.setTimePart('due', date);
		if (Ext.isDate(v) && Ext.isDate(start) && v < start) me.set('due', start);
	}
	
	
	/*
	setRecurStart: function(date) {
		var me = this, sta;
		me.setDatePart('rstart', date);
		if (me.phantom && Ext.isDate(date)) {
			sta = me.get('start');
			if (Ext.isDate(sta)) me.setStartDate(Ext.Date.clone(Sonicle.Date.min(date, sta)));
		}
	}
	*/
	
	/*
	setStartDate: function(date) {
		var me = this,
				due = me.get('due'), dt;
		dt = me.setDatePart('start', date);
		if(!Ext.isDate(dt) || !Ext.isDate(due)) return;
		if(dt > due) me.set('due', dt);
	},
	
	setDueDate: function(date) {
		var me = this,
				start = me.get('start'), dt;
		dt = me.setDatePart('due', date);
		if(!Ext.isDate(dt) || !Ext.isDate(start)) return;
		if(dt < start) me.set('start', dt);
	},
	*/
	/*'
	setReminderDate: function(date) {
		var me = this,
			dt = me.setDatePart('reminderDate', date);
		if(Ext.isDate(dt)) me.set('reminderDate', dt);
	},
	
	setReminderTime: function(date) {
		var me = this,
			dt = me.setTimePart('reminderDate', date);
		if(Ext.isDate(dt)) me.set('reminderDate', dt);
	},
	*/
	
	
	
	/*
	setDatePart: function(field, date) {
		var me = this,
				v = me.get(field) || new Date(), dt;
		dt = !Ext.isDate(date) ? null : Sonicle.Date.copyDate(date, v);
		me.set(field, dt);
		return dt;
	},
	
	setTimePart: function(field, date) {
		var me = this,
				v = me.get(field) || new Date(), dt;
		if(!Ext.isDate(date) || !Ext.isDate(v)) return;
		dt = Sonicle.Date.copyTime(date, v);
		me.set(field, dt);
		return dt;
	}
	
	*/
});
