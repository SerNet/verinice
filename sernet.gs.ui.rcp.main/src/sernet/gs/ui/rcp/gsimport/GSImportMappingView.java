/**
 *
 */
package sernet.gs.ui.rcp.gsimport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.rcp.RightsEnabledView;

/**
 * @author shagedorn
 *
 */
public class GSImportMappingView extends RightsEnabledView {

    private static final Logger LOG = Logger.getLogger(GSImportMappingView.class);

    public static final String ID = "sernet.gs.ui.rcp.gsimport.gsimportmappingview";

    private Table mainTable;
    private TableItem[] items;
    private Set<Text> texts;
    private Set<CCombo> combos;
    private Set<String> propertyIDs;
    private String[] columnHeaders = null;

    private static final Set<String> veriniceITGSObjectTypes = new HashSet<>(8);

    static {
        veriniceITGSObjectTypes.addAll(Arrays.asList(new String[] {ITVerbund.TYPE_ID,
                Anwendung.TYPE_ID,
                Client.TYPE_ID,
                Server.TYPE_ID,
                Raum.TYPE_ID,
                Gebaeude.TYPE_ID,
                TelefonKomponente.TYPE_ID,
                SonstIT.TYPE_ID}));

    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        this.combos = new HashSet<>();
        this.texts = new HashSet<>();
        this.propertyIDs = new HashSet<>();

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

        for (int i = 0; i < 2; i++) {
            TableColumn column = new TableColumn(this.mainTable, SWT.NONE);
            column.setText(titles[i]);
            column.setWidth(tableColumnDefaultWidth);
        }

        // fill table
        String[] propertyColumns = new String[] {"test", "test1", "test2"};
        for (int i = 1; i < propertyColumns.length; i++) {
            new TableItem(this.mainTable, SWT.NONE);
        }

        this.items = this.mainTable.getItems();
        TableEditor editor;
        // fill the combos with content
        for (int i = 0; i < this.items.length; i++) {
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
            Iterator<String> iter = veriniceITGSObjectTypes.iterator();
            while(iter.hasNext()) {
                combo.add(iter.next());
            }
            this.combos.add(combo);

            editor.grabHorizontal = true;
            editor.setEditor(combo, this.items[i], 1);
        }
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
