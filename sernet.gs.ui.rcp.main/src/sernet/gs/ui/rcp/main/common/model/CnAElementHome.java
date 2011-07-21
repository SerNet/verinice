/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.hibernate.StaleObjectStateException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelForTreeView;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCurrentUserConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateControlEntity;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.gs.ui.rcp.main.service.taskcommands.CreateScenario;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllTags;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveLink;

/**
 * DAO class for model objects. Uses Hibernate as persistence framework.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */

public class CnAElementHome {

    private final Logger log = Logger.getLogger(CnAElementHome.class);

    private Set<String> roles = null;

    private static CnAElementHome instance;
    
    protected static final String LINK_NO_COMMENT = ""; //$NON-NLS-1$

    private static final String QUERY_FIND_BY_ID = "from " + CnATreeElement.class.getName() + " as element " + "where element.dbId = ?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final String QUERY_FIND_CHANGES_SINCE = "from " + ChangeLogEntry.class.getName() + " as change " + "where change.changetime > ? " + "and not change.stationId = ? " + "order by changetime"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private ICommandService commandService;

    public ICommandService getCommandService() {
        if (commandService == null) {
            try {
                open();
            } catch (Exception e) {
                log.error("Error while opening command service", e); //$NON-NLS-1$
            }
        }
        return commandService;
    }

    private CnAElementHome() {
        // singleton
    }

    public static CnAElementHome getInstance() {
        if (instance == null) {
            instance = new CnAElementHome();
        }
        return instance;
    }

    public boolean isOpen() {
        return commandService != null;
    }

    public void open(IProgress monitor) throws Exception {
        open(CnAWorkspace.getInstance().getConfDir(), monitor);
    }

    public void preload(String confDir) {
        // do nothing
    }

    public void open(String confDir, IProgress monitor) throws Exception {
        monitor.beginTask(Messages.getString("CnAElementHome.0"), IProgress.UNKNOWN_WORK); //$NON-NLS-1$
        ServiceFactory.openCommandService();
        commandService = ServiceFactory.lookupCommandService();
    }

    public void open() throws Exception {
        // causes NoClassDefFoundError: org/eclipse/ui/plugin/AbstractUIPlugin
        // in web environment
        // TODO: this class should only be used on the RCP client!!!
        ServiceFactory.openCommandService();
        commandService = createCommandService();
    }

    private ICommandService createCommandService() {
        commandService = ServiceFactory.lookupCommandService();
        return commandService;
    }

    public void close() {
        ServiceFactory.closeCommandService();
        commandService = null;
    }

