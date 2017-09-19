/*******************************************************************************
 * Copyright (c) 2017 Alexander Koderman <ak[at]sernet[dot]de>.
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
package sernet.verinice.service.bp;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.service.commands.crud.CreateBpModel;

/**
 * Loads the base protection from the database.
 * If no model exists in the database a new model is created and returned.
 * If more than model exists a RuntimeCommandException is thrown.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LoadBpModel extends GenericCommand implements INoAccessControl {

    private static final long serialVersionUID = -7352618220257525973L;

    private transient Logger log = Logger.getLogger(LoadBpModel.class);

	private BpModel model;

	public LoadBpModel() {
	    // nothing to do
	}
	
	@SuppressWarnings("unchecked")
    public void execute() {
		List<BpModel> modelList = getDaoFactory().getDAO(BpModel.class).findAll(RetrieveInfo.getChildrenInstance());
		if(modelList==null || modelList.isEmpty()) {
		    if (getLog().isInfoEnabled()) {
		        getLog().info("No base protection model found. Creating a new one...");
            }
		    createBpModel();
		} else if(modelList.size()>1) {
				throw new RuntimeCommandException("More than one base protection model found.");
		} else if(modelList.size()==1) {			
			model = modelList.get(0);
		}
	}
	
	private void createBpModel() {
        try {
            CreateBpModel modelCreationCommand = new CreateBpModel();
            modelCreationCommand = getCommandService()
                    .executeCommand(modelCreationCommand);
            model = modelCreationCommand.getElement();
        } catch (CommandException e) {
            throw new RuntimeCommandException("Error while creating base protection model.", e);
        }      
    }

	public BpModel getModel() {
		return model;
	}
	
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadBpModel.class);
        }
        return log;
    }
	
}
