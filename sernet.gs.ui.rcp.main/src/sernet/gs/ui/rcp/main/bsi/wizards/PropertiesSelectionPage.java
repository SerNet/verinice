package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyType;
import au.com.bytecode.opencsv.CSVReader;

public class PropertiesSelectionPage extends WizardPage {

    private static final Logger LOG = Logger.getLogger(PropertiesSelectionPage.class);
    
    private static final char DEFAULT_SEPARATOR = ';';

    private char separator = DEFAULT_SEPARATOR;
    private String entityName;
    private String entityId;
    private File csvDatei;
    private Table mainTable;
    private TableItem[] items;

    private List<Text> texts;
    private List<CCombo> combos;
    private List<String> propertyIDs;
    private String[] columnHeaders = null;
    private List<List<String>> csvContent = null;

    private Charset charset;

    protected PropertiesSelectionPage(String pageName) {
        super(pageName);
        this.setTitle(Messages.PropertiesSelectionPage_0);
        this.setDescription(Messages.PropertiesSelectionPage_1);
        setPageComplete(false);
        combos = new Vector<CCombo>();
        propertyIDs = new Vector<String>();
        texts = new Vector<Text>();
        csvContent = new ArrayList<List<String>>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public void createControl(Composite parent) {
        
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
        setControl(container);

        Label lab = new Label(container, SWT.NONE);
        lab.setText(Messages.PropertiesSelectionPage_2);
        lab.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        mainTable = new Table(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.verticalSpan = gdVerticalSpan;
        int listHeight = mainTable.getItemHeight() * mainTableItemHeightFactor;
        Rectangle trim = mainTable.computeTrim(0, 0, 0, listHeight);
        gridData.heightHint = trim.height;
        mainTable.setLayoutData(gridData);
        mainTable.setHeaderVisible(true);
        mainTable.setLinesVisible(true);

        // set the columns of the table
        String[] titles = { Messages.PropertiesSelectionPage_3, Messages.PropertiesSelectionPage_4 };

        for (int i = 0; i < 2; i++) {
            TableColumn column = new TableColumn(mainTable, SWT.NONE);
            column.setText(titles[i]);
            column.setWidth(tableColumnDefaultWidth);
        }
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    // if next is pressed call this method to fill the table with content
    public void fillTable() throws IOException {
        TableEditor editor;
        // get entities from verinice
        if (propertyIDs.size() > 0){
            propertyIDs.clear();
        }
        String[] propertyNames = null;
        
        if (entityId != null) {
            Activator.inheritVeriniceContextState();
            EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(entityId);         
            List<PropertyType> propertyTypes = entityType.getAllPropertyTypes();
            Collections.sort(propertyTypes);
            propertyNames = new String[propertyTypes.size()];
            int count = 0;
            for (IEntityElement element : propertyTypes) {
                propertyNames[count] = element.getName();
                propertyIDs.add(element.getId());
                count++;
            }
        }

        if (items != null) {
            for (int i = 0; i < items.length; i++){
                items[i].dispose();
            }
        }

        String[] propertyColumns = getFirstLine();
        for (int i = 1; i < propertyColumns.length; i++) {
            new TableItem(mainTable, SWT.NONE);
        }

        items = mainTable.getItems();
        // clear the combos
        for (CCombo combo : this.combos) {
            combo.dispose();
        }
        // clear text
        for (Text text : this.texts) {
            text.dispose();
        }
        // get the combos in default state
        if (this.combos.size() > 0) {
            combos.clear();
            texts.clear();
        }
        // fill the combos with content
        for (int i = 0; i < items.length; i++) {
            editor = new TableEditor(mainTable);
            Text text = new Text(mainTable, SWT.NONE);
            text.setText(propertyColumns[i + 1]);
            text.setEditable(false);
            editor.grabHorizontal = true;
            editor.setEditor(text, items[i], 0);
            texts.add(text);
            
            
            editor = new TableEditor(mainTable);
            final CCombo combo = new CCombo(mainTable, SWT.NONE);
            combo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setPageComplete(validateCCombos());
                }
            });
            combo.setText(""); //$NON-NLS-1$
            for (int j = 0; j < propertyNames.length; j++) {
                combo.add(propertyNames[j]);         
            }
            combos.add(combo);
            selectItemByName(combo, propertyColumns[i+1], propertyNames);
            editor.grabHorizontal = true;
            editor.setEditor(combo, items[i], 1);
        }
    }

    /**
     * Pre-select property if it has the same name as a CSV table column.
     * 
     * @param combo
     * @param cString 
     * @param string
     */
    private void selectItemByName(CCombo combo, String name, String[] cString) {
        for (int i = 0; i < cString.length; i++) {
            String propName = cString[i];
            if (name.equalsIgnoreCase(propName)) {
                combo.select(i);
            }
        }
    }

    // check if the properties are selected
    private boolean validateCCombos() {
        boolean valid = false;
        try {
        for (CCombo combo : this.combos) {
            if (combo.getSelectionIndex() > -1) {
                valid = true;
                break;
            }
        }
        if (valid) {
            for (int i = 0; i < this.combos.size() - 1; i++) {
                for (int j = i + 1; j < this.combos.size() - 1; j++) {
                    int is = combos.get(i).getSelectionIndex();
                    int js = combos.get(j).getSelectionIndex();
                    if(is>-1 && js>-1 && combos.get(i).getItem(is).equals(combos.get(j).getItem(js))) {
                        valid = false;
                        break;
                    }
                }
            }
        }
        } catch(Exception e) {
            LOG.error("Error while validating combo boxes.", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Comco boxes valid = " + valid);
        }
        return valid;
    }

    public List<Vector<String>> getPropertyTable() throws IOException {
        List<Vector<String>> table = new Vector<Vector<String>>();
        String[] spalten = getFirstLine();
        for (int i = 1; i < spalten.length; i++) {
            Vector<String> temp = new Vector<String>();
            // first column (ext-id) is not displayed: i-1
            int index = combos.get(i - 1).getSelectionIndex();
            if (index != -1) {
                temp.add(this.propertyIDs.get(index));
                temp.add(spalten[i]);
                table.add(temp);
            }
        }
        return table;
    }

    public void setCSVDatei(File csvDatei) {
        this.csvDatei = csvDatei;
        this.columnHeaders = null;
        this.csvContent.clear();
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    // get property and values of the csv
    public String[] getFirstLine() throws IOException {
        if (columnHeaders == null) {
            readFile();
        }
        return columnHeaders.clone();
    }

    public List<List<String>> getContent() throws IOException {
        if (csvContent == null) {
            readFile();
        }
        return csvContent;
    }

    // get property and values of the csv
    private void readFile() throws IOException {
        CSVReader reader = new CSVReader(
                new BufferedReader(new InputStreamReader(new FileInputStream(csvDatei), getCharset())), 
                getSeparator(), 
                '"', 
                false);
        // ignore first line
        columnHeaders = reader.readNext();
        this.csvContent.clear();
        String[] nextLine = null;
        while ((nextLine = reader.readNext()) != null) {
            this.csvContent.add(Arrays.asList(nextLine));
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    protected char getSeparator() {
        return separator;
    }

    protected void setSeparator(char separator) {
        this.separator = separator;
    }
}
