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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.service.commands.CheckOwnGefaehrdungInUseCommand;
import sernet.verinice.service.commands.risk.LoadAssociatedGefaehrdungen;
import sernet.verinice.service.commands.risk.UpdateRiskAnalysis;

/**
 * WizardPage which lists all Gefaehrdungen from BSI IT-Grundschutz-Kataloge and
 * additionally all self-defined OwnGefaehrdungen in a CheckboxTableViewer.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
@SuppressWarnings("restriction")
public class ChooseGefaehrdungPage extends RiskAnalysisWizardPage<CheckboxTableViewer> {

    private TableViewerColumn checkboxColumn;
    private TableViewerColumn imageColumn;
    private TableViewerColumn numberColumn;
    private TableViewerColumn nameColumn;
    private TableViewerColumn categoryColumn;
    private RiskAnalysisDialogItems<Gefaehrdung> itemsToCheckForUniqueNumber;
    private static final Logger LOG = Logger.getLogger(ChooseGefaehrdungPage.class);

    /**
     * Constructor sets title an description of WizardPage.
     */
    protected ChooseGefaehrdungPage() {
        super(Messages.ChooseGefaehrdungPage_0, Messages.ChooseGefaehrdungPage_1,
                Messages.ChooseGefaehrdungPage_2);
    }

    @Override
    protected void setColumns() {
        checkboxColumn = new TableViewerColumn(viewer, SWT.LEFT);
        checkboxColumn.getColumn().setText(""); //$NON-NLS-1$

        imageColumn = new TableViewerColumn(viewer, SWT.LEFT);
        imageColumn.getColumn().setText(""); //$NON-NLS-1$

        numberColumn = new TableViewerColumn(viewer, SWT.LEFT);
        numberColumn.getColumn().setText(Messages.ChooseGefaehrdungPage_5);

        nameColumn = new TableViewerColumn(viewer, SWT.LEFT);
        nameColumn.getColumn().setText(Messages.ChooseGefaehrdungPage_6);

        categoryColumn = new TableViewerColumn(viewer, SWT.LEFT);
        categoryColumn.getColumn().setText(Messages.ChooseGefaehrdungPage_7);
    }
    


    @Override
    protected void addSpecificListenersForPage() {

        addButtonListeners();
        addFilterListeners();

        /*
         * listener adds/removes Gefaehrdungen to Array of selected
         * Gefaehrdungen
         */
        viewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Gefaehrdung currentGefaehrdung = (Gefaehrdung) event.getElement();
                associateGefaehrdung(currentGefaehrdung, event.getChecked());
            }

        });

        /* listener opens edit Dialog for the selected Gefaehrdung */
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            /**
             * Notifies of a double click.
             * 
             * @param event
             *            event object describing the double-click
             */
            @Override
            public void doubleClick(DoubleClickEvent event) {
                /* retrieve selected Gefaehrdung and open edit dialog with it */
                editGefaehrdung();
            }
        });
    }

    private void editGefaehrdung() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        IGSModel selectedGefaehrdung = (IGSModel) selection.getFirstElement();
        if (selectedGefaehrdung instanceof OwnGefaehrdung) {
            OwnGefaehrdung ownGefSelected = (OwnGefaehrdung) selectedGefaehrdung;
            boolean isEditable = isUnusedOwnGefaehrdung(ownGefSelected);
            if (isEditable) {
                itemsToCheckForUniqueNumber = new RiskAnalysisDialogItems<>(
                        getRiskAnalysisWizard().getAllGefaehrdungen(), Gefaehrdung.class);
                final EditGefaehrdungDialog dialog = new EditGefaehrdungDialog(
                        rootContainer.getShell(),
                        ownGefSelected, itemsToCheckForUniqueNumber);
                dialog.open();
                refresh();
            } else {
                MessageDialog.openError(getShell(), Messages.ChooseGefaehrdungPage_Error_0,
                        NLS.bind(Messages.ChooseGefaehrdungPage_Error_2, ownGefSelected.getId()));
            }
        }
    }

    private boolean isUnusedOwnGefaehrdung(OwnGefaehrdung ownGefaehrdung) {
        boolean isUnused = false;
        CheckOwnGefaehrdungInUseCommand command = new CheckOwnGefaehrdungInUseCommand(ownGefaehrdung);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            isUnused = !command.isInUse();
        } catch (CommandException e) {
            LOG.warn("Error while checking if OwnGefaehrdung is used", e);
        }
        return isUnused;
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
                    checkAllSelectedGefaehrdungen();
                    packAllColumns();
                } else {
                    viewer.removeFilter(gefaehrdungFilter);
                    refresh();
                    assignBausteinGefaehrdungen();
                    refresh();
                    checkAllSelectedGefaehrdungen();
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
                    addFilter();
                } else {
                    removeFilter();
                }
            }

            private void removeFilter() {
                viewer.removeFilter(ownGefaehrdungFilter);
                refresh();
                assignBausteinGefaehrdungen();
                checkAllSelectedGefaehrdungen();
                packAllColumns();
            }

            private void addFilter() {
                viewer.addFilter(ownGefaehrdungFilter);
                viewer.refresh();
                checkAllSelectedGefaehrdungen();
                packAllColumns();
            }
        });

    }

    @Override
    protected void doAfterUpdateFilter() {
        checkAllSelectedGefaehrdungen();
    }

    @Override
    protected void doAfterRemoveSearchFilter() {
        checkAllSelectedGefaehrdungen();
    }

    private void addButtonListeners() {

        /* Listener opens MessageDialog and deletes selected Gefaehrdung */
        buttonDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Gefaehrdung selectedGefaehrdung = (Gefaehrdung) selection.getFirstElement();
                if (selectedGefaehrdung instanceof OwnGefaehrdung) {
                    boolean isDeletable = isUnusedOwnGefaehrdung(
                            (OwnGefaehrdung) selectedGefaehrdung);
                    if (isDeletable) {
                        /* ask user to confirm */
                        boolean confirmed = MessageDialog.openQuestion(rootContainer.getShell(),
                                Messages.ChooseGefaehrdungPage_14,
                                NLS.bind(Messages.ChooseGefaehrdungPage_15,
                                        selectedGefaehrdung.getTitel()));
                        if (confirmed) {
                            deleteOwnGefaehrdung((OwnGefaehrdung) selectedGefaehrdung);
                            assignBausteinGefaehrdungen();
                            refresh();
                        }
                    } else {
                        String message = NLS.bind(Messages.ChooseGefaehrdungPage_Error_2,
                                selectedGefaehrdung.getId());
                        MessageDialog.openError(getShell(), Messages.ChooseGefaehrdungPage_Error_1, message);
                    }
                }
            }
        });

        /* Listener opens Dialog for creation of new OwnGefaehrdung */
        buttonNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                List<OwnGefaehrdung> arrListOwnGefaehrdungen = getRiskAnalysisWizard()
                        .getAllOwnGefaehrdungen();
                itemsToCheckForUniqueNumber = new RiskAnalysisDialogItems<>(
                        getRiskAnalysisWizard().getAllGefaehrdungen(), Gefaehrdung.class);
                final NewGefaehrdungDialog dialog = new NewGefaehrdungDialog(
                        rootContainer.getShell(), arrListOwnGefaehrdungen,
                        itemsToCheckForUniqueNumber);
                dialog.open();
                getRiskAnalysisWizard().addOwnGefaehrdungen();
                refresh();
                assignBausteinGefaehrdungen();
            }
        });

        /* Listener opens Dialog for editing the selected Gefaehrdung */
        buttonEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                editGefaehrdung();

            }

        });

    }


    protected void associateGefaehrdung(Gefaehrdung currentGefaehrdung, boolean select) {

        if (select) {
            getRiskAnalysisWizard().addAssociatedGefaehrdung(currentGefaehrdung);
        } else {
            try {
                getRiskAnalysisWizard().removeAssociatedGefaehrdung(currentGefaehrdung);
            } catch (Exception e) {
                ExceptionUtil.log(e,
                        NLS.bind(Messages.ChooseGefaehrdungPage_18, currentGefaehrdung.getTitel()));
            }
        }
        checkPageComplete();

    }

    /**
     * Fills the CheckboxTableViewer with all Gefaehrdungen available. Is
     * processed each time the WizardPage is set visible.
     */
    @Override
    protected void doInitContents() {

        ArrayList<Gefaehrdung> arrListAllGefaehrdungen = (ArrayList<Gefaehrdung>) getRiskAnalysisWizard().getAllGefaehrdungen();

        /* map a domain model object into multiple images and text labels */
        viewer.setLabelProvider(new CheckboxTableViewerLabelProvider());

        /* map domain model into array */
        viewer.setContentProvider(new ArrayContentProvider());
        /* associate domain model with viewer */
        viewer.setInput(arrListAllGefaehrdungen);
        viewer.setSorter(new GefaehrdungenSorter());

        assignBausteinGefaehrdungen();
        checkAllSelectedGefaehrdungen();
        packAllColumns();

        getRiskAnalysisWizard().setCanFinish(false);
        checkPageComplete();
    }

    private void checkAllSelectedGefaehrdungen() {
        List<Gefaehrdung> toCheck = new ArrayList<>();
        for (GefaehrdungsUmsetzung associatedGefaehrdung : getRiskAnalysisWizard().getAssociatedGefaehrdungen()) {
            if (associatedGefaehrdung != null) {
                for (Gefaehrdung gefaehrdung : getRiskAnalysisWizard().getAllGefaehrdungen()) {
                    if (gefaehrdung != null && gefaehrdung.getId() != null && gefaehrdung.getId().equals(associatedGefaehrdung.getId())) {
                        toCheck.add(gefaehrdung);
                    }
                }
            }
        }

        Gefaehrdung[] checkarray = toCheck.toArray(new Gefaehrdung[toCheck.size()]);
        viewer.setCheckedElements(checkarray);
    }

    /**
     * Marks all checkboxes of Gefaehrdungen associated to the selected
     * Baustein.
     */
    private void assignBausteinGefaehrdungen() {
        try {
            LoadAssociatedGefaehrdungen command = new LoadAssociatedGefaehrdungen(getRiskAnalysisWizard().getCnaElement());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<GefaehrdungsUmsetzung> list = command.getAssociatedGefaehrdungen();

            for (GefaehrdungsUmsetzung selectedGefaehrdung : list) {
                if (selectedGefaehrdung != null) {
                    for (Gefaehrdung gefaehrdung : getRiskAnalysisWizard().getAllGefaehrdungen()) {
                        if (gefaehrdung != null && gefaehrdung.getId() != null && gefaehrdung.getId().equals(selectedGefaehrdung.getId())) {
                            associateGefaehrdung(gefaehrdung, true);
                        }
                    }
                }
            }
            UpdateRiskAnalysis updateCommand = new UpdateRiskAnalysis(getRiskAnalysisWizard().getFinishedRiskAnalysisLists());
            updateCommand = ServiceFactory.lookupCommandService().executeCommand(updateCommand);
        } catch (CommandException e) {
            ExceptionUtil.log(e, ""); //$NON-NLS-1$
        }
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
        categoryColumn.getColumn().pack();
    }

    /**
     * Activates the next button, if the List of selected Gefaehrdungen is not
     * empty.
     */
    private void checkPageComplete() {
        if (getRiskAnalysisWizard().getAssociatedGefaehrdungen().isEmpty()) {
            setPageComplete(false);
        } else {
            setPageComplete(true);
        }
    }


    /**
     * Deletes a OwnGefaehrdung.
     * 
     * @param delGefaehrdung
     *            the (Own)Gefaehrdung to delete
     */
    private void deleteOwnGefaehrdung(Gefaehrdung delGefaehrdung) {
        ArrayList<Gefaehrdung> arrListAllGefaehrdungen = (ArrayList<Gefaehrdung>) getRiskAnalysisWizard().getAllGefaehrdungen();
        List<GefaehrdungsUmsetzung> arrListAssociatedGefaehrdungen = getRiskAnalysisWizard().getAssociatedGefaehrdungen();
        List<OwnGefaehrdung> arrListOwnGefaehrdungen = getRiskAnalysisWizard().getAllOwnGefaehrdungen();

        try {
            if (arrListOwnGefaehrdungen.contains(delGefaehrdung)) {
                /* delete OwnGefaehrdung from Database */
                OwnGefaehrdungHome.getInstance().remove((OwnGefaehrdung) delGefaehrdung);

                /* delete OwnGefaehrdung from List of OwnGefaehrdungen */
                arrListOwnGefaehrdungen.remove(delGefaehrdung);

                /* delete OwnGefaehrdung from List of selected Gefaehrdungen */
                GefaehrdungsUtil.removeBySameId(arrListAssociatedGefaehrdungen, delGefaehrdung);

                /* delete OwnGefaehrdung from list of all Gefaehrdungen */
                if (arrListAllGefaehrdungen.contains(delGefaehrdung)) {
                    arrListAllGefaehrdungen.remove(delGefaehrdung);
                }
            }
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.ChooseGefaehrdungPage_20);
        }
    }


    @Override
    protected CheckboxTableViewer initializeViewer(Composite parent) {
        return CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
    }


}
