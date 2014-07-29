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
package sernet.hui.swt.widgets;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * Behavior for controls which depends on the selection of a button (checkbox).
 * Control is enabled if checkbox is selected or vice versa if inverse is true.
 * 
 * This class is used by HitroUIView to implement 'depends' elements from
 * SNCA.xml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DependsButtonBehavior extends DependsBehavior implements IEditorBehavior {

    private Button controlDependsOn;

    private SelectionListener selectionListener = new SelectionListener() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Button button = (Button) e.getSource();
            boolean isValueSet = isValueSet(button, valueDependsOn);
            DependsButtonBehavior.this.control.setEnabled(isValueSet);
        }
    };

    public DependsButtonBehavior(Control control, Button controlDependsOn, String value) {
        this.control = control;
        this.controlDependsOn = controlDependsOn;
        this.valueDependsOn = value;
    }

    /**
     * @param control2
     */
    public DependsButtonBehavior(Button controlDependsOn) {
        this.controlDependsOn = controlDependsOn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.hui.swt.widgets.IEditorBehavior#init()
     */
    @Override
    public void init() {
        boolean isValueSet = isValueSet(controlDependsOn, valueDependsOn);
        control.setEnabled(isValueSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.hui.swt.widgets.IEditorBehavior#addBehavior()
     */
    @Override
    public void addBehavior() {
        controlDependsOn.addSelectionListener(selectionListener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.hui.swt.widgets.IEditorBehavior#removeBehavior()
     */
    @Override
    public void removeBehavior() {
        controlDependsOn.removeSelectionListener(selectionListener);
    }

    /**
     * @param control
     * @param valueDependsOn2
     * @return
     */
    protected boolean isValueSet(Button control, String valueDependsOn) {
        boolean result = (control.getSelection() && "1".equals(valueDependsOn)) 
                || (!control.getSelection() && "0".equals(valueDependsOn));
        if (inverse) {
            result = !result;
        }
        return result;
    }

}