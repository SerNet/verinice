package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.GefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.OwnGefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.views.SerializeBrowserLoadingListener;

public abstract class RiskAnalysisWizardPage<T extends TableViewer> extends WizardPage {


    protected Composite rootContainer;
    protected Button buttonOwnGefaehrdungen, buttonGefaehrdungen;
    protected Text textSearch;
    private Browser browser;
    private RiskAnalysisWizardBrowserUpdateListener browserListener;
    private SerializeBrowserLoadingListener browserLoadingListener;
    protected Button buttonNew, buttonEdit, buttonDelete;
    protected T viewer;

    protected OwnGefaehrdungenFilter ownGefaehrdungFilter = new OwnGefaehrdungenFilter();
    protected GefaehrdungenFilter gefaehrdungFilter = new GefaehrdungenFilter();
    protected RiskAnalysisWizardPageSearchFilter searchFilter = new RiskAnalysisWizardPageSearchFilter();

    private RiskAnalysisWizard riskWizard;

    // SWT & JFace
    protected static final Point ADD_EDIT_REMOVE_BUTTON_SIZE = new Point(70, 30);
    protected static final int BUTTONS_GRID_COLUMN_AMOUNT = 5;
    protected static final int NUM_COLS_BUTTONS = 3;
    protected static final int NUM_COLS_FILTERS = 1;
    protected static final int NUM_COLS_CONTROLS = 2;
    protected static final int WIDTH_COL_NAME = 400;
    protected static final int WIZARD_BROWSER_WIDTH = 500;
    protected static final int WIZARD_NUM_COLS_ROOT = 2;

    private static final Point DEFAULT_MARGINS = new Point(5, 5);
    private static final Rectangle DEFAULT_EXTENDED_MARGINS = new Rectangle(5, 5, 5, 5);
    private static final Point DEFAULT_SPACING = new Point(5, 5);

    protected RiskAnalysisWizardPage(String pageName, String title, String description) {
        super(pageName);
        setTitle(title);
        setDescription(description);
    }

    protected RiskAnalysisWizard getRiskAnalysisWizard() {
        if (riskWizard == null) {
            riskWizard = (RiskAnalysisWizard) getWizard();
        }
        return riskWizard;
    }


    @Override
    public void createControl(Composite parent) {
        rootContainer = new Composite(parent, SWT.NONE);

        setLeftColumn(rootContainer);
        setRightColumn(rootContainer);
        addControls(rootContainer);
        GridLayoutFactory.fillDefaults().numColumns(WIZARD_NUM_COLS_ROOT).extendedMargins(DEFAULT_EXTENDED_MARGINS).spacing(DEFAULT_SPACING).generateLayout(rootContainer);

        setControl(rootContainer);
        rootContainer.layout();
        addListeners();

    }

    public void refresh() {
        viewer.refresh();
        browserListener.selectionChanged(null);
    }

    private void setRightColumn(Composite parent) {
        Composite rightColumn = new Composite(parent, SWT.FULL_SELECTION);

        browser = new Browser(rightColumn, SWT.BORDER);
        browserLoadingListener = new SerializeBrowserLoadingListener(browser);
        browser.addProgressListener(browserLoadingListener);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 5, 5, 5).generateLayout(rightColumn);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH
                | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        GridDataFactory.generate(rightColumn, 1, 1);
        GridDataFactory.fillDefaults().hint(WIZARD_BROWSER_WIDTH, SWT.LONG).grab(false, true).applyTo(rightColumn);
    }

    /**
     * function adds a new checkboxTableview. Overwrite to change
     * 
     * @param parent
     */
    protected void setLeftColumn(Composite parent) {
        
        /* CheckboxTableViewer */
        Composite leftColumn = new Composite(parent, SWT.NONE);
        viewer = initializeViewer(leftColumn);
        final Table table = viewer.getTable();

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        setColumns();



        table.layout();
        GridLayoutFactory.fillDefaults().extendedMargins(5, 5, 5, 5).spacing(DEFAULT_SPACING).generateLayout(leftColumn);
        GridDataFactory.generate(leftColumn, 1, 1);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(leftColumn);
    }

    protected abstract T initializeViewer(Composite parent);

    protected abstract void setColumns();

    protected void addControls(Composite parent) {


        Composite controls = new Composite(parent, SWT.NONE);
        addFilters(controls);

        addButtons(controls);

        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_CONTROLS).margins(DEFAULT_MARGINS).spacing(DEFAULT_SPACING).generateLayout(controls);

    }

    private void addFilters(Composite parent) {
        Composite compositeFilter = new Composite(parent, SWT.NONE);

        /* filter button - OwnGefaehrdungen only */
        buttonOwnGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonOwnGefaehrdungen.setText(Messages.ChooseGefaehrdungPage_8);

        /* filter button - BSI Gefaehrdungen only */
        buttonGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
        buttonGefaehrdungen.setText(Messages.ChooseGefaehrdungPage_9);

        /* filter button - search */

        Composite search = new Composite(compositeFilter, SWT.NULL);
        new Label(search, SWT.NULL).setText(Messages.ChooseGefaehrdungPage_10);
        textSearch = new Text(search, SWT.SINGLE | SWT.BORDER);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(DEFAULT_MARGINS).spacing(DEFAULT_SPACING).generateLayout(search);
        GridDataFactory.fillDefaults();
        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_FILTERS).margins(DEFAULT_MARGINS).spacing(DEFAULT_SPACING).generateLayout(compositeFilter);
        GridDataFactory.fillDefaults().hint(125, SWT.DEFAULT).align(SWT.LEFT, SWT.TOP).applyTo(textSearch);
    }

    protected void addButtons(Composite parent) {

        /* group the buttons with Group */
        Group groupButtons = new Group(parent, SWT.SHADOW_ETCHED_OUT);
        groupButtons.setText(Messages.ChooseGefaehrdungPage_11);

        buttonNew = new Button(groupButtons, SWT.PUSH);
        buttonNew.setText(Messages.ChooseGefaehrdungPage_12);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonNew);

        /* edit button */
        buttonEdit = new Button(groupButtons, SWT.PUSH);
        buttonEdit.setText(Messages.ChooseGefaehrdungPage_17);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonEdit);

        /* delete button */
        buttonDelete = new Button(groupButtons, SWT.PUSH);
        buttonDelete.setText(Messages.ChooseGefaehrdungPage_13);
        GridDataFactory.fillDefaults().hint(ADD_EDIT_REMOVE_BUTTON_SIZE).applyTo(buttonDelete);


        GridLayoutFactory.fillDefaults().numColumns(NUM_COLS_BUTTONS).margins(DEFAULT_MARGINS).spacing(DEFAULT_SPACING).generateLayout(groupButtons);

        GridDataFactory.generate(groupButtons, 1, 1);
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(groupButtons);

    }

    protected void addListeners() {

        addSpecificListenersForPage();

        browserListener = new RiskAnalysisWizardBrowserUpdateListener(browserLoadingListener, viewer);
        viewer.addSelectionChangedListener(browserListener);

    }


    protected abstract void addSpecificListenersForPage();



}
