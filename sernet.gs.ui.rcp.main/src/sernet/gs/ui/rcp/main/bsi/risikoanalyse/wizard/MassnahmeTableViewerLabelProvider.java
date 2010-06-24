/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah[at]sernet[dot]de>
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
 *     Anne Hanekop <ah[at]sernet[dot]de> 	- initial API and implementation
 *     ak[at]sernet[dot]de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * Provides an image or text for each column per item in the TableViewer.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class MassnahmeTableViewerLabelProvider implements ITableLabelProvider {

    /**
     * Returns the image of the element for the given column.
     * 
     * @param element
     *            the element representing the row
     * @param columnIndex
     *            zero-based index of the column
     * @return the image of the element if it's the first column, null else
     */
    public Image getColumnImage(Object element, int columnIndex) {

        if (columnIndex == 0) {
            if (element instanceof RisikoMassnahmenUmsetzung) {
                return GefaehrdungsElementImageProvider.getImage(element);
            } else {
                return ImageCache.getInstance().getImage(ImageCache.MASSNAHMEN_UMSETZUNG_JA);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the text of the element for the given column.
     * 
     * @param element
     *            the element representing the row
     * @param columnIndex
     *            zero-based index of the column
     * @return the element's text for the column, empty string else
     */
    public String getColumnText(Object element, int columnIndex) {

        if (element instanceof RisikoMassnahmenUmsetzung) {
            RisikoMassnahmenUmsetzung massnahme = (RisikoMassnahmenUmsetzung) element;
            switch (columnIndex) {
            case 0:
                return null;
            case 1:
                return massnahme.getNumber();
            case 2:
                return "[" + massnahme.getStufe() + "] " + massnahme.getName(); //$NON-NLS-1$ //$NON-NLS-2$
            case 3:
        		RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(massnahme);
                return shorten(massnahme.getDescription());
            }
            ;
        } else {
            MassnahmenUmsetzung massnahme = (MassnahmenUmsetzung) element;
            switch (columnIndex) {
            case 0:
                return null;
            case 1:
                return massnahme.getKapitel();
            case 2:
                return "[" + massnahme.getStufe() + "] " + massnahme.getName(); //$NON-NLS-1$ //$NON-NLS-2$
            case 3:
                return Messages.MassnahmeTableViewerLabelProvider_4;
            }
            ;
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Not used. Must be implemented due to IBaseLabelProvider.
     * 
     * @param listener
     *            a label provider listener
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /**
     * Not used. Must be implemented due to IBaseLabelProvider.
     */
    public void dispose() {
    }

    /**
     * Returns whether the label would be affected by a change to the given
     * property of the given element.
     * 
     * @param element
     *            the element
     * @param property
     *            the property
     * @return always false
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * Not used. Must be implemented due to IBaseLabelProvider.
     * 
     * @param listener
     *            a label provider listener
     */
    public void removeListener(ILabelProviderListener listener) {
    }

    /**
     * Shortens the description for single-line table display.
     * 
     * @param description
     *            the full length description
     * @return shortened version of the description without newline-characters
     */
    private String shorten(String description) {
        String oneline = description.replaceAll(System.getProperty("line.separator"), " "); //$NON-NLS-1$ //$NON-NLS-2$
        if (oneline.length() > 100) {
            return oneline.substring(0, 100) + "..."; //$NON-NLS-1$
        }
        return oneline;

    }
}
