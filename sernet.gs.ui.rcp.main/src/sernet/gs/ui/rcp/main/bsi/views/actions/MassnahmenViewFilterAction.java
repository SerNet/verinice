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
package sernet.gs.ui.rcp.main.bsi.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.MassnahmenViewFilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.BSISchichtFilter;
import sernet.gs.ui.rcp.main.bsi.filter.BSISearchFilter;
import sernet.gs.ui.rcp.main.bsi.filter.GefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;

/**
 * Action that opens a dialog to let the user change filter settings
 * for the given view.
 * 
 * The action will also maintain the connection between filters and viewer.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class MassnahmenViewFilterAction extends Action {
	private Shell shell;
	private MassnahmenSiegelFilter siegelFilter;
	private BSISearchFilter suchFilter;
	private BSISchichtFilter schichtFilter;
	private GefaehrdungenFilter gefFilter;

	public MassnahmenViewFilterAction(StructuredViewer viewer,
			String title,
			MassnahmenSiegelFilter filter2,
			GefaehrdungenFilter gefFilter) {
		super(title);
		shell = viewer.getControl().getShell();
		this.siegelFilter = filter2;
		suchFilter = new BSISearchFilter(viewer);
		this.schichtFilter = new BSISchichtFilter(viewer);
		this.gefFilter = gefFilter;
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
	}
	
	@Override
	public void run() {
		MassnahmenViewFilterDialog dialog = new MassnahmenViewFilterDialog(shell,
				siegelFilter.getPattern(),
				suchFilter.getPattern(),
				schichtFilter.getPattern(),
				gefFilter.getPattern());
		
		if (dialog.open() != InputDialog.OK)
			return;
		
		siegelFilter.setPattern(dialog.getSiegelSelection());
		suchFilter.setPattern(dialog.getSuche());
		schichtFilter.setPattern(dialog.getSchichtSelection());
		gefFilter.setPattern(dialog.getGefFilterSelection());
	}
}
