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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.NegativeEstimateGefaehrdung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.PositiveEstimateGefaehrdung;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * WizardPage lists all previously selected Gefaehrdungen for the user to decide
 * which Gefaehrdungen need further processing.
 * 
 * @author ahanekop[at]sernet[dot]de
 * @author koderman
 * 
 */
public class EstimateGefaehrdungPage extends WizardPage {

    private TableColumn checkboxColumn;
    private TableColumn imageColumn;
    private TableColumn numberColumn;
    private TableColumn nameColumn;
    private TableColumn descriptionColumn;
    private CheckboxTableViewer viewer;
    private OwnGefaehrdungenFilter ownGefaehrdungFilter = new OwnGefaehrdungenFilter();
    private GefaehrdungenFilter gefaehrdungFilter = new GefaehrdungenFilter();
    private SearchFilter searchFilter = new SearchFilter();

    /**
     * Constructor sets title and description of WizardPage.
     */
    protected EstimateGefaehrdungPage() {
        super(Messages.EstimateGefaehrdungPage_0);
        setTitle(Messages.EstimateGefaehrdungPage_1);
        setDescription(Messages.EstimateGefaehrdungPage_2);
    }

    /**
     * Adds widgets to the wizardPage. Called once at startup of Wizard.
     * 
     * @param parent
     *            the parent Composite
     */
    public void createControl(Composite parent) {
        
        final int checkboxColumnWidth = 35;
        final int imageColumnWidth = checkboxColumnWidth;
        final int numberColumnWidth = 100;
        final int nameColumnWidth = numberColumnWidth;
        final int descriptionColumnWidht = 200;
        
        Composite composite = new Composite(parent, SWT.NULL);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);
        setControl(composite);

        /* CheckboxTableViewer */
        viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);
        final Table table = viewer.getTable();
        GridData data1 = new GridData();
        data1.grabExcessHorizontalSpace = true;
        data1.grabExcessVerticalSpace = true;
        data1.horizontalAlignment = SWT.FILL;
        data1.verticalAlignment = SWT.FILL;
        table.setLayoutData(data1);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        checkboxColumn = new TableColumn(table, SWT.LEFT);
        checkboxColumn.setText(""); //$NON-NLS-1$
        checkboxColumn.setWidth(checkboxColumnWidth);

        imageColumn = new TableColumn(table, SWT.LEFT);
        imageColumn.setText(""); //$NON-NLS-1$
        imageColumn.setWidth(imageColumnWidth);

        numberColumn = new TableColumn(table, SWT.LEFT);
        numberColumn.setText(Messages.EstimateGefaehrdungPage_5);
        numberColumn.setWidth(numberColumnWidth);

        nameColumn = new TableColumn(table, SWT.LEFT);
        nameColumn.setText(Messages.EstimateGefaehrdungPage_6);
        nameColumn.setWidth(nameColumnWidth);

        descriptionColumn = new TableColumn(table, SWT.LEFT);
        descriptionColumn.setText(Messages.EstimateGefaehrdungPage_7);
        descriptionColumn.setWidth(descriptionColumnWidht);

        /**
         * listener adds/removes Gefaehrdungen to Arrays of Gefaehrdungen
         */
        viewer.addCheckStateListener(new ICheckStateListener() {

            /**
             * Notifies of a change to the checked state of an element.
             */
            public void checkStateChanged(CheckStateChangedEvent event) {
                RiskAnalysisWizard internalWizard = ((RiskAnalysisWizard) getWizard());
                GefaehrdungsUmsetzung gefaehrdungsUmsetzung = (GefaehrdungsUmsetzung) event.getElement();

                if (event.getChecked()) {
                    /* checkbox set */

                    try {
                        NegativeEstimateGefaehrdung command = new NegativeEstimateGefaehrdung(internalWizard.getFinishedRiskAnalysisLists().getDbId(), gefaehrdungsUmsetzung, internalWizard.getFinishedRiskAnalysis());
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        internalWizard.setFinishedRiskLists(command.getLists());
                    } catch (Exception e) {
                        ExceptionUtil.log(e, Messages.EstimateGefaehrdungPage_8);
                    }

                } else {
                    try {
                        /* checkbox unset */
                        PositiveEstimateGefaehrdung command = new PositiveEstimateGefaehrdung(internalWizard.getFinishedRiskAnalysisLists().getDbId(), gefaehrdungsUmsetzung, internalWizard.getFinishedRiskAnalysis());
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        internalWizard.setFinishedRiskLists(command.getLists());
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, Messages.EstimateGefaehrdungPage_9);
                    }

                }
                ((RiskAnalysisWizard) getWizard()).setCanFinish(false);
                checkPageComplete();
            }
        });

        /* group the Filter checkboxes with composite */
        Composite compositeFilter = new Composite(composite, SWT.NULL);
        GridLayout gridLayoutFilters = new GridLayout();
        gridLayoutFilters.numColumns = 2;
        compositeFilter.setLayout(gridLayoutFilters);
        GridData gridCompositeFilter = new GridData();
        gridCompositeFilter.horizontalAlignment = SWT.LEFT;
        gridCompositeFilter.verticalAlignment = SWT.TOP;
        compositeFilter.setLayoutData(gridCompositeFilter);

        /* filter button - OwnGefaehrdungen only */
        Button buttonOwnGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonOwnGefaehrdungen.setText(Messages.EstimateGefaehrdungPage_10);
        GridData gridOwnGefaehrdungen = new GridData();
        gridOwnGefaehrdungen.horizontalSpan = 2;
        buttonOwnGefaehrdungen.setLayoutData(gridOwnGefaehrdungen);

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
                    viewer.refresh();
                } else {
                    viewer.removeFilter(ownGefaehrdungFilter);
                    viewer.refresh();
                    selectAssignedGefaehrdungen();
                }
            }
        });

        /* filter button - BSI Gefaehrdungen only */
        Button buttonGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonGefaehrdungen.setText(Messages.EstimateGefaehrdungPage_11);
        GridData gridGefaehrdungen = new GridData();
        gridGefaehrdungen.horizontalSpan = 2;
        buttonGefaehrdungen.setLayoutData(gridGefaehrdungen);

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
                    viewer.refresh();
                } else {
                    viewer.removeFilter(gefaehrdungFilter);
                    viewer.refresh();
                    selectAssignedGefaehrdungen();
                }
            }
        });

        /* filter button - search */
        new Label(compositeFilter, SWT.NULL).setText(Messages.EstimateGefaehrdungPage_12);
        Text textSearch = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
        GridData dataSearch = new GridData();
        dataSearch.horizontalAlignment = SWT.FILL;
        textSearch.setLayoutData(dataSearch);

        /* Listener adds/removes Filter searchFilter */
        textSearch.addModifyListener(new ModifyListener() {

            /**
             * Adds/removes Filter when Text is modified depending on event.
             * 
             * @param event
             *            event containing information about the selection
             */
            public void modifyText(ModifyEvent event) {
                Text text = (Text) event.widget;
                if (text.getText().length() > 0) {

                    ViewerFilter[] filters = viewer.getFilters();
                    SearchFilter thisFilter = null;
                    boolean contains = false;

                    for (ViewerFilter item : filters) {
                        if (item instanceof SearchFilter) {
                            contains = true;
                            thisFilter = (SearchFilter) item;
                        }
                    }
                    if (contains) {
                        /* filter is already active - update filter */
                        thisFilter.setPattern(text.getText());
                        viewer.refresh();
                        selectAssignedGefaehrdungen();

                    } else {
                        /* filter is not active - add */
                        searchFilter.setPattern(text.getText());
                        viewer.addFilter(searchFilter);
                        viewer.refresh();
                        selectAssignedGefaehrdungen();
                    }
                } else {
                    viewer.removeFilter(searchFilter);
                    viewer.refresh();
                    selectAssignedGefaehrdungen();
                }
            }

        });
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
     * Sets the control to the given visibility state.
     * 
     * @param visible
     *            boolean indicating if content should be visible
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            initContents();
        }
    }

    /**
     * Fills the CheckboxTableViewer with all previously selected Gefaehrdungen.
     * Is processed each time the WizardPage is set visible.
     */
    private void initContents() {   
        RiskAnalysisWizard wizard = ((RiskAnalysisWizard) getWizard());
        List<GefaehrdungsUmsetzung> arrListAssociatedGefaehrdungen = wizard.getAssociatedGefaehrdungen();

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
        checkboxColumn.pack();
        imageColumn.pack();
        numberColumn.pack();
        nameColumn.pack();
        descriptionColumn.pack();
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

    /**
     * Filter to extract all OwnGefaehrdungen in CheckboxTableViewer.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    class OwnGefaehrdungenFilter extends ViewerFilter {

        /**
         * Returns true, if the given element is an OwnGefaehrdung.
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
            return isOwnGefaehrung(element);
        }
    }

    /**
     * Filter to extract all Gefaehrdungen in CheckboxTableViewer.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    class GefaehrdungenFilter extends ViewerFilter {

        /**
         * Returns true, if the given element is a BSI Gefaehrdung.
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
            return !isOwnGefaehrung(element);
        }
    }

    public boolean isOwnGefaehrung(Object element) {
        if (element instanceof GefaehrdungsUmsetzung) {
            GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
            // only gefaehrdungen from BSI catalog have a URL associated with
            // them:
            return (gef.getUrl() == null || gef.getUrl().length() == 0 || gef.getUrl().equals("null")); //$NON-NLS-1$
        } else {
            return false;
        }
    }

    /**
     * Filter to extract all (Own)Gefaehrdungen matching a given String.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    class SearchFilter extends ViewerFilter {

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
            String gefaehrdungTitle = ""; //$NON-NLS-1$
            if (element instanceof Gefaehrdung) {
                Gefaehrdung gefaehrdung = (Gefaehrdung) element;
                gefaehrdungTitle = gefaehrdung.getTitel();
            } else if (element instanceof GefaehrdungsUmsetzung) {
                GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) element;
                gefaehrdungTitle = gefaehrdung.getTitle();
            }

            Matcher matcher = pattern.matcher(gefaehrdungTitle);
            if (matcher.find()) {
                return true;
            } 
            return false;
        }
    }
}
