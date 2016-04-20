/*
 * webtop-task is a WebTop Service developed by Sonicle S.r.l.
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
Ext.define('Sonicle.webtop.tasks.view.Task', {
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.form.Separator',
		'Sonicle.form.field.IconComboBox',
		'WT.ux.data.EmptyModel',
		'WT.ux.data.ValueModel',
		'WT.ux.field.SuggestCombo',
		'Sonicle.webtop.tasks.model.Task',
		'Sonicle.webtop.tasks.store.Importance',
		'Sonicle.webtop.tasks.store.Status',
		'Sonicle.webtop.tasks.model.CategoryLkp'
	],
	
	dockableConfig: {
		title: '{task.tit}',
		iconCls: 'wttasks-icon-task-xs',
		width: 700,
		height: 480
	},
	confirm: 'yn',
	autoToolbar: false,
	fieldTitle: 'subject',
	modelName: 'Sonicle.webtop.tasks.model.Task',
	
	viewModel: {
		formulas: {
			startDate: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setDatePart('startDate', val);
				}
			},
			dueDate: {
				bind: {bindTo: '{record.dueDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setDatePart('dueDate', val);
				}
			},
			reminderDate: {
				bind: {bindTo: '{record.reminderDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setDatePart('reminderDate', val);
				}
			},
			reminderTime: {
				bind: {bindTo: '{record.reminderDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setTimePart('reminderDate', val);
				}
			},
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			hasReminder: {
				bind: {bindTo: '{record.reminderDate}'},
				get: function(val) {
					return !Ext.isEmpty(val);
				}
			}
		}
	},
	
	initComponent: function() {
		var me = this;
		
		Ext.apply(me, {
			tbar: [
				me.addAction('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					iconCls: 'wt-icon-saveClose-xs',
					handler: function() {
						me.saveTask();
					}
				}),
				'-',
				me.addAction('delete', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete-xs',
					handler: function() {
						me.deleteTask();
					}
				}),
				//'-',
				//me.addAction('print', {
				//	text: null,
				//	tooltip: WT.res('act-print.lbl'),
				//	iconCls: 'wt-icon-print-xs',
				//	handler: function() {
				//		//TODO: aggiungere l'azione 'salva' permettendo così la stampa senza chiudere la form
				//		me.printTask(me.getModel().getId());
				//	}
				//}),
				'->',
				WTF.localCombo('id', 'desc', {
					reference: 'fldowner',
					bind: '{record._profileId}',
					store: {
						autoLoad: true,
						model: 'WT.model.Simple',
						proxy: WTF.proxy(me.mys.ID, 'LookupCategoryRoots', 'roots')
					},
					fieldLabel: me.mys.res('task.fld-owner.lbl'),
					labelWidth: 75,
					listeners: {
						select: function(s, rec) {
							me.updateCategoryFilters();
						}
					}
				}), 
				WTF.lookupCombo('categoryId', 'name', {
					xtype: 'soiconcombo',
					reference: 'fldcategory',
					bind: '{record.categoryId}',
					store: {
						autoLoad: true,
						model: me.mys.preNs('model.CategoryLkp'),
						proxy: WTF.proxy(me.mys.ID, 'LookupCategoryFolders', 'folders')
					},
					iconClsField: 'colorCls',
					listeners: {
						select: function(s, rec) {
							me.onCategorySelect(rec);
						}
					}
				})
			]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			modelValidation: true,
			defaults: {
				labelWidth: 100
			},
			items: [{
				xtype: 'wtsuggestcombo',
				reference: 'fldsubject',
				bind: '{record.subject}',
				sid: me.mys.ID,
				suggestionContext: 'tasksubject',
				fieldLabel: me.mys.res('task.fld-subject.lbl'),
				anchor: '100%',
				listeners: {
					enterkey: function() {
						me.getAction('saveClose').execute();
					}
				}
			}, {
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: me.mys.res('task.fld-description.lbl'),
				height: 100,
				anchor: '100%'
			}, {
				xtype: 'formseparator'
			}, {
				xtype: 'container',
				layout: 'column',
				defaults: {
					xtype: 'wtform',
					style: 'width:50%'
				},
				items: [{
					items: [
						WTF.lookupCombo('id', 'desc', {
							bind: '{record.importance}',
							store: Ext.create(me.mys.preNs('store.Importance'), {
								autoLoad: true
							}),
							fieldLabel: me.mys.res('task.fld-importance.lbl')
						})
					]
				}, {
					items: [{
						xtype: 'checkbox',
						bind: '{isPrivate}',
						hideEmptyLabel: true,
						boxLabel: me.mys.res('task.fld-private.lbl')
					}]
				}]
			}, {
				xtype: 'formseparator'
			}, {
				xtype: 'container',
				layout: 'column',
				defaults: {
					xtype: 'wtform',
					style: 'width:50%'
				},
				items: [{
					items: [{
						xtype: 'datefield',
						bind: '{startDate}',
						startDay: WT.getStartDay(),
						fieldLabel: me.mys.res('task.fld-startDate.lbl')
					},
						WTF.lookupCombo('id', 'desc', {
							bind: '{record.status}',
							store: Ext.create(me.mys.preNs('store.Status'), {
								autoLoad: true
							}),
							fieldLabel: me.mys.res('task.fld-status.lbl'),
							listeners: {
								select: function(s, rec) {
									if (rec.get('id') === 'notstarted') 
										me.getModel().set('percentage',0);
									if (rec.get('id') === 'completed') 
										me.getModel().set('percentage',100);
								}
							}
						})
					]
				}, {
					items: [{
						xtype: 'datefield',
						bind: '{dueDate}',
						startDay: WT.getStartDay(),
						fieldLabel: me.mys.res('task.fld-dueDate.lbl'),
						labelWidth: 120
					}, {
						xtype: 'numberfield',
						step: 25,
						minValue: 0,
						maxValue: 100,
						allowDecimal: false,
						bind: '{record.percentage}',
						fieldLabel: me.mys.res('task.fld-percentage.lbl'),
						labelWidth: 120,
						listeners: {
							blur: function(s) {
								var nv = s.getValue();
								if (nv === 100) 
									me.getModel().set('status','completed');
								if (nv === 0) 
									me.getModel().set('status','notstarted');
							}
						}
					}]
				}]
			}, {
				xtype: 'formseparator'
			}, {
				xtype: 'fieldcontainer',
				layout: 'hbox',
				defaults: {
					labelWidth: 120,
					margin: '0 10 0 0'
				},
				items: [{
					width: 100,
					xtype: 'checkbox',
					reference: 'fldhasreminder',
					bind: '{hasReminder}',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('task.fld-reminderDate.lbl'),
				},{
					xtype: 'datefield',
					bind: {
						value: '{reminderDate}',
						disabled: '{!fldhasreminder.checked}'
					},
					startDay: WT.getStartDay(),
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{reminderTime}',
						disabled: '{!fldhasreminder.checked}'
					},
					format: WT.getShortTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}]
			}]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				owner = me.lref('fldowner');
		
		me.updateCategoryFilters();
		if (me.isMode(me.MODE_NEW)) {
			owner.setDisabled(false);
			me.getAction('delete').setDisabled(true);
		} else if(me.isMode(me.MODE_VIEW)) {
			me.getAction('saveClose').setDisabled(true);
			me.getAction('delete').setDisabled(true);
			owner.setDisabled(true);
		} else if(me.isMode(me.MODE_EDIT)) {
			owner.setDisabled(true);
		}
		me.lref('fldsubject').focus(true);
	},
	
	updateCategoryFilters: function() {
		this.lref('fldcategory').getStore().addFilter({
			property: '_profileId',
			value: this.getModel().get('_profileId')
		});
	},
	
	onCategorySelect: function(cat) {
		var mo = this.getModel();
		mo.set({
			isPrivate: cat.get('isPrivate')
		});
	},
	
	saveView: function(closeAfter) {
		var me = this,
				rec = me.getModel();
		if (!me.lref('fldhasreminder').getValue())
			rec.set('reminderDate',null);
		me.callParent(arguments);
	},
	
	saveTask: function() {
		var me = this;
		me.saveView(true);
	},
	
	deleteTask: function() {
		var me = this,
				rec = me.getModel();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				me.wait();
				WT.ajaxReq(me.mys.ID, 'ManageTasks', {
					params: {
						crud: 'delete',
						id: rec.get('taskId')
					},
					callback: function(success) {
						me.unwait();
						if(success) {
							me.fireEvent('viewsave', me, true, rec);
							me.closeView(false);
						}
					}
				});
			}
		}, me);
	},
	
	printTask: function(taskId) {
		var me = this;
		if(me.getModel().isDirty()) {
			WT.warn(WT.res('warn.print.notsaved'));
		} else {
			me.mys.printTasksDetail([taskId]);
		}
	}
	
});
