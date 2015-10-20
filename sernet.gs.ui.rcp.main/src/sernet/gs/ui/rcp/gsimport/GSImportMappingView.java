/**
 *
 */
package sernet.gs.ui.rcp.gsimport;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.rcp.RightsEnabledView;

/**
 * @author shagedorn
 *
 */
public class GSImportMappingView extends RightsEnabledView {

    private static final Logger LOG = Logger.getLogger(GSImportMappingView.class);

    public static final String ID = "sernet.gs.ui.rcp.gsimport.gsimportmappingview";

    // new way
    private TableViewer viewer;
    private TableSorter tableSorter = new TableSorter();
    private ISelectionListener selectionListener;
    private GsImportMappingLabelProvider labelProvider;
    private GsImportMappingContentProvider contentProvider;

    private Action addMappingEntryAction;

    private Action deleteMappingEntryAction;


    // old way
    private Table mainTable;
    private TableItem[] items;
    private Set<Text> texts;
    private Set<CCombo> combos;

    private boolean useNewWay = true;


    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        if(this.useNewWay) {
            alternateCreatePartControl(parent);
        } else {

            this.combos = new HashSet<>();
            this.texts = new HashSet<>();

            final int layoutMarginWidth = 5;
            final int layoutMarginHeight = 10;
            final int layoutSpacing = 3;
            final int gdVerticalSpan = 4;
            final int mainTableItemHeightFactor = 20;
            final int tableColumnDefaultWidth = 225;

            FillLayout layout = new FillLayout();
            layout.type = SWT.VERTICAL;
            layout.marginWidth = layoutMarginWidth;
            layout.marginHeight = layoutMarginHeight;
            layout.spacing = layoutSpacing;

            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;

            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(gridLayout);

            createTable(gdVerticalSpan, mainTableItemHeightFactor, tableColumnDefaultWidth, container);

            this.mainTable.setSortColumn(this.mainTable.getColumn(0));
            this.mainTable.setSortDirection(SWT.DOWN);

        }

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
    }

    private void createTableViewer(Composite parent) {
        TableViewerColumn gstoolTypeColumn;
        TableViewerColumn veriniceTypeColumn;

        final int gstoolTypeColumnWidth = 150;
        final int veriniceTypeColumnWidth = 80;

        this.viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        //        viewer.setContentProvider(contentProvider);
        //        viewer.setLabelProvider(new ValidationLabelProvider());
        Table table = this.viewer.getTable();

        gstoolTypeColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
        gstoolTypeColumn.getColumn().setWidth(gstoolTypeColumnWidth);
        gstoolTypeColumn.getColumn().setText(Messages.GSImportMappingView_1);
        gstoolTypeColumn.getColumn().addSelectionListener(new SortSelectionAdapter(this, gstoolTypeColumn.getColumn(), 0));
        gstoolTypeColumn.setEditingSupport(new GsImportMappingStringEditingSupport(this.viewer));

        veriniceTypeColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
        veriniceTypeColumn.getColumn().setWidth(veriniceTypeColumnWidth);
        veriniceTypeColumn.getColumn().setText(Messages.GSImportMappingView_2);
        veriniceTypeColumn.getColumn().addSelectionListener(new SortSelectionAdapter(this, veriniceTypeColumn.getColumn(), 1));
        veriniceTypeColumn.setEditingSupport(new GsImportMappingComboBoxEditingSupport(this.viewer));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        this.viewer.setSorter(this.tableSorter);
        this.viewer.setContentProvider(this.contentProvider);
        this.viewer.setLabelProvider(this.labelProvider);

        this.viewer.setInput(GstoolTypeMapper.getGstoolSubtypes());

        this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if(event.getSelection() instanceof IStructuredSelection) {
                    if(((IStructuredSelection)event.getSelection()).getFirstElement() instanceof Object[]) {
                        deleteMappingEntryAction.setEnabled(true);
                    } else {
                        deleteMappingEntryAction.setEnabled(false);
                    }
                }
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

    }

    private void deleteMappingEntry() {

    }


    private static class SortSelectionAdapter extends SelectionAdapter {
        private GSImportMappingView gsiView;
        private TableColumn column;
        private int index;

        public SortSelectionAdapter(GSImportMappingView gsiView, TableColumn column, int index) {
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
            this.gsiView.viewer.refresh();
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
            this.direction = ASCENDING;
        }

        public void setColumn(int column) {
            if (column == this.propertyIndex) {
                // Same column as last sort; toggle the direction
                this.direction = (this.direction == ASCENDING) ? DESCENDING : ASCENDING;
            } else {
                // New column; do an ascending sort
                this.propertyIndex = column;
                this.direction = ASCENDING;
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
            Object[] a1 = (Object[]) e1;
            Object[] a2 = (Object[]) e2;
            int rc = 0;
            if (e1 == null) {
                if (e2 != null) {
                    rc = 1;
                }
            } else if (e2 == null) {
                rc = -1;
            } else {
                // e1 and e2 != null
                switch (this.propertyIndex) {
                case 0:
                    if(a1.length == 2 && a2.length == 2 && a1[0] != null && a2[0] != null){
                        rc = ((String)a1[0]).compareTo((String)a2[0]);
                        break;
                    }
                case 1:
                    if(a1.length == 2 && a2.length == 2 && a1[1] != null && a2[1] != null){
                        rc = ((String)a1[1]).compareTo((String)a2[1]);
                        break;
                    }
                default:
                    rc = 0;
                }
            }

            // If descending order, flip the direction
            if (this.direction == DESCENDING) {
                rc = -rc;
            }
            return rc;
        }

    }




    /**
     * @param gdVerticalSpan
     * @param mainTableItemHeightFactor
     * @param tableColumnDefaultWidth
     * @param container
     */
    private void createTable(final int gdVerticalSpan, final int mainTableItemHeightFactor, final int tableColumnDefaultWidth, Composite container) {

        this.mainTable = new Table(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.verticalSpan = gdVerticalSpan;
        int listHeight = this.mainTable.getItemHeight() * mainTableItemHeightFactor;
        Rectangle trim = this.mainTable.computeTrim(0, 0, 0, listHeight);
        gridData.heightHint = trim.height;
        this.mainTable.setLayoutData(gridData);
        this.mainTable.setHeaderVisible(true);
        this.mainTable.setLinesVisible(true);

        // set the columns of the table
        String[] titles = { Messages.GSImportMappingView_1, Messages.GSImportMappingView_2};


        for (String title2 : titles) {
            TableColumn column = new TableColumn(this.mainTable, SWT.NONE);
            column.setText(title2);
            column.setWidth(tableColumnDefaultWidth);
        }

        // fill table
        initData();
    }

    /**
     *
     */
    private void initData() {
        Set<String> gstoolSubtypes = GstoolTypeMapper.getGstoolSubtypes().keySet();
        String[] propertyColumns = gstoolSubtypes.toArray(new String[gstoolSubtypes.size()]);
        for (int i = 1; i < propertyColumns.length; i++) {
            new TableItem(this.mainTable, SWT.NONE);
        }

        this.items = this.mainTable.getItems();
        // fill the combos with content
        for (int i = 0; i < this.items.length; i++) {
            handleSubtype(propertyColumns, i);
        }
    }

    /**
     * @param propertyColumns
     * @param i
     */
    private void handleSubtype(String[] propertyColumns, int i) {
        TableEditor editor;
        editor = new TableEditor(this.mainTable);

        Text text = new Text(this.mainTable, SWT.NONE);
        text.setText(propertyColumns[i + 1]);
        text.setEditable(false);
        editor.grabHorizontal = true;
        editor.setEditor(text, this.items[i], 0);
        this.texts.add(text);

        editor = new TableEditor(this.mainTable);
        final CCombo combo = new CCombo(this.mainTable, SWT.NONE);
        combo.setText(""); //$NON-NLS-1$
        String gstoolSubtype = text.getText();
        String veriniceValue = GstoolTypeMapper.getGstoolSubtypes().get(gstoolSubtype);
        int index = -1;
        for(int j = 0; j < combo.getItems().length; j++) {
            if(veriniceValue.equals(combo.getItem(j))) {
                index = j;
                break;
            }
        }
        if(index > -1) {
            combo.select(index);
        }
        this.combos.add(combo);

        editor.grabHorizontal = true;
        editor.setEditor(combo, this.items[i], 1);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GSTOOLIMPORT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

}
