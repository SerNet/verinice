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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TableItem;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.SelectRiskTreatment;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * Sets a Gefaehrdung's alternative to the chosen value.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class PropertiesComboBoxCellModifier implements ICellModifier {
    private Viewer viewer;
    private RiskAnalysisWizard wizard;
    private RiskHandlingPage page;

    /**
     * Constructor sets the needed data.
     * 
     * @param newViewer
     *            Viewer, which the CellModifier is associated to
     * @param newWizard
     *            the RiskAnalysisWizard
     * @param newPage
     *            Page, which newViewer is asscoiated to
     */
    public PropertiesComboBoxCellModifier(Viewer newViewer, RiskAnalysisWizard newWizard, RiskHandlingPage newPage) {
        viewer = newViewer;
        wizard = newWizard;
        page = newPage;
    }

    /**
     * Checks whether the given property of the given element can be modified.
     * 
     * @param element
     *            the selected GefaehrdungsUmsetzung
     * @param property
     *            the selected column
     * @return true, if column "Risikoalternative", false else
     */
    public boolean canModify(Object element, String property) {
        if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the value for the given property of the given element.
     * 
     * @param element
     *            the selected GefaehrdungsUmsetzung
     * @param property
     *            the selected column
     * @return alternative as integer, if column "Risikoalternative", null else
     */
    public Object getValue(Object element, String property) {
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = (GefaehrdungsUmsetzung) element;

        if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
            return gefaehrdungsUmsetzung.getAlternativeIndex();
        } else {
            return null;
        }
    }

    /**
     * Modifies the value for the given property of the given element.
     * 
     * @param element
     *            the selected GefaehrdungsUmsetzung
     * @param property
     *            the selected column
     * @param value
     *            the selected GefaehrdungsUmsetzung's alternative
     */
    public void modify(Object element, String property, Object value) {
        if (element == null) {
            return;
        }

        Object item = ((TableItem) element).getData();

        if (item instanceof GefaehrdungsUmsetzung) {
            GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) item;

            if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
                int index = (Integer) value;
                String alternative = null;
                switch (index) {
                case 0:
                    alternative = GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_A;
                    break;
                case 1:
                    alternative = GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_B;
                    break;
                case 2:
                    alternative = GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_C;
                    break;
                case 3:
                    alternative = GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_D;
                    break;
                default:
                    break;
                }

                if (alternative == null) {
                    return;
                }

                try {
                    SelectRiskTreatment command = new SelectRiskTreatment(wizard.getFinishedRiskAnalysisLists().getDbId(), wizard.getFinishedRiskAnalysis(), gefaehrdung, alternative);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    wizard.setFinishedRiskLists(command.getFinishedRiskLists());

                    // set to update local display:
                    gefaehrdung.setAlternative(alternative);

                } catch (CommandException e) {
                    ExceptionUtil.log(e, Messages.PropertiesComboBoxCellModifier_0);
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
