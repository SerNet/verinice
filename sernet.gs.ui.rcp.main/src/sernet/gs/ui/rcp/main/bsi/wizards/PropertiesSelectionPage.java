package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;
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

public class PropertiesSelectionPage extends WizardPage {//implements PageWizardListener
	private String entityName;
	private File csvDatei;
	private Table tabelle;
	private TableItem[] items;
	private TableEditor editor;
	private Vector<Text> texts;
	private Vector<CCombo> combos;
	private Vector<String> idCombos;
	private Vector<Vector<String>> inhaltDerTabelle;
	
	protected PropertiesSelectionPage(String pageName) {
		super(pageName);
		this.setTitle("Attributzuweisung");
		this.setDescription("Ordnen Sie die Attribute Ihrer CSV Datei den Attributen in Verinice zu.");
		setPageComplete(false);
		combos = new Vector<CCombo>();
		idCombos = new Vector<String>();
		texts = new Vector<Text>();
		inhaltDerTabelle = new Vector<Vector<String>>();
	}

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
		lab.setText("W�hlen Sie die entsprechenden Attribute in der ComboBox aus.");
		lab.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		
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
		String[] titles = { "Attribute in der CSV-Datei", "Attribute in Verinice"};
		
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
	public void fillTable() {
		//get entities from verinice
		if(idCombos.size()>0)
			idCombos.removeAllElements();
		
		String[] cString = null;
		Collection<EntityType> allEntityTypes =  HitroUtil.getInstance().getTypeFactory().getAllEntityTypes();
		if(entityName != null){
	        for (EntityType entityType : allEntityTypes) {
	        	if(entityType.getName().equals(entityName)){
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
		
		String[] propertyColumns = getPropertyColumns();
		if(items!= null){
			for(int i = 0; i < items.length; i++)
				items[i].dispose();
		}
		
		for (int i = 0; i < propertyColumns.length; i++){
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
			combos.removeAllElements();
			texts.removeAllElements();
		}
		//fill the combos with content	
	    for (int i = 0; i < items.length; i++) {
	    	editor = new TableEditor(tabelle);
	    	Text text = new Text(tabelle, SWT.NONE);
	    	text.setText(propertyColumns[i]);
	    	editor.grabHorizontal = true;
	    	editor.setEditor(text, items[i], 0);
	    	texts.add(text);
	    	
	    	editor = new TableEditor(tabelle);
	    	final CCombo combo = new CCombo(tabelle, SWT.NONE);
	    	
	    	combo.setText("");
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
	
	public Vector<Vector<String>> getPropertyTable(){
		Vector<Vector<String>> table = new Vector<Vector<String>>();
		String[] spalten = getPropertyColumns();
		for(int i = 0; i < spalten.length; i++){
			Vector<String> temp = new Vector<String>();
			temp.add(this.idCombos.get(combos.get(i).getSelectionIndex()));
			//temp.add(combos.get(i).getItem(combos.get(i).getSelectionIndex()));
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
	
	public Vector<Vector<String>> getInhaltDerDatei(){
		return this.inhaltDerTabelle;
	}
	//get property and values of the csv
	public String[] getPropertyColumns(){
		this.inhaltDerTabelle.removeAllElements();
		RandomAccessFile file;
		String[] spalten = null;
		String input = "";
		try {
			file = new RandomAccessFile(csvDatei, "r");
			file.seek(0); 
			spalten = file.readLine().split(";");
			while ((input = file.readLine()) != null) {
				// part the row with split 
				// in an new array
				String[] splittedLine = input.split(";");
				Vector<String> rows = new Vector<String>(Arrays.asList(splittedLine));
				this.inhaltDerTabelle.add(rows);
			}
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return spalten;
	}
}
