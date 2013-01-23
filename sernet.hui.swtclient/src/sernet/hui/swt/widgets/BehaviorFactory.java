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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * Factory to create DependsBehavior instances for Controls.
 *
 * This class is used by HitroUIView to implement 'depends' elements from
 * SNCA.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class BehaviorFactory {
    
    private BehaviorFactory(){}

    public static DependsBehavior createBehaviorForControl(Control control) {
        DependsBehavior behavior = null;
        if(control instanceof Button) {
            behavior = new DependsButtonBehavior((Button) control);
        }
        if(control instanceof Text) {
            behavior = new DependsTextBehavior((Text) control);
        }
        if(control instanceof Combo) {
            behavior = new DependsComboBehavior((Combo) control);
        }
        return behavior;
    }

}
