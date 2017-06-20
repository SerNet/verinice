/*******************************************************************************
 * Copyright (c) 2016 Viktor Schmidt.
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class TreeElementDecoratingLabelProvider extends DecoratingLabelProvider {

    /**
     * @param provider
     * @param decorator
     */
    public TreeElementDecoratingLabelProvider(ILabelProvider provider, ILabelDecorator decorator) {
        super(provider, decorator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object o) {
        if (o instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung safeguard = (MassnahmenUmsetzung) o;
            return getForegroundColorForSafeguard(safeguard);
        } else if (o instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) o;
            return getForegroundColorForElement(element);
        }
        return super.getForeground(o);
    }

    private Color getForegroundColorForSafeguard(MassnahmenUmsetzung safeguard) {
        if (safeguard.isImplementation()) {
            return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        } else if (safeguard.getParent().isTemplate() && !safeguard.isTemplate()) {
            return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
        }
        return super.getForeground(safeguard);
    }

    private Color getForegroundColorForElement(CnATreeElement element) {
        if (element.isImplementation()) {
            return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        }
        return super.getForeground(element);
    }
}
