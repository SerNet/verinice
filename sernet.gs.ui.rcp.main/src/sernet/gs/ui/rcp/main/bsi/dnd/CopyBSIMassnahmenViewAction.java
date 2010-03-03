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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.views.BSIMassnahmenView;
import sernet.verinice.iso27k.rcp.CnPItems;

/**
 * Copies modules (Bausteine) from the BSI's GS-catalogues to be used elsewhere.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class CopyBSIMassnahmenViewAction extends Action {
	private BSIMassnahmenView view;

	public CopyBSIMassnahmenViewAction(BSIMassnahmenView view, String text) {
		super(text);
		this.view = view;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setToolTipText(Messages.getString("CopyBSIMassnahmenViewAction.0")); //$NON-NLS-1$
	}

	public void run() {
		List<Baustein> bausteine = view.getSelectedBausteine();
		if (bausteine.size() > 0) {
			CnPItems.clearCutItems();
			CnPItems.clearCopyItems();
			CnPItems.setCopyItems(bausteine);
		}
	}

}
