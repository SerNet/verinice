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
import sernet.gs.ui.rcp.main.bsi.dialogs.TodoFilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.AuditDurchFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ZielobjektPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.views.GenericMassnahmenView;

public class AuditViewFilterAction extends Action {
	private Shell shell;
    private GenericMassnahmenView view;
	private MassnahmenUmsetzungFilter umsetzungFilter;
	private MassnahmenSiegelFilter siegelFilter;
	private AuditDurchFilter umsetzungDurchFilter;
	private ZielobjektPropertyFilter zielobjektFilter;

	public AuditViewFilterAction(GenericMassnahmenView view,
	        StructuredViewer viewer,
			String title,
			MassnahmenUmsetzungFilter filter1,
			MassnahmenSiegelFilter filter2) {
		super(title);
		this.view = view;
		shell = viewer.getControl().getShell();
		
		this.umsetzungFilter = filter1;
		this.siegelFilter = filter2;
		this.umsetzungDurchFilter = new AuditDurchFilter(viewer);
		this.zielobjektFilter = new ZielobjektPropertyFilter(viewer);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
		
	}
	
	@Override
	public void run() {
		TodoFilterDialog dialog = new TodoFilterDialog(shell, 
				umsetzungFilter.getUmsetzungPattern(),
				siegelFilter.getPattern(),
				umsetzungDurchFilter.getPattern(),
				zielobjektFilter.getPattern());
		if (dialog.open() != InputDialog.OK)
			return;
		
		umsetzungFilter.setUmsetzungPattern(dialog.getUmsetzungSelection());
		siegelFilter.setPattern(dialog.getSiegelSelection());
		umsetzungDurchFilter.setPattern(dialog.getUmsetzungDurch());
		zielobjektFilter.setPattern(dialog.getZielobjekt());
		view.loadBlockNumber=0;
		view.loadMoreAction.setEnabled(true);
		view.reloadMeasures();
		
	}
}
