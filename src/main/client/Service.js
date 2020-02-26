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
		'Sonicle.grid.column.Tag',
		'Sonicle.menu.TagItem',
		'Sonicle.tree.Column',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.SimpleModel',
		'Sonicle.webtop.tasks.store.Importance',
		'Sonicle.webtop.tasks.store.Status',
		'Sonicle.webtop.tasks.model.FolderNode',
		'Sonicle.webtop.tasks.model.GridTask'
	],
	uses: [
		'WTA.util.FoldersTree',
		'WTA.ux.SelectTagsBox',
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
	
	treeSelEnabled: false,
	
	getApiInstance: function() {
		var me = this;
		if (!me.api) me.api = Ext.create('Sonicle.webtop.tasks.ServiceApi', {service: me});
		return me.api;
	},
	
	init: function() {
		var me = this,
				tagsStore = WT.getTagsStore();
		
		me.initActions();
		me.initCxm();
		
		me.on('activate', me.onActivate, me);
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			items: [
				'-',
				me.getAct('refresh'),
				me.getAct('printTask2'),
				me.getAct('deleteTask2'),
				'->',
				{
					xtype: 'wtsearchfield',
					reference: 'fldsearch',
					highlightKeywords: ['subject'],
					fields: [
						{
							name: 'subject',
							type: 'string',
							label: me.res('fld-search.field.subject.lbl')
						}, {
							name: 'description',
							type: 'string',
							label: me.res('fld-search.field.description.lbl')
						}, {
							name: 'after',
							type: 'date',
							labelAlign: 'left',
							label: me.res('fld-search.field.after.lbl')
						}, {
							name: 'before',
							type: 'date',
							labelAlign: 'left',
							label: me.res('fld-search.field.before.lbl')
						}, {
							name: 'private',
							type: 'boolean',
							boolKeyword: 'is',
							label: me.res('fld-search.field.private.lbl')
						}, {
							name: 'done',
							type: 'boolean',
							boolKeyword: 'is',
							label: me.res('fld-search.field.done.lbl')
						}, {
							name: 'tag',
							type: 'tag[]',
							label: me.res('fld-search.field.tags.lbl'),
							customConfig: {
								valueField: 'id',
								displayField: 'name',
								colorField: 'color',
								store: WT.getTagsStore() // This is filterable, let's do a separate copy!
							}
						}
					],
					tooltip: me.res('fld-search.tip'),
					searchTooltip: me.res('fld-search.tip'),
					emptyText: me.res('fld-search.emp'),
					listeners: {
						query: function(s, value, qObj) {
							me.queryTasks(qObj);
						}
					}
				},
				'->'
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
				selModel: {
					mode: 'SIMPLE',
					toggleOnClick: true,
					ignoreRightMouseSelection: true
				},
				hideHeaders: true,
				columns: [{
					xtype: 'sotreecolumn',
					dataIndex: 'text',
					renderer: WTA.util.FoldersTree.coloredCheckboxTreeRenderer({
						defaultText: me.res('category.fld-default.lbl').toLowerCase()
					}),
					flex: 1
				}],
				listeners: {
					checkchange: function(n, ck) {
						n.refreshActive();
					},
					beforeselect: function(s, rec) {
						if (me.treeSelEnabled === false) return false;
					},
					beforedeselect: function(s, rec) {
						if (me.treeSelEnabled === false) return false;
					},
					itemclick: function(s, rec, itm, i, e) {
						if (me.treeSelEnabled === false) {
							if (!e.getTarget(s.checkboxSelector, itm)) {
								s.setChecked(rec, !rec.get('checked'), e);
							}
						}
					},
					itemcontextmenu: function(vw, rec, itm, i, e) {
						if (rec.isFolderRoot()) {
							WT.showContextMenu(e, me.getRef('cxmRootFolder'), {node: rec});
						} else {
							WT.showContextMenu(e, me.getRef('cxmFolder'), {node: rec});
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
				//FIXME: keep commented otherwise columns size may be wrong
				//stateful: true,
				//stateId: me.buildStateId('gptasks'),
				store: {
					model: 'Sonicle.webtop.tasks.model.GridTask',
					proxy: WTF.apiProxy(me.ID, 'ManageGridTasks', 'tasks', {
						extraParams: {
							query: null
						}
					}),
					listeners: {
						load: function() {
							var el = me.gpTasks().getEl(),
									search = me.getToolbar().lookupReference('fldsearch');
							search.highlight(el, '.x-grid-item-container');
						}
					}
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
					xtype: 'soiconcolumn',
					dataIndex: 'importance',
					hideable: false,
					header: WTF.headerWithGlyphIcon('fa fa-exclamation'),
					getIconCls: function(v, rec) {
						return ['wt-icon-priority3-low', '', 'wt-icon-priority3-high'][v];
					},
					getTip: function(v, rec) {
						return me.res('gptasks.importance.lbl') + ': ' + me.res('store.importance.' + v);
					},
					iconSize: 16,
					width: 40
				}, {
					xtype: 'sotagcolumn',
					dataIndex: 'tags',
					header: me.res('gptasks.tags.lbl'),
					tagsStore: tagsStore,
					width: 90
				}, {
					dataIndex: 'subject',
					header: me.res('gptasks.subject.lbl'),
					flex: 2
				}, /*{
					dataIndex: 'importance',
					header: me.res('gptasks.importance.lbl'),
					renderer: WTF.resColRenderer({
						id: me.ID,
						key: 'store.importance'
					}),
					flex: 1
				},*/ {
					dataIndex: 'dueDate',
					header: me.res('gptasks.dueDate.lbl'),
					xtype: 'datecolumn',
					format: WT.getShortDateFmt(),
					width: 120
				}, {
					dataIndex: 'status',
					header: me.res('gptasks.status.lbl'),
					renderer: WTF.resColRenderer({
						id: me.ID,
						key: 'store.status'
					}),
					width: 120
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
						var er = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
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
				hidden: !WT.plTags.desktop,
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
		var me = this,
				hdscale = WT.getHeaderScale();
		
		if (WT.isPermitted(WT.ID, 'CUSTOM_FIELDS', 'MANAGE')) {
			me.addAct('toolbox', 'manageCustomFields', {
				text: WT.res('act-manageCustomFields.lbl'),
				tooltip: WT.res('act-manageCustomFields.tip'),
				iconCls: 'wt-icon-customField',
				handler: function() {
					me.showCustomFieldsUI();
				}
			});
			me.addAct('toolbox', 'manageCustomPanels', {
				text: WT.res('act-manageCustomPanels.lbl'),
				tooltip: WT.res('act-manageCustomPanels.tip'),
				iconCls: 'wt-icon-customPanel',
				handler: function() {
					me.showCustomPanelsUI();
				}
			});
		}
		
		me.addAct('new', 'newTask', {
			ignoreSize: true,
			handler: function() {
				me.getAct('addTask').execute();
			}
		});
		
		me.addAct('editSharing', {
			text: WT.res('sharing.tit'),
			tooltip: null,
			iconCls: WTF.cssIconCls(WT.XID, 'sharing'),
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.editShare(node.getId());
			}
		});
		me.addAct('manageHiddenCategories', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.manageHiddenCategoriesUI(node);
			}
		});
		me.addAct('hideCategory', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.hideCategoryUI(node);
			}
		});
		me.addAct('addCategory', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.addCategoryUI(node.get('_domainId'), node.get('_userId'));
			}
		});
		me.addAct('editCategory', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.editCategoryUI(node.get('_catId'));
			}
		});
		me.addAct('deleteCategory', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.deleteCategoryUI(node);
			}
		});
		me.addAct('applyTags', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.applyCategoryTagsUI(node);
			}
		});
		me.addAct('tags', {
			text: me.res('mni-tags.lbl'),
			tooltip: null,
			menu: {
				xtype: 'sostoremenu',
				useItemIdPrefix: true,
				store: WT.getTagsStore(),
				textField: 'name',
				tagField: 'id',
				bottomStaticItems: [
					'-',
					me.addAct('manageTags', {
						tooltip: null,
						handler: function(s, e) {
							var sel = me.getSelectedTasks();
							if (sel.length > 0) me.manageTaskItemsTagsUI(sel);
						}
					})
				],
				itemCfgCreator: function(rec) {
					return {
						xclass: 'Sonicle.menu.TagItem',
						color: rec.get('color'),
						hideOnClick: true
					};
				},
				listeners: {
					beforeshow: function(s) {
						s.setCheckedItems(me.toMutualTags(me.getSelectedTasks()) || []);
					},
					click: function(s, itm, e) {
						if (itm.tag) {
							var ids = WTU.collectIds(me.getSelectedTasks());
							me.updateTaskItemsTags(ids, !itm.checked ? 'unset' : 'set', [itm.tag], {
								callback: function(success) {
									if (success) me.reloadTasks();
								}
							});
						}
					}
				}
			}
		});
		me.addAct('categoryColor', {
			text: me.res('mni-categoryColor.lbl'),
			tooltip: null,
			menu: {
				showSeparator: false,
				itemId: 'categoryColor',
				items: [{
						xtype: 'colorpicker',
						colors: WT.getColorPalette(),
						listeners: {
							select: function(s, color) {
								var node = s.menuData.node;
								me.getRef('cxmFolder').hide();
								if (node) me.updateCategoryColorUI(node, '#'+color);
							}
						}
					},
					'-',
					me.addAct('restoreCategoryColor', {
						tooltip: null,
						handler: function(s, e) {
							var node = e.menuData.node;
							if (node) me.updateCategoryColorUI(node, null);
						}
					})
				]
			}
		});
		var onItemClick = function(s, e) {
			var node = e.menuData.node;
			if (node && s.checked) me.updateCategorySyncUI(node, s.getItemId());
		};
		me.addAct('categorySync', {
			text: me.res('mni-categorySync.lbl'),
			tooltip: null,
			menu: {
				itemId: 'categorySync',
				items: [{
						itemId: 'O',
						text: me.res('store.sync.O'),
						group: 'categorySync',
						checked: false,
						listeners: {
							click: onItemClick
						}
					}, {
						itemId: 'R',
						text: me.res('store.sync.R'),
						group: 'categorySync',
						checked: false,
						listeners: {
							click: onItemClick
						}
					}, {
						itemId: 'W',
						text: me.res('store.sync.W'),
						group: 'categorySync',
						checked: false,
						listeners: {
							click: onItemClick
						}
					}
				]
			}
		});
		me.addAct('viewThisFolderOnly', {
			tooltip: null,
			iconCls: 'wt-icon-select-one',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree.activateSingleFolder(node.getFolderRootNode(), node.getId());
			}
		});
		me.addAct('viewAllFolders', {
			tooltip: null,
			iconCls: 'wt-icon-select-all',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree.setActiveAllFolders(node.getFolderRootNode(), true);
			}
		});
		me.addAct('viewNoneFolders', {
			tooltip: null,
			iconCls: 'wt-icon-select-none',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree.setActiveAllFolders(node.getFolderRootNode(), false);
			}
		});
		me.addAct('showTask', {
			text: WT.res('act-open.lbl'),
			tooltip: null,
			handler: function() {
				var rec = me.getSelectedTask(), er;
				if (rec) {
					er = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
					me.openTaskUI(er.UPDATE, rec.get('taskId'));
				}
			}
		});
		me.addAct('addTask', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var node = (e && e.menuData) ? e.menuData.node : WTA.util.FoldersTree.getTargetFolder(me.trFolders());
				if (node) me.addTaskUI(node.get('_pid'), node.get('_catId'));
			}
		});
		me.addAct('deleteTask', {
			text: WT.res('act-delete.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-delete',
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
			iconCls: 'wt-icon-print',
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) me.printTaskSel(sel);
			}
		});
		
		me.addAct('refresh', {
			scale: hdscale,
			text: '',
			tooltip: WT.res('act-refresh.lbl'),
			iconCls: 'wt-icon-refresh',
			handler: function() {
				me.reloadTasks();
			}
		});
		me.addAct('printTask2', {
			scale: hdscale,
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print',
			handler: function() {
				me.getAct('printTask').execute();
			}
		});
		me.addAct('deleteTask2', {
			scale: hdscale,
			text: null,
			tooltip: WT.res('act-delete.lbl'),
			iconCls: 'wt-icon-delete',
			handler: function() {
				me.getAct('deleteTask').execute();
			}
		});
		me.addAct('addTask2', {
			scale: hdscale,
			ignoreSize: true,
			text: null,
			tooltip: me.res('act-addTask.lbl'),
			iconCls: me.cssIconCls('addTask'),
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
					var rec = s.menuData.node,
							mine = rec.isPersonalNode(),
							rr = WTA.util.FoldersTree.toRightsObj(rec.get('_rrights'));
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
				{
					text: me.res('mni-customizeFolder.lbl'),
					menu: {
						items: [
							me.getAct('hideCategory'),
							me.getAct('categoryColor'),
							me.getAct('categorySync')
						]
					}
				},
				'-',
				me.getAct('applyTags'),
				'-',
				me.getAct('addTask')
				//TODO: azioni altri servizi?
			],
			listeners: {
				beforeshow: function(s) {
					var FT = WTA.util.FoldersTree,
							rec = s.menuData.node,
							mine = rec.isPersonalNode(),
							rr = FT.toRightsObj(rec.get('_rrights')),
							fr = FT.toRightsObj(rec.get('_frights')),
							er = FT.toRightsObj(rec.get('_erights'));
					me.getAct('editCategory').setDisabled(!fr.UPDATE);
					me.getAct('deleteCategory').setDisabled(!fr.DELETE || rec.get('_builtIn'));
					me.getAct('editSharing').setDisabled(!rr.MANAGE);
					me.getAct('addTask').setDisabled(!er.CREATE);
					me.getAct('hideCategory').setDisabled(mine);
					me.getAct('restoreCategoryColor').setDisabled(mine);
					me.getAct('applyTags').setDisabled(!er.UPDATE);
					
					var picker = s.down('menu#categoryColor').down('colorpicker');
					picker.menuData = s.menuData; // Picker's handler doesn't carry the event, injects menuData inside the picket itself
					picker.select(rec.get('_color'), true);
					s.down('menu#categorySync').getComponent(rec.get('_sync')).setChecked(true);
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
				me.getAct('tags'),
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
	
	loadRootNode: function(pid, reloadItemsIf) {
		var me = this,
				sto = me.trFolders().getStore(),
				node;
		
		node = sto.findNode('_pid', pid, false);
		if (node) {
			sto.load({node: node});
			if (reloadItemsIf && node.get('checked')) me.reloadTasks();
		}
	},
	
	queryTasks: function(query) {
		var isString = Ext.isString(query),
				obj = {
					allText: isString ? query : query.anyText,
					conditions: isString ? [] : query.conditionArray
				};
			this.reloadTasks({query: Ext.JSON.encode(obj)});
	},
	
	reloadTasks: function(opts) {
		opts = opts || {};
				var me = this, sto, pars = {};
		if(me.isActive()) {
			sto = me.gpTasks().getStore();
			if(opts.query !== undefined) Ext.apply(pars, {query: opts.query});
			WTU.loadWithExtraParams(sto, pars);
		} else {
			me.needsReload = true;
		}
	},
	
	getSelectedTask: function(forceSingle) {
		if (forceSingle === undefined) forceSingle = true;
		var sel = this.getSelectedTasks();
		if (forceSingle && sel.length !== 1) return null;
		return (sel.length > 0) ? sel[0] : null;
	},
	
	getSelectedTasks: function() {
		return this.gpTasks().getSelection();
	},
	
	showCustomPanelsUI: function() {
		var me = this;
		WT.createView(WT.ID, 'view.CustomPanels', {
			swapReturn: true,
			viewCfg: {
				dockableConfig: {
					title: WT.res('customPanels.tit') + ' [' + me.getName() + ']'
				},
				serviceId: me.ID
			}
		}).showView();
	},
	
	showCustomFieldsUI: function() {
		var me = this;
		WT.createView(WT.ID, 'view.CustomFields', {
			swapReturn: true,
			viewCfg: {
				dockableConfig: {
					title: WT.res('customFields.tit') + ' [' + me.getName() + ']'
				},
				serviceId: me.ID
			}
		}).showView();
	},
	
	addCategoryUI: function(domainId, userId) {
		var me = this;
		me.addCategory(domainId, userId, {
			callback: function(success, model) {
				if (success) me.loadRootNode(model.get('_profileId'));
			}
		});
	},
	
	editCategoryUI: function(categoryId) {
		var me = this;
		me.editCategory(categoryId, {
			callback: function(success, model) {
				if (success) me.loadRootNode(model.get('_profileId'), true);
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
					me.loadRootNode(pid);
				});
			}
		});
		vct.show();
	},
	
	hideCategoryUI: function(node) {
		var me = this;
		WT.confirm(this.res('category.confirm.hide', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if (bid === 'yes') {
				me.updateCategoryVisibility(node.get('_catId'), true, {
					callback: function(success) {
						if (success) {
							me.loadRootNode(node.get('_pid'));
							me.setActive(false);
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
				if (success) {
					me.loadRootNode(node.get('_pid'));
					if (node.isActive()) me.reloadTasks();
				}
			}
		});
	},
	
	updateCategorySyncUI: function(node, sync) {
		var me = this;
		me.updateCategorySync(node.get('_catId'), sync, {
			callback: function(success) {
				if (success) {
					me.loadRootNode(node.get('_pid'));
				}
			}
		});
	},
	
	applyCategoryTagsUI: function(node) {
		var me = this, op;
		WT.confirmSelectTags(function(bid, value) {
			if (bid === 'yes' || bid === 'no') {
				op = (bid === 'yes') ? 'set' : ((bid === 'no') ? 'unset' : ''); 
				WT.confirm(me.res('category.confirm.tags.' + op, Ext.String.ellipsis(node.get('text'), 40)), function(bid2) {
					if (bid2 === 'yes') {
						me.updateCategoryTags(node.get('_catId'), op, value, {
							callback: function(success) {
								if (success) me.reloadTasks();
							}
						});
					}
				}, this);
			}
		}, me);
	},
	
	/*
	updateCheckedFoldersUI: function(node, checked) {
		var me = this;
		me.updateCheckedFolders(node.getId(), checked, {
			callback: function(success) {
				if(success) {
					if (node.isActive()) {
						me.reloadTasks();
					} else {
						if (checked) node.setActive(checked);
					}
				}
			}
		});
	},
	*/
	
	manageTaskItemsTagsUI: function(recs) {
		var me = this,
				ids = WTU.collectIds(recs),
				tags = me.toMutualTags(recs),
				vw = WT.createView(WT.ID, 'view.Tags', {
					swapReturn: true,
					viewCfg: {
						data: {
							selection: tags
						}
					}
				});
		vw.on('viewok', function(s, data) {
			if (Sonicle.String.difference(tags, data.selection).length > 0) {
				me.updateTaskItemsTags(ids, 'reset', data.selection, {
					callback: function(success) {
						if (success) me.reloadTasks();
					}
				});
			}	
		});
		vw.showView();
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
			ids = WTU.collectIds(sel),
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
				id = sel[0].get('taskId');
		
		me.confirmMoveTask(copy, id, {
			callback: function() {
				me.reloadTasks();
			}
		});
	},
	
	confirmMoveTask: function(copy, id, opts) {
		var me = this,
				vct = me.createCategoryChooser(copy);
		
		vct.on('viewok', function(s, categoryId) {
			me.moveTask(copy, id, categoryId, opts);
		});
		vct.showView();
	},
	
	printTasksDetail: function(taskIds) {
		var me = this, url;
		url = WTF.processBinUrl(me.ID, 'PrintTasksDetail', {ids: WTU.arrayAsParam(taskIds)});
		Sonicle.URLMgr.openFile(url, {filename: 'tasks-detail', newWindow: true});
	},
	
	printTaskSel: function(sel) {
		var me = this;
		me.printTasksDetail(WTU.collectIds(sel));
	},
	
	editShare: function(id) {
		var me = this,
				vw = WT.createView(me.ID, 'view.Sharing', {swapReturn: true});
		
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: id
				}
			});
		});
	},
	
	addCategory: function(domainId, userId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Category', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
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
				vw = WT.createView(me.ID, 'view.Category', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('edit', {
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
	
	updateCategorySync: function(categoryId, sync, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetCategorySync', {
			params: {
				id: categoryId,
				sync: sync
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json]);
			}
		});
	},
	
	updateCategoryTags: function(categoryId, op, tagIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageCategory', {
			params: {
				crud: 'updateTag',
				id: categoryId,
				op: op,
				tags: WTU.arrayAsParam(tagIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	/*
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
	*/
	
	addTask: function(ownerId, categoryId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Task', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
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
				vw = WT.createView(me.ID, 'view.Task', {swapReturn: true}),
				mode = edit ? 'edit' : 'view';
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin(mode, {
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
	
	updateTaskItemsTags: function(taskIds, op, tagIds, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageGridTasks', {
			params: {
				crud: 'updateTag',
				ids: WTU.arrayAsParam(taskIds),
				op: op,
				tags: WTU.arrayAsParam(tagIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json]);
			}
		});
	},
	
	/**
	 * @private
	 */
	updateDisabled: function(action) {
		var me = this,
				dis = me.isDisabled(action);
		
		if(action === 'printTask') {
			me.setActDisabled('printTask2', dis);
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
		var me = this,
				FT = WTA.util.FoldersTree,
				sel, er;
		
		switch(action) {
			case 'showTask':
			case 'copyTask':
				sel = me.getSelectedTasks();
				if (sel.length === 1) {
					return false;
				} else {
					return true;
				}
			case 'moveTask':
				sel = me.getSelectedTasks();
				if (sel.length === 1) {
					er = FT.toRightsObj(sel[0].get('_erights'));
					return !er.DELETE;
				} else {
					return true;
				}
			case 'deleteTask':
				sel = me.getSelectedTasks();
				if (sel.length === 0) {
					return true;
				} else if (sel.length === 1) {
					er = FT.toRightsObj(sel[0].get('_erights'));
					return !er.DELETE;
				} else {
					for(var i=0; i<sel.length; i++) {
						if (!FT.toRightsObj(sel[i].get('_erights')).DELETE) return true;
					}
					return false;
				}
			case 'printTask':
				sel = me.getSelectedTasks();
				if (sel.length > 0) {
					return false;
				} else {
					return true;
				}
				break;
		}
	},
	
	privates: {
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
		
		createCategoryChooser: function(copy) {
			var me = this;
			return WT.createView(me.ID, 'view.CategoryChooser', {
				swapReturn: true,
				viewCfg: {
					dockableConfig: {
						title: me.res(copy ? 'act-copyTask.lbl' : 'act-moveTask.lbl')
					},
					writableOnly: true
				}
			});
		},
		
		toMutualTags: function(recs) {
			var arr, ids;
			Ext.iterate(recs, function(rec) {
				ids = Sonicle.String.split(rec.get('tags'), '|');
				if (!arr) {
					arr = ids;
				} else {
					arr = Ext.Array.intersect(arr, ids);
				}
				if (arr.length === 0) return false;
			});
			return arr;
		}
	}
});
