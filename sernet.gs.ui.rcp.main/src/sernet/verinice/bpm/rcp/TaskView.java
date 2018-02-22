/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/

package sernet.verinice.bpm.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ComboModelNumericStringComparator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.editors.TaskEditorContext;
import sernet.gs.ui.rcp.main.bsi.views.HtmlWriter;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.bpm.TaskLoader;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskListener;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.KeyMessage;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.iso27k.rcp.RegexComboModelFilter;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.PersonAdapter;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.TextEventAdapter;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * RCP view to show task loaded by instances of {@link ITaskService}.
 * 
 * New tasks are loaded by a {@link ITaskListener} registered at
 * {@link TaskLoader}.
 * 
 * Double clicking a task opens {@link CnATreeElement} in an editor. View
 * toolbar provides a button to complete tasks.
 * 
 * @see TaskViewDataLoader
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskView extends RightsEnabledView implements IAttachedToPerspective, IPartListener2 {

    private static final Logger LOG = Logger.getLogger(TaskView.class);
    static final NumericStringComparator NSC = new NumericStringComparator();

    static final ComboModelNumericStringComparator<CnATreeElement> COMPARATOR_CNA_TREE_ELEMENT = new ComboModelNumericStringComparator<>();
    static final ComboModelNumericStringComparator<Configuration> COMPARATOR_CONFIGURATION = new ComboModelNumericStringComparator<>();
    static final ComboModelNumericStringComparator<KeyMessage> COMPARATOR_KEY_MESSAGE = new ComboModelNumericStringComparator<>();

    public static final String ID = "sernet.verinice.bpm.rcp.TaskView"; //$NON-NLS-1$

    private static final int WEIGHT_40 = 40;
    private static final int WEIGHT_60 = 60;
    private static final int COMBO_WIDTH = 140;
    private static final int WIDTH_SEARCH_FORM = 1060;
    private static final int HEIGHT_SEARCH_FORM = 60;

    CnATreeElement selectedScope;
    CnATreeElement selectedAudit;
    String selectedAssignee;
    KeyMessage selectedProcessType;
    KeyMessage selectedTaskType;

    private TableViewer tableViewer;
    private TaskTableSorter tableSorter = new TaskTableSorter();
    private TaskContentProvider contentProvider;
    private Listener collapseAndExpandListener = new TableCollapseAndExpandListener();
    private Browser textPanel;
    private Label labelDateFrom;
    private Label labelDateUntil;
    Date dueDateFrom = null;
    Date dueDateTo = null;
    Button searchButton;

    private TaskViewDataLoader dataLoader;

    ComboModel<CnATreeElement> comboModelScope;
    Combo comboScope;
    RegexComboModelFilter filterScope;
    ComboModel<CnATreeElement> comboModelAudit;
    Combo comboAudit;
    ComboModel<Configuration> comboModelAccount;
    Combo comboAccount;

    ComboModel<KeyMessage> comboModelProcessType;
    Combo comboProcessType;
    ComboModelTaskType comboModelTaskType;
    Combo comboTaskType;

    DateTime dateTimeFrom;
    Button disableDateButtonFrom;
    DateTime dateTimeUntil;
    Button disableDateButtonTo;

    private Action doubleClickAction;
    private Action cancelTaskAction;

    private ICommandService commandService;
    private RightsServiceClient rightsService;
    private ITaskListener taskListener;

    public TaskView() {
        super();
        dataLoader = new TaskViewDataLoader(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.RightsEnabledView#createPartControl(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        try {
            super.createPartControl(parent);
            initView(parent);
        } catch (Exception e) {
            LOG.error("Error while creating task view.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.TaskView_5);
        }
    }

    private void initView(Composite parent) {
        createRootComposite(parent);
        dataLoader.initData();
        makeActions();
        addActions();
        addListener();
    }

    public void loadTasks() {
        dataLoader.loadTasks();
    }

    private void createRootComposite(Composite parent) {
        Composite rootComposite = CompositeCreator.create1ColumnComposite(parent);
        Composite topComposite = CompositeCreator.create2ColumnComposite(rootComposite);

        createSearchComposite(topComposite);

        SashForm splitComposite = CompositeCreator.createSplitComposite(rootComposite,
                SWT.VERTICAL);
        createInfoComposite(splitComposite);
        createTableComposite(splitComposite);
        splitComposite.setWeights(new int[] { WEIGHT_40, WEIGHT_60 });
    }

    private void createSearchComposite(Composite composite) {
        ScrolledComposite scrolledComposite = CompositeCreator.createScrolledComposite(composite);

        Composite formComposite = createSearchFormComposite(scrolledComposite);

        scrolledComposite.setContent(formComposite);
        scrolledComposite.setVisible(true);
        scrolledComposite.setMinSize(WIDTH_SEARCH_FORM, HEIGHT_SEARCH_FORM);

        createLoadButtonComposite(composite);
    }

    private void createLoadButtonComposite(Composite parent) {
        // Load button
        Composite buttonComposite = CompositeCreator.create1ColumnComposite(parent, false, true);
        // create a dummy label
        new Label(buttonComposite, SWT.WRAP);
        createButtonControls(buttonComposite);
        buttonComposite.pack();
    }

    private Composite createSearchFormComposite(Composite parent) {
        Composite formComposite = createInnerFormComposite(parent);

        // Scope filter
        Label label = new Label(formComposite, SWT.WRAP);
        label.setText(Messages.TaskView_11);
        setLayoutData(label, 50, false);
        // Scope
        label = new Label(formComposite, SWT.WRAP);
        label.setText(""); //$NON-NLS-1$
        setLayoutData(label, true);
        // Audit
        label = new Label(formComposite, SWT.WRAP);
        label.setText(Messages.TaskView_22);
        setLayoutData(label, true);
        // Assignee
        label = new Label(formComposite, SWT.WRAP);
        label.setText(Messages.TaskView_12);
        setLayoutData(label, true);
        // Process
        label = new Label(formComposite, SWT.WRAP);
        label.setText(Messages.TaskView_13);
        setLayoutData(label, true);
        // Task type
        label = new Label(formComposite, SWT.WRAP);
        label.setText(Messages.TaskView_14);
        setLayoutData(label, false);
        // Due date
        labelDateFrom = new Label(formComposite, SWT.WRAP);
        labelDateFrom.setText(Messages.TaskView_18);
        labelDateUntil = new Label(formComposite, SWT.WRAP);
        labelDateUntil.setText(Messages.TaskView_1);

        // Group
        createGroupControls(formComposite);
        // Assignee
        createAssigneeControls(formComposite);
        // Process
        createProcessTypeControls(formComposite);
        // Task type
        createTaskTypeControls(formComposite);
        // Due date
        createDateFromControls(formComposite);
        createDateToControls(formComposite);
        return formComposite;
    }

    private void setLayoutData(Label label, boolean grabExcessHorizontalSpace) {
        setLayoutData(label, COMBO_WIDTH, grabExcessHorizontalSpace);
    }

    private void setLayoutData(Label label, int minimumWidth, boolean grabExcessHorizontalSpace) {
        GridData gridData = new GridData();
        gridData.minimumWidth = minimumWidth;
        gridData.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        label.setLayoutData(gridData);
    }

    public static Composite createInnerFormComposite(Composite parentComposite) {
        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        composite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(8, false);
        gridLayout.marginHeight = 4;
        gridLayout.marginWidth = 4;
        composite.setLayout(gridLayout);
        return composite;
    }

    private void createTableComposite(Composite parent) {
        this.tableViewer = new TableViewer(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.tableViewer.getControl().setLayoutData(gridData);
        this.tableViewer.setUseHashlookup(true);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);

        createTableColumn(null, 0);
        createTableColumn(Messages.TaskView_9, 1);
        createTableColumn(Messages.TaskView_10, 2);
        createTableColumn(Messages.TaskView_4, 3);
        createTableColumn(Messages.TaskViewColumn_1, 4);
        createTableColumn(Messages.TaskViewColumn_2, 5);
        createTableColumn(Messages.TaskViewColumn_3, 6);

        // set initial column widths
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(22, 22, false));
        layout.addColumnData(new ColumnWeightData(192, 192, true));
        layout.addColumnData(new ColumnWeightData(350, 350, true));
        layout.addColumnData(new ColumnWeightData(130, 130, true));
        layout.addColumnData(new ColumnWeightData(110, 110, true));
        layout.addColumnData(new ColumnWeightData(120, 120, true));
        layout.addColumnData(new ColumnWeightData(76, 76, true));

        getTable().setLayout(layout);

        for (TableColumn tc : getTable().getColumns()) {
            tc.pack();
        }

        getTable().addListener(SWT.Expand, collapseAndExpandListener);
        getTable().addListener(SWT.Collapse, collapseAndExpandListener);

        this.contentProvider = new TaskContentProvider(tableViewer);
        this.tableViewer.setContentProvider(this.contentProvider);
        TaskLabelProvider labelProvider = new TaskLabelProvider();
        this.tableViewer.setLabelProvider(labelProvider);
        this.tableViewer.setSorter(tableSorter);
    }

    private void createInfoComposite(Composite container) {
        final int gridDataHeight = 80;
        textPanel = new Browser(container, SWT.NONE);
        textPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = gridDataHeight;
        textPanel.setLayoutData(gridData);
    }

    private void createGroupControls(Composite searchComposite) {
        final Text textFilterScope = new Text(searchComposite, SWT.BORDER);
        textFilterScope.setToolTipText(Messages.TaskView_26);
        textFilterScope.addKeyListener(new TextEventAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterScope.setFilter(textFilterScope.getText());
                dataLoader.refreshScopes();
                dataLoader.loadAudits();
            }
        });

        comboModelScope = new ComboModel<>(new GroupLabelProvider());
        filterScope = new RegexComboModelFilter();
        comboModelScope.setFilter(filterScope);
        comboScope = createComboBox(searchComposite);
        comboScope.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelScope.setSelectedIndex(comboScope.getSelectionIndex());
                selectedScope = comboModelScope.getSelectedObject();
                selectedAudit = null;
                dataLoader.loadAudits();
            }
        });
        comboModelAudit = new ComboModel<>(new GroupLabelProvider());
        comboAudit = createComboBox(searchComposite);
        comboAudit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelAudit.setSelectedIndex(comboAudit.getSelectionIndex());
                selectedAudit = comboModelAudit.getSelectedObject();
            }
        });
    }

    private void createAssigneeControls(Composite searchComposite) {
        comboModelAccount = new ComboModel<>(new IComboModelLabelProvider<Configuration>() {
            @Override
            public String getLabel(Configuration account) {
                StringBuilder sb = new StringBuilder(
                        PersonAdapter.getFullName(account.getPerson()));
                sb.append(" [").append(account.getUser()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
                return sb.toString();
            }
        });
        comboAccount = createComboBox(searchComposite);
        comboAccount.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelAccount.setSelectedIndex(comboAccount.getSelectionIndex());
                Configuration account = comboModelAccount.getSelectedObject();
                if (account != null) {
                    selectedAssignee = account.getUser();
                } else {
                    selectedAssignee = null;
                }
            }
        });
    }

    private void createProcessTypeControls(Composite searchComposite) {
        comboModelProcessType = new ComboModel<>(new IComboModelLabelProvider<KeyMessage>() {
            @Override
            public String getLabel(KeyMessage object) {
                return object.getValue();
            }
        });
        comboProcessType = createComboBox(searchComposite);
        comboProcessType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelProcessType.setSelectedIndex(comboProcessType.getSelectionIndex());
                selectedProcessType = comboModelProcessType.getSelectedObject();
            }
        });
    }

    private void createTaskTypeControls(Composite searchComposite) {
        comboModelTaskType = new ComboModelTaskType();
        comboTaskType = createComboBox(searchComposite);
        comboTaskType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelTaskType.setSelectedIndex(comboTaskType.getSelectionIndex());
                selectedTaskType = comboModelTaskType.getSelectedObject();
            }
        });
    }

    private void createDateFromControls(Composite searchComposite) {
        GridData gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        Composite dateFromComposite = CompositeCreator.create2ColumnComposite(searchComposite,
                gridData);
        dateTimeFrom = new DateTime(dateFromComposite, SWT.DATE | SWT.DROP_DOWN);
        dateTimeFrom.setEnabled(false);
        labelDateFrom.setEnabled(false);
        dateTimeFrom.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dueDateFrom = extractDateFrom(dateTimeFrom);
                dueDateTo = extractDateTo(dateTimeFrom);
            }
        });

        disableDateButtonFrom = new Button(dateFromComposite, SWT.CHECK);
        disableDateButtonFrom.setSelection(false);
        disableDateButtonFrom.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dateTimeFrom.setEnabled(!dateTimeFrom.isEnabled());
                labelDateFrom.setEnabled(dateTimeFrom.isEnabled());
                disableDateButtonFrom.setSelection(dateTimeFrom.isEnabled());
                setDuedateFromLabel();
                extractDates();
            }
        });
        dateFromComposite.pack();
    }

    private void createDateToControls(Composite searchComposite) {
        GridData gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false);
        Composite dateToComposite = CompositeCreator.create2ColumnComposite(searchComposite,
                gridData);
        dateTimeUntil = new DateTime(dateToComposite, SWT.DATE | SWT.DROP_DOWN);
        dateTimeUntil.setEnabled(false);
        labelDateUntil.setEnabled(false);
        dateTimeUntil.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dueDateTo = extractDateTo(dateTimeUntil);
            }
        });
        disableDateButtonTo = new Button(dateToComposite, SWT.CHECK);
        disableDateButtonTo.setSelection(false);
        disableDateButtonTo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dateTimeUntil.setEnabled(!dateTimeUntil.isEnabled());
                labelDateUntil.setEnabled(dateTimeUntil.isEnabled());
                disableDateButtonTo.setSelection(dateTimeUntil.isEnabled());
                setDuedateFromLabel();
                extractDates();
            }
        });
        dateToComposite.pack();
    }

    private void setDuedateFromLabel() {
        if (disableDateButtonTo.getSelection()) {
            labelDateFrom.setText(Messages.TaskView_18);
        } else {
            labelDateFrom.setText(Messages.TaskView_16);
        }
    }

    private void extractDates() {
        if (!dateTimeFrom.isEnabled()) {
            dueDateFrom = null;
            dueDateTo = null;
        } else {
            dueDateFrom = extractDateFrom(dateTimeFrom);
            if (!dateTimeUntil.isEnabled()) {
                dueDateTo = extractDateTo(dateTimeFrom);
            } else {
                dueDateTo = extractDateTo(dateTimeUntil);
            }
        }
    }

    private void createButtonControls(Composite searchComposite) {
        searchButton = new Button(searchComposite, SWT.NONE);
        searchButton.setText(Messages.TaskView_17);
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dataLoader.loadTasks();
            }
        });
    }

    private Date extractDateFrom(DateTime dueDate) {
        Calendar cal = getDateWithoutTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    private Date extractDateTo(DateTime dueDate) {
        Calendar cal = getDateWithoutTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    private Calendar getDateWithoutTime(DateTime dueDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dueDate.getYear());
        cal.set(Calendar.MONTH, dueDate.getMonth());
        cal.set(Calendar.DATE, dueDate.getDay());
        return cal;
    }

    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                if (getViewer().getSelection() instanceof IStructuredSelection
                        && ((IStructuredSelection) getViewer().getSelection())
                                .getFirstElement() instanceof TaskInformation) {
                    try {
                        TaskInformation task = (TaskInformation) ((IStructuredSelection) getViewer()
                                .getSelection()).getFirstElement();
                        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                        LoadAncestors loadControl = new LoadAncestors(task.getElementType(),
                                task.getUuid(), ri);
                        loadControl = getCommandService().executeCommand(loadControl);
                        CnATreeElement element = loadControl.getElement();
                        if (element != null) {
                            if (task.isWithAReleaseProcess()) {
                                TaskEditorContext editorContext = new TaskEditorContext(task,
                                        element);
                                EditorFactory.getInstance().updateAndOpenObject(editorContext);
                            } else {
                                EditorFactory.getInstance().updateAndOpenObject(element);
                            }
                        } else {
                            showError("Error", Messages.TaskView_25); //$NON-NLS-1$
                        }
                    } catch (Exception t) {
                        LOG.error("Error while opening control.", t); //$NON-NLS-1$
                    }
                }
            }
        };

        cancelTaskAction = new Action(Messages.ButtonCancel, SWT.TOGGLE) {
            @Override
            public void run() {
                try {
                    cancelTask();
                    this.setChecked(false);
                } catch (Exception e) {
                    LOG.error("Error while canceling task.", e); //$NON-NLS-1$
                    showError(Messages.TaskView_6, Messages.TaskView_7);
                }
            }
        };
        cancelTaskAction.setEnabled(false);
        cancelTaskAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.MASSNAHMEN_UMSETZUNG_NEIN));

        if (Activator.getDefault().isStandalone()
                && !Activator.getDefault().getInternalServer().isRunning()) {
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        configureActions();
                    }
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            configureActions();
        }
    }

    private void configureActions() {
        cancelTaskAction.setEnabled(getRightsService().isEnabled(ActionRightIDs.TASKDELETE));
        comboAccount.setEnabled(isTaskShowAllEnabled());
    }

    boolean isTaskShowAllEnabled() {
        return getRightsService().isEnabled(ActionRightIDs.TASKSHOWALL);
    }

    private void addActions() {
        addToolBarActions();
        getViewer().addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void addToolBarActions() {
        IActionBars bars = getViewSite().getActionBars();
        final IToolBarManager manager = bars.getToolBarManager();
        // Dummy action to force displaying the toolbar in a new line
        Action dummyAction = new Action() {
        };
        dummyAction.setText(" "); //$NON-NLS-1$
        dummyAction.setEnabled(false);
        ActionContributionItem item = new ActionContributionItem(dummyAction);
        item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        manager.add(item);
        manager.add(cancelTaskAction);
    }

    private void addListener() {
        taskListener = new ITaskListener() {
            @Override
            public void newTasks(List<ITask> taskList) {
                addTasks(taskList);
            }

            @Override
            public void newTasks() {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        dataLoader.loadTasks();
                    }
                });
            }
        };
        TaskChangeRegistry.addTaskChangeListener(taskListener);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (isTaskSelected()) {
                    try {
                        selectTask();
                    } catch (Exception t) {
                        LOG.error("Error while configuring task actions.", t); //$NON-NLS-1$
                    }
                } else {
                    resetToolbar();
                    getInfoPanel().setText(""); //$NON-NLS-1$
                }
                getViewSite().getActionBars().updateActionBars();
            }

            private boolean isTaskSelected() {
                return getViewer().getSelection() instanceof IStructuredSelection
                        && ((IStructuredSelection) getViewer().getSelection())
                                .getFirstElement() instanceof TaskInformation;
            }
        });
        // First we create a menu Manager
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getTable());
        // Set the MenuManager
        tableViewer.getTable().setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewer);
        // Make the selection available
        getSite().setSelectionProvider(tableViewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        TaskChangeRegistry.removeTaskChangeListener(taskListener);
        dataLoader.dispose();
        contentProvider.dispose();
        super.dispose();
    }

    private void selectTask() {
        IToolBarManager manager = resetToolbar();

        cancelTaskAction.setEnabled(false);
        cancelTaskAction.setEnabled(getRightsService().isEnabled(ActionRightIDs.TASKDELETE));
        TaskInformation task = (TaskInformation) ((IStructuredSelection) getViewer().getSelection())
                .getFirstElement();
        getInfoPanel().setText(HtmlWriter.getPage(task.getDescription()));

        if (task.isWithAReleaseProcess()) {
            CompareChangedElementPropertiesAction compareChangesAction = new CompareChangedElementPropertiesAction(
                    this, task);
            compareChangesAction.setText(Messages.CompareTaskChangesAction_0);
            compareChangesAction.setImageDescriptor(ImageCache.getInstance()
                    .getImageDescriptor(ImageCache.VIEW_TASK_COMPARE_CHANGES));
            ActionContributionItem item = new ActionContributionItem(compareChangesAction);
            manager.add(item);
        }

        List<KeyValue> outcomeList = task.getOutcomes();
        for (KeyValue keyValue : outcomeList) {
            CompleteTaskAction completeAction = new CompleteTaskAction(this, keyValue.getKey());
            completeAction.setText(keyValue.getValue());
            completeAction.setImageDescriptor(ImageCache.getInstance()
                    .getImageDescriptor(ImageCache.MASSNAHMEN_UMSETZUNG_JA));
            ActionContributionItem item = new ActionContributionItem(completeAction);

            item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
            manager.add(item);
        }
    }

    private IToolBarManager resetToolbar() {
        IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
        manager.removeAll();

        addToolBarActions();
        return manager;
    }

    /**
     * @param taskList
     */
    protected void addTasks(List<ITask> taskList) {
        final List<ITask> newList;
        if (taskList == null || taskList.isEmpty()) {
            newList = new LinkedList<>();
        } else {
            newList = taskList;
        }
        List<ITask> currentTaskList = (List<ITask>) getViewer().getInput();
        if (currentTaskList != null) {
            for (ITask task : currentTaskList) {
                if (!newList.contains(task)) {
                    newList.add(task);
                }
            }
        }
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                getViewer().setInput(newList);
            }
        });
    }

    private void cancelTask() throws InvocationTargetException, InterruptedException {
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        final List<TaskInformation> taskList = getSelectedTasks();
        if (!taskList.isEmpty()
                && MessageDialog.openConfirm(getShell(), Messages.ConfirmTaskDelete_0,
                        Messages.bind(Messages.ConfirmTaskDelete_1, taskList.size()))) {
            closeEditors(taskList);
            progressService.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    for (TaskInformation task : taskList) {
                        ServiceFactory.lookupTaskService().cancelTask(task.getId());
                        TaskView.this.contentProvider.removeTask(task);
                    }
                }
            });
            getInfoPanel().setText(""); //$NON-NLS-1$
        }
    }

    protected List<TaskInformation> getSelectedTasks() {
        final StructuredSelection selection = (StructuredSelection) getViewer().getSelection();
        List<TaskInformation> taskList = new ArrayList<>(selection.size());
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            Object sel = iterator.next();
            if (sel instanceof TaskInformation) {
                taskList.add((TaskInformation) sel);
            }
        }
        return taskList;
    }

    public void removeTask(ITask task) {
        contentProvider.removeTask(task);
    }

    public void closeEditorForElement(String uuid) {
        for (IEditorReference editorReference : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getEditorReferences()) {
            try {
                CnATreeElement element = ((BSIElementEditorInput) editorReference.getEditorInput())
                        .getCnAElement();
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage();
                if (editorReference.getEditorInput() instanceof BSIElementEditorInput
                        && uuid.equals(element.getUuid())) {
                    activePage.closeEditors(new IEditorReference[] { editorReference }, true);
                    break;
                }
            } catch (PartInitException e) {
                LOG.error("Error while closing element editor.", e); //$NON-NLS-1$
            }
        }
    }

    private void closeEditors(List<TaskInformation> taskList) {
        for (TaskInformation taskInformation : taskList) {
            closeEditorForElement(taskInformation.getUuid());
        }
    }

    private Combo createComboBox(Composite composite) {
        Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return combo;
    }

    private void createTableColumn(String label, int columnIndex) {
        TableColumn column;
        column = new TableColumn(getTable(), SWT.LEFT);
        if (label != null) {
            column.setText(label);
        }
        column.addSelectionListener(new TaskSortSelectionAdapter(this, column, columnIndex));
    }

    protected TableViewer getViewer() {
        return tableViewer;
    }

    protected TaskTableSorter getTableSorter() {
        return tableSorter;
    }

    Browser getInfoPanel() {
        return textPanel;
    }

    private Table getTable() {
        return this.tableViewer.getTable();
    }

    protected void showError(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(getShell(), title, message);
            }
        });
    }

    static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    private static Shell getShell() {
        return getDisplay().getActiveShell();
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }

    public RightsServiceClient getRightsService() {
        if (rightsService == null) {
            rightsService = (RightsServiceClient) VeriniceContext
                    .get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#setFocus()
     */
    @Override
    public void setFocus() {
        // empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    public String getPerspectiveId() {
        return Iso27kPerspective.ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TASKVIEW;
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

}