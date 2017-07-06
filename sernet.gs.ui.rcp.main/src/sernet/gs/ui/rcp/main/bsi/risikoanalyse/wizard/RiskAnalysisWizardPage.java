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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.views.SerializeBrowserLoadingListener;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * Base risk analysis wizard page. Every RiskAnalysisWizard page inherits
 * fundamental buttons and functions
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 * @param <T>
 */
public abstract class RiskAnalysisWizardPage<T extends TableViewer> extends WizardPage {


    protected Composite rootContainer;
    protected Button buttonOwnGefaehrdungen, buttonGefaehrdungen;
    protected Text textSearch;
    private Browser browser;
    private RiskAnalysisWizardBrowserUpdateListener browserListener;
    protected SerializeBrowserLoadingListener browserLoadingListener;
    protected Button buttonNew, buttonEdit, buttonDelete;
    protected T viewer;

    protected OwnGefaehrdungenFilter ownGefaehrdungFilter = new OwnGefaehrdungenFilter();
    protected GefaehrdungenFilter gefaehrdungFilter = new GefaehrdungenFilter();
    protected RiskAnalysisWizardPageSearchFilter searchFilter = new RiskAnalysisWizardPageSearchFilter();

    private RiskAnalysisWizard riskWizard;

    // SWT & JFace
    protected static final Point ADD_EDIT_REMOVE_BUTTON_SIZE = new Point(110, 30);
    protected static final int BUTTONS_GRID_COLUMN_AMOUNT = 5;
    protected static final int NUM_COLS_BUTTONS = 3;
    protected static final int NUM_COLS_FILTERS = 1;
    protected static final int NUM_COLS_CONTROLS = 2;
    protected static final int WIDTH_COL_NAME = 400;
    protected static final int WIZARD_BROWSER_WIDTH = 500;
    protected static final int WIZARD_NUM_COLS_ROOT = 2;

    private static final String DB_NULL = "null";

    protected static final Point DEFAULT_MARGINS = new Point(5, 5);

    protected RiskAnalysisWizardPage(String pageName, String title, String description) {
        super(pageName);
        setTitle(title);
        setDescription(description);
    }

    protected RiskAnalysisWizard getRiskAnalysisWizard() {
        if (riskWizard == null) {
            riskWizard = (RiskAnalysisWizard) getWizard();
        }
        return riskWizard;
    }


    @Override
    public void createControl(Composite parent) {
        rootContainer = new Composite(parent, SWT.NONE);
        
        setLeftColumn(rootContainer);
        setRightColumn(rootContainer);
        addControls(rootContainer);
        GridLayoutFactory.fillDefaults().numColumns(WIZARD_NUM_COLS_ROOT).margins(DEFAULT_MARGINS).generateLayout(rootContainer);
        setControl(rootContainer);
        addListeners();

    }


    private void resetSearchField() {
        if (textSearch != null) {
            textSearch.setText("");
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

    private void initContents() {

        resetSearchField();

        doInitContents();
    }

    protected abstract void doInitContents();

    public void refresh() {
        viewer.refresh();
        browserListener.selectionChanged(new SelectionChangedEvent(viewer, viewer.getSelection()));
    }


    private void setRightColumn(Composite parent) {
        Composite rightColumn = new Composite(parent, SWT.FULL_SELECTION);

        browser = new Browser(rightColumn, SWT.BORDER);
        browserLoadingListener = new SerializeBrowserLoadingListener(browser);
        browser.addProgressListener(browserLoadingListener);
        GridLayoutFactory.fillDefaults().margins(DEFAULT_MARGINS).generateLayout(rightColumn);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH
                | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        GridDataFactory.fillDefaults().hint(WIZARD_BROWSER_WIDTH, SWT.LONG).grab(false, true).applyTo(rightColumn);
    }

    /**
     * function adds a new checkboxTableview. Overwrite to change
     * 
     * @param parent
     */
    protected void setLeftColumn(Composite parent) {
        
        /* CheckboxTableViewer */
        Composite leftColumn = new Composite(parent, SWT.NONE);
        viewer = initializeViewer(leftColumn);
        final Table table = viewer.getTable();

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        setColumns();



        table.layout();
        GridLayoutFactory.fillDefaults().margins(DEFAULT_MARGINS).generateLayout(leftColumn);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(leftColumn);
    }

    protected abstract T initializeViewer(Composite parent);

    protected abstract void setColumns();

    protected void addControls(Composite parent) {


        Composite controls = new Composite(parent, SWT.NONE);
        addFilters(controls);

        addButtons(controls, Messages.ChooseGefaehrdungPage_11);

        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_CONTROLS).margins(DEFAULT_MARGINS).generateLayout(controls);

    }

    protected void addFilters(Composite parent) {
        Composite compositeFilter = new Composite(parent, SWT.NONE);

        /* filter button - OwnGefaehrdungen only */
        buttonOwnGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonOwnGefaehrdungen.setText(Messages.ChooseGefaehrdungPage_8);

        /* filter button - BSI Gefaehrdungen only */
        buttonGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonGefaehrdungen.setText(Messages.ChooseGefaehrdungPage_9);

        /* filter button - search */
        Composite search = new Composite(compositeFilter, SWT.NULL);
        new Label(search, SWT.NULL).setText(Messages.ChooseGefaehrdungPage_10);
        textSearch = new Text(search, SWT.SINGLE | SWT.BORDER);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(DEFAULT_MARGINS).generateLayout(search);
        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_FILTERS).margins(DEFAULT_MARGINS).generateLayout(compositeFilter);
        GridDataFactory.fillDefaults().hint(125, SWT.DEFAULT).applyTo(textSearch);

    }

