/* 
 * Copyright (C) 2024 Sonicle S.r.l.
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2024 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.tasks.view.Task', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.Data',
		'Sonicle.String',
		'Sonicle.VMUtils',
		'Sonicle.form.FieldSection',
		'Sonicle.form.FieldHGroup',
		'Sonicle.form.field.ComboBox',
		'Sonicle.form.field.Display',
		'Sonicle.form.field.rr.Repeat',
		'Sonicle.form.field.TagDisplay',
		'Sonicle.plugin.DropMask',
		'Sonicle.toolbar.LinkItem',
		'WTA.util.CustomFields',
		'WTA.ux.UploadButton',
		'WTA.ux.field.Attachments',
		'WTA.ux.field.RecipientSuggestCombo',
		'WTA.ux.field.SuggestCombo',
		'WTA.ux.panel.CustomFieldsEditor',
		'Sonicle.webtop.tasks.model.CategoryLkp',
		'Sonicle.webtop.tasks.model.Task',
		'Sonicle.webtop.tasks.store.TaskImportance',
		'Sonicle.webtop.tasks.store.TaskReminder',
		'Sonicle.webtop.tasks.store.TaskStatus',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.ValueModel'
	],
	uses: [
		'Sonicle.URLMgr',
		'Sonicle.webtop.core.view.Tags',
		'Sonicle.webtop.tasks.view.RecurrenceEditor'
	],
	
	dockableConfig: {
		title: '{task.tit}',
		iconCls: 'wttasks-icon-task',
		width: 700,
		height: 540
	},
	confirm: 'yn',
	fieldTitle: 'subject',
	modelName: 'Sonicle.webtop.tasks.model.Task',
	
	viewModel: {
		data: {
			hidden: {
				fldlocation: true,
				fldcontact: true
			}
		}
	},
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsView: WTF.foIsEqual('_mode', null, me.MODE_VIEW),
			foIsNew: WTF.foIsEqual('_mode', null, me.MODE_NEW),
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
			importance: WTF.foFieldTwoWay('record', 'importance', function(v) {
					return Sonicle.webtop.tasks.store.TaskImportance.homogenizedValue(v);
				}, function(v) {
					return v;
			}),
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			foHasLocation: WTF.foIsEmpty('record', 'location', true),
			foHasReminder: WTF.foIsEmpty('record', 'reminder', true),
			//foHasDocRef: WTF.foIsEmpty('record', 'docRef', true),
			foHasRecurrence: WTF.foIsEmpty('record', 'rrule', true),
			foRRuleString: WTF.foFieldTwoWay('record', 'rruleString', function(v) {
					return v;
				}, function(v, rec) {
					var split = Sonicle.form.field.rr.Recurrence.splitRRuleString(v);
					rec.set('rrule', split.rrule, {convert: false});
					rec.setStartDate(split.start, {convert: false});
					return v;
			}),
			foHumanReadableRRule: WTF.foGetFn('record', 'rruleString', function(v) {
				return WT.toHumanReadableRRule(v);
			}),
			foWasRecurring: WTF.foIsEqual('record', '_recurringInfo', 'recurring'),
			foTags: WTF.foFieldTwoWay('record', 'tags', function(v) {
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
				return Ext.isEmpty(val) ? '' : 'wt-glyph-link wt-opacity-50';
			}),
			foHideLocationAsAddress: WTF.foMultiGetFn(undefined, ['hidden.fldlocation', 'foHasLocation', 'foLocationIsMeeting'], function(v) {
				if (v['hidden.fldlocation']) return true;
				return !!v['foLocationIsMeeting'];
			}),
			foHideLocationAsMeeting: WTF.foMultiGetFn(undefined, ['hidden.fldlocation', 'foHasLocation', 'foLocationIsMeeting'], function(v) {
				if (v['hidden.fldlocation']) return true;
				return !!!v['foLocationIsMeeting'];
			}),
			foHasAssignees: WTF.foAssociationIsEmpty('record', 'assignees', true),
			foHasAttachments: WTF.foAssociationIsEmpty('record', 'attachments', true)
		});
	},
	
	initComponent: function() {
		var me = this,
			vm = me.getViewModel();
		
		me.plugins = Sonicle.Utils.mergePlugins(me.plugins, [
			{
				ptype: 'sodropmask',
				text: WT.res('sofiledrop.text'),
				monitorExtDrag: false,
				shouldSkipMasking: function(dragOp) {
					return !Sonicle.plugin.DropMask.isBrowserFileDrag(dragOp);
				}
			}
		]);
		me.callParent(arguments);
		
		me.addRef('cxmAssignee', Ext.create({
			xtype: 'menu',
			items: [
				{
					iconCls: 'wt-icon-trash',
					text: WT.res('act-remove.lbl'),
					handler: function(s, e) {
						var rec = e.menuData.rec;
						if (rec) me.deleteAssigneeUI(rec);
					}
				}/*,
				'-',
				{
					xtype: 'somenuheader',
					text: WT.res('act-edit.lbl')
				}, {
					itemId: 'responseStatus',
					text: me.res('task.mni-responseStatus.lbl'),
					menu: {
						xtype: 'sostoremenu',
						store: {
							autoLoad: true,
							type: 'wttasksassigneerespstatus'
						},
						textField: 'desc',
						listeners: {
							click: function(s, itm, e) {
								var rec = e.menuData.rec;
								if (rec) rec.set('responseStatus', itm.getItemId());
							}
						}
					}
				}*/
			]
		}));
		
		me.addRef('cxmAttachment', Ext.create({
			xtype: 'menu',
			items: [
				{
					iconCls: 'wt-icon-open',
					text: WT.res('act-open.lbl'),
					handler: function(s, e) {
						var rec = e.menuData.rec;
						if (rec) me.openAttachmentUI(rec, false);
					}
				}, {
					iconCls: 'wt-icon-download',
					text: WT.res('act-download.lbl'),
					handler: function(s, e) {
						var rec = e.menuData.rec;
						if (rec) me.openAttachmentUI(rec, true);
					}
				}
			]
		}));
		
		me.add({
			region: 'center',
			xtype: 'wttabpanel',
			reference: 'tpnlmain',
			activeTab: 0,
			deferredRender: false,
			tabBar: {hidden: true},
			items: [
				{
					xtype: 'wtfieldspanel',
					title: me.res('task.main.tit'),
					paddingTop: true,
					paddingSides: true,
					scrollable: true,
					modelValidation: true,
					items: me.prepareMainFields()
				}, {
					xtype: 'wtcfieldseditorpanel',
					reference: 'tabcfields',
					title: me.res('task.cfields.tit'),
					bind: {
						store: '{record.cvalues}',
						fieldsDefs: '{record._cfdefs}'
					},
					serviceId: me.mys.ID,
					mainView: me,
					defaultLabelWidth: 120,
					listeners: {
						prioritize: function(s) {
							me.lref('tpnlmain').setActiveItem(s);
						}
					}
				}
			]
		});
		me.on('viewload', me.onViewLoad);
		me.on('viewclose', me.onViewClose);
		me.on('beforemodelsave', me.onBeforeModelSave, me);
		vm.bind('{foTags}', me.onTagsChanged, me);
		
		me.addRef('cxmChildren', Ext.create({
			xtype: 'sostoremenu',
			plain: true,
			store: {
				model: 'WTA.ux.data.SimpleModel',
				proxy: WTF.proxy(me.mys.ID, 'GetTaskChildren'),
				listeners: {
					beforeload: function(s) {
						Sonicle.Data.applyExtraParams(s, {
							parentId: me.getModel().get('id')
						});
					}
				}
			},
			textField: 'desc',
			loadingIndicator: true,
			useItemIdPrefix: true,
			topStaticItems: [
				{
					xtype: 'somenuheader',
					text: me.res('task.cxmChildren.tit')
				}, '-'
			],
			listeners: {
				click: function(s,itm) {
					if (itm) me.mys.openTaskUI(true, Sonicle.String.removeStart(itm.getItemId(), s.itemIdPrefix));
				}
			}
		}));
	},
	
	initTBar: function() {
		var me = this,
			SoU = Sonicle.Utils;
		
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'top', [
			me.createTopToolbar1Cfg(me.prepareTopToolbarItems())
		]);
		me.dockedItems = SoU.mergeDockedItems(me.dockedItems, 'bottom', [
			me.createStatusbarCfg()
		]);
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
	
	deleteAssigneeUI: function(rec) {
		this.getModel().assignees().remove(rec);
	},
	
	privates: {
		
		prepareTopToolbarItems: function() {
			var me = this;
			return [
				WTF.lookupCombo('categoryId', '_label', {
					xtype: 'socombo',
					reference: 'fldcategory',
					bind: {
						value: '{record.categoryId}',
						readOnly: '{foIsView}'
					},
					listConfig: {
						displayField: 'name'
					},
					swatchGeometry: 'circle',
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
									if (rec.get('_profileId') === mo.get('_profileId') && rec.get('_writable')) return true;
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
					width: 300,
					listeners: {
						select: function(s, rec) {
							me.setCategoryDefaults(rec);
						}
					}
				}),
				me.addAct('tags', {
					text: null,
					tooltip: me.res('act-manageTags.lbl'),
					iconCls: 'wt-icon-tags',
					handler: function() {
						me.manageTagsUI(Sonicle.String.split(me.getModel().get('tags'), '|'));
					}
				}),
				{
					xtype: 'wtuploadbutton',
					bind: {
						hidden: '{foIsView}',
						disabled: '{foIsView}'
					},
					tooltip: WT.res('act-attach.lbl'),
					iconCls: 'wt-icon-attach',
					sid: me.mys.ID,
					uploadContext: 'TaskAttachment',
					uploadTag: WT.uiid(me.getId()),
					dropElement: me.getId(),
					listeners: {
						beforeupload: function(up, file) {
							me.wait(file.name, true);
						},
						uploaderror: function(up, file, cause, json) {
							me.unwait();
							WTA.mixin.HasUpload.handleUploadError(up, file, cause);
						},
						uploadprogress: function(up, file) {
							me.wait(Ext.String.format('{0}: {1}%', file.name, file.percent), true);
						},
						fileuploaded: function(up, file, resp) {
							me.unwait();
							var sto = me.getModel().attachments();
							sto.add(sto.createModel({
								name: file.name,
								size: file.size,
								_uplId: resp.data.uploadId
							}));
						}
					}
				},
				me.addAct('print', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print',
					handler: function() {
						//TODO: aggiungere l'azione 'salva' permettendo così la stampa senza chiudere la form
						me.printTaskUI();
					}
				}),
				me.addAct('delete', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete',
					hidden: true,
					handler: function() {
						me.deleteTaskUI();
					}
				}),
				'->',
				{
					xtype: 'checkbox',
					bind: '{isPrivate}',
					hideEmptyLabel: true,
					boxLabel: me.res('task.fld-private.lbl')
				}
			];
		},
		
		prepareMainFields: function() {
			var me = this;
			return [
				me.createTagsFieldCfg(),
				{
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionSubject',
					items: [
						{
							xtype: 'sofieldhgroup',
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
									emptyText: me.res('task.fld-subject.emp'),
									flex: 1
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'sotogglebutton',
									bind: {
										pressed: '{foHasLocation}'
									},
									ui: 'default-toolbar',
									iconCls: 'wttasks-icon-showLocation',
									tooltip: me.res('task.btn-showLocation.tip'),
									toggleHandler: function(s, state) {
										me.showHideField('fldlocation', !state);
									}
								}, {
									xtype: 'sohspacer'
								}, {
									xtype: 'sotogglebutton',
									bind: {
										pressed: '{foHasContact}'
									},
									ui: 'default-toolbar',
									iconCls: 'wttasks-icon-showContact',
									tooltip: me.res('task.btn-showContact.tip'),
									toggleHandler: function(s, state) {
										me.showHideField('fldcontact', !state);
									}
								}
							],
							fieldLabel: me.res('task.fld-subject.lbl')
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionLocation',
					bind: {
						hidden: '{foHideLocationAsAddress}'
					},
					hidden: true,
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'textfield',
									bind: '{record.location}',
									fieldLabel: me.res('task.fld-location.lbl'),
									flex: 1
								}
							]
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionLocation',
					bind: {
						hidden: '{foHideLocationAsMeeting}'
					},
					hidden: true,
					items: [
						{
							xtype: 'textfield',
							bind: '{record.location}',
							triggers: {
								clear: WTF.clearTrigger()
							},
							fieldLabel: me.res('task.fld-location.lbl')
						}, {
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'button',
									ui: '{secondary}',
									iconCls: 'wt-icon-clipboard-copy',
									tooltip: me.res('task.btn-copyMeeting.tip'),
									handler: function() {
										var location = me.getModel().get('location');
										if (!Ext.isEmpty(location)) {
											Sonicle.ClipboardMgr.copy(location);
											WT.toast(WT.res('toast.info.copied'));
										}
									}
								}, {
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'button',
									ui: '{secondary}',
									iconCls: 'wt-glyph-open',
									text: me.res('task.btn-goToMeeting.lbl'),
									handler: function() {
										var location = me.getModel().get('location');
										if (Sonicle.String.startsWith(location, 'http', true)) {
											Sonicle.URLMgr.open(location, true);
										}
									}
								}
							]
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionContact',
					bind: {
						hidden: '{hidden.fldcontact}'
					},
					hidden: true,
					items: [
						{
							//xtype: 'sofieldhgroup',
							//items: [
							//	{
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
									fieldLabel: me.res('task.fld-contact.lbl')
							//	}
							//]
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionDescription',
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								me.createDescriptionFieldCfg({
									minHeight: 100,
									flex: 1
								})
							]
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionDateTime',
					items: [
						{
							xtype: 'sofieldhgroup',
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
									fieldLabel: me.res('task.fld-start.lbl'),
									width: 130
								}, {
									xtype: 'sohspacer',
									ui: 'small'
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
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'datefield',
									bind: '{dueDate}',
									startDay: WT.getStartDay(),
									format: WT.getShortDateFmt(),
									triggers: {
										clear: WTF.clearTrigger()
									},
									emptyText: me.res('task.fld-due.emp'),
									fieldLabel: me.res('task.fld-due.lbl'),
									width: 130
								}, {
									xtype: 'sohspacer',
									ui: 'small'
								}, {
									xtype: 'timefield',
									bind: '{dueTime}',
									format: WT.getShortTimeFmt(),
									width: 90
								}
							]
						}, {
							xtype: 'sofieldhgroup',
							items: [
								{
									xtype: 'sorrrepeatfield',
									bind: {
										value: '{foRRuleString}',
										defaultStartDate: '{startDate}',
										disabled: '{foRRDisabled}'
									},
									noneText: WT.res('sorrrepeatfield.none'),
									dailyText: WT.res('sorrrepeatfield.daily'),
									weeklyText: WT.res('sorrrepeatfield.weekly'),
									monthlyText: WT.res('sorrrepeatfield.monthly'),
									yearlyText: WT.res('sorrrepeatfield.yearly'),
									advancedText: WT.res('sorrrepeatfield.advanced'),
									width: 240
								}, {
									xtype: 'sohspacer',
									ui: 'medium',
									cls: 'wttasks-spacer-event-reminder'
								}, {
									xtype: 'checkbox',
									bind: '{foHasReminder}',
									hideEmptyLabel: true,
									boxLabel: me.res('task.fld-reminder.lbl'),
									handler: function(s, nv) {
										var mo = me.getModel(),
											reminder = mo.get('reminder');
										if (nv === true && Ext.isEmpty(reminder)) {
											mo.set('reminder', 5);
										} else if (nv === false && !Ext.isEmpty(reminder)) {
											mo.set('reminder', null);
										}
									}
								}
							]
						}, {
							xtype: 'so-displayfield',
							bind: {
								value: '{foHumanReadableRRule}',
								disabled: '{foRRDisabled}',
								hidden: '{!foHasRecurrence}'
							},
							hidden: true,
							enableClickEvents: true,
							tooltip: me.res('task.btn-editRecurrence.tip'),
							handler: function(s) {
								me.editRecurrence();
							}
						}, {
							xtype: 'sofieldvgroup',
							bind: {
								hidden: '{!foHasRecurrence}'
							},
							items: [
								{
									xtype: 'sotext',
									bind: {
										hidden: '{!foIsSeriesInstance}'
									},
									hidden: true,
									iconType: 'info',
									cls: 'wt-color-info',
									text: me.res('task.rrnoteditable.series.info')
								}, {
									xtype: 'sotext',
									bind: {
										hidden: '{!foIsParent}'
									},
									hidden: true,
									iconType: 'info',
									cls: 'wt-color-info',
									text: me.res('task.rrnoteditable.parent.info')
								}
							]
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionReminder',
					bind: {
						hidden: '{!foHasReminder}'
					},
					hidden: true,
					items: [
						WTF.localCombo('id', 'desc', {
							bind: '{record.reminder}',
							store: {
								xclass: 'Sonicle.webtop.tasks.store.TaskReminder',
								autoLoad: true
							},
							triggers: {
								clear: WTF.clearTrigger()
							},
							emptyText:  me.res('task.fld-reminder.emp'),
							fieldLabel: me.res('task.fld-reminder.lbl')
						})
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionStatus',
					items: [
						{
							xtype: 'sofieldhgroup',
							items: [
								WTF.lookupCombo('id', 'desc', {
									xtype: 'socombo',
									bind: '{record.status}',
									store: {
										xclass: me.mys.preNs('store.TaskStatus'),
										autoLoad: true
									},
									iconField: 'icon',
									listeners: {
										select: function(s, rec) {
											me.getModel().setProgressOnStatus(rec.get('id'));
										}
									},
									fieldLabel: me.res('task.fld-status.lbl'),
									flex: 1
								}), {
									xtype: 'sohspacer',
									ui: 'small',
									cls: 'wttasks-spacer-task-status'
								},
								WTF.lookupCombo('id', 'desc', {
									bind: '{importance}',
									store: {
										xclass: me.mys.preNs('store.TaskImportance'),
										autoLoad: true
									},
									fieldLabel: me.res('task.fld-importance.lbl'),
									flex: 1
								}), {
									xtype: 'sohspacer',
									ui: 'small',
									cls: 'wttasks-spacer-task-status'
								},
								{
									xtype: 'numberfield',
									bind: '{record.progress}',
									step: 25,
									minValue: 0,
									maxValue: 100,
									allowDecimal: false,
									listeners: {
										blur: function(s) {
											me.getModel().setStatusOnProgress(s.getValue());
										}
									},
									fieldLabel: me.res('task.fld-progress.lbl'),
									flex: 1
								}
							]	
						}
					]
				}, {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionDocRef',
					//bind: {
					//	hidden: '{!foHasDocRef}'
					//},
					//hidden: true,
					items: [
						{
							xtype: 'textfield',
							bind: '{record.docRef}',
							fieldLabel: me.res('task.fld-docRef.lbl')
						}
					]
				}, /*{
					xtype: 'sofieldsection',
					labelIconCls: 'wtcal-icon-eventAssignees',
					items: [
						{
							xtype: 'wtrcptsuggestcombo',
							listConfig: {
								width: 350,
								minWidth: 350
							},
							emptyText: me.res('event.fld-assignees.emp'),
							triggers: {
								clear: WTF.clearTrigger()
							},
							listeners: {
								select: function(s, rec) {
									me.doImportRecipientsAsAssignees([rec.get('description')]);
									s.setValue(null);
								},
								enterkeypress: function(s, e, value) {
									me.doImportRecipientsAsAssignees([value]);
									s.setValue(null);
								}
							},
							flex: 1
						}
					]
				}, {
					xtype: 'sofieldsection',
					bind: {
						hidden: '{!foHasAssigees}'
					},
					hidden: true,
					items: [
						me.createAssigneesGridCfg({
							reference: 'gpassignees',
							maxHeight: 150
						})
					]
				},*/ {
					xtype: 'sofieldsection',
					labelIconCls: 'wttasks-icon-sectionAttachments',
					bind: {
						hidden: '{!foHasAttachments}'
					},
					hidden: true,
					items: [
						{
							xtype: 'wtattachmentsfield',
							bind: {
								itemsStore: '{record.attachments}'
							},
							itemClickHandler: function(s, rec, e) {
								Sonicle.Utils.showContextMenu(e, me.getRef('cxmAttachment'), {rec: rec});
							},
							fieldLabel: me.res('task.fld-attachments.lbl')
						}
					]
				}
			];
		},
		
		createTagsFieldCfg: function(cfg) {
			return Ext.apply({
				xtype: 'sotagdisplayfield',
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
				hideLabel: true
			}, cfg);
		},
		
		createDescriptionFieldCfg: function(cfg) {
			return Ext.apply({
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: this.res('task.fld-description.lbl'),
				resizable: true,
				smartResize: true
			}, cfg);
		},
		
		createAssigneesGridCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'gridpanel',
				bind: {
					store: '{record.assignees}'
				},
				border: true,
				rowLines: false,
				hideHeaders: true,
				columns: [
					{
						xtype: 'soiconcolumn',
						dataIndex: 'responseStatus',
						getIconCls: function(v) {
							return 'wttasks-icon-assigneeResponse-' + v;
						},
						getTooltip: function(v, rec) {
							return me.res('store.assigneeRespStatus.' + v);
						},
						hideText: false,
						getText: function(v, rec) {
							return Sonicle.String.htmlEncode(rec.get('recipient'));
						},
						flex: 1
					}, {
						xtype: 'soactioncolumn',
						items: [
							{
								iconCls: 'wt-glyph-menu-kebab',
								handler: function(g, ridx, cidx, itm, e, node, row) {
									var rec = g.getStore().getAt(ridx);
									Sonicle.Utils.showContextMenu(e, me.getRef('cxmAssignee'), {rec: rec});
								}
							}
						]
					}
				]
			}, cfg);
		},
		
		createStatusbarCfg: function() {
			var me = this;
			return {
				xtype: 'statusbar',
				items: [
					{
						xtype: 'button',
						ui: '{tertiary}',
						bind: {
							hidden: '{!foIsParent}',
							text: '{record._childComplCount}/{record._childTotalCount}'
						},
						icon: Sonicle.String.toDataURL('image/svg+xml', Sonicle.grid.column.Nest.hierarchySvg(null, null, 'red')),
						tooltip: me.res('task.btn-openChildren.tip'),
						hidden: true,
						handler: function(s, e) {
							var cmx = Sonicle.Utils.showContextMenu(e, me.getRef('cxmChildren'));
							cmx.getStore().load();
						}
					}, {
						xtype: 'button',
						ui: '{tertiary}',
						bind: {
							hidden: '{!foIsChild}',
							text: '{record._parentSubject:ellipsis(30)}'
						},
						icon: Sonicle.String.toDataURL('image/svg+xml', Sonicle.grid.column.Nest.hierarchySvg(null, 'red')),
						tooltip: me.res('task.btn-openParent.tip'),
						hidden: true,
						handler: function() {
							me.mys.openTaskUI(true, me.getModel().get('parentId'));
						}
					}, 
					me.mys.hasAuditUI() ? me.addAct('taskAuditLog', {
						text: null,
						bind: {
							hidden: '{foIsNew}'
						},
						tooltip: WT.res('act-auditLog.lbl'),
						iconCls: 'wt-icon-audit',
						handler: function() {
							me.mys.openAuditUI(Sonicle.String.substrBefore(me.getModel().getId(), '.'), 'TASK');
						},
						scope: me
					}) : null
				]
			};
		},
		
		setCategoryDefaults: function(cat) {
			var mo = this.getModel();
			if (mo) {
				mo.set({
					isPrivate: cat.get('tasPrivate'),
					reminder: cat.get('tasReminder')
				});
			}
		},
		
		showHideField: function(vmField, hidden) {
			this.getVM().set('hidden.'+vmField, hidden);
		},
		
		showField: function(vmField, focusRef) {
			this.showHideField(vmField, false);
			if (focusRef) this.lref(vmField).focus(true, true);
		},
		
		isFieldHidden: function(vmField) {
			return !this.getVM().get('hidden.'+vmField);
		},
		
		initHiddenFields: function() {
			var mo = this.getModel();
			Sonicle.VMUtils.set(this.getVM(), 'hidden', {
				fldlocation: mo.isFieldEmpty('location'),
				fldcontact: mo.isFieldEmpty('contact')
			});
		},
		
		onViewLoad: function(s, success) {
			var me = this;
			
			if (me.isMode(me.MODE_NEW)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setHidden(true);
				me.getAct('tags').setHidden(false);
				if (me.mys.hasAuditUI()) me.getAct('taskAuditLog').setDisabled(true);
				me.reloadCustomFields((me.opts.data || {}).tags, me.opts.cfData, false);
				
			} else if (me.isMode(me.MODE_VIEW)) {
				me.getAct('saveClose').setDisabled(true);
				me.getAct('delete').setHidden(true);
				me.getAct('tags').setHidden(true);
				if (me.mys.hasAuditUI()) me.getAct('taskAuditLog').setDisabled(false);
				me.hideCustomFields(me.getModel().cvalues().getCount() < 1);
				
			} else if (me.isMode(me.MODE_EDIT)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setHidden(false);
				me.getAct('tags').setHidden(false);
				me.lref('fldcategory').setReadOnly(false);
				if (me.mys.hasAuditUI()) me.getAct('taskAuditLog').setDisabled(false);
				me.hideCustomFields(me.getModel().cvalues().getCount() < 1);
			}
			me.initHiddenFields();
		},
				
		onViewClose: function(s) {
			s.mys.cleanupUploadedFiles(WT.uiid(s.getId()));
		},
		
		onBeforeModelSave: function(s) {
			var me = this,
				cp = me.lref('tabcfields');
			if (!cp.isValid()) {
				me.lref('tpnlmain').getLayout().setActiveItem(cp);
				return false;
			}
		},
		
		onTagsChanged: function(nv, ov) {
			var me = this;
			if (ov && Sonicle.String.difference(nv, ov).length > 0) { // Make sure that there are really differences!
				me.reloadCustomFields(nv, false);
			}
		},
		
		reloadCustomFields: function(tags, cfInitialData) {
			var me = this;
			WTA.util.CustomFields.reloadCustomFields(tags, cfInitialData, {
				serviceId: me.mys.ID,
				model: me.getModel(),
				idField: 'id',
				cfPanel: me.lref('tabcfields'),
				callback: function(success, json) {
					if (success) me.hideCustomFields(json.total < 1);
				},
				scope: me
			});
		},
		
		hideCustomFields: function(hide) {
			this.lref('tpnlmain').getTabBar().setHidden(hide);
		},
		
		editRecurrence: function() {
			var me = this,
				mo = me.getModel(),
				vw = WT.createView(me.mys.ID, 'view.RecurrenceEditor', {
					swapReturn: true,
					viewCfg: {
						rruleString: mo.get('rruleString')
					}
				});
			
			vw.on('viewok', function(s, data) {
				var mo = me.getModel(),
					split = Sonicle.form.field.rr.Recurrence.splitRRuleString(data.rruleString);
				mo.set('rrule', split.rrule);
				mo.setStartDate(split.start);
			});
			vw.showView();
		}
	}
});
