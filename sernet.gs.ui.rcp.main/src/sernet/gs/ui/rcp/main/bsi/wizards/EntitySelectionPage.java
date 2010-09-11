package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.IEntityElement;

public class EntitySelectionPage extends WizardPage{
	private final static String[] FILTEREXTEND = { "*.csv", "*.CSV", "*.*"};
	private File csvFile;
	private String entityName = "";
	private Text csvText;
	private Text entityText;
	private Vector<String> entityNames;
	private String entityNameId = "";
	private boolean insert;
	private boolean update;
	private boolean delete;

	protected EntitySelectionPage(String pageName) {
	    // FIXME externalize strings
		super(pageName);
		this.setTitle("Entitätimport und Einstellungen");
		this.setDescription("Synchronisationseinstellungen festlegen und die zu importierende Entität angeben.");
		entityNames = new Vector<String>();
		setPageComplete(false);
	}

	@Override
	public void createControl(final Composite parent) {

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayout);
		setControl(container);
		
		//set ID
		
		Group idGroup = new Group(container,SWT.NULL);
    	idGroup.setText(Messages.XMLImportDialog_3);
		idGroup.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 5, 1));
		
		
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.makeColumnsEqualWidth = true;
		idGroup.setLayout(gridLayout);
		
		//Operations of database (update,insert,delete)
		
		Group operationGroup = new Group(container,SWT.NULL);
    	operationGroup.setText(Messages.XMLImportDialog_6);
		operationGroup.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
		
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		operationGroup.setLayout(gridLayout);
		
		Label operationIntro = new Label(operationGroup, SWT.LEFT);
	    operationIntro.setText(Messages.XMLImportDialog_7);
		operationIntro.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 2, 1));
		
		final Button insertCheck = new Button(operationGroup, SWT.CHECK);
		insertCheck.setSelection(true);
    	insertCheck.setText("insert");
    	insertCheck.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
    	insertCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				 insert = insertCheck.getSelection();
				 setPageComplete(validateData());
			}
		});
    	
    	Label insertText = new Label(operationGroup, SWT.LEFT);
    	insertText.setText(Messages.XMLImportDialog_8);
    	insertText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
		
    	final Button updateCheck = new Button(operationGroup, SWT.CHECK);
    	updateCheck.setText("update");
    	updateCheck.setSelection(true);
    	updateCheck.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
    	updateCheck.addSelectionListener(new SelectionAdapter() {
			 @Override
			 public void widgetSelected(SelectionEvent arg0) {
				 update = updateCheck.getSelection();
				 setPageComplete(validateData());
			 }
		});
    	
    	Label updateText = new Label(operationGroup, SWT.LEFT);
    	updateText.setText(Messages.XMLImportDialog_9);
    	updateText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
		
    	final Button deleteCheck = new Button(operationGroup, SWT.CHECK);
    	deleteCheck.setText("delete");
    	deleteCheck.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
    	deleteCheck.addSelectionListener(new SelectionAdapter() {
			 @Override
			 public void widgetSelected(SelectionEvent arg0) {
				 delete = deleteCheck.getSelection();
				 setPageComplete(validateData());
			 }
		});
    	
    	Label deleteText = new Label(operationGroup, SWT.LEFT);
    	deleteText.setText(Messages.XMLImportDialog_10);
    	deleteText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
    	
	    final List list = new List(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
	    GridData gridData = new GridData(GridData.FILL,GridData.FILL, true, true);
	    gridData.verticalSpan = 4;
	    int listHeight = list.getItemHeight() * 12;
	    Rectangle trim = list.computeTrim(0, 0, 0, listHeight);
	    gridData.heightHint = trim.height;
	    list.setLayoutData(gridData);
	    Collection<EntityType> types =  HitroUtil.getInstance().getTypeFactory().getAllEntityTypes();
	    java.util.List<EntityType> allEntityTypes = new ArrayList<EntityType>();
	    allEntityTypes.addAll(types);
	    Collections.sort(allEntityTypes, new Comparator<EntityType>() {
            public int compare(EntityType o1, EntityType o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (EntityType entityType : allEntityTypes) {
        	list.add(entityType.getName());
        	this.entityNames.add(entityType.getId());
        }
        
	    list.addSelectionListener(new SelectionListener() {
	    	public void widgetSelected(SelectionEvent event) {
	    		entityText.setText(list.getItem(list.getSelectionIndex()));
		        entityName = list.getItem(list.getSelectionIndex());
		        entityNameId = entityNames.get(list.getSelectionIndex());
		        setPageComplete(validateData());
	    	}
	
		    public void widgetDefaultSelected(SelectionEvent event) {
		        int[] selectedItems = list.getSelectionIndices();
		        String outString = "";
		        for (int loopIndex = 0; loopIndex < selectedItems.length; loopIndex++)
		        	outString += selectedItems[loopIndex] + " ";
		    }
	    });
        
        gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 30;
		Composite comp = new Composite(container, 0);
		comp.setLayout(gridLayout);
		
		Label selectedEntity = new Label(comp, SWT.NONE);
		selectedEntity.setText("Verinice Entität:");
		selectedEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));

		Label importedCSV = new Label(comp, SWT.NONE);
		importedCSV.setText("Importierte Entität:");
		importedCSV.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 2, 1));
	    
	    this.entityText = new Text(comp, SWT.BORDER);
	    this.entityText.setEnabled(false);
	    entityText.setLayoutData(new GridData(120, 15));
	    
	    this.csvText = new Text(comp, SWT.BORDER);
	    csvText.setEnabled(false);
	    csvText.setLayoutData(new GridData(120,15));
	    
	    final Button dataBrowse = new Button(comp,SWT.PUSH);
    	dataBrowse.setText(Messages.XMLImportDialog_14);
		dataBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1,1));
		dataBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayFiles(parent.getShell());
				setPageComplete(validateData());
			}
		});
		
	}
	
	public boolean validateData(){
		if(this.entityName.equals("") || csvText.getText().equals("")|| (!insert && !update && !delete))
			return false;
		return true;
	}
	
	private void displayFiles(Shell shell) {
		FileDialog dialog = new FileDialog(shell, SWT.NULL);  
		dialog.setFilterExtensions(FILTEREXTEND);
		String path = dialog.open();
		if (path != null) {
			File file = new File(path);
			if(file.isFile()){
				this.csvText.setText(file.getName());//file.getPath()
				this.csvFile = file;
			}
		}
	}
	
	public String getEntityName(){
		return this.entityName;
	}
	public String getEntityNameId(){
		return this.entityNameId;
	}
	public File getCSVDatei(){
		return this.csvFile;
	}
	//set the page in default state
	public void resetEntitySelectionPage() {
		this.csvText.setText("");
		this.entityText.setText("");
	}
	// get the number of attributes in the *.csv
	private int getColumnLength(){
		// open the file to read
		RandomAccessFile file;
		String[] spalten = null;
		try {
			file = new RandomAccessFile(csvFile, "r");
			file.seek(0); // set pointer on the beginning of the file
			spalten = file.readLine().split(";");
			file.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return spalten.length;
	}
	//get the Number of properties in Verinice
	private int getNumberOfProperties(){
		int numberOfElements = 0;
		Collection<EntityType> allEntityTypes =  HitroUtil.getInstance().getTypeFactory().getAllEntityTypes();
		if(entityName != null){
	        for (EntityType entityType : allEntityTypes) {
	        	if(entityType.getName().equals(entityName)){
	        		Collection<IEntityElement> elements =  entityType.getElements();
	        		numberOfElements = elements.size();
	        	}
	        }
		}
		return numberOfElements;
	}
    
    public boolean getInsertState() {
    	return insert;
    }
    
    public boolean getUpdateState() {
    	return update;
    }
    
    public boolean getDeleteState() {
    	return delete;
    }
}
