/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.model.LoadModel;

/**
 * Creates a {@link CatalogModel}. There exists only one instance of
 * {@link CatalogModel} within verinice model.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class CreateCatalogModel extends SaveElement<CatalogModel> {

    
    private static transient Logger logger;
    
    private static final long serialVersionUID = 1L;
    
    public CreateCatalogModel() {
        this.element = new CatalogModel();
        this.stationId = ChangeLogEntry.STATION_ID;
    }
    
    
    @Override
    public void execute() {
        LoadModel<CatalogModel> loadModel = new LoadModel<>(CatalogModel.class);
        try {
            loadModel = getCommandService().executeCommand(loadModel);
        } catch (CommandException e) {
            getLogger().error("Error while loading model.", e);
            throw new RuntimeException("Error while loading model.", e);
        }
        final CatalogModel model = loadModel.getModel();
        if(model==null) {
            super.execute();
        } else {
            getLogger().warn(model.getTitle() + " model exists. Will NOT create another model. Returning existing model.");
            element = model;
        }
    }


    private Logger getLogger() {
        if(logger ==null){
            logger = Logger.getLogger(CreateCatalogModel.class);
        }
        
        return logger;
    }
    
    

}
