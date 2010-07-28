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
package sernet.verinice.samt.audit.rcp;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.commands.LoadElementByClass;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class OrganizationView extends GenericElementView {

    private static final Logger LOG = Logger.getLogger(OrganizationView.class);
    
    public static final String ID = "sernet.verinice.samt.audit.rcp.OrganizationView"; //$NON-NLS-1$
    
    
    public OrganizationView() {
        super(new OrganizationCommandFactory());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#getElementList()
     */
    @Override
    protected List<? extends CnATreeElement> getElementList() throws CommandException {
        List<? extends CnATreeElement> elementList = Collections.emptyList();
        if(getCommandFactory()!=null) {
            LoadElementByClass command = getCommandFactory().getElementCommand();
            command = getCommandService().executeCommand(command);
            elementList = command.getElementList();
        }
        if(selectedGroup!=null && (elementList==null || !elementList.contains(getSelectedGroup()))) {    
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing selected group, Type: " + selectedGroup.getObjectType() + ", name: " + selectedGroup.getTitle());
            }
            setSelectedGroup(null);
        }
        return elementList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#getLinkedElements(int)
     */
    @Override
    protected List<? extends CnATreeElement> getLinkedElements(int selectedId) throws CommandException {
        LoadElementByClass command = new LoadElementByClass(Organization.TYPE_ID);
        command = getCommandService().executeCommand(command);
        return command.getElementList();
    }
}
