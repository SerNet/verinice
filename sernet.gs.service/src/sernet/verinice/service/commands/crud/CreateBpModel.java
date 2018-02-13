/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin.
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
package sernet.verinice.service.commands.crud;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.model.LoadModel;

/**
 * Creates a new base protection model in the database
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CreateBpModel extends SaveElement<BpModel> {

    private static final long serialVersionUID = 6111954689912451921L;
    private transient Logger log = Logger.getLogger(CreateBpModel.class);
    
    public CreateBpModel() {
        this.element = new BpModel();
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        LoadModel<BpModel> loadModel = new LoadModel<>(BpModel.class);
        try {
            loadModel = getCommandService().executeCommand(loadModel);
        } catch (CommandException e) {
            getLog().error("Error while loading base protection model", e);
            throw new RuntimeCommandException("Error while loading base protection model.", e);
        }
        final BpModel model = loadModel.getModel();
        if(model==null) {
            super.execute();
        } else {
            getLog().warn("Base protection model exists. Will NOT create another model. Returning existing model.");
            element = model;
        }
    }
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateBpModel.class);
        }
        return log;
    }
   
}
