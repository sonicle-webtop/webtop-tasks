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
Ext.define('Sonicle.webtop.tasks.Service', {
	extend: 'WTA.sdk.Service',
	requires: [
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Color',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.SimpleModel',
		'Sonicle.webtop.tasks.store.Importance',
		'Sonicle.webtop.tasks.store.Status',
		'Sonicle.webtop.tasks.model.FolderNode',
		'Sonicle.webtop.tasks.model.GridTask',
		'Sonicle.webtop.tasks.view.Sharing',
		'Sonicle.webtop.tasks.view.Category',
		'Sonicle.webtop.tasks.view.Task',
		'Sonicle.webtop.tasks.view.CategoryChooser'
	],
	mixins: [
		'WTA.mixin.FoldersTree'
	],
	
	needsReload: true,
	
	init: function() {
		var me = this;
		
		me.initActions();
		me.initCxm();
		
		me.on('activate', me.onActivate, me);
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			items: [
				'-',
				me.getAction('print'),
				me.getAction('deleteTask2'),
				'->',
				{
					xtype: 'textfield',
					width: 200,
					triggers: {
						search: {
							cls: Ext.baseCSSPrefix + 'form-search-trigger',
							handler: function(s) {
								me.queryTasks(s.getValue());
							}
						}
					},
					listeners: {
						specialkey: function(s, e) {
							if(e.getKey() === e.ENTER) me.queryTasks(s.getValue());
						}
					}
				}
			]
		}));
		
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			layout: 'border',
			referenceHolder: true,
			title: me.getName(),
			items: [{
				region: 'center',
				xtype: 'treepanel',
				reference: 'trfolders',
				border: false,
				useArrows: true,
				rootVisible: false,
				store: {
					autoLoad: true,
					autoSync: true,
					model: 'Sonicle.webtop.tasks.model.FolderNode',
					proxy: WTF.apiProxy(me.ID, 'ManageFoldersTree', 'children', {
						writer: {
							allowSingle: false // Always wraps records into an array
						}
					}),
					root: {
						id: 'root',
						expanded: true
					},
					listeners: {
						write: function(s,op) {
							me.reloadTasks();
						}
					}
				},
				hideHeaders: true,
				listeners: {
					checkchange: function(n, ck) {
						me.showHideFolder(n, ck);
					},
					itemcontextmenu: function(vw, rec, itm, i, e) {
						if(rec.get('_type') === 'root') {
							WT.showContextMenu(e, me.getRef('cxmRootFolder'), {folder: rec});
						} else {
							WT.showContextMenu(e, me.getRef('cxmFolder'), {folder: rec});
						}
					}
				}
			}]
		}));
		
		me.setMainComponent(Ext.create({
			xtype: 'container',
			layout: 'border',
			referenceHolder: true,
			viewModel: {
				formulas: {
					selectedTask: {
						bind: {bindTo: '{gptasks.selection}'},
						get: function (val) {
							return val;
						}
					},
					selectedTaskPercentage: {
						bind: {bindTo: '{selectedTask.percentage}'},
						get: function (val) {
							return Ext.isEmpty(val) ? '' : val + '%';
						}
					},
					selectedTaskReminder: {
						bind: {bindTo: '{selectedTask.reminderDate}'},
						get: function (val) {
							return !Ext.isDate(val) ? WT.res('word.none.male') : Ext.Date.format(val, WT.getShortDateTimeFmt());
						}
					}
				}
			},
			items: [{
				region: 'center',
				xtype: 'grid',
				reference: 'gptasks',
				stateful: true,
				stateId: me.buildStateId('gptasks'),
				store: {
					model: 'Sonicle.webtop.tasks.model.GridTask',
					proxy: WTF.apiProxy(me.ID, 'ManageGridTasks', 'tasks', {
						extraParams: {
							query: null
						}
					})
				},
				viewConfig: {
					getRowClass: function (rec, indx) {
						if (rec.get('status') === 'completed')
							return 'wttasks-row-completed';
						if (Ext.isDate(rec.get('dueDate')) && Sonicle.Date.compare(rec.get('dueDate'),new Date(),false)>0 )
							return 'wttasks-row-expired';
						return '';
					}
				},
				selModel: {
					type: 'rowmodel',
					mode : 'MULTI'
				},
				columns: [{
					dataIndex: 'subject',
					header: me.res('gptasks.subject.lbl'),
					flex: 2
				}, {
					dataIndex: 'importance',
					header: me.res('gptasks.importance.lbl'),
					renderer: WTF.resColRenderer({
						id: me.ID,
						key: 'store.importance'
					}),
					flex: 1
				}, {
					dataIndex: 'dueDate',
					header: me.res('gptasks.dueDate.lbl'),
					xtype: 'datecolumn',
					format: WT.getShortDateFmt(),
					flex: 1
				}, {
					dataIndex: 'status',
					header: me.res('gptasks.status.lbl'),
					renderer: WTF.resColRenderer({
						id: me.ID,
						key: 'store.status'
					}),
					flex: 1
				}, {
					xtype: 'widgetcolumn',
					dataIndex: 'progress',
					header: me.res('gptasks.percentage.lbl'),
					widget: {
						xtype: 'progressbarwidget',
						textTpl: ['{percent:number("0")}%']
					},
					sortable: true,
					width: 120
				}, {
					xtype: 'socolorcolumn',
					dataIndex: 'categoryName',
					colorField: 'categoryColor',
					displayField: 'categoryName',
					header: me.res('gptasks.category.lbl'),
					width: 150
				}],
				listeners: {
					selectionchange: function() {
						me.updateDisabled('showTask');
						me.updateDisabled('printTask');
						me.updateDisabled('copyTask');
						me.updateDisabled('moveTask');
						me.updateDisabled('deleteTask');
					},
					rowdblclick: function(s, rec) {
						var er = me.toRightsObj(rec.get('_erights'));
						me.openTaskUI(er.UPDATE, rec.get('taskId'));
					},
					rowcontextmenu: function(s, rec, itm, i, e) {
						WT.showContextMenu(e, me.getRef('cxmGrid'), {
							task: rec,
							tasks: s.getSelection()
						});
					}
				}
			}, {
				region: 'east',
				xtype: 'wtfieldspanel',
				stateful: true,
				stateId: me.buildStateId('gptaskspreview'),
				split: true,
				collapsible: true,
				title: WT.res('word.preview'),
				width: 200,
				defaults: {
					labelAlign: 'top',
					readOnly: true,
					anchor: '100%'
				},
				items: [{
					xtype: 'textfield',
					bind: '{selectedTask.subject}',
					fieldLabel: me.res('task.fld-subject.lbl')
				}, WTF.lookupCombo('id', 'desc', {
					bind: '{selectedTask.importance}',
					store: Ext.create(me.preNs('store.Importance'), {
						autoLoad: true
					}),
					fieldLabel: me.res('task.fld-importance.lbl')
				}), {
					xtype: 'datefield',
					bind: '{selectedTask.startDate}',
					format: WT.getShortDateFmt(),
					fieldLabel: me.res('task.fld-startDate.lbl')
				},{
					xtype: 'checkbox',
					bind: '{selectedTask.isPrivate}',
					hideEmptyLabel: true,
					boxLabel: me.res('task.fld-private.lbl')
				}, {
					xtype: 'datefield',
					bind: '{selectedTask.dueDate}',
					format: WT.getShortDateFmt(),
					fieldLabel: me.res('task.fld-dueDate.lbl')
				}, WTF.lookupCombo('id', 'desc', {
					bind: '{selectedTask.status}',
					store: Ext.create(me.preNs('store.Status'), {
						autoLoad: true
					}),
					fieldLabel: me.res('task.fld-status.lbl')
				}), {
					xtype: 'displayfield',
					bind: '{selectedTaskPercentage}',
					labelAlign: 'left',
					labelWidth: 100,
					fieldLabel: me.res('gptasks.percentage.lbl')
				}, {
					xtype: 'displayfield',
					bind: '{selectedTaskReminder}',
					labelAlign: 'left',
					labelWidth: 100,
					fieldLabel: me.res('task.fld-reminderDate.lbl')
				}, {
					xtype: 'textareafield',
					bind: '{selectedTask.description}',
					fieldLabel: me.res('task.fld-description.lbl'),
					height: 250
				}]
			}]
		}));
	},
	
	trFolders: function() {
		return this.getToolComponent().lookupReference('trfolders');
	},
	
	gpTasks: function() {
		return this.getMainComponent().lookupReference('gptasks');
	},
	
	initActions: function() {
		var me = this;
		
		me.addAction('new', 'newTask', {
			handler: function() {
				me.getAction('addTask').execute();
			}
		});
		me.addAction('editSharing', {
			text: WT.res('sharing.tit'),
			iconCls: WTF.cssIconCls(WT.XID, 'sharing', 'xs'),
			handler: function() {
				var node = me.getSelectedNode(me.trFolders());
				if(node) me.editShare(node.getId());
			}
		});
		me.addAction('addCategory', {
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if(node) me.addCategoryUI(node.get('_domainId'), node.get('_userId'));
			}
		});
		me.addAction('editCategory', {
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if(node) me.editCategoryUI(node.get('_catId'));
			}
		});
		me.addAction('deleteCategory', {
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if(node) me.deleteCategoryUI(node);
			}
		});
		me.addAction('viewAllFolders', {
			iconCls: 'wt-icon-select-all-xs',
			handler: function() {
				me.showHideAllFolders(me.getSelectedRootFolder(me.trFolders()), true);
			}
		});
		me.addAction('viewNoneFolders', {
			iconCls: 'wt-icon-select-none-xs',
			handler: function() {
				me.showHideAllFolders(me.getSelectedRootFolder(me.trFolders()), false);
			}
		});
		me.addAction('showTask', {
			text: WT.res('act-open.lbl'),
			handler: function() {
				var rec = me.getSelectedTask(), er;
				if(rec) {
					er = me.toRightsObj(rec.get('_erights'));
					me.openTaskUI(er.UPDATE, rec.get('taskId'));
				}
			}
		});
		me.addAction('addTask', {
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if(node) me.addTaskUI(node.get('_pid'), node.get('_catId'));
			}
		});
		me.addAction('deleteTask', {
			text: WT.res('act-delete.lbl'),
			iconCls: 'wt-icon-delete-xs',
			handler: function() {
				var sel = me.getSelectedTasks();
				if(sel.length > 0) me.deleteTaskSel(sel);
			}
		});
		me.addAction('copyTask', {
			handler: function() {
				me.moveTasksSel(true, me.getSelectedTasks());
			}
		});
		me.addAction('moveTask', {
			handler: function() {
				me.moveTasksSel(false, me.getSelectedTasks());
			}
		});
		me.addAction('printTask', {
			text: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print-xs',
			handler: function() {
				var sel = me.getSelectedTasks();
				if(sel.length > 0) me.printTaskSel(sel);
			}
		});
		me.addAction('print', {
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print-xs',
			handler: function() {
				me.getAction('printTask').execute();
			}
		});
		me.addAction('deleteTask2', {
			text: null,
			tooltip: WT.res('act-delete.tip'),
			iconCls: 'wt-icon-delete-xs',
			handler: function() {
				me.getAction('deleteTask').execute();
			}
		});
		me.addAction('addTask2', {
			text: null,
			tooltip: me.res('act-addTask.lbl'),
			iconCls: me.cssIconCls('addTask', 'xs'),
			handler: function() {
				me.getAction('addTask').execute();
			}
		});
	},
	
	initCxm: function() {
		var me = this;
		
		me.addRef('cxmRootFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('addCategory'),
				'-',
				me.getAction('editSharing')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.folder,
							rr = me.toRightsObj(rec.get('_rrights'));
					me.getAction('addCategory').setDisabled(!rr.MANAGE);
					me.getAction('editSharing').setDisabled(!rr.MANAGE);
				}
			}
		}));
		
		me.addRef('cxmFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('editCategory'),
				me.getAction('deleteCategory'),
				me.getAction('addCategory'),
				'-',
				me.getAction('editSharing'),
				'-',
				me.getAction('viewAllFolders'),
				me.getAction('viewNoneFolders'),
				'-',
				me.getAction('addTask')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.folder,
							rr = me.toRightsObj(rec.get('_rrights')),
							fr = me.toRightsObj(rec.get('_frights')),
							er = me.toRightsObj(rec.get('_erights'));
					me.getAction('editCategory').setDisabled(!fr.UPDATE);
					me.getAction('deleteCategory').setDisabled(!fr.DELETE || rec.get('_builtIn'));
					me.getAction('addCategory').setDisabled(!rr.MANAGE);
					me.getAction('editSharing').setDisabled(!rr.MANAGE);
					me.getAction('addTask').setDisabled(!er.CREATE);
				}
			}
		}));
		
		me.addRef('cxmGrid', Ext.create({
			xtype: 'menu',
			items: [
				me.getAction('showTask'),
				{
					text: me.res('copyormove.lbl'),
					menu: {
						items: [
							me.getAction('moveTask'),
							me.getAction('copyTask')
						]
					}
				},
				me.getAction('printTask'),
				'-',
				me.getAction('deleteTask')
			]
		}));
	},
	
	onActivate: function() {
		var me = this,
				gp = me.gpTasks();
		
		if(me.needsReload) {
			me.needsReload = false;
			me.reloadTasks();
		}
		
		me.updateDisabled('showTask');
		me.updateDisabled('printTask');
		me.updateDisabled('copyTask');
		me.updateDisabled('moveTask');
		me.updateDisabled('deleteTask');
	},
	
	loadFolderNode: function(pid) {
		var me = this,
				sto = me.trFolders().getStore(),
				node;
		
		node = sto.findNode('_pid', pid, false);
		if (node) {
			sto.load({node: node});
			if(node.get('checked'))	me.reloadTasks();
		}
	},
	
	queryTasks: function(txt) {
		this.reloadTasks(txt);
	},
	
	reloadTasks: function(query) {
		var me = this, sto, pars;
		
		if(me.isActive()) {
			sto = me.gpTasks().getStore();
			pars = {};
			if(query !== undefined) Ext.apply(pars, {query: query});
			WTU.loadWithExtraParams(sto, pars);
		} else {
			me.needsReload = true;
		}
	},
	
	getSelectedTask: function(forceSingle) {
		if(forceSingle === undefined) forceSingle = true;
		var sel = this.getSelectedTasks();
		if(forceSingle && sel.length !== 1) return null;
		return (sel.length > 0) ? sel[0] : null;
	},
	
	getSelectedTasks: function() {
		return this.gpTasks().getSelection();
	},
	
	addCategoryUI: function(domainId, userId) {
		var me = this;
		me.addCategory(domainId, userId, {
			callback: function(success, model) {
				if(success) me.loadFolderNode(model.get('_profileId'));
			}
		});
	},
	
	editCategoryUI: function(categoryId) {
		var me = this;
		me.editCategory(categoryId, {
			callback: function(success, model) {
				if(success) me.loadFolderNode(model.get('_profileId'));
			}
		});
	},
	
	deleteCategoryUI: function(node) {
		WT.confirm(this.res('category.confirm.delete', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if(bid === 'yes') node.drop();
		}, this);
	},
	
	addTaskUI: function(ownerId, categoryId) {
		var me = this;
		me.addTask(ownerId, categoryId, {
			callback: function(success) {
				if(success) me.reloadTasks();
			}
		});
	},
	
	openTaskUI: function(edit, taskId) {
		var me = this;
		me.openTask(edit, taskId, {
			callback: function(success) {
				if(success && edit) me.reloadTasks();
			}
		});
	},
	
	printTasksDetail: function(taskIds) {
		var me = this, url;
		url = WTF.processBinUrl(me.ID, 'PrintTasksDetail', {ids: WTU.arrayAsParam(taskIds)});
		Sonicle.URLMgr.openFile(url, {filename: 'tasks-detail', newWindow: true});
	},
	
	printTaskSel: function(sel) {
		var me = this;
		me.printTasksDetail(me.selectionIds(sel));
	},
	
	deleteTaskSel: function(sel) {
		var me = this,
			sto = me.gpTasks().getStore(),
			ids = me.selectionIds(sel),
			msg;
		
		if(sel.length === 1) {
			msg = me.res('task.confirm.delete', Ext.String.ellipsis(sel[0].get('subject'), 40));
		} else {
			msg = me.res('gptasks.confirm.delete.selection');
		}
		
		WT.confirm(msg, function(bid) {
			if(bid === 'yes') {
				me.deleteTasks(ids, {
					callback: function(success) {
						if(success) sto.remove(sel);
						//me.reloadTasks();
					}
				});
			}
		});
	},
	
	moveTasksSel: function(copy, sel) {
		var me = this,
				id = sel[0].get('taskId'),
				pid = sel[0].get('_profileId'),
				cat = sel[0].get('categoryId');
		
		me.confirmMoveTask(copy, id, pid, cat, {
			callback: function() {
				me.reloadTasks();
			}
		});
	},
	
	confirmMoveTask: function(copy, id, ownerId, catId, opts) {
		var me = this,
				vw = me.createCategoryChooser(copy, ownerId, catId);
		
		vw.getView().on('viewok', function(s) {
			me.moveTask(copy, id, s.getVMData().categoryId, opts);
		});
		vw.show();
	},
	
	editShare: function(id) {
		var me = this,
				vct = WT.createView(me.ID, 'view.Sharing');
		
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					id: id
				}
			});
		});
	},
	
	addCategory: function(domainId, userId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Category');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					domainId: domainId,
					userId: userId
				}
			});
		});
	},
	
	editCategory: function(categoryId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Category');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('edit', {
				data: {
					categoryId: categoryId
				}
			});
		});
	},
	
	showTask: function(edit, taskId, opts) {
		this.openTask(edit, taskId, opts);
	},
	
	addTask: function(ownerId, categoryId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Task');
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin('new', {
				data: {
					_profileId: ownerId,
					categoryId: categoryId
				}
			});
		});
	},
	
	editTask: function(taskId, opts) {
		this.openTask(true, taskId, opts);
	},
	
	openTask: function(edit, taskId, opts) {
		opts = opts || {};
		var me = this,
				vct = WT.createView(me.ID, 'view.Task'),
				mode = edit ? 'edit' : 'view';
		
		vct.getView().on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vct.show(false, function() {
			vct.getView().begin(mode, {
				data: {
					taskId: taskId
				}
			});
		});
	},
	
	deleteTasks: function(taskIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageTasks', {
			params: {
				crud: 'delete',
				ids: WTU.arrayAsParam(taskIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	moveTask: function(copy, taskId, targetCategoryId, opts) {
		opts = opts || {};
		var me = this;
		
		WT.ajaxReq(me.ID, 'ManageTasks', {
			params: {
				crud: 'move',
				copy: copy,
				id: taskId,
				targetCategoryId: targetCategoryId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	selectionIds: function(sel) {
		var ids = [];
		Ext.iterate(sel, function(rec) {
			ids.push(rec.getId());
		});
		return ids;
	},
	
	/**
	 * @private
	 */
	updateDisabled: function(action) {
		var me = this,
				dis = me.isDisabled(action);
		
		if(action === 'printTask') {
			me.setActionDisabled('print', dis);
			me.setActionDisabled(action, dis);
		} else if (action === 'deleteTask') {
			me.setActionDisabled(action, dis);
			me.setActionDisabled('deleteTask2', dis);
		} else {
			me.setActionDisabled(action, dis);
		}
	},
	
	/**
	 * @private
	 */
	isDisabled: function(action) {
		var me = this, sel, er;
		
		switch(action) {
			case 'showTask':
			case 'copyTask':
				sel = me.getSelectedTasks();
				if(sel.length === 1) {
					return false;
				} else {
					return true;
				}
			case 'moveTask':
				sel = me.getSelectedTasks();
				if(sel.length === 1) {
					er = me.toRightsObj(sel[0].get('_erights'));
					return !er.UPDATE;
				} else {
					return true;
				}
			case 'deleteTask':
				sel = me.getSelectedTasks();
				if(sel.length === 0) {
					return true;
				} else if(sel.length === 1) {
					er = me.toRightsObj(sel[0].get('_erights'));
					return !er.DELETE;
				} else {
					for(var i=0; i<sel.length; i++) {
						if(!me.toRightsObj(sel[i].get('_erights')).DELETE) return true;
					}
					return false;
				}
			case 'printTask':
				sel = me.getSelectedTasks();
				if(sel.length > 0) {
					return false;
				} else {
					return true;
				}
				break;
		}
	},
	
	/**
	 * @private
	 */
	createCategoryChooser: function(copy, ownerId, catId) {
		var me = this;
		return WT.createView(me.ID, 'view.CategoryChooser', {
			viewCfg: {
				dockableConfig: {
					title: me.res(copy ? 'act-copyTask.lbl' : 'act-moveTask.lbl')
				},
				ownerId: ownerId,
				categoryId: catId
			}
		});
	}
});
