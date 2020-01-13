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
		'Sonicle.form.field.ComboBox',
		'Sonicle.plugin.FileDrop',
		'WTA.ux.data.EmptyModel',
		'WTA.ux.data.ValueModel',
		'WTA.ux.field.SuggestCombo',
		'WTA.ux.grid.Attachments',
		'Sonicle.webtop.tasks.store.Importance',
		'Sonicle.webtop.tasks.store.Status',
		'Sonicle.webtop.tasks.model.Task',
		'Sonicle.webtop.tasks.model.CategoryLkp'
	],
	
	dockableConfig: {
		title: '{task.tit}',
		iconCls: 'wttasks-icon-task',
		width: 700,
		height: 480
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
		});
	},
	
	initComponent: function() {
		var me = this,
				pid = WT.getVar('profileId');
		
		Ext.apply(me, {
			tbar: [
				me.addAct('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					tooltip: null,
					iconCls: 'wt-icon-saveClose-xs',
					handler: function() {
						me.saveView(true);
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
				me.addAct('print', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print',
					handler: function() {
						//TODO: aggiungere l'azione 'salva' permettendo così la stampa senza chiudere la form
						me.printTask(me.getModel().getId());
					}
				}),
				'->',
				WTF.lookupCombo('categoryId', '_label', {
					xtype: 'socombo',
					reference: 'fldcategory',
					bind: '{record.categoryId}',
					listConfig: {
						displayField: 'name',
						groupCls: 'wt-theme-text-greyed'
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
		});
		me.plugins = me.plugins || [];
		me.plugins.push({
			ptype: 'sofiledrop',
			text: WT.res('sofiledrop.text')
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wttabpanel',
			deferredRender: false,
			items: [{
				xtype: 'wtform',
				title: me.mys.res('task.main.tit'),
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
							me.getAct('saveClose').execute();
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
							format: WT.getShortDateFmt(),
							triggers: {
								clear: WTF.clearTrigger()
							},
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
										if(rec.get('id') === 'notstarted') me.getModel().set('percentage', 0);
										if(rec.get('id') === 'completed') me.getModel().set('percentage', 100);
									}
								}
							})
						]
					}, {
						items: [{
							xtype: 'datefield',
							bind: '{dueDate}',
							startDay: WT.getStartDay(),
							format: WT.getShortDateFmt(),
							triggers: {
								clear: WTF.clearTrigger()
							},
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
									if (nv === 100) me.getModel().set('status','completed');
									if (nv === 0) me.getModel().set('status','notstarted');
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
						boxLabel: me.mys.res('task.fld-reminderDate.lbl')
					},{
						xtype: 'datefield',
						bind: {
							value: '{reminderDate}',
							disabled: '{!fldhasreminder.checked}'
						},
						startDay: WT.getStartDay(),
						format: WT.getShortDateFmt(),
						margin: '0 5 0 0',
						width: 120
					}, {
						xtype: 'timefield',
						bind: {
							value: '{reminderTime}',
							disabled: '{!fldhasreminder.checked}'
						},
						format: WT.getShortTimeFmt(),
						margin: '0 5 0 0',
						width: 90
					}]
				}]
			}, {
				xtype: 'wtattachmentsgrid',
				title: me.mys.res('task.attachments.tit'),
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
						me.getComponent(0).getLayout().setActiveItem(s);
					}
				}
			}]
		});
		me.on('viewload', me.onViewLoad);
		me.on('viewclose', me.onViewClose);
	},
	
	saveView: function(closeAfter) {
		var me = this,
				rec = me.getModel();
		if (!me.lref('fldhasreminder').getValue())
			rec.set('reminderDate',null);
		me.callParent(arguments);
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
				taskId: me.getModel().getId(),
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
				rec = me.getModel();
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				me.wait();
				WT.ajaxReq(me.mys.ID, 'ManageTasks', {
					params: {
						crud: 'delete',
						ids: WTU.arrayAsParam([rec.get('taskId')])
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
	},
	
	privates: {
		onViewLoad: function(s, success) {
			if (!success) return;
			var me = this;

			if (me.isMode(me.MODE_NEW)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setDisabled(true);
				me.lref('fldcategory').setReadOnly(false);
			} else if(me.isMode(me.MODE_VIEW)) {
				me.getAct('saveClose').setDisabled(true);
				me.getAct('delete').setDisabled(true);
				me.lref('fldcategory').setReadOnly(true);
			} else if(me.isMode(me.MODE_EDIT)) {
				me.getAct('saveClose').setDisabled(false);
				me.getAct('delete').setDisabled(false);
				me.lref('fldcategory').setReadOnly(false);
			}
			me.lref('fldsubject').focus(true);
		},
		
		onViewClose: function(s) {
			s.mys.cleanupUploadedFiles(WT.uiid(s.getId()));
		},
		
		setCategoryDefaults: function(cat) {
			var mo = this.getModel();
			if (mo) {
				mo.set({
					isPrivate: cat.get('tasPrivate')
				});
			}
		}
	}
});
