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
package sernet.verinice.iso27k.rcp.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
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

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class AddElement implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;

	private static final Logger LOG = Logger.getLogger(AddElement.class);
	
	public static final Map<String, String> TITLE_FOR_TYPE;
	
	static {
		TITLE_FOR_TYPE = new HashMap<String, String>();
		TITLE_FOR_TYPE.put(AssetGroup.TYPE_ID, Messages.getString("AddElement.0")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(AuditGroup.TYPE_ID, Messages.getString("AddElement.1")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ControlGroup.TYPE_ID, Messages.getString("AddElement.2")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(DocumentGroup.TYPE_ID, Messages.getString("AddElement.3")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(EvidenceGroup.TYPE_ID, Messages.getString("AddElement.4")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ExceptionGroup.TYPE_ID, Messages.getString("AddElement.5")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(FindingGroup.TYPE_ID, Messages.getString("AddElement.6")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(IncidentGroup.TYPE_ID, Messages.getString("AddElement.7")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(IncidentScenarioGroup.TYPE_ID, Messages.getString("AddElement.8")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(InterviewGroup.TYPE_ID, Messages.getString("AddElement.9")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(PersonGroup.TYPE_ID, Messages.getString("AddElement.10")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ProcessGroup.TYPE_ID, Messages.getString("AddElement.11")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(RecordGroup.TYPE_ID, Messages.getString("AddElement.12")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(RequirementGroup.TYPE_ID, Messages.getString("AddElement.13")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ResponseGroup.TYPE_ID, Messages.getString("AddElement.14")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(ThreatGroup.TYPE_ID, Messages.getString("AddElement.15")); //$NON-NLS-1$
		TITLE_FOR_TYPE.put(VulnerabilityGroup.TYPE_ID, Messages.getString("AddElement.16")); //$NON-NLS-1$
        TITLE_FOR_TYPE.put(Asset.TYPE_ID, Messages.getString("AddElement.18")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		try {
			Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
			CnATreeElement newElement = null;

			if (sel instanceof IISO27kGroup) {
			    IISO27kGroup group = (IISO27kGroup) sel;
			    String childType = null;
				if(group.getChildTypes()!=null && group.getChildTypes().length>0) {
				    // TODO - getChildTypes()[0] problem for more than one type
				    childType = group.getChildTypes()[0];
				    if(group instanceof Asset) {
				        childType = Control.TYPE_ID;
				    }
				} else {
					LOG.error(Messages.getString("AddElement.17")); //$NON-NLS-1$
				}
				if(childType!=null) {
				    newElement = CnAElementFactory.getInstance().saveNew((CnATreeElement) group, childType, null);           		
				}
			}
			if (newElement != null) {
				EditorFactory.getInstance().updateAndOpenObject(newElement);
			}
		} catch (Exception e) {
			LOG.error("Could not add element", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.getString("AddElement.19")); //$NON-NLS-1$
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			boolean allowed = false;
			boolean enabled = false;		
			if (sel instanceof CnATreeElement) {
				allowed = CnAElementHome.getInstance().isNewChildAllowed((CnATreeElement) sel);           
			}
			if(sel instanceof Audit) {
				enabled = false;
			    action.setText(Messages.getString("AddElement.20"));
			} else if(sel instanceof IISO27kGroup) {
			    enabled = true;
			    IISO27kGroup group = (IISO27kGroup) sel;
				// TODO - getChildTypes()[0] might be a problem for more than one type
			    String childType = group.getChildTypes()[0];
                if(group instanceof Asset) {
                    childType = Control.TYPE_ID;
                }
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(childType)));	
				action.setText( TITLE_FOR_TYPE.get(group.getTypeId())!=null ? TITLE_FOR_TYPE.get(group.getTypeId()) : Messages.getString("AddElement.20") ); //$NON-NLS-1$
			}
			// Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(allowed && enabled);
            }
		}
	}
}
