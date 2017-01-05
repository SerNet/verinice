/*******************************************************************************  
 * Copyright (c) 2016 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.TemplateTableViewer.PathCellLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.service.commands.LoadTemplates;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */
public class TemplateView extends RightsEnabledView {

    private static final Logger LOG = Logger.getLogger(TemplateView.class);

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.TemplateView"; //$NON-NLS-1$

    private TableViewer table;
    private CnATreeElement inputElement;
    private Set<CnATreeElement> templates = new HashSet<CnATreeElement>();
    private TemplateViewContentProvider contentProvider;

    private Action doubleClickAction;

    private IModelLoadListener loadListener;
    private ISelectionListener selectionListener;

    /**
     * The constructor.
     */
    public TemplateView() {
    }

    public void loadTemplates() {
        if (!CnAElementHome.getInstance().isOpen() || inputElement == null) {
            return;
        }

        WorkspaceJob job = new WorkspaceJob(Messages.TemplateView_0) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();
                try {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            table.setInput(new PlaceHolder(Messages.TemplateView_0));
                        }
                    });

                    monitor.setTaskName(Messages.TemplateView_0);

                    LoadTemplates command = new LoadTemplates(inputElement);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    templates = command.getTemplates();

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            table.setInput(inputElement);
                        }
                    });
                } catch (Exception e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            table.setInput(new PlaceHolder(Messages.TemplateView_3));
                        }
                    });
                    ExceptionUtil.log(e, Messages.TemplateView_4);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        table = new TemplateTableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        contentProvider = new TemplateViewContentProvider(this, table);
        table.setContentProvider(contentProvider);

        TemplateViewLabelProvider templateViewLabelProvider = new TemplateViewLabelProvider(this);
        table.setLabelProvider(templateViewLabelProvider);

        // table.setSorter(new CnAElementByTitelSorter());

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(table, ToolTip.RECREATE);
        List<PathCellLabelProvider> cellLabelProviders = ((TemplateTableViewer) table).initToolTips(templateViewLabelProvider, parent);

        // register resize listener for cutting the tooltips
        addResizeListener(parent, cellLabelProviders);

        // try to add listeners once on startup, and register for model changes:
        addBSIModelListeners();
        hookModelLoadListener();

        makeActions();
        hookActions();

        contributeToActionBars();
        hookPageSelection();
    }

    /**
     * Tracks changes of viewpart size and delegates them to the tooltip
     * provider.
     */
    private void addResizeListener(final Composite parent, final List<PathCellLabelProvider> cellLabelProviders) {

        parent.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                for (PathCellLabelProvider c : cellLabelProviders) {
                    c.updateShellWidthAndX(parent.getShell().getBounds().width, parent.getShell().getBounds().x);
                }
            }

        });
    }

    private void hookModelLoadListener() {
        this.loadListener = new IModelLoadListener() {

            @Override
            public void closed(BSIModel model) {
                removeModelListeners();
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        table.setInput(new PlaceHolder("")); //$NON-NLS-1$
                    }
                });
            }

            @Override
            public void loaded(BSIModel model) {
                synchronized (loadListener) {
                    addBSIModelListeners();
                }
            }

            @Override
            public void loaded(ISO27KModel model) {
                // only BSI view

            }

        };
        CnAElementFactory.getInstance().addLoadListener(loadListener);
    }

    protected void addBSIModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getInstance().getLoadedModel().addBSIModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.TemplateView_7, e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    @Override
    public void setFocus() {
        table.getControl().setFocus();
    }

    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                if (table.getSelection() instanceof IStructuredSelection) {
                    Object selection = ((IStructuredSelection) table.getSelection()).getFirstElement();
                    EditorFactory.getInstance().updateAndOpenObject(selection);
                }
            }
        };
    }

    private void hookActions() {
        table.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void hookPageSelection() {
        selectionListener = new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                pageSelectionChanged(part, selection);
            }
        };
        getSite().getPage().addPostSelectionListener(selectionListener);

        /**
         * Own selection provider returns an Element object of the selected row.
         * Uses the table for all other methods.
         */
        getSite().setSelectionProvider(table);
    }

    protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part == this) {
            return;
        }
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        if (((IStructuredSelection) selection).size() != 1) {
            return;
        }
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof CnATreeElement) {
            setNewInputElement((CnATreeElement) element);
        }
    }

    public CnATreeElement getInputElement() {
        return this.inputElement;
    }

    public Set<CnATreeElement> getTemplates() {
        checkAndRetrieveTemplates();
        return this.templates;
    }

    private void checkAndRetrieveTemplates() {
        for (CnATreeElement element : templates) {
            element.setEntity(Retriever.checkRetrieveElement(element).getEntity());
            // element.setParent(Retriever.checkRetrieveParent(element.getParent()));
        }
    }

    public void reloadAll() {
        loadTemplates();
    }

    public void setInputElement(CnATreeElement inputElement) {
        this.inputElement = inputElement;
    }

    @SuppressWarnings("restriction")
    protected void setNewInputElement(CnATreeElement element) {
        if (element.isTemplate()) {
            setViewTitle(Messages.bind(Messages.TemplateView_8, element.getTitle()));
        } else if (element.isImplementation()) {
            setViewTitle(Messages.bind(Messages.TemplateView_9, element.getTitle()));
        } else {
            setViewTitle("");
        }

        this.inputElement = element;
        loadTemplates();
    }

    private void setViewTitle(String title) {
        this.setContentDescription(title);
    }


    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        // manager.add(this.linkWithEditorAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TEMPLATES;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        CnAElementFactory.getInstance().removeLoadListener(loadListener);
        removeModelListeners();
        getSite().getPage().removePostSelectionListener(selectionListener);
        super.dispose();
    }

    protected void removeModelListeners() {
        if (CnAElementFactory.isModelLoaded()) {
            CnAElementFactory.getLoadedModel().removeBSIModelListener(contentProvider);
        }
    }
}
