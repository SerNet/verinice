/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.model.common.TypeParameter;
import sernet.verinice.model.common.TagParameter;
import sernet.verinice.iso27k.rcp.ISMViewFilterDialog;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class ISMViewFilter extends Action {
    
    private static final Logger LOG = Logger.getLogger(ISMViewFilter.class);
    
    private Shell shell;
    private TagParameter tagParameter;
    private HideEmptyFilter hideEmptyFilter;
    private TypeParameter typeParameter;

    public ISMViewFilter(
            StructuredViewer viewer, 
            String title, 
            TagParameter tagFilter, 
            HideEmptyFilter hideEmptyFilter,
            TypeParameter typeFilter) {
        super(title, SWT.TOGGLE);
        shell = new Shell();
        this.tagParameter = tagFilter;
        this.hideEmptyFilter = hideEmptyFilter;
        this.typeParameter = typeFilter;
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
        setUpCheckStatus();
    }
    
    public void setUpCheckStatus() {
        this.setChecked(tagParameter.isActive() || hideEmptyFilter.isActive());
    }

    @Override
    public void run() {
        ISMViewFilterDialog dialog = new ISMViewFilterDialog(shell, this);
        if (dialog.open() == InputDialog.OK) {
            tagParameter.setPattern(dialog.getCheckedElements());
            tagParameter.setFilterOrgs(dialog.getFilterOrgs());
            hideEmptyFilter.setHideEmpty(dialog.getHideEmpty());
            typeParameter.setVisibleTypeSet(dialog.getVisibleTypes());
            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask("Activating filter...", IProgressMonitor.UNKNOWN);
                        CnAElementFactory.getInstance().reloadModelFromDatabase();
                        monitor.done();
                    }
                 });
            } catch (Exception e) {
                LOG.error("Error while activating filter", e);
            } 
            
        }
        setUpCheckStatus();
    }

    public TagParameter getTagParameter() {
        return tagParameter;
    }

    public HideEmptyFilter getHideEmptyFilter() {
        return hideEmptyFilter;
    }
    
    public TypeParameter getTypeParameter() {
        return typeParameter;
    }
}
