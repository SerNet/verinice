/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.rcp.ISMViewFilterDialog;


/**
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ISMViewFilter extends Action {
	private Shell shell;
	private TagFilter tagFilter;
	

	public ISMViewFilter(StructuredViewer viewer,
			String title,
			TagFilter tagFilter) {
		super(title,SWT.TOGGLE);
		shell = new Shell();
		this.tagFilter = tagFilter;
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
	}
	
	
	@Override
	public void run() {
		ISMViewFilterDialog dialog = new ISMViewFilterDialog(shell,tagFilter.getPattern());
		if (dialog.open() == InputDialog.OK) {
			tagFilter.setPattern(dialog.getCheckedElements());
		}
		this.setChecked(tagFilter.getPattern()!=null && tagFilter.getPattern().length>0);
	}
}
