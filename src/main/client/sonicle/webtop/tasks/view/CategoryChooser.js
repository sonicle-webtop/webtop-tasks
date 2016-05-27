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
Ext.define('Sonicle.webtop.tasks.view.CategoryChooser', {
	extend: 'WT.sdk.DockableView',
	requires: [
		'Sonicle.form.field.ColorComboBox',
		'Sonicle.webtop.tasks.model.CategoryLkp'
	],
	
	dockableConfig: {
		width: 300,
		height: 150,
		modal: true,
		minimizable: false,
		maximizable: false
	},
	promptConfirm: false,
	
	viewModel: {
		data: {
			ownerId: null,
			categoryId: null
		}
	},
	
	/**
	 * @cfg {String} ownerId
	 * Initial ownerId value
	*/
	
	/**
	 * @cfg {String} categoryId
	 * Initial categoryId value
	*/
	
	initComponent: function() {
		var me = this,
				ic = me.getInitialConfig();
		
		if(!Ext.isEmpty(ic.ownerId)) me.getVM().set('ownerId', ic.ownerId);
		if(!Ext.isEmpty(ic.categoryId)) me.getVM().set('categoryId', ic.categoryId);
		
		Ext.apply(me, {
			buttons: [{
				text: WT.res('act-ok.lbl'),
				handler: me.onOkClick,
				scope: me
			}, {
				text: WT.res('act-cancel.lbl'),
				handler: me.onCancelClick,
				scope: me
			}]
		});
		me.callParent(arguments);
		
		me.add({
			region: 'center',
			xtype: 'wtfieldspanel',
			modelValidation: true,
			defaults: {
				labelWidth: 100
			},
			items: [
				WTF.localCombo('id', 'desc', {
					reference: 'fldowner',
					bind: '{ownerId}',
					store: {
						autoLoad: true,
						model: 'WT.ux.data.SimpleModel',
						proxy: WTF.proxy(me.mys.ID, 'LookupCategoryRoots', 'roots')
					},
					fieldLabel: me.mys.res('categoryChooser.fld-owner.lbl'),
					anchor: '100%',
					listeners: {
						change: function(s, nv) {
							me.updateCategoryFilters(nv);
						}
					},
					allowBlank: false
				}),
				WTF.lookupCombo('categoryId', 'name', {
					xtype: 'socolorcombo',
					reference: 'fldcategory',
					bind: '{categoryId}',
					store: {
						autoLoad: true,
						model: me.mys.preNs('model.CategoryLkp'),
						proxy: WTF.proxy(me.mys.ID, 'LookupCategoryFolders', 'folders')
					},
					colorField: 'color',
					fieldLabel: me.mys.res('categoryChooser.fld-category.lbl'),
					anchor: '100%',
					allowBlank: false
			})]
		});
	},
	
	onOkClick: function() {
		var me = this;
		if(!me.lref('fldowner').isValid() || !me.lref('fldcategory').isValid()) return;
		me.fireEvent('viewok', me);
		me.closeView(false);
	},
	
	onCancelClick: function() {
		this.closeView(false);
	},
	
	updateCategoryFilters: function(owner) {
		var me = this,
				fld = me.lref('fldcategory'),
				sto = fld.getStore();
		
		sto.clearFilter();
		sto.addFilter({
			property: '_profileId',
			value: owner
		});
	}
});
