package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.Note;
import sernet.verinice.model.common.Domain;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.CnATypeMapper;

public class EntitySelectionPage extends WizardPage {
    private static final String[] FILTEREXTEND = { "*.csv", "*.CSV", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final int NUMBER_OF_COLUMNS_UPPER_GRID = 6;
    private static final int NUMBER_OF_COLUMNS_BOTTOM_GRID = 3;
    private static final int DEFAULT_GRID_DATA_VERTICAL_SPAN = 4;
    private static final int DEFAULT_HORIZONTAL_SPACING = 30;
    private static final int DEFAULT_LIST_HEIGHT_FACTOR = 12;
    private static final int DEFAULT_GRID_DATA_WIDTH = 120;
    private static final int DEFAULT_GRID_DATA_HEIGHT = 15;

    private File csvFile;
    private String entityName = ""; //$NON-NLS-1$
    private Text sourceIdText;
    private Combo separatorCombo;
    private Combo charsetCombo;
    private Text csvText;
    private Text entityText;
    private Label warningLabel;
    private String entityNameId = ""; //$NON-NLS-1$
    private boolean insert;
    private boolean update;
    private boolean delete;

    protected EntitySelectionPage(String pageName) {
        super(pageName);
        this.setTitle(Messages.EntitySelectionPage_0);
        this.setDescription(Messages.EntitySelectionPage_1);
        setPageComplete(false);
    }

    @Override
    public void createControl(final Composite parent) {

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(gridLayout);
        setControl(container);

        // settings

        Group idGroup = new Group(container, SWT.NULL);
        idGroup.setText(Messages.EntitySelectionPage_2);
        gridLayout = new GridLayout(NUMBER_OF_COLUMNS_UPPER_GRID, false);
        idGroup.setLayout(gridLayout);
        idGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label sourceIdLabel = new Label(idGroup, SWT.LEFT);
        sourceIdLabel.setText(Messages.EntitySelectionPage_3);
        sourceIdText = new Text(idGroup, SWT.BORDER);
        sourceIdText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        Label separatorLabel = new Label(idGroup, SWT.LEFT);
        separatorLabel.setText(Messages.EntitySelectionPage_4);
        separatorCombo = new Combo(idGroup, SWT.READ_ONLY);
        separatorCombo.add(";"); //$NON-NLS-1$
        separatorCombo.add(","); //$NON-NLS-1$
        separatorCombo.select(0);
        separatorCombo.setLayoutData(
                new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        separatorCombo.pack();

        Label charsetLabel = new Label(idGroup, SWT.LEFT);
        charsetLabel.setText(Messages.EntitySelectionPage_7);
        charsetCombo = new Combo(idGroup, SWT.READ_ONLY);
        charsetCombo.add(VeriniceCharset.CHARSET_UTF_8.displayName());
        charsetCombo.add(VeriniceCharset.CHARSET_ISO_8859_15.displayName());
        charsetCombo.add(VeriniceCharset.CHARSET_WINDOWS_1252.displayName());
        charsetCombo.select(0);

        // Operations of database (update,insert,delete)

        Group operationGroup = new Group(container, SWT.NULL);
        operationGroup.setText(Messages.EntitySelectionPage_8);
        operationGroup
                .setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        operationGroup.setLayout(gridLayout);

        final Button insertCheck = new Button(operationGroup, SWT.CHECK);
        insertCheck.setSelection(true);
        insert = true;
        insertCheck.setText(Messages.EntitySelectionPage_9);
        insertCheck.setLayoutData(
                new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        insertCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                insert = insertCheck.getSelection();
                setPageComplete(validateData());
            }
        });

        Label insertText = new Label(operationGroup, SWT.LEFT);
        insertText.setText(Messages.EntitySelectionPage_10);
        insertText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        final Button updateCheck = new Button(operationGroup, SWT.CHECK);
        updateCheck.setText(Messages.EntitySelectionPage_11);
        updateCheck.setSelection(true);
        update = true;
        updateCheck.setLayoutData(
                new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        updateCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                update = updateCheck.getSelection();
                setPageComplete(validateData());
            }
        });

        Label updateText = new Label(operationGroup, SWT.LEFT);
        updateText.setText(Messages.EntitySelectionPage_12);
        updateText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        final Button deleteCheck = new Button(operationGroup, SWT.CHECK);
        deleteCheck.setText(Messages.EntitySelectionPage_13);
        deleteCheck.setLayoutData(
                new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        deleteCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                delete = deleteCheck.getSelection();
                setPageComplete(validateData());
            }
        });

        Label deleteText = new Label(operationGroup, SWT.LEFT);
        deleteText.setText(Messages.EntitySelectionPage_14);
        deleteText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        createTypeSelectionList(container);

        gridLayout = new GridLayout(NUMBER_OF_COLUMNS_BOTTOM_GRID, false);
        gridLayout.horizontalSpacing = DEFAULT_HORIZONTAL_SPACING;
        Composite comp = new Composite(container, 0);
        comp.setLayout(gridLayout);

        Label selectedEntity = new Label(comp, SWT.NONE);
        selectedEntity.setText(Messages.EntitySelectionPage_16);
        selectedEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label importedCSV = new Label(comp, SWT.NONE);
        importedCSV.setText(Messages.EntitySelectionPage_17);
        importedCSV.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

        this.entityText = new Text(comp, SWT.BORDER);
        this.entityText.setEnabled(false);
        entityText.setLayoutData(new GridData(DEFAULT_GRID_DATA_WIDTH, DEFAULT_GRID_DATA_HEIGHT));

        this.csvText = new Text(comp, SWT.BORDER);
        csvText.setEnabled(false);
        csvText.setLayoutData(new GridData(DEFAULT_GRID_DATA_WIDTH, DEFAULT_GRID_DATA_HEIGHT));

        final Button dataBrowse = new Button(comp, SWT.PUSH);
        dataBrowse.setText(Messages.EntitySelectionPage_18);
        dataBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        dataBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayFiles(parent.getShell());
                setPageComplete(validateData());
            }
        });

        warningLabel = new Label(comp, SWT.NONE);
        warningLabel.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, true, false, NUMBER_OF_COLUMNS_BOTTOM_GRID, 1));

    }

    @SuppressWarnings("deprecation")
    protected void createTypeSelectionList(Composite container) {
        final List list = new List(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.verticalSpan = DEFAULT_GRID_DATA_VERTICAL_SPAN;
        int listHeight = list.getItemHeight() * DEFAULT_LIST_HEIGHT_FACTOR;
        Rectangle trim = list.computeTrim(0, 0, 0, listHeight);
        gridData.heightHint = trim.height;
        list.setLayoutData(gridData);

        java.util.List<String> entityNames = new ArrayList<>();
        HitroUtil.getInstance().getTypeFactory().getAllEntityTypes().stream()
                .filter(EntitySelectionPage::isDomainElement)
                .filter(type -> CnATypeMapper
                        .getDomainFromTypeId(type.getId()) != Domain.DATA_PROTECTION)
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).forEach(entityType -> {
                    list.add(entityType.getName());
                    entityNames.add(entityType.getId());
                });
        list.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                entityText.setText(list.getItem(list.getSelectionIndex()));
                entityName = list.getItem(list.getSelectionIndex());
                entityNameId = entityNames.get(list.getSelectionIndex());
                setPageComplete(validateData());
            }
        });
    }

    private static boolean isDomainElement(EntityType type) {
        String typeId = type.getId();
        return !(Attachment.TYPE_ID.equals(typeId) || Configuration.TYPE_ID.equals(typeId)
                || Note.TYPE_ID.equals(typeId) || Configuration.ROLE_TYPE_ID.equals(typeId)
                || typeId.toLowerCase().endsWith("group"));
    }

    public boolean validateData() {
        boolean valid = sourceIdText.getText() != null && !sourceIdText.getText().isEmpty();
        valid = valid && entityName != null && !entityName.isEmpty();
        valid = valid && csvText.getText() != null && !csvText.getText().isEmpty();
        return valid && (insert || update || delete);

    }

    private void displayFiles(Shell shell) {
        FileDialog dialog = new FileDialog(shell, SWT.NULL);
        dialog.setFilterExtensions(FILTEREXTEND);
        String path = dialog.open();
        if (path != null) {
            File file = new File(path);
            if (file.isFile()) {
                this.csvText.setText(file.getName());// file.getPath()
                this.csvFile = file;
                if (this.sourceIdText.getText() == null || this.sourceIdText.getText().isEmpty()
                        || this.sourceIdText.getText().endsWith(".csv")) {
                    this.sourceIdText.setText(file.getName());
                }
            }
        }
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getEntityNameId() {
        return this.entityNameId;
    }

    public File getCSVDatei() {
        return this.csvFile;
    }

    // set the page in default state
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
        warningLabel.setForeground(SWTResourceManager.getColor(red, green, blue));
        warningLabel.setText(message);
    }
}
