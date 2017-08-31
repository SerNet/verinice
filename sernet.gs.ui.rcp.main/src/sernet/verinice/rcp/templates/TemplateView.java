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
package sernet.verinice.rcp.templates;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElement.TemplateType;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.moditbp.elements.BpModel;
import sernet.verinice.rcp.IProgressRunnable;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.templates.TemplateTableViewer.PathCellLabelProvider;
import sernet.verinice.service.commands.LoadTemplatesOrImplementations;

/**
 * This view shows for given ({@link CnATreeElement}) all modeling templates
 * ({@link TemplateType#TEMPLATE}) applied (implemented) in this element, if the
 * element is an implementation or all implementations
 * ({@link TemplateType#IMPLEMENTATION}) that belong to this modeling template,
 * if the element is a modeling template.
 * 
 * Further, in this view it is possible to add an additional modeling template
 * to the given ({@link CnATreeElement}).
 * 
 * @see TemplateSelectionDialog
 * @see CnATreeElement#implementedTemplateUuids
 * @see TemplateType
 * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class TemplateView extends RightsEnabledView {

    private static final Logger LOG = Logger.getLogger(TemplateView.class);

    public static final String ID = "sernet.verinice.rcp.templates.TemplateView"; //$NON-NLS-1$

    private TableViewer tableViewer;
    private CnATreeElement inputElement;
    /**
     * A Set of modeling templates ({@link TemplateType#TEMPLATE}) applied
     * (implemented) in given element, if the element is an implementation or
     * all implementations ({@link TemplateType#IMPLEMENTATION}) that belong to
     * this modeling template, if the element is a modeling template.
     */
    private Set<CnATreeElement> elements = new HashSet<CnATreeElement>();
    private TemplateViewContentProvider contentProvider;

    private Action doubleClickAction;
    private Action addTemplateAction;

    private IModelLoadListener loadListener;
    private ISelectionListener selectionListener;

    /**
     * The constructor.
     */
    public TemplateView() {
    }

    /**
     * Loads for given ({@link CnATreeElement}) all modeling templates
     * ({@link TemplateType#TEMPLATE}) applied (implemented) in this element, if
     * the element is an implementation or all implementations
     * ({@link TemplateType#IMPLEMENTATION}) that belong to this modeling
     * template, if the element is a modeling template.
     * 
     * @see CnATreeElement#implementedTemplateUuids
     * @see TemplateType
     * @see LoadTemplatesOrImplementations
     */
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
                            tableViewer.setInput(new PlaceHolder(Messages.TemplateView_0));
                        }
                    });

                    monitor.setTaskName(Messages.TemplateView_0);

                    LoadTemplatesOrImplementations command = new LoadTemplatesOrImplementations(inputElement);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    final Set<CnATreeElement> elements = command.getElements();

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            tableViewer.setInput(elements);
                        }
                    });

                } catch (Exception e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            tableViewer.setInput(new PlaceHolder(Messages.TemplateView_3));
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
        tableViewer = new TemplateTableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
        contentProvider = new TemplateViewContentProvider(this, tableViewer);
        tableViewer.setContentProvider(contentProvider);

        TemplateViewLabelProvider templateViewLabelProvider = new TemplateViewLabelProvider(this);
        tableViewer.setLabelProvider(templateViewLabelProvider);
        // table.setSorter(new CnAElementByTitelSorter());

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.RECREATE);
        List<PathCellLabelProvider> cellLabelProviders = ((TemplateTableViewer) tableViewer).initToolTips(templateViewLabelProvider, parent);
        // register resize listener for cutting the tooltips
        addResizeListener(parent, cellLabelProviders);

        // try to add listeners once on startup, and register for model changes:
        addBSIModelListeners();
        hookModelLoadListener();

        makeActions();
        hookActions();
        addToolBarActions();

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

    protected void addBSIModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @SuppressWarnings("static-access")
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getInstance().getLoadedModel().addBSIModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error(Messages.TemplateView_7, e); // $NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.TemplateView_7, e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    private void hookModelLoadListener() {
        this.loadListener = new IModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                removeModelListeners();
                setInputAsync();
            }

            @Override
            public void loaded(BSIModel model) {
                synchronized (loadListener) {
                    addBSIModelListeners();
                }
                setInputAsync();
            }

            @Override
            public void loaded(ISO27KModel model) {
                // only BSI view
            }

            @Override
            public void loaded(BpModel model) {
                // only BSI view
            }
        };
        CnAElementFactory.getInstance().addLoadListener(loadListener);
    }

    private void setInputAsync() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (tableViewer.getContentProvider() != null) {
                    tableViewer.setInput(new PlaceHolder("")); //$NON-NLS-1$
                }
            }

        });
    }

    @Override
    public void setFocus() {
        tableViewer.getControl().setFocus();
    }

    private void makeActions() {
        doubleClickAction = new Action() {
            @Override
            public void run() {
                if (tableViewer.getSelection() instanceof IStructuredSelection) {
                    Object selection = ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                    EditorFactory.getInstance().updateAndOpenObject(selection);
                }
            }
        };

        addTemplateAction = new Action() {
            @Override
            public void run() {
                try {
                    addTemplates();
                    this.setChecked(false);
                } catch (Exception e) {
                    LOG.error(Messages.TemplateView_6, e); // $NON-NLS-1$
                    showError(Messages.TemplateView_5, Messages.TemplateView_6);
                }
            }
        };
        addTemplateAction.setText(Messages.TemplateView_1);
        addTemplateAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PLUS));
        addTemplateAction.setEnabled(false);
    }

    private void hookActions() {
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {
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
        getSite().setSelectionProvider(tableViewer);
    }

    @SuppressWarnings("restriction")
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
            CnATreeElement cnTreeElement = (CnATreeElement) element;
            setNewInputElement(cnTreeElement);

            if (MassnahmenUmsetzung.TYPE_ID.equals(cnTreeElement.getTypeId()) || ITVerbund.TYPE_ID.equals(cnTreeElement.getTypeId())) {
                addTemplateAction.setEnabled(false);
                addTemplateAction.setText(Messages.TemplateView_1);
            } else {
                addTemplateAction.setEnabled(true);
                addTemplateAction.setText(Messages.bind(Messages.TemplateView_2, inputElement.getTitle()));
            }
        }
    }

    public CnATreeElement getInputElement() {
        return this.inputElement;
    }

    protected void setNewInputElement(CnATreeElement element) {
        this.inputElement = element;
        setViewTitle();
        loadTemplates();
    }

    @SuppressWarnings("restriction")
    private void setViewTitle() {
        if (inputElement.isTemplate()) {
            this.setContentDescription(Messages.bind(Messages.TemplateView_8, inputElement.getTitle()));
        } else if (inputElement.isImplementation()) {
            this.setContentDescription(Messages.bind(Messages.TemplateView_9, inputElement.getTitle()));
        } else {
            this.setContentDescription("");
        }
    }

    public Set<CnATreeElement> getElements() {
        return this.elements;
    }

    public void setElements(Set<CnATreeElement> elements) {
        this.elements = elements;
    }

    public void reloadAll() {
        loadTemplates();
    }

    private void addToolBarActions() {
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().add(this.addTemplateAction);
    }

    /**
     * Add for given ({@link CnATreeElement}) selected modeling templates
     * ({@link TemplateType#TEMPLATE}) and apply (implement) these to this
     * element.
     * 
     * @see CnATreeElement#implementedTemplateUuids
     * @see TemplateType
     * @see TemplateSelectionDialog
     * 
     * @throws InvocationTargetException
     * @throws InterruptedException
     * @throws CommandException
     */
    private void addTemplates() throws InvocationTargetException, InterruptedException, CommandException {
        CnATreeElementSelectionDialog dialog = new TemplateSelectionDialog(getShell(), inputElement);
        if (dialog.open() != Window.OK) {
            return;
        }

        List<CnATreeElement> templateCandidates = dialog.getSelectedElements();
        Set<String> templateCandidateUuids = new HashSet<String>();
        List<CnATreeElement> newChildren = new ArrayList<CnATreeElement>();

        for (CnATreeElement templateCandidate : templateCandidates) {
            newChildren.addAll(templateCandidate.getChildren());
            templateCandidateUuids.add(templateCandidate.getUuid());
        }
        addTemplatesChildren(templateCandidateUuids, newChildren);

        // notify all listeners:
        CnAElementFactory.getModel(inputElement.getParent()).childChanged(inputElement);
        CnAElementFactory.getModel(inputElement.getParent()).databaseChildChanged(inputElement);
    }

    /**
     * @param templateCandidateUuids
     * @param newChildren
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    private void addTemplatesChildren(Set<String> templateCandidateUuids, List<CnATreeElement> newChildren) throws InvocationTargetException, InterruptedException {
            IProgressRunnable operation = new CopyTemplateElements(inputElement, newChildren, templateCandidateUuids);
            if (operation != null) {
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                progressService.run(true, true, operation);
            }
    }

    protected void showError(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(getShell(), title, message);
            }
        });
    }

    private static Shell getShell() {
        return getDisplay().getActiveShell();
    }

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
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
