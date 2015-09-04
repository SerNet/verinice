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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.bsi.dnd.DNDHelper;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kGroupTransfer;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.iso27k.service.PasteService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.rcp.IProgressRunnable;
/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ElementViewDropPerformer extends ViewerDropAdapter implements DropPerformer, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(ElementViewDropPerformer.class);

    private GenericElementView elementView;

    private Object target = null;

    private boolean isActive = false;

    /**
     * @param elementView
     */
    public ElementViewDropPerformer(GenericElementView elementView, TreeViewer viewer) {
        super(viewer);
        this.elementView = elementView;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        // TODO Auto-generated method stub
        // get dragged items: DNDItems.getItems()

        if (LOG.isDebugEnabled()) {
            LOG.debug("performDrop...");
        } 


        try {

            if(!checkRights()){
                return false;
            }

            if (!validateDropObjects()) {
                return false;
            }
            if(!validateLinkTypes(data)){
                return false;
            }

            CnATreeElement groupToAdd = null;
            if(target instanceof Group) {
                groupToAdd = (CnATreeElement) target;
            } else {
                groupToAdd = elementView.getGroupToAdd();
            }

            CnATreeElement elementToLink = elementView.getElementToLink();
            PasteService task = new AuditCutService(groupToAdd, elementToLink, DNDHelper.arrayToList(data));
            IProgressRunnable operation = new PasteOperation(task,"{0} elements moved to group {1}",PreferenceConstants.INFO_ELEMENTS_CUT) ;
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            progressService.run(true, true, operation);
            operation.openInformation(); 
            elementView.reload();
            return true;
        } catch (Exception e) {
            LOG.error("Error while dropping items.", e);
            return false;
        }

    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        isActive = isSupportedData(transferType);
        return isActive;
    }

    /**
     * @param target
     * @return
     */
    private boolean validateDropObjects() {
        //validation is done in performDrop(), because data is not available here
        return true;
    }

    private boolean isSupportedData(TransferData transferData){
        return (ISO27kElementTransfer.getInstance().isSupportedType(transferData) ||
                ISO27kGroupTransfer.getInstance().isSupportedType(transferData));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(Object arg0) {
        return false;
    }

    private boolean validateLinkTypes(Object data){
        boolean valid = true;
        if(target instanceof CnATreeElement) {
            List<Object> list = new ArrayList<Object>();
            list.addAll(addDataToList(data));
            Set<String> draggedTypeSet = new HashSet<String>(0);
            for(Object object : list){
                if(object instanceof CnATreeElement){
                    CnATreeElement c = (CnATreeElement)object;
                    draggedTypeSet.add(c.getTypeId());
                }
            }
            CnATreeElement elementToLink = elementView.getElementToLink();
            String linkToType = elementToLink.getTypeId();
            for (Iterator<String> iterator = draggedTypeSet.iterator(); iterator.hasNext() && valid;) {
                valid = false;
                String typeId = iterator.next();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("validateDrop for target " + linkToType + " and drop " + typeId);
                }                         
                EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(linkToType);          
                Set<HuiRelation> relationSet = entityType.getPossibleRelations();
                for (HuiRelation huiRelation : relationSet) {
                    if (huiRelation.getTo().equals(typeId)) {
                        valid = true;
                        break;
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("validateDrop for target " + linkToType + ": " + valid);
            }
        }
        return valid;
    }

    private List<Object> addDataToList(Object data) {
        ArrayList<Object> retList = new ArrayList<Object>(0);
        if(data instanceof Object[]){
            Object[] o = (Object[])data;
            for(Object object : o){
                retList.add(object);
            }
        } else if (data instanceof Object){
            retList.add(data);
        }
        return retList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TREEDND;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // nothing to do
    }

}
