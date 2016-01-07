/*******************************************************************************
 * Copyright (c) 2015 Ruth Motza.
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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * Filter to extract all (Own)Gefaehrdungen matching a given String.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class RiskAnalysisWizardPageSearchFilter extends ViewerFilter {

    private Pattern pattern;

    /**
     * Updates the Pattern.
     * 
     * @param searchString
     *            the String to search for
     */
    void setPattern(String searchString) {
        pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Selects all (Own)Gefaehrdungen matching the Pattern.
     * 
     * @param viewer
     *            the Viewer to operate on
     * @param parentElement
     *            not used
     * @param element
     *            given element
     * @return true if element passes test, false else
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        String title = "";
        if (element instanceof Gefaehrdung) {

            Gefaehrdung gefaehrdung = (Gefaehrdung) element;
            title = gefaehrdung.getTitel();
        } else if (element instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung massnahmeUmsetzung = (MassnahmenUmsetzung) element;
            title = massnahmeUmsetzung.getTitle();
        } else if (element instanceof GefaehrdungsUmsetzung) {
            GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) element;
            title = gefaehrdung.getText();
        }

        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            return true;
        }
        return false;
    }
}
