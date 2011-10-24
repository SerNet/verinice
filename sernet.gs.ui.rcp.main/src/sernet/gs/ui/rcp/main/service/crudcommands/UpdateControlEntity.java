/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.verinice.interfaces.bpm.IProcessCommand;
import sernet.verinice.interfaces.bpm.IProcessService;
import sernet.verinice.model.iso27k.Control;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UpdateControlEntity extends UpdateElementEntity<Control> implements IProcessCommand {

    private transient IProcessService processService;
    
    /**
     * @param element
     * @param fireUpdates
     * @param stationId
     */
    public UpdateControlEntity(Control element, boolean fireUpdates, String stationId) {
        super(element, fireUpdates, stationId);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessCommand#getProcessService()
     */
    @Override
    public IProcessService getProcessService() {
        return processService;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessCommand#setProcessService(sernet.verinice.interfaces.IProcessService)
     */
    @Override
    public void setProcessService(IProcessService processService) {
        this.processService = processService;      
    }
    
}
