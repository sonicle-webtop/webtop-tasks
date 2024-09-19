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
		'WTA.util.FoldersTree2',
		'WTA.ux.SelectTagsBox',
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
		'Sonicle.webtop.tasks.ux.RecurringConfirmBox',
		'Sonicle.webtop.tasks.view.FolderSharing',
		'Sonicle.webtop.tasks.view.Category',
		'Sonicle.webtop.tasks.view.Task',
		'Sonicle.webtop.tasks.view.ImportTasks',
		'Sonicle.webtop.tasks.view.CategoryChooser',
		'Sonicle.webtop.tasks.view.HiddenCategories',
		'Sonicle.webtop.tasks.ServiceApi',
		'Sonicle.webtop.tasks.portlet.Tasks'
	],
	
	/**
	 * @private
	 * @property {Boolean} pendingReload
	 * This flag is set to `true` when a reload is needed after activating this service.
	 */
	pendingReload: true,
	
	/**
	 * @private
	 * @property {list} lastMainView
	 * The last activated main view name. Is considered a main view the components 
	 * that provides principal interoperability with the user: de facto excluding 'search'.
	 */
	
	/**
	 * @private
	 * @property {list} pendingView
	 * The view name to be activated after activating this service: a view is 
	 * savede here when a reload operation is issued but the service is not 
	 * curently active. Like the above, only Main view should be tracked here.
	 */
	
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
			scfields = WTA.ux.field.Search.customFieldDefs2Fields(me.ID, me.getVar('cfieldsSearchable'));
		
		me.activeGridView = me.getVar('gridView');
		me.initActions();
		me.initCxm();
		
		me.on('activate', me.onActivate, me);
		
		me.setToolbar(Ext.create({
			xtype: 'toolbar',
			referenceHolder: true,
			items: [
				'->',
				{
					xtype: 'wtsearchfield',
					reference: 'fldsearch',
					suggestionServiceId: me.ID,
					suggestionContext: 'mainsearch',
					enableQuerySaving: true,
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
								listConfig: {
									sourceCls: 'wt-source'
								}
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
								me.reloadTasks({view: me.lastMainView});
							} else {
								me.queryTasks(qObj);
							}
						}
					}
				},
				'->'
			]
		}));
		
		var mineOrigin = 'O|'+WT.getVar('profileId');
		me.setToolComponent(Ext.create({
			xtype: 'panel',
			referenceHolder: true,
			layout: 'vbox',
			items: [
				{
					xtype: 'wtplainpanel',
					bodyCls: 'wttasks-tool-searchpanel-body',
					items: [
						{
							xtype: 'textfield',
							emptyText: me.res('fldfolderssearch.emp'),
							triggers: {
								clear: {
									type: 'soclear',
									weight: -1,
									hideWhenEmpty: true,
									hideWhenMouseOut: true
								}
							},
							listeners: {
								change: {
									fn: function(s, nv) {
										WTA.util.FoldersTree2.filterFolder(this.trFolders(), nv);
									},
									scope: me,
									buffer: 250
								}
							},
							width: '100%'
						}
					],
					width: '100%'
				}, {
					xtype: 'sotreepanel',
					reference: 'trfolders',
					cls: 'wttasks-tool-tree',
					bodyCls: 'wt-tool-bg',
					border: false,
					useArrows: true,
					hideRowBackground: true,
					stateful: WT.plTags.desktop ? true : false,
					stateId: me.buildStateId('trfolders'),
					statefulExpansion: true,
					defaultExpandedNodesState: Sonicle.Object.setProp({}, mineOrigin, '/root/'+mineOrigin),
					rootVisible: false,
					store: {
						autoLoad: true,
						autoSync: true,
						hierarchyBulkLoad: true,
						model: 'Sonicle.webtop.tasks.model.FolderNode',
						proxy: WTF.apiProxy(me.ID, 'ManageFoldersTree', 'children', {
							writer: {
								allowSingle: false // Make update/delete using array payload
							}
						}),
						root: {
							id: 'root',
							expanded: true
						},
						filterer: 'bottomup',
						listeners: {
							beforeload: function(s, op) {
								if (op.getAction() === 'read' && op.getId() === 'root') {
									op.setParams(Ext.apply(op.getParams() || {}, {
										bulk: true
									}));
								}
							},
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
					columns: [
						{
							xtype: 'sotreecolumn',
							dataIndex: 'text',
							renderer: WTA.util.FoldersTree2.coloredCheckboxTreeRenderer({
								defaultText: me.res('trfolders.default'),
								showElbow: true,
								getNodeText: function(node, val) {
									val = Ext.String.htmlEncode(val);
									if ((node.isOrigin() && node.isPersonalNode()) || node.isGrouper()) {
										return me.resTpl(val);
									} else {
										return val;
									}
								}
							}),
							flex: 1
						}, {
							xtype: 'soactioncolumn',
							showOnSelection: true,
							showOnOver: true,
							items: [
								{
									iconCls: 'fas fa-ellipsis-v',
									handler: function(v, ridx, cidx, itm, e, node, row) {
										if (node.isOrigin()) {
											Sonicle.Utils.showContextMenu(e, me.getRef('cxmTreeOrigin'), {node: node});
										} else if (node.isGrouper()) {
											Sonicle.Utils.showContextMenu(e, me.getRef('cxmTreeGrouper'), {node: node});
										} else {
											Sonicle.Utils.showContextMenu(e, me.getRef('cxmTreeFolder'), {node: node});
										}
									}
								}
							]
						}
					],
					listeners: {
						checkchange: function(n, ck) {
							n.refreshActive();
						},
						beforeselect: function(s, node) {
							if (me.treeSelEnabled === false) return false;
						},
						beforedeselect: function(s, node) {
							if (me.treeSelEnabled === false) return false;
						},
						itemcontextmenu: function(s, node, itm, i, e) {
							if (node.isOrigin() || node.isGrouper()) {
								Sonicle.Utils.showContextMenu(e, me.getRef('cxmTreeOrigin'), {node: node});
							} else {
								Sonicle.Utils.showContextMenu(e, me.getRef('cxmTreeFolder'), {node: node});
							}
						}
					},
					width: '100%',
					flex: 1
				}
			],
			border: false
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
					activeItem: me.lastMainView = 'list',
					items: [
						{
							xtype: 'wtpanel',
							itemId: 'list',
							cls: 'wttasks-main-list',
							layout: 'vbox',
							defaults: {
								width: '100%'
							},
							items: [
								{
									xtype: 'toolbar',
									cls: 'wttasks-main-list-toolbar',
									border: false,
									overflowHandler: 'scroller',
									items: [
										{
											xtype: 'segmentedbutton',
											reference: 'sbtntaskslist',
											defaults: {
												ui: '{segmented|toolbar}',
												toggleGroup: viewGroup,
												toggleHandler: function(s, pressed) {
													if (pressed) me.reloadTasks({view: 'list', gridView: s.getItemId()});
												}
											},
											items: [
												{
													itemId: 'today',
													text: me.res('gptasks.viewOptions.today.lbl'),
													tooltip: me.res('gptasks.viewOptions.today.tip')
												}, {
													itemId: 'next7',
													text: me.res('gptasks.viewOptions.next7.lbl'),
													tooltip: me.res('gptasks.viewOptions.next7.tip')
												}, {
													itemId: 'notStarted',
													text: me.res('gptasks.viewOptions.notStarted.lbl'),
													tooltip: me.res('gptasks.viewOptions.notStarted.tip')
												}, {
													itemId: 'late',
													text: me.res('gptasks.viewOptions.late.lbl'),
													tooltip: me.res('gptasks.viewOptions.late.tip')
												}, {
													itemId: 'completed',
													text: me.res('gptasks.viewOptions.completed.lbl'),
													tooltip: me.res('gptasks.viewOptions.completed.tip')
												}, {
													itemId: 'notCompleted',
													text: me.res('gptasks.viewOptions.notCompleted.lbl'),
													tooltip: me.res('gptasks.viewOptions.notCompleted.tip')
												}, {
													itemId: 'all',
													text: me.res('gptasks.viewOptions.all.lbl'),
													tooltip: me.res('gptasks.viewOptions.all.tip')
												}
											]
										},
										'->',
										me.getAct('refresh'),
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
									]
								},
								me.createGridCfg(tagsStore, true, {
									itemId: 'gp',
									reference: 'gptaskslist',
									border: true,
									cls: 'wttasks-main-list-grid',
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
												Sonicle.Data.applyExtraParams(s, {
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
									recoverLostSelection: true,
									viewConfig: {
										preserveScrollOnRefresh: true,
										preserveScrollOnReload: true
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
									listeners: {
										selectionchange: function(s) {
											me.updateDisabled('openTask');
											me.updateDisabled('openTaskSeries');
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
											if (me.hasAuditUI()) me.updateDisabled('taskAuditLog');
										},
										rowdblclick: function(s, rec) {
											me.openTaskUI(rec.getItemsRights().UPDATE, rec.getId());
										},
										rowcontextmenu: function(s, rec, itm, i, e) {
											Sonicle.Utils.showContextMenu(e, me.getRef('cxmGrid'), {
												task: rec,
												tasks: s.getSelection()
											});
										}
									},
									flex: 1
								})
							]
						}, {
							xtype: 'wtpanel',
							itemId: 'search',
							layout: 'vbox',
							cls: 'wttasks-main-search',
							defaults: {
								width: '100%'
							},
							items: [
								me.createGridCfg(tagsStore, false, {
									itemId: 'gp',
									reference: 'gptasksresults',
									cls: 'wttasks-main-search-grid',
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
												Sonicle.Data.applyExtraParams(s, {
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
											me.updateDisabled('openTask');
											me.updateDisabled('openTaskSeries');
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
											if (me.hasAuditUI()) me.updateDisabled('taskAuditLog');
										},
										rowdblclick: function(s, rec) {
											me.openTaskUI(rec.getItemsRights().UPDATE, rec.getId());
										},
										rowcontextmenu: function(s, rec, itm, i, e) {
											Sonicle.Utils.showContextMenu(e, me.getRef('cxmGrid'), {
												task: rec,
												tasks: s.getSelection()
											});
										}
									},
									flex: 1
								})
							]
						}
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
						showmenu: function(s, e) {
							Sonicle.Utils.showContextMenu(e, me.getRef('cxmGrid'), {
								hideActions: true
							});
						},
						print: function(s) {
							me.getAct('printTask').execute();
						},
						clearselection: function(s) {
							me.gpTasks().getSelectionModel().deselectAll();
						},
						opentask: function(s, edit, id) {
							me.openTaskUI(edit, id);
						},
						setcompleted: function(s, ids) {
							WT.confirmOk(me.res('task.confirm.complete.selection'), function(bid) {
								if (bid === 'ok') {
									me.setTaskItemsCompleted(ids, {
										callback: function(success) {
											if (success) me.reloadTasks();
										}
									});
								}
							}, me, {title: me.res('task.confirm.complete.tit'), okText: me.res('task.confirm.complete.ok')});
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
						},
						showaudit: function(s, id) {
							me.openAuditUI(id, 'TASK');
						}
					},
					width: '40%',
					minWidth: 250
				}
			]
		}));
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
	
	sbtnTasksList: function() {
		return this.getMainComponent().lookupReference('sbtntaskslist');
	},
	
	gpTasksResults: function() {
		return this.getMainComponent().lookupReference('gptasksresults');
	},
	
	gpTasks: function() {
		// Returns the active component: gptaskslist or gptasksresults
		return this.pnlCard().getLayout().getActiveItem().getComponent('gp');
	},
	
	pnlPreview: function() {
		return this.getMainComponent().lookupReference('pnlpreview');
	},
	
	getActiveView: function() {
		var me = this,
			active = me.pnlCard().getLayout().getActiveItem();
		if (active) return active.getItemId();
		return null;
	},
		
	activateView: function(view) {
		var me = this, cmp;
		if (Sonicle.String.isIn(view, ['list', 'search'])) {
			cmp = me.pnlCard().getComponent(view);
			if ('search' === view) {
				cmp.setTitle(WT.res('word.search') + ': ' + Sonicle.String.deflt(arguments[1], ''));
				me.pnlCard().setActiveItem(cmp);
			} else if ('list' === view) {
				me.pnlCard().setActiveItem(cmp);
				me.activateGridView(me.activeGridView);
				me.lastMainView = view;
			}
			delete me.pendingView;
		}
	},
	
	activateGridView: function(gridView) {
		var seg = this.sbtnTasksList(),
			active = seg.getComponent(gridView);
		if (active) active.toggle(true, true);
	},
	
	initActions: function() {
		var me = this;
		
		me.addAct('toolbox', 'printTaskView', {
			text: null,
			tooltip: WT.res('act-print.lbl'),
			iconCls: 'wt-icon-print',
			handler: function() {
				me.printTasks('list');
			}
		});
		me.addAct('toolbox', 'manageTags', {
			text: WT.res('act-manageTags.lbl'),
			tooltip: WT.res('act-manageTags.tip'),
			iconCls: 'wt-icon-tags',
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
				if (node) me.manageFolderSharingUI(node.getId());
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
				if (node) me.addCategoryUI(node.getOwnerDomainId(), node.getOwnerUserId());
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
			userCls: 'wt-dangerzone',
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
			iconCls: 'wt-icon-tags',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) me.applyCategoryTagsUI(node);
			}
		});
		me.addAct('tags', {
			text: me.res('mni-tags.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-tags',
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
			handler: function(s, e) {
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
		me.addAct('sendByEmail', {
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
					me.addTaskUI(sel[0].get('categoryId'), sel[0].get('id'), sel[0].get('subject'));
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
								me.getRef('cxmTreeFolder').hide();
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
				if (node) WTA.util.FoldersTree2.activateSingleFolder(node.getFolderRootNode(), node.getId());
			}
		});
		me.addAct('viewAllFolders', {
			tooltip: null,
			iconCls: 'wt-icon-select-all',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree2.setActiveAllFolders(node.getFolderRootNode(), true);
			}
		});
		me.addAct('viewNoneFolders', {
			tooltip: null,
			iconCls: 'wt-icon-select-none',
			handler: function(s, e) {
				var node = e.menuData.node;
				if (node) WTA.util.FoldersTree2.setActiveAllFolders(node.getFolderRootNode(), false);
			}
		});
		if (me.hasAuditUI()) {
			me.addAct('categoryAuditLog', {
				text: WT.res('act-auditLog.lbl'),
				tooltip: null,
				handler: function(s, e) {
					var node = e.menuData.node;
					if (node) me.openAuditUI(node.getFolderId(), 'CATEGORY');
				}
			});
		}
		me.addAct('openTask', {
			text: WT.res('act-open.lbl'),
			tooltip: null,
			handler: function() {
				var rec = me.getSelectedTask();
				if (rec) {
					me.openTaskUI(rec.getItemsRights().UPDATE, rec.getId());
				}
			}
		});
		me.addAct('openTaskSeries', {
			text: me.res('act-openSeries.lbl'),
			tooltip: null,
			handler: function() {
				var rec = me.getSelectedTask();
				if (rec && (rec.isSeriesItem() || rec.isSeriesBroken())) {
					me.openTaskUI(rec.getItemsRights().UPDATE, rec.getId(), true);
				}
			}
		});
		me.addAct('addTask', {
			ignoreSize: true,
			tooltip: null,
			handler: function(s, e) {
				var folderId = (e && e.menuData) ? e.menuData.node.getFolderId() : null,
					node = WTA.util.FoldersTree2.getFolderForAdd(me.trFolders(), folderId);
				if (node) me.addTaskUI(node.getFolderId());
			}
		});
		me.addAct('deleteTask', {
			text: WT.res('act-delete.lbl'),
			tooltip: null,
			iconCls: 'wt-icon-delete',
			userCls: 'wt-dangerzone',
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
			text: '',
			tooltip: WT.res('act-refresh.lbl'),
			iconCls: 'wt-icon-refresh',
			handler: function() {
				me.reloadTasks();
			}
		});
		if (me.hasAuditUI()) {
			me.addAct('taskAuditLog', {
				text: WT.res('act-auditLog.lbl'),
				tooltip: null,
				handler: function(s, e) {
					var rec = me.getSelectedTask();
					me.openAuditUI(rec.get('taskId'), 'TASK');
				},
				scope: me
			});
		}
	},
	
	initCxm: function() {
		var me = this;
		
		me.addRef('cxmTreeOrigin', Ext.create({
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
					var node = s.menuData.node,
						mine = node.isPersonalNode(),
						or = node.getOriginRights();
					me.getAct('addCategory').setDisabled(!or.MANAGE);
					me.getAct('editSharing').setDisabled(!or.MANAGE);
					me.getAct('manageHiddenCategories').setDisabled(mine);
				}
			}
		}));
		me.addRef('cxmTreeGrouper', Ext.create({
			xtype: 'menu',
			items: [
				{
					text: me.res('mni-viewFolders.lbl'),
					menu: {
						items: [
							me.getAct('viewAllFolders'),
							me.getAct('viewNoneFolders')
						]
					}
				}
			],
			listeners: {
				beforeshow: function(s) {
					var node = s.menuData.node,
						grouper = node.isGrouper();
					
					me.getAct('viewAllFolders').setDisabled(!grouper);
					me.getAct('viewNoneFolders').setDisabled(!grouper);
				}
			}
		}));
		me.addRef('cxmTreeFolder', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('editCategory'),
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
					iconCls: 'wt-icon-customize',
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
				me.getAct('applyTags'),
				'-',
				me.getAct('importTasks'),
				me.hasAuditUI() ? me.getAct('categoryAuditLog'): null,
				'-',
				me.getAct('deleteCategory'),
				'-',
				//TODO: maybe add support to external actions coming from other services
				me.getAct('addTask')
			],
			listeners: {
				beforeshow: function(s) {
					var node = s.menuData.node,
						mine = node.isPersonalNode(),
						fr = node.getFolderRights(),
						ir = node.getItemsRights();
					
					me.getAct('editCategory').setDisabled(!fr.UPDATE);
					me.getAct('deleteCategory').setDisabled(!fr.DELETE || node.isBuiltInFolder());
					me.getAct('editSharing').setDisabled(!fr.MANAGE);
					me.getAct('addTask').setDisabled(!ir.CREATE);
					me.getAct('importTasks').setDisabled(!ir.CREATE);
					me.getAct('hideCategory').setDisabled(mine);
					me.getAct('restoreCategoryColor').setDisabled(mine);
					me.getAct('applyTags').setDisabled(!ir.UPDATE);
					
					var picker = s.down('menu#categoryColor').down('colorpicker');
					picker.menuData = s.menuData; // Picker's handler doesn't carry the event, injects menuData inside the picket itself
					picker.select(node.get('_color'), true);
					s.down('menu#categorySync').getComponent(node.get('_sync')).setChecked(true);
					
					var defltCmp = s.down('menuitem#defaultCategory');
					defltCmp.setChecked(node.isDefaultFolder());
					defltCmp.setDisabled(!ir.CREATE);
				}
			}
		}));
		
		me.addRef('cxmGrid', Ext.create({
			xtype: 'menu',
			items: [
				me.getAct('openTask'),
				me.getAct('openTaskSeries'),
				me.getAct('addSubTask'),
				me.getAct('moveTask'),
				me.getAct('copyTask'),
				me.getAct('tags'),
				'-',
				me.getAct('printTask'),	
				me.hasAuditUI() ? me.getAct('taskAuditLog') : null,
				'-',
				me.getAct('sendByEmail'),
				'-',
				me.getAct('setTaskImportance'),
				me.getAct('setTaskProgress'),
				me.getAct('setTaskCompleted'),
				'-',
				//TODO: maybe add support to external actions coming from other services
				me.getAct('deleteTask')
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
					
					me.updateDisabled('openTask');
					me.updateDisabled('openTaskSeries');
					me.updateDisabled('addSubTask');
					me.updateDisabled('moveTask');
					me.updateDisabled('copyTask');
					me.updateDisabled('tags');
					me.updateDisabled('printTask');
					if (me.hasAuditUI()) me.updateDisabled('taskAuditLog');
					me.updateDisabled('sendByEmail');
					me.updateDisabled('setTaskImportance');
					me.updateDisabled('setTaskProgress');
					me.updateDisabled('setTaskCompleted');
					me.updateDisabled('deleteTask');
				}
			}
		}));
	},
	
	onActivate: function() {
		var me = this;
		
		if (me.pendingReload === true) {
			delete me.pendingReload;
			me.reloadTasks();
		}
		
		/*
		me.updateDisabled('openTask');
		me.updateDisabled('openTaskSeries');
		me.updateDisabled('printTask');
		me.updateDisabled('copyTask');
		me.updateDisabled('moveTask');
		me.updateDisabled('deleteTask');
		if (me.hasAuditUI()) me.updateDisabled('taskAuditLog');
		*/
	},
	
	loadOriginNode: function(originPid, reloadItemsIf) {
		var me = this,
			FT = WTA.util.FoldersTree2,
			tree = me.trFolders(),
			node = FT.getOrigin(tree, originPid),
			fnode;
		
		// If node was not found, passed profileId may be the owner 
		// of a Resource: get the first match and check if it was found 
		// from a resource grouper parent.
		if (!node) {
			fnode = FT.getFolderByOwnerProfile(tree, originPid);
			if (fnode && fnode.isResource() && fnode.parentNode.isGrouper()) node = fnode.parentNode;
		}
		if (node) {
			tree.getStore().load({node: node});
			if (reloadItemsIf && node.get('checked')) me.reloadTasks();
		}
	},
	
	queryTasks: function(query) {
		var isString = Ext.isString(query),
			queryText = isString ? query : query.value,
			obj = {
				allText: isString ? query : query.anyText,
				conditions: isString ? [] : query.conditionArray
			};
		this.reloadTasks({view: 'search', query: Ext.JSON.encode(obj), queryText: queryText});
	},
	
	reloadTasks: function(opts) {
		opts = opts || {};
		var me = this, view, sto, pars = {};
		
		if (Ext.isString(opts.gridView)) me.activeGridView = opts.gridView;
		if (me.isActive()) {
			view = opts.view || me.pendingView || me.getActiveView();
			if (Sonicle.String.isIn(view, ['list', 'search'])) {
				if ('search' === view) {
					sto = me.gpTasksResults().getStore();
				} else {
					sto = me.gpTasksList().getStore();
				}
				if (opts.query !== undefined) Ext.apply(pars, {query: opts.query, queryText: opts.queryText});
				Sonicle.Data.loadWithExtraParams(sto, pars, false, opts.callback, me);
				if ('search' === view) {
					me.activateView(view, opts.queryText);
				} else {
					me.activateView(view);
				}
			}
		} else {
			view = opts.view || '';
			if ('search' !== view) me.pendingView = opts.view;
			me.pendingReload = true;
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
				if (success) me.loadOriginNode(model.get('_profileId'));
			}
		});
	},
	
	editCategoryUI: function(categoryId) {
		var me = this;
		me.editCategory(categoryId, {
			callback: function(success, model) {
				if (success) me.loadOriginNode(model.get('_profileId'), true);
			}
		});
	},
	
	deleteCategoryUI: function(node) {
		WT.confirmDelete(this.res('category.confirm.delete', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if (bid === 'ok') node.drop();
		}, this);
	},
	
	manageHiddenCategoriesUI: function(node) {
		var me = this,
				vct = me.createHiddenCategories(node.getId());
		
		vct.getView().on('viewcallback', function(s, success, json) {
			if (success) {
				Ext.iterate(json.data, function(originPid) {
					me.loadOriginNode(originPid);
				});
			}
		});
		vct.show();
	},
	
	hideCategoryUI: function(node) {
		var me = this;
		WT.confirmOk(me.res('category.confirm.hide', Ext.String.ellipsis(node.get('text'), 40)), function(bid) {
			if (bid === 'ok') {
				me.updateCategoryVisibility(node.getFolderId(), true, {
					callback: function(success) {
						if (success) {
							me.loadOriginNode(node.getOwnerPid());
							me.setActive(false);
						}
					}
				});
			}
		}, this, {
			title: me.res('category.confirm.hide.tit'),
			okText: me.res('category.confirm.hide.ok')
		});
	},
	
	updateCategoryColorUI: function(node, color) {
		var me = this;
		me.updateCategoryColor(node.getFolderId(), color, {
			callback: function(success) {
				if (success) {
					me.loadOriginNode(node.getOwnerPid());
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
					me.loadOriginNode(node.getOwnerPid());
				}
			}
		});
	},
	
	updateDefaultCategoryUI: function(node) {
		var me = this;
		me.updateDefaultCategory(node.getFolderId(), {
			callback: function(success, data) {
				if (success) {
					var FT = WTA.util.FoldersTree2,
						tree = me.trFolders(),
						node = FT.getFolderById(tree, data);
					if (node) FT.setFolderAsDefault(tree, node.getId());
				}
			}
		});
	},
	
	applyCategoryTagsUI: function(node) {
		var me = this, op;
		WT.confirmSelectTags(function(bid, value) {
			if (bid === 'yes' || bid === 'no') {
				op = (bid === 'yes') ? 'set' : ((bid === 'no') ? 'unset' : ''); 
				WT.confirmOk(me.res('category.confirm.tags.' + op, Ext.String.ellipsis(node.get('text'), 40)), function(bid2) {
					if (bid2 === 'ok') {
						me.updateCategoryTags(node.getFolderId(), op, value, {
							callback: function(success) {
								if (success) me.reloadTasks();
							}
						});
					}
				}, this, {
					title: me.res('category.confirm.tags.tit'),
					okText: me.res('category.confirm.tags.' + op + '.ok')
				});
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
	
	addTaskUI: function(categoryId, parentId, parentSubject) {
		var me = this, data = {};
		// Default values are handled in setCategoryDefaults of Task view
		//if (Ext.isBoolean(isPrivate)) data['isPrivate'] = isPrivate;
		//if (Ext.isNumber(reminder)) data['reminder'] = reminder;
		if (Ext.isDefined(parentId)) data['parentId'] = parentId;
		if (Ext.isDefined(parentSubject)) data['parentSubject'] = parentSubject;
		me.addTask(categoryId, data, {
			callback: function(success) {
				if(success) me.reloadTasks();
			}
		});
	},
	
	openTaskUI: function(edit, id, series) {
		var me = this,
			id2 = (series === true) ? Sonicle.webtop.tasks.Service.taskInstanceIdToSeriesId(id) : id;
		
		me.openTask(edit, id2, {
			callback: function(success) {
				if(success && edit) me.reloadTasks();
			}
		});
	},
	
	completeTasksUI: function(recs) {
		recs = Ext.Array.from(recs);
		var me = this,
				SoD = Sonicle.Data,
				ids = SoD.collectValues(recs),
				doFn = function(reloadCallback) {
					me.setTaskItemsCompleted(ids, {
						callback: function(success) {
							if (success) me.reloadTasks({callback: reloadCallback});
						}
					});
				},
				rec, s;
		
		if (recs.length === 1) {
			var SoS = Sonicle.String,
				gp = me.gpTasks(),
				selModel = gp.getSelectionModel(),
				rec = recs[0],
				s = Ext.String.ellipsis(rec.get('subject'), 40);
			
			WT.confirmOk(rec.isSeriesMaster() ? me.res('task.confirm.complete.series', s) : (me.res('task.confirm.complete', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.complete.warn.parent')) : '')), function(bid) {
				if (bid === 'ok') {
					if (rec.isSeriesItem() && selModel.isSelected(rec)) {
						// Restore selection on next item of the same series of the item just completed!
						doFn(function(recs, op, success) {
							if (success) {
								var seriesId = SoS.substrBefore(rec.getId(), '.'), i;
								for (i=0; i<recs.length; i++) {
									if (SoS.startsWith(recs[i].getId(), seriesId)) {
										selModel.select(recs[i]);
										break;
									}
								}
							}
						});
					} else {
						doFn();
					}	
				}
			}, me, {title: me.res('task.confirm.complete.tit'), okText: me.res('task.confirm.complete.ok')});
		} else {
			WT.confirmOk(me.res('task.confirm.complete.selection'), function(bid) {
				if (bid === 'ok') doFn();
			}, me, {title: me.res('task.confirm.complete.tit'), okText: me.res('task.confirm.complete.ok')});
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
				WT.confirmDelete(me.res('task.confirm.delete.recurring', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.delete.warn.parent')) : ''), function(bid, value) {
					if (bid === 'ok') {
						me.deleteTasks(ids, {
							callback: cb,
							target: value
						});
					}
				}, me, me.configureRecurringConfirm());
			} else {
				WT.confirmDelete(rec.isSeriesMaster() ? me.res('task.confirm.delete.series', s) : (me.res('task.confirm.delete', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.delete.warn.parent')) : '')), function(bid) {
					if (bid === 'ok') {
						me.deleteTasks(ids, {
							callback: cb
						});
					}
				}, me);
			}
		} else {
			WT.confirmDelete(me.res('task.confirm.delete.selection'), function(bid) {
				if (bid === 'ok') {
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
			vw = me.createCategoryChooser(copy);
		
		vw.on('viewok', function(s, data) {
			me.moveTask(copy ? (data.deepCopy ? 'tree' : 'root') : 'none', id, data.categoryId, opts);
		});
		vw.showView();
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
			SoO = Sonicle.Object,
			obj = {},
			apnl, fname, epars, url;
		
		if (!Ext.isArray(ids) || Ext.isEmpty(ids)) {
			apnl = me.gpTasks();
			epars = Sonicle.Data.getParams(apnl.getStore());
			SoO.copyProp(obj, true, epars, 'sort');
			SoO.copyProp(obj, true, epars, 'view');
			SoO.copyProp(obj, true, epars, 'query');
		}
		
		if ('detail' === type) {
			fname = 'tasks-detail';
		} else if ('list' === type) {
			fname = 'tasks-list';
		}
		
		url = WTF.processBinUrl(me.ID, 'PrintTasks', Ext.apply(obj, {type: type, ids: Sonicle.Utils.toJSONArray(ids)}));
		Sonicle.URLMgr.openFile(url, {filename: Sonicle.String.deflt(filename, fname), newWindow: true});
	},
	
	manageFolderSharingUI: function(nodeId) {
		var me = this,
			vw = WT.createView(me.ID, 'view.FolderSharing', {swapReturn: true});
		
		vw.showView(function() {
			vw.begin('edit', {
				data: {
					id: nodeId
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
	
	addTask: function(categoryId, moreData, opts) {
		opts = opts || {};
		var me = this,
			prep = me.prepareTaskData(Ext.apply(moreData || {}, {categoryId: categoryId})),
			vw = WT.createView(me.ID, 'view.Task', {
				swapReturn: true,
				viewCfg: {
					uploadTag: opts.uploadTag
				}
			});
		
		vw.on('viewsave', function(s, success, model) {
			Ext.callback(opts.callback, opts.scope || me, [success, model]);
		});
		vw.showView(function() {
			vw.begin('new', {
				data: prep.data,
				cfData: prep.cfData,
				dirty: Ext.isBoolean(opts.dirty) ? opts.dirty : false
			});
		});
		return vw;
	},
	
	addSubTask: function(categoryId, parentId, parentSubject, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.ID, 'view.Task', {
				swapReturn: true,
				viewCfg: {
					uploadTag: opts.uploadTag
				}
			});
		
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
		return vw;
	},
	
	editTask: function(id, opts) {
		return this.openTask(true, id, opts);
	},
	
	openTask: function(edit, id, opts) {
		opts = opts || {};
		var me = this,
			vw = WT.createView(me.ID, 'view.Task', {
				swapReturn: true,
				viewCfg: {
					uploadTag: opts.uploadTag
				}
			}),
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
		return vw;
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
	
	moveTask: function(copyMode, iids, targetCategoryId, opts) {
		opts = opts || {};
		var me = this;
		
		WT.ajaxReq(me.ID, 'ManageTask', {
			params: {
				crud: 'move',
				copyMode: copyMode,
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
		var me = this, sel;
		switch(action) {
			case 'openTask':
			case 'copyTask':
			case 'taskAuditLog':
				sel = me.getSelectedTasks();
				if (sel.length === 1) {
					return false;
				} else {
					return true;
				}
			case 'openTaskSeries':
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
					return !sel[0].getItemsRights().DELETE || sel[0].isChild();
				} else {
					return true;
				}
			case 'deleteTask':
				sel = me.getSelectedTasks();
				if (sel.length === 0) {
					return true;
				} else if (sel.length === 1) {
					return !sel[0].getItemsRights().DELETE;
				} else {
					for(var i=0; i<sel.length; i++) {
						if (!sel[i].getItemsRights().DELETE) return true;
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
						return !sel[0].getItemsRights().UPDATE;
					} else {
						for (var i=0; i<sel.length; i++) {
							if (!sel[i].getItemsRights().UPDATE) return true;
						}
						return false;
					}
					break;
			case 'printTask':
			case 'sendByEmail':
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
	
	openAuditUI: function(referenceId, context) {
		var me = this,
				tagsStore = WT.getTagsStore();
		
		WT.getServiceApi(WT.ID).showAuditLog(me.ID, context, null, referenceId, function(data) {
			var str = '', logDate, actionString, eldata;
			
			Ext.each(data,function(el) {
				logDate = Ext.Date.parseDate(el.timestamp, 'Y-m-d H:i:s');
				actionString = Ext.String.format('auditLog.{0}.{1}', context, el.action);
				str += Ext.String.format('{0} - {1} - {2} ({3})\n', Ext.Date.format(logDate, WT.getShortDateTimeFmt()), me.res(actionString), el.userName, el.userId);
				eldata = Ext.JSON.decode(el.data);
				
				if (el.action === 'TAG' && eldata) {
					if (eldata.set) {
						Ext.each(eldata.set, function(tag) {
							var r = tagsStore.findRecord('id', tag);
							var desc = r ? r.get('name') : tag;
							str += Ext.String.format('\t+ {0}\n', desc);
						});
					}
					if (eldata.unset) {
						Ext.each(eldata.unset, function(tag) {
							var r = tagsStore.findRecord('id', tag);
							var desc = r ? r.get('name') : tag;
							str += Ext.String.format('\t- {0}\n', desc);
						});
					}
				}
			});
			return str;
		});
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
		
		createGridCfg: function(tagsStore, nest, cfg) {
			var me = this,
				durRes = function(sym) { return WT.res('word.dur.'+sym); },
				durSym = [durRes('y'), durRes('d'), durRes('h'), durRes('m'), durRes('s')];

			return Ext.merge({
				xtype: 'grid',
				componentCls: 'wttasks-main-grid',
				viewConfig: {
					getRowClass: function (rec, idx) {
						if (rec.isCompleted()) return 'wt-text-striked wt-text-off wt-theme-text-color-off';
						if (!rec.isSeriesMaster() && rec.isOverdue()) return 'wt-theme-color-error';
						return '';
					}
				},
				columns: [
					{
						xtype: 'socolorcolumn',
						dataIndex: 'categoryColor',
						labelField: 'categoryName',
						swatchGeometry: 'circle',
						hideLabel: true,
						getTooltip: function(v, rec) {
							return Sonicle.webtop.tasks.Service.calcCategoryLabel(rec.get('categoryName'), rec.get('_orDN'));
						},
						text: WTF.headerWithGlyphIcon('fas fa-folder'),
						menuText: me.res('gptasks.category.lbl'),
						sortable: nest ? false : true,
						hidden: true,
						width: 35
					}, {
						xtype: 'soiconcolumn',
						dataIndex: 'reminder',
						getIconCls: function(v, rec) {
							return v !== null ? 'far fa-bell' : '';
						},
						getTip: function(v, rec) {
							return v !== null ? me.res('store.taskReminder.'+v) : null;
						},
						iconSize: 16,
						text: WTF.headerWithGlyphIcon('fas fa-bell'),
						menuText: me.res('gptasks.reminder.lbl'),
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
						text: WTF.headerWithGlyphIcon('fas fa-triangle-exclamation'),
						menuText: me.res('gptasks.importance.lbl'),
						sortable: nest ? false : true,
						width: 35
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
						text: WTF.headerWithGlyphIcon('fas fa-tachometer-alt'),
						menuText: me.res('gptasks.status.i.lbl'),
						sortable: nest ? false : true,
						width: 35
					}, {
						dataIndex: 'status',
						renderer: WTF.resColRenderer({
							id: me.ID,
							key: 'store.taskStatus',
							keepcase: true
						}),
						text: me.res('gptasks.status.lbl'),
						sortable: false,
						hidden: true,
						maxWidth: 120,
						flex: 1
					}, {
						xtype: nest ? 'so-nestcolumn' : 'soiconcolumn',
						dataIndex: 'subject',
						hideText: false,
						getText: function(v) {
							return Ext.String.htmlEncode(v);
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
						collapsedIconCls: 'fas fa-angle-down',
						expandedIconCls: 'fas fa-angle-down',
						hierarchySymbolExtraCls: 'wttasks-grid-hierarchysymbol',
						isParentField: 'isParent',
						isChildField: 'isChild',
						collapsedField: '_collapsed',
						depthField: '_depth',
						text: me.res('gptasks.subject.lbl'),
						flex: 3
					}, {
						xtype: 'datecolumn',
						dataIndex: 'start',
						format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
						usingDefaultRenderer: true, // Necessary for renderer usage below
						renderer : function(v, meta, rec) {
							if (rec.isSeriesMaster()) {
								meta.tdCls = 'wt-text-off wt-theme-text-color-off';
								return me.res('task.repeated.info');
							} else {
								return this.defaultRenderer.apply(this, arguments);
							}
						},
						text: me.res('gptasks.start.lbl'),
						maxWidth: 140,
						flex: 1.5
					}, {
						dataIndex: 'due',
						xtype: 'sodatecolumn',
						format: WT.getShortDateFmt() + ' ' + WT.getShortTimeFmt(),
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
								meta.tdCls = 'wt-text-off wt-theme-text-color-off';
								return me.res('task.repeated.info');
							} else {
								return this.defaultRenderer.apply(this, arguments);
							}
						},
						text: me.res('gptasks.due.lbl'),
						maxWidth: 140,
						flex: 1.5
					}, {
						dataIndex: 'start',
						renderer : function(v, meta, rec) {
							if (rec.isSeriesMaster()) {
								meta.tdCls = 'wt-text-off wt-theme-text-color-off';
								return me.res('task.repeated.info');
							} else {
								var SoD = Sonicle.Date,
									diff = SoD.diff(v, rec.get('due'), Ext.Date.SECOND, true);
								return diff ? SoD.humanReadableDuration(Math.abs(diff), {hours: false, minutes: false, seconds: false}, durSym) : '';
							}
						},
						sortable: false,
						hidden: true,
						text: me.res('gptasks.duration.lbl'),
						maxWidth: 80,
						flex: 1
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
						text: me.res('gptasks.completedOn.lbl'),
						sortable: nest ? false : true,
						hidden: true,
						maxWidth: 140,
						flex: 1.5
					}, {
						xtype: 'widgetcolumn',
						itemId: 'progress',
						dataIndex: 'progressPerc',
						widget: {
							xtype: 'progressbarwidget',
							textTpl: ['{percent:number("0")}%']
						},
						text: me.res('gptasks.progress.lbl'),
						sortable: nest ? false : true,
						hidden: true,
						maxWidth: 80,
						flex: 1
					}, {
						xtype: 'sotagcolumn',
						dataIndex: 'tags',
						tagsStore: tagsStore,
						text: me.res('gptasks.tags.lbl'),
						sortable: false,
						maxWidth: 90,
						flex: 1
					}, {
						dataIndex: 'docRef',
						text: me.res('gptasks.docRef.lbl'),
						sortable: false,
						hidden: true,
						maxWidth: 100,
						flex: 1
					}, {
						xtype: 'soactioncolumn',
						menuText: WT.res('grid.actions.lbl'),
						items: [
							{
								iconCls: 'fas fa-ellipsis-vertical',
								handler: function(view, ridx, cidx, itm, e, rec) {
									view.setSelection(rec);
									Sonicle.Utils.showContextMenu(e, me.getRef('cxmGrid'));
								}
							}
						],
						draggable: true,
						hideable: true
					}
				]
			}, cfg);
		},
		
		prepareTaskData: function(data) {
			data = data || {};
			var me = this,
				isDef = Ext.isDefined,
				copy = function(tgt, src, name, newName) {
					Sonicle.Object.copyProp(tgt, true, src, name, newName);
				},
				o = {}, cfo;

			o.categoryId = isDef(data.categoryId) ? data.categoryId : WTA.util.FoldersTree2.getDefaultOrBuiltInFolder(me.trFolders());
			if (isDef(data.parentId)) {
				o.parentId = data.parentId;
				if (isDef(data.parentSubject)) {
					o._parentSubject = data.parentSubject;
				}
			}
			copy(o, data, 'subject');
			copy(o, data, 'location');
			copy(o, data, 'description');
			copy(o, data, 'start');
			copy(o, data, 'due');
			copy(o, data, 'progress');
			copy(o, data, 'status');
			copy(o, data, 'importance');
			copy(o, data, 'isPrivate');
			copy(o, data, 'reminder');
			copy(o, data, 'docRef');
			if (isDef(data.tags)) {
				if (Ext.isArray(data.tags)) {
					o.tags = Sonicle.String.join('|', data.tags);
				} else if (Ext.isString(data.tags)) {
					o.tags = data.tags;
				}
			}
			if (isDef(data.customFields) && Ext.isObject(data.customFields)) {
				cfo = data.customFields;
			}
			
			return {data: o, cfdata: cfo};
		},
		
		configureRecurringConfirm: function(opts) {
			return Ext.merge(opts || {}, {
				instClass: 'Sonicle.webtop.tasks.ux.RecurringConfirmBox',
				instConfig: {
					thisText: this.res('recurringConfirmBox.this'),
					allText: this.res('recurringConfirmBox.all')
				},
				config: {
					value: 'this'
				}
			});
		},
		
		createHiddenCategories: function(originNodeId) {
			var me = this;
			return WT.createView(me.ID, 'view.HiddenCategories', {
				viewCfg: {
					action: 'ManageHiddenCategories',
					extraParams: {
						node: originNodeId
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
					writableOnly: true,
					showDeepCopy: copy,
					defaultFolder: WTA.util.FoldersTree2.getDefaultFolder(me.trFolders())
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
	},
	
	statics: {
		
		/**
		 * Builds a Task instance ID from passed parameters.
		 * @param {String} taskId The task ID.
		 * @param {String} [yyyymmdd] The instance Data in format 'yyyymmdd'.
		 * @returns {String}
		 */
		createTaskInstanceId: function(taskId, yyyymmdd) {
			if (Ext.isString(yyyymmdd)) {
				return taskId + '.' + Sonicle.String.left(yyyymmdd, 8);
			} else {
				return taskId + '.00000000';
			}
		},
		
		/**
		 * Calculates the Task series ID from a passed instance ID.
		 * @param {String} iid A task instance ID.
		 * @returns {String}
		 */
		taskInstanceIdToSeriesId: function(iid) {
			return Sonicle.String.substrBefore(iid, '.') + '.00000000';
		},
		
		/**
		 * Computes a label for displaying category info.
		 * @param {String} name Category name.
		 * @param {String} [ownerDN] Category's owner display-name.
		 * @param {Object} [opts] An object containing configuration.
		 * 
		 * This object may contain any of the following properties:
		 * 
		 * @param {Boolean} opts.htmlEncode Set to `true` to apply HTML encoding to resulting output.
		 * @returns {String} The computed label
		 */
		calcCategoryLabel: function(name, ownerDN, opts) {
			opts = opts || {};
			var pattern = '{1}';
			if (!Ext.isEmpty(ownerDN)) pattern = '[{0}] ' + pattern;
			return Ext.String.format(pattern, ownerDN, name);
		}
	}
});
