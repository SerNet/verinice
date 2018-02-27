/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportTemplateService.OutputFormat;
import sernet.verinice.interfaces.ReportDepositException;
import sernet.verinice.interfaces.ReportTemplateServiceException;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.rcp.ReportTemplateSync;
import sernet.verinice.rcp.RightsEnabledView;

/**
 *
 */
public class ReportDepositView extends RightsEnabledView {

    static final Logger LOG = Logger.getLogger(ReportDepositView.class);

    public static final String ID = "sernet.verinice.report.rcp.ReportDepositView";

    private RightsServiceClient rightsService;
    private ICommandService commandService;

    private TableSorter tableSorter = new TableSorter();
    private TableViewer viewer;

    private ReportDepositContentProvider contentprovider = new ReportDepositContentProvider(this);

    private RightsEnabledAction addTemplateAction;

    private RightsEnabledAction deleteTemplateAction;

    private RightsEnabledAction editTemplateAction;

    private Action doubleclickAction;

    private Action refreshAction;

    private WorkspaceJob loadDataJob;

    public ReportDepositView() {
        super();
        loadDataJob = new WorkspaceJob("load-deposit-content") {

            @Override
            public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {

                Activator.inheritVeriniceContextState();
                IStatus status = Status.OK_STATUS;
                Object content = getContent();
                setInput(content);

                return status;
            }
        };
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        parent.setLayout(new FillLayout());
        createTable(parent);
        getSite().setSelectionProvider(viewer);
        hookPageSelection();

        makeActions();
        hookActions();

        fillLocalToolBar();

        viewer.setInput(getContent());
    }

