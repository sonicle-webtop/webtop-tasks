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
package com.sonicle.webtop.tasks.rpt;

import com.sonicle.commons.LangUtils;
import com.sonicle.webtop.core.io.output.AbstractReport;
import com.sonicle.webtop.core.io.output.ReportConfig;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author malbinola
 */
public class RptTaskList extends AbstractReport {
	
	public RptTaskList(ReportConfig config) {
		super(config);
		this.name = "tasklist";
		this.resourceBundleName = this.name;
	}
	
	@Override
	protected void fillBuiltInParams() {
		super.fillBuiltInParams();
		
		String pkgName = LangUtils.getClassPackageName(this.getClass());
		String basepath = LangUtils.packageToPath(pkgName);
		ClassLoader cl = LangUtils.findClassLoader(this.getClass());
		
		addSvgStreamAsParam("SVG_TASKTYPE_DEFAULT", cl, basepath + "/tasktype-default_16.svg", StandardCharsets.UTF_8);
		addSvgStreamAsParam("SVG_TASKTYPE_SERIESMASTER", cl, basepath + "/tasktype-series-master_16.svg", StandardCharsets.UTF_8);
		addSvgStreamAsParam("SVG_TASKTYPE_SERIESITEM", cl, basepath + "/tasktype-series-item_16.svg", StandardCharsets.UTF_8);
		addSvgStreamAsParam("SVG_TASKTYPE_SERIESBROKEN", cl, basepath + "/tasktype-series-broken_16.svg", StandardCharsets.UTF_8);
		
		// Example on how to pass an SVG, with color properly changed, as a JRRenderable object.
		// In jrxml expression it will be used in this way: $P{REPORT_PARAMETERS_MAP}.get("SVG_TASKIMPORTANCE_" + $F{importance})
		/*
		try {
			String exclamationSvg = readStringResource(cl, basepath + "/exclamation-solid.svg", StandardCharsets.UTF_8);
			params.put("SVG_TASKIMPORTANCE_0", (JRRenderable)BatikRenderer.getInstanceFromText(exclamationSvg.replace("currentColor", "#B4B4B4")));
			params.put("SVG_TASKIMPORTANCE_2", (JRRenderable)BatikRenderer.getInstanceFromText(exclamationSvg.replace("currentColor", "#CC4B31")));
			
		} catch (IOException | JRException ex) {
			throw new WTRuntimeException("Unable to read stream [{}]", "exclamation-solid.svg", ex);
		}
		*/
	}
}
