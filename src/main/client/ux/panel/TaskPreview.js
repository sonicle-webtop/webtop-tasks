/* 
 * Copyright (C) 2021 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2021 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.tasks.ux.panel.TaskPreview', {
	extend: 'WTA.ux.panel.Panel',
	requires: [
		'Sonicle.form.field.Display',
		'Sonicle.form.field.ColorDisplay',
		'Sonicle.form.field.TagDisplay',
		'WTA.util.FoldersTree2',
		'WTA.ux.grid.TileList',
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
	referenceHolder: true,
	bodyPadding: 10,
	
	viewModel: {},
	modelName: 'Sonicle.webtop.tasks.model.TaskPreview',
	mys: null,
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		var durRes = function(sym) { return WT.res('word.dur.'+sym); },
			durSym = [durRes('y'), durRes('d'), durRes('h'), durRes('m'), durRes('s')];
		
		WTU.applyFormulas(me.getVM(), {
			foIsEditable: WTF.foGetFn('record', '_itPerms', function(val) {
				return WTA.util.FoldersTree2.toRightsObj(val).UPDATE;
			}),
			foSubject: WTF.foGetFn('record', 'subject', function(val) {
				var TI = Sonicle.webtop.tasks.store.TaskImportance,
					tipAttrs = Sonicle.Utils.generateTooltipAttrs,
					impo = this.get('record.importance'),
					s = '';
				if (impo !== 1) s += '<i class="'+TI.buildIcon(impo)+'" aria-hidden="true" '+tipAttrs(TI.buildLabel(impo))+' style="margin-right:5px;font-size:initial"></i>';
				if (this.get('record.isPrivate') === true) s += '<i class="fas fa-lock" aria-hidden="true" '+tipAttrs(me.mys.res('task.fld-private.lbl'))+' style="margin-right:5px;font-size:initial"></i>';
				return s + Ext.String.htmlEncode(val);
			}),
			foCategoryName: WTF.foGetFn('record', 'categoryName', function(val) {
				var dn = this.get('record._orDN'),
					s = '';
				if (!Ext.isEmpty(dn)) {
					s += '&nbsp;<span class="wt-theme-text-lighter2">('+dn+')</span>';
				}
				return val + s;
			}),
			foStatus: WTF.foGetFn('record', 'status', function(val) {
				var complOn = this.get('record.completedOn'),
					s = '';
				if ('CO' === val && Ext.isDate(complOn)) {
					s += '&nbsp;<span class="wt-theme-text-lighter2">('+Ext.Date.format(complOn, WT.getShortDateTimeFmt())+')</span>';
				} else if (Sonicle.String.isIn(val, ['IP','CA','WA'])) {
					s += '&nbsp;<span class="wt-theme-text-lighter2">('+this.get('record.progress')+'%)</span>';
				}
				return me.mys.res('store.taskStatus.'+val) + s;
			}),
			foStart: WTF.foGetFn('record', 'start', function(val) {
				return Ext.isDate(val) ? Ext.Date.format(val, 'D ' + WT.getLongDateFmt() + ' ' + WT.getShortTimeFmt()) : null;
			}),
			foDue: WTF.foGetFn('record', 'due', function(val) {
				var s = '', ret;
				if (Ext.isDate(val)) {
					if ('CO' !== this.get('record.status')) {
						var SoD = Sonicle.Date,
							diff = SoD.diffDays(val, new Date()),
							hrd;
						if (diff > 0) {
							hrd = SoD.humanReadableDuration(Math.abs(diff * 86400), {hours: false, minutes: false, seconds: false}, durSym);
							if (!Ext.isEmpty(hrd)) {
								s += '&nbsp;<span class="wt-theme-text-lighter2">('+me.mys.res('taskPreview.single.task.due.late', '+'+hrd)+')</span>';
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
			foHasTags: WTF.foIsEmpty('record', 'tags', true),
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
			Ext.apply(me.createEmptyItemCfg(), {
				itemId: 'empty'
			}),
			Ext.apply(me.createSingleItemCfg(), {
				itemId: 'single'
			}),
			Ext.apply(me.createMultiItemCfg(), {
				itemId: 'multi'
			})
		]);
		me.setActiveItem('empty');
		me.getViewModel().bind('{record._cfdefs}', me.onCFDefsUpdate, me);
	},
	
	createEmptyItemCfg: function() {
		var me = this;
		return {
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
					cls: 'wt-theme-text-header1',
					style: 'font-size:1.2em'
				}, {
					xtype: 'label',
					text: me.mys.res('taskPreview.empty.txt'),
					cls: 'wt-theme-text-subtitle',
					style: 'font-size:0.9em'
				}
			]
		};
	},
	
	createSingleItemCfg: function() {
		var me = this;
		return {
			xtype: 'container',
			layout: 'border',
			items: [
				{
					xtype: 'wtfieldspanel',
					region: 'north',
					items: [
						{
							xtype: 'label',
							bind: '{foSubject}',
							cls: 'wt-text-ellipsis',
							style: {
								fontSize: '2em'
							}
						}, {
							xtype: 'fieldcontainer',
							layout: 'hbox',
							items: [
								{
									xtype: 'socolordisplayfield',
									bind: '{record.categoryColor}'
								}, {
									xtype: 'displayfield',
									bind: '{foCategoryName}'
								}
							]
						}, {
							xtype: 'container',
							layout: 'hbox',
							defaults: {
								margin: '0 2 0 0',
								xtype: 'button',
								ui: 'default-toolbar'
							},
							items: [
								{
									bind: {
										disabled: '{foIsCompleted}'
									},
									tooltip: me.mys.res('taskPreview.act-setComplete.tip'),
									iconCls: 'wttasks-icon-setCompleted',
									handler: function() {
										me.fireEvent('setcompleted', me, me.getVM().get('record.id'));
									}									
								}, {
									tooltip: me.mys.res('taskPreview.act-sendByEmail.tip'),
									iconCls: 'wttasks-icon-sendByEmail',
									handler: function() {
										me.fireEvent('sendbyemail', me, me.getVM().get('record.id'));
									}	
								}, {
									bind: {
										disabled: '{!foHasEmail}',
										tooltip: '{foWriteMessageTip}'
									},
									tooltip: me.mys.res('taskPreview.act-writeMessage.tip'),
									iconCls: 'wttasks-icon-writeMessage',
									handler: function() {
										var vm = me.getVM();
										me.fireEvent('writeemail', me, [vm.get('record.contactEmail')], vm.get('record.id'));
									}
								}
							],
							// Bottom margin to space tags
							margin: '0 0 5 0'
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
						}, {
							xtype: 'so-displayfield',
							bind: '{foStatus}',
							plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}],
							labelWidth: 25,
							iconCls: 'fas fa-tachometer-alt',
							tooltip: me.mys.res('taskPreview.single.task.status.lbl')
						}, {
							xtype: 'so-displayfield',
							bind: {
								value: '{foStart}',
								hidden: '{!foHasStart}'
							},
							plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}],
							labelWidth: 25,
							iconCls: 'far fa-calendar',
							tooltip: me.mys.res('taskPreview.single.task.start.lbl')
						}, {
							xtype: 'so-displayfield',
							bind: {
								value: '{foDue}',
								hidden: '{!foHasDue}'
							},
							plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}],
							labelWidth: 25,
							iconCls: 'far fa-calendar-times',
							tooltip: me.mys.res('taskPreview.single.task.due.lbl')
						}, {
							xtype: 'so-displayfield',
							bind: {
								value: '{record.location}',
								hidden: '{!foHasLocation}'
							},
							plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}],
							labelWidth: 25,
							iconCls: 'fas fa-map-marker-alt',
							tooltip: me.mys.res('taskPreview.single.task.location.lbl')
						}, {
							xtype: 'so-displayfield',
							bind: {
								value: '{record.docRef}',
								hidden: '{!foHasDocRef}'
							},
							plugins: [{ptype: 'sofieldtooltip', tooltipTarget: 'label'}],
							labelWidth: 25,
							iconCls: 'far fa-file-alt',
							tooltip: me.mys.res('taskPreview.single.task.docRef.lbl')
						}
					]
				}, {
					xtype: 'tabpanel',
					region: 'center',
					border: false,
					items: [
						{
							xtype: 'wtpanel',
							title: me.mys.res('taskPreview.single.task.tit'),
							layout: 'anchor',
							defaults: {
								labelAlign: 'top',
								readOnly: true,
								anchor: '100%'
							},
							items: [
								{
									xtype: 'textarea',
									bind: '{record.description}',
									fieldLabel: me.mys.res('taskPreview.single.task.description.lbl'),
									anchor: '100% 100%'
								}
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
							serviceId: me.mys.ID
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
									me.fireEvent('opentaskaudit', me, vm.get('record.taskId'));
								}
							} : null, {
								xtype: 'button',
								ui: 'default-toolbar',
								bind: {
									disabled: '{!foIsEditable}'
								},
								text: me.mys.res('taskPreview.act-edit.lbl'),
								handler: function() {
									var vm = me.getVM();
									me.fireEvent('edittask', me, vm.get('record.id'));
								}
							}
						]
					}
				}
			]
		};
	},
	
	createMultiItemCfg: function() {
		var me = this;
		return {
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
					cls: 'wt-theme-text-header1',
					style: 'font-size:1.2em'
				}, {
					xtype: 'toolbar',
					vertical: true,
					margin: '20 0 0 0',
					items: [
						{
							text: me.mys.res('taskPreview.act-setComplete.tip'),
							iconCls: 'wttasks-icon-setCompleted',
							handler: function() {
								me.fireEvent('setcompleted', me, Sonicle.Data.collectValues(me.getVM().get('records')));
							}
						}, {
							text: me.mys.res('taskPreview.act-sendByEmail.tip'),
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
		};
	},
	
	setSelection: function(records) {
		var me = this,
				card = me;
		me.getViewModel().set('records', records);
		if (records && records.length === 1) {
			/*
			card.remove('single');
			card.add(Ext.apply(me.createSingleItemCfg(), {
				itemId: 'single'
			}));
			*/
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
		onCFDefsUpdate: function(nv, ov) {
			var cmp = this.lookupReference('tabcfields');
			if (cmp) cmp.setFieldsDefs(nv);
		}
	}
});