    public <T extends CnATreeElement> T save(T element) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Saving new element: " + element); //$NON-NLS-1$
        }
        SaveElement<T> saveCommand = new SaveElement<T>(element);
        saveCommand = getCommandService().executeCommand(saveCommand);
        return saveCommand.getElement();
    }

    /**
     * Creates a new instance of class clazz as a child of container.
     * The new instance is saved in the database.
     * 
     * A localized title of the instance is set on the server 
     * which locale may differ from the clients locale.
     * 
     * @param container The parent of the new instance
     * @param clazz Class of the new instance
     * @return the new instance which is saved in the database
     */
    public <T extends CnATreeElement> T save(CnATreeElement container, Class<T> clazz) throws Exception {
        return save(container, clazz, null);
    }
    
    /**
     * Creates a new instance of class clazz as a child of container.
     * The new instance is saved in the database.
     * 
     * If you pass a typeId (HUI-Type-id) a localized title of the 
     * instance is set on the cliant via HUITypeFactory from message bundle.
     * If typeId is null a localized title of the instance is set on the server 
     * which locale may differ from the clients locale.
     * 
     * @param container The parent of the new instance
     * @param clazz Class of the new instance
     * @param typeId HUI-Type-Id or null
     * @return the new instance which is saved in the database
     */
    public <T extends CnATreeElement> T save(CnATreeElement container, Class<T> clazz, String typeId) throws Exception {
        String title = null;
        if(typeId!=null) {
            // load the localized title via HUITypeFactory from message bundle
            title = HitroUtil.getInstance().getTypeFactory().getMessage(typeId);
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating new instance of " + clazz.getName() + " in " + container + " with title: " + title); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        CreateElement<T> saveCommand = new CreateElement<T>(container, clazz, title);
        saveCommand = getCommandService().executeCommand(saveCommand);
        return saveCommand.getNewElement();
    }

    public BausteinUmsetzung save(CnATreeElement container, Baustein baustein) throws Exception {
        log.debug("Creating new element in " + container); //$NON-NLS-1$
        CreateBaustein saveCommand = new CreateBaustein(container, baustein);
        saveCommand = getCommandService().executeCommand(saveCommand);
        return saveCommand.getNewElement();
    }

    public CnALink createLink(CnATreeElement dropTarget, CnATreeElement dragged) throws CommandException {
        log.debug("Saving new link from " + dropTarget + " to " + dragged); //$NON-NLS-1$ //$NON-NLS-2$
        CreateLink command = new CreateLink(dropTarget, dragged);
        command = getCommandService().executeCommand(command);
        return command.getLink();
    }

    public CnALink createLink(CnATreeElement dropTarget, CnATreeElement dragged, String typeId, String comment) throws CommandException {
        log.debug("Saving new link from " + dropTarget + " to " + dragged + "of type " + typeId); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        CreateLink command = new CreateLink(dropTarget, dragged, typeId, comment);
        command = getCommandService().executeCommand(command);

        return command.getLink();
    }

    public void remove(CnATreeElement element) throws Exception {
        log.debug("Deleting " + element.getTitle()); //$NON-NLS-1$
        RemoveElement command = new RemoveElement(element);
        command = getCommandService().executeCommand(command);
    }

    public void remove(CnALink element) throws Exception {
        RemoveLink command = new RemoveLink(element);
        command = getCommandService().executeCommand(command);
    }

    public CnATreeElement update(CnATreeElement element) throws Exception {
        UpdateElement command = new UpdateElement(element, true, ChangeLogEntry.STATION_ID);
        command = getCommandService().executeCommand(command);
        return (CnATreeElement) command.getElement();
    }
    
    /**
     * @param cnAElement
     * @throws Exception 
     */
    public CnATreeElement updateEntity(CnATreeElement element) throws Exception {
        UpdateElementEntity<? extends CnATreeElement> command = createCommand(element);
        command = getCommandService().executeCommand(command);
        return (CnATreeElement) command.getElement(); 
    }

    public void update(List<? extends CnATreeElement> elements) throws StaleObjectStateException, CommandException {
        UpdateMultipleElements command = new UpdateMultipleElements(elements, ChangeLogEntry.STATION_ID);
        command = getCommandService().executeCommand(command);
    }

   

    /**
     * Load object with given ID for given class.
     * 
     * @param clazz
     * @param id
     * @return
     * @throws CommandException
     */
    @SuppressWarnings("unchecked")
	public CnATreeElement loadById(String typeId, int id) throws CommandException {
		LoadCnAElementById command = new LoadCnAElementById(typeId, id);
        command = getCommandService().executeCommand(command);
        return command.getFound();
    }

    /**
     * Load whole model from DB (lazy). Proxies will be instantiated by
     * hibernate on first access.
     * 
     * @param nullMonitor
     * 
     * @return BSIModel object which is the top level object of the model
     *         hierarchy.
     * @throws Exception
     */
    public BSIModel loadModel(IProgress nullMonitor) throws Exception {
        log.debug("Loading model instance"); //$NON-NLS-1$

        nullMonitor.setTaskName(Messages.getString("CnAElementHome.1")); //$NON-NLS-1$

        LoadBSIModelForTreeView command = new LoadBSIModelForTreeView();
        command = getCommandService().executeCommand(command);
        BSIModel model = command.getModel();
        return model;
    }

    /**
     * Refresh given object from the database, looses all changes made in
     * memory, sets element and all properties to actual state in database.
     * 
     * Does not reload children or other collections of this object.
     * 
     * @param cnAElement
     * @throws CommandException
     */
    public void refresh(CnATreeElement cnAElement) throws CommandException {
        RefreshElement command = new RefreshElement(cnAElement);
        command = getCommandService().executeCommand(command);
        CnATreeElement refreshedElement = command.getElement();
        cnAElement.setEntity(refreshedElement.getEntity());
    }

    public List<ITVerbund> getItverbuende() throws CommandException {
        LoadCnAElementByType<ITVerbund> command = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
        command = getCommandService().executeCommand(command);
        return command.getElements();
    }

    public List<Person> getPersonen() throws CommandException {
        LoadCnAElementByType<Person> command = new LoadCnAElementByType<Person>(Person.class);
        command = getCommandService().executeCommand(command);
        return command.getElements();

    }

    public List<String> getTags() throws CommandException {
        FindAllTags command = new FindAllTags();
        command = getCommandService().executeCommand(command);
        return command.getTags();
    }

    /**
     * Returns whether it is allowed to perform a delete operation on the given
     * element.
     * 
     * @param cte
     * @return
     */
    public boolean isDeleteAllowed(CnATreeElement cte) {
        if (cte instanceof ImportIsoGroup) {
            return true;
        }
        
        // Category objects cannot be deleted.
        if (cte instanceof IBSIStrukturKategorie) {
            return false;
        }

        // ITVerbund instances can be removed when
        // one has write access to it (There is no parent to check).
        if (cte instanceof ITVerbund) {
            return isWriteAllowed(cte);
        }

        // For normal CnATreeElement instance we need write privileges
        // on the instance and its parent to be able to delete it.
        return isWriteAllowed(cte) && isWriteAllowed(cte.getParent());
    }

    /**
     * Returns whether it is allowed to perform a delete operation on the given
     * {@link CnALink} element.
     * 
     * <p>
     * The link can be deleted if write permissions exist on the
     * {@link CnATreeElement} instance the link belongs to.
     * </p>
     * 
     * @param cte
     * @return
     */
    public boolean isDeleteAllowed(CnALink cl) {
        return isWriteAllowed(cl.getDependant());
    }

    /**
     * Returns whether it is allowed to perform the creation of a new child
     * element on the given element.
     * 
     * @param cte
     * @return
     */
    public boolean isNewChildAllowed(CnATreeElement cte) {
        return cte instanceof BSIModel || cte instanceof ISO27KModel || isWriteAllowed(cte);
    }

    /**
     * Returns whether it is allowed to perform modifications to properties of
     * this element.
     * 
     * @param cte
     * @return
     */
    public boolean isWriteAllowed(CnATreeElement cte) {
        // Short cut: If no permission handling is needed than all objects are
        // writable.
        ServiceFactory.lookupAuthService();
        if (!ServiceFactory.isPermissionHandlingNeeded()) {
            return true;
        }

        // Short cut 2: If we are the admin, then everything is writable as
        // well.
        if (AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN })) {
            return true;
        }

        if (roles == null) {
            LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();
            try {
                lcuc = getCommandService().executeCommand(lcuc);
            } catch (CommandException e) {
                ExceptionUtil.log(e, Messages.getString("CnAElementHome.2")); //$NON-NLS-1$
                return false;
            }

            Configuration c = lcuc.getConfiguration();

            // No configuration for the current user (anymore?). Then nothing is
            // writable.
            if (c == null) {
                return false;
            }

            roles = c.getRoles();
        }

        for (Permission p : cte.getPermissions()) {
            if (p.isWriteAllowed() && roles.contains(p.getRole())) {
                return true;
            }
        }

        return false;
    }
    
    public void createLinksAccordingToBusinessLogic(final CnATreeElement dropTarget, final List<CnATreeElement> toDrop) {
        if (log.isDebugEnabled()) {
            log.debug("createLink..."); //$NON-NLS-1$
        }
        
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                Activator.inheritVeriniceContextState();
                List<CnALink> newLinks = new ArrayList<CnALink>();
                allDragged: for (CnATreeElement dragged : toDrop) {
                    try {
                        // ISO 27k elements are only linked using XML-defined relations:
                        if (dropTarget instanceof IISO27kElement && dragged instanceof IISO27kElement) {

                            // special case: threats and vulnerabilities can create a new scenario when dropped:
                            if (dropTarget instanceof Threat && dragged instanceof Vulnerability) {
                                Threat threat;
                                Vulnerability vuln;
                                threat = (Threat) dropTarget;
                                vuln = (Vulnerability) dragged;
                                createScenario(threat, vuln);
                            } 
                            else if (dropTarget instanceof Vulnerability && dragged instanceof Threat) {
                                Threat threat;
                                Vulnerability vuln;
                                vuln = (Vulnerability) dropTarget;
                                threat = (Threat) dragged;
                                createScenario(threat, vuln);
                            }
                            
                            Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory()
                                .getPossibleRelations(dropTarget.getEntityType().getId(), dragged.getEntityType().getId());
                            // try to link from target to dragged elements first:
                            // use first relation type (user can change it later):
                            if (!possibleRelations.isEmpty()) {
                                boolean linkCreated = createTypedLink(newLinks, dropTarget, dragged, possibleRelations.iterator().next().getId(), LINK_NO_COMMENT);
                                if (linkCreated)
                                    continue allDragged;
                            }
                            
                            // if none found: try reverse direction from dragged element to target (link is always modelled from one side only)
                            possibleRelations = HitroUtil.getInstance().getTypeFactory()
                                .getPossibleRelations(dragged.getEntityType().getId(), dropTarget.getEntityType().getId());
                            if ( !possibleRelations.isEmpty()) {
                                // use first relation type (user can change it later):
                                boolean linkCreated = createTypedLink(newLinks, dragged, dropTarget, possibleRelations.iterator().next().getId(), LINK_NO_COMMENT);
                                if (linkCreated)
                                    continue allDragged;
                            }
                        } // end for ISO 27k elements
                        
                        // backwards compatibility: BSI elements can be linked without a defined relation type, but we use one if present:
                        if (dropTarget instanceof IBSIStrukturElement || dragged instanceof IBSIStrukturElement) {
                            CnATreeElement from = dropTarget;
                            CnATreeElement to = dragged;
                            Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory()
                                .getPossibleRelations(from.getEntityType().getId(), to.getEntityType().getId());
                            if (possibleRelations.isEmpty()) {
                                // try again for reverse direction:
                                from = dragged;
                                to = dropTarget;
                                possibleRelations = HitroUtil.getInstance().getTypeFactory()
                                .getPossibleRelations(from.getEntityType().getId(), to.getEntityType().getId());
                            }
                            if (possibleRelations.isEmpty()) {
                                //still nothing found, create untyped link:
                                CnALink link = CnAElementHome.getInstance().createLink(dropTarget, dragged);
                                newLinks.add(link);
                            }
                            else {
                                // create link with type:
                                createTypedLink(newLinks, from, to, possibleRelations.iterator().next().getId(), LINK_NO_COMMENT);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Saving link failed.", e); //$NON-NLS-1$
                    }
                }
        
                // fire model changed events:
                for (CnALink link : newLinks) {
                    if (link.getDependant() instanceof ITVerbund) {
                        CnAElementFactory.getInstance().reloadModelFromDatabase();
                        return;
                    } else {
                        if (link.getDependant() instanceof IBSIStrukturElement || link.getDependency() instanceof IBSIStrukturElement) {
                            CnAElementFactory.getLoadedModel().linkAdded(link);
                        }
                        if (link.getDependant() instanceof IISO27kElement || link.getDependency() instanceof IISO27kElement) {
                            CnAElementFactory.getInstance().getISO27kModel().linkAdded(link);
                        }
                    }
                }
                DNDItems.clear();
            }
        });
    }

    /**
     * @param threat
     * @param vuln
     */
    protected void createScenario(Threat threat, Vulnerability vuln) {
        boolean confirm = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
                Messages.getString("CnAElementHome.5"), Messages.getString("CnAElementHome.6") + //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("CnAElementHome.7")); //$NON-NLS-1$
        if (!confirm)
            return;
        
        try {
            CreateScenario command = new CreateScenario(threat, vuln);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            IncidentScenario newElement = command.getNewElement();
            CnAElementFactory.getInstance().getISO27kModel().childAdded(newElement.getParent(), newElement);
        } catch (CommandException e) {
            ExceptionUtil.log(e, "Error while creating the new scenario."); //$NON-NLS-1$
        }
    }



    /**
     * @param newLinks
     * @param dropTarget
     * @param dragged
     * @param id
     * @param noComment
     * @return
     * @throws CommandException 
     */
    protected boolean createTypedLink(List<CnALink> newLinks,
            CnATreeElement from, CnATreeElement to, String relationTypeid,
            String comment) throws CommandException {
        // use first one (user can change it later):
        CnALink link = CnAElementHome.getInstance()
            .createLink(from, to, relationTypeid, comment );
        if (link == null)
            return false;
        newLinks.add(link);
        if (log.isDebugEnabled())
            log.debug("Link created"); //$NON-NLS-1$
        return true;
    }

    /**
     * Creates an specific update command for an element.
     * 
     * @param element
     * @return update command
     */
    private UpdateElementEntity<? extends CnATreeElement> createCommand(CnATreeElement element) {
        UpdateElementEntity<? extends CnATreeElement>  command = null;
        if(Control.TYPE_ID.equals(element.getTypeId())) {
            command = new UpdateControlEntity((Control) element, true, ChangeLogEntry.STATION_ID);
        } else {
            command = new UpdateElementEntity<CnATreeElement>(element, true, ChangeLogEntry.STATION_ID);
        }
        return command;
    }

   

}
