/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.NotSufficientRightsException;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CnATreeElementBuildException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpDocumentGroup;
import sernet.verinice.model.bp.groups.BpIncidentGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRecordGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.rcp.RightsEnabledHandler;

/**
 * THis handler creates new groups for ISO2700 and base protection
 * elements. 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class AddGroupHandler extends RightsEnabledHandler implements IElementUpdater {
	
    private static final Logger LOG = Logger.getLogger(AddGroupHandler.class);	
	protected static final Map<String, String> TITLE_FOR_TYPE;
	
	static {
        TITLE_FOR_TYPE = new HashMap<>();
        // ISO27000 
        TITLE_FOR_TYPE.put(AssetGroup.TYPE_ID, Messages.AddGroup_0); 
        TITLE_FOR_TYPE.put(AuditGroup.TYPE_ID, Messages.AddGroup_1);
        TITLE_FOR_TYPE.put(ControlGroup.TYPE_ID, Messages.AddGroup_2);
        TITLE_FOR_TYPE.put(DocumentGroup.TYPE_ID, Messages.AddGroup_3);
        TITLE_FOR_TYPE.put(EvidenceGroup.TYPE_ID, Messages.AddGroup_4);
        TITLE_FOR_TYPE.put(ExceptionGroup.TYPE_ID, Messages.AddGroup_5);
        TITLE_FOR_TYPE.put(FindingGroup.TYPE_ID, Messages.AddGroup_6);
        TITLE_FOR_TYPE.put(IncidentGroup.TYPE_ID, Messages.AddGroup_7);
        TITLE_FOR_TYPE.put(IncidentScenarioGroup.TYPE_ID, Messages.AddGroup_8);
        TITLE_FOR_TYPE.put(InterviewGroup.TYPE_ID, Messages.AddGroup_9);
        TITLE_FOR_TYPE.put(PersonGroup.TYPE_ID, Messages.AddGroup_10);
        TITLE_FOR_TYPE.put(ProcessGroup.TYPE_ID, Messages.AddGroup_11);
        TITLE_FOR_TYPE.put(RecordGroup.TYPE_ID, Messages.AddGroup_12);
        TITLE_FOR_TYPE.put(RequirementGroup.TYPE_ID, Messages.AddGroup_13);
        TITLE_FOR_TYPE.put(ResponseGroup.TYPE_ID, Messages.AddGroup_14);
        TITLE_FOR_TYPE.put(ThreatGroup.TYPE_ID, Messages.AddGroup_15);
        TITLE_FOR_TYPE.put(VulnerabilityGroup.TYPE_ID, Messages.AddGroup_16);
        TITLE_FOR_TYPE.put(Asset.TYPE_ID, Messages.AddGroup_17);
        // Base protection
        TITLE_FOR_TYPE.put(ApplicationGroup.TYPE_ID, Messages.AddGroupHandler_application);
        TITLE_FOR_TYPE.put(BpPersonGroup.TYPE_ID, Messages.AddGroupHandler_group);
        TITLE_FOR_TYPE.put(BpRequirementGroup.TYPE_ID, Messages.AddGroupHandler_requirement);
        TITLE_FOR_TYPE.put(BpThreatGroup.TYPE_ID, Messages.AddGroupHandler_threat);
        TITLE_FOR_TYPE.put(BusinessProcessGroup.TYPE_ID, Messages.AddGroupHandler_business_process);
        TITLE_FOR_TYPE.put(DeviceGroup.TYPE_ID, Messages.AddGroupHandler_device);
        TITLE_FOR_TYPE.put(IcsSystemGroup.TYPE_ID, Messages.AddGroupHandler_ics_system);
        TITLE_FOR_TYPE.put(ItSystemGroup.TYPE_ID, Messages.AddGroupHandler_it_system);
        TITLE_FOR_TYPE.put(NetworkGroup.TYPE_ID, Messages.AddGroupHandler_network);
        TITLE_FOR_TYPE.put(RoomGroup.TYPE_ID, Messages.AddGroupHandler_room);
        TITLE_FOR_TYPE.put(SafeguardGroup.TYPE_ID, Messages.AddGroupHandler_safeguard);
        TITLE_FOR_TYPE.put(BpDocumentGroup.TYPE_ID, Messages.AddGroupHandler_document);
        TITLE_FOR_TYPE.put(BpIncidentGroup.TYPE_ID, Messages.AddGroupHandler_incident);
        TITLE_FOR_TYPE.put(BpRecordGroup.TYPE_ID, Messages.AddGroupHandler_record);
    }
	
	private CnATreeElement parent;
    
    private String typeId;
	
    public AddGroupHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            if(checkRights()){
                parent = getSelectedElement(event);                       
                createGroup();
            } else {
                throw new NotSufficientRightsException("Action not allowed for user"); //$NON-NLS-1$
            }
        } catch (NotSufficientRightsException e){
            LOG.error("Could not add element", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddGroupHandler_permission_error); 
        } catch (Exception e) {
            LOG.error("Could not add element group", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddGroupHandler_error);
        }
        return null;
    }

    protected void createGroup() throws CommandException, CnATreeElementBuildException {
        CnATreeElement newGroup = null;
        if( parent != null) {          
            String groupTypeId = this.typeId;
            if(groupTypeId==null) {
                // child groups have the same type as parents
                groupTypeId = parent.getTypeId();
                if(parent instanceof Asset) {
                    groupTypeId = ControlGroup.TYPE_ID;
                }
            }
            boolean inheritIcon = Activator.getDefault().getPreferenceStore()
                    .getBoolean(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON);
            newGroup = CnAElementFactory.getInstance().saveNew((CnATreeElement) parent, groupTypeId, null, inheritIcon);       
        }
        if (newGroup != null) {
            EditorFactory.getInstance().openEditor(newGroup);
        }
    }

    protected CnATreeElement getSelectedElement(ExecutionEvent event) {
        CnATreeElement element = null;
        final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);         
        Object sel = selection.getFirstElement();
        if(sel instanceof IISO27kGroup || sel instanceof IBpGroup) {
            element = (CnATreeElement) sel;
        }
        return element;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void updateElement(UIElement menu, Map arg1) {
        CnATreeElement selectedElement = getSelectedElement();
        if(selectedElement!=null) {
            configureMenu(menu, selectedElement);
        }    
    }
    
    private void configureMenu(UIElement menu, CnATreeElement selectedElement) {
        boolean allowed = CnAElementHome.getInstance().isNewChildAllowed(selectedElement);
        boolean enabled = false;         
        if(selectedElement instanceof Audit) {
            enabled = false;
            menu.setText(Messages.AddGroupHandler_new_group); 
        } else if(selectedElement instanceof Group<?>) {
            enabled = true;
            Group<?> group = (Group<?>) selectedElement;
            String childTypeId = group.getChildTypes()[0];
            if(selectedElement instanceof Asset) {
                childTypeId = Control.TYPE_ID;
            }
            menu.setIcon(ImageDescriptor.createFromImage(ImageCache.getInstance().getImageForTypeId(childTypeId)));   
            menu.setText( TITLE_FOR_TYPE.get(group.getTypeId())!=null ? TITLE_FOR_TYPE.get(group.getTypeId()) : Messages.AddGroupHandler_new_group );
        } 
        // Only change state when it is enabled, since we do not want to
        // trash the enablement settings of plugin.xml
        if (this.isEnabled()) {
            this.setEnabled(allowed && enabled);
        }     
    }

    private CnATreeElement getSelectedElement() {
        CnATreeElement element = null;
        ISelection selection = getSelection();
        if(selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if (sel instanceof CnATreeElement) {
                element = (CnATreeElement) sel;
            }
        }
        return element;
    }

    public ISelection getSelection() {
        Activator activator = Activator.getDefault();
        IWorkbench workbench = activator.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        ISelectionService selectionService = workbenchWindow.getSelectionService();
        return selectionService.getSelection();
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

}
