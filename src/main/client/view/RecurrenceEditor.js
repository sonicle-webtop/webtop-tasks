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
Ext.define('Sonicle.webtop.tasks.view.RecurrenceEditor', {
	extend: 'WTA.sdk.UIView',
	requires: [
		'Sonicle.form.field.rr.Recurrence'
	],
	
	dockableConfig: {
		title: '{recurrenceEditor.tit}',
		width: 750,
		height: 430,
		modal: true
	},
	promptConfirm: false,
	
	viewModel: {
		data: {
			data: {
				rruleString: null
			}
		}
	},
	defaultButton: 'btnok',
	
	constructor: function(cfg) {
		var me = this;
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			foStart: WTF.foPropTwoWay('data', 'rruleString', function(v) {
					var split = Sonicle.form.field.rr.Recurrence.splitRRuleString(v);
					return split.start;
				}, function(v, path) {
					var ov = this.get(path),
						split = Sonicle.form.field.rr.Recurrence.splitRRuleString(ov);
					if (!Ext.isEmpty(split.rrule) && Ext.isDate(v)) {
						this.set(path, Sonicle.form.field.rr.Recurrence.joinRRuleString(split.rrule, v));
					}
			}, {noset: true})
		});
	},
	
	initComponent: function() {
		var me = this;
		
		me.setVMData({
			rruleString: me.rruleString
		});
		
		Ext.apply(me, {
			buttons: [
				{
					ui: '{secondary}',
					text: WT.res('act-cancel.lbl'),
					handler: me.onCancelClick,
					scope: me
				}, {
					ui: '{primary}',
					text: WT.res('act-save.lbl'),
					handler: me.onOkClick,
					scope: me
				}
			]
		});
		
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			paddingTop: true,
			paddingSides: true,
			defaults: {
				labelWidth: 80
			},
			items: [
				{
					xtype: 'datefield',
					bind: {
						value: '{foStart}'
					},
					startDay: WT.getStartDay(),
					format: WT.getShortDateFmt(),
					fieldLabel: WT.res('sorrfield.starts')
				}, {
					xtype: 'sorrfield',
					bind: {
						value: '{data.rruleString}'
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
						rawpasteinvalid: function() {
							WT.warn(me.mys.res('event.error.rrpaste'));
						}
					},
					fieldLabel: WT.res('sorrfield.repeats')
				}
			]
		});
	},
	
	privates: {
		onOkClick: function() {
			var me = this;
			me.fireEvent('viewok', me, me.getVMData());
			me.closeView(false);
		},

		onCancelClick: function() {
			var me = this;
			me.fireEvent('viewcancel', me);
			me.closeView(false);
		}
	}
});