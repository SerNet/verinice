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
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dialogs.SanityCheckDialog;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementBuildException;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.LinkKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * Handles drop events of objects to create links between them.
 * Also creates module (baustein) instances when a module is applied to a target object.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class BSIModelViewDropPerformer implements DropPerformer, RightEnabledUserInteraction {

	private static final Logger LOG = Logger.getLogger(BSIModelViewDropPerformer.class);
	
	private boolean isActive = false;
	
	public BSIModelViewDropPerformer() {
	}

	public boolean performDrop(Object data, Object target, Viewer viewer ) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("performDrop...");
		}
		Object toDrop = DNDItems.getItems().get(0);
		if(isActive()) {
			if (toDrop instanceof Baustein) {
				return dropBaustein((CnATreeElement) target, viewer);
			} else if (toDrop != null && (toDrop instanceof IBSIStrukturElement || toDrop instanceof BausteinUmsetzung || toDrop instanceof IISO27kElement)) {
				CnATreeElement element;
				if (target instanceof LinkKategorie){
					element = ((LinkKategorie) target).getParent();
				} else {
					element = (CnATreeElement) target;
				}
				LinkDropper dropper = new LinkDropper();
				return dropper.dropLink(DNDItems.getItems(), element);
			}
		}
		return false;

	}

	private boolean dropBaustein(final CnATreeElement target, Viewer viewer) {
		if (!CnAElementHome.getInstance().isNewChildAllowed(target)){
			return false;
		}
		final List<Baustein> toDrop = DNDItems.getItems();
		Check: for (Baustein baustein : toDrop) {
			int targetSchicht = 0;
			if (target instanceof IBSIStrukturElement){
				targetSchicht = ((IBSIStrukturElement) target).getSchicht();
			}
			if (baustein.getSchicht() != targetSchicht) {
				if (!SanityCheckDialog.checkLayer(viewer.getControl().getShell(), baustein.getSchicht(),
						targetSchicht)){
					return false;
				} else {
					break Check; // user say he knows what he's doing, stop
				}
				// checking.
			}

		}

		try {
			Job dropJob = new Job(Messages.getString("BSIModelViewDropListener.3")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Activator.inheritVeriniceContextState();
					
					try {
						createBausteinUmsetzung(toDrop, target);
					} catch (Exception e) {
						Logger.getLogger(this.getClass()).error("Drop failed", e); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					}
					DNDItems.clear();
					return Status.OK_STATUS;
				}
			};
			dropJob.setUser(true);
			dropJob.setSystem(false);
			dropJob.schedule();
		} catch (Exception e) {
			LOG.error(Messages.getString("BSIModelViewDropListener.5"), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void createBausteinUmsetzung(List<Baustein> toDrop, CnATreeElement target) throws CommandException, CnATreeElementBuildException {
		CnATreeElement saveNew = null;
		for (Baustein baustein : toDrop) {
			saveNew = CnAElementFactory.getInstance().saveNew(target,
					BausteinUmsetzung.TYPE_ID,
					new BuildInput<Baustein>(baustein),
					false /* do not notify single elements*/);
		}
		// notifying for the last element is sufficient to update all views:
		CnAElementFactory.getLoadedModel().childAdded(target,saveNew);
	}

	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target);
		}
	    
	    if(!checkRights()){
	        return false;
	    }
		if(!dropValidationTargetCheck(target)){
		    isActive = false;
		    return isActive;
		}

		List items = DNDItems.getItems();

		if(items==null || items.isEmpty()) {
			isActive=false;
			return isActive;
		}
		
		// use bstUms as template for bstUmsTarget
		if (items.get(0) instanceof BausteinUmsetzung) {
			BausteinUmsetzung sourceBst = (BausteinUmsetzung) DNDItems.getItems().get(0);
			if (target instanceof BausteinUmsetzung) {
				BausteinUmsetzung targetBst = (BausteinUmsetzung) target;
				if (targetBst.getKapitel().equals(sourceBst.getKapitel())){
					isActive=true;
				    return isActive;
				}
			}
			if (target instanceof IBSIStrukturElement) {
			    isActive=true;
			    return isActive;
			}
			isActive=false;
			return isActive;
		}

		// link drop:
		if (items.get(0) instanceof IBSIStrukturElement || items.get(0) instanceof IISO27kElement) {
			for (Object item : items) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("validateDrop, dragged item: " + item );
				}
				if(!doItemAndTargetMatch(target, item)){
				    isActive = false;
				    return isActive;
				}
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("validateDrop, validated!");
			}
			isActive=true;
			return isActive;
		}

		// other drop type:
		if (!(target instanceof CnATreeElement)){
			isActive=false;
		    return isActive;
		}
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			CnATreeElement cont = (CnATreeElement) target;
			if (!cont.canContain(obj)){
				isActive=false;
				return isActive;
			}

		}
		isActive=true;
		return isActive;
	}

    private boolean doItemAndTargetMatch(Object target, Object item) {
        if (target.equals(item)){
        	return false;
        } else if (!(item instanceof IBSIStrukturElement || item instanceof IISO27kElement)){
        	return false;
        } else if (item instanceof IBSIStrukturElement && target instanceof LinkKategorie &&
        	((LinkKategorie) target).getParent().equals(item)){ /* is same object */
        		return false;
        }
        return true;
    }

    private boolean dropValidationTargetCheck(Object target) {
        if (target == null){
			return false;
        } else if (!(target instanceof CnATreeElement || target instanceof LinkKategorie)){
			return false;
        } else if (target instanceof IBSIStrukturKategorie){
			return false;
        } 
        return true;
    }

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
	 */
	public boolean isActive() {
		return isActive;
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
        // Do nothing
    }

}
