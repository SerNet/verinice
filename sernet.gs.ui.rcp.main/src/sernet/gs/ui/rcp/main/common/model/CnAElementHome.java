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

import java.net.MalformedURLException;
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
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModelForTreeView;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCurrentUserConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.gs.ui.rcp.main.service.taskcommands.CreateScenario;
import sernet.gs.ui.rcp.main.service.taskcommands.FindAllTags;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.commands.UpdateElement;

/**
 * DAO class for model objects. Uses Hibernate as persistence framework.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */

public final class CnAElementHome {

    private final Logger log = Logger.getLogger(CnAElementHome.class);

    private Set<String> roles = null;

    private static CnAElementHome instance;
    
    protected static final String LINK_NO_COMMENT = ""; //$NON-NLS-1$

    private static final String QUERY_FIND_BY_ID = "from " + CnATreeElement.class.getName() + " as element " + "where element.dbId = ?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private ICommandService commandService;
    
    private IValidationService validationService;

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

    public void open(IProgress monitor) throws MalformedURLException {
        open(CnAWorkspace.getInstance().getConfDir(), monitor);
    }

    public void preload(String confDir) {
        // do nothing
    }

    public void open(String confDir, IProgress monitor) throws MalformedURLException {
        monitor.beginTask(Messages.getString("CnAElementHome.0"), IProgress.UNKNOWN_WORK); //$NON-NLS-1$
        ServiceFactory.openCommandService();
        commandService = ServiceFactory.lookupCommandService();
    }

