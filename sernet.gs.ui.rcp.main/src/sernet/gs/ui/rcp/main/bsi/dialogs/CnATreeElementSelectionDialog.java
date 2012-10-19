/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.ElementSelectionComponent;

/**
 * Dialog to allow speedy selection of multiple elements of a given type, with filter function.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CnATreeElementSelectionDialog extends Dialog {

    private String entityType;

    private CnATreeElement inputElmt;
    
    private ElementSelectionComponent selectionComponent;
    
    private boolean scopeOnly=true, showScopeCheckbox=true;
    
    /**
     * @param shell
     * @param selectedType
     */
    public CnATreeElementSelectionDialog(Shell shell, String selectedType, CnATreeElement inputElmt) {
        super(shell);
        setShellStyle(SWT.MAX | SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
        this.entityType = selectedType;
        this.inputElmt = inputElmt;
        
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.CnATreeElementSelectionDialog_2);
        newShell.setSize(400, 500);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-200, cursorLocation.y-250));
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        selectionComponent = new ElementSelectionComponent(container, this.entityType, this.inputElmt);
        selectionComponent.setScopeOnly(scopeOnly);
        selectionComponent.setShowScopeCheckbox(showScopeCheckbox);
        selectionComponent.init();
        selectionComponent.loadElements();

        selectionComponent.getViewer().addDoubleClickListener(new IDoubleClickListener() {           
            @Override
            public void doubleClick(DoubleClickEvent event) {
                close();
            }
        });
        return selectionComponent.getContainer();
    }

    /**
     * @return
     */
    public List<CnATreeElement> getSelectedElements() {
        return selectionComponent.getSelectedElements();
    }

    public void setScopeOnly(boolean scopeOnly) {
        this.scopeOnly = scopeOnly;
        if(selectionComponent!=null) {
            selectionComponent.setScopeOnly(scopeOnly);
        }
    }

    public void setShowScopeCheckbox(boolean showScopeCheckbox) {
        this.showScopeCheckbox = showScopeCheckbox;
        if(selectionComponent!=null) {
            selectionComponent.setShowScopeCheckbox(showScopeCheckbox);
        }
    }

}


