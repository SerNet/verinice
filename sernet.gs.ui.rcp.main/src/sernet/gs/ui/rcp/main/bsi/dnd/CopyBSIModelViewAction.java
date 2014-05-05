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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;

/**
 * Copies modules  (Bausteine) from the BSI's GS-catalogues to be used elsewhere.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class CopyBSIModelViewAction extends Action {
	private BsiModelView view;

	public CopyBSIModelViewAction(BsiModelView view, String text) {
		super(text);
		this.view = view;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setToolTipText(Messages.getString("CopyBSIModelViewAction.0")); //$NON-NLS-1$
		
	}
	
	public void run() {
		CnPItems.setItems(view.getSelection().toList());
	}
	
}
