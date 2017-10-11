/* 
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
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.tasks.portlet.MyTasksBody', {
	extend: 'WTA.sdk.PortletBody',
	requires: [
		'Sonicle.webtop.tasks.model.PletMyTasks'
	],
	
	initComponent: function() {
		var me = this;
		me.callParent(arguments);
		me.add({
			region: 'center',
			xtype: 'gridpanel',
			reference: 'gp',
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.tasks.model.PletMyTasks',
				proxy: WTF.apiProxy(me.mys.ID, 'PletMyTasks', 'data', {
					extraParams: {
						query: null
					}
				})
			},
			viewConfig: {
				getRowClass: function (rec, indx) {
					if (Ext.isDate(rec.get('dueDate')) && Sonicle.Date.compare(rec.get('dueDate'), new Date(), false)>0)
						return 'wttasks-row-expired';
					return '';
				}
			},
			columns: [{
				xtype: 'socolorcolumn',
				dataIndex: 'categoryName',
				colorField: 'categoryColor',
				width: 30
			}, {
				dataIndex: 'subject',
				flex: 1
			}],
			features: [{
				ftype: 'rowbody',
				getAdditionalData: function(data, idx, rec, orig) {
					var desc = Ext.String.ellipsis(rec.get('description'), 100),
						date = Ext.Date.format(rec.get('dueDate'), WT.getShortDateFmt()),
						html = Ext.String.format(me.mys.res('portlet.mytasks.dueon'), date);
						if (!Ext.isEmpty(desc)) html += (' <span style="color:grey;">' + Ext.String.htmlEncode(desc) + '</span>');
					return {
						rowBody: html
					};
				}
			}],
			listeners: {
				rowdblclick: function(s, rec) {
					var er = me.mys.toRightsObj(rec.get('_erights'));
					me.mys.openTaskUI(er.UPDATE, rec.get('taskId'));
				}
			}
		});
	},
	
	refresh: function() {
		this.lref('gp').getStore().load();
	},
	
	search: function(s) {
		WTU.loadWithExtraParams(this.lref('gp').getStore(), {query: s});
	}
});