    public void open() throws MalformedURLException {
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

    public <T extends CnATreeElement> T save(T element) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Saving new element, uuid " + element.getUuid()); //$NON-NLS-1$
        }
        SaveElement<T> saveCommand = new SaveElement<T>(element);
        saveCommand = getCommandService().executeCommand(saveCommand);
        if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
            validateElement(saveCommand.getElement());
        }
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
    public <T extends CnATreeElement> T save(CnATreeElement container, Class<T> clazz) throws CommandException {
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
    public <T extends CnATreeElement> T save(CnATreeElement container, Class<T> clazz, String typeId) throws CommandException {
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
        if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
            validateElement(saveCommand.getNewElement());
        }
        return saveCommand.getNewElement();
    }

    public BausteinUmsetzung save(CnATreeElement container, Baustein baustein) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Creating new element, parent uuid: " + container.getUuid()); //$NON-NLS-1$
        }
        CreateBaustein saveCommand = new CreateBaustein(container, baustein);
        saveCommand = getCommandService().executeCommand(saveCommand);
        if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
            validateElement(saveCommand.getNewElement());
        }
        return saveCommand.getNewElement();
    }

    public CnALink createLink(CnATreeElement dropTarget, CnATreeElement dragged) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Saving new link from " + dropTarget.getUuid() + " to " + dragged.getUuid()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        CreateLink command = new CreateLink(dropTarget, dragged);
        command = getCommandService().executeCommand(command);
        return command.getLink();
    }

    public CnALink createLink(CnATreeElement dropTarget, CnATreeElement dragged, String typeId, String comment) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Saving new link from " + dropTarget.getUuid() + " to " + dragged.getUuid() + " of type " + typeId); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        CreateLink command = new CreateLink(dropTarget, dragged, typeId, comment);
        command = getCommandService().executeCommand(command);

        return command.getLink();
    }

    public void remove(CnATreeElement element) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting element, uuid: " + element.getUuid()); //$NON-NLS-1$
        }
        
        RemoveElement command = new RemoveElement(element);
        deleteValidations(element);
        getCommandService().executeCommand(command);
    }

    public void remove(CnALink element) throws CommandException {
        RemoveLink command = new RemoveLink(element);
        getCommandService().executeCommand(command);
    }

    public CnATreeElement update(CnATreeElement element) throws CommandException {
        UpdateElement command = new UpdateElement(element, true, ChangeLogEntry.STATION_ID);
        command = getCommandService().executeCommand(command);
        return (CnATreeElement) command.getElement();
    }
    
    /**
     * @param cnAElement
     * @throws Exception 
     */
    public CnATreeElement updateEntity(CnATreeElement element) throws CommandException {
        UpdateElementEntity<? extends CnATreeElement> command = createCommand(element);
        command = getCommandService().executeCommand(command);
        if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
            validateElement(command.getElement());
        }
        return command.getElement(); 
    }

    public void update(List<? extends CnATreeElement> elements) throws StaleObjectStateException, CommandException {
        UpdateMultipleElements command = new UpdateMultipleElements(elements, ChangeLogEntry.STATION_ID);
        command = getCommandService().executeCommand(command);
        if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
            for(Object o :  command.getChangedElements()){
                if(o instanceof CnATreeElement){
                    validateElement((CnATreeElement)o);
                }
            }
        }
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
    public BSIModel loadModel(IProgress nullMonitor) throws CommandException {
        log.debug("Loading model instance"); //$NON-NLS-1$

        nullMonitor.setTaskName(Messages.getString("CnAElementHome.1")); //$NON-NLS-1$

        LoadBSIModelForTreeView command = new LoadBSIModelForTreeView();
        command = getCommandService().executeCommand(command);
        return command.getModel();
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
        if (cte instanceof ITVerbund || cte instanceof Organization) {
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
        try {
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
            
            CnATreeElement elemntWithPermissions = Retriever.checkRetrievePermissions(cte);
            for (Permission p : elemntWithPermissions.getPermissions()) {
                if (p.isWriteAllowed() && roles.contains(p.getRole())) {
                    return true;
                }
            }
        } catch(Exception e) {
            log.error("Error while checking write permission.", e);
        }
        return false;
    }
    
    public void createLinksAccordingToBusinessLogic(final CnATreeElement dropTarget, final List<CnATreeElement> toDrop) {
        createLinksAccordingToBusinessLogic(dropTarget, toDrop, null);
    }
    
    public void createLinksAccordingToBusinessLogic(final CnATreeElement dropTarget, final List<CnATreeElement> toDrop, final String linkId) {
        if (log.isDebugEnabled()) {
            log.debug("createLink..."); //$NON-NLS-1$
        }
        
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                Activator.inheritVeriniceContextState();
                List<CnALink> newLinks = new ArrayList<CnALink>();
                allDragged: for (CnATreeElement dragged : toDrop) {
                    try {
                        // ISO 27k elements are only linked using XML-defined relations:
                        if (dropTarget instanceof IISO27kElement && dragged instanceof IISO27kElement) {

                            // special case: threats and vulnerabilities can create a new scenario when dropped:
                            specialISO27kDndHandling(dropTarget, dragged);
                            String linkIdParam = linkId;
                            if(linkIdParam==null) {
                                // use first relation type since param linkId is null
                                linkIdParam = getFirstLinkdId(dragged, dropTarget);
                            }
                            
                            // try to link from target to dragged elements first:                            
                            if (linkIdParam!=null) {
                                boolean linkCreated = createTypedLink(newLinks, dropTarget, dragged, linkIdParam, LINK_NO_COMMENT);
                                if (linkCreated){
                                    continue allDragged;
                                }
                            }
                                                   
                            if(linkIdParam==null) {
                                linkIdParam = getFirstLinkdId(dropTarget, dragged);
                            }
                            if ( linkIdParam!=null ) {
                                // use first relation type (user can change it later):
                                boolean linkCreated = createTypedLink(newLinks, dragged, dropTarget, linkIdParam, LINK_NO_COMMENT);
                                if (linkCreated){
                                    continue allDragged;
                                }
                            }
                        } // end for ISO 27k elements
                        
                        // backwards compatibility: BSI elements can be linked without a defined relation type, but we use one if present:
                        boolean bsiHandlingNeeded = dropTarget instanceof IBSIStrukturElement || dragged instanceof IBSIStrukturElement || dropTarget instanceof BausteinUmsetzung;
                        bsiHandlingNeeded = bsiHandlingNeeded || dragged instanceof BausteinUmsetzung || dropTarget instanceof MassnahmenUmsetzung;
                        bsiHandlingNeeded = bsiHandlingNeeded || dragged instanceof MassnahmenUmsetzung;
                        if ( bsiHandlingNeeded ) {
                            bsiElementLinkHandling(dropTarget, linkId, newLinks, dragged);
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
                    }
                }
                
                // calling linkAdded for one link reloads all changed links
                if(newLinks!=null && !newLinks.isEmpty()) {
                    CnALink link = newLinks.get(newLinks.size()-1);
                    if ((link.getDependant() instanceof IBSIStrukturElement || link.getDependant() instanceof MassnahmenUmsetzung)  || (link.getDependency() instanceof IBSIStrukturElement || link.getDependency() instanceof MassnahmenUmsetzung)) {
                        CnAElementFactory.getLoadedModel().linkAdded(link);
                    }
                    if (link.getDependant() instanceof IISO27kElement || link.getDependency() instanceof IISO27kElement) {
                        CnAElementFactory.getInstance().getISO27kModel().linkAdded(link);
                    }                 
                }
                DNDItems.clear();
            }

            private void bsiElementLinkHandling(final CnATreeElement dropTarget, final String linkId, List<CnALink> newLinks, CnATreeElement dragged) throws CommandException {
                CnATreeElement from = dropTarget;
                CnATreeElement to = dragged;
                String linkIdParam = linkId;
                if(linkIdParam==null) {
                    linkIdParam = getFirstLinkdId(to, from);
                }
                if (linkIdParam==null) {
                    // try again for reverse direction:
                    from = dragged;
                    to = dropTarget;
                    linkIdParam = getFirstLinkdId(to, from);
                }
                if (linkIdParam==null) {
                    //still nothing found, create untyped link:
                    CnALink link = CnAElementHome.getInstance().createLink(dropTarget, dragged);
                    newLinks.add(link);
                }
                else {
                    // create link with type:
                    createTypedLink(newLinks, from, to, linkIdParam, LINK_NO_COMMENT);
                }
            }

            private void specialISO27kDndHandling(final CnATreeElement dropTarget, CnATreeElement dragged) {
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
            }

            /**
             * @param dropTarget
             * @param dragged
             * @param linkIdParam
             * @return
             */
            private String getFirstLinkdId(final CnATreeElement dropTarget, CnATreeElement dragged) {
                String linkIdParam = null;
                // if none found: try reverse direction from dragged element to target (link is always modelled from one side only)
                Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory()
                    .getPossibleRelations(dragged.getEntityType().getId(), dropTarget.getEntityType().getId());
                if ( !possibleRelations.isEmpty()) {
                    linkIdParam = possibleRelations.iterator().next().getId();
                }
                return linkIdParam;
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
        if (!confirm){
            return;
        }
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
        if (link == null){
            return false;
        }
        newLinks.add(link);
        if (log.isDebugEnabled()){
            log.debug("Link created"); //$NON-NLS-1$
        }
        return true;
    }

    /**
     * Creates an specific update command for an element.
     * 
     * @param element
     * @return update command
     */
    private UpdateElementEntity<? extends CnATreeElement> createCommand(CnATreeElement element) {
        return new UpdateElementEntity<CnATreeElement>(element, ChangeLogEntry.STATION_ID);
    }

    private void validateElement(CnATreeElement elmt){
        getValidationService().createValidationForSingleElement(elmt);
        CnAElementFactory.getModel(elmt).validationAdded(elmt.getScopeId());
    }
    
    private IValidationService getValidationService(){
        if(validationService == null){
            validationService = ServiceFactory.lookupValidationService();
        }
        return validationService;
    }
    
    private void deleteValidations(CnATreeElement element){
        if(element.getScopeId() != null && element.getDbId() != null){
            getValidationService().deleteValidationsOfSubtree(element);
            CnAElementFactory.getModel(element).validationRemoved(element.getScopeId());
        } else {
            log.info("Can't delete validations of element, scopeId or elementId not set");
        }
    }

}
