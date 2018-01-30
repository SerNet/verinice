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
 *     Anne Hanekop <ah[at]sernet[dot]de>     - initial API and implementation
 *     ak[at]sernet[dot]de                    - various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.service.commands.risk.NegativeEstimateGefaehrdung;
import sernet.verinice.service.commands.risk.PositiveEstimateGefaehrdung;

/**
 * WizardPage lists all previously selected Gefaehrdungen for the user to decide
 * which Gefaehrdungen need further processing.
 * 
 * @author ahanekop[at]sernet[dot]de
 * @author koderman
 * 
 */
public class EstimateGefaehrdungPage extends RiskAnalysisWizardPage<CheckboxTableViewer> {

    private static final Logger LOG = Logger.getLogger(EstimateGefaehrdungPage.class);
    
    private TableViewerColumn checkboxColumn;
    private TableViewerColumn imageColumn;
    private TableViewerColumn numberColumn;
    private TableViewerColumn nameColumn;
    private TableViewerColumn descriptionColumn;

    /**
     * Constructor sets title and description of WizardPage.
     */
    protected EstimateGefaehrdungPage() {
        super(Messages.EstimateGefaehrdungPage_0, Messages.EstimateGefaehrdungPage_1, Messages.EstimateGefaehrdungPage_2);
    }

    /**
     * Marks all checkboxes of Gefaehrdungen that are selected as not okay.
     */
    private void selectAssignedGefaehrdungen() {
        List<GefaehrdungsUmsetzung> associatedGefaehrdungen = ((RiskAnalysisWizard) getWizard()).getAssociatedGefaehrdungen();

        for (GefaehrdungsUmsetzung gefaehrdung : associatedGefaehrdungen) {
            if (!gefaehrdung.getOkay()) {
                viewer.setChecked(gefaehrdung, true);
            }
        }
    }

    /**
     * Fills the CheckboxTableViewer with all previously selected Gefaehrdungen.
     * Is processed each time the WizardPage is set visible.
     */
    @Override
    protected void doInitContents() {
        List<GefaehrdungsUmsetzung> arrListAssociatedGefaehrdungen = getRiskAnalysisWizard().getAssociatedGefaehrdungen();

        /* map a domain model object into multiple images and text labels */
        viewer.setLabelProvider(new CheckboxTableViewerLabelProvider());
        /* map domain model into array */
        viewer.setContentProvider(new ArrayContentProvider());
        /* associate domain model with viewer */
        viewer.setInput(arrListAssociatedGefaehrdungen);
        viewer.setSorter(new GefaehrdungenSorter());
        selectAssignedGefaehrdungen();
        packAllColumns();

        checkPageComplete();
    }

    /**
     * Adjusts all columns of the CheckboxTableViewer.
     */
    private void packAllColumns() {
        checkboxColumn.getColumn().pack();
        imageColumn.getColumn().pack();
        numberColumn.getColumn().pack();
        nameColumn.getColumn().pack();
        nameColumn.getColumn().setWidth(Math.min(nameColumn.getColumn().getWidth(), WIDTH_COL_NAME));
        descriptionColumn.getColumn().pack();
    }

    /**
     * Activates the next button, if the List of selected Gefaehrdungen is not
     * empty.
     */
    private void checkPageComplete() {
        if (((RiskAnalysisWizard) getWizard()).getAllGefaehrdungsUmsetzungen().isEmpty()) {
            setPageComplete(false);
        } else {
            setPageComplete(true);
        }
    }


    @Override
    protected void setColumns() {
        final int checkboxColumnWidth = 35;
        final int imageColumnWidth = checkboxColumnWidth;
        final int numberColumnWidth = 100;
        final int nameColumnWidth = numberColumnWidth;
        final int descriptionColumnWidht = 200;
        checkboxColumn = new TableViewerColumn(viewer, SWT.LEFT);
        checkboxColumn.getColumn().setText(""); //$NON-NLS-1$
        checkboxColumn.getColumn().setWidth(checkboxColumnWidth);

        imageColumn = new TableViewerColumn(viewer, SWT.LEFT);
        imageColumn.getColumn().setText(""); //$NON-NLS-1$
        imageColumn.getColumn().setWidth(imageColumnWidth);

        numberColumn = new TableViewerColumn(viewer, SWT.LEFT);
        numberColumn.getColumn().setText(Messages.EstimateGefaehrdungPage_5);
        numberColumn.getColumn().setWidth(numberColumnWidth);

        nameColumn = new TableViewerColumn(viewer, SWT.LEFT);
        nameColumn.getColumn().setText(Messages.EstimateGefaehrdungPage_6);
        nameColumn.getColumn().setWidth(nameColumnWidth);

        descriptionColumn = new TableViewerColumn(viewer, SWT.LEFT);
        descriptionColumn.getColumn().setText(Messages.EstimateGefaehrdungPage_7);
        descriptionColumn.getColumn().setWidth(descriptionColumnWidht);

    }


