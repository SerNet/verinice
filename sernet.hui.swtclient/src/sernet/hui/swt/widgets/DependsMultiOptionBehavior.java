/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.hui.swt.widgets;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Behavior for controls which depends on the input of a multioption-field.
 * Control is enabled if text input is 'valueDependsOn' or vice versa if inverse is true.
 * 
 * This class is used by HitroUIView to implement 'depends' elements from
 * SNCA.xml
 * 
 * Class extends {@link DependsTextBehavior} to just overwrite 
 * the method that checks if the value to depend on is part of the selection
 * in the multiselection control
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class DependsMultiOptionBehavior extends DependsTextBehavior {

    public DependsMultiOptionBehavior(Control control, Text controlDependsOn, String value) {
        super(control, controlDependsOn, value);
    }
    
    public DependsMultiOptionBehavior(Text text) {
        super(text);
    }
    
    /**
     * valueDependsOn can be part of a string that contains several options 
     * (comma separated values), so in contrast to {@link DependsTextBehavior}
     * control.getText() is checked, if it contains the value
     * to check for 
     */
    @Override
    protected boolean isValueSet(Text control, String valueDependsOn) {
        boolean result = control.getText().contains(valueDependsOn);
        if(inverse) {
            result = !result;
        }
        if(result && getNext()!=null) {
            IEditorBehavior next = getNext();
            if(next instanceof DependsMultiOptionBehavior 
                    || next instanceof DependsTextBehavior){
                result = ((DependsTextBehavior)next).isValueSet(control, 
                        ((DependsTextBehavior)next).valueDependsOn);
            }
        }
        return result;
    }

}
