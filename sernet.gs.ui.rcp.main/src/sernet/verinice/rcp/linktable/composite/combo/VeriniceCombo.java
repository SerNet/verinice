/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable.composite.combo;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceCombo extends Combo {

    public VeriniceCombo(Composite parent, int style) {
        super(parent, style);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Combo#computeSize(int, int, boolean)
     */
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {

        Point size = super.computeSize(wHint, hHint, changed);
        // Point selection = getSelection();
        // int sizeSelection = selection.y - selection.x;
        //
        // if (size.x > 100 && sizeSelection > 0) {
        // size.x = Math.min(sizeSelection, size.x);
        // }

        return size;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Combo#checkSubclass()
     */
    @Override
    protected void checkSubclass() {
        // TODO rmotza Auto-generated method stub
        // super.checkSubclass();
    }

}
