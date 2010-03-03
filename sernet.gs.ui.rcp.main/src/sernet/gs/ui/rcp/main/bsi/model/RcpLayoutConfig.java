/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;

import sernet.gs.ui.rcp.main.CnAWorkspace;

/**
 * Layout configuration for {@link BSIMassnahmenModel} in a RCP environment
 * 
 * @author Daniel <dm[at]sernet[dot]de>
 */
public class RcpLayoutConfig implements ILayoutConfig {

	String cssFilePath;
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.ILayoutConfig#getCssFilePath()
	 */
	public String getCssFilePath() {
		if(cssFilePath==null) {
			cssFilePath = createCssFilePath();
		}
		return cssFilePath;
	}

	private String createCssFilePath() {
		return CnAWorkspace.getInstance().getWorkdir() + File.separator + "html" + File.separator + "screen.css";
	}

}
