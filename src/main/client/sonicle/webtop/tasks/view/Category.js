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
	extend: 'WT.sdk.ModelView',
	requires: [
		'Sonicle.form.field.Palette',
		'Sonicle.form.RadioGroup'
	],
	
	dockableConfig: {
		title: '{category.tit}',
		iconCls: 'wttasks-icon-category-xs',
		width: 360,
		height: 260
	},
	fieldTitle: 'name',
	modelName: 'Sonicle.webtop.tasks.model.Category',
	
	constructor: function(config) {
		var me = this;
		
		me.config.viewModel = {
			formulas: {
				isPrivate: WTF.checkboxBind('record', 'isPrivate'),
				isDefault: WTF.checkboxBind('record', 'isDefault'),
				sync: WTF.checkboxBind('record', 'sync')
			}
		};
		me.callParent([config]);
	},
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtform',
			modelValidation: true,
			defaults: {
				labelWidth: 100
			},
			items: [{
				xtype: 'textfield',
				reference: 'fldname',
				bind: '{record.name}',
				fieldLabel: me.mys.res('category.fld-name.lbl'),
				anchor: '100%'
			}, {
				xtype: 'textareafield',
				bind: '{record.description}',
				fieldLabel: me.mys.res('category.fld-description.lbl'),
				anchor: '100%'
			}, {
				xtype: 'sopalettefield',
				bind: '{record.color}',
				colors: WT.getColorPalette(),
				fieldLabel: me.mys.res('category.fld-color.lbl'),
				width: 200
			}, {
				xtype: 'checkbox',
				bind: '{isPrivate}',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('category.fld-private.lbl')
			}, {
				xtype: 'checkbox',
				bind: '{isDefault}',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('category.fld-default.lbl')
			}, {
				xtype: 'checkbox',
				bind: '{sync}',
				hideEmptyLabel: false,
				boxLabel: me.mys.res('category.fld-sync.lbl')
			}]
		});
		me.on('viewload', me.onViewLoad);
	},
	
	onViewLoad: function(s, success) {
		if(!success) return;
		var me = this;
		me.lref('fldname').focus(true);
	}
});
