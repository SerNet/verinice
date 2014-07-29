/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class SWTElementFactory {
    
    public static Button generateRadioButton(Composite composite, String text, Boolean selection, SelectionListener listener){
        Button button = new Button(composite, SWT.RADIO);
        return setButtonAttributes(button, text, selection, listener);
    }
    
    public static Button generateCheckboxButton(Composite composite, String text, Boolean selection, SelectionListener listener){
        Button button = new Button(composite, SWT.CHECK);
        return setButtonAttributes(button, text, selection, listener);
    }

    public static Button generatePushButton(Composite composite, String text, Boolean selection, SelectionListener listener){
        Button button = new Button(composite, SWT.PUSH);
        return setButtonAttributes(button, text, selection, listener);
    }

    public static Button generateButton(Composite composite, String text, Boolean selection, SelectionListener listener){
        Button button = new Button(composite, SWT.NONE);
        return setButtonAttributes(button, text, selection, listener);
    }

    
    private static Button setButtonAttributes(Button button, String text, Boolean selection, SelectionListener listener){
        button.setText(text);
        button.setSelection((selection != null) ? selection.booleanValue() : button.getSelection());
        if(listener != null){
            button.addSelectionListener(listener);
        }        
        return button;
    }
    
    

}