    @Override
    protected void addSpecificListenersForPage() {

        addFilterListeners();

        /**
         * listener adds/removes Gefaehrdungen to Arrays of Gefaehrdungen
         */
        viewer.addCheckStateListener(new ICheckStateListener() {

            /**
             * Notifies of a change to the checked state of an element.
             */
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                GefaehrdungsUmsetzung gefaehrdungsUmsetzung = (GefaehrdungsUmsetzung) event.getElement();

                Integer raListDbId = getRiskAnalysisWizard().getFinishedRiskAnalysisLists().getDbId();
                if (event.getChecked()) {
                    /* checkbox set */

                    try {
                        NegativeEstimateGefaehrdung command = new NegativeEstimateGefaehrdung(raListDbId, gefaehrdungsUmsetzung, getRiskAnalysisWizard().getFinishedRiskAnalysis());
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        getRiskAnalysisWizard().setFinishedRiskLists(command.getRaList());
                    } catch (Exception e) {
                        LOG.error("Error while selecting", e);
                        ExceptionUtil.log(e, Messages.EstimateGefaehrdungPage_8);
                    }

                } else {
                    try {
                        /* checkbox unset */
                        PositiveEstimateGefaehrdung command = new PositiveEstimateGefaehrdung(raListDbId, gefaehrdungsUmsetzung, getRiskAnalysisWizard().getFinishedRiskAnalysis());
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        if (command.getLists() != null) {
                            getRiskAnalysisWizard().setFinishedRiskLists(command.getLists());
                        }
                    } catch (Exception e) {
                        LOG.error("Error while deselecting", e);
                        ExceptionUtil.log(e, Messages.EstimateGefaehrdungPage_9);
                    }

                }
                ((RiskAnalysisWizard) getWizard()).setCanFinish(false);
                checkPageComplete();
            }
        });
    }

    private void addFilterListeners() {


        /* Listener adds/removes Filter gefaehrdungFilter */
        buttonGefaehrdungen.addSelectionListener(new SelectionAdapter() {

            /**
             * Adds/removes Filter depending on event.
             * 
             * @param event
             *            event containing information about the selection
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                Button button = (Button) event.widget;
                if (button.getSelection()) {
                    viewer.addFilter(gefaehrdungFilter);
                    refresh();  
                    packAllColumns();
                } else {
                    viewer.removeFilter(gefaehrdungFilter);
                    refresh();
                    selectAssignedGefaehrdungen();
                    packAllColumns();
                }
            }
        });

        /* Listener adds/removes Filter ownGefaehrdungFilter */
        buttonOwnGefaehrdungen.addSelectionListener(new SelectionAdapter() {

            /**
             * Adds/removes Filter depending on event.
             * 
             * @param event
             *            event containing information about the selection
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                Button button = (Button) event.widget;
                if (button.getSelection()) {
                    viewer.addFilter(ownGefaehrdungFilter);
                    refresh();
                    packAllColumns();
                } else {
                    viewer.removeFilter(ownGefaehrdungFilter);
                    refresh();
                    selectAssignedGefaehrdungen();
                    packAllColumns();
                }
            }
        });

    }

    @Override
    protected void doAfterUpdateFilter() {
        selectAssignedGefaehrdungen();
    }

    @Override
    protected void doAfterRemoveSearchFilter() {
        selectAssignedGefaehrdungen();

    }


    @Override
    protected void addButtons(Composite parent, String groupName) {
        /* there are no buttons needed in this Page */
    }

    @Override
    protected CheckboxTableViewer initializeViewer(Composite parent) {
        return CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
    }


}
