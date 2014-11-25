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
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BSIModelFilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.StringPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;

/**
 * Sets filters for the BSI model view.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class BSIModelViewFilterAction extends Action {
	private Shell shell;
	private MassnahmenUmsetzungFilter umsetzungFilter;
	private MassnahmenSiegelFilter siegelFilter;
	private StringPropertyFilter lebenszyklusFilter;
	private StringPropertyFilter objektLebenszyklusFilter;
	private BSIModelElementFilter elementFilter;
	private TagFilter tagFilter;
	

	public BSIModelViewFilterAction(
			String title,
			MassnahmenUmsetzungFilter filter1,
			MassnahmenSiegelFilter filter2,
			StringPropertyFilter filter3,
			StringPropertyFilter filter5,
			BSIModelElementFilter filter4,
			TagFilter filter6) {
		super(title);
		shell = new Shell();
		
		this.umsetzungFilter = filter1;
		this.siegelFilter = filter2;
		this.lebenszyklusFilter = filter3;
		this.elementFilter = filter4;
		this.objektLebenszyklusFilter = filter5;
		this.tagFilter = filter6;
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
	
	}
	
	
	@Override
	public void run() {
		BSIModelFilterDialog dialog = new BSIModelFilterDialog(shell,
				umsetzungFilter.getUmsetzungPattern(),
				siegelFilter.getPattern(),
				lebenszyklusFilter.getPattern(),
				objektLebenszyklusFilter.getPattern(),
				elementFilter.getFilteredClasses(),
				tagFilter.getPattern(),
	            tagFilter.isFilterItVerbund());

		
		if (dialog.open() != InputDialog.OK){
			return;
		}
		umsetzungFilter.setUmsetzungPattern(dialog.getUmsetzungSelection());
		siegelFilter.setPattern(dialog.getSiegelSelection());
		
		lebenszyklusFilter.setPattern(dialog.getLebenszyklus());
		objektLebenszyklusFilter.setPattern(dialog.getObjektLebenszyklus());
		
		elementFilter.setFilteredClasses(dialog.getFilteredClasses());
		
		tagFilter.setFilterItVerbund(dialog.isFilterItVerbund());
		tagFilter.setPattern(dialog.getCheckedElements());
		
		
	}
}
