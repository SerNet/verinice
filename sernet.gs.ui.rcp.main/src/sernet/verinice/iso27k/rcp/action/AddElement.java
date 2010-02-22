/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
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
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.verinice.iso27k.model.AssetGroup;
import sernet.verinice.iso27k.model.AuditGroup;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.DocumentGroup;
import sernet.verinice.iso27k.model.EvidenceGroup;
import sernet.verinice.iso27k.model.ExceptionGroup;
import sernet.verinice.iso27k.model.FindingGroup;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.IncidentGroup;
import sernet.verinice.iso27k.model.IncidentScenarioGroup;
import sernet.verinice.iso27k.model.InterviewGroup;
import sernet.verinice.iso27k.model.PersonGroup;
import sernet.verinice.iso27k.model.ProcessGroup;
import sernet.verinice.iso27k.model.RecordGroup;
import sernet.verinice.iso27k.model.RequirementGroup;
import sernet.verinice.iso27k.model.ResponseGroup;
import sernet.verinice.iso27k.model.ThreatGroup;
import sernet.verinice.iso27k.model.VulnerabilityGroup;

/**
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
public class AddElement implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;

	private static final Logger LOG = Logger.getLogger(AddElement.class);
	
	private static final Map<String, String> TITLE_FOR_TYPE;
	
	static {
		TITLE_FOR_TYPE = new HashMap<String, String>();
		TITLE_FOR_TYPE.put(AssetGroup.TYPE_ID, "Add Asset...");
		TITLE_FOR_TYPE.put(AuditGroup.TYPE_ID, "Add Audit...");
		TITLE_FOR_TYPE.put(ControlGroup.TYPE_ID, "Add Control...");
		TITLE_FOR_TYPE.put(DocumentGroup.TYPE_ID, "Add Document...");
		TITLE_FOR_TYPE.put(EvidenceGroup.TYPE_ID, "Add Evidence...");
		TITLE_FOR_TYPE.put(ExceptionGroup.TYPE_ID, "Add Exception...");
		TITLE_FOR_TYPE.put(FindingGroup.TYPE_ID, "Add Improvement Note...");
		TITLE_FOR_TYPE.put(IncidentGroup.TYPE_ID, "Add Incident...");
		TITLE_FOR_TYPE.put(IncidentScenarioGroup.TYPE_ID, "Add Incident Scenario...");
		TITLE_FOR_TYPE.put(InterviewGroup.TYPE_ID, "Add Interview...");
		TITLE_FOR_TYPE.put(PersonGroup.TYPE_ID, "Add Person...");
		TITLE_FOR_TYPE.put(ProcessGroup.TYPE_ID, "Add Process...");
		TITLE_FOR_TYPE.put(RecordGroup.TYPE_ID, "Add Record...");
		TITLE_FOR_TYPE.put(RequirementGroup.TYPE_ID, "Add Requirement...");
		TITLE_FOR_TYPE.put(ResponseGroup.TYPE_ID, "Add Response...");
		TITLE_FOR_TYPE.put(ThreatGroup.TYPE_ID, "Add Threat...");
		TITLE_FOR_TYPE.put(VulnerabilityGroup.TYPE_ID, "Add Vulnerability...");
	}
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		try {
			Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
			CnATreeElement newElement = null;

			if (sel instanceof Group) {
				Group group = (Group) sel;
				if(group.getChildTypes()!=null && group.getChildTypes().length>0) {
					// TODO: Fix this for group.getChildTypes().length > 1
					newElement = CnAElementFactory.getInstance().saveNew(group, group.getChildTypes()[0], null);
				} else {
					LOG.error("Can not determine child type");
				}			
			}
			if (newElement != null) {
				EditorFactory.getInstance().openEditor(newElement);
			}
		} catch (Exception e) {
			LOG.error("Could not add asset", e);
			ExceptionUtil.log(e, "Could not add asset");
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO: dm - set the new icons here
		if(selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			if(sel instanceof Group) {
				Group group = (Group) sel;
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getISO27kTypeImage(group.getChildTypes()[0])));	
				action.setText( TITLE_FOR_TYPE.get(group.getTypeId())!=null ? TITLE_FOR_TYPE.get(group.getTypeId()) : "New Object" );
			}
			
		}
	}
}
