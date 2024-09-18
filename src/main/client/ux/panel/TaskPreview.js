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
Ext.define('Sonicle.webtop.tasks.ux.panel.TaskPreview', {
	extend: 'WTA.ux.panel.Panel',
	requires: [
		'Sonicle.VMUtils',
		'Sonicle.form.field.Display',
		'Sonicle.form.field.ColorDisplay',
		'Sonicle.form.field.TagDisplay',
		'Sonicle.grid.TileList',
		'WTA.util.FoldersTree2',
		'WTA.ux.panel.CustomFieldsPreview',
		'Sonicle.webtop.tasks.model.TaskPreview',
		'Sonicle.webtop.tasks.store.TaskImportance'
	],
	mixins: [
		'WTA.mixin.HasModel'
	],
	
	/*
	 * @cfg {Ext.data.Store} tagsStore
	 * The Store that holds available tags data.
	 */
	tagsStore: null,
	
	layout: 'card',
	cls: 'wttasks-preview',
	referenceHolder: true,
	
	viewModel: {},
	modelName: 'Sonicle.webtop.tasks.model.TaskPreview',
	mys: null,
	
	/**
	 * @event print
	 * @param {Sonicle.webtop.tasks.ux.panel.TaskPreview} this
	 */
	
	/**
	 * @event showmenu
	 * @param {Sonicle.webtop.tasks.ux.panel.TaskPreview} this
	 * @param {Event} e The click event
	 */
	
	/**
	 * @event clearselection
	 * @param {Sonicle.webtop.tasks.ux.panel.TaskPreview} this
	 */
	
	/**
	 * @event opentask
	 * @param {Sonicle.webtop.tasks.ux.panel.TaskPreview} this
	 * @param {Boolean} edit
	 * @param {String} id Record's ID to edit
	 */
	
	/**
	 * @event setcompleted
	 * @param {Sonicle.webtop.tasks.ux.panel.TaskPreview} this
	 * @param {String[]} ids Record IDs to complete
	 */
	
	/**
	 * @event sendbyemail
	 * @param {Sonicle.webtop.tasks.ux.panel.TaskPreview} this
	 * @param {String[]} ids Record IDs to send by email
	 */
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		var durRes = function(sym) { return WT.res('word.dur.'+sym); },
			durSym = [durRes('y'), durRes('d'), durRes('h'), durRes('m'), durRes('s')];
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foIsEditable: WTF.foGetFn('record', '_itPerms', function(val) {
				return WTA.util.FoldersTree2.toRightsObj(val).UPDATE;
			}),
			foCategory: WTF.foMultiGetFn('record', ['categoryName', '_orDN'], function(v) {
				return Sonicle.webtop.tasks.Service.calcCategoryLabel(v['categoryName'], v['_orDN']);
			}),
			foSubject: WTF.foGetFn('record', 'subject', function(val) {
				var tipAttrs = Sonicle.Utils.generateTooltipAttrs,
					s = '';
				if (this.get('record.isPrivate') === true) s += '<i class="fas fa-lock" aria-hidden="true" '+tipAttrs(me.mys.res('task.fld-private.lbl'))+' style="margin-right:5px;font-size:initial"></i>';
				return s + Ext.String.htmlEncode(Ext.String.ellipsis(val, 50));
			}),
			foSubjectIconCls: WTF.foGetFn('record', 'isPrivate', function(val) {
				return val === true ? 'wttasks-icon-taskPrivate' : '';
			}),
			foStatus: WTF.foGetFn('record', 'status', function(val) {
				var complOn = this.get('record.completedOn'),
					infoCls = me.cls + '-taskstatus-info',
					s = '';
				if ('CO' === val && Ext.isDate(complOn)) {
					s += '<span class="' + infoCls + '">('+Ext.Date.format(complOn, WT.getShortDateTimeFmt())+')</span>';
				} else if (Sonicle.String.isIn(val, ['IP','CA','WA'])) {
					s += '<span class="' + infoCls + '">('+this.get('record.progress')+'%)</span>';
				}
				return me.mys.res('store.taskStatus.'+val) + s;
			}),
			foStatusIconCls: WTF.foGetFn('record', 'status', function(val) {
				return 'wttasks-icon-taskStatus-' + val;
			}),
			foImportance: WTF.foGetFn('record', 'importance', function(val) {
				return Sonicle.webtop.tasks.store.TaskImportance.buildLabel(val);
			}),
			foImportanceIconCls: WTF.foGetFn('record', 'importance', function(val) {
				return Sonicle.webtop.tasks.store.TaskImportance.buildIcon(val);
			}),
			foStart: WTF.foGetFn('record', 'start', function(val) {
				return Ext.isDate(val) ? Ext.Date.format(val, 'D ' + WT.getLongDateFmt() + ' ' + WT.getShortTimeFmt()) : null;
			}),
			foDue: WTF.foGetFn('record', 'due', function(val) {
				var infoCls = me.cls + '-taskdue-info',
					s = '', ret;
				if (Ext.isDate(val)) {
					if ('CO' !== this.get('record.status')) {
						var SoD = Sonicle.Date,
							diff = SoD.diffDays(val, new Date()),
							hrd;
						if (diff > 0) {
							hrd = SoD.humanReadableDuration(Math.abs(diff * 86400), {hours: false, minutes: false, seconds: false}, durSym);
							if (!Ext.isEmpty(hrd)) {
								s += '<span class="' + infoCls + '">('+me.mys.res('taskPreview.single.task.due.late', '+'+hrd)+')</span>';
							}
						}
					}
					ret = Ext.Date.format(val, 'D ' + WT.getLongDateFmt() + ' ' + WT.getShortTimeFmt()) + s;
				}
				return ret;
			}),
			foHasStart: WTF.foIsEmpty('record', 'start', true),
			foHasDue: WTF.foIsEmpty('record', 'due', true),
			foHasLocation: WTF.foIsEmpty('record', 'location', true),
			foHasDocRef: WTF.foIsEmpty('record', 'docRef', true),
			foHasContact: WTF.foIsEmpty('record', 'contactEmail', true),
			foHasReminder: WTF.foIsEmpty('record', 'reminder', true),
			foHasTags: WTF.foIsEmpty('record', 'tags', true),
			foHasDescription: WTF.foIsEmpty('record', 'description', true),
			foMultiSelTitle: WTF.foGetFn(null, 'records', function(val) {
				return val ? me.mys.res('taskPreview.multi.tit', val.length) : null;
			}),
			foHasEmail: WTF.foIsEmpty('record', 'contactEmail', true),
			foIsCompleted: WTF.foIsEqual('record', 'status', 'CO'),
			foWriteMessageTip: WTF.foResFormat('record', 'contactEmail', me.mys.ID, 'taskPreview.act-writeMessage.tip')
		});
		me.loadTaskBuffered = Ext.Function.createBuffered(me.loadTask, 200);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.add([
			me.createEmptyItemCfg({itemId: 'empty'}),
			me.createSingleItemCfg({itemId: 'single'}),
			me.createMultiItemCfg({itemId: 'multi'})
		]);
		me.setActiveItem('empty');
		me.getViewModel().bind('{record._cfdefs}', me.onCFDefsUpdate, me);
	},
	
	setSelection: function(records) {
		var me = this, card = me;
		me.getViewModel().set('records', records);
		if (records && records.length === 1) {
			me.loadTaskBuffered(records[0].getId(), 'single');
		} else if (records && records.length > 1) {
			card.setActiveItem('multi');
		} else {
			me.loadTaskBuffered(null, 'empty');
		}
	},
	
	isLoaded: function(id, idField) {
		var recs = this.getViewModel().get('records'), val;
		if (recs && recs.length === 1) {
			val = Ext.isEmpty(idField) ? recs[0].getId() : recs[0].get(idField);
			return id === val;
		} else {
			return false;
		}
	},
	
	loadTask: function(id, /*private*/ activateCard) {
		var me = this;
		me.clearModel();
		if (id) {
			me.loadModel({
				data: {id: id},
				dirty: false
			});
		}
		if (!Ext.isEmpty(activateCard)) {
			me.setActiveItem(activateCard);
		}
	},
	
	privates: {
		createEmptyItemCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'container',
				layout: {
					type: 'vbox',
					pack: 'center',
					align: 'middle'
				},
				items: [
					{
						xtype: 'label',
						text: me.mys.res('taskPreview.empty.tit'),
						cls: 'wt-theme-text-title'
					}, {
						xtype: 'label',
						text: me.mys.res('taskPreview.empty.txt'),
						cls: 'wt-theme-text-subtitle'
					}
				]
			}, cfg);
		},
		
		createMultiItemCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'container',
				layout: {
					type: 'vbox',
					pack: 'center',
					align: 'middle'
				},
				items: [
					{
						xtype: 'label',
						bind: {
							text: '{foMultiSelTitle}'
						},
						cls: 'wt-theme-text-title'
					}, {
						xtype: 'toolbar',
						vertical: true,
						margin: '20 0 0 0',
						items: [
							{
								text: me.mys.res('taskPreview.multi.tb.setCompleted.lbl'),
								iconCls: 'wttasks-icon-setTaskCompleted',
								handler: function() {
									me.fireEvent('setcompleted', me, Sonicle.Data.collectValues(me.getVM().get('records')));
								}
							}, {
								text: me.mys.res('taskPreview.multi.tb.sendByEmail.lbl'),
								iconCls: 'wttasks-icon-sendByEmail',
								handler: function() {
									me.fireEvent('sendbyemail', me, Sonicle.Data.collectValues(me.getVM().get('records')));
								}
							}
						]
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'formseparator',
						width: 180
					}, {
						xtype: 'sospacer'
					}, {
						xtype: 'toolbar',
						vertical: true,
						items: [
							{
								text: me.mys.res('taskPreview.multi.tb.cancel.lbl'),
								tooltip: me.mys.res('taskPreview.multi.tb.cancel.tip'),
								handler: function() {
									me.fireEvent('clearselection', me);
								}
							}
						]
					}
				]
			}, cfg);
		},
		
		createTaskTopToolbarCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'toolbar',
				border: false,
				items: [
					'->',
					{
						xtype: 'button',
						tooltip: WT.res('act-print.lbl'),
						iconCls: 'wt-icon-print',
						handler: function() {
							me.fireEvent('print', me);
						}
					}, {
						xtype: 'button',
						iconCls: 'fas fa-ellipsis-v',
						arrowVisible: false,
						handler: function(s, e) {
							me.fireEvent('showmenu', me, e);
						}
					}
				]
			}, cfg);
		},
		
		createTaskActionsToolbarCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'toolbar',
				border: false,
				items: Ext.Array.join(
					{
						xtype: 'button',
						ui: '{primary}',
						text: me.mys.res('taskPreview.single.tb.openTask.lbl'),
						iconCls: 'wttasks-icon-openTask',
						handler: function() {
							var vm = me.getVM();
							me.fireEvent('opentask', me, WTA.util.FoldersTree2.toRightsObj(vm.get('record._itPerms')).UPDATE, vm.get('record.id'));
						}
					},
					'-',
					{
						xtype: 'button',
						bind: {
							disabled: '{foIsCompleted}'
						},
						ui: '{tertiary}',
						text: me.mys.res('taskPreview.single.tb.setCompleted.lbl'),
						iconCls: 'wttasks-icon-setTaskCompleted',
						handler: function() {
							me.fireEvent('setcompleted', me, me.getVM().get('record.id'));
						}
					}, {
						xtype: 'button',
						ui: '{tertiary}',
						tooltip: me.mys.res('taskPreview.single.tb.sendByEmail.tip'),
						iconCls: 'wttasks-icon-sendByEmail',
						handler: function() {
							me.fireEvent('sendbyemail', me, me.getVM().get('record.id'));
						}
					}, {
						xtype: 'button',
						ui: '{tertiary}',
						bind: {
							hidden: '{!foHasEmails}',
							tooltip: '{foWriteMessageTip}'
						},
						tooltip: me.mys.res('taskPreview.single.tb.writeMessage.tip'),
						iconCls: 'wttasks-icon-writeMessage',
						hidden: true,
						handler: function() {
							var vm = me.getVM();
							me.fireEvent('writeemail', me, [vm.get('record.contactEmail')], vm.get('record.id'));
						}
					}
				)
			}, cfg);
		},
		
		createSingleItemCfg: function(cfg) {
			var me = this;
			return Ext.apply({
				xtype: 'container',
				layout: 'vbox',
				defaults: {
					width: '100%'
				},
				items: [
					{
						xtype: 'wtpanel',
						cls: me.cls + '-header',
						layout: 'vbox',
						items: [
							{
								xtype: 'so-displayfield',
								bind: {
									value: '{record.subject}',
									iconCls: '{foSubjectIconCls}'
								},
								cls: me.cls + '-header-subject'
							}, {
								xtype: 'so-displayfield',
								bind: {
									value: '{foCategory}',
									color: '{record.categoryColor}'
								},
								colorize: true,
								swatchGeometry: 'circle',
								cls: me.cls + '-header-category'
							}, {
								xtype: 'sotagdisplayfield',
								bind: {
									value: '{record.tags}',
									hidden: '{!foHasTags}'
								},
								delimiter: '|',
								store: me.tagsStore,
								valueField: 'id',
								displayField: 'name',
								colorField: 'color',
								sourceField: 'source',
								hidden: true,
								hideLabel: true
							}
						],
						dockedItems: [
							me.createTaskTopToolbarCfg({dock: 'top'}),
							me.createTaskActionsToolbarCfg({dock: 'bottom', cls: me.cls + '-header-tbactions'})
						]
					}, {
						xtype: 'tabpanel',
						cls: me.cls + '-tab',
						border: false,
						items: [
							{
								xtype: 'wtpanel',
								title: me.mys.res('taskPreview.single.task.tit'),
								scrollable: true,
								cls: me.cls + '-main',
								layout: 'vbox',
								defaults: {
									width: '100%'
								},
								items: [
									{
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.status.lbl') + ': {foStatus}',
											iconCls: '{foStatusIconCls}'
										},
										htmlEncode: false,
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.importance.lbl') + ': {foImportance}',
											iconCls: '{foImportanceIconCls}'
										},
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.start.lbl') + ': {foStart}',
											hidden: '{!foHasStart}'
										},
										iconCls: 'wttasks-icon-taskStart',
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.due.lbl') + ': {foDue}',
											hidden: '{!foHasDue}'
										},
										htmlEncode: false,
										iconCls: 'wttasks-icon-taskDue',
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.reminder.lbl') + ': {foReminder}',
											hidden: '{!foHasReminder}'
										},
										htmlEncode: false,
										iconCls: 'wttasks-icon-taskDue',
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.location.lbl') + ': {record.location}',
											hidden: '{!foHasLocation}'
										},
										iconCls: 'wttasks-icon-taskLocation',
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.contact.lbl') + ': {record.contactEmail}',
											hidden: '{!foHasContact}'
										},
										iconCls: 'wttasks-icon-taskContact',
										labelWidth: 25 // Reduce label in order to display icon only
									}, {
										xtype: 'so-displayfield',
										bind: {
											value: me.mys.res('taskPreview.single.task.docRef.lbl') + ': {record.docRef}',
											hidden: '{!foHasDocRef}'
										},
										iconCls: 'wttasks-icon-taskDocRef',
										labelWidth: 25 // Reduce label in order to display icon only
									},
									me.createDescriptionFieldCfg({
										minHeight: 100,
										flex: 1
										
										//grow: true,
										//growMin: 60,
										//growMax: 200
									})
								]
							}, {
								xtype: 'wtcfieldspreviewpanel',
								reference: 'tabcfields',
								title: me.mys.res('taskPreview.single.cfields.tit'),
								bind: {
									store: '{record.cvalues}'
									// Do not use this binding here, it will cause internal exception
									// in Ext.app.bind.Stub during model load with new ID. (see explicit vm.bind in initComponent)
									//fieldsDefs: '{record._cfdefs}'
								},
								serviceId: me.mys.ID,
								cls: me.cls + '-cfields'
							}
						],
						tabBar:	{
							items: [
								{
									xtype: 'tbfill'
								}, me.mys.hasAuditUI() ? {
									xtype: 'button',
									margin: '0 5 0 5',
									ui: 'default-toolbar',
									text: null,
									tooltip: WT.res('act-auditLog.lbl'),
									iconCls: 'fas fa-history',
									handler: function() {
										var vm = me.getVM();
										me.fireEvent('showaudit', me, vm.get('record.isList'), vm.get('record.id'));
									}
								} : null
							]
						},
						flex: 1
					}
				]
			}, cfg);
		},
		
		createDescriptionFieldCfg: function(cfg) {
			return Ext.apply({
				xtype: 'textarea',
				bind: {
					value: '{record.description}',
					hidden: '{!foHasDescription}'
				},
				hidden: true,
				cls: this.cls + '-description',
				fieldLabel: this.mys.res('taskPreview.single.task.description.lbl'),
				labelAlign: 'top',
				readOnly: true
			}, cfg);
		},
		
		onCFDefsUpdate: function(nv, ov) {
			var cmp = this.lookupReference('tabcfields');
			if (cmp) cmp.setFieldsDefs(nv);
		}
	}
});
