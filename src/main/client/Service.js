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
		'Sonicle.Data',
		'Sonicle.String',
		'Sonicle.Utils',
		'Sonicle.picker.Color',
		'Sonicle.button.Toggle',
		'Sonicle.grid.column.Color',
		'Sonicle.grid.column.Date',
		'Sonicle.grid.column.Icon',
		'Sonicle.grid.column.Nest',
		'Sonicle.grid.column.Tag',
		'Sonicle.grid.plugin.StateResetMenu',
		'Sonicle.tree.Column',
		'WTA.util.FoldersTree',
		'WTA.ux.field.Search',
		'WTA.ux.menu.TagMenu',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.SimpleModel',
		'Sonicle.webtop.tasks.store.TaskImportance',
		'Sonicle.webtop.tasks.store.TaskStatus',
		'Sonicle.webtop.tasks.model.FolderNode',
		'Sonicle.webtop.tasks.model.GridTask',
		'Sonicle.webtop.tasks.ux.panel.TaskPreview'
	],
	uses: [
		'WTA.ux.SelectTagsBox',
		'Sonicle.webtop.tasks.ux.RecurringConfirmBox',
		'Sonicle.webtop.tasks.view.Sharing',
		'Sonicle.webtop.tasks.view.Category',
		'Sonicle.webtop.tasks.view.Task',
		'Sonicle.webtop.tasks.view.ImportTasks',
		'Sonicle.webtop.tasks.view.CategoryChooser',
		'Sonicle.webtop.tasks.view.HiddenCategories',
		'Sonicle.webtop.tasks.ServiceApi',
		'Sonicle.webtop.tasks.portlet.Tasks'
	],
	
	needsReload: true,
	api: null,
	
	treeSelEnabled: false,
	
	skipViewOptionsCheckChange: 0,
	
	getApiInstance: function() {
		var me = this;
		if (!me.api) me.api = Ext.create('Sonicle.webtop.tasks.ServiceApi', {service: me});
		return me.api;
	},
	
	init: function() {
		var me = this,
				tagsStore = WT.getTagsStore(),
				scfields = WTA.ux.field.Search.customFieldDefs2Fields(me.getVar('cfieldsSearchable'));
		
		me.activeView = 'list';
		me.activeGridView = me.getVar('gridView');
		me.initActions();
		me.initCxm();
		
		me.on('activate', me.onActivate, me);
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			items: [
				'-',
				me.getAct('refresh'),
				me.getAct('printTaskView'),
				'->',
				{
					xtype: 'wtsearchfield',
					reference: 'fldsearch',
					highlightKeywords: ['subject', 'location', 'doc'],
					fields: Ext.Array.push([
						{
							name: 'subject',
							type: 'string',
							label: me.res('fld-search.field.subject.lbl')
						}, {
							name: 'location',
							type: 'string',
							label: me.res('fld-search.field.location.lbl')
						}, {
							name: 'description',
							type: 'string',
							label: me.res('fld-search.field.description.lbl')
						}, {
							name: 'doc',
							type: 'string',
							label: me.res('fld-search.field.docRef.lbl')
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
							type: 'tag',
							label: me.res('fld-search.field.tags.lbl'),
							customConfig: {
								store: WT.getTagsStore(), // This is filterable, let's do a separate copy!
								valueField: 'id',
								displayField: 'name',
								colorField: 'color',
								sourceField: 'source',
								sourceCls: 'wt-source'
							}
						}
					], scfields),
					tabs: Ext.isEmpty(scfields) ? undefined: [
						{
							title: WT.res('wtsearchfield.main.tit'),
							fields: ['subject', 'location', 'description', 'doc', 'after', 'before', 'private', 'done', 'tag']
						}, {
							title: WT.res('wtsearchfield.customFields.tit'),
							fields: Ext.Array.pluck(scfields, 'name')
						}
					],
					tooltip: me.res('fld-search.tip'),
					searchTooltip: me.res('fld-search.tip'),
					emptyText: me.res('fld-search.emp'),
					listeners: {
						query: function(s, value, qObj) {
							if (Ext.isEmpty(value)) {
								me.activateView(me.activeView);
							} else {
								me.queryTasks(qObj);
							}
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
			items: [
				{
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
							defaultText: me.res('trfolders.default')
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
				}
			]
		}));
		
		var viewGroup = Ext.id(null, 'tasks-view-'),
				sortFieldGroup = Ext.id(null, 'tasks-sortfield-'),
				sortDirGroup = Ext.id(null, 'tasks-sortdir-');
		me.setMainComponent(Ext.create({
			xtype: 'container',
			layout: 'border',
			referenceHolder: true,
			items: [
				{
					region: 'center',
					xtype: 'container',
					reference: 'pnlcard',
					layout: 'card',
					activeItem: me.activeView,
					items: [
						me.createGridConfig(tagsStore, true, {
							reference: 'gptaskslist',
							itemId: 'list',
							stateful: true,
							stateId: me.buildStateId('gptaskslist'),
							store: {
								model: 'Sonicle.webtop.tasks.model.GridTask',
								proxy: WTF.apiProxy(me.ID, 'ManageGridTasks', null, {
									extraParams: {
										query: null
									}
								}),
								remoteSort: true,
								listeners: {
									beforeload: function(s) {
										WTU.applyExtraParams(s, {
											view: me.activeGridView
											//groupBy: me.activeGroupBy,
											//showBy: me.getVar('showBy')
										});
									},
									load: function() {
										// FIXME: Do trick to avoid breaking selection during many sequential setComplete operations
										// 1) Create a daily task with many repetitions
										// 2) Move to not completed view
										// 3) Select the task created above, right-click opening context menu and choose "Complete"
										// 4) Then again, right-click opening context menu on the next instance and choose "Complete"
										// 5) After some operations, selection breaks returning wrong items on getSelected
										me.trFolders().focus();
										// ------------------------------
										me.pnlPreview().setSelection(me.getSelectedTasks(true));
									}
								}
							},
							selModel: {
								type: 'rowmodel',
								mode : 'MULTI'
							},
							plugins: [
								{
									ptype: 'so-gridstateresetmenu',
									menuStateResetText: WT.res('act-clearColumnState.lbl'),
									menuStateResetTooltip: WT.res('act-clearColumnState.tip'),
									listeners: {
										stateresetclick: function(s, grid) {
											WT.clearState(grid);
										}
									}
								}
							],
							tbar: [
								{
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.today.tip.tit'), text: me.res('gptasks.viewOptions.today.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.today.tip.tit'), text: me.res('gptasks.viewOptions.today.tip.txt')},
									offIconCls: 'wttasks-icon-viewToday-grayed',
									onIconCls: 'wttasks-icon-viewToday',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'today',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'today'});
									}
								}, {
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.next7.tip.tit'), text: me.res('gptasks.viewOptions.next7.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.next7.tip.tit'), text: me.res('gptasks.viewOptions.next7.tip.txt')},
									offIconCls: 'wttasks-icon-viewNext7-grayed',
									onIconCls: 'wttasks-icon-viewNext7',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'next7',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'next7'});
									}
								}, '-', {
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.notStarted.tip.tit'), text: me.res('gptasks.viewOptions.notStarted.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.notStarted.tip.tit'), text: me.res('gptasks.viewOptions.notStarted.tip.txt')},
									offIconCls: 'wttasks-icon-viewNotStarted-grayed',
									onIconCls: 'wttasks-icon-viewNotStarted',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'notStarted',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'notStarted'});
									}
								}, {
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.late.tip.tit'), text: me.res('gptasks.viewOptions.late.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.late.tip.tit'), text: me.res('gptasks.viewOptions.late.tip.txt')},
									offIconCls: 'wttasks-icon-viewLate-grayed',
									onIconCls: 'wttasks-icon-viewLate',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'late',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'late'});
									}
								}, {
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.completed.tip.tit'), text: me.res('gptasks.viewOptions.completed.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.completed.tip.tit'), text: me.res('gptasks.viewOptions.completed.tip.txt')},
									offIconCls: 'wttasks-icon-viewCompleted-grayed',
									onIconCls: 'wttasks-icon-viewCompleted',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'completed',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'completed'});
									}
								}, {
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.notCompleted.tip.tit'), text: me.res('gptasks.viewOptions.notCompleted.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.notCompleted.tip.tit'), text: me.res('gptasks.viewOptions.notCompleted.tip.txt')},
									offIconCls: 'wttasks-icon-viewNotCompleted-grayed',
									onIconCls: 'wttasks-icon-viewNotCompleted',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'notCompleted',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'notCompleted'});
									}
								}, {
									xtype: 'sotogglebutton',
									offTooltip: {title: me.res('gptasks.viewOptions.all.tip.tit'), text: me.res('gptasks.viewOptions.all.tip.txt')},
									onTooltip: {title: me.res('gptasks.viewOptions.all.tip.tit'), text: me.res('gptasks.viewOptions.all.tip.txt')},
									offIconCls: 'wttasks-icon-viewAll-grayed',
									onIconCls: 'wttasks-icon-viewAll',
									toggleGroup: viewGroup,
									allowDepress: false,
									pressed: me.activeGridView === 'all',
									toggleHandler: function() {
										me.reloadTasks({view: 'list', gridView: 'all'});
									}
								},
								'->',
								{
									iconCls: 'wt-icon-viewOptions',
									tooltip: me.res('gptasks.viewOptions.tip'),
									menu: {
										defaults: {
											scope: me
										},
										items: [
											{
												xtype: 'somenuheader',
												text: me.res('gptasks.viewOptions.sortField.lbl')
											}, {
												itemId: 'sortField-subject',
												text: me.res('gptasks.viewOptions.sortField.subject.lbl'),
												checked: false,
												group: sortFieldGroup,
												checkHandler: me.onViewOptionsSortFieldChange
											}, {
												itemId: 'sortField-start',
												text: me.res('gptasks.viewOptions.sortField.start.lbl'),
												checked: false,
												group: sortFieldGroup,
												checkHandler: me.onViewOptionsSortFieldChange
											}, {
												itemId: 'sortField-due',
												text: me.res('gptasks.viewOptions.sortField.due.lbl'),
												checked: false,
												group: sortFieldGroup,
												checkHandler: me.onViewOptionsSortFieldChange
											}, '-', {
												itemId: 'sortDir-ASC',
												text: me.res('gptasks.viewOptions.sortDirection.asc.lbl'),
												checked: false,
												group: sortDirGroup,
												checkHandler: me.onViewOptionsSortDirChange
											}, {
												itemId: 'sortDir-DESC',
												text: me.res('gptasks.viewOptions.sortDirection.desc.lbl'),
												checked: false,
												group: sortDirGroup,
												checkHandler: me.onViewOptionsSortDirChange
											}
										],
										listeners: {
											scope: me,
											beforeshow: function(s) {
												var me = this,
													sto = me.gpTasksList().getStore(),
													meta = sto.getProxy().getReader().metaData,
													sortField = (meta && meta.sortInfo) ? meta.sortInfo.field : null,
													sortDir = (meta && meta.sortInfo) ? meta.sortInfo.direction : null,
													cmp;
												
												sortField = sortField || 'start';
												sortDir = sortDir || 'ASC';
												
												me.skipViewOptionsCheckChange++;
												if (!sto.isLoading()) {
													cmp = s.getComponent('sortField-' + sortField);
													if (cmp) cmp.setChecked(true);
													cmp = s.getComponent('sortDir-' + sortDir);
													if (cmp) cmp.setChecked(true);
												}
												me.skipViewOptionsCheckChange--;
											}
										}
									}
								}
							],
							listeners: {
								selectionchange: function(s) {
									me.updateDisabled('showTask');
									me.updateDisabled('showTaskSeries');
									me.updateDisabled('addSubTask');
									me.updateDisabled('printTask');
									me.updateDisabled('copyTask');
									me.updateDisabled('moveTask');
									me.updateDisabled('deleteTask');
									me.updateDisabled('tags');
									me.updateDisabled('setTaskImportance');
									me.updateDisabled('setTaskProgress');
									me.updateDisabled('setTaskCompleted');
									me.pnlPreview().setSelection(s.getSelection());
								},
								rowdblclick: function(s, rec) {
									var er = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
									me.openTaskUI(er.UPDATE, rec.getId());
								},
								rowcontextmenu: function(s, rec, itm, i, e) {
									WT.showContextMenu(e, me.getRef('cxmGrid'), {
										task: rec,
										tasks: s.getSelection()
									});
								}
							}
						}),
						
						me.createGridConfig(tagsStore, false, {
							reference: 'gptasksresults',
							itemId: 'search',
							stateful: true,
							stateId: me.buildStateId('gptasksresults'),
							store: {
								model: 'Sonicle.webtop.tasks.model.GridTask',
								proxy: WTF.apiProxy(me.ID, 'ManageGridTasks', null, {
									extraParams: {
										query: null
									}
								}),
								listeners: {
									beforeload: function(s) {
										WTU.applyExtraParams(s, {
											view: 'none'
											//groupBy: me.activeGroupBy,
											//showBy: me.getVar('showBy')
										});
									},
									load: function() {
										// FIXME: Do trick to avoid breaking selection during many sequential setComplete operations
										// 1) Create a daily task with many repetitions
										// 2) Move to not completed view
										// 3) Select the task created above, right-click opening context menu and choose "Complete"
										// 4) Then again, right-click opening context menu on the next instance and choose "Complete"
										// 5) After some operations, selection breaks returning wrong items on getSelected
										me.trFolders().focus();
										// ------------------------------
										me.pnlPreview().setSelection(me.getSelectedTasks(true));
										me.fldSearch().highlight(me.gpTasks().getEl(), '.x-grid-item-container');
									}
								}
							},
							selModel: {
								type: 'rowmodel',
								mode : 'MULTI'
							},
							plugins: [
								{
									ptype: 'so-gridstateresetmenu',
									menuStateResetText: WT.res('act-clearColumnState.lbl'),
									menuStateResetTooltip: WT.res('act-clearColumnState.tip'),
									listeners: {
										stateresetclick: function(s, grid) {
											WT.clearState(grid);
										}
									}
								}
							],
							tools: [
								{
									type: 'close',
									callback: function() {
										me.activateView('list');
									}
								}
							],
							listeners: {
								selectionchange: function(s) {
									me.updateDisabled('showTask');
									me.updateDisabled('showTaskSeries');
									me.updateDisabled('addSubTask');
									me.updateDisabled('printTask');
									me.updateDisabled('copyTask');
									me.updateDisabled('moveTask');
									me.updateDisabled('deleteTask');
									me.updateDisabled('tags');
									me.updateDisabled('setTaskImportance');
									me.updateDisabled('setTaskProgress');
									me.updateDisabled('setTaskCompleted');
									me.pnlPreview().setSelection(s.getSelection());
								},
								rowdblclick: function(s, rec) {
									var er = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
									me.openTaskUI(er.UPDATE, rec.getId());
								},
								rowcontextmenu: function(s, rec, itm, i, e) {
									WT.showContextMenu(e, me.getRef('cxmGrid'), {
										task: rec,
										tasks: s.getSelection()
									});
								}
							}
						})
					]
				}, {
					region: 'east',
					xclass: 'Sonicle.webtop.tasks.ux.panel.TaskPreview',
					reference: 'pnlpreview',
					title: WT.res('word.preview'),
					stateful: true,
					stateId: me.buildStateId('gptaskspreview'),
					split: true,
					collapsible: true,
					hidden: !WT.plTags.desktop,
					mys: me,
					tagsStore: tagsStore,
					listeners: {
						clearselection: function(s) {
							me.gpTasks().getSelectionModel().deselectAll();
						},
						edittask: function(s, id) {
							me.openTaskUI(true, id);
						},
						setcompleted: function(s, ids) {
							WT.confirm(me.res('task.confirm.complete.selection'), function(bid) {
								if (bid === 'yes') {
									me.setTaskItemsCompleted(ids, {
										callback: function(success) {
											if (success) me.reloadTasks();
										}
									});
								}
							}, me);		
						},
						sendbyemail: function(s, ids) {
							me.sendTaskItemsByEmail(ids, {
								callback: function(success, json) {
									WT.handleError(success, json);
								}
							});
						},
						writeemail: function(s, rcpts) {
							WT.handleNewMailMessage(rcpts);
						}
					},
					width: '40%',
					minWidth: 250
				}
			]
		}));
	},
	
	createGridConfig: function(tagsStore, nest, cfg) {
		var me = this,
				durRes = function(sym) { return WT.res('word.dur.'+sym); },
				durSym = [durRes('y'), durRes('d'), durRes('h'), durRes('m'), durRes('s')];
		
		return Ext.apply({
			xtype: 'grid',
			viewConfig: {
				getRowClass: function (rec, idx) {
					if (rec.isCompleted()) return 'wt-text-striked wt-theme-text-lighter2';
					if (!rec.isSeriesMaster() && rec.isOverdue()) return 'wt-theme-text-error';
					return '';
				}
			},
			columns: [
				{
					xtype: 'soiconcolumn',
					dataIndex: 'reminder',
					getIconCls: function(v, rec) {
						return v !== null ? 'fa fa-bell-o' : '';
					},
					getTip: function(v, rec) {
						return v !== null ? me.res('store.taskReminder.'+v) : null;
					},
					iconSize: 16,
					header: WTF.headerWithGlyphIcon('fa fa-bell'),
					sortable: nest ? false : true,
					width: 35
				}, {
					xtype: 'soiconcolumn',
					itemId: 'importance',
					dataIndex: 'importance',
					getIconCls: function(v, rec) {
						return Sonicle.webtop.tasks.store.TaskImportance.buildIcon(v);
					},
					getTip: function(v, rec) {
						return me.res('gptasks.importance.lbl') + ': ' + Sonicle.webtop.tasks.store.TaskImportance.buildLabel(v);
					},
					iconSize: 16,
					header: WTF.headerWithGlyphIcon('fa fa-exclamation'),
					sortable: nest ? false : true,
					width: 30
				}, {
					xtype: 'soiconcolumn',
					itemId: 'status',
					dataIndex: 'status',
					getIconCls: function(v, rec) {
						return Sonicle.webtop.tasks.store.TaskStatus.buildIcon(v);
					},
					getTip: function(v, rec) {
						return me.res('gptasks.status.lbl') + ': ' + me.res('store.taskStatus.'+v);
					},
					iconSize: 16,
					header: WTF.headerWithGlyphIcon('fa fa-tachometer'),
					sortable: nest ? false : true,
					width: 35
				}, {
					xtype: nest ? 'so-nestcolumn' : 'soiconcolumn',
					dataIndex: 'subject',
					hideText: false,
					getText: function(v) {
						return v;
					},
					getIconCls: function(v, rec) {
						var type = 'default';
						//if (rec.isParent()) type = 'parent';
						if (rec.isSeriesMaster()) type = 'seriesMaster';
						else if (rec.isSeriesBroken()) type = 'seriesBroken';
						else if (rec.isSeriesItem()) type = 'seriesItem';
						return Ext.isEmpty(type) ? '' : 'wttasks-icon-taskType-'+type;
					},
					getTip: function(v, rec) {
						var type = 'default';
						//if (rec.isParent()) type = 'parent';
						if (rec.isSeriesMaster()) type = 'seriesMaster';
						else if (rec.isSeriesBroken()) type = 'seriesBroken';
						else if (rec.isSeriesItem()) type = 'seriesItem';
						return me.res('gptasks.type.'+type+'.tip');
					},
					collapseDisabled: true,
					collapsedIconCls: 'fa-angle-down',
					expandedIconCls: 'fa-angle-down',
					hierarchySymbolExtraCls: 'wt-theme-text-lighter1',
					isParentField: 'isParent',
					isChildField: 'isChild',
					collapsedField: '_collapsed',
					depthField: '_depth',
					indentationSize: 20,
					header: me.res('gptasks.subject.lbl'),
					flex: 2
				}/*, {
					xtype: 'soiconcolumn',
					itemId: 'status',
					getIconCls: function(v, rec) {
						//var type = 'default';
						var type = '';
						//if (rec.isParent()) type = 'parent';
						if (rec.isSeriesMaster()) type = 'series';
						else if (rec.isSeriesBroken()) type = 'broken';
						else if (rec.isSeriesItem()) type = 'series';
						return Ext.isEmpty(type) ? '' : 'wttasks-icon-taskType-'+type;
						//return 'wttasks-icon-taskType-'+type;
					},
					getTip: function(v, rec) {
						var type = 'default';
						//if (rec.isParent()) type = 'parent';
						if (rec.isSeriesMaster()) type = 'seriesMaster';
						else if (rec.isSeriesBroken()) type = 'seriesBroken';
						else if (rec.isSeriesItem()) type = 'seriesItem';
						return me.res('gptasks.type.'+type+'.tip');
					},
					header: WTF.headerWithGlyphIcon('fa fa-file-o'),
					sortable: nest ? false : true,
					width: 30
				}, {
					xtype: nest ? 'so-nestcolumn' : 'soiconcolumn',
					dataIndex: 'subject',
					hideText: false,
					getText: function(v) {
						return v;
					},
					getIconCls: function(v, rec) {
						return Sonicle.webtop.tasks.store.TaskStatus.buildIcon(rec.get('status'));
					},
					getTip: function(v, rec) {
						return me.res('gptasks.status.lbl') + ': ' + me.res('store.taskStatus.'+rec.get('status'));
					},
					collapseDisabled: true,
					collapsedIconCls: 'fa-angle-down',
					expandedIconCls: 'fa-angle-down',
					hierarchySymbolExtraCls: 'wt-theme-text-lighter1',
					isParentField: 'isParent',
					isChildField: 'isChild',
					collapsedField: '_collapsed',
					depthField: '_depth',
					indentationSize: 20,
					header: me.res('gptasks.subject.lbl'),
					flex: 2
				}*/, {
					xtype: 'datecolumn',
					dataIndex: 'start',
					format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
					header: me.res('gptasks.start.lbl'),
					usingDefaultRenderer: true, // Necessary for renderer usage below
					renderer : function(v, meta, rec) {
						if (rec.isSeriesMaster()) {
							meta.tdCls = 'wt-theme-text-lighter2';
							return me.res('task.repeated.info');
						} else {
							return this.defaultRenderer.apply(this, arguments);
						}
					},
					width: 140
				}, {
					dataIndex: 'due',
					xtype: 'sodatecolumn',
					format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
					header: me.res('gptasks.due.lbl'),
					getTip: function(v, rec) {
						if (Ext.isDate(v) && !rec.isCompleted()) {
							var SoD = Sonicle.Date,
									diff = SoD.diffDays(v, new Date()),
									hrd = SoD.humanReadableDuration(Math.abs(diff * 86400), {hours: false, minutes: false, seconds: false}, durSym);
							return Ext.String.format(me.res((diff <= 0) ? 'gptasks.due.value.left.tip' : 'gptasks.due.value.late.tip'), hrd);
						}
						return '';
					},
					usingDefaultRenderer: true, // Necessary for renderer usage below
					renderer : function(v, meta, rec) {
						if (rec.isSeriesMaster()) {
							meta.tdCls = 'wt-theme-text-lighter2';
							return me.res('task.repeated.info');
						} else {
							return this.defaultRenderer.apply(this, arguments);
						}
					},
					width: 140
				}, {
					dataIndex: 'start',
					header: me.res('gptasks.duration.lbl'),
					renderer : function(v, meta, rec) {
						if (rec.isSeriesMaster()) {
							meta.tdCls = 'wt-theme-text-lighter2';
							return me.res('task.repeated.info');
						} else {
							var SoD = Sonicle.Date,
									diff = SoD.diff(v, rec.get('due'), Ext.Date.SECOND, true);
							return diff ? SoD.humanReadableDuration(Math.abs(diff), {hours: false, minutes: false, seconds: false}, durSym) : '';
						}
					},
					sortable: false,
					hidden: true,
					width: 80
				}, {
					xtype: 'sodatecolumn',
					dataIndex: 'completedOn',
					format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
					getTip: function(v, rec) {
						if (Ext.isDate(v)) {
							var due = rec.get('due'), key;
							if (Ext.isDate(due)) {
								var SoD = Sonicle.Date,
										diff = SoD.diffDays(due, v),
										hrd = SoD.humanReadableDuration(Math.abs(diff * 86400), {hours: false, minutes: false, seconds: false}, durSym);
								return Ext.String.format(me.res((diff <= 0) ? 'gptasks.completedOn.value.advance.tip' : 'gptasks.completedOn.value.delayed.tip'), hrd);
							}
						}
						return '';
					},
					header: me.res('gptasks.completedOn.lbl'),
					sortable: nest ? false : true,
					hidden: true,
					width: 140
				}, {
					dataIndex: 'status',
					header: me.res('gptasks.status.lbl'),
					renderer: WTF.resColRenderer({
						id: me.ID,
						key: 'store.taskStatus',
						keepcase: true
					}),
					sortable: false,
					hidden: true,
					width: 120
				}, {
					xtype: 'widgetcolumn',
					itemId: 'progress',
					dataIndex: 'progressPerc',
					header: me.res('gptasks.progress.lbl'),
					widget: {
						xtype: 'progressbarwidget',
						textTpl: ['{percent:number("0")}%']
					},
					sortable: nest ? false : true,
					hidden: true,
					width: 100
				}, {
					xtype: 'sotagcolumn',
					dataIndex: 'tags',
					header: me.res('gptasks.tags.lbl'),
					tagsStore: tagsStore,
					sortable: false,
					width: 90
				}, {
					dataIndex: 'docRef',
					header: me.res('gptasks.docRef.lbl'),
					sortable: false,
					hidden: true,
					width: 100
				}, {
					xtype: 'socolorcolumn',
					dataIndex: 'categoryName',
					colorField: 'categoryColor',
					displayField: 'categoryName',
					header: me.res('gptasks.category.lbl'),
					sortable: nest ? false : true,
					width: 150
				}, {
					xtype: 'soactioncolumn',
					items: [
						{
							iconCls: 'fa fa-check',
							tooltip: me.res('act-setTaskCompleted.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.completeTasksUI(rec);
							},
							isDisabled: function(s, ridx, cidx, itm, rec) {
								return rec.isCompleted();
							}
						}, {
							iconCls: 'fa fa-trash-o',
							tooltip: WT.res('act-remove.lbl'),
							handler: function(g, ridx) {
								var rec = g.getStore().getAt(ridx);
								me.deleteTasksUI([rec]);
							}
						}
					],
					draggable: true,
					hideable: true
				}
			]
		}, cfg);
	},
	
	fldSearch: function() {
		return this.getToolbar().lookupReference('fldsearch');
	},
	
	trFolders: function() {
		return this.getToolComponent().lookupReference('trfolders');
	},
	
	pnlCard: function() {
		return this.getMainComponent().lookupReference('pnlcard');
	},
	
	gpTasksList: function() {
		return this.getMainComponent().lookupReference('gptaskslist');
	},
	
	gpTasksResults: function() {
		return this.getMainComponent().lookupReference('gptasksresults');
	},
	
	gpTasks: function() {
		return this.pnlCard().getLayout().getActiveItem();
	},
	
	pnlPreview: function() {
		return this.getMainComponent().lookupReference('pnlpreview');
	},
		
	activateView: function(view) {
		var me = this, cmp;
		if (Sonicle.String.isIn(view, ['list', 'search'])) {
			if ('search' === view) {
				cmp = me.gpTasksResults();
				cmp.setTitle(WT.res('word.search') + ': ' + Sonicle.String.deflt(arguments[1], ''));
				me.pnlCard().setActiveItem(cmp);
			} else if ('list' === view) {
				me.pnlCard().setActiveItem(me.gpTasksList());
				me.activeMode = view;
			}
		}
	},
	
	initActions: function() {
		var me = this,
				hdscale = WT.getHeaderScale();
		
		me.addAct('toolbox', 'manageTags', {
			text: WT.res('act-manageTags.lbl'),
			tooltip: WT.res('act-manageTags.tip'),
			iconCls: 'wt-icon-tag',
			handler: function() {
				me.showManageTagsUI();
			}
		});
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
				if (node) me.editCategoryUI(node.getFolderId());
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
		me.addAct('importTasks', {
			tooltip: null,
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.importTasksUI(node.getFolderId());
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
				xtype: 'wttagmenu',
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
				restoreSelectedTags: function() {
					return me.toMutualTags(me.getSelectedTasks());
				},
				listeners: {
					tagclick: function(s, tagId, checked) {
						var ids = Sonicle.Data.collectValues(me.getSelectedTasks());
						me.updateTaskItemsTags(ids, !checked ? 'unset' : 'set', [tagId], {
							callback: function(success) {
								if (success) me.reloadTasks();
							}
						});
					}
				}
			}
		});
		me.addAct('setTaskCompleted', {
			tooltip: null,
			//iconCls: 'fa fa-check wt-theme-text-off',
			handler: function(s, e) {
				//var sel = e.getContextMenuData('task') || me.getSelectedTasks();
				me.completeTasksUI(me.getSelectedTasks());
			}
		});
		me.addAct('setTaskProgress', {
			tooltip: null,
			menu: {
				itemId: 'progress',
				items: [	
					{itemId: 'p_0', text: me.res('mni-progress.0.lbl'), group: 'progress', checked: false},
					{itemId: 'p_25', text: me.res('mni-progress.25.lbl'), group: 'progress', checked: false},
					{itemId: 'p_50', text: me.res('mni-progress.50.lbl'), group: 'progress', checked: false},
					{itemId: 'p_75', text: me.res('mni-progress.75.lbl'), group: 'progress', checked: false},
					{itemId: 'p_100', text: me.res('mni-progress.100.lbl'), group: 'progress', checked: false}
				],
				listeners: {
					click: function(s, itm) {
						var ids = Sonicle.Data.collectValues(me.getSelectedTasks());
						me.updateTaskItemsProgress(ids, parseInt(Sonicle.String.substrAfterLast(itm.getItemId(), '_')), {
							callback: function(success) {
								if (success) {
									var gp = me.gpTasks(),
											reload = false;
									Ext.iterate(['progress','status'], function(col) {
										if (!Sonicle.Utils.isGridColumnHidden(gp, col)) {
											reload = true;
											return false;
										}
									});
									if (reload) me.reloadTasks();
								}
							}
						});
					}
				}
			}
		});
		var TI = Sonicle.webtop.tasks.store.TaskImportance;
		me.addAct('setTaskImportance', {
			tooltip: null,
			menu: {
				itemId: 'importance',
				items: [
					{itemId: 'i_9', text: TI.buildLabel(9), group: 'importance', checked: false},
					{itemId: 'i_5', text: TI.buildLabel(5), group: 'importance', checked: false},
					{itemId: 'i_1', text: TI.buildLabel(1), group: 'importance', checked: false}
				],
				listeners: {
					click: function(s, itm) {
						var ids = Sonicle.Data.collectValues(me.getSelectedTasks());
						me.updateTaskItemsImportance(ids, parseInt(Sonicle.String.substrAfterLast(itm.getItemId(), '_')), {
							callback: function(success) {
								if (success) {
									var gp = me.gpTasks(),
											reload = false;
									Ext.iterate(['importance','type'], function(col) {
										if (!Sonicle.Utils.isGridColumnHidden(gp, col)) {
											reload = true;
											return false;
										}
									});
									if (reload) me.reloadTasks();
								}
							}
						});
					}
				}
			}
		});
		me.addAct('sendTaskByEmail', {
			tooltip: null,
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) {
					me.sendTaskItemsByEmail(Sonicle.Data.collectValues(sel, 'id'), {
						callback: function(success, json) {
							WT.handleError(success, json);
						}
					});
				}
			}
		});
		me.addAct('addSubTask', {
			ignoreSize: true,
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) {
					me.addSubTaskUI(sel[0].get('categoryId'), sel[0].get('id'), sel[0].get('subject'));
				}
			}
		});
		me.addAct('categoryColor', {
			text: me.res('mni-categoryColor.lbl'),
			tooltip: null,
			menu: {
				showSeparator: false,
				itemId: 'categoryColor',
				items: [
					{
						xtype: 'socolorpicker',
						colors: WT.getColorPalette('default'),
						tilesPerRow: 11,
						listeners: {
							select: function(s, color) {
								var node = s.menuData.node;
								me.getRef('cxmFolder').hide();
								if (node) me.updateCategoryColorUI(node, Sonicle.String.prepend(color, '#', true));
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
				items: [
					{
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
					me.openTaskUI(er.UPDATE, rec.getId());
				}
			}
		});
		me.addAct('showTaskSeries', {
			text: me.res('act-openSeries.lbl'),
			tooltip: null,
			handler: function() {
				var rec = me.getSelectedTask(), er;
				if (rec && (rec.isSeriesItem() || rec.isSeriesBroken())) {
					er = WTA.util.FoldersTree.toRightsObj(rec.get('_erights'));
					me.openTaskUI(er.UPDATE, rec.getId(), true);
				}
			}
		});
		me.addAct('addTask', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var folderId = (e && e.menuData) ? e.menuData.node.getFolderId() : null,
						node = WTA.util.FoldersTree.getFolderForAdd(me.trFolders(), folderId);
				if (node) me.addTaskUI(node.getFolderId());
			}
		});
		me.addAct('deleteTask', {
			text: WT.res('act-delete.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-delete',
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) me.deleteTasksUI(sel);
			}
		});
		me.addAct('copyTask', {
			tooltip: null,
			handler: function() {
				me.moveTasksUI(true, me.getSelectedTasks());
			}
		});
		me.addAct('moveTask', {
			tooltip: null,
			handler: function() {
				me.moveTasksUI(false, me.getSelectedTasks());
			}
		});
		me.addAct('printTask', {
			text: WT.res('act-print.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-print',
			handler: function() {
				var sel = me.getSelectedTasks();
				if (sel.length > 0) me.printTasksUI('detail', sel);
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
		me.addAct('printTaskView', {
			scale: hdscale,
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print',
			handler: function() {
				me.printTasks('list');
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
							me.getAct('categorySync'),
							{
								itemId: 'defaultCategory',
								text: me.res('mni-defaultCategory.lbl'),
								group: 'defaultCategory',
								checked: false,
								listeners: {
									click: function(s, e) {
										var node = e.menuData.node;
										if (node && s.checked) me.updateDefaultCategoryUI(node);
									}
								}
							}
						]
					}
				},
				'-',
				me.getAct('applyTags'),
				'-',
				me.getAct('addTask'),
				me.getAct('importTasks')
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
					me.getAct('deleteCategory').setDisabled(!fr.DELETE || rec.isBuiltInFolder());
					me.getAct('importTasks').setDisabled(!er.CREATE);
					me.getAct('editSharing').setDisabled(!rr.MANAGE);
					me.getAct('addTask').setDisabled(!er.CREATE);
					me.getAct('hideCategory').setDisabled(mine);
					me.getAct('restoreCategoryColor').setDisabled(mine);
					me.getAct('applyTags').setDisabled(!er.UPDATE);
					
					var picker = s.down('menu#categoryColor').down('colorpicker');
					picker.menuData = s.menuData; // Picker's handler doesn't carry the event, injects menuData inside the picket itself
					picker.select(rec.get('_color'), true);
					s.down('menu#categorySync').getComponent(rec.get('_sync')).setChecked(true);
					
					var defltCmp = s.down('menuitem#defaultCategory');
					defltCmp.setChecked(rec.isDefaultFolder());
					defltCmp.setDisabled(!er.CREATE);
				}
			}
		}));
		
		me.addRef('cxmGrid', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('showTask'),
				me.getAct('showTaskSeries'),
				me.getAct('addSubTask'),
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
				me.getAct('deleteTask'),
				'-',
				me.getAct('tags'),
				'-',
				me.getAct('setTaskImportance'),
				me.getAct('setTaskProgress'),
				me.getAct('setTaskCompleted'),
				'-',
				me.getAct('sendTaskByEmail')
			],
			listeners: {
				beforeshow: function(s) {
					var SoU = Sonicle.Utils,
							sel = me.getSelectedTasks(),
							progMni = ['p_0','p_25','p_50','p_75','p_100'],
							progChk = false,
							impoMni = ['i_9','i_5','i_1'],
							impoChk = false,
							prog, impo;
					
					if (sel.length === 1) {
						prog = sel[0].get('progress');
						if (progMni.indexOf('p_'+prog) !== -1) {
							progMni = 'p_'+prog;
							progChk = true;
						}
						impo = Sonicle.webtop.tasks.store.TaskImportance.homogenizedValue(sel[0].get('importance'));
						if (impoMni.indexOf('i_'+impo) !== -1) {
							impoMni = 'i_'+impo;
							impoChk = true;
						}
					}
					SoU.checkMenuItem(s.down('menu#progress'), progMni, progChk);
					SoU.checkMenuItem(s.down('menu#importance'), impoMni, impoChk);
				}
			}
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
		me.updateDisabled('showTaskSeries');
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
				value = isString ? query : query.value,
				obj = {
					allText: isString ? query : query.anyText,
					conditions: isString ? [] : query.conditionArray
				};
		this.reloadTasks({view: 'search', query: Ext.JSON.encode(obj), squery: value});
	},
	
	reloadTasks: function(opts) {
		opts = opts || {};
		var me = this, curView, sto, pars = {};
		
		if (Ext.isString(opts.gridView)) me.activeGridView = opts.gridView;
		if (me.isActive()) {
			curView = opts.view || me.activeView;
			if (Sonicle.String.isIn(curView, ['list', 'search'])) {
				if ('search' === curView) {
					sto = me.gpTasksResults().getStore();
				} else {
					sto = me.gpTasksList().getStore();
				}
				if (opts.query !== undefined) Ext.apply(pars, {query: opts.query});
				WTU.loadWithExtraParams(sto, pars);
				if ('search' === curView) {
					me.activateView(curView, opts.squery);
				} else {
					me.activateView(curView);
				}
			}
		} else {
			if (Ext.isString(opts.view) && 'search' !== opts.view) me.activeMode = opts.view;
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
		var gp = this.gpTasks();
		return gp ? gp.getSelection() : [];
	},
	
	showManageTagsUI: function() {
		var me = this,
				vw = WT.createView(WT.ID, 'view.Tags', {
					swapReturn: true,
					viewCfg: {
						enableSelection: false
					}
				});
		vw.on('viewclose', function(s) {
			if (s.syncCount > 0) me.reloadTasks();
		});
		vw.showView();
	},
	
	showCustomFieldsUI: function() {
		var me = this;
		WT.createView(WT.ID, 'view.CustomFields', {
			swapReturn: true,
			preventDuplicates: true,
			viewCfg: {
				serviceId: me.ID,
				serviceName: me.getName()
			}
		}).showView();
	},
	
	showCustomPanelsUI: function() {
		var me = this;
		WT.createView(WT.ID, 'view.CustomPanels', {
			swapReturn: true,
			preventDuplicates: true,
			viewCfg: {
				serviceId: me.ID,
				serviceName: me.getName()
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
				me.updateCategoryVisibility(node.getFolderId(), true, {
					callback: function(success) {
						if (success) {
							me.loadRootNode(node.getProfileId());
							me.setActive(false);
						}
					}
				});
			}
		}, this);
	},
	
	updateCategoryColorUI: function(node, color) {
		var me = this;
		me.updateCategoryColor(node.getFolderId(), color, {
			callback: function(success) {
				if (success) {
					me.loadRootNode(node.getProfileId());
					if (node.isActive()) me.reloadTasks();
				}
			}
		});
	},
	
	updateCategorySyncUI: function(node, sync) {
		var me = this;
		me.updateCategorySync(node.getFolderId(), sync, {
			callback: function(success) {
				if (success) {
					me.loadRootNode(node.getProfileId());
				}
			}
		});
	},
	
	updateDefaultCategoryUI: function(node) {
		var me = this;
		me.updateDefaultCategory(node.getFolderId(), {
			callback: function(success, data) {
				if (success) {
					var FT = WTA.util.FoldersTree,
							tree = me.trFolders(),
							nn = FT.getFolderById(tree, data);
					if (nn) FT.setFolderAsDefault(tree, nn.getId());
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
						me.updateCategoryTags(node.getFolderId(), op, value, {
							callback: function(success) {
								if (success) me.reloadTasks();
							}
						});
					}
				}, this);
			}
		}, me);
	},
	
	importTasksUI: function(categoryId) {
		var me = this;
		me.importTasks(categoryId, {
			callback: function(success) {
				if (success) me.reloadTasks();
			}
		});
	},
	
	manageTaskItemsTagsUI: function(recs) {
		var me = this,
				ids = Sonicle.Data.collectValues(recs),
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
	
	addTaskUI: function(categoryId) {
		var me = this;
		me.addTask(categoryId, {
			callback: function(success) {
				if(success) me.reloadTasks();
			}
		});
	},
	
	addSubTaskUI: function(categoryId, parentId, parentSubject) {
		var me = this;
		me.addSubTask(categoryId, parentId, parentSubject, {
			callback: function(success) {
				if(success) me.reloadTasks();
			}
		});
	},
	
	openTaskUI: function(edit, id, series) {
		var me = this,
				id2 = (series === true) ? me.toSeriesId(id) : id;
		
		me.openTask(edit, id2, {
			callback: function(success) {
				if(success && edit) me.reloadTasks();
			}
		});
	},
	
	completeTasksUI: function(recs) {
		recs = Ext.Array.from(recs);
		var me = this,
				ids = Sonicle.Data.collectValues(recs),
				doFn = function() {
					me.setTaskItemsCompleted(ids, {
						callback: function(success) {
							if (success) me.reloadTasks();
						}
					});
				},
				rec, s;
		
		if (recs.length === 1) {
			rec = recs[0];
			s = Ext.String.ellipsis(rec.get('subject'), 40);
			WT.confirm(rec.isSeriesMaster() ? me.res('task.confirm.complete.series', s) : (me.res('task.confirm.complete', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.complete.warn.parent')) : '')), function(bid) {
				if (bid === 'yes') doFn();
			}, me);
		} else {
			WT.confirm(me.res('task.confirm.complete.selection'), function(bid) {
				if (bid === 'yes') doFn();
			}, me);
		}	
	},
	
	deleteTasksUI: function(recs) {
		recs = Ext.Array.from(recs);
		var me = this,
				ids = Sonicle.Data.collectValues(recs),
				cb = function(success) {
					if (success) me.reloadTasks();
				},
				rec, s;
		
		if (recs.length === 1) {
			rec = recs[0];
			s = Ext.String.ellipsis(rec.get('subject'), 40);
			if (rec.isSeriesItem()) {
				me.confirmOnRecurring(me.res('task.confirm.delete.recurring', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.delete.warn.parent')) : ''), function(bid, value) {
					if (bid === 'ok') {
						me.deleteTasks(ids, {
							callback: cb,
							target: value
						});
					}
				});
			} else {
				WT.confirm(rec.isSeriesMaster() ? me.res('task.confirm.delete.series', s) : (me.res('task.confirm.delete', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.delete.warn.parent')) : '')), function(bid) {
					if (bid === 'yes') {
						me.deleteTasks(ids, {
							callback: cb
						});
					}
				}, me);
			}
		} else {
			WT.confirm(me.res('task.confirm.delete.selection'), function(bid) {
				if (bid === 'yes') {
					me.deleteTasks(ids, {
						callback: cb
					});
				}
			}, me);
		}
	},
	
	moveTasksUI: function(copy, recs) {
		var me = this,
				id = Ext.Array.from(recs)[0].getId();
		
		me.confirmMoveTask(copy, id, {
			callback: function(success, json) {
				if (success) me.reloadTasks();
				WT.handleError(success, json);
			}
		});
	},
	
	printTasksUI: function(type, recs) {
		var ids = Sonicle.Data.collectValues(recs);
		this.printTasks(type, ids);
	},
	
	confirmMoveTask: function(copy, id, opts) {
		var me = this,
				vct = me.createCategoryChooser(copy);
		
		vct.on('viewok', function(s, categoryId) {
			me.moveTask(copy, id, categoryId, opts);
		});
		vct.showView();
	},
	
	importTasks: function(categoryId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.ImportTasks', {
					swapReturn: true,
					viewCfg: {
						categoryId: categoryId
					}
				});
		
		vw.on('dosuccess', function() {
			Ext.callback(opts.callback, opts.scope || me, [true]);
		});
		vw.showView();
	},
	
	printTasks: function(type, ids, filename) {
		var me = this,
				SU = Sonicle.Utils,
				obj = {},
				apnl, fname, epars, url;
		
		if (!Ext.isArray(ids) || Ext.isEmpty(ids)) {
			apnl = me.pnlCard().getLayout().getActiveItem();
			epars = apnl.getStore().getProxy().getExtraParams();
			SU.applyProp(obj, true, epars, 'view');
			SU.applyProp(obj, true, epars, 'query');
		}
		
		if ('detail' === type) {
			fname = 'tasks-detail';
		} else if ('list' === type) {
			fname = 'tasks-list';
		}
		
		url = WTF.processBinUrl(me.ID, 'PrintTasks', Ext.apply(obj, {type: type, ids: SU.toJSONArray(ids)}));
		Sonicle.URLMgr.openFile(url, {filename: Sonicle.String.deflt(filename, fname), newWindow: true});
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
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	updateDefaultCategory: function(categoryId, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'SetDefaultCategory', {
			params: {
				id: categoryId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
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
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
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
				tags: Sonicle.Utils.toJSONArray(tagIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	addTask: function(categoryId, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Task', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {
					categoryId: categoryId
				}
			});
		});
	},
	
	addTask2: function(data, opts) {
		opts = opts || {};
		var me = this,
				data2 = me.parseTaskApiData(data),
				vw = WT.createView(me.ID, 'view.Task', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: data2,
				dirty: opts.dirty
			});
		});
	},
	
	addSubTask: function(categoryId, parentId, parentSubject, opts) {
		opts = opts || {};
		var me = this,
				vw = WT.createView(me.ID, 'view.Task', {swapReturn: true});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: {
					categoryId: categoryId,
					parentId: parentId,
					_parentSubject: parentSubject
				}
			});
		});
	},
	
	editTask: function(id, opts) {
		this.openTask(true, id, opts);
	},
	
	openTask: function(edit, id, opts) {
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
					id: id
				}
			});
		});
	},
	
	deleteTasks: function(iids, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageTask', {
			params: {
				crud: 'delete',
				iids: Sonicle.Utils.toJSONArray(iids),
				target: opts.target || 'this'
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	moveTask: function(copy, iids, targetCategoryId, opts) {
		opts = opts || {};
		var me = this;
		
		WT.ajaxReq(me.ID, 'ManageTask', {
			params: {
				crud: 'move',
				copy: copy,
				iids: Sonicle.Utils.toJSONArray(iids),
				targetCategoryId: targetCategoryId
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
			}
		});
	},
	
	updateTaskItemsTags: function(ids, op, tagIds, opts) {
		opts = opts || {};
		var me = this,
				SU = Sonicle.Utils;
		WT.ajaxReq(me.ID, 'ManageGridTasks', {
			params: {
				crud: 'updateTag',
				ids: SU.toJSONArray(ids),
				op: op,
				tags: SU.toJSONArray(tagIds)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	setTaskItemsCompleted: function(iids, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageGridTasks', {
			params: {
				crud: 'complete',
				ids: Sonicle.Utils.toJSONArray(iids)
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	updateTaskItemsProgress: function(iids, progress, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageGridTasks', {
			params: {
				crud: 'setProgress',
				ids: Sonicle.Utils.toJSONArray(iids),
				progress: progress
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	updateTaskItemsImportance: function(iids, importance, opts) {
		opts = opts || {};
		var me = this;
		WT.ajaxReq(me.ID, 'ManageGridTasks', {
			params: {
				crud: 'setImportance',
				ids: Sonicle.Utils.toJSONArray(iids),
				importance: importance
			},
			callback: function(success, json) {
				Ext.callback(opts.callback, opts.scope, [success, json.data, json]);
			}
		});
	},
	
	sendTaskItemsByEmail: function(iids, opts) {
		opts = opts || {};
		var me = this,
			mapi = WT.getServiceApi('com.sonicle.webtop.mail');
		if (mapi) {
			var meid = mapi.buildMessageEditorId();
			WT.ajaxReq(me.ID, 'PrepareSendTaskByEmail', {
				params: {
					uploadTag: meid,
					ids: Sonicle.Utils.toJSONArray(iids)
				},
				callback: function(success, json) {
					if (success) {
						mapi.newMessage({
							messageEditorId: meid,
							format: 'html',
							content: '<br>',
							attachments: json.data
						}, {
							dirty: true,
							contentReady: false,
							appendContent: false
						});
					}
					Ext.callback(opts.callback, opts.scope || me, [success, json.data, json]);
				}
			});
		} else {
			Ext.callback(opts.callback, opts.scope || me, [false]);
		}
	},
	
	toSeriesId: function(id) {
		return Sonicle.String.substrBefore(id, '.') + '.00000000';
	},
	
	/**
	 * @private
	 */
	updateDisabled: function(action) {
		var me = this,
				dis = me.isDisabled(action);
		
		me.setActDisabled(action, dis);
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
			case 'showTaskSeries':
				sel = me.getSelectedTasks();
				if (sel.length === 1) {
					return !sel[0].isSeriesItem() && !sel[0].isSeriesBroken();
				} else {
					return true;
				}
			case 'addSubTask':
				sel = me.getSelectedTasks();
				if (sel.length === 1) {
					return sel[0].isChild() || sel[0].isSeriesMaster();
				} else {
					return true;
				}
			case 'moveTask':
				sel = me.getSelectedTasks();
				if (sel.length === 1) {
					er = FT.toRightsObj(sel[0].get('_erights'));
					return !er.DELETE || sel[0].isChild();
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
			case 'tags':
			case 'setTaskImportance':
			case 'setTaskProgress':
			case 'setTaskCompleted':
					sel = me.getSelectedTasks();
					if (sel.length === 0) {
						return true;
					} else if (sel.length === 1) {
						er = FT.toRightsObj(sel[0].get('_erights'));
						return !er.UPDATE;
					} else {
						for (var i=0; i<sel.length; i++) {
							if (!FT.toRightsObj(sel[i].get('_erights')).UPDATE) return true;
						}
						return false;
					}
					break;
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
	
	buildPushMessageEventName: function(msg) {
		var name = this.callParent(arguments);
		if ('taskImportLog' === msg.action && msg.payload && msg.payload.oid) {
			name += '-' + msg.payload.oid;
		}
		return name;
	},
	
	privates: {
		onViewOptionsSortFieldChange: function(s, checked) {
			var me = this;
			if (!me.skipViewOptionsCheckChange && checked) {
				me.gpTasksList().getStore().sort(Sonicle.String.substrAfterLast(s.getItemId(), '-'), 'ASC'); 
			}
		},

		onViewOptionsSortDirChange: function(s, checked) {
			var me = this;
			if (!me.skipViewOptionsCheckChange && checked) {
				var gp = me.gpTasksList(),
					sto = gp.getStore(),
					meta = sto.getProxy().getReader().metaData,
					field = (meta && meta.sortInfo) ? meta.sortInfo.field : null;
				sto.sort(field || 'start', Sonicle.String.substrAfterLast(s.getItemId(), '-')); 
			}
		},
		
		parseTaskApiData: function(data) {
			data = data || {};
			var obj = {};
			
			obj.categoryId = WTA.util.FoldersTree.getFolderForAdd(this.trFolders(), data.categoryId).getFolderId();
			if (Ext.isDefined(data.subject)) obj.subject = data.subject;
			if (Ext.isDefined(data.location)) obj.location = data.location;
			if (Ext.isDefined(data.description)) obj.description = data.description;
			if (Ext.isDefined(data.start)) obj.start = data.start;
			if (Ext.isDefined(data.due)) obj.due = data.due;
			if (Ext.isDefined(data.progress)) obj.progress = data.progress;
			if (Ext.isDefined(data.status)) obj.status = data.status;
			if (Ext.isDefined(data.importance)) obj.importance = data.importance;
			if (Ext.isDefined(data.docRef)) obj.reminder = data.reminder;
			if (Ext.isDefined(data.docRef)) obj.docRef = data.docRef;
			
			return obj;
		},
		
		confirmOnRecurring: function(msg, cb, scope) {
			var me = this;
			WT.confirm(msg, cb, scope, {
				buttons: Ext.Msg.OKCANCEL,
				instClass: 'Sonicle.webtop.tasks.ux.RecurringConfirmBox',
				instConfig: {
					thisText: me.res('recurringConfirmBox.this'),
					allText: me.res('recurringConfirmBox.all')
				},
				config: {
					value: 'this'
				}
			});
		},
		
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
