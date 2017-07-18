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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsBaumRoot;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.IGefaehrdungsBaumElement;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.risk.RemoveMassnahmeFromGefaherdung;

/**
 * Add security measures (Massnahmen) to the risks (Gefaehrdungen). New security
 * measures may be created.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
@SuppressWarnings("restriction")
public class AdditionalSecurityMeasuresPage extends RiskAnalysisWizardPage<TableViewer> {

    private TableColumn imageColumnMassnahme;
    private TableColumn numberColumnMassnahme;
    private TableColumn nameColumnMassnahme;
    private TreeViewer viewerScenario;
    private RiskAnalysisDialogItems<MassnahmenUmsetzung> itemsToCheckForUniqueNumber;

    private static final int IMAGE_CM_WIDTH = 35;
    private static final int NUMBER_CM_WIDTH = 100;
    private static final int NAME_CM_WIDTH = NUMBER_CM_WIDTH;

    private MassnahmenUmsetzungenFilter controlFilter = new MassnahmenUmsetzungenFilter();
    private RisikoMassnahmenUmsetzungenFilter ownControlFilter = new RisikoMassnahmenUmsetzungenFilter();
    private Button buttonRemoveControlFromScenario;
    private static final Logger LOG = Logger.getLogger(AdditionalSecurityMeasuresPage.class);

    /**
     * Constructor sets title an description of WizardPage.
     */
    protected AdditionalSecurityMeasuresPage() {
        super(Messages.AdditionalSecurityMeasuresPage_0, Messages.AdditionalSecurityMeasuresPage_1, Messages.AdditionalSecurityMeasuresPage_2);
    }


    /**
     * fills the CheckboxTableViewer with all Gefaehrdungen available
     */

    /**
     * Fills the TreeViewer with Gefaehrdungen and the TableViewer with
     * Massnahmen. Is processed each time the WizardPage is set visible.
     */
    @Override
    protected void doInitContents() {

        List<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen = getRiskAnalysisWizard().getNotOKGefaehrdungsUmsetzungen();
        
        /* root of TreeViewer */
        IGefaehrdungsBaumElement baum = new GefaehrdungsBaumRoot(arrListGefaehrdungsUmsetzungen);

        viewerScenario.setLabelProvider(new GefaehrdungTreeViewerLabelProvider());
        viewerScenario.setContentProvider(new GefaehrdungTreeViewerContentProvider());
        viewerScenario.setInput(baum);
        viewerScenario.expandAll();

        ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen = (ArrayList<MassnahmenUmsetzung>) getRiskAnalysisWizard().getAllMassnahmenUmsetzungen();

        /* map a domain model object into multiple images and text labels */
        viewer.setLabelProvider(new MassnahmeTableViewerLabelProvider());
        /* map domain model into array */
        viewer.setContentProvider(new ArrayContentProvider());
        /* associate domain model with viewer */
        viewer.setInput(arrListMassnahmenUmsetzungen);
        viewer.setSorter(new MassnahmenSorter());
        packAllMassnahmeColumns();

        getRiskAnalysisWizard().setCanFinish(true);
    }

    /**
     * Adjusts all columns of the TableViewer.
     */
    private void packAllMassnahmeColumns() {
        imageColumnMassnahme.pack();
        numberColumnMassnahme.pack();
        nameColumnMassnahme.pack();
    }

    /**
     * Deletes a Massnahme from the TreeViewer.
     * 
     * @param massnahme
     *            the Massnahme to delete
     */
    private void deleteControlFromTreeViewer(RisikoMassnahmenUmsetzung massnahme) {

        try {
            GefaehrdungsUmsetzung parent = (GefaehrdungsUmsetzung) massnahme.getParent();

            if (massnahme instanceof RisikoMassnahmenUmsetzung && parent instanceof GefaehrdungsUmsetzung) {

                RemoveMassnahmeFromGefaherdung command = new RemoveMassnahmeFromGefaherdung(parent, massnahme);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                parent = command.getParent();

                parent.getChildren().remove(massnahme);
                /* refresh viewer */
                GefaehrdungsBaumRoot baumElement = (GefaehrdungsBaumRoot) viewerScenario.getInput();
                baumElement.replaceChild(parent);
                viewerScenario.refresh();
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Deletes a self-defined Massnahme from the TableViewer.
     * 
     * @param massnahme
     *            the Massnahme to delete
     */
    private void deleteOwnControl(RisikoMassnahmenUmsetzung risikoMassnahmenUmsetzung) {

        ArrayList<MassnahmenUmsetzung> arrListMassnahmenUmsetzungen = (ArrayList<MassnahmenUmsetzung>) getRiskAnalysisWizard().getAllMassnahmenUmsetzungen();

        try {
            /* delete from List of MassnahmenUmsetzungen */
            if (arrListMassnahmenUmsetzungen.contains(risikoMassnahmenUmsetzung)) {
                arrListMassnahmenUmsetzungen.remove(risikoMassnahmenUmsetzung);
                RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(risikoMassnahmenUmsetzung);
                RisikoMassnahmeHome.getInstance().remove(risikoMassnahmenUmsetzung.getRisikoMassnahme());
            }

            // TODO an dieser Stelle müssten eigentlich auch die
            // RisikoMassnahmenUmsetzungen,
            // die duch DND dieser RisikoMassnahmenUmsetzungen in den
            // TreeViewer
            // gelangt sind, geloescht werden..
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.AdditionalSecurityMeasuresPage_22);
        }
    }

    protected void editOwnControl() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection.getFirstElement();
        if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
            RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung = (RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;
            itemsToCheckForUniqueNumber = new RiskAnalysisDialogItems<>(
                    getRiskAnalysisWizard().getAllMassnahmenUmsetzungen(),
                    MassnahmenUmsetzung.class);
            final EditRisikoMassnahmenUmsetzungDialog dialog = new EditRisikoMassnahmenUmsetzungDialog(
                    rootContainer.getShell(), selectedRisikoMassnahmenUmsetzung,
                    itemsToCheckForUniqueNumber);
            int result = dialog.open();
            if (result == Window.OK) {
                getRiskAnalysisWizard().replaceMassnahmenUmsetzung(dialog.getRisikoMassnahmenUmsetzung());
                refresh();
                packAllMassnahmeColumns();
            }

        }
    }

    /**
     * Filter to extract all self-defined Massnahmen in the TableViewer.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    class RisikoMassnahmenUmsetzungenFilter extends ViewerFilter {

        /**
         * Returns true, if the given element is a self-defined Massnahme.
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
            return element instanceof RisikoMassnahmenUmsetzung;
        }
    }

    /**
     * Filter to extract all BSI-Massnahmen in the TableViewer.
     * 
     * @author ahanekop[at]sernet[dot]de
     */
    class MassnahmenUmsetzungenFilter extends ViewerFilter {

        /**
         * Returns true, if the given element is a BSI-Massnahme from
         * BSI-Standard 100-3.
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
            return !(element instanceof RisikoMassnahmenUmsetzung);
        }
    }


    @Override
    protected void setLeftColumn(Composite parent) {

        Composite leftColumn = new Composite(parent, SWT.NONE);
        /* TreeViewer: Gefaehrdungen */
        Composite tree = new Composite(leftColumn, SWT.NONE);

        viewerScenario = new TreeViewer(tree, SWT.MULTI);
        viewerScenario.getTree().setLayoutData(new GridData(GridData.FILL_BOTH
                | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        GridLayoutFactory.fillDefaults().generateLayout(tree);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tree);

        Composite tableComp = new Composite(leftColumn, SWT.NONE);
        viewer = initializeViewer(tableComp);
        final Table table = viewer.getTable();

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        setColumns();

        GridLayoutFactory.fillDefaults().generateLayout(tableComp);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComp);

        GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).margins(DEFAULT_MARGINS).generateLayout(leftColumn);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(leftColumn);
    }

    @Override
    protected TableViewer initializeViewer(Composite parent) {
        return new TableViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
    }

    @Override
    protected void setColumns() {

        Table tableMassnahme = viewer.getTable();

        imageColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
        imageColumnMassnahme.setText(""); //$NON-NLS-1$
        imageColumnMassnahme.setWidth(IMAGE_CM_WIDTH);

        numberColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
        numberColumnMassnahme.setText(Messages.AdditionalSecurityMeasuresPage_4);
        numberColumnMassnahme.setWidth(NUMBER_CM_WIDTH);

        nameColumnMassnahme = new TableColumn(tableMassnahme, SWT.LEFT);
        nameColumnMassnahme.setText(Messages.AdditionalSecurityMeasuresPage_5);
        nameColumnMassnahme.setWidth(NAME_CM_WIDTH);

    }

    @Override
    protected void addSpecificListenersForPage() {

        addButtonListeners();
        addFilterListeners();

        RiskAnalysisWizardBrowserUpdateListener browserListener = new RiskAnalysisWizardBrowserUpdateListener(browserLoadingListener, viewerScenario);
        viewerScenario.addSelectionChangedListener(browserListener);

        /* listener opens edit Dialog for selected Massnahme */
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                /* retrieve selected Massnahme and open edit dialog with it */
                editOwnControl();
            }
        });



        /* Listener opens MessageDialog and deletes selected Massnahme */
        buttonRemoveControlFromScenario.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewerScenario.getSelection();
                RisikoMassnahmenUmsetzung selectedRisikoMassnahmenUmsetzung = (RisikoMassnahmenUmsetzung) selection.getFirstElement();

                /* ask user to confirm */
                boolean confirmed = MessageDialog.openQuestion(rootContainer.getShell(),
                        Messages.AdditionalSecurityMeasuresPage_9,
                        NLS.bind(Messages.AdditionalSecurityMeasuresPage_10, selectedRisikoMassnahmenUmsetzung.getTitle()));
                if (confirmed) {
                    deleteControlFromTreeViewer(selectedRisikoMassnahmenUmsetzung);
                    viewerScenario.refresh();
                    refresh();
                }
            }
        });

        viewerScenario.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    if (((IStructuredSelection) event.getSelection()).getFirstElement() instanceof RisikoMassnahmenUmsetzung) {
                        buttonRemoveControlFromScenario.setEnabled(true);
                    } else {
                        buttonRemoveControlFromScenario.setEnabled(false);
                    }
                }
            }
        });







        /* add drag and drop support */
        CnATreeElement cnaElement = getRiskAnalysisWizard().getFinishedRiskAnalysis();

        /*
         * note: this Transfer is not used for data transfer, but for fulfilling
         * parameter needs of addDropSupport and addDragSupport. The actual data
         * transfer ins realized through DNDItems in
         * RisikoMassnahmenUmsetzungDragListener and
         * RisikoMassnahmenUmsetzungDropListener
         */
        Transfer[] types = new Transfer[] { RisikoMassnahmenUmsetzungTransfer.getInstance() };
        int operations = DND.DROP_COPY | DND.DROP_MOVE;

        viewerScenario.addDropSupport(operations, types, new RisikoMassnahmenUmsetzungDropListener(viewerScenario));

        viewer.addDragSupport(operations, types, new RisikoMassnahmenUmsetzungDragListener(viewer, cnaElement));

    }

    private void addFilterListeners() {

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

                Button thisButton = (Button) event.widget;

                if (thisButton.getSelection()) {
                    viewer.addFilter(ownControlFilter);
                    refresh();
                } else {
                    viewer.removeFilter(ownControlFilter);
                    refresh();
                }
            }
        });

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

                Button thisButton = (Button) event.widget;

                if (thisButton.getSelection()) {
                    viewer.addFilter(controlFilter);
                    refresh();
                } else {
                    viewer.removeFilter(controlFilter);
                    refresh();
                }
            }
        });

        
    }

    @Override
    protected void doAfterUpdateFilter() {
        /*nothing to do*/
    }

    @Override
    protected void doAfterRemoveSearchFilter() {
        /* nothing to do */

    }


    private void addButtonListeners() {

        /*
         * Listener opens Dialog for creation of new RisikoMassnahmenUmsetzung
         */
        buttonNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {


                /* create new RisikoMassnahmenUmsetzung */
                itemsToCheckForUniqueNumber = new RiskAnalysisDialogItems<>(
                        getRiskAnalysisWizard().getAllMassnahmenUmsetzungen(),
                        MassnahmenUmsetzung.class);
                final NewRisikoMassnahmeDialog dialog = new NewRisikoMassnahmeDialog(
                        rootContainer.getShell(),
                        itemsToCheckForUniqueNumber);
                int result = dialog.open();

                if (result == Window.OK) {
                    /* add new RisikoMassnahmenUmsetzung to List and viewer */
                    getRiskAnalysisWizard().addRisikoMassnahmeUmsetzung(dialog.getNewRisikoMassnahme());
                    refresh();
                    packAllMassnahmeColumns();
                }
            }
        });
        
        /* Listener opens Dialog for editing the selected Gefaehrdung */
        buttonEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editOwnControl();
            }
        });

        /* Listener opens MessageDialog and deletes selected Massnahme */
        buttonDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                MassnahmenUmsetzung selectedMassnahmenUmsetzung = (MassnahmenUmsetzung) selection.getFirstElement();

                /* only RisikoMassnahmenUmsetzungen can be deleted */
                if (selectedMassnahmenUmsetzung instanceof RisikoMassnahmenUmsetzung) {
                    RisikoMassnahmenUmsetzung rsUmsetzung = (RisikoMassnahmenUmsetzung) selectedMassnahmenUmsetzung;

                    /* ask user to confirm */
                    boolean confirmed = MessageDialog.openQuestion(rootContainer.getShell(),
                            Messages.AdditionalSecurityMeasuresPage_15,
                            NLS.bind(Messages.AdditionalSecurityMeasuresPage_10, selectedMassnahmenUmsetzung.getTitle()));
                    /* delete */
                    if (confirmed) {
                        deleteOwnControl(rsUmsetzung);
                        refresh();
                    }
                }
            }
        });

    }


    @Override
    protected void addControls(Composite parent) {

        Composite controls = new Composite(parent, SWT.NONE | SWT.FULL_SELECTION);
        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_CONTROLS).generateLayout(controls);
        GridDataFactory.fillDefaults().applyTo(controls);

        addDeleteGefaehrdungButton(controls);

        Composite right = new Composite(controls, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(DEFAULT_MARGINS).generateLayout(right);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.RIGHT, SWT.TOP).applyTo(right);

        super.addButtons(right, Messages.AdditionalSecurityMeasuresPage_12);
        addFilters(right);

    }

    private void addDeleteGefaehrdungButton(Composite parent) {

        /* group the buttons with Group */
        Group groupButtons = new Group(parent, SWT.SHADOW_ETCHED_OUT);
        groupButtons.setText(Messages.AdditionalSecurityMeasuresPage_7);

        /* delete button */
        buttonRemoveControlFromScenario = new Button(groupButtons, SWT.PUSH);
        buttonRemoveControlFromScenario.setText(Messages.AdditionalSecurityMeasuresPage_8);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonRemoveControlFromScenario);

        GridLayoutFactory.fillDefaults().margins(DEFAULT_MARGINS).generateLayout(groupButtons);
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.TOP).applyTo(groupButtons);
    }

    @Override
    protected void addFilters(Composite parent) {
        Composite compositeFilter = new Composite(parent, SWT.NONE);

        /* filter button - OwnGefaehrdungen only */
        buttonOwnGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonOwnGefaehrdungen.setText(Messages.AdditionalSecurityMeasuresPage_19);

        /* filter button - BSI Gefaehrdungen only */
        buttonGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonGefaehrdungen.setText(Messages.AdditionalSecurityMeasuresPage_20);

        /* filter button - search */
        Composite search = new Composite(compositeFilter, SWT.NONE);
        new Label(search, SWT.NONE).setText(Messages.ChooseGefaehrdungPage_10);
        textSearch = new Text(search, SWT.SINGLE | SWT.BORDER);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(DEFAULT_MARGINS).generateLayout(search);
        GridLayoutFactory.fillDefaults().generateLayout(compositeFilter);
        GridDataFactory.fillDefaults().applyTo(search);
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(compositeFilter);

    }




}
