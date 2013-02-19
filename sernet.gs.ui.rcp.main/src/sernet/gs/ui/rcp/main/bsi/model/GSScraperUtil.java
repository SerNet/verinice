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

import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.hui.common.VeriniceContext;

public final class GSScraperUtil {
	
	private static final Logger LOG = Logger.getLogger(GSScraperUtil.class);
	
	private BSIMassnahmenModel model;
	
	private static boolean isInitialized = false;

	private static IProgress nullMonitor = new IProgress() {
        public void beginTask(String name, int totalWork) {
        }

        public void done() {
        }

        public void setTaskName(String string) {
        }

        public void subTask(String string) {
        }

        public void worked(int work) {
        }
        
    };
	
	private GSScraperUtil() {
		LOG.debug(
		"Initializing GS catalogues service ...");
	}

	public static GSScraperUtil getInstance() {
		return (GSScraperUtil) VeriniceContext.get(VeriniceContext.GS_SCRAPER_UTIL);
	}
	
	public static GSScraperUtil getInstanceWeb() {
	    if(!isInitialized) {
            init();
        }
		GSScraperUtil instance = (GSScraperUtil) VeriniceContext.get(VeriniceContext.GS_SCRAPER_UTIL);
		instance.getModel().setLayoutConfig(new WebLayoutConfig());
		return instance;
	}
	
	private static void init() {
	    GSScraperUtil gsScraperUtil = GSScraperUtil.getInstance();
        // initialize grundschutz scraper:
        try {
            gsScraperUtil.getModel().loadBausteine(nullMonitor);
        } catch (Exception e) {
            LOG.error("Fehler beim Laden der Grundschutzkataloge: " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("stacktrace: " + e);
            }
        }
	}
	
	public BSIMassnahmenModel getModel()
	{
		return model;
	}

	public void setModel(BSIMassnahmenModel model) {
		this.model = model;
	}

}
