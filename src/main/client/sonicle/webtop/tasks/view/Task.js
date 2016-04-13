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
Ext.define('Sonicle.webtop.task.view.Task', {
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.form.field.Palette',
		'Sonicle.form.Separator',
		'Sonicle.form.field.IconComboBox',
		'WT.ux.data.EmptyModel',
		'WT.ux.data.ValueModel',
		'WT.ux.field.SuggestCombo',
		'Sonicle.webtop.tasks.model.Task',
		'Sonicle.webtop.tasks.model.CategoryLkp',
		'Sonicle.webtop.tasks.store.Importance',
		'Sonicle.webtop.tasks.store.Status'
	],
	
	dockableConfig: {
		title: '{task.tit}',
		iconCls: 'wttasks-icon-tasks-xs',
		width: 650,
		height: 510
	},
	confirm: 'yn',
	autoToolbar: false,
	fieldTitle: 'title',
	modelName: 'Sonicle.webtop.tasks.model.Task',
	
	viewModel: {
		formulas: {
			startDate: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setStartDate(val);
				}
			},
			startTime: {
				bind: {bindTo: '{record.startDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setStartTime(val);
				}
			},
			endDate: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setEndDate(val);
				}
			},
			endTime: {
				bind: {bindTo: '{record.endDate}'},
				get: function(val) {
					return (val) ? Ext.Date.clone(val): null;
				},
				set: function(val) {
					this.get('record').setEndTime(val);
				}
			},
			allDay: WTF.checkboxBind('record', 'allDay'),
			isPrivate: WTF.checkboxBind('record', 'isPrivate'),
			busy: WTF.checkboxBind('record', 'busy'),
			rrType: WTF.checkboxGroupBind('record', 'rrType'),
			rrDaylyType: WTF.checkboxGroupBind('record', 'rrDaylyType'),
			rrWeeklyDay1: WTF.checkboxBind('record', 'rrWeeklyDay1'),
			rrWeeklyDay2: WTF.checkboxBind('record', 'rrWeeklyDay2'),
			rrWeeklyDay3: WTF.checkboxBind('record', 'rrWeeklyDay3'),
			rrWeeklyDay4: WTF.checkboxBind('record', 'rrWeeklyDay4'),
			rrWeeklyDay5: WTF.checkboxBind('record', 'rrWeeklyDay5'),
			rrWeeklyDay6: WTF.checkboxBind('record', 'rrWeeklyDay6'),
			rrWeeklyDay7: WTF.checkboxBind('record', 'rrWeeklyDay7'),
			rrEndsMode: WTF.checkboxGroupBind('record', 'rrEndsMode'),
			
			isRRNone: WTF.equalsFormula('record', 'rrType', '_'),
			isRRDayly: WTF.equalsFormula('record', 'rrType', 'D'),
			isRRWeekly: WTF.equalsFormula('record', 'rrType', 'W'),
			isRRMonthly: WTF.equalsFormula('record', 'rrType', 'M'),
			isRRYearly: WTF.equalsFormula('record', 'rrType', 'Y')
		}
	},
	
	initComponent: function() {
		var me = this,
				vm = me.getViewModel(),
				main, appointment, invitation, recurrence;
		
		Ext.apply(me, {
			tbar: [
				me.addAction('saveClose', {
					text: WT.res('act-saveClose.lbl'),
					iconCls: 'wt-icon-saveClose-xs',
					handler: function() {
						me.saveEvent();
					}
				}),
				'-',
				me.addAction('delete', {
					text: null,
					tooltip: WT.res('act-delete.lbl'),
					iconCls: 'wt-icon-delete-xs',
					handler: function() {
						me.deleteEvent();
					}
				}),
				me.addAction('restore', {
					text: null,
					tooltip: WT.res('act-restore.lbl'),
					iconCls: 'wt-icon-restore-xs',
					handler: function() {
						me.restoreEvent();
					},
					disabled: true
				}),
				'-',
				me.addAction('print', {
					text: null,
					tooltip: WT.res('act-print.lbl'),
					iconCls: 'wt-icon-print-xs',
					handler: function() {
						//TODO: aggiungere l'azione 'salva' permettendo così la stampa senza chiudere la form
						me.printEvent(me.getModel().getId());
					}
				}),
				'->',
				WTF.localCombo('id', 'desc', {
					reference: 'fldowner',
					bind: '{record._profileId}',
					store: {
						autoLoad: true,
						model: 'WT.model.Simple',
						proxy: WTF.proxy(me.mys.ID, 'LookupCalendarRoots', 'roots')
					},
					fieldLabel: me.mys.res('event.fld-owner.lbl'),
					labelWidth: 75,
					listeners: {
						select: function(s, rec) {
							me.updateCalendarFilters();
							//me.updateActivityParams(true);
							me.refreshActivities();
						}
					}
				})
			]
		});
		me.callParent(arguments);
		
		main = Ext.create({
			xtype: 'wtform',
			modelValidation: true,
			defaults: {
				labelWidth: 60
			},
			items: [{
				xtype: 'wtsuggestcombo',
				reference: 'fldtitle',
				bind: '{record.title}',
				sid: me.mys.ID,
				suggestionContext: 'eventcalendar',
				fieldLabel: me.mys.res('event.fld-title.lbl'),
				anchor: '100%',
				listeners: {
					enterkey: function() {
						me.getAction('saveClose').execute();
					}
				}
			}, {
				xtype: 'wtsuggestcombo',
				bind: '{record.location}',
				sid: me.mys.ID,
				suggestionContext: 'report_idcalendar', //TODO: verificare nome contesto
				fieldLabel: me.mys.res('event.fld-location.lbl'),
				anchor: '100%',
				plugins: [
					'soenterkeyplugin'
				],
				listeners: {
					enterkey: function() {
						me.getAction('saveClose').execute();
					}
				}
			}, {
				xtype: 'formseparator'
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: me.mys.res('event.fld-startDate.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'datefield',
					bind: {
						value: '{startDate}',
						disabled: '{record._isRecurring}'
					},
					startDay: WT.getStartDay(),
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{startTime}',
						disabled: '{fldallDay.checked}'
					},
					format: WT.getShortTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}, {
					xtype: 'button',
					iconCls: 'wtcal-icon-now-xs',
					tooltip: me.mys.res('event.btn-now.tip'),
					handler: function() {
						me.getModel().setStartTime(new Date());
					},
					bind: {
						disabled: '{fldallDay.checked}'
					}
				}, {
					xtype: 'checkbox',
					reference: 'fldallDay', // Publishes field into viewmodel...
					bind: '{allDay}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-allDay.lbl')
				}, {
					xtype: 'soiconcombo',
					reference: 'fldcalendar',
					bind: '{record.calendarId}',
					typeAhead: false,
					queryMode: 'local',
					forceSelection: true,
					selectOnFocus: true,
					store: {
						autoLoad: true,
						model: 'Sonicle.webtop.calendar.model.CalendarLkp',
						proxy: WTF.proxy(me.mys.ID, 'LookupCalendarFolders', 'folders')
					},
					valueField: 'calendarId',
					displayField: 'name',
					iconClsField: 'colorCls',
					labelWidth: 70,
					fieldLabel: me.mys.res('event.fld-calendar.lbl'),
					margin: 0,
					flex: 1,
					listeners: {
						select: function(s, rec) {
							me.onCalendarSelect(rec);
						}
					}
				}]
			}, {
				xtype: 'fieldcontainer',
				fieldLabel: me.mys.res('event.fld-endDate.lbl'),
				layout: 'hbox',
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'datefield',
					bind: {
						value: '{endDate}',
						disabled: '{record._isRecurring}'
					},
					startDay: WT.getStartDay(),
					margin: '0 5 0 0',
					width: 105
				}, {
					xtype: 'timefield',
					bind: {
						value: '{endTime}',
						disabled: '{fldallDay.checked}'
					},
					format: WT.getShortTimeFmt(),
					margin: '0 5 0 0',
					width: 80
				}, {
					xtype: 'button',
					iconCls: 'wtcal-icon-now-xs',
					tooltip: me.mys.res('event.btn-now.tip'),
					handler: function() {
						me.getModel().setEndTime(new Date());
					},
					bind: {
						disabled: '{fldallDay.checked}'
					}
				}, {
					xtype: 'combo',
					bind: '{record.timezone}',
					typeAhead: true,
					queryMode: 'local',
					forceSelection: true,
					selectOnFocus: true,
					store: Ext.create('WT.store.Timezone', {
						autoLoad: true
					}),
					valueField: 'id',
					displayField: 'desc',
					fieldLabel: me.mys.res('event.fld-timezone.lbl'),
					margin: 0,
					flex: 1,
					labelWidth: 75
				}]
			}]
		});
		appointment = Ext.create({
			xtype: 'wtform',
			title: me.mys.res('event.appointment.tit'),
			modelValidation: true,
			defaults: {
				labelWidth: 110
			},
			items: [{
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: me.mys.res('event.fld-description.lbl'),
				height: 100,
				anchor: '100%'
			}, {
				xtype: 'fieldcontainer',
				layout: 'hbox',
				fieldLabel: me.mys.res('event.fld-reminder.lbl'),
				defaults: {
					margin: '0 10 0 0'
				},
				items: [{
					xtype: 'combo',
					bind: '{record.reminder}',
					editable: false,
					store: Ext.create('Sonicle.webtop.calendar.store.Reminder', {
						autoLoad: true
					}),
					valueField: 'id',
					displayField: 'desc',
					triggers: {
						clear: WTF.clearTrigger()
					},
					emptyText: WT.res('word.none.male'),
					width: 150
				}, {
					xtype: 'checkbox',
					bind: '{isPrivate}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-private.lbl')
				}, {
					xtype: 'checkbox',
					bind: '{busy}',
					margin: '0 20 0 0',
					hideEmptyLabel: true,
					boxLabel: me.mys.res('event.fld-busy.lbl')
				}]
			}, {
				xtype: 'formseparator'
			}, WTF.remoteCombo('id', 'desc', {
				reference: 'fldactivity',
				bind: '{record.activityId}',
				autoLoadOnValue: true,
				store: {
					model: 'WT.model.ActivityLkp',
					proxy: WTF.proxy(WT.ID, 'LookupActivities', 'activities'),
					filters: [{
						filterFn: function(rec) {
							if(rec.get('readOnly')) {
								if(rec.getId() !== me.lref('fldactivity').getValue()) {
									return null;
								}
							}
							return rec;
						}
					}],
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-activity.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldcustomer',
				bind: '{record.customerId}',
				autoLoadOnValue: true,
				store: {
					model: 'WT.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupCustomers', 'customers')
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				listeners: {
					select: function() {
						var model = me.getModel();
						model.set('statisticId', null);
						model.set('causalId', null);
					},
					clear: function() {
						var model = me.getModel();
						model.set('statisticId', null);
						model.set('causalId', null);
					}
				},
				fieldLabel: me.mys.res('event.fld-customer.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldstatistic',
				bind: '{record.statisticId}',
				autoLoadOnValue: true,
				store: {
					model: 'WT.model.Simple',
					proxy: WTF.proxy(WT.ID, 'LookupStatisticCustomers', 'customers'),
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId'),
									parentCustomerId: me.getModel().get('customerId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-statistic.lbl'),
				anchor: '100%'
			}),
			WTF.remoteCombo('id', 'desc', {
				reference: 'fldcausal',
				bind: '{record.causalId}',
				autoLoadOnValue: true,
				store: {
					model: 'WT.model.CausalLkp',
					proxy: WTF.proxy(WT.ID, 'LookupCausals', 'causals'),
					listeners: {
						beforeload: {
							fn: function(s) {
								WTU.applyExtraParams(s, {
									profileId: me.getModel().get('_profileId'),
									customerId: me.getModel().get('customerId')
								});
							}
						}
					}
				},
				triggers: {
					clear: WTF.clearTrigger()
				},
				fieldLabel: me.mys.res('event.fld-causal.lbl'),
				anchor: '100%'
			})]
		});
		invitation = Ext.create({
			xtype: 'panel',
			reference: 'tabinvitation',
			referenceHolder: true,
			title: me.mys.res('event.invitation.tit'),
			layout: 'card',
			items: [
				{
					xtype: 'gridpanel',
					reference: 'gpattendees',
					itemId: 'attendees',
					bind: {
						store: '{record.attendees}'
					},
					columns: [{
						dataIndex: 'notify',
						xtype: 'checkcolumn',
						editor: {
							xtype: 'checkbox'
						},
						header: me.mys.res('event.gp-attendees.notify.lbl'),
						width: 70
					}, {
						dataIndex: 'recipient',
						editor: {
							xtype: 'textfield'
						},
						header: me.mys.res('event.gp-attendees.recipient.lbl'),
						flex: 1
					}, {
						dataIndex: 'recipientType',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRcptType'
						}),
						editor: Ext.create(WTF.localCombo('id', 'desc', {
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRcptType', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.recipientType.lbl'),
						width: 180
					}, {
						dataIndex: 'responseStatus',
						renderer: WTF.resColRenderer({
							id: me.mys.ID,
							key: 'store.attendeeRespStatus'
						}),
						editor: Ext.create(WTF.localCombo('id', 'desc', {
							store: Ext.create('Sonicle.webtop.calendar.store.AttendeeRespStatus', {
								autoLoad: true
							})
						})),
						header: me.mys.res('event.gp-attendees.responseStatus.lbl'),
						width: 100
					}],
					plugins: [
						Ext.create('Ext.grid.plugin.RowEditing', {
							pluginId: 'rowediting',
							clicksToMoveEditor: 2,
							saveBtnText: WT.res('act-confirm.lbl'),
							cancelBtnText: WT.res('act-cancel.lbl')
						})
					],
					tbar: [
						me.addAction('addAttendee', {
							text: WT.res('act-add.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-add-xs',
							handler: function() {
								me.addAttendee();
							}
						}),
						me.addAction('deleteAttendee', {
							text: WT.res('act-delete.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-delete-xs',
							handler: function() {
								var sm = me.lref('tabinvitation.gpattendees').getSelectionModel();
								me.deleteAttendee(sm.getSelection());
							},
							disabled: true
						}),
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-planning.lbl'),
							handler: function() {
								me.lref('tabinvitation').getLayout().setActiveItem('planning');
							}
						}
					],
					listeners: {
						selectionchange: function(s,recs) {
							me.getAction('deleteAttendee').setDisabled(!recs.length);
						}
					}
				}, {
					xtype: 'gridpanel',
					reference: 'gpplanning',
					itemId: 'planning',
					enableLocking: true,
					store: {
						model: 'WT.ux.data.EmptyModel',
						proxy: WTF.proxy(me.mys.ID, 'GetPlanning', 'data'),
						listeners: {
							metachange: function(s, meta) {
								if(meta.colsInfo) {
									// In order to draw a better view we need to nest grid columns (hours) 
									// belonging to same day date under the same master header.
									// So we need to create a nested structure identifying useful columns.
									
									var colsInfo = [];
									Ext.iterate(meta.colsInfo, function(col,i) {
										if(col.dataIndex === 'recipient') {
											col.header = me.mys.res('event.gp-planning.recipient.lbl');
											col.locked = true;
											col.width = 200;
											
											// Add this column as is... skip nesting
											colsInfo.push(col);
											
										} else {
											col.renderer = WTF.clsColRenderer({
												clsPrefix: 'wtcal-planning-',
												moreCls: (col.overlaps) ? 'wtcal-planning-overlaps' : null
											});
											col.lockable = false;
											col.sortable = false;
											col.hideable = false;
											col.menuDisabled = true;
											col.draggable = false;
											col.width = 55;
											
											// Nest this column under right day date
											if(colsInfo[colsInfo.length-1].date !== col.date) {
												colsInfo.push({
													date: col.date,
													text: col.date,
													columns: []
												});
											}
											colsInfo[colsInfo.length-1].columns.push(col);
										}
									});
									me.lref('tabinvitation.gpplanning').reconfigure(s, colsInfo);
								}
							}
						}
					},
					columns: [],
					tbar: [
						me.addAction('refreshPlanning', {
							text: WT.res('act-refresh.lbl'),
							tooltip: null,
							iconCls: 'wt-icon-refresh-xs',
							handler: function() {
								me.refreshPlanning();
							}
						}),
						'-',
						{
							xtype: 'tbitem',
							width: 15,
							height: 15,
							cls: 'wtcal-planning-legend-free'
						}, {
							xtype: 'tbtext',
							html: me.mys.res('event.gp-planning.free')
						}, {
							xtype: 'tbitem',
							width: 15,
							height: 15,
							cls: 'wtcal-planning-legend-busy'
						}, {
							xtype: 'tbtext',
							html: me.mys.res('event.gp-planning.busy')
						}, {
							xtype: 'tbitem',
							width: 15,
							height: 15,
							cls: 'wtcal-planning-legend-unknown'
						}, {
							xtype: 'tbtext',
							html: me.mys.res('event.gp-planning.unknown')
						},
						'->',
						{
							xtype: 'button',
							text: me.mys.res('event.btn-attendees.lbl'),
							handler: function() {
								me.lref('tabinvitation').getLayout().setActiveItem('attendees');
							}
						}
					],
					listeners: {
						activate: function() {
							me.refreshPlanning();
						}
					}
				}
			]
		});
		recurrence = Ext.create({
			xtype: 'panel',
			reference: 'tabrecurrence',
			itemId: 'recurrence',
			//layout: 'anchor',
			bodyPadding: 5,
			title: me.mys.res('event.recurrence.tit'),
			items: [{
				xtype: 'container',
				layout: 'column',
				items: [{
					xtype: 'form',
					layout: 'anchor',
					width: 120,
					items: [{
						xtype: 'radiogroup',
						bind: {
							value: '{rrType}'
						},
						layout: 'vbox',
						defaults: {
							name: 'rrType',
							margin: '0 0 15 0'
						},
						items: [{
							inputValue: '_',
							boxLabel: WT.res('rr.type.none')
						}, {
							inputValue: 'D',
							boxLabel: WT.res('rr.type.daily')
						}, {
							inputValue: 'W',
							boxLabel: WT.res('rr.type.weekly')
						}, {
							inputValue: 'M',
							boxLabel: WT.res('rr.type.monthly')
						}, {
							inputValue: 'Y',
							boxLabel: WT.res('rr.type.yearly')
						}]
					}]
				}, {
					xtype: 'form',
					layout: 'anchor',
					columnWidth: 1,
					items: [{
						xtype: 'displayfield' // none row
					}, {
						xtype: 'formseparator'
					}, {
						xtype: 'fieldcontainer', // dayly row
						bind: {
							disabled: '{!isRRDayly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'radiogroup',
							bind: {
								value: '{rrDaylyType}'
							},
							columns: [80, 60, 80, 150],
							items: [{
								name: 'rrDaylyType',
								inputValue: '1',
								boxLabel: WT.res('rr.type.daily.type.1')
							}, {
								xtype: 'combo',
								bind: '{record.rrDailyFreq}',
								typeAhead: true,
								queryMode: 'local',
								forceSelection: true,
								store: Ext.create('WT.store.RRDailyFreq'),
								valueField: 'id',
								displayField: 'id',
								width: 60,
								margin: '0 5 0 0',
								listeners: {
									change: function() {
										me.getModel().set('rrDaylyType', '1');
									}
								}
							}, {
								xtype: 'label',
								text: WT.res('rr.type.daily.freq')
							}, {
								name: 'rrDaylyType',
								inputValue: '2',
								boxLabel: WT.res('rr.type.daily.type.2')
							}]
						}]
					}, {
						xtype: 'formseparator'
					}, {
						xtype: 'fieldcontainer', // weekly row
						bind: {
							disabled: '{!isRRWeekly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'label',
							text: WT.res('rr.type.weekly.msg1'),
							width: 75
						}, {
							xtype: 'combo',
							bind: '{record.rrWeeklyFreq}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRWeeklyFreq'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.weekly.freq')
						}, {
							xtype: 'label',
							text: WT.res('rr.type.weekly.msg2')
						}, {
							xtype: 'checkboxgroup',
							items: [{
								bind: '{rrWeeklyDay1}',
								boxLabel: Sonicle.Date.getShortestDayName(1)
							}, {
								bind: '{rrWeeklyDay2}',
								boxLabel: Sonicle.Date.getShortestDayName(2)
							}, {
								bind: '{rrWeeklyDay3}',
								boxLabel: Sonicle.Date.getShortestDayName(3)
							}, {
								bind: '{rrWeeklyDay4}',
								boxLabel: Sonicle.Date.getShortestDayName(4)
							}, {
								bind: '{rrWeeklyDay5}',
								boxLabel: Sonicle.Date.getShortestDayName(5)
							}, {
								bind: '{rrWeeklyDay6}',
								boxLabel: Sonicle.Date.getShortestDayName(6)
							}, {
								bind: '{rrWeeklyDay7}',
								boxLabel: Sonicle.Date.getShortestDayName(0)
							}],
							width: 270
						}]
					}, {
						xtype: 'formseparator'
					}, {
						xtype: 'fieldcontainer', // monthly row
						bind: {
							disabled: '{!isRRMonthly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'label',
							text: WT.res('rr.type.monthly.msg1'),
							width: 75
						}, {
							xtype: 'combo',
							bind: '{record.rrMonthlyDay}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRMonthlyDay'),
							valueField: 'id',
							displayField: 'desc',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.monthly.msg2')
						}, {
							xtype: 'combo',
							bind: '{record.rrMonthlyFreq}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRMonthlyFreq'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.monthly.freq')
						}]
					}, {
						xtype: 'formseparator'
					}, {
						xtype: 'fieldcontainer', // yearly row
						bind: {
							disabled: '{!isRRYearly}'
						},
						layout: 'hbox',
						defaults: {
							margin: '0 10 0 0'
						},
						items: [{
							xtype: 'label',
							text: WT.res('rr.type.yearly.msg1'),
							width: 75
						}, {
							xtype: 'combo',
							bind: '{record.rrYearlyDay}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRYearlyDay'),
							valueField: 'id',
							displayField: 'id',
							width: 60
						}, {
							xtype: 'label',
							text: WT.res('rr.type.yearly.msg2')
						}, {
							xtype: 'combo',
							bind: '{record.rrYearlyFreq}',
							typeAhead: true,
							queryMode: 'local',
							forceSelection: true,
							store: Ext.create('WT.store.RRYearlyFreq'),
							valueField: 'id',
							displayField: 'desc',
							width: 120
						}]
					}]
				}]
			}, {
				xtype: 'form',
				layout: 'anchor',
				items: [{
					xtype: 'radiogroup',
					bind: {
						value: '{rrEndsMode}',
						disabled: '{isRRNone}'
					},
					columns: [70, 70, 50, 90, 50, 105],
					items: [{
						name: 'rrEndsMode',
						inputValue: 'never',
						boxLabel: WT.res('rr.end.never')
					}, {
						name: 'rrEndsMode',
						inputValue: 'repeat',
						boxLabel: WT.res('rr.end.repeat')
					}, {
						xtype: 'numberfield',
						bind: '{record.rrRepeatTimes}',
						minValue: 1,
						maxValue: 10,
						width: 50,
						margin: '0 5 0 0',
						listeners: {
							change: function() {
								me.getModel().set('rrEndsMode', 'repeat');
							}
						}
					}, {
						xtype: 'label',
						text: WT.res('rr.end.repeat.times')
					}, {
						name: 'rrEndsMode',
						inputValue: 'until',
						boxLabel: WT.res('rr.end.until')
					}, {
						xtype: 'datefield',
						bind: '{record.rrUntilDate}',
						startDay: WT.getStartDay(),
						width: 105,
						listeners: {
							select: function() {
								me.getModel().set('rrEndsMode', 'until');
							}
						}
					}],
					fieldLabel: WT.res('rr.end')
				}]
			}]
		});
		
		me.add(Ext.create({
			region: 'center',
			xtype: 'container',
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			items: [
				main,
				{
					xtype: 'tabpanel',
					flex: 1,
					activeTab: 0,
					items: [
						appointment,
						invitation,
						recurrence
					]
			}]
		}));
		
		//me.updateCalendarFilters();
		//me.updateActivityParams(false);
		//me.updateStatisticParams(false);
		//me.updateCausalParams(false);
		
		me.on('viewload', me.onViewLoad);
		vm.bind('{record.startDate}', me.onDatesChanged, me);
		vm.bind('{record.endDate}', me.onDatesChanged, me);
		vm.bind('{record.timezone}', me.onDatesChanged, me);
		vm.bind('{record.customerId}', me.onCustomerChanged, me);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this,
				model = me.getModel(),
				owner = me.lref('fldowner');
		
		// Overrides autogenerated string id by extjs...
		// It avoids type conversion problems server-side!
		//if(me.isMode(me.MODE_NEW)) me.getModel().set('eventId', -1, {dirty: false});
		me.updateCalendarFilters();
		
		// Gui updates...
		me.lref('tabinvitation').setDisabled(!model.get('_isSingle') && !model.get('_isBroken'));
		me.lref('tabrecurrence').setDisabled(!model.get('_isSingle') && !model.get('_isRecurring'));
		
		if(me.isMode(me.MODE_NEW)) {
			owner.setDisabled(false);
			me.getAction('delete').setDisabled(true);
			me.getAction('restore').setDisabled(true);
		} else if(me.isMode(me.MODE_EDIT)) {
			owner.setDisabled(true);
			me.getAction('restore').setDisabled(!(model.get('_isBroken') === true));
		}
		
		me.lref('fldtitle').focus(true);
	},
	
	refreshActivities: function() {
		this.lref('fldactivity').getStore().load();
	},
	
	refreshStatistics: function() {
		this.lref('fldstatistic').getStore().load();
	},
	
	refreshCausals: function() {
		this.lref('fldcausal').getStore().load();
	},
	
	updateCalendarFilters: function() {
		this.lref('fldcalendar').getStore().addFilter({
			property: '_profileId',
			value: this.getModel().get('_profileId')
		});
	},
	
	onCalendarSelect: function(cal) {
		var mo = this.getModel();
		mo.set({
			isPrivate: cal.get('isPrivate'),
			busy: cal.get('busy'),
			reminder: cal.get('reminder')
		});
	},
	
	onDatesChanged: function() {
		if(this.isPlanningActive()) this.refreshPlanning();
	},
	
	onCustomerChanged: function() {
		this.refreshStatistics();
		this.refreshCausals();
	},
	
	saveEvent: function() {
		var me = this,
				rec = me.getModel();
		
		if(rec.get('_isRecurring') === true) {
			me.mys.confirmForRecurrence(me.mys.res('event.recurring.confirm.save'), function(bid) {
				if(bid === 'ok') {
					var target = WT.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']),
						proxy = rec.getProxy();
					
					// Inject target param into proxy...
					proxy.setExtraParams(Ext.apply(proxy.getExtraParams(), {
						target: target
					}));
					me.saveView(true);
				}
			}, me);
		} else {
			me.saveView(true);
		}
	},
	
	deleteEvent: function() {
		var me = this,
				rec = me.getModel(),
				ajaxFn;
		
		ajaxFn = function(target, id) {
			me.wait();
			WT.ajaxReq(me.mys.ID, 'ManageEventsScheduler', {
				params: {
					crud: 'delete',
					target: target,
					id: id
				},
				callback: function(success, o) {
					me.unwait();
					if(success) {
						me.closeView(false);
						me.mys.refreshEvents();
					}
				}
			});
		};
		
		if(rec.get('_isRecurring') === true) {
			me.mys.confirmForRecurrence(me.mys.res('event.recurring.confirm.delete'), function(bid) {
				if(bid === 'ok') {
					var target = WT.Util.getCheckedRadioUsingDOM(['this', 'since', 'all']);
					ajaxFn(target, rec.get('id'));
				}
			}, me);
		} else {
			WT.confirm(me.mys.res('event.confirm.delete', rec.get('title')), function(bid) {
				if(bid === 'yes') {
					ajaxFn('this', rec.get('id'));
				}
			}, me);
		}
	},
	
	restoreEvent: function() {
		var me = this,
				rec = me.getModel();
		WT.confirm(me.mys.res('event.recurring.confirm.restore'), function(bid) {
			if(bid === 'yes') {
				me.wait();
				WT.ajaxReq(me.mys.ID, 'ManageEventsScheduler', {
					params: {
						crud: 'restore',
						id: rec.get('id')
					},
					callback: function(success, o) {
						me.unwait();
						if(success) {
							me.closeView(false);
							me.mys.refreshEvents();
						}
					}
				});
			}
		}, me);
	},
	
	addAttendee: function() {
		var me = this,
				grid = me.lref('tabinvitation.gpattendees'),
				sto = grid.getStore(),
				re = grid.getPlugin('rowediting'),
				cal = me.lref('fldcalendar').getSelection(),
				rec;
		/*
		rowEditing.cancelEdit();
		rec = Ext.create('Sonicle.webtop.calendar.model.EventAttendee', {
			recipientType: 'N',
			responseStatus: 'needsAction',
			notify: (cal) ? cal.get('invitation') : false
		});
		sto.insert(0, rec);
		rowEditing.startEdit(0, 0);
		*/
		re.cancelEdit();
		rec = sto.add(Ext.create('Sonicle.webtop.calendar.model.EventAttendee', {
			recipientType: 'N',
			responseStatus: 'needsAction',
			notify: (cal) ? cal.get('invitation') : false
		}))[0];
		re.startEdit(rec);
	},
	
	deleteAttendee: function(rec) {
		var me = this,
				grid = me.lref('tabinvitation.gpattendees'),
				sto = grid.getStore(),
				rowEditing = grid.getPlugin('rowediting');
		
		WT.confirm(WT.res('confirm.delete'), function(bid) {
			if(bid === 'yes') {
				rowEditing.cancelEdit();
				sto.remove(rec);
			}
		}, me);
	},
	
	isPlanningActive: function() {
		return (this.lref('tabinvitation').getLayout().getActiveItem().getItemId() === 'planning');
	},
	
	refreshPlanning: function() {
		var me = this,
				sto = me.lref('tabinvitation.gpplanning').getStore(),
				model = me.getModel(),
				serData = model.getData({serialize: true, associated: true});

		WT.Util.applyExtraParams(sto.getProxy(), {
			startDate: serData['startDate'],
			endDate: serData['endDate'],
			timezone: serData['timezone'],
			attendees: Ext.JSON.encode(serData['attendees'])
		});
		sto.load();
	},
	
	printEvent: function(eventKey) {
		var me = this;
		if(me.getModel().isDirty()) {
			WT.warn(WT.res('warn.print.notsaved'));
		} else {
			me.mys.printEventsDetail([eventKey]);
		}
	},
	
	printEvent22: function(record, calendar, activity, customer, statistic) {
		var me = this,
				//var event_id = rec.get('eventId'),
				eventBy = record.get('_profileId'),
				title = record.get('title') || '',
				location = record.get('location') || '',
				startdate = Ext.Date.format(record.get('startDate'), WT.getShortDateFmt()),
				starttime = Ext.Date.format(record.get('startDate'), WT.getShortTimeFmt()),
				enddate = Ext.Date.format(record.get('endDate'), WT.getShortDateFmt()),
				endtime = Ext.Date.format(record.get('endDate'), WT.getShortTimeFmt()),
				description = record.get('description') || '',
				private = (record.get('isPrivate')) ? WT.res('word.yes') : WT.res('word.no'),
				reminder = WTU.humanReadableDuration(record.get('reminder'));
		//var share_with = eventForm.share_with.getValue();
		//var activity_id = eventForm.activity_id.getRawValue();
		//var calendar_id = eventForm.calendar_id.getRawValue();
		//var customer_id = eventForm.customer_id.getRawValue();
		//var statistic_id = eventForm.statistic_id.getRawValue();
		var rrtype = record.get('rrType');
		var recurrence = "";
		if (rrtype === '_')
			recurrence = WT.res('rr.type.none');
		if (rrtype === 'D')
			recurrence = WT.res('rr.type.daily');
		if (rrtype === 'W')
			recurrence = WT.res('rr.type.weekly');
		if (rrtype === 'M')
			recurrence = WT.res('rr.type.monthly');
		if (rrtype === 'Y')
			recurrence = WT.res('rr.type.yearly');

		//var dayly1 = eventForm.dayly1.getValue();
		//var dayly_step = eventForm.dayly_step.getValue();
		//var dayly2 = eventForm.dayly2.getValue();
		//var weekly_step = eventForm.weekly_step.getValue();
		//var weekly1 = eventForm.weekly1.getValue();
		//var weekly2 = eventForm.weekly2.getValue();
		//var weekly3 = eventForm.weekly3.getValue();
		//var weekly4 = eventForm.weekly4.getValue();
		//var weekly5 = eventForm.weekly5.getValue();
		//var weekly6 = eventForm.weekly6.getValue();
		//var weekly7 = eventForm.weekly7.getValue();
		//var monthly_day = eventForm.monthly_day.getValue();
		//var monthly_month = eventForm.monthly_month.getValue();
		//var yearly_day = eventForm.yearly_day.getValue();
		//var yearly_month = eventForm.yearly_month.getValue();
		//var until_ldate = eventForm.until_yyyymmdd.getValue();
		//var until_yyyy = "";
		//var until_mm = "";
		//var until_dd = "";
		//if (until_ldate!=null && until_ldate!="") {
		//  until_yyyy = until_ldate.getFullYear();
		//  until_mm = ""+(until_ldate.getMonth()+1);
		//  if ((until_ldate.getMonth()+1)<10) until_mm="0"+until_mm;
		//  until_dd = ""+until_ldate.getDate();
		//  if (until_ldate.getDate()<10) until_dd="0"+until_dd;
		//}
		var htmlSrc = "<h3>" + eventBy + "</h3>";
		htmlSrc += "<hr width=100% size='4' color='black' align='center'>"
		htmlSrc += "<br>";
		htmlSrc += "<table width='100%' border='0'>"
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-title.lbl') + "</b></td>";
		htmlSrc += "<td>" + title + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-location.lbl') + "</b></td>";
		htmlSrc += "<td>" + location + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-calendar.lbl') + "</b></td>";
		htmlSrc += "<td>" + calendar + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr><td>&nbsp</td></tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-startDate.lbl') + "</b></td>";
		htmlSrc += "<td>" + startdate + " " + starttime + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-endDate.lbl') + "</b></td>";
		htmlSrc += "<td>" + enddate + " " + endtime + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr><td>&nbsp</td></tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.recurrence.tit') + ":</b></td>";
		htmlSrc += "<td>" + recurrence + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr><td>&nbsp</td></tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td>" + description + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr><td>&nbsp</td></tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-reminder.lbl') + "</b></td>";
		htmlSrc += "<td>" + reminder + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-private.lbl') + "</b></td>";
		htmlSrc += "<td>" + private + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr><td>&nbsp</td></tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-activity.lbl') + "</b></td>";
		htmlSrc += "<td>" + activity + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-customer.lbl') + "</b></td>";
		htmlSrc += "<td>" + customer + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "<tr>";
		htmlSrc += "<td width='30%'><b>" + me.mys.res('event.fld-statistic.lbl') + "</b></td>";
		htmlSrc += "<td>" + statistic + "</td>";
		htmlSrc += "</tr>";
		htmlSrc += "</table>";
		
		WT.print(htmlSrc);
	}
});
