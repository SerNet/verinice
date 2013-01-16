/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.actions.Messages;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *  Removes configuration objects from orgs / it networks
 */
public class PrepareObjectWithAccountDataForDeletion extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(PrepareObjectWithAccountDataForDeletion.class);

    private CnATreeElement elemnt;
    
    
    public PrepareObjectWithAccountDataForDeletion(CnATreeElement element){
        this.elemnt = element;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try{
            iterateThroughGroup(elemnt);
        } catch (Exception e) {
            LOG.error("Error while deleting element.", e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
        }
    }
    
    /** 
     * deletes existing configuration object on Person, PersonIso or PersonGroup(recursivly)
     */
    private void freePersonElement(CnATreeElement personElement, String typeID){
        Configuration c = null;
        try{
            GenericCommand command = null;
            if(typeID.equals(Person.TYPE_ID) || typeID.equals(PersonIso.TYPE_ID)){
                command = new LoadConfigurationByUser(personElement);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                c = ((LoadConfigurationByUser)command).getConfiguration();
                if(c != null){
                    command = new RemoveConfiguration(c);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                } else {
                    return;
                }
            } else if(typeID.equals(PersonGroup.TYPE_ID) 
                    || typeID.equals(AuditGroup.TYPE_ID)
                    || typeID.equals(Audit.TYPE_ID)){
                if(!personElement.isChildrenLoaded()){
                    personElement = loadChildren(personElement);
                }
                for(CnATreeElement child : personElement.getChildren()){
                    freePersonElement(child, child.getTypeId());
                }
            }
        } catch (CommandException e){
            
            LOG.error("Error while deleting configuration elements", e);
        }
    }
    
    private void iterateThroughGroup(CnATreeElement element) throws CommandException{
        if(!element.isChildrenLoaded()){
            element = loadChildren(element);
        }
        if(isPersonElement(element)){
            freePersonElement(element, element.getTypeId());
        } else {
            for(CnATreeElement child : element.getChildren()){
                if(child instanceof IISO27kGroup){
                    for(String s : ((IISO27kGroup) child).getChildTypes()){
                        if(isPersonElement(s)){
                            freePersonElement(child, child.getTypeId());
                        }
                    }
                    iterateThroughGroup(child);
                }
            }
        }
    }
    
    private boolean isPersonElement(CnATreeElement person){
        return isPersonElement(person.getTypeId());
    }
    
    private boolean isPersonElement(String typeID){
        return (typeID.equals(PersonIso.TYPE_ID) || typeID.equals(Person.TYPE_ID) || typeID.equals(PersonGroup.TYPE_ID));
    }
    
    private CnATreeElement loadChildren(CnATreeElement element) throws CommandException{
        LoadChildrenForExpansion command = new LoadChildrenForExpansion(element);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        element = ((LoadChildrenForExpansion)command).getElementWithChildren();
        element.setChildrenLoaded(true);
        return element;
    }

}
