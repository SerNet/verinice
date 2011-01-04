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

import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.bpm.TaskLoader;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskListener;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.model.bpm.TaskInformation;
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
    
    private final Logger log = Logger.getLogger(TaskView.class);
    
    public static final String ID = "sernet.verinice.bpm.rcp.TaskView";
    
    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();
    
    private TableViewer viewer;
    
    private Action refreshAction;
    
    private Action completeTaskAction;
    
    private Action doubleClickAction;
    
    private ICommandService commandService;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = createContainer(parent);
        createViewer(container);
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
    
    private void createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

        createColumns(parent, viewer);
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());

        loadTasks();

        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
    }

    private void loadTasks() {
        List<ITask> taskList = ServiceFactory.lookupTaskService().getTaskList();
        
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        try {
            viewer.setInput(taskList.toArray());
        } catch (Throwable t) {
            log.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }
    
    private void createColumns(final Composite parent, final TableViewer viewer) {
        String[] titles = { "Name", "Control", "Date" };
        int[] bounds = { 100, 150, 100 };

        // First column: title of the role
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((ITask) element).getName();
            }
        });
        
        // 2. column
        col = createTableViewerColumn(titles[1], bounds[1], 1);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {      
                return ((ITask) element).getControlTitle();
            }
        });

        // 3. column
        col = createTableViewerColumn(titles[2], bounds[2], 2);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {      
                return DATE_FORMAT.format(((ITask) element).getCreateDate());
            }
        });
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
        
        completeTaskAction = new Action() {
            @Override
            public void run() {
                StructuredSelection selection = (StructuredSelection) viewer.getSelection();              
                for (Iterator<TaskInformation> iterator = selection.iterator(); iterator.hasNext();) {
                    completeTasks(iterator.next());                               
                }
            }
        };
        completeTaskAction.setText("Complete");
        completeTaskAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.MASSNAHMEN_UMSETZUNG_JA));
        doubleClickAction = new Action() {
            public void run() {
                if(viewer.getSelection() instanceof IStructuredSelection) {
                    try {
                        TaskInformation task = (TaskInformation) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                        LoadElementByUuid<Control> loadControl = new LoadElementByUuid<Control>(Control.TYPE_ID, task.getControlUuid(), RetrieveInfo.getPropertyInstance());
                        loadControl = getCommandService().executeCommand(loadControl);
                        EditorFactory.getInstance().updateAndOpenObject(loadControl.getElement());
                    } catch (Throwable t) {
                        log.error("Error while opening control.",t);
                    }
                }
            }
        };
    }
    
    /**
     * @param next
     */
    protected void completeTasks(TaskInformation task) {
        ServiceFactory.lookupTaskService().completeTask(task.getId());
        viewer.remove(task);
    }

    private void addActions() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.refreshAction);
        manager.add(this.completeTaskAction);
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }
    

    private void addListener() {
        TaskLoader.addTaskListener(new ITaskListener() {          
            @Override
            public void newTasks(List<ITask> taskList) {
                addTasks(taskList);           
            }
        });      
    }
    
    /**
     * @param taskList
     */
    protected void addTasks(final List<ITask> taskList) {
        Object[] taskArray = (Object[]) viewer.getInput();
        for (Object task : taskArray) {
            if(!taskList.contains((ITask)task)) {
                taskList.add((ITask)task);
            }
        }
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                viewer.setInput(taskList.toArray());
            }
        });
        
    }

    private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;

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

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    @Override
    public String getPerspectiveId() {
        return Iso27kPerspective.ID;
    }
    
    

}
