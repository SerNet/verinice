/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.SWTElementFactory;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PersonTypeSelectDialog extends Dialog {

    private String elementType = PersonIso.TYPE_ID;
    
    public PersonTypeSelectDialog(Shell shell) {
        super(shell);
    }
        
    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.PersonTypeSelectDialog_0);
        Composite container = (Composite) super.createDialogArea(parent);
        SelectionAdapter ismSelectionAdapter = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                if(e.getSource() instanceof Button){
                    elementType = PersonIso.TYPE_ID;
                }
            }
        };
        SelectionAdapter bsiSelectionAdapter = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                if(e.getSource() instanceof Button){
                    elementType = Person.TYPE_ID;
                }
            }
        };       
        Label label = new Label(container, SWT.LEFT);
        label.setText(Messages.PersonTypeSelectDialog_1);
        SWTElementFactory.generateRadioButton(container, Messages.PersonTypeSelectDialog_2, true, ismSelectionAdapter);
        SWTElementFactory.generateRadioButton(container, Messages.PersonTypeSelectDialog_3, false, bsiSelectionAdapter);
        parent.pack();
        return container;
    }

    public String getElementType() {
        return elementType;
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
}
