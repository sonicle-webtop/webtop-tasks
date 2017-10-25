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
		'Sonicle.webtop.tasks.model.GridTask'
	],
	uses: [
		'Sonicle.webtop.tasks.view.Sharing',
		'Sonicle.webtop.tasks.view.Category',
		'Sonicle.webtop.tasks.view.Task',
		'Sonicle.webtop.tasks.view.CategoryChooser',
		'Sonicle.webtop.tasks.view.HiddenCategories',
		'Sonicle.webtop.tasks.ServiceApi',
		'Sonicle.webtop.tasks.portlet.Tasks'
	],
	mixins: [
		'WTA.mixin.FoldersTree'
	],
	
	needsReload: true,
	api: null,
	
	getApiInstance: function() {
		var me = this;
		if (!me.api) me.api = Ext.create('Sonicle.webtop.tasks.ServiceApi', {service: me});
		return me.api;
	},
	
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
				me.getAct('refresh'),
				me.getAct('print'),
				me.getAct('deleteTask2'),
				'->',
				{
					xtype: 'textfield',
					tooltip: me.res('textfield.tip'),
					plugins: ['sofieldtooltip'],
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
					},
					width: 200
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
					startDay: WT.getStartDay(),
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
					startDay: WT.getStartDay(),
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
		
		me.addAct('new', 'newTask', {
			handler: function() {
				me.getAct('addTask').execute();
			}
		});
		me.addAct('refresh', {
			text: '',
			tooltip: WT.res('act-refresh.lbl'),
			iconCls: 'wt-icon-refresh-xs',
			handler: function() {
				me.reloadTasks();
			}
		});
		me.addAct('editSharing', {
			text: WT.res('sharing.tit'),
			tooltip: null,
			iconCls: WTF.cssIconCls(WT.XID, 'sharing', 'xs'),
			handler: function() {
				var node = me.getSelectedNode(me.trFolders());
				if (node) me.editShare(node.getId());
			}
		});
		me.addAct('manageHiddenCategories', {
			tooltip: null,
			handler: function() {
				var node = me.getSelectedRootFolder(me.trFolders());
				if (node) me.manageHiddenCategoriesUI(node);
			}
		});
		me.addAct('hideCategory', {
			tooltip: null,
			handler: function() {
				var node = me.getSelectedNode(me.trFolders());
				if (node) me.hideCategoryUI(node);
			}
		});
		me.addAct('addCategory', {
			tooltip: null,
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if (node) me.addCategoryUI(node.get('_domainId'), node.get('_userId'));
			}
		});
		me.addAct('editCategory', {
			tooltip: null,
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if (node) me.editCategoryUI(node.get('_catId'));
			}
		});
		me.addAct('deleteCategory', {
			tooltip: null,
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if (node) me.deleteCategoryUI(node);
			}
		});
		me.addAct('categoryColor', {
			text: me.res('mni-categoryColor.lbl'),
			tooltip: null,
			menu: {
				showSeparator: false,
				items: [{
						xtype: 'colorpicker',
						colors: WT.getColorPalette(),
						listeners: {
							select: function(s, color) {
								var node = me.getSelectedFolder(me.trFolders());
								me.getRef('cxmFolder').hide();
								if (node) me.updateCategoryColorUI(node, '#'+color);
							}
						}
					},
					'-',
					me.addAct('restoreCategoryColor', {
						tooltip: null,
						handler: function() {
							var node = me.getSelectedFolder(me.trFolders());
							if (node) me.updateCategoryColorUI(node, null);
						}
					})
				]
			}
		});
		me.addAct('viewThisFolderOnly', {
			tooltip: null,
			iconCls: 'wt-icon-select-one-xs',
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if(node) me.showOneF3FolderOnly(me.getSelectedRootFolder(me.trFolders()), node.getId());
			}
		});
		me.addAct('viewAllFolders', {
			tooltip: null,
			iconCls: 'wt-icon-select-all-xs',
			handler: function() {
				var node = me.getSelectedRootFolder(me.trFolders());
				if (node) {
					if (node.isLoaded()) {
						me.showHideAllF3Folders(node, true);
					} else {
						me.updateCheckedFoldersUI(node, true);
					}
				}
			}
		});
		me.addAct('viewNoneFolders', {
			tooltip: null,
			iconCls: 'wt-icon-select-none-xs',
			handler: function() {
				var node = me.getSelectedRootFolder(me.trFolders());
				if (node) {
					if (node.isLoaded()) {
						me.showHideAllF3Folders(node, false);
					} else {
						me.updateCheckedFoldersUI(node, false);
					}
				}
			}
		});
		me.addAct('showTask', {
			text: WT.res('act-open.lbl'),
			tooltip: null,
			handler: function() {
				var rec = me.getSelectedTask(), er;
				if (rec) {
					er = me.toRightsObj(rec.get('_erights'));
					me.openTaskUI(er.UPDATE, rec.get('taskId'));
				}
			}
		});
		me.addAct('addTask', {
			tooltip: null,
			handler: function() {
				var node = me.getSelectedFolder(me.trFolders());
				if (node) me.addTaskUI(node.get('_pid'), node.get('_catId'));
			}
		});
		me.addAct('deleteTask', {
			text: WT.res('act-delete.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-delete-xs',
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) me.deleteTaskSel(sel);
			}
		});
		me.addAct('copyTask', {
			tooltip: null,
			handler: function() {
				me.moveTasksSel(true, me.getSelectedTasks());
			}
		});
		me.addAct('moveTask', {
			tooltip: null,
			handler: function() {
				me.moveTasksSel(false, me.getSelectedTasks());
			}
		});
		me.addAct('printTask', {
			text: WT.res('act-print.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-print-xs',
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) me.printTaskSel(sel);
			}
		});
		me.addAct('print', {
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print-xs',
			handler: function() {
				me.getAct('printTask').execute();
			}
		});
		me.addAct('deleteTask2', {
			text: null,
			tooltip: WT.res('act-delete.lbl'),
			iconCls: 'wt-icon-delete-xs',
			handler: function() {
				me.getAct('deleteTask').execute();
			}
		});
		me.addAct('addTask2', {
			text: null,
			tooltip: me.res('act-addTask.lbl'),
			iconCls: me.cssIconCls('addTask', 'xs'),
			handler: function() {
				me.getAct('addTask').execute();
			}
		});
	},
	
	initCxm: function() {
		var me = this;
		
		me.addRef('cxmRootFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('addCategory'),
				'-',
				{
					text: me.res('mni-viewFolders.lbl'),
					menu: {
						items: [
							me.getAct('viewAllFolders'),
							me.getAct('viewNoneFolders')
						]
					}
				},
				'-',
				me.getAct('editSharing'),
				me.getAct('manageHiddenCategories')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.folder,
							mine = rec.getId().startsWith('0'),
							rr = me.toRightsObj(rec.get('_rrights'));
					me.getAct('addCategory').setDisabled(!rr.MANAGE);
					me.getAct('editSharing').setDisabled(!rr.MANAGE);
					me.getAct('manageHiddenCategories').setDisabled(mine);
				}
			}
		}));
		
		me.addRef('cxmFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('editCategory'),
				me.getAct('deleteCategory'),
				me.getAct('addCategory'),
				'-',
				{
					text: me.res('mni-viewFolder.lbl'),
					menu: {
						items: [
							me.getAct('viewThisFolderOnly'),
							me.getAct('viewAllFolders'),
							me.getAct('viewNoneFolders')
						]
					}
				},
				'-',
				me.getAct('editSharing'),
				me.getAct('hideCategory'),
				me.getAct('categoryColor'),
				'-',
				me.getAct('addTask')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var rec = s.menuData.folder,
							mine = rec.getId().startsWith('0'),
							rr = me.toRightsObj(rec.get('_rrights')),
							fr = me.toRightsObj(rec.get('_frights')),
							er = me.toRightsObj(rec.get('_erights'));
					me.getAct('editCategory').setDisabled(!fr.UPDATE);
					me.getAct('deleteCategory').setDisabled(!fr.DELETE || rec.get('_builtIn'));
					me.getAct('addCategory').setDisabled(!rr.MANAGE);
					me.getAct('editSharing').setDisabled(!rr.MANAGE);
					me.getAct('addTask').setDisabled(!er.CREATE);
					me.getAct('hideCategory').setDisabled(mine);
					me.getAct('categoryColor').setDisabled(mine);
					if (!mine) s.down('colorpicker').select(rec.get('_color'), true);
				}
			}
		}));
		
		me.addRef('cxmGrid', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('showTask'),
				{
					text: me.res('mni-copyormove.lbl'),
					menu: {
						items: [
							me.getAct('moveTask'),
							me.getAct('copyTask')
						]
					}
				},
				me.getAct('printTask'),
				'-',
				me.getAct('deleteTask')
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
		if (!Ext.isEmpty(txt)) {
			this.reloadTasks(txt);
		}
	},
	
	reloadTasks: function(query) {
		var me = this,
				pars = {},
				sto;
		
		if(me.isActive()) {
			sto = me.gpTasks().getStore();
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
	
	manageHiddenCategoriesUI: function(node) {
		var me = this,
				vct = me.createHiddenCategories(node.getId());
		
		vct.getView().on('viewcallback', function(s, success, json) {
			if (success) {
				Ext.iterate(json.data, function(pid) {
					me.loadFolderNode(pid);
				});
			}
		});
		vct.show();
	},
	
	hideCategoryUI: function(node) {
		var me = this;
		WT.confirm(this.res('category.confirm.hide', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if(bid === 'yes') {
				me.updateCategoryVisibility(node.get('_catId'), true, {
					callback: function(success) {
						if(success) {
							me.loadFolderNode(node.get('_pid'));
							me.showHideF3Node(node, false);
						}
					}
				});
			}
		}, this);
	},
	
	updateCategoryColorUI: function(node, color) {
		var me = this;
		me.updateCategoryColor(node.get('_catId'), color, {
			callback: function(success) {
				if(success) {
					me.loadFolderNode(node.get('_pid'));
					if (node.get('_visible')) me.reloadTasks();
				}
			}
		});
	},
	
	updateCheckedFoldersUI: function(node, checked) {
		var me = this;
		me.updateCheckedFolders(node.getId(), checked, {
			callback: function(success) {
				if(success) {
					if (node.get('_visible')) {
						me.reloadTasks();
					} else {
						if (checked) me.showHideF3Node(node, checked);
					}
				}
			}
		});
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
				vct = me.createCategoryChooser(copy, ownerId, catId);
		
		vct.getView().on('viewok', function(s) {
			me.moveTask(copy, id, s.getVMData().categoryId, opts);
		});
		vct.show();
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
					userId: userId,
					sync: me.getVar('defaultCategorySync')
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
	
	updateCategoryVisibility: function(categoryId, hidden, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageHiddenCategories', {
			params: {
				crud: 'update',
				categoryId: categoryId,
				hidden: hidden
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateCategoryColor: function(categoryId, color, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetCategoryColor', {
			params: {
				id: categoryId,
				color: color
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateCheckedFolders: function(rootId, checked, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'UpdateCheckedFolders', {
			params: {
				rootId: rootId,
				checked: checked
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
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
			me.setActDisabled('print', dis);
			me.setActDisabled(action, dis);
		} else if (action === 'deleteTask') {
			me.setActDisabled(action, dis);
			me.setActDisabled('deleteTask2', dis);
		} else {
			me.setActDisabled(action, dis);
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
					return !er.DELETE;
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
	createHiddenCategories: function(rootNodeId) {
		var me = this;
		return WT.createView(me.ID, 'view.HiddenCategories', {
			viewCfg: {
				action: 'ManageHiddenCategories',
				extraParams: {
					crud: 'list',
					rootId: rootNodeId
				}
			}
		});
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
