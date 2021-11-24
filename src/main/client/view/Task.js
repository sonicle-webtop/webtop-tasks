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
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.String',
		'Sonicle.form.Text',
		'Sonicle.form.field.ComboBox',
		'Sonicle.form.field.TagDisplay',
		'Sonicle.form.field.rr.Recurrence',
		'Sonicle.toolbar.LinkItem',
		'Sonicle.plugin.FileDrop',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.ValueModel',
		'WTA.ux.field.SuggestCombo',
		'WTA.ux.field.RecipientSuggestCombo',
		'WTA.ux.grid.Attachments',
		'WTA.ux.panel.CustomFieldsEditor',
		'Sonicle.webtop.tasks.store.TaskImportance',
		'Sonicle.webtop.tasks.store.TaskReminder',
		'Sonicle.webtop.tasks.store.TaskStatus',
		'Sonicle.webtop.tasks.model.Task',
		'Sonicle.webtop.tasks.model.CategoryLkp'
	],
	uses: [
		'Sonicle.webtop.core.view.Tags'
	],
	
	dockableConfig: {
		title: '{task.tit}',
		iconCls: 'wttasks-icon-task',
		width: 700,
		height: 500
	},
	confirm: 'yn',
	autoToolbar: false,
	fieldTitle: 'subject',
	modelName: 'Sonicle.webtop.tasks.model.Task',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		WTU.applyFormulas(me.getVM(), {
			startDate: {
				bind: {bindTo: '{record.start}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val) : null;
				},
				set: function(val) {
					this.get('record').setStartDate(val);
				}
			},
			startTime: {
				bind: {bindTo: '{record.start}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val) : null;
				},
				set: function(val) {
					this.get('record').setStartTime(val);
				}
			},
			dueDate: {
				bind: {bindTo: '{record.due}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val) : null;
				},
				set: function(val) {
					this.get('record').setDueDate(val);
				}
			},
			dueTime: {
				bind: {bindTo: '{record.due}'},
				get: function(val) {
					return val ? Ext.Date.clone(val) : null;
				},
				set: function(val) {
					this.get('record').setDueTime(val);
				}
			},
			reminderDate: {
				bind: {bindTo: '{record.reminderDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val) : null;
				},
				set: function(val) {
					this.get('record').setDatePart('reminderDate', val);
				}
			},
			reminderTime: {
				bind: {bindTo: '{record.reminderDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val) : null;
				},
				set: function(val) {
					this.get('record').setTimePart('reminderDate', val);
				}
			},
			importance: WTF.foTwoWay('record', 'importance', function(v) {
					return Sonicle.webtop.tasks.store.TaskImportance.homogenizedValue(v);
				}, function(v) {
					return v;
			}),
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			hasReminder: {
				bind: {bindTo: '{record.reminderDate}'},
				get: function(val) {
					return !Ext.isEmpty(val);
				}
			},
			foTags: WTF.foTwoWay('record', 'tags', function(v) {
					return Sonicle.String.split(v, '|');
				}, function(v) {
					return Sonicle.String.join('|', v);
			}),
			foHasTags: WTF.foIsEmpty('record', 'tags', true),
			foIsSeriesMaster: WTF.foGetFn('record', 'id', function(val) {
				var rec = this.get('record');
				return !rec.phantom && rec.isSeriesMaster();
			}),
			foIsSeriesInstance: WTF.foGetFn('record', 'id', function(val) {
				var rec = this.get('record');
				return !rec.phantom && rec.isSeriesInstance();
			}),
			foRRDisabled: WTF.foGetFn('record', 'id', function(val) {
				var rec = this.get('record');
				return (!rec.phantom && rec.isSeriesInstance()) || rec.get('_childTotalCount') > 0;
			}),
			foHasHierarchy: WTF.foGetFn('record', 'parentId', function(val) {
				var rec = this.get('record');
				return rec.get('_childTotalCount') > 0 || !Ext.isEmpty(rec.get('parentId'));
			}),
			foIsParent: WTF.foGetFn('record', '_childTotalCount', function(val) {
				return val > 0;
			}),
			foIsChild: WTF.foIsEmpty('record', 'parentId', true),
			foLocationIsMeeting: WTF.foGetFn('record', 'location', function(val) {
				return WT.isMeetingUrl(val);
			}),
			foHasMeeting: WTF.foGetFn('record', 'extractedUrl', function(val) {
				return WT.isMeetingUrl(val);
			}),
			foContactIconCls: WTF.foGetFn('record', 'contactId', function(val) {
				return Ext.isEmpty(val) ? '' : 'fa fa-link';
			})
		});
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel();
		
		Ext.apply(me, {
			dockedItems: [
				{
					xtype: 'toolbar',
					dock: 'top',
					items: [
						me.addAct('saveClose', {
							text: WT.res('act-saveClose.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-saveClose-xs',
							handler: function() {
								me.saveUI();
							}
						}),
						'-',
						me.addAct('delete', {
							text: null,
							tooltip: WT.res('act-delete.lbl'),
							iconCls: 'wt-icon-delete',
							handler: function() {
								me.deleteTask();
							}
						}),
						'-',
						me.addAct('openSeries', {
							text: null,
							tooltip: me.res('act-openSeries.lbl'),
							iconCls: 'wttasks-icon-taskType-seriesMaster',
							hidden: true,
							handler: function() {
								me.openSeriesTask();
							}
						}),
						me.addAct('print', {
							text: null,
							tooltip: WT.res('act-print.lbl'),
							iconCls: 'wt-icon-print',
							handler: function() {
								//TODO: add 'Save' action to allow printing without closing and reopening view
								me.printTask(me.getModel().getId());
							}
						}),
						me.addAct('tags', {
							text: null,
							tooltip: me.mys.res('act-manageTags.lbl'),
							iconCls: 'wt-icon-tag',
							handler: function() {
								me.manageTagsUI(Sonicle.String.split(me.getModel().get('tags'), '|'));
							}
						}),
						'->',
						WTF.lookupCombo('categoryId', '_label', {
							xtype: 'socombo',
							reference: 'fldcategory',
							bind: {
								value: '{record.categoryId}',
								disabled: '{foIsChild}'
							},
							listConfig: {
								displayField: 'name',
								groupCls: 'wt-theme-text-lighter2'
							},
							autoLoadOnValue: true,
							store: {
								model: me.mys.preNs('model.CategoryLkp'),
								proxy: WTF.proxy(me.mys.ID, 'LookupCategoryFolders', 'folders'),
								grouper: {
									property: '_profileId',
									sortProperty: '_order'
								},
								filters: [{
									filterFn: function(rec) {
										var mo = me.getModel();
										if (mo && me.isMode(me.MODE_NEW)) {
											return rec.get('_writable');
										} else if (mo && me.isMode(me.MODE_VIEW)) {
											if (rec.getId() === mo.get('categoryId')) return true;
										} else if (mo && me.isMode(me.MODE_EDIT)) {
											if (rec.getId() === mo.get('categoryId')) return true;
											if (rec.get('_profileId') === mo.get('_ownerId') && rec.get('_writable')) return true;
										}
										return false;
									}
								}],
								listeners: {
									load: function(s, recs, succ) {
										if (succ && (s.loadCount === 1) && me.isMode(me.MODE_NEW)) {
											var rec = s.getById(me.lref('fldcategory').getValue());
											if (rec) me.setCategoryDefaults(rec);
										}
									}
								}
							},
							groupField: '_profileDescription',
							colorField: 'color',
							fieldLabel: me.mys.res('task.fld-category.lbl'),
							labelAlign: 'right',
							width: 400,
							listeners: {
								select: function(s, rec) {
									me.setCategoryDefaults(rec);
								}
							}
						})
					]
				}, {
					xtype: 'statusbar',
					dock: 'bottom',
					defaults: {
						cls: 'wt-theme-text-lighter2'
					},
					items: [
						{
							xtype: 'so-tblink',
							tooltip: me.mys.res('task.act-showChildren.tip'),
							bind: {
								hidden: '{!foIsParent}',
								link: '{record._childComplCount}/{record._childTotalCount}'
							},
							hidden: true,
							useLinkSyntax: false,
							disableNavigation: true,
							preHtml: '<span style="position:relative;bottom:-2px;">' + Sonicle.grid.column.Nest.hierarchySvg(null, null, 'red') + '</span>&nbsp;',
							handler: function(p1, p2, e) {
								var cmx = WT.showContextMenu(e, me.getRef('cxmChildren'));
								cmx.getStore().load();
							}
						}, {
							xtype: 'so-tblink',
							tooltip: me.mys.res('task.act-showParent.tip'),
							bind: {
								hidden: '{!foIsChild}',
								link: '{record._parentSubject}'
							},
							hidden: true,
							useLinkSyntax: false,
							disableNavigation: true,
							preHtml: '<span style="position:relative;bottom:-2px;">' + Sonicle.grid.column.Nest.hierarchySvg(null, 'red') + '</span>&nbsp;',
							handler: function() {
								me.mys.openTaskUI(true, me.getModel().get('parentId'));
							}
						}
					]
				}, {
					xtype: 'sotagdisplayfield',
					dock : 'top',
					bind: {
						value: '{foTags}',
						hidden: '{!foHasTags}'
					},
					valueField: 'id',
					displayField: 'name',
					colorField: 'color',
					store: WT.getTagsStore(),
					dummyIcon: 'loading',
					hidden: true,
					hideLabel: true,
					margin: '0 0 5 0'
				}
			]
		});
		me.plugins = me.plugins || [];
		me.plugins.push({
			ptype: 'sofiledrop',
			text: WT.res('sofiledrop.text')
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				me.createTopCfg(),
				{
					xtype: 'wttabpanel',
					reference: 'tpnlmain',
					activeTab: 0,
					deferredRender: false,
					items: [
						me.createMainCfg(),
						//me.createAssigneesCfg(),
						me.createRecurCfg(),
						me.createAttachCfg(),
						me.createCustomFieldsCfg()
					],
					flex: 1
				}
			]
		});
		
		me.on('viewload', me.onViewLoad);
		me.on('viewclose', me.onViewClose);
		me.on('beforemodelsave', me.onBeforeModelSave, me);
		vm.bind('{foIsSeriesMaster}', function(nv, ov) {
			if (ov === undefined && nv === true) {
				me.setViewTitle(me.res('task.series.tit'));
				me.setViewIconCls('wttasks-icon-taskType-seriesMaster');
			}
		});
		vm.bind('{foIsSeriesInstance}', function(nv, ov) {
			if (ov === undefined) {
				me.setActDisabled('openSeries', !!!nv);
				me.setActHiddenIfDisabled('openSeries');
			}
		});
		vm.bind('{foIsChild}', function(nv, ov) {
			if (ov === undefined && nv === true) {
				me.setViewTitle(me.res('task.child.tit'));
			}
		});
		vm.bind('{foIsParent}', function(nv, ov) {
			if (ov === undefined && nv === true) {
				me.setViewTitle(me.res('task.parent.tit'));
				//me.setViewIconCls('wttasks-icon-taskType-parent');
			}
		});
		vm.bind('{foTags}', me.onTagsChanged, me);
		
		me.addRef('cxmChildren', Ext.create({
			xtype: 'sostoremenu',
			plain: true,
			store: {
				model: 'WTA.ux.data.SimpleModel',
				proxy: WTF.proxy(me.mys.ID, 'GetTaskChildren'),
				listeners: {
					beforeload: function(s) {
						WTU.applyExtraParams(s, {
							parentId: me.getModel().get('id')
						});
					}
				}
			},
			textField: 'desc',
			loadingIndicator: true,
			useItemIdPrefix: true,
			listeners: {
				click: function(s,itm) {
					if (itm) me.mys.openTaskUI(true, Sonicle.String.removeStart(itm.getItemId(), s.itemIdPrefix));
				}
			}
		}));
	},
	
	saveUI: function() {
		var me = this,
				mo = me.getModel(),
				doFn = function() {
					me.saveView(true);
				};
		
		if (!mo.phantom && mo.isParent() && (mo.getChanges() || {}).hasOwnProperty('status') && 'CO' === mo.get('status')) {
			WT.confirm(me.res('task.confirm.complete', Ext.String.ellipsis(mo.get('subject'), 40)) + '\n' + me.res('task.confirm.complete.warn.parent'), function(bid) {
				if (bid === 'yes') doFn();
			}, me);
		} else {
			doFn();
		}
	},
	
	manageTagsUI: function(selTagIds) {
		var me = this,
				vw = WT.createView(WT.ID, 'view.Tags', {
					swapReturn: true,
					viewCfg: {
						data: {
							selection: selTagIds
						}
					}
				});
		vw.on('viewok', function(s, data) {
			me.getModel().set('tags', Sonicle.String.join('|', data.selection));
		});
		vw.showView();
	},
	
	openAttachmentUI: function(rec, download) {
		var me = this,
				name = rec.get('name'),
				uploadId = rec.get('_uplId'),
				url;
		
		if (!Ext.isEmpty(uploadId)) {
			url = WTF.processBinUrl(me.mys.ID, 'DownloadTaskAttachment', {
				inline: !download,
				uploadId: uploadId
			});
		} else {
			url = WTF.processBinUrl(me.mys.ID, 'DownloadTaskAttachment', {
				inline: !download,
				iid: me.getModel().getId(),
				attachmentId: rec.get('id')
			});
		}
		if (download) {
			Sonicle.URLMgr.downloadFile(url, {filename: name});
		} else {
			Sonicle.URLMgr.openFile(url, {filename: name});
		}
	},
	
	deleteTask: function() {
		var me = this,
				rec = me.getModel(),
				s = Ext.String.ellipsis(rec.get('subject'), 40);
		
		WT.confirm(rec.isSeriesMaster() ? me.res('task.confirm.delete.series', s) : (me.res('task.confirm.delete', s) + (rec.isParent() ? ('\n' + me.res('task.confirm.delete.warn.parent')) : '')), function(bid) {
			if (bid === 'yes') {
				me.wait();
				me.mys.deleteTasks(rec.getId(), {
					callback: function(success, data, json) {
						me.unwait();
						if (success) {
							me.fireEvent('viewsave', me, true, rec);
							me.closeView(false);
						}
						WT.handleError(success, json);
					}
				});
			}
		}, me);
	},
	
	openSeriesTask: function() {
		var me = this;
		WT.confirm(me.res('task.confirm.openseries'), function(bid) {
			if (bid === 'yes') {
				var edit = me.isMode('edit'),
					id = me.getModel().getId();
				me.closeView(false);
				me.mys.openTaskUI(edit, id, true);
			}
		}, me);
	},
	
	printTask: function(id) {
		var me = this;
		if(me.getModel().isDirty()) {
			WT.warn(WT.res('warn.print.notsaved'));
		} else {
			me.mys.printTasks('detail', [id]);
		}
	},
	
	privates: {
		createTopCfg: function() {
			var me = this;
			return {
				xtype: 'wtfieldspanel',
				modelValidation: true,
				defaults: {
					labelWidth: 100
				},
				items: [
					{
						xtype: 'wtsuggestcombo',
						reference: 'fldsubject',
						bind: '{record.subject}',
						sid: me.mys.ID,
						suggestionContext: 'tasksubject',
						listeners: {
							enterkey: function() {
								me.getAct('saveClose').execute();
							}
						},
						fieldLabel: me.mys.res('task.fld-subject.lbl'),
						anchor: '100%'
					}, {
						xtype: 'textfield',
						bind: {
							value: '{record.location}',
							hidden: '{foLocationIsMeeting}'
						},
						fieldLabel: me.mys.res('task.fld-location.lbl'),
						anchor: '100%'
					}, {
						xtype: 'wtmeetingfield',
						bind: {
							value: '{record.location}',
							hidden: '{!foLocationIsMeeting}'
						},
						listeners: {
							copy: function() {
								WT.toast(WT.res('meeting.toast.link.copied'));
							}
						},
						fieldLabel: me.mys.res('task.fld-location.lbl'),
						anchor: '100%',
						hidden: true
					}, {
						xtype: 'fieldcontainer',
						fieldLabel: me.res('task.fld-start.lbl'),
						layout: 'hbox',
						items: [
							{
								xtype: 'datefield',
								bind: {
									value: '{startDate}',
									disabled: '{foIsSeriesInstance}'
								},
								startDay: WT.getStartDay(),
								format: WT.getShortDateFmt(),
								triggers: {
									clear: WTF.clearTrigger()
								},
								listeners: {
									blur: function(s) {
										var mo = me.getModel(),
												val = s.getValue();
										if (Ext.isEmpty(val) && !Ext.isEmpty(mo.get('rrule'))) {
											s.setValue(mo.get('start')); // Re-apply model's value to field!
											WT.warn(me.res('task.warn.startinuse'));
										}
									}
								},
								emptyText: me.res('task.fld-start.emp'),
								margin: '0 5 0 0',
								width: 120
							}, {
								xtype: 'timefield',
								bind: {
									value: '{startTime}',
									disabled: '{foIsSeriesInstance}'
								},
								format: WT.getShortTimeFmt(),
								listeners: {
									blur: function(s) {
										var mo = me.getModel(),
												val = s.getValue();
										if (Ext.isEmpty(val) && !Ext.isEmpty(mo.get('rrule'))) {
											s.setValue(mo.get('start')); // Re-apply model's value to field!
											WT.warn(me.res('task.warn.startinuse'));
										}
									}
								},
								width: 90
							}, {
								xtype: 'fieldcontainer',
								layout: {type: 'hbox', pack: 'end'},
								items: [
									WTF.lookupCombo('id', 'desc', {
										xtype: 'socombo',
										bind: '{record.status}',
										store: Ext.create(me.mys.preNs('store.TaskStatus'), {
											autoLoad: true
										}),
										iconField: 'icon',
										fieldLabel: me.mys.res('task.fld-status.lbl'),
										labelAlign: 'right',
										listeners: {
											select: function(s, rec) {
												if (rec.get('id') === 'NA') me.getModel().set('progress', 0);
												if (rec.get('id') === 'CO') me.getModel().set('progress', 100);
											}
										},
										width: 230+105
									})
								],
								flex: 1
							}

						]
					}, {
						xtype: 'fieldcontainer',
						fieldLabel: me.res('task.fld-due.lbl'),
						layout: 'hbox',
						items: [
							{
								xtype: 'datefield',
								bind: '{dueDate}',
								startDay: WT.getStartDay(),
								format: WT.getShortDateFmt(),
								triggers: {
									clear: WTF.clearTrigger()
								},
								emptyText: me.res('task.fld-due.emp'),
								margin: '0 5 0 0',
								width: 120
							}, {
								xtype: 'timefield',
								bind: '{dueTime}',
								format: WT.getShortTimeFmt(),
								width: 90
							}, {
								xtype: 'fieldcontainer',
								layout: {type: 'hbox', pack: 'end'},
								items: [
									{
										xtype: 'numberfield',
										step: 25,
										minValue: 0,
										maxValue: 100,
										allowDecimal: false,
										bind: '{record.progress}',
										fieldLabel: me.mys.res('task.fld-progress.lbl'),
										labelAlign: 'right',
										labelWidth: 120,
										width: 90+120,
										listeners: {
											blur: function(s) {
												var mo = me.getModel(),
														status = mo.get('status'),
														nv = s.getValue();
												if (nv === 100) {
													mo.set('status', 'CO');
												} else if (nv === 0) {
													mo.set('status', 'NA');
												} else if ('NA' === status) {
													mo.set('status', 'IP');
												}
											}
										}
									}
								],
								flex: 1
							}
						]
					}
				]
			};
		},

		createMainCfg: function() {
			var me = this;
			return {
				xtype: 'wtform', // Needed for streching textarea!
				title: me.res('task.main.tit'),
				layout: {type: 'vbox', align: 'stretch'}, // Needed for streching textarea!
				defaults: {
					labelWidth: 100
				},
				items: [
					{
						xtype: 'wtfieldspanel',
						bodyPadding: 0,
						items: [
							{
								xtype: 'wtrcptsuggestcombo',
								bind: {
									value: '{record.contact}',
									idValue: '{record.contactId}',
									iconCls: '{foContactIconCls}'
								},
								autoLast: true,
								plugins: [
									{
										ptype: 'sofieldicon',
										iconAlign: 'afterInput'
										//tooltip: me.mys.res('netLink.fld-name.hlp')
									}
								],
								fieldLabel: me.res('task.fld-contact.lbl'),
								anchor: '100%'
							}
						]
					},
					{
						xtype: 'wtfieldspanel',
						bodyPadding: 0,
						modelValidation: true,
						layout: {type: 'vbox', align: 'stretch'},
						items: [
							{
								xtype: 'textareafield',
								bind: '{record.description}',
								fieldLabel: me.res('task.fld-description.lbl'),
								flex: 1 // Needed for streching textarea!
							}
						],
						flex: 1 // Needed for streching textarea!
					}, {
						xtype: 'wtfieldspanel',
						bodyPadding: 0,
						items: [
							{
								xtype: 'fieldcontainer',
								layout: 'hbox',
								fieldLabel: me.res('task.fld-reminder.lbl'),
								items: [
									{
										xtype: 'combo',
										bind: '{record.reminder}',
										editable: false,
										store: {
											xclass: 'Sonicle.webtop.tasks.store.TaskReminder',
											autoLoad: true
										},
										valueField: 'id',
										displayField: 'desc',
										triggers: {
											clear: WTF.clearTrigger()
										},
										emptyText:  me.res('task.fld-reminder.emp'),
										width: 110
									}, {
										xtype: 'fieldcontainer',
										layout: {type: 'hbox', pack: 'end'},
										items: [
											{
												xtype: 'textfield',
												bind: '{record.docRef}',
												fieldLabel: me.mys.res('task.fld-docRef.lbl'),
												labelAlign: 'right',
												labelWidth: 80,
												maxWidth: 80+150,
												flex: 1
											},
											WTF.lookupCombo('id', 'desc', {
												bind: '{importance}',
												store: {
													xclass: me.mys.preNs('store.TaskImportance'),
													autoLoad: true
												},
												fieldLabel: me.res('task.fld-importance.lbl'),
												labelAlign: 'right',
												labelWidth: 80,
												margin: '0 10 0 0',
												width: 80+100
											}),
											{
												xtype: 'checkbox',
												bind: '{isPrivate}',
												hideEmptyLabel: true,
												boxLabel: me.res('task.fld-private.lbl')
											}
										],
										flex: 1
									}
								]
							}
						]
					}
				]
			};
		},
		
		createAssigneesCfg: function() {
			var me = this;
			return {
				xtype: 'gridpanel',
				reference: 'gpassignees',
				title: me.res('task.assignees.tit'),
				bind: {
					store: '{record.assignees}'
				},
				viewConfig: {
					deferEmptyText: false,
					emptyText: me.res('task.gp-assignees.emp')
				},
				columns: [
					{
						dataIndex: 'recipient',
						editor: {
							xtype: 'wtrcptsuggestcombo',
							matchFieldWidth: false,
							listConfig: {
								width: 350,
								minWidth: 350
							}
						},
						renderer: Ext.util.Format.htmlEncode,
						header: me.mys.res('task.gp-assignees.recipient.lbl'),
						flex: 1
					}, /*{
						dataIndex: 'respStatus',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.assigneeRespStatus',
							keepcase: true
						}),
						editor: WTF.localCombo('id', 'desc', {
							store: {
								xclass: 'Sonicle.webtop.tasks.store.AssigneeRespStatus',
								autoLoad: true
							}
						}),
						header: me.mys.res('task.gp-assignees.respStatus.lbl'),
						width: 110
					},*/ {
						xtype: 'soactioncolumn',
						items: [
							{
								glyph: 'xf014@FontAwesome',
								tooltip: WT.res('act-remove.lbl'),
								handler: function(g, ridx) {
									var rec = g.getStore().getAt(ridx);
									me.deleteAssigneeUI(rec);
								}
							}
						]
					}
				],
				plugins: [
					{
						id: 'cellediting',
						ptype: 'cellediting',
						clicksToEdit: 1
					}
				],
				tbar: [
					me.addAct('addAssignee', {
						text: WT.res('act-add.lbl'),
						tooltip: null,
						iconCls: null,
						handler: function() {
							me.addAssigneeUI();
						}
					})
				],
				border: false
			};
		},
		
		createRecurCfg: function() {
			var me = this;
			return {
				xtype: 'wtfieldspanel',
				title: me.res('task.recurrence.tit'),
				modelValidation: true,
				defaults: {
					labelWidth: 80
				},
				items: [
					{
						xtype: 'sotext',
						bind: {
							hidden: '{!foIsSeriesInstance}'
						},
						hidden: true,
						iconType: 'info',
						cls: 'wt-theme-text-lighter1',
						text: me.res('task.rrnoteditable.series.info')
					}, {
						xtype: 'sotext',
						bind: {
							hidden: '{!foIsParent}'
						},
						hidden: true,
						iconType: 'info',
						cls: 'wt-theme-text-lighter1',
						text: me.res('task.rrnoteditable.parent.info')
					}, {
						xtype: 'sorrfield',
						bind: {
							value: '{record.rrule}',
							disabled: '{foRRDisabled}',
							startDate: '{startDate}'
						},
						startDay: WT.getStartDay(),
						dateFormat: WT.getShortDateFmt(),
						endsText: WT.res('sorrfield.ends'),
						frequencyTexts: {
							'none': WT.res('sorrfield.freq.none'),
							'raw': WT.res('sorrfield.freq.raw'),
							'3': WT.res('sorrfield.freq.daily'),
							'2': WT.res('sorrfield.freq.weekly'),
							'1': WT.res('sorrfield.freq.monthly'),
							'0': WT.res('sorrfield.freq.yearly')
						},
						onEveryText: WT.res('sorrfield.onEvery'),
						onEveryWeekdayText: WT.res('sorrfield.onEveryWeekday'),
						onDayText: WT.res('sorrfield.onDay'),
						onTheText: WT.res('sorrfield.onThe'),
						thDayText: WT.res('sorrfield.thDay'),
						ofText: WT.res('sorrfield.of'),
						ofEveryText: WT.res('sorrfield.ofEvery'),
						dayText: WT.res('sorrfield.day'),
						weekText: WT.res('sorrfield.week'),
						monthText: WT.res('sorrfield.month'),
						yearText: WT.res('sorrfield.year'),
						ordinalsTexts: {
							'1': WT.res('sorrfield.nth.1st'),
							'2': WT.res('sorrfield.nth.2nd'),
							'3': WT.res('sorrfield.nth.3rd'),
							'4': WT.res('sorrfield.nth.4th'),
							'-2': WT.res('sorrfield.nth.las2nd'),
							'-1': WT.res('sorrfield.nth.last')
						},
						byDayText: WT.res('sorrfield.byDay'),
						byWeekdayText: WT.res('sorrfield.byWeekday'),
						byWeText: WT.res('sorrfield.byWe'),
						endsNeverText: WT.res('sorrfield.endsNever'),
						endsAfterText: WT.res('sorrfield.endsAfter'),
						endsByText: WT.res('sorrfield.endsBy'),
						occurrenceText: WT.res('sorrfield.occurrence'),
						rawFieldEmptyText: WT.res('sorrfield.raw.emp'),
						listeners: {
							select: function(s, freq) {
								var mo = me.getModel(), due;
								if (freq !== 'none' && !Ext.isDate(mo.get('start'))) {
									due = mo.get('due');
									if (Ext.isDate(due)) {
										mo.set('start', Sonicle.Date.clone(due));
									} else {
										mo.setStartDate(new Date());
									}
								}
							},
							rawpasteinvalid: function() {
								WT.warn(me.res('task.error.rrpaste'));
							}
						},
						fieldLabel: WT.res('sorrfield.repeats')
					}
				]
			};
		},

		createAttachCfg: function() {
			var me = this;
			return {
				xtype: 'wtattachmentsgrid',
				title: me.res('task.attachments.tit'),
				bind: {
					store: '{record.attachments}'
				},
				sid: me.mys.ID,
				uploadContext: 'TaskAttachment',
				uploadTag: WT.uiid(me.getId()),
				dropElementId: me.getId(),
				typeField: 'ext',
				listeners: {
					attachmentlinkclick: function(s, rec) {
						me.openAttachmentUI(rec, false);
					},
					attachmentdownloadclick: function(s, rec) {
						me.openAttachmentUI(rec, true);
					},
					attachmentdeleteclick: function(s, rec) {
						s.getStore().remove(rec);
					},
					attachmentuploaded: function(s, uploadId, file) {
						var sto = s.getStore();
						sto.add(sto.createModel({
							name: file.name,
							size: file.size,
							_uplId: uploadId
						}));
						me.lref('tpnlmain').getLayout().setActiveItem(s);
					}
				}
			};
		},

		createCustomFieldsCfg: function() {
			var me = this;
			return {
				xtype: 'wtcfieldseditorpanel',
				reference: 'tabcfields',
				title: me.res('task.cfields.tit'),
				bind: {
					store: '{record.cvalues}',
					fieldsDefs: '{record._cfdefs}'
				},
				defaultLabelWidth: 120
			};
		},
		
		setCategoryDefaults: function(cat) {
			var mo = this.getModel();
			if (mo) {
				mo.set({
					isPrivate: cat.get('tasPrivate')
				});
			}
		},
		
		onViewLoad: function(s, success) {
			if (!success) return;
			var me = this;
			
			if (me.isMode(me.MODE_NEW)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setDisabled(true);
				me.getAct('tags').setDisabled(false);
				me.lref('fldcategory').setReadOnly(false);
				me.reloadCustomFields([]);
			} else if (me.isMode(me.MODE_VIEW)) {
				me.getAct('saveClose').setDisabled(true);
				me.getAct('delete').setDisabled(true);
				me.getAct('tags').setDisabled(true);
				me.lref('fldcategory').setReadOnly(true);
			} else if (me.isMode(me.MODE_EDIT)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setDisabled(false);
				me.getAct('tags').setDisabled(false);
				me.lref('fldcategory').setReadOnly(false);
			}
			me.lref('fldsubject').focus(true);
		},
		
		onViewClose: function(s) {
			s.mys.cleanupUploadedFiles(WT.uiid(s.getId()));
		},
		
		onBeforeModelSave: function(s) {
			var cp = this.lref('tabcfields');
			if (!cp.isValid()) {
				this.lref('tpnlmain').getLayout().setActiveItem(cp);
				return false;
			}
		},
		
		onTagsChanged: function(nv, ov) {
			if (ov && Sonicle.String.difference(nv, ov).length > 0) { // Make sure that there are really differences!
				this.reloadCustomFields(nv);
			}
		},
		
		reloadCustomFields: function(tags) {
			var me = this,
					mo = me.getModel(),
					cftab = me.lref('tabcfields');
			me.getCustomFieldsDefsData(tags, mo.getId(), {
				callback: function(success, json) {
					if (success) {
						Ext.iterate(json.data.cvalues, function(cval) {
							var rec = mo.cvalues().getById(cval.id);
							if (!rec) {
								mo.cvalues().add(cval);
							} else {
								rec.set(cval);
							}
						});
						mo.set('_cfdefs', json.data.cfdefs);
						me.lref('tabcfields').setStore(mo.cvalues());
					}
					cftab.unwait();
				}
			});
		},
		
		getCustomFieldsDefsData: function(tags, iid, opts) {
			opts = opts || {};
			var me = this;
			WT.ajaxReq(me.mys.ID, 'GetCustomFieldsDefsData', {
				params: {
					tags: WTU.arrayAsParam(tags),
					iid: me.getModel().phantom ? null : iid
				},
				callback: function(success, json) {
					Ext.callback(opts.callback, opts.scope || me, [success, json]);
				}
			});
		},
		
		addAssigneeUI: function() {
			var me = this,
					sto = me.getModel().assignees(),
					gp = me.lref('gpassignees'),
					ed = gp.getPlugin('cellediting');

			ed.cancelEdit();
			sto.add(sto.createModel({
				respStatus: 'NA'
				//notify: true
			}));
			ed.startEditByPosition({row: sto.getCount()-1, column: 0});
		},

		deleteAssigneeUI: function(rec) {
			this.getModel().assignees().remove(rec);
		}
	}
});