    private void createTable(Composite parent) {
        TableColumn reportNameColumn;
        TableColumn outputFormatColumn;
        TableColumn templateColumn;

        final int reportNameWidth = 200;
        final int outputFormatWidth = 200;
        final int templateWidth = 100;

        viewer = new TableViewer(parent,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        viewer.setContentProvider(contentprovider);
        viewer.setLabelProvider(new ReportDepositLabelProvider());
        Table table = viewer.getTable();

        reportNameColumn = new TableColumn(table, SWT.LEFT);
        reportNameColumn.setWidth(reportNameWidth);
        reportNameColumn.setText(Messages.ReportDepositView_1);
        reportNameColumn.addSelectionListener(new SortSelectionAdapter(this, reportNameColumn, 0));

        outputFormatColumn = new TableColumn(table, SWT.LEFT);
        outputFormatColumn.setWidth(outputFormatWidth);
        outputFormatColumn.setText(Messages.ReportDepositView_2);
        outputFormatColumn
                .addSelectionListener(new SortSelectionAdapter(this, outputFormatColumn, 1));

        templateColumn = new TableColumn(table, SWT.LEFT);
        templateColumn.setWidth(templateWidth);
        templateColumn.setText(Messages.ReportDepositView_3);
        templateColumn.addSelectionListener(new SortSelectionAdapter(this, templateColumn, 2));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer.setSorter(tableSorter);

        // ensure initial table sorting (by filename)
        ((TableSorter) viewer.getSorter()).setColumn(0);

    }

    private void hookPageSelection() {
        ISelectionListener selectionListener = new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                pageSelectionChanged(part, selection);
            }
        };
        getSite().getPage().addPostSelectionListener(selectionListener);
    }

    protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();
        // elementSelected(element);
        if (element instanceof ReportTemplateMetaData) {
            editTemplateAction.setEnabled(true);
            deleteTemplateAction.setEnabled(true);
        } else {
            editTemplateAction.setEnabled(false);
            deleteTemplateAction.setEnabled(false);
        }
    }

    private void hookActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleclickAction.run();
            }
        });
    }

    private void makeActions() {

        addTemplateAction = new RightsEnabledAction(ActionRightIDs.REPORTDEPOSITADD) {
            @Override
            public void doRun() {
                AddReportToDepositDialog dlg = new AddReportToDepositDialog(
                        Display.getDefault().getActiveShell());
                dlg.open();
                updateView();
            }
        };
        addTemplateAction.setText(Messages.ReportDepositView_5);
        addTemplateAction.setToolTipText(Messages.ReportDepositView_7);
        addTemplateAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
        addTemplateAction.setEnabled(true);

        deleteTemplateAction = new RightsEnabledAction(ActionRightIDs.REPORTDEPOSITDELETE) {

            @Override
            public void doRun() {
                int count = ((IStructuredSelection) viewer.getSelection()).size();
                boolean confirm = MessageDialog.openConfirm(viewer.getControl().getShell(),
                        Messages.ReportDepositView_15,
                        NLS.bind(Messages.ReportDepositView_16, count));
                if (!confirm) {
                    return;
                }
                deleteAttachments();
                updateView();
            }
        };

        deleteTemplateAction.setText(Messages.ReportDepositView_13);
        deleteTemplateAction.setToolTipText(Messages.ReportDepositView_14);
        deleteTemplateAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DELETE));
        deleteTemplateAction.setEnabled(false);

        editTemplateAction = new RightsEnabledAction(ActionRightIDs.REPORTDEPOSITEDIT) {

            @Override
            public void doRun() {
                int count = ((IStructuredSelection) viewer.getSelection()).size();
                if (count == 1) {
                    AddReportToDepositDialog dlg = new AddReportToDepositDialog(
                            Display.getDefault().getActiveShell(),
                            (ReportTemplateMetaData) ((IStructuredSelection) viewer.getSelection())
                                    .getFirstElement());
                    dlg.open();
                    updateView();
                } else {
                    MessageDialog.openWarning(Display.getDefault().getActiveShell(),
                            Messages.ReportDepositView_20, Messages.ReportDepositView_21);
                    return;
                }
            }
        };

        editTemplateAction.setText(Messages.ReportDepositView_17);
        editTemplateAction.setToolTipText(Messages.ReportDepositView_18);
        editTemplateAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EDIT));
        editTemplateAction.setEnabled(false);

        refreshAction = new Action() {

            @Override
            public void run() {
                JobScheduler.scheduleInitJob(loadDataJob);
            }
        };

        refreshAction.setText(Messages.ReportDepositView_19);
        refreshAction.setToolTipText(Messages.ReportDepositView_19);
        refreshAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
        refreshAction.setEnabled(true);

        doubleclickAction = new Action() {

            @Override
            public void run() {
                editTemplateAction.run();
            }
        };

    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.refreshAction);
        manager.add(this.addTemplateAction);
        manager.add(this.editTemplateAction);
        manager.add(this.deleteTemplateAction);
    }

    private static class ReportDepositLabelProvider extends LabelProvider
            implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                if (element instanceof PlaceHolder) {
                    if (columnIndex == 1) {
                        PlaceHolder ph = (PlaceHolder) element;
                        return ph.getTitle();
                    }
                    return ""; //$NON-NLS-1$
                }
                ReportTemplateMetaData data = (ReportTemplateMetaData) element;
                switch (columnIndex) {
                case 0:
                    return data.getOutputname(); // $NON-NLS-1$
                case 1:
                    StringBuilder sb = new StringBuilder();
                    OutputFormat[] formats = data.getOutputFormats();
                    for (int i = 0; i < formats.length; i++) {
                        sb.append(formats[i]);
                        if (i != formats.length - 1) {
                            sb.append(", ");
                        }
                    }
                    return sb.toString(); // $NON-NLS-1$
                case 2:
                    return data.getFilename(); // $NON-NLS-1$
                default:
                    return null;
                }
            } catch (Exception e) {
                LOG.error("Error while getting column text", e); //$NON-NLS-1$
                throw new RuntimeException(e);
            }
        }

    }

    private static class SortSelectionAdapter extends SelectionAdapter {
        private ReportDepositView depositView;
        private TableColumn column;
        private int index;

        public SortSelectionAdapter(ReportDepositView depositView, TableColumn column, int index) {
            super();
            this.depositView = depositView;
            this.column = column;
            this.index = index;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            depositView.tableSorter.setColumn(index);
            int dir = depositView.viewer.getTable().getSortDirection();
            if (depositView.viewer.getTable().getSortColumn() == column) {
                dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
            } else {

                dir = SWT.DOWN;
            }
            depositView.viewer.getTable().setSortDirection(dir);
            depositView.viewer.getTable().setSortColumn(column);
            depositView.viewer.refresh();
        }

    }

    private static class TableSorter extends ViewerSorter {
        private int propertyIndex;
        private static final int DEFAULT_SORT_COLUMN = 0;
        private static final int DESCENDING = 1;
        private static final int ASCENDING = 0;
        private int direction = ASCENDING;

        public TableSorter() {
            super();
            this.propertyIndex = DEFAULT_SORT_COLUMN;
            this.direction = DESCENDING;
        }

        public void setColumn(int column) {
            if (column == this.propertyIndex) {
                // Same column as last sort; toggle the direction
                direction = (direction == ASCENDING) ? DESCENDING : ASCENDING;
            } else {
                // New column; do an ascending sort
                this.propertyIndex = column;
                direction = ASCENDING;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
         * .viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            ReportTemplateMetaData data1 = (ReportTemplateMetaData) e1;
            ReportTemplateMetaData data2 = (ReportTemplateMetaData) e2;
            int rc = 0;
            if (e1 == null) {
                if (e2 != null) {
                    rc = 1;
                }
            } else if (e2 == null) {
                rc = -1;
            } else {
                // e1 and e2 != null
                switch (propertyIndex) {
                case 0:
                    rc = comporeToLowerCase(data1.getDecoratedOutputname(),
                            data2.getDecoratedOutputname());
                    break;
                case 1:
                    // implement a sorted list here that needs to be compared
                    String s1 = getSortedOutputFormatsString(data1.getOutputFormats());
                    String s2 = getSortedOutputFormatsString(data2.getOutputFormats());
                    if (s1 != null && s2 != null) {
                        rc = s1.compareTo(s2);
                    }
                    break;
                case 2:
                    rc = comporeToLowerCase(data1.getFilename(), data2.getFilename());
                    break;
                default:
                    rc = 0;
                }
            }

            // If descending order, flip the direction
            if (direction == DESCENDING) {
                rc = -rc;
            }
            return rc;
        }

        private int comporeToLowerCase(String filename1, String filename2) {
            int rc = 0;
            if (filename1 != null && filename2 != null) {
                rc = filename1.toLowerCase().compareTo(filename2.toLowerCase());
            }
            return rc;
        }

        private String getSortedOutputFormatsString(OutputFormat[] input) {
            ArrayList<String> list = new ArrayList<>();
            for (OutputFormat format : input) {
                list.add(format.toString());
            }
            Collections.sort(list);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i != list.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();

        }

    }

    @Override
    public String getRightID() {
        return ActionRightIDs.REPORTDEPOSIT;
    }

    @Override
    public String getViewId() {
        return ID;
    }

    public RightsServiceClient getRightsService() {
        if (rightsService == null) {
            rightsService = (RightsServiceClient) VeriniceContext
                    .get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

    private Object getContent() {
        try {
            Set<ReportTemplateMetaData> templateSet = getReportService()
                    .getServerReportTemplates(Locale.getDefault().getLanguage());
            return templateSet.toArray(new ReportTemplateMetaData[templateSet.size()]);
        } catch (ReportTemplateServiceException e) {
            String msg = "Something went wrong with reading the propertyfiles";
            ExceptionUtil.log(e, msg);
        } catch (Exception e) {
            String msg = "Error reading reports from deposit";
            ExceptionUtil.log(e, msg);
        }
        return new PlaceHolder(Messages.ReportDepositView_4);
    }

    private IReportDepositService getReportService() {
        return ServiceFactory.lookupReportDepositService();
    }

    private void deleteAttachments() {
        Iterator<?> iterator = ((IStructuredSelection) viewer.getSelection()).iterator();
        while (iterator.hasNext()) {
            ReportTemplateMetaData sel = (ReportTemplateMetaData) iterator.next();
            try {
                ServiceFactory.lookupReportDepositService().remove(sel,
                        Locale.getDefault().getLanguage());
            } catch (ReportDepositException e) {
                ExceptionUtil.log(e, "Error deleting Reporttemplate:\t" + sel.getOutputname());
            }
        }
    }

    private void updateView() {
        ReportTemplateSync.sync();
        setInput(getContent());
    }

    private void setInput(final Object content) {
        getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                viewer.setInput(content);

            }
        });
    }

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }
}
