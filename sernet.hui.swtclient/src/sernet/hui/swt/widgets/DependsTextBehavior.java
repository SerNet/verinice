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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Behavior for controls which depends on the input of a textfield.
 * Control is enabled if text input is 'valueDependsOn' or vice versa if inverse is true.
 * 
 * This class is used by HitroUIView to implement 'depends' elements from
 * SNCA.xml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DependsTextBehavior extends DependsBehavior implements IEditorBehavior {

    private Text controlDependsOn;

    private ModifyListener modifyListener = new ModifyListener() {     
        @Override
        public void modifyText(ModifyEvent e) {
            Text text = (Text) e.getSource();
            if(DependsTextBehavior.this.control!=null) {
                boolean isValueSet = isValueSet(text,valueDependsOn);
                DependsTextBehavior.this.control.setEnabled(isValueSet); 
            }
        }
    };
    
    public DependsTextBehavior(Control control, Text controlDependsOn, String value) {
        this.control = control;
        this.controlDependsOn = controlDependsOn;
        this.valueDependsOn = value;
    }

    /**
     * @param control2
     */
    public DependsTextBehavior(Text text) {
        this.controlDependsOn = text;
    }

    /* (non-Javadoc)
     * @see sernet.hui.swt.widgets.IEditorBehavior#init()
     */
    @Override
    public void init() {
        boolean isValueSet = isValueSet(controlDependsOn,valueDependsOn);
        control.setEnabled(isValueSet);
    }

    /* (non-Javadoc)
     * @see sernet.hui.swt.widgets.IEditorBehavior#addBehavior()
     */
    @Override
    public void addBehavior() {
        controlDependsOn.addModifyListener(modifyListener);        
    }

    /* (non-Javadoc)
     * @see sernet.hui.swt.widgets.IEditorBehavior#removeBehavior()
     */
    @Override
    public void removeBehavior() {       
        controlDependsOn.removeModifyListener(modifyListener);       
    }
    

    protected boolean isValueSet(Text control, String valueDependsOn) {
        boolean result = valueDependsOn.equals(control.getText());
        if(inverse) {
            result = !result;
        }
        if(result && getNext()!=null) {
            DependsTextBehavior next = (DependsTextBehavior)getNext();
            result = next.isValueSet(control, next.valueDependsOn);
        }
        return result;
    }

}