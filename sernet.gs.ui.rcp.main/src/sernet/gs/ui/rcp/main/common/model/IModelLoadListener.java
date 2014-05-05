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
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;

public interface IModelLoadListener {
	
    /**
     * Method is called when an BSIModel is loaded or created
     * 
     * @param model a new loaded or created {@link BSIModel}
     */
    void loaded(BSIModel model);
	
	
	/**
	 * Method is called when an ISO27KModel is loaded or created
	 * 
	 * @param model a new loaded or created {@link ISO27KModel}
	 */
	void loaded(ISO27KModel model);

	
	/**
	 * Method is called when an BSIModel is closed
	 * 
	 * @param model a BSIModel before it is closed
	 */
	void closed(BSIModel model);
}