    protected void addButtons(Composite parent, String groupName) {

        /* group the buttons with Group */
        Group groupButtons = new Group(parent, SWT.SHADOW_ETCHED_OUT);
        groupButtons.setText(groupName);

        buttonNew = new Button(groupButtons, SWT.PUSH);
        buttonNew.setText(Messages.ChooseGefaehrdungPage_12);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonNew);

        /* edit button */
        buttonEdit = new Button(groupButtons, SWT.PUSH);
        buttonEdit.setText(Messages.ChooseGefaehrdungPage_17);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonEdit);

        /* delete button */
        buttonDelete = new Button(groupButtons, SWT.PUSH);
        buttonDelete.setText(Messages.ChooseGefaehrdungPage_13);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonDelete);


        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_BUTTONS).margins(DEFAULT_MARGINS).generateLayout(groupButtons);

        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(groupButtons);

    }

    protected void addListeners() {

        addSpecificListenersForPage();

        browserListener = new RiskAnalysisWizardBrowserUpdateListener(browserLoadingListener, viewer);
        viewer.addSelectionChangedListener(browserListener);

        if (buttonDelete != null && buttonEdit != null) {
            viewer.addSelectionChangedListener(new ISelectionChangedListener() {

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    if (event.getSelection() instanceof IStructuredSelection) {
                        Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
                        if (element instanceof RisikoMassnahmenUmsetzung || element instanceof OwnGefaehrdung) {
                            buttonDelete.setEnabled(true);
                            buttonEdit.setEnabled(true);
                        } else {
                            buttonDelete.setEnabled(false);
                            buttonEdit.setEnabled(false);
                        }
                    }
                }
            });
        }
        
        if (textSearch != null) {
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
                    String searchText = textSearch.getText();
                    if (!searchText.isEmpty()) {
                        filterItems(searchText);
                    } else {
                        viewer.removeFilter(searchFilter);
                        viewer.refresh();
                        doAfterRemoveSearchFilter();
                    }
                }

                private void filterItems(String text) {
                    ViewerFilter[] filters = viewer.getFilters();
                    RiskAnalysisWizardPageSearchFilter thisFilter = null;
                    boolean contains = false;

                    for (ViewerFilter item : filters) {
                        if (item instanceof RiskAnalysisWizardPageSearchFilter) {
                            contains = true;
                            thisFilter = (RiskAnalysisWizardPageSearchFilter) item;
                        }
                    }
                    updateOrAddFilter(text, thisFilter, contains);
                }

            });
        }
    }

    protected void updateOrAddFilter(String text, RiskAnalysisWizardPageSearchFilter thisFilter, boolean contains) {
        if (contains) {
            /* filter is already active - update filter */
            thisFilter.setPattern(text);
            viewer.refresh();
            doAfterUpdateFilter();

        } else {
            /* filter is not active - add */
            searchFilter.setPattern(text);
            viewer.addFilter(searchFilter);
            viewer.refresh();
        }

    }

    protected abstract void doAfterUpdateFilter();

    protected abstract void addSpecificListenersForPage();

    protected abstract void doAfterRemoveSearchFilter();

    /**
     * Filter to extract all OwnGefaehrdungen in CheckboxTableViewer.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    static class OwnGefaehrdungenFilter extends ViewerFilter {

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
            if (element instanceof OwnGefaehrdung) {
                return true;
            }
            if (element instanceof GefaehrdungsUmsetzung) {
                return isOwnGefaehrung((GefaehrdungsUmsetzung) element);

            }
            return false;
        }

        public boolean isOwnGefaehrung(GefaehrdungsUmsetzung gef) {
            // only gefaehrdungen from BSI catalog have a URL associated
            // with
            // them:
            return gef.getUrl() == null || gef.getUrl().length() == 0 || gef.getUrl().equals(DB_NULL); // $NON-NLS-1$
        }
    }

    /**
     * Filter to extract all Gefaehrdungen in CheckboxTableViewer.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    static class GefaehrdungenFilter extends ViewerFilter {

        /**
         * Returns true, if the given element is a Gefaehrdung.
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
            if (element instanceof GefaehrdungsUmsetzung) {
                return !isOwnGefaehrung((GefaehrdungsUmsetzung) element);
            }
            return !(element instanceof OwnGefaehrdung);
        }

        public boolean isOwnGefaehrung(GefaehrdungsUmsetzung gef) {
            // only gefaehrdungen from BSI catalog have a URL associated
            // with
            // them:
            return gef.getUrl() == null || gef.getUrl().length() == 0 || gef.getUrl().equals(DB_NULL); // $NON-NLS-1$
        }

    }


}
