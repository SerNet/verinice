/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Checks whether the ownGefaehrdung is already used - means if there is a
 * GefaehrdungsUmsetzung made of the OwnGefaehrdung
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class CheckOwnGefaehrdungInUseCommand extends GenericCommand {

    private static final long serialVersionUID = 6512515989362442858L;
    private OwnGefaehrdung ownGefaehrdung;
    private boolean isInUse;
    private transient Logger log = Logger.getLogger(CheckOwnGefaehrdungInUseCommand.class);

    public CheckOwnGefaehrdungInUseCommand(OwnGefaehrdung ownGefaehrdungToCheck) {
        super();
        ownGefaehrdung = ownGefaehrdungToCheck;
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CheckOwnGefaehrdungInUseCommand.class);
        }
        return log;
    }

    @Override
    public void execute() {
        try {
            LoadElementByTypeId command = new LoadElementByTypeId(
                    GefaehrdungsUmsetzung.TYPE_ID, RetrieveInfo.getPropertyInstance());
            command = getCommandService().executeCommand(command);
            List<? extends CnATreeElement> elements = command.getElementList();
            isInUse = GefaehrdungsUtil.listContainsById(elements, ownGefaehrdung);
        } catch (CommandException e) {
            log.error("Error while checking OwnGefaehrdung", e);

        }

    }

    public boolean isInUse() {
        return isInUse;
    }
}
