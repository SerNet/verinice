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
import sernet.verinice.iso27k.model.ExceptionGroup;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.PersonGroup;
import sernet.verinice.iso27k.model.RequirementGroup;

/**
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
public class AddElement implements IObjectActionDelegate {
	private IWorkbenchPart targetPart;

	private static final Logger LOG = Logger.getLogger(AddElement.class);
	
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
				if(group.getChildTypes()!=null && group.getChildTypes().length==1) {
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
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO: dm - set the new icons here
		if(selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			if(sel instanceof PersonGroup) {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.PERSON)));	
				action.setText("New Person");
			} else if(sel instanceof AssetGroup) {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.UNKNOW_NEW)));	
				action.setText("New Asset");
			} else if(sel instanceof AuditGroup) {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.UNKNOW_NEW)));	
				action.setText("New Audit");
			} else if(sel instanceof ControlGroup) {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.UNKNOW_NEW)));	
				action.setText("New Control");
			} else if(sel instanceof ExceptionGroup) {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.UNKNOW_NEW)));	
				action.setText("New Exception");
			} else if(sel instanceof RequirementGroup) {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.UNKNOW_NEW)));	
				action.setText("New Requirement");
			} else {
				action.setImageDescriptor(ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(ImageCache.UNKNOW_NEW)));	
			}
		}
	}
}
