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
import sernet.verinice.iso27k.rcp.ISO27KModelViewUpdate;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadElementByTypeId;

/**
 * ElementView which shows {@link Organization}s.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
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
            LoadElementByTypeId command = getCommandFactory().getElementCommand();
            command = getCommandService().executeCommand(command);
            elementList = command.getElementList();
        }
        if(selectedGroup!=null && (elementList==null || !elementList.contains(getSelectedGroup()))) {    
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing selected group, Type: " + selectedGroup.getTypeId() + ", name: " + selectedGroup.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
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
        LoadElementByTypeId command = new LoadElementByTypeId(Organization.TYPE_ID);
        command = getCommandService().executeCommand(command);
        return command.getElementList();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#createISO27KModelViewUpdate()
     */
    protected ISO27KModelViewUpdate createISO27KModelViewUpdate() {
        return new ISO27KModelViewUpdate(viewer,cache) {
            /* (non-Javadoc)
             * @see sernet.verinice.iso27k.model.IISO27KModelListener#linkAdded(sernet.gs.ui.rcp.main.common.model.CnALink)
             */
            public void linkAdded(CnALink link) {
                reload();
            }
            /* (non-Javadoc)
             * @see sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildAdded(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
             */
            public void databaseChildAdded(CnATreeElement child) {
                super.databaseChildAdded(child);
                reload();
            }
        };
    }
}
