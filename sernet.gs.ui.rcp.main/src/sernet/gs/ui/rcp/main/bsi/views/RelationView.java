package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer.PathCellLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.catalog.CatalogView;
import sernet.verinice.service.commands.task.FindRelationsFor;

/**
 * This view displays all relations (links) for a slected element and allows the
 * user to change the link type.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationView extends RightsEnabledView implements IRelationTable, ILinkedWithEditorView {

    private static final Logger LOG = Logger.getLogger(ISMView.class);

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.RelationView"; //$NON-NLS-1$

    private TableViewer viewer;
    private Action jumpToAction;
    private Action doubleClickAction;
    private ISelectionListener selectionListener;
    private CnATreeElement inputElmt;

    private RelationViewContentProvider contentProvider;

    private IModelLoadListener loadListener;

    private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(this);

    private Action linkWithEditorAction;

    private boolean linkingActive = false;
    
    private boolean readOnly = false;


    /**
     * The constructor.
     */
    public RelationView() {
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.RELATIONS;
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

    /**
     * @param elmt
     */
    public void loadLinks(final CnATreeElement elmt) {
        if (!CnAElementHome.getInstance().isOpen() || inputElmt == null) {
            return;
        }

        WorkspaceJob job = new WorkspaceJob(Messages.RelationView_0) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();
                try {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            viewer.setInput(new PlaceHolder(Messages.RelationView_0));
                        }
                    });

                    monitor.setTaskName(Messages.RelationView_0);

                    FindRelationsFor command = new FindRelationsFor(elmt);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    final CnATreeElement linkElmt = command.getElmt();

                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            viewer.setInput(linkElmt);
                        }
                    });
                } catch (Exception e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            viewer.setInput(new PlaceHolder(Messages.RelationView_3));
                        }
                    });
                    ExceptionUtil.log(e, Messages.RelationView_4);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        viewer = new RelationTableViewer(this, parent, SWT.FULL_SELECTION | SWT.MULTI, false);
        contentProvider = new RelationViewContentProvider(this, viewer);
        viewer.setContentProvider(contentProvider);

        RelationViewLabelProvider relationViewLabelProvider = new RelationViewLabelProvider(this);
        viewer.setLabelProvider(relationViewLabelProvider);
        viewer.setSorter(new RelationByNameSorter(this, COLUMN_TITLE, COLUMN_TYPE_IMG));

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.RECREATE);
        List<PathCellLabelProvider> cellLabelProviders = ((RelationTableViewer) viewer).initToolTips(relationViewLabelProvider, parent);

        // register resize listener for cutting the tooltips
        addResizeListener(parent, cellLabelProviders);

        toggleLinking(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LINK_TO_EDITOR));

        // try to add listeners once on startup, and register for model changes:
        addBSIModelListeners();
        addISO27KModelListeners();
        hookModelLoadListener();

        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
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

    /**
	 * 
	 */
    private void hookModelLoadListener() {
        this.loadListener = new IModelLoadListener() {

            @Override
            public void closed(BSIModel model) {
                removeModelListeners();
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        viewer.setInput(new PlaceHolder("")); //$NON-NLS-1$
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
                synchronized (loadListener) {
                    addISO27KModelListeners();
                }
            }

            @Override
            public void loaded(BpModel model) {
                synchronized (loadListener) {
                    addBpModelListeners();
                }                
            }

            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }

        };
        CnAElementFactory.getInstance().addLoadListener(loadListener);
    }

    /**
     * 
     */
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
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.RelationView_7, e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    protected void addBpModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getInstance().getBpModel().addModITBOModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.RelationView_7, e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }
    
    /**
	 * 
	 */
    protected void addISO27KModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isIsoModelLoaded()) {
                        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.RelationView_7, e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    /**
	 * 
	 */
    protected void removeModelListeners() {
        if (CnAElementFactory.isModelLoaded()) {
            CnAElementFactory.getLoadedModel().removeBSIModelListener(contentProvider);
        }
        if (CnAElementFactory.isIsoModelLoaded()) {
            CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(contentProvider);
        }
        if(CnAElementFactory.isBpModelLoaded()) {
            CnAElementFactory.getInstance().getBpModel().removeBpModelListener(contentProvider);
        }
    }

    public CnATreeElement getInputElement() {
        return this.inputElmt;
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                RelationView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
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
         * Own selection provider returns a CnALin k Object of the selected row.
         * Uses the viewer for all other methods.
         */
        getSite().setSelectionProvider(viewer);

        getSite().getPage().addPartListener(linkWithEditorPartListener);
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
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        super.dispose();
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
            readOnly =  part instanceof CatalogView ? true : false;
            setNewInput((CnATreeElement) element);
        }
    }

    /**
     * @param element
     */
    private void setNewInput(CnATreeElement elmt) {
        this.inputElmt = elmt;
        loadLinks(elmt);
        setViewTitle(Messages.RelationView_9 + " " + elmt.getTitle());
    }

    private void setViewTitle(String title) {
        this.setContentDescription(title);
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(jumpToAction);
        manager.add(new Separator());
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(jumpToAction);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(jumpToAction);
        manager.add(this.linkWithEditorAction);
    }

    private void makeActions() {
        jumpToAction = new Action() {
            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                if (obj == null) {
                    return;
                }
                CnALink link = (CnALink) obj;
                if (CnALink.isDownwardLink(inputElmt, link))
                    setNewInput(link.getDependency());
                else
                    setNewInput(link.getDependant());
            }
        };
        jumpToAction.setText(Messages.RelationView_10);
        jumpToAction.setToolTipText(Messages.RelationView_11);
        jumpToAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ARROW_IN));

        doubleClickAction = new Action() {

            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                CnALink link = (CnALink) obj;

                // open the object on the other side of the link:
                if (CnALink.isDownwardLink(inputElmt, link))
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependency(), readOnly);
                else
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependant(), readOnly);
            }
        };

        linkWithEditorAction = new Action(Messages.RelationView_2, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                toggleLinking(isChecked());
            }
        };
        linkWithEditorAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
        linkWithEditorAction.setChecked(isLinkingActive());
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
	 * 
	 */
    @Override
    public void reload(CnALink oldLink, CnALink newLink) {
        newLink.setDependant(oldLink.getDependant());
        newLink.setDependency(oldLink.getDependency());

        boolean removedLinkDown = getInputElmt().removeLinkDown(oldLink);
        boolean removedLinkUp = getInputElmt().removeLinkUp(oldLink);
        if (removedLinkUp) {
            getInputElmt().addLinkUp(newLink);
        }
        if (removedLinkDown) {
            getInputElmt().addLinkDown(newLink);
        }
        viewer.refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#getInputElmt()
     */
    @Override
    public CnATreeElement getInputElmt() {
        checkAndRetrieve();
        return this.inputElmt;
    }

    /**
     * @return
     */
    private CnATreeElement checkAndRetrieve() {
        CnATreeElement elementWithProperties = Retriever.checkRetrieveElement(inputElmt);
        this.inputElmt.setEntity(elementWithProperties.getEntity());
        CnATreeElement elementWithLinks = Retriever.checkRetrieveLinks(inputElmt, true);
        this.inputElmt.setLinksDown(elementWithLinks.getLinksDown());
        this.inputElmt.setLinksUp(elementWithLinks.getLinksUp());
        return this.inputElmt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.bsi.views.IRelationTable#setInputElmt(sernet.gs
     * .ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void setInputElmt(CnATreeElement inputElmt) {
        this.inputElmt = inputElmt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#reloadAll()
     */
    @Override
    public void reloadAll() {
        loadLinks(inputElmt);
    }

    protected void toggleLinking(boolean checked) {
        this.linkingActive = checked;
        if (checked) {
            editorActivated(getSite().getPage().getActiveEditor());
        }
    }

    protected boolean isLinkingActive() {
        return linkingActive;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.
     * eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart activeEditor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this)) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(activeEditor);
        if (element == null) {
            return;
        }
        if (activeEditor.getEditorInput() instanceof BSIElementEditorInput) {
            BSIElementEditorInput editorInput = (BSIElementEditorInput) activeEditor.getEditorInput();
            readOnly = editorInput.isReadOnly();
        }
        setNewInput(element);
    }
}