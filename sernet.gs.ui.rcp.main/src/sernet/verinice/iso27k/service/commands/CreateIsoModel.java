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
package sernet.verinice.iso27k.service.commands;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.service.iso27k.LoadModel;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CreateIsoModel extends SaveElement<ISO27KModel> {

    private transient Logger log = Logger.getLogger(CreateIsoModel.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateIsoModel.class);
        }
        return log;
    }
    
    /**
     * @param element
     */
    public CreateIsoModel() {
        this.element = new ISO27KModel();
        this.stationId = ChangeLogEntry.STATION_ID;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.crudcommands.SaveElement#execute()
     */
    @Override
    public void execute() {
        LoadModel loadModel = new LoadModel();     
        try {
            loadModel = getCommandService().executeCommand(loadModel);
        } catch (CommandException e) {
            getLog().error("Error while loading model.", e);
            throw new RuntimeException("Error while loading model.", e);
        }
        final ISO27KModel model = loadModel.getModel();
        if(model==null) {
            super.execute();
        } else {
            getLog().warn("ISO27k model exists. Will NOT create another model. Returning existing model.");
            element = model;
        }
    }
    
    

}
