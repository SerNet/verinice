/*******************************************************************************
 * Copyright (c) 2013 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementsByEntityIds;
import sernet.gs.ui.rcp.main.service.taskcommands.LoadMassnahmeById;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.service.commands.CreateLink;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class AssignResponsiblePersonCommand extends GenericCommand{
    public static final String ID = "sernet.gs.ui.rcp.main.actions.assignresponsiblecommand"; //$NON-NLS-1$
    private transient Logger log = Logger.getLogger(AssignResponsiblePersonCommand.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(AssignResponsiblePersonCommand.class);
        }
        return log;
    }
    private Object o;
    private Integer anzahlMassnahmen;
    public AssignResponsiblePersonCommand(Object o) {
        this.o = o;
    }
   
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
      
        if (o instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung elmt = (MassnahmenUmsetzung) o;
            MassnahmenUmsetzung massnahme = getMassnahme(elmt);
            PropertyList umsetzungDurch = massnahme.getUmsetzungDurchLink();
            try {
                if (umsetzungDurch != null) {
                createRelation(massnahme, umsetzungDurch);
                }else{
                    MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "InfoRmation", Messages.AssignResponsiblePersonAction_4);
                }
            } catch (CommandException e) {
               log.error("Error while execute create relation", e);
            }
        }
    }
    
    /**
     * @param massnahme
     * @param umsetzungDurch
     * @throws CommandException
     */
    private void createRelation(MassnahmenUmsetzung massnahme, PropertyList umsetzungDurch) throws CommandException {
        Set<CnALink> linkedPersons = new HashSet<CnALink>();
        if (umsetzungDurch != null) {
            List<Person> personenUmsetzungDurch = getPersonsForDbIds(massnahme);
            Set<CnALink> allLinks = massnahme.getLinksUp();
            for (CnALink link : allLinks) {
                if (link.getId().getTypeId().equals(MassnahmenUmsetzung.MNUMS_RELATION_ID)) {
                    linkedPersons.add(link);
                }
            }
            createLinks(massnahme, personenUmsetzungDurch, linkedPersons);
        }
    }

    /**
     * @param massnahme
     * @param personenUmsetzungDurch
     * @param linkedPersons
     * @throws CommandException
     */
    private void createLinks(MassnahmenUmsetzung massnahme, List<Person> personenUmsetzungDurch, Set<CnALink> linkedPersons) throws CommandException {
        for (Person person : personenUmsetzungDurch) {
            boolean createLink = (linkedPersons == null || linkedPersons.isEmpty());
            if (!createLink) {
                createLink = true;
                for (CnALink link : linkedPersons) {
                    // ist der link schon vorhanden ?
                    Integer dependantId = link.getDependant().getDbId();
                    if (person.getDbId() == dependantId) {
                        createLink = false;
                        break;
                    }
                }
            }
            if (createLink) {
                createLinkCommand(massnahme, person, MassnahmenUmsetzung.MNUMS_RELATION_ID);
            }
        }
    }
    /**
     * @param massnahme
     * @param person
     * @throws CommandException
     */
    private void createLinkCommand(MassnahmenUmsetzung massnahme, Person person, String typeId) throws CommandException {
        CreateLink createLinkcommand = new CreateLink(person, massnahme, MassnahmenUmsetzung.MNUMS_RELATION_ID);
        ServiceFactory.lookupCommandService().executeCommand(createLinkcommand);
    }
    
    
    private List<Person> getPersonsForDbIds(MassnahmenUmsetzung mu) throws CommandException {
        String field = MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK;
        PropertyList pl = mu.getEntity().getProperties(field);
        List<Property> props = null; 
            
        if (pl != null){
            props = pl.getProperties();
        }
        if (props != null && !props.isEmpty())
        {
            List<Integer> ids = new ArrayList<Integer>(props.size());
            for (Property p : props)
            {
                ids.add(Integer.valueOf(p.getPropertyValue()));
            }
            
            LoadCnAElementsByEntityIds<Person> le = new LoadCnAElementsByEntityIds<Person>(Person.class, ids);
            try
            {
                le = getCommandService().executeCommand(le);
            }
            catch (CommandException ce)
            {
                getLog().error("Error while executing command: LoadCnAElementsByEntityIds", ce);
                throw new RuntimeException("Error while executing command: LoadCnAElementsByEntityIds", ce);
            }
            
            return le.getElements();
        }
        
        return Collections.emptyList();
    }
    
    
    private MassnahmenUmsetzung getMassnahme(MassnahmenUmsetzung massnahme) {
        LoadMassnahmeById command = new LoadMassnahmeById(massnahme.getDbId());
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException ce) {
            log.error("Error while get modul", ce);
        }
        massnahme = command.getElmt();

        return massnahme;
    }
  
   public Integer getMassnahmenanzahl(){
       return anzahlMassnahmen;
   }
    
 }