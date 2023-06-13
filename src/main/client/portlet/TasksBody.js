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
Ext.define('Sonicle.webtop.tasks.portlet.TasksBody', {
	extend: 'WTA.sdk.PortletBody',
	requires: [
		'Sonicle.webtop.tasks.model.PletTasks'
	],
	
	isSearch: false,
	
	initComponent: function() {
		var me = this;
		Ext.apply(me, {
			bbar: {
				xtype: 'statusbar',
				reference: 'sbar',
				hidden: true
			}
		});
		me.callParent(arguments);
		me.add({
			region: 'center',
			xtype: 'gridpanel',
			reference: 'gp',
			store: {
				autoLoad: true,
				model: 'Sonicle.webtop.tasks.model.PletTasks',
				proxy: WTF.apiProxy(me.mys.ID, 'PortletTasks', 'data', {
					extraParams: {
						query: null
					}
				}),
				listeners: {
					load: function(s) {
						if (me.isSearch) me.lref('sbar').setStatus(me.buildSearchStatus(s.getCount()));
					}
				}
			},
			viewConfig: {
				deferEmptyText: false,
				emptyText: me.mys.res('portlet.tasks.gp.emp'),
				getRowClass: function (rec, indx) {
					if (rec.isOverdue()) return 'wt-theme-text-error';
					return '';
				}
			},
			columns: [
				{
					xtype: 'socolorcolumn',
					dataIndex: 'categoryName',
					colorField: 'categoryColor',
					width: 30
				}, {
					dataIndex: 'subject',
					flex: 1
				}
			],
			features: [
				{
					ftype: 'rowbody',
					getAdditionalData: function(data, idx, rec, orig) {
						var body = Ext.String.ellipsis(rec.get('body'), 100),
							date = Ext.Date.format(rec.get('due'), WT.getShortDateFmt()),
							html = Ext.String.format(me.mys.res('portlet.tasks.gp.dueon'), date);
							if (!Ext.isEmpty(body)) html += (' <span style="color:grey;">' + Ext.String.htmlEncode(body) + '</span>');
						return {
							rowBody: html
						};
					}
				}
			],
			listeners: {
				rowdblclick: function(s, rec) {
					me.mys.openTaskUI(rec.getItemsRights().UPDATE, rec.getId());
				}
			}
		});
	},
	
	refresh: function() {
		if (!this.isSearch) {
			this.lref('gp').getStore().load();
		}
	},
	
	recents: function() {
		var me = this;
		me.isSearch = false;
		me.lref('sbar').hide();
		WTU.loadWithExtraParams(me.lref('gp').getStore(), {query: null});
	},
	
	search: function(s) {
		var me = this;
		me.isSearch = true;
		me.lref('sbar').setStatus(me.buildSearchStatus(-1));
		me.lref('sbar').show();
		WTU.loadWithExtraParams(me.lref('gp').getStore(), {query: s});
	},
	
	buildSearchStatus: function(count) {
		return Ext.String.format(this.mys.res('portlet.tasks.sbar.count'), (count > -1) ? count : '?');
	}
});
