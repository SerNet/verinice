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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementsByEntityIds;
import sernet.gs.ui.rcp.main.service.taskcommands.LoadMassnahmeById;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class AssignResponsiblePersonAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.assignresponsiblepersonaction"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(AssignResponsiblePersonAction.class);
    private final IWorkbenchWindow window;
    private boolean serverIsRunning = true;
    private static final String DEFAULT_ERR_MSG = "Error while creating relation.";

    public AssignResponsiblePersonAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PERSON));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText("");
        setRightID(ActionRightIDs.RELATIONS);
        if (Activator.getDefault().isStandalone() && !Activator.getDefault().getInternalServer().isRunning()) {
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        serverIsRunning = true;
                        setEnabled(checkRights());
                    }
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
    }

    @Override
    public void run() {
        Activator.inheritVeriniceContextState();
        final IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }
        final List<CnATreeElement> elementList = createList(selection.toList());

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @SuppressWarnings("restriction")
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Object sel = null;
                    try {
                        Activator.inheritVeriniceContextState();
                        monitor.beginTask(Messages.AssignResponsiblePersonAction_2, selection.size());
                        for (Iterator iter = elementList.iterator(); iter.hasNext();) {
                            sel = iter.next();
                            MassnahmenUmsetzung massnahme = (MassnahmenUmsetzung) sel;
                            monitor.setTaskName(NLS.bind(Messages.AssignResponsiblePersonAction_3, massnahme.getTitle()));
                            doRun(massnahme);
                            monitor.worked(1);
                        }
                    } catch (Exception e) {
                        LOG.error("Error while command", e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            LOG.error(DEFAULT_ERR_MSG, e);

        } catch (InterruptedException e) {
            LOG.error(DEFAULT_ERR_MSG, e);
        }
    }

    /**
     * @param o
     * @throws CommandException
     */
    private void doRun(Object o) throws CommandException {
        if (o instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung elmt = (MassnahmenUmsetzung) o;
            MassnahmenUmsetzung massnahme = getMassnahme(elmt);
            PropertyList umsetzungDurch = massnahme.getUmsetzungDurchLink();
            createRelation(massnahme, umsetzungDurch);
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
            List<Integer> umsetzungDurchDbIds = getProperties(umsetzungDurch);
            List<Person> personenUmsetzungDurch = getPersonsForDbIds(umsetzungDurchDbIds);
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
     * @param dependantIds
     * @param personenUmsetzungDurch
     * @param allLinks
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
                createLink(massnahme, person, MassnahmenUmsetzung.MNUMS_RELATION_ID);
            }
        }
    }

    /**
     * @param massnahme
     * @param person
     * @throws CommandException
     */
    private void createLink(MassnahmenUmsetzung massnahme, Person person, String typeId) throws CommandException {
        CreateLink createLinkcommand = new CreateLink(person, massnahme, MassnahmenUmsetzung.MNUMS_RELATION_ID);
        ServiceFactory.lookupCommandService().executeCommand(createLinkcommand);
    }

    /**
     * @param umsetzungDurch
     * @return
     */
    private List<Integer> getProperties(PropertyList umsetzungDurch) {
        List<Property> props;
        props = umsetzungDurch.getProperties();
        List<Integer> ids = new ArrayList<Integer>(props.size());
        for (Property p : props) {
            ids.add(Integer.valueOf(p.getPropertyValue()));
        }
        return ids;
    }

    /**
     * @param ids
     * @return
     * @throws CommandException
     */
    private List<Person> getPersonsForDbIds(List<Integer> ids) throws CommandException {
        LoadCnAElementsByEntityIds<Person> personListe = new LoadCnAElementsByEntityIds<Person>(Person.class, ids);
        personListe = ServiceFactory.lookupCommandService().executeCommand(personListe);
        List<Person> personen = personListe.getElements();
        return personen;
    }

    private MassnahmenUmsetzung getMassnahme(MassnahmenUmsetzung massnahme) {
        LoadMassnahmeById command = new LoadMassnahmeById(massnahme.getDbId());
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException ce) {
            LOG.error("Error while get modul", ce);
        }
        massnahme = command.getElmt();

        return massnahme;
    }

    protected List<CnATreeElement> createList(List elementList) {
        List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        int depth = 0;
        int removed = 0;
        for (Object sel : elementList) {
            if (sel instanceof MassnahmenUmsetzung) {
                createList((CnATreeElement) sel, tempList, insertList, depth, removed);
            }
        }
        return insertList;
    }

    private void createList(CnATreeElement element, List<CnATreeElement> tempList, List<CnATreeElement> insertList, int depth, int removed) {
        if (!tempList.contains(element)) {
            tempList.add(element);
            if (depth == 0) {
                insertList.add(element);
            }
            if (element instanceof MassnahmenUmsetzung) {
                int newDepth = depth++;
                element = Retriever.checkRetrieveChildren(element);
                for (CnATreeElement child : element.getChildren()) {
                    createList(child, tempList, insertList, newDepth, removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
     * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        // TODO Auto-generated method stub
        if (serverIsRunning) {
            setEnabled(checkRights());
            if (input instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) input;
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object element = iter.next();
                    if (!(element instanceof CnATreeElement)) {
                        setEnabled(false);
                        return;
                    }
                }
                if (checkRights()) {
                    setEnabled(true);
                }
                return;
            }
            setEnabled(false);
        }
    }

}