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
package sernet.verinice.service.commands;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.service.commands.LoadCnAElementsByEntityIds;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("serial")
public class AssignResponsiblePersonCommand extends GenericCommand {
    public static final String ID = "sernet.gs.ui.rcp.main.actions.assignresponsiblecommand"; //$NON-NLS-1$
    private transient Logger log = Logger.getLogger(AssignResponsiblePersonCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(AssignResponsiblePersonCommand.class);
        }
        return log;
    }

    private List<MassnahmenUmsetzung> selectedElements;
    private Set<CnALink> changedElements;
    private Set<CnALink> linkedElements;

    public AssignResponsiblePersonCommand(List<MassnahmenUmsetzung> selectMassnahmen) {
        this.selectedElements = selectMassnahmen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        changedElements = new HashSet<CnALink>();
        linkedElements = new HashSet<CnALink>();
        IBaseDao<MassnahmenUmsetzung, Serializable> massnahmeDAO = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
        for (MassnahmenUmsetzung massnahme : selectedElements) {
            massnahme = massnahmeDAO.findById(massnahme.getDbId());
            checkRelations(massnahme);
        }
    }

    /**
     * @param massnahme
     * @param umsetzungDurch
     * @throws CommandException
     */
    private void checkRelations(MassnahmenUmsetzung massnahme) {
        Set<CnALink> linkedPersons = new HashSet<CnALink>();
        try {
            List<Person> personenUmsetzungDurch = getPersonsbyProperty(massnahme);
            Set<Property> rolesToSearch = findRole(massnahme);

            if (personenUmsetzungDurch != null && !personenUmsetzungDurch.isEmpty()) {
                for (Person person : personenUmsetzungDurch) {
                    assignPerson(massnahme, linkedPersons, rolesToSearch, person);
                }
            } else {
                findLinkedPersonsUpTree(massnahme, linkedPersons, rolesToSearch);
            }

        } catch (CommandException ce) {
            log.error("Error while creating relation", ce);
        }
    }

    /**
     * @param massnahme
     * @param linkedPersons
     * @param rolesToSearch
     * @param person
     * @throws CommandException
     */
    private void assignPerson(MassnahmenUmsetzung massnahme, Set<CnALink> linkedPersons, Set<Property> rolesToSearch, Person person) throws CommandException {
        Set<CnALink> allLinks = massnahme.getLinksUp();
        if (allLinks == null || allLinks.isEmpty()) {
            allLinks = massnahme.getParent().getParent().getLinksUp();
        }
        for (CnALink link : allLinks) {
            if (link.getId().getTypeId().equals(MassnahmenUmsetzung.MNUMS_RELATION_ID)) {
                linkedPersons.add(link);
                linkedElements.add(link);
            }
        }
        for (Property role : rolesToSearch) {
            if (person.hasRole(role)) {
                createLinks(massnahme, person, linkedPersons);
            }
        }
    }

    /**
     * @param massnahme
     * @return
     */
    private Set<Property> findRole(MassnahmenUmsetzung massnahme) {
        PropertyList rolen = massnahme.getEntity().getProperties(MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
        Set<Property> rolesToSearch = new HashSet<Property>();
        rolesToSearch.addAll(rolen.getProperties());
        return rolesToSearch;
    }

    /**
     * @param massnahme
     * @param personenUmsetzungDurch
     * @param linkedPersons
     * @throws CommandException
     */
    private void createLinks(MassnahmenUmsetzung massnahme, Person person, Set<CnALink> linkedPersons) throws CommandException {
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

    /**
     * @param massnahme
     * @param person
     * @throws CommandException
     */
    private CnALink createLinkCommand(MassnahmenUmsetzung massnahme, Person person, String typeId) throws CommandException {
        @SuppressWarnings("unchecked")
        CreateLink command = new CreateLink(person, massnahme, MassnahmenUmsetzung.MNUMS_RELATION_ID);
        getCommandService().executeCommand(command);
        changedElements.add(command.getLink());
        return command.getLink();
    }

    private List<Person> getPersonsbyProperty(MassnahmenUmsetzung mu) throws CommandException {
        String field = MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK;
        PropertyList pl = mu.getEntity().getProperties(field);
        List<Property> props = null;

        if (pl != null) {
            props = pl.getProperties();
        }
        if (props != null && !props.isEmpty()) {
            List<Integer> ids = new ArrayList<Integer>(props.size());
            for (Property p : props) {
                ids.add(Integer.valueOf(p.getPropertyValue()));
            }

            LoadCnAElementsByEntityIds<Person> le = new LoadCnAElementsByEntityIds<Person>(Person.class, ids);
            try {
                le = getCommandService().executeCommand(le);
            } catch (CommandException ce) {
                getLog().error("Error while executing command: LoadCnAElementsByEntityIds", ce);
                throw new RuntimeException("Error while executing command: LoadCnAElementsByEntityIds", ce);
            }

            return le.getElements();
        }

        return Collections.emptyList();
    }

    private void findLinkedPersonsUpTree(MassnahmenUmsetzung massnahme, Set<CnALink> linkedPersons, Set<Property> rolesToSearch) {
        IBaseDao<Person, Serializable> personDAO = getDaoFactory().getDAO(Person.class);
        try {
            Set<CnALink> modulResponseLinks = massnahme.getParent().getLinksUp();
            if (modulResponseLinks == null || modulResponseLinks.isEmpty()) {
                modulResponseLinks = massnahme.getParent().getParent().getLinksUp();
                for (CnALink plink : modulResponseLinks) {
                    if (plink.getDependant().getTypeId().equals(Person.TYPE_ID)) {
                        Person person = personDAO.findById(plink.getDependant().getDbId());
                        assignPerson(massnahme, linkedPersons, rolesToSearch, person);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while searching relations", e);
        }
    }

    public Set<CnALink> getchanedElements() {
        return changedElements;
    }

    public Set<CnALink> getlinkedElements() {
        return linkedElements;
    }

}