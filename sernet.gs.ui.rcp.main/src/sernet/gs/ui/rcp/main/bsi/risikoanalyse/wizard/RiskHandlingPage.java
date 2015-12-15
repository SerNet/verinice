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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * Choose an alternative, how to deal with the Gefaerdungen.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class RiskHandlingPage extends RiskAnalysisWizardPage<TableViewer> {

    private static final Logger LOG = Logger.getLogger(RiskHandlingPage.class);
    
    private TableColumn imgColumn;
    private TableColumn numberColumn;
    private TableColumn nameColumn;
    private TableColumn choiceColumn;

    private static final int IMG_COL_WODTH = 35;
    private static final int NUM_COL_WIDTH = 100;
    private static final int NAME_COL_WIDTH = NUM_COL_WIDTH;
    private static final int CHOISE_COL_WIDTH = 200;
    private SearchFilter searchFilter = new SearchFilter();
    public static final String IMG_COLUMN_ID = "image"; //$NON-NLS-1$
    public static final String NUMBER_COLUMN_ID = "number"; //$NON-NLS-1$
    public static final String NAME_COLUMN_ID = "name"; //$NON-NLS-1$
    public static final String CHOICE_COLUMN_ID = "choice"; //$NON-NLS-1$
    private static final String CHOISE_CONDITION_NEXT_PAGE = "A";

    /**
     * Constructor sets title an description of WizardPage.
     */
    protected RiskHandlingPage() {
        super(Messages.RiskHandlingPage_4, Messages.RiskHandlingPage_5, Messages.RiskHandlingPage_6);
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
     * Fills the TableViewer with all previously selected Gefaehrdungen in order
     * to choose an alternate proceeding. Is processed each time the WizardPage
     * is set visible.
     */
    private void initContents() {
        List<GefaehrdungsUmsetzung> arrListAllGefaehrdungsUmsetzungen = ((RiskAnalysisWizard) getWizard()).getAllGefaehrdungsUmsetzungen();

        final ComboBoxCellEditor choiceEditor = new ComboBoxCellEditor(viewer.getTable(),
                GefaehrdungsUmsetzung.getAlternativenText(),
                SWT.READ_ONLY);
        choiceEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        viewer.setCellEditors(new CellEditor[] { null, null, null, choiceEditor });
        viewer.setCellModifier(new PropertiesComboBoxCellModifier(viewer, (RiskAnalysisWizard) getWizard(), this));

        /* needed for PropertiesComboBoxCellModifier */
        viewer.setColumnProperties(new String[] {
                IMG_COLUMN_ID,
                NUMBER_COLUMN_ID,
                NAME_COLUMN_ID,
                CHOICE_COLUMN_ID
        });
        /* map a domain model object into multiple images and text labels */
        viewer.setLabelProvider(new TableViewerLabelProvider());
        /* map domain model into array */
        viewer.setContentProvider(new ArrayContentProvider());
        /* associate domain model with viewer */
        viewer.setInput(arrListAllGefaehrdungsUmsetzungen);
        /* sort elements */
        viewer.setSorter(new GefaehrdungenSorter());
        packAllColumns();

        /* Wizard may be finished at this page */
        ((RiskAnalysisWizard) getWizard()).setCanFinish(true);

        checkPageComplete();
    }

    /**
     * Adjusts all columns of the TableViewer.
     */
    private void packAllColumns() {
        imgColumn.pack();
        numberColumn.pack();
        nameColumn.pack();
        choiceColumn.pack();
    }

    /**
     * Activates the next button, if any of the Gefaehrdungen's alternative is
     * "A" .
     */
    private void checkPageComplete() {
        List<GefaehrdungsUmsetzung> arrListAllGefaehrdungsUmsetzungen = ((RiskAnalysisWizard) getWizard()).getAllGefaehrdungsUmsetzungen();
        Boolean complete = false;

        /*
         * setPageComplete(false) if no GefaehrdungsUmsetzung is of alternative
         * "A"
         */
        for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : arrListAllGefaehrdungsUmsetzungen) {
            if (gefaehrdungsUmsetzung.getAlternative().equals(CHOISE_CONDITION_NEXT_PAGE)) { // $NON-NLS-1$
                complete = true;
                break;
            }
        }

        if (complete) {
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }
    }

    /**
     * Filter to extract all GefaehrdungsUmsetzungen matching a given String.
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
         * Selects all GefaehrdungsUmsetzungen matching the Pattern.
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
            try {
                GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) element;
                String gefaehrdungTitle = gefaehrdung.getTitle();
                Matcher matcher = pattern.matcher(gefaehrdungTitle);

                if (matcher.find()) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                LOG.error("Error while filtering table.", e);
                return true;
            }
        }
    }


    @Override
    protected void setColumns() {
        Table table = viewer.getTable();

        imgColumn = new TableColumn(table, SWT.LEFT);
        imgColumn.setText(""); //$NON-NLS-1$
        imgColumn.setWidth(IMG_COL_WODTH);

        numberColumn = new TableColumn(table, SWT.LEFT);
        numberColumn.setText(Messages.RiskHandlingPage_8);
        numberColumn.setWidth(NUM_COL_WIDTH);

        nameColumn = new TableColumn(table, SWT.LEFT);
        nameColumn.setText(Messages.RiskHandlingPage_9);
        nameColumn.setWidth(NAME_COL_WIDTH);

        choiceColumn = new TableColumn(table, SWT.LEFT);
        choiceColumn.setText(Messages.RiskHandlingPage_10);
        choiceColumn.setWidth(CHOISE_COL_WIDTH);

    }

    @Override
    protected void addSpecificListenersForPage() {

        /* Listener adds/removes Filter searchFilter */
        textSearch.addModifyListener(new ModifyListener() {

            /**
             * Adds/removes Filter when Text is modified depending on event.
             * 
             * @param event
             *            event containing information about the selection
             */
            @Override
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

                    } else {
                        /* filter is not active - add */
                        searchFilter.setPattern(text.getText());
                        viewer.addFilter(searchFilter);
                        viewer.refresh();
                    }
                } else {
                    viewer.removeFilter(searchFilter);
                    viewer.refresh();
                    packAllColumns();
                }
            }
        });

    }


    @Override
    protected TableViewer initializeViewer(Composite parent) {
        return new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
    }

    @Override
    protected void addButtons(Composite parent, String groupName) {
        /* there are no buttons needed in this Page */
    }

    @Override
    protected void addFilters(Composite parent) {
        Composite search = new Composite(parent, SWT.NULL);
        new Label(search, SWT.NULL).setText(Messages.ChooseGefaehrdungPage_10);
        textSearch = new Text(search, SWT.SINGLE | SWT.BORDER);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(DEFAULT_MARGINS).generateLayout(search);
        GridDataFactory.fillDefaults().hint(125, SWT.DEFAULT).applyTo(textSearch);
    }

}
