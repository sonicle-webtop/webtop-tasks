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
Ext.define('Sonicle.webtop.tasks.view.Category', {
	extend: 'WTA.sdk.ModelView',
	requires: [
		'Sonicle.VMUtils',
		'Sonicle.form.field.Palette',
		'Sonicle.form.RadioGroup',
		'Sonicle.webtop.tasks.store.Sync',
		'Sonicle.webtop.tasks.store.TaskReminder'
	],
	
	dockableConfig: {
		title: '{category.tit}',
		iconCls: 'wttasks-icon-category',
		width: 360,
		height: 340
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.tasks.model.Category',
	
	constructor: function(cfg) {
		var me = this;
		me.visibilityName = Ext.id(null, 'visibility-');
		me.callParent([cfg]);
		
		Sonicle.VMUtils.applyFormulas(me.getVM(), {
			visibility: WTF.radioGroupBind('record', 'isPrivate', me.visibilityName)
		});
	},
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
        	bbar: {
        		xtype: 'statusbar',
        		items: [
					WTF.recordInfoButton({
						getTooltip: function() {
							return Ext.String.format('ID: {0}', Sonicle.String.coalesce(me.getModel().get('categoryId'), '?'));
						}
					})
        		]
        	}
        });
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			paddingTop: true,
			paddingSides: true,
			scrollable: true,
			modelValidation: true,
			defaults: {
				labelAlign: 'top',
				labelSeparator: ''
			},
			items: [
				{
					xtype: 'sofieldhgroup',
					items: [
						{
							xtype: 'textfield',
							reference: 'fldname',
							bind: '{record.name}',
							fieldLabel: me.res('category.fld-name.lbl'),
							flex: 1
						}, {
							xtype: 'sohspacer'
						}, {
							xtype: 'sopalettefield',
							bind: '{record.color}',
							colors: WT.getColorPalette('default'),
							fieldLabel: me.res('category.fld-color.lbl'),
							tilesPerRow: 11,
							width: 64
						}
					]
				}, {
					xtype: 'textareafield',
					bind: '{record.description}',
					fieldLabel: me.res('category.fld-description.lbl'),
					anchor: '100%'
				},
				WTF.lookupCombo('id', 'desc', {
					bind: '{record.sync}',
					store: {
						xclass: 'Sonicle.webtop.tasks.store.Sync',
						autoLoad: true
					},
					fieldLabel: me.res('category.fld-sync.lbl'),
					width: 250
				}),
				{
					xtype: 'soformseparator',
					title: me.res('category.defaults.tit')
				},
				{
					xtype: 'radiogroup',
					bind: '{visibility}',
					layout: 'hbox',
					defaults: {
						name: me.visibilityName,
						margin: '0 20 0 0'
					},
					items: [
						{
							inputValue: false,
							boxLabel: me.res('category.fld-visibility.default')
						}, {
							inputValue: true,
							boxLabel: me.res('category.fld-visibility.private')
						}
					],
					fieldLabel: me.res('category.fld-visibility.lbl')
				},
				WTF.lookupCombo('id', 'desc', {
					bind: '{record.reminder}',
					store: {
						xclass: 'Sonicle.webtop.tasks.store.TaskReminder',
						autoLoad: true
					},
					triggers: {
						clear: WTF.clearTrigger()
					},
					emptyText: WT.res('word.none.male'),
					fieldLabel: me.res('category.fld-reminder.lbl'),
					width: 250
				})
			]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if (!success) return;
		var me = this;
		me.lref('fldname').focus(true);
	}
});
