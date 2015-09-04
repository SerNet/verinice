package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.VeriniceCharset;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;

public class EntitySelectionPage extends WizardPage{
	private static final String[] FILTEREXTEND = { "*.csv", "*.CSV", "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private File csvFile;
	private String entityName = ""; //$NON-NLS-1$
    private Text sourceIdText;
    private Combo separatorCombo;
    private Combo charsetCombo;
	private Text csvText;
	private Text entityText;
	private Label warningLabel;
	private java.util.List<String> entityNames;
	private String entityNameId = ""; //$NON-NLS-1$
	private boolean insert;
	private boolean update;
	private boolean delete;

	protected EntitySelectionPage(String pageName) {
		super(pageName);
		this.setTitle(Messages.EntitySelectionPage_0); 
		this.setDescription(Messages.EntitySelectionPage_1); 
		entityNames = new Vector<String>();
		setPageComplete(false);
	}

	@Override
	public void createControl(final Composite parent) {

	    final int sixColumnAmount = 6;
	    final int threeColumnAmount = 3;
	    final int defaultGridDataVerticalSpan = 4;
	    final int defaultHorizontalSpacing = 30;
	    final int defaultGridDataHorizontalSpan = threeColumnAmount;
	    final int defaultListHeightFactor = 12;
	    final int defaultGridDataWidth = 120;
	    final int defaultGridDataHeight = 15;
	    
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayout);
		setControl(container);
		
		// settings
		
		Group idGroup = new Group(container,SWT.NULL);
    	idGroup.setText(Messages.EntitySelectionPage_2);
		gridLayout = new GridLayout(sixColumnAmount, false);
		idGroup.setLayout(gridLayout);
		idGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label sourceIdLabel = new Label(idGroup, SWT.LEFT);
		sourceIdLabel.setText(Messages.EntitySelectionPage_3);
		sourceIdText = new Text(idGroup, SWT.BORDER);
		sourceIdText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
        
		Label separatorLabel = new Label(idGroup, SWT.LEFT);
		separatorLabel.setText(Messages.EntitySelectionPage_4);
		separatorCombo = new Combo(idGroup, SWT.READ_ONLY );
		separatorCombo.add(";"); //$NON-NLS-1$
		separatorCombo.add(","); //$NON-NLS-1$
		separatorCombo.select(0);
		separatorCombo.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
		separatorCombo.pack();
		
		Label charsetLabel = new Label(idGroup, SWT.LEFT);
		charsetLabel.setText(Messages.EntitySelectionPage_7);
		charsetCombo = new Combo(idGroup, SWT.READ_ONLY );
		charsetCombo.add(VeriniceCharset.CHARSET_UTF_8.displayName());
        charsetCombo.add(VeriniceCharset.CHARSET_ISO_8859_15.displayName());
        charsetCombo.add(VeriniceCharset.CHARSET_WINDOWS_1252.displayName());
		charsetCombo.select(0);
		
		//Operations of database (update,insert,delete)
		
		Group operationGroup = new Group(container,SWT.NULL);
    	operationGroup.setText(Messages.EntitySelectionPage_8);
		operationGroup.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
		
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		operationGroup.setLayout(gridLayout);
		
		final Button insertCheck = new Button(operationGroup, SWT.CHECK);
		insertCheck.setSelection(true);
		insert = true;
    	insertCheck.setText(Messages.EntitySelectionPage_9); 
    	insertCheck.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
    	insertCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				 insert = insertCheck.getSelection();
				 setPageComplete(validateData());
			}
		});
    	
    	Label insertText = new Label(operationGroup, SWT.LEFT);
    	insertText.setText(Messages.EntitySelectionPage_10);
    	insertText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
		
    	final Button updateCheck = new Button(operationGroup, SWT.CHECK);
    	updateCheck.setText(Messages.EntitySelectionPage_11); 
    	updateCheck.setSelection(true);
    	update = true;
    	updateCheck.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
    	updateCheck.addSelectionListener(new SelectionAdapter() {
			 @Override
			 public void widgetSelected(SelectionEvent arg0) {
				 update = updateCheck.getSelection();
				 setPageComplete(validateData());
			 }
		});
    	
    	Label updateText = new Label(operationGroup, SWT.LEFT);
    	updateText.setText(Messages.EntitySelectionPage_12);
    	updateText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
		
    	final Button deleteCheck = new Button(operationGroup, SWT.CHECK);
    	deleteCheck.setText(Messages.EntitySelectionPage_13); 
    	deleteCheck.setLayoutData(new GridData(GridData.BEGINNING,GridData.CENTER, false, false, 1, 1));
    	deleteCheck.addSelectionListener(new SelectionAdapter() {
			 @Override
			 public void widgetSelected(SelectionEvent arg0) {
				 delete = deleteCheck.getSelection();
				 setPageComplete(validateData());
			 }
		});
    	
    	Label deleteText = new Label(operationGroup, SWT.LEFT);
    	deleteText.setText(Messages.EntitySelectionPage_14);
    	deleteText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER, true, false, 1, 1));
    	
	    final List list = new List(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
	    GridData gridData = new GridData(GridData.FILL,GridData.FILL, true, true);
	    gridData.verticalSpan = defaultGridDataVerticalSpan;
	    int listHeight = list.getItemHeight() * defaultListHeightFactor;
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
            if(!entityType.getId().toLowerCase().endsWith("group")) { //$NON-NLS-1$
            	list.add(entityType.getName());
            	this.entityNames.add(entityType.getId());
            }
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
		        String outString = ""; //$NON-NLS-1$
		        StringBuffer buffer = new StringBuffer();
		        for (int loopIndex = 0; loopIndex < selectedItems.length; loopIndex++){
		        	buffer.append(selectedItems[loopIndex]).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		        }
		        outString = buffer.toString();
		    }
	    });
        
        gridLayout = new GridLayout(threeColumnAmount, false);
		gridLayout.horizontalSpacing = defaultHorizontalSpacing;
		Composite comp = new Composite(container, 0);
		comp.setLayout(gridLayout);
		
		Label selectedEntity = new Label(comp, SWT.NONE);
		selectedEntity.setText(Messages.EntitySelectionPage_16); 
		selectedEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));

		Label importedCSV = new Label(comp, SWT.NONE);
		importedCSV.setText(Messages.EntitySelectionPage_17); 
		importedCSV.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 2, 1));
	    
	    this.entityText = new Text(comp, SWT.BORDER);
	    this.entityText.setEnabled(false);
	    entityText.setLayoutData(new GridData(defaultGridDataWidth, defaultGridDataHeight));
	    
	    this.csvText = new Text(comp, SWT.BORDER);
	    csvText.setEnabled(false);
	    csvText.setLayoutData(new GridData(defaultGridDataWidth, defaultGridDataHeight));
	    
	    final Button dataBrowse = new Button(comp,SWT.PUSH);
    	dataBrowse.setText(Messages.EntitySelectionPage_18);
		dataBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1,1));
		dataBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayFiles(parent.getShell());
				setPageComplete(validateData());
			}
		});
		
		warningLabel = new Label(comp, SWT.NONE);
		warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, defaultGridDataHorizontalSpan,1));
		
	}
	

	public boolean validateData() {
	    boolean valid = sourceIdText.getText()!=null && !sourceIdText.getText().isEmpty();
	    valid = valid && entityName!=null && !entityName.isEmpty();
	    valid = valid && csvText.getText()!=null && !csvText.getText().isEmpty();
	    return valid && (insert || update || delete);	
	    
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
				if(this.sourceIdText.getText()==null 
				   || this.sourceIdText.getText().isEmpty()
				   || this.sourceIdText.getText().endsWith(".csv")) {
				    this.sourceIdText.setText(file.getName());
				}
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
		this.csvText.setText(""); //$NON-NLS-1$
		this.entityText.setText(""); //$NON-NLS-1$
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

    protected Text getSourceIdText() {
        return sourceIdText;
    }

    public Combo getSeparatorCombo() {
        return separatorCombo;
    }

    public Combo getCharsetCombo() {
        return charsetCombo;
    }
    
    public void setWarning(String message) {
        final int red = 200;
        final int green = 0;
        final int blue = green;
        warningLabel.setForeground(new Color(getShell().getDisplay(),red,green,blue));
        warningLabel.setText(message);
    }
}
