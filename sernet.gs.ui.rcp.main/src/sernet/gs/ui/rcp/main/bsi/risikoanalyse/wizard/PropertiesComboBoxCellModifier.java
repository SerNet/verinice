/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TableItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 * Sets a Gefaehrdung's alternative to the chosen value.
 * 
 * @author ahanekop@sernet.de
 */
public class PropertiesComboBoxCellModifier implements ICellModifier {
	private Viewer viewer;
	private RiskAnalysisWizard wizard;
	private RiskHandlingPage page;

	/**
	 * Constructor sets the needed data.
	 * 
	 * @param newViewer Viewer, which the CellModifier is associated to
	 * @param newWizard the RiskAnalysisWizard
	 * @param newPage Page, which newViewer is asscoiated to
	 */
	public PropertiesComboBoxCellModifier(Viewer newViewer,
			RiskAnalysisWizard newWizard, RiskHandlingPage newPage) {
		viewer = newViewer;
		wizard = newWizard;
		page = newPage;
	}

	/**
	 * Checks whether the given property of the given element can be modified.
	 * 
	 * @param element the selected GefaehrdungsUmsetzung
	 * @param property the selected column
	 * @return true, if column "Risikoalternative", false else
	 */
	public boolean canModify(Object element, String property) {
		if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property))
			return true;
		return false;
	}

	/**
	 * Returns the value for the given property of the given element.
	 * 
	 * @param element the selected GefaehrdungsUmsetzung
	 * @param property the selected column
	 * @return alternative as integer, if column "Risikoalternative", null else
	 */
	public Object getValue(Object element, String property) {
		GefaehrdungsUmsetzung gefaehrdungsUmsetzung =
				(GefaehrdungsUmsetzung) element;

		if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
			return gefaehrdungsUmsetzung.getAlternativeIndex();
		} else {
			return null;
		}
	}

	/**
	 * Modifies the value for the given property of the given element.
	 * 
	 * @param element the selected GefaehrdungsUmsetzung
	 * @param property the selected column
	 * @param value the selected GefaehrdungsUmsetzung's alternative
	 */
	public void modify(Object element, String property, Object value) {
		if (element == null)
			return;
		
		Object item = ((TableItem) element).getData();
		List<GefaehrdungsUmsetzung> arrListNotOKGefaehrdungsUmsetzungen =
				wizard.getNotOKGefaehrdungsUmsetzungen();

		if (item instanceof GefaehrdungsUmsetzung) {
			GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) item;

			if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
				int index = (Integer) value;
				switch (index) {
				case 0:
					gefaehrdung.setAlternative(GefaehrdungsUmsetzung.
							GEFAEHRDUNG_ALTERNATIVE_A);
					if (!arrListNotOKGefaehrdungsUmsetzungen
							.contains(gefaehrdung)) {
						arrListNotOKGefaehrdungsUmsetzungen.add(gefaehrdung);
					}
					break;
				case 1:
					gefaehrdung.setAlternative(GefaehrdungsUmsetzung.
							GEFAEHRDUNG_ALTERNATIVE_B);
					if (arrListNotOKGefaehrdungsUmsetzungen
							.contains(gefaehrdung)) {
						arrListNotOKGefaehrdungsUmsetzungen.remove(gefaehrdung);
					}
					break;
				case 2:
					gefaehrdung.setAlternative(GefaehrdungsUmsetzung.
							GEFAEHRDUNG_ALTERNATIVE_C);
					if (arrListNotOKGefaehrdungsUmsetzungen
							.contains(gefaehrdung)) {
						arrListNotOKGefaehrdungsUmsetzungen.remove(gefaehrdung);
					}
					break;
				case 3:
					gefaehrdung.setAlternative(GefaehrdungsUmsetzung.
							GEFAEHRDUNG_ALTERNATIVE_D);
					if (arrListNotOKGefaehrdungsUmsetzungen
							.contains(gefaehrdung)) {
						arrListNotOKGefaehrdungsUmsetzungen.remove(gefaehrdung);
					}
					break;
				default:
					break;
				}

				viewer.refresh();

				if (wizard.getNotOKGefaehrdungsUmsetzungen().isEmpty()) {
					page.setPageComplete(false);
				} else {
					page.setPageComplete(true);
				} /* end if isEmpty */
			} /* end if equals */
		} /* end if instanceof */
	} /* end method modify() */
} /* end class PropertiesComboBoxCellModifier */
