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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.bpm.TaskLoader;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskListener;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * RCP view to display task loaded by instances of {@link ITaskService}.
 * 
 * New tasks are loaded by a {@link ITaskListener} registered at
 * {@link TaskLoader}.
 * 
 * Double clicking a task opens {@link CnATreeElement} in an editor.
 * View toolbar provides a button to complete tasks.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskView extends ViewPart implements IAttachedToPerspective {
    
    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     *
     */
    private final class CompleteTaskAction extends Action {
        
        final String id = TaskView.class.getName() + ".complete";      
        String outcomeId;
        
        public CompleteTaskAction() {
            super();
            setId(id);
        }

        public CompleteTaskAction(String outcomeId) {
            super();
            this.outcomeId = outcomeId;
            setId(id + "." + outcomeId);
        }

        @Override
        public void run() {
            StructuredSelection selection = (StructuredSelection) getViewer().getSelection();          
            for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
                Object sel = iterator.next();
                if(sel instanceof TaskInformation) {                  
                    completeTasks((TaskInformation) sel,outcomeId);
                }
            }
        }
    }

    private final Logger log = Logger.getLogger(TaskView.class);
    
    public static final String ID = "sernet.verinice.bpm.rcp.TaskView";
    
    private static final String[] ALLOWED_ROLES = new String[] { ApplicationRoles.ROLE_ADMIN };
    
    private TreeViewer treeViewer;
    
    TaskLabelProvider labelProvider;
    
    private Action refreshAction;
    
    private Action doubleClickAction;
    
    private Action myTasksAction;
    
    private ICommandService commandService;
    
    private boolean onlyMyTasks = true;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = createContainer(parent);
        //createViewer(container);
        createTreeViewer(container);
        loadTasks();
        makeActions();     
        addActions();
        addListener();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // empty
    }
    
    private void loadTasks() {
        TaskParameter param = new TaskParameter();
        param.setAllUser(!onlyMyTasks);
        List<ITask> taskList = ServiceFactory.lookupTaskService().getTaskList(param);
        Collections.sort(taskList);
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        try {
            getViewer().setInput(taskList);
        } catch (Throwable t) {
            log.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }
    
    private void createTreeViewer(Composite parent) {
        this.treeViewer = new TreeViewer(parent);
        Tree tree = this.treeViewer.getTree();
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.treeViewer.getControl().setLayoutData(gridData);
        this.treeViewer.setUseHashlookup(true);

        /*** Tree table specific code starts ***/

        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        
        TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText("Audit / Control");   
        
        treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText("Task");
        
        treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText("User");
        
        treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText("Date");
        
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(50,true));
        layout.addColumnData(new ColumnWeightData(25,false));
        layout.addColumnData(new ColumnWeightData(10,false));
        layout.addColumnData(new ColumnWeightData(15,false));

        tree.setLayout(layout);

        /*** Tree table specific code ends ***/

        this.treeViewer.setContentProvider(new TaskContentProvider());
        labelProvider = new TaskLabelProvider(onlyMyTasks);
        this.treeViewer.setLabelProvider(labelProvider);
    }
    
    private void makeActions() {
        refreshAction = new Action() {
            @Override
            public void run() {
                loadTasks();
            }
        };
        refreshAction.setText("Refresh");
        refreshAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
        
        doubleClickAction = new Action() {
            public void run() {
                if(getViewer().getSelection() instanceof IStructuredSelection
                   && ((IStructuredSelection) getViewer().getSelection()).getFirstElement() instanceof TaskInformation) {
                    try {
                        TaskInformation task = (TaskInformation) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
                        LoadElementByUuid<Control> loadControl = new LoadElementByUuid<Control>(task.getType(), task.getUuid(), RetrieveInfo.getPropertyInstance());
                        loadControl = getCommandService().executeCommand(loadControl);
                        EditorFactory.getInstance().updateAndOpenObject(loadControl.getElement());
                    } catch (Throwable t) {
                        log.error("Error while opening control.",t);
                    }
                }
            }
        };       
        myTasksAction = new Action("Only my tasks", SWT.TOGGLE) {
            public void run() {
                onlyMyTasks = !onlyMyTasks;
                myTasksAction.setChecked(onlyMyTasks);   
                labelProvider.setOnlyMyTasks(onlyMyTasks);
                loadTasks();
            }
        };
        myTasksAction.setChecked(onlyMyTasks);
        myTasksAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ISO27K_PERSON));     
    }
    
    /**
     * @param next
     */
    protected void completeTasks(TaskInformation task, String outcomeId) {
        if(outcomeId==null) {
            ServiceFactory.lookupTaskService().completeTask(task.getId());
        } else {
            ServiceFactory.lookupTaskService().completeTask(task.getId(),outcomeId);
        }
        getViewer().remove(task);
    }

    private void addActions() {
        addToolBarActions();
        getViewer().addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void addToolBarActions() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.refreshAction);
        boolean hasRole = AuthenticationHelper.getInstance().currentUserHasRole(ALLOWED_ROLES);
        if(hasRole) {
            manager.add(myTasksAction);
        }
    }
    
    private void addListener() {
        TaskLoader.addTaskListener(new ITaskListener() {          
            @Override
            public void newTasks(List<ITask> taskList) {
                addTasks(taskList);           
            }
        });
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {        
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if(getViewer().getSelection() instanceof IStructuredSelection
                        && ((IStructuredSelection) getViewer().getSelection()).getFirstElement() instanceof TaskInformation) {
                     try {
                         TaskInformation task = (TaskInformation) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
                         List<KeyValue> outcomeList = task.getOutcomes();
                         IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
                         manager.removeAll();
                         addToolBarActions();
                         for (KeyValue keyValue : outcomeList) {
                            CompleteTaskAction completeAction = new CompleteTaskAction(keyValue.getKey());
                            completeAction.setText(keyValue.getValue());
                            completeAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.MASSNAHMEN_UMSETZUNG_JA));                      
                            ActionContributionItem item = new ActionContributionItem(completeAction);
                            item.setMode(ActionContributionItem.MODE_FORCE_TEXT);                          
                            manager.add(item);
                        }
                        getViewSite().getActionBars().updateActionBars();
                     } catch (Throwable t) {
                         log.error("Error while opening control.",t);
                     }
                 }           
            }
        });
    }
    
    /**
     * @param taskList
     */
    protected void addTasks(final List<ITask> taskList) {
        List<ITask> currentTaskList = (List<ITask>) getViewer().getInput();
        if(currentTaskList!=null) {
            for (ITask task : currentTaskList) { 
                if(!taskList.contains(task)) {
                    taskList.add(task);
                }
            }
        }
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                getViewer().setInput(taskList);
            }
        });      
    }
    
    private Composite createContainer(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layoutRoot = new GridLayout(1, false);
        layoutRoot.marginWidth = 2;
        layoutRoot.marginHeight = 2;
        composite.setLayout(layoutRoot);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        composite.setLayoutData(gd);
        return composite;
    }
    
    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }
    
    protected TreeViewer getViewer() {
        return treeViewer;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    @Override
    public String getPerspectiveId() {
        return Iso27kPerspective.ID;
    }

}
