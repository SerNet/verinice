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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.iso27k.service.PasteService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.rcp.IProgressRunnable;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ElementViewDropPerformer implements DropPerformer {

    private static final Logger LOG = Logger.getLogger(ElementViewDropPerformer.class);
    
    GenericElementView elementView;
    
    /**
     * @param elementView
     */
    public ElementViewDropPerformer(GenericElementView elementView) {
        this.elementView = elementView;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
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
            if (!validateDropObjects(target)) {
                return false;
            }
            
            CnATreeElement groupToAdd = null;
            if(target instanceof Group) {
                groupToAdd = (CnATreeElement) target;
            } else {
                groupToAdd = elementView.getGroupToAdd();
            }
            
            CnATreeElement elementToLink = elementView.getElementToLink();
            PasteService task = new AuditCutService(groupToAdd, elementToLink, DNDItems.getItems());
            IProgressRunnable operation = new PasteOperation(task,"{0} elements moved to group {1}",PreferenceConstants.INFO_ELEMENTS_CUT) ;
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            progressService.run(true, true, operation);
            operation.openInformation(); 
            if(elementView!=null) {
                elementView.reload();
            }
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
        boolean valid = true;
        if(target instanceof CnATreeElement) {
            Set<String> draggedTypeSet = DNDItems.getTypes();
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
    
    /**
     * @param target
     * @return
     */
    private boolean validateDropObjects(Object target) {
        // TODO Auto-generated method stub
        return true;
    }

}
