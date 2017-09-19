/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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

package sernet.gs.ui.rcp.gsimport;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.RightsEnabledView;

/**
 * View for mapping the specified Subtypes of the GS-Tool with the verinice
 * Types Frontend for GSToolTypeMapper
 * 
 * @author shagedorn
 * 
 */
public class GstoolImportMappingView extends RightsEnabledView implements IGstoolImportMappingChangeListener {

    private static final Logger LOG = Logger.getLogger(GstoolImportMappingView.class);
    public static final String ID = "sernet.gs.ui.rcp.gsimport.gstoolimportmappingview"; //$NON-NLS-1$

    private TableViewer viewer;
    private TableSorter tableSorter = new TableSorter();
    private GsImportMappingLabelProvider labelProvider;
    private GsImportMappingContentProvider contentProvider;
    private WorkspaceJob initDataJob;
    private Action addMappingEntryAction;
    private Action deleteMappingEntryAction;
    private IModelLoadListener modelLoadListener;

    public GstoolImportMappingView() {
        initDataJob = new WorkspaceJob("") { //$NON-NLS-1$
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    init();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.", e); //$NON-NLS-1$ //$NON-NLS-2$
                } finally {
                    monitor.done();
                }
                return status;
            }

        };
        GstoolTypeMapper.addChangeListener(this);
    }

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    private void init() {
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    protected void startInitDataJob() {
        if (CnAElementFactory.isIsoModelLoaded()) {
            JobScheduler.scheduleInitJob(initDataJob);
        } else if (modelLoadListener == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No model loaded, adding model load listener."); //$NON-NLS-1$
            }
            createModelLoadListener();
        }
    }

    private void createModelLoadListener() {
        // model is not loaded yet: add a listener to load data when it's loaded
        modelLoadListener = new IModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                // nothing to do
            }

            @Override
            public void loaded(BSIModel model) {
                JobScheduler.scheduleInitJob(initDataJob);
                CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
            }

            @Override
            public void loaded(ISO27KModel model) {
                // nothing to do
            }

            @Override
            public void loaded(BpModel model) {
                // nothing to do
            }

            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }
        };
        CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
            alternateCreatePartControl(parent);

    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.addMappingEntryAction);
        manager.add(this.deleteMappingEntryAction);
    }

    private void alternateCreatePartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        this.labelProvider = new GsImportMappingLabelProvider();
        this.contentProvider = new GsImportMappingContentProvider(this);

        createTableViewer(parent);

        makeActions();
        fillLocalToolBar();
        getSite().setSelectionProvider(this.viewer);
        startInitDataJob();
    }

    private void createTableViewer(Composite parent) {
        TableViewerColumn gstoolTypeColumn;
        TableViewerColumn veriniceTypeColumn;

        final int gstoolTypeColumnWidth = 150;
        final int veriniceTypeColumnWidth = 80;

        this.viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        Table table = this.viewer.getTable();

        gstoolTypeColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
        gstoolTypeColumn.getColumn().setWidth(gstoolTypeColumnWidth);
        gstoolTypeColumn.getColumn().setText(Messages.GSImportMappingView_1);
        gstoolTypeColumn.getColumn().addSelectionListener(new SortSelectionAdapter(this, gstoolTypeColumn.getColumn(), 0));
        gstoolTypeColumn.setEditingSupport(new GsImportMappingStringEditingSupport(this.viewer, this));

        veriniceTypeColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
        veriniceTypeColumn.getColumn().setWidth(veriniceTypeColumnWidth);
        veriniceTypeColumn.getColumn().setText(Messages.GSImportMappingView_2);
        veriniceTypeColumn.getColumn().addSelectionListener(new SortSelectionAdapter(this, veriniceTypeColumn.getColumn(), 1));
        veriniceTypeColumn.setEditingSupport(new GsImportMappingComboBoxEditingSupport(this.viewer, this));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        this.viewer.setSorter(this.tableSorter);
        this.viewer.setContentProvider(this.contentProvider);
        this.viewer.setLabelProvider(this.labelProvider);

        this.viewer.setInput(new PlaceHolder("")); //$NON-NLS-1$

        this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    if (((IStructuredSelection) event.getSelection()).getFirstElement() instanceof GstoolImportMappingElement) {
                        deleteMappingEntryAction.setEnabled(true);
                    } else {
                        deleteMappingEntryAction.setEnabled(false);
                    }
                }
            }
        });
    }

    void refresh() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                StructuredSelection selection = (StructuredSelection) viewer.getSelection();
                viewer.setInput(GstoolTypeMapper.getGstoolSubtypesAsList());
                viewer.setSelection(selection, true);
            }
        });
    }

    private void makeActions() {
        this.addMappingEntryAction = new Action() {
            @Override
            public void run() {
                addMappingEntry();
            }
        };
        this.addMappingEntryAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PLUS));
        this.addMappingEntryAction.setEnabled(true);
        this.deleteMappingEntryAction = new Action() {
            @Override
            public void run() {
                deleteMappingEntry();
            }
        };
        this.deleteMappingEntryAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.MINUS));
        this.deleteMappingEntryAction.setEnabled(false);

    }

    private void addMappingEntry() {
        GstoolImportMappingElement mappingElement = new GstoolImportMappingElement("< " + Messages.GSImportMappingView_newEntry + " >", SonstIT.TYPE_ID); //$NON-NLS-1$ //$NON-NLS-2$
        GstoolTypeMapper.addGstoolSubtypeToPropertyFile(mappingElement);
        refresh();
        viewer.setSelection(new StructuredSelection(mappingElement), true);
    }

    private void deleteMappingEntry() {
        if (viewer.getSelection() instanceof StructuredSelection) {
            StructuredSelection selection = (StructuredSelection) viewer.getSelection();
            GstoolImportMappingElement deletedObject = null;
            Iterator iterator = selection.iterator();
            while (iterator.hasNext()) {
                deletedObject = (GstoolImportMappingElement) iterator.next();
                GstoolTypeMapper.removeGstoolSubtypeToPropertyFile(deletedObject);
            }
            refresh();
        } else {
            LOG.warn("wrong selection type", new IllegalArgumentException("wrong selection type")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static class SortSelectionAdapter extends SelectionAdapter {
        private GstoolImportMappingView gsiView;
        private TableColumn column;
        private int index;

        public SortSelectionAdapter(GstoolImportMappingView gsiView, TableColumn column, int index) {
            super();
            this.gsiView = gsiView;
            this.column = column;
            this.index = index;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            this.gsiView.tableSorter.setColumn(this.index);
            int dir = this.gsiView.viewer.getTable().getSortDirection();
            if (this.gsiView.viewer.getTable().getSortColumn() == this.column) {
                dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
            } else {
                dir = SWT.DOWN;
            }
            this.gsiView.viewer.getTable().setSortDirection(dir);
            this.gsiView.viewer.getTable().setSortColumn(this.column);
            this.gsiView.refresh();
        }
    }

    private static class TableSorter extends ViewerSorter {

        private int currentColumnIndex;
        private static final int DEFAULT_SORT_COLUMN = 0;
        private boolean isAscending = true;

        public TableSorter() {
            super();
            this.currentColumnIndex = DEFAULT_SORT_COLUMN;
            this.isAscending = true;
        }

        public void setColumn(int column) {
            if (column == this.currentColumnIndex) {
                // Same column as last sort; toggle the direction
                this.isAscending = !isAscending;
            } else {
                // New column; do an ascending sort
                this.currentColumnIndex = column;
                this.isAscending = true;
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

            int rc = 0;
            if ((e1 instanceof GstoolImportMappingElement && e2 instanceof GstoolImportMappingElement)) {
                GstoolImportMappingElement g1 = (GstoolImportMappingElement) e1;
                GstoolImportMappingElement g2 = (GstoolImportMappingElement) e2;
                if (this.currentColumnIndex == 0) {
                rc = g1.compareTo(g2);
                } else if (this.currentColumnIndex == 1) {
                    rc = (g1.getValue()).compareToIgnoreCase(g2.getValue());
                } else {
                    rc = 0;
                }
                // If descending order, flip the direction
                if (!isAscending) {
                    rc = -rc;
                }
            }
            return rc;

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GSTOOLIMPORT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.gsimport.IGstoolImportMappingChangeListener#mappingAdded(sernet.gs.ui.rcp.gsimport.GstoolImportMappingElement)
     */
    @Override
    public void mappingAdded(GstoolImportMappingElement mappingElement) {
        refresh();
        viewer.setSelection(new StructuredSelection(mappingElement), true);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.gsimport.IGstoolImportMappingChangeListener#mappingChanged(sernet.gs.ui.rcp.gsimport.GstoolImportMappingElement)
     */
    @Override
    public void mappingChanged(GstoolImportMappingElement mappingElement) {
        refresh();
        viewer.setSelection(new StructuredSelection(mappingElement), true);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.gsimport.IGstoolImportMappingChangeListener#mappingRemoved(sernet.gs.ui.rcp.gsimport.GstoolImportMappingElement)
     */
    @Override
    public void mappingRemoved(GstoolImportMappingElement mappingElement) {
        refresh();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        GstoolTypeMapper.removeChangeListener(this);
        super.dispose();
    }
}
