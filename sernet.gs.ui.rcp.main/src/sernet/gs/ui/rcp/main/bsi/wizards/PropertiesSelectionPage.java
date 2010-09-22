package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

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

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IEntityElement;
import au.com.bytecode.opencsv.CSVReader;

public class PropertiesSelectionPage extends WizardPage {

    private static final char DEFAULT_SEPARATOR = ';';
    
    private char separator = DEFAULT_SEPARATOR;
	private String entityName;
    private String entityId;
	private File csvDatei;
	private Table tabelle;
	private TableItem[] items;
	private TableEditor editor;
	private List<Text> texts;
	private List<CCombo> combos;
	private List<String> idCombos;
	private String[] firstLine = null;
	private List<List<String>> inhaltDerTabelle = null;

    private Charset charset;
	
	protected PropertiesSelectionPage(String pageName) {
		super(pageName);
		this.setTitle(Messages.PropertiesSelectionPage_0);
		this.setDescription(Messages.PropertiesSelectionPage_1);
		setPageComplete(false);
		combos = new Vector<CCombo>();
		idCombos = new Vector<String>();
		texts = new Vector<Text>();
		inhaltDerTabelle = new ArrayList<List<String>>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		FillLayout layout = new FillLayout();
	    layout.type = SWT.VERTICAL;
	    layout.marginWidth = 5;
	    layout.marginHeight = 10;
	    layout.spacing = 3;
	    
	    GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
	    
		Composite container = new Composite(parent, SWT.NONE);
	    container.setLayout(gridLayout);
	    setControl(container);
	    
	    Label lab = new Label(container, SWT.NONE);
		lab.setText(Messages.PropertiesSelectionPage_2);
		lab.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		tabelle = new Table(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gridData = new GridData(GridData.FILL,GridData.FILL, true, true);
	    gridData.verticalSpan = 4;
	    int listHeight = tabelle.getItemHeight() * 20;
	    Rectangle trim = tabelle.computeTrim(0, 0, 0, listHeight);
	    gridData.heightHint = trim.height;
	    tabelle.setLayoutData(gridData);
		tabelle.setHeaderVisible(true);
		tabelle.setLinesVisible(true);
		
		//set the columns of the table
		String[] titles = { Messages.PropertiesSelectionPage_3, Messages.PropertiesSelectionPage_4};
		
		for (int i = 0; i < 2; i++) {
			TableColumn column = new TableColumn(tabelle, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(225);
		}
	}
	
	public void setEntityName(String entityName){
		this.entityName = entityName;
	}
	//if next is pressed call this method to fill the table with content 
	public void fillTable() throws IOException {
		//get entities from verinice
		if(idCombos.size()>0)
			idCombos.clear();
		
		String[] cString = null;
		Collection<EntityType> allEntityTypes =  HitroUtil.getInstance().getTypeFactory().getAllEntityTypes();
		if(entityId != null){
	        for (EntityType entityType : allEntityTypes) {
	        	if(entityType.getId().equals(entityId)){
	        		cString = new String[entityType.getElements().size()];
	        		Collection<IEntityElement> elements =  entityType.getElements();
	        		int count = 0;
	        		for(IEntityElement element: elements){
	        			cString[count] = element.getName(); 
	        			idCombos.add(element.getId());
	        			count++;
	        		}
	        	}
	        }
		}
		
		
		if(items!= null){
			for(int i = 0; i < items.length; i++)
				items[i].dispose();
		}
		
		String[] propertyColumns = getFirstLine();
		for (int i = 1; i < propertyColumns.length; i++){
			new TableItem(tabelle, SWT.NONE);
		}
		
		items = tabelle.getItems();
		//clear the combos
		for(CCombo combo : this.combos){
			combo.dispose();
		}
		//clear text
		for(Text text: this.texts){
			text.dispose();
		}
		//get the combos in default state
		if(this.combos.size() > 0){
			combos.clear();
			texts.clear();
		}
		//fill the combos with content	
	    for (int i = 0; i < items.length; i++) {
	    	editor = new TableEditor(tabelle);
	    	Text text = new Text(tabelle, SWT.NONE);
	    	text.setText(propertyColumns[i+1]);
	    	text.setEditable(false);
	    	editor.grabHorizontal = true;
	    	editor.setEditor(text, items[i], 0);
	    	texts.add(text);
	    	
	    	editor = new TableEditor(tabelle);
	    	final CCombo combo = new CCombo(tabelle, SWT.NONE);
	    	
	    	combo.setText(""); //$NON-NLS-1$
	    	for(int j=0; j < cString.length; j++){
	    		combo.add(cString[j]);
	    		combo.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(validateCCombos())
						setPageComplete(true);
				}
	    	  });
	    	}
	    	combos.add(combo);
		    editor.grabHorizontal = true;
		    editor.setEditor(combo, items[i], 1);
	    }
	}
	//check if the properties are selected
	private boolean validateCCombos(){
		for(CCombo combo :this.combos){
			if(combo.getSelectionIndex()==-1)
				return false;
		}
		for(int i = 0; i < this.combos.size()-1; i++){
			for(int j = i+1; j < this.combos.size();j++){
				if(combos.get(i).getItem(combos.get(i).getSelectionIndex()).
						equals(combos.get(j).getItem(combos.get(j).getSelectionIndex())))
					return false;
			}
		}
		return true;
	}
	
	public Vector<Vector<String>> getPropertyTable() throws IOException{
		Vector<Vector<String>> table = new Vector<Vector<String>>();
		String[] spalten = getFirstLine();
		for(int i = 1; i < spalten.length; i++){
			Vector<String> temp = new Vector<String>();
			// first column (ext-id) is not displayed: i-1
			int index = combos.get(i-1).getSelectionIndex();
			temp.add(this.idCombos.get(index));
			temp.add(spalten[i]);
			table.add(temp);
		}
		return table;
	}
	
	public void setCSVDatei(File csvDatei){
		this.csvDatei = csvDatei;
	}
	
	public String getEntityName(){
		return this.entityName;
	}
	
	public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    //get property and values of the csv
    public String[] getFirstLine() throws IOException {
        if(firstLine==null) {
            readFile();
        }
        return firstLine;
    }
    
    public List<List<String>> getContent() throws IOException {
        if(inhaltDerTabelle==null) {
            readFile();
        }
        return inhaltDerTabelle;
    }
	
	//get property and values of the csv
	private void readFile() throws IOException {
	    CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(csvDatei), getCharset())), getSeparator(), '"', false);
        // ignore first line
	    firstLine = reader.readNext();	    
		this.inhaltDerTabelle.clear();
		String[] nextLine = null;
		while ((nextLine = reader.readNext()) != null) {
		    this.inhaltDerTabelle.add(Arrays.asList(nextLine));
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
