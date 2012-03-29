/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster@tarent.de> - simplified for usage in Spring
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;

public class GSScraperUtil {
	
	private static final Logger log = Logger.getLogger(GSScraperUtil.class);
	
	private BSIMassnahmenModel model;

	private GSScraperUtil() {
		log.debug(
		"Initializing GS catalogues service ...");
	}

	public static GSScraperUtil getInstance() {
		return (GSScraperUtil) VeriniceContext.get(VeriniceContext.GS_SCRAPER_UTIL);
	}
	
	public static GSScraperUtil getInstanceWeb() {
		GSScraperUtil instance = (GSScraperUtil) VeriniceContext.get(VeriniceContext.GS_SCRAPER_UTIL);
		instance.getModel().setLayoutConfig(new WebLayoutConfig());
		return instance;
	}
	
	public BSIMassnahmenModel getModel()
	{
		return model;
	}

	public void setModel(BSIMassnahmenModel model) {
		this.model = model;
	}

}
