/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.consolidator;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.bp.ConsoliData;
import sernet.verinice.service.commands.bp.ConsolidatorCommand;

/**
 * This makes a {@link ConsolidatorCommand} and sends it to the server.
 */
public class Consolidator {

    private static final Logger logger = Logger.getLogger(Consolidator.class);

    private Consolidator() {
    }

    public static String consolidate(@NonNull ConsoliData data) {
        ConsolidatorCommand command = new ConsolidatorCommand(data);
        try {
            ServiceFactory.lookupCommandService().executeCommand(command);
            CnAElementFactory.getInstance().reloadBpModelFromDatabase();
        } catch (CommandException e) {
            logger.error(e.getMessage());
            return e.getLocalizedMessage();
        }
        return null;
    }
}