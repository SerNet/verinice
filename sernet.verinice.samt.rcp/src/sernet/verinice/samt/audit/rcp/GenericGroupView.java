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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.commands.LoadElementByClass;
import sernet.verinice.iso27k.service.commands.LoadLinkedElements;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class GenericGroupView extends ElementView {

    static Map<String, ICommandFactory> commandMap;

    static {
        commandMap = new Hashtable<String, ICommandFactory>();
        commandMap.put(Asset.TYPE_ID, new AssetCommandFactory());
        commandMap.put(Audit.TYPE_ID, new AuditCommandFactory());
        commandMap.put(Finding.TYPE_ID, new FindingCommandFactory());
        commandMap.put(Evidence.TYPE_ID, new EvidenceCommandFactory());
        commandMap.put(Control.TYPE_ID, new ControlCommandFactory());
        commandMap.put(Organization.TYPE_ID, new OrganizationCommandFactory());
    }
    
    private ICommandFactory commandFactory;
    
    public static GenericGroupView getAssetInstance() {
        return new GenericGroupView(new AssetCommandFactory());
    }
    
    public static GenericGroupView getAuditInstance() {
        return new GenericGroupView(new AuditCommandFactory());
    }
    
    public static GenericGroupView getControlInstance() {
        return new GenericGroupView(new ControlCommandFactory());
    }
    
    public GenericGroupView(ICommandFactory commandFactory) {
        super();
        this.commandFactory = commandFactory;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#getElementList()
     */
    @Override
    protected List<? extends CnATreeElement> getElementList() throws CommandException {
        List<AssetGroup> elementList = Collections.emptyList();
        if(commandFactory!=null) {
            LoadElementByClass command = commandFactory.getElementCommand();
            command = getCommandService().executeCommand(command);
            elementList = command.getElementList();
        }
        return elementList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ElementView#getLinkedElements(int)
     */
    @Override
    protected List<CnATreeElement> getLinkedElements(int selectedId) throws CommandException {
        List<CnATreeElement> elementList = Collections.emptyList();
        if(commandFactory!=null) {
            LoadLinkedElements command = commandFactory.getLinkedElementCommand(selectedId);
            command = getCommandService().executeCommand(command);
            elementList = command.getElementList();
        }
        return elementList;
    }

    protected ICommandFactory getCommandFactory() {
        return commandFactory;
    }

    protected void setCommandFactory(ICommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * @param objectTypeId
     */
    public void switchElement(String objectTypeId) {
        if (commandMap.get(objectTypeId) != null) {
            setCommandFactory(commandMap.get(objectTypeId));
            setIcon(ImageCache.getInstance().getISO27kTypeImage(objectTypeId));
            reload();
            setViewTitle(objectTypeId);
        }   
    }

}
