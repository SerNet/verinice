package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
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
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
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
public class RelationView extends RightsEnabledView
        implements IRelationTable, ILinkedWithEditorView {

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.RelationView"; //$NON-NLS-1$

    private TableViewer viewer;
    private Action jumpToAction;
    private ISelectionListener selectionListener;
    private CnATreeElement inputElmt;

    private RelationViewContentProvider contentProvider;

    private IModelLoadListener loadListener;

    private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(this);
    private IPropertyChangeListener proceedingFilterDisabledToggleListener;

    private Action linkWithEditorAction;

    private boolean linkingActive = false;

    private boolean readOnly = false;

    @Override
    public String getRightID() {
        return ActionRightIDs.RELATIONS;
    }

    /*
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

    public void loadLinks(final CnATreeElement elmt) {
        if (!CnAElementHome.getInstance().isOpen() || inputElmt == null) {
            return;
        }

        Display.getDefault().syncExec(() -> {
            setContentDescription(Messages.RelationView_9 + " " + elmt.getTitle());
            viewer.setInput(new PlaceHolder(Messages.RelationView_0));
        });

        WorkspaceJob job = new WorkspaceJob(Messages.RelationView_0) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();

                FindRelationsFor command = new FindRelationsFor(elmt);
                try {

                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    final CnATreeElement linkElmt = command.getElmt();
                    Display.getDefault().syncExec(() -> viewer.setInput(linkElmt));
                } catch (Exception e) {
                    viewer.setInput(new PlaceHolder(Messages.RelationView_3));
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
        viewer.setComparator(new RelationByNameSorter(this, COLUMN_TITLE, COLUMN_TYPE_IMG));

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.RECREATE);
        List<PathCellLabelProvider> cellLabelProviders = ((RelationTableViewer) viewer)
                .initToolTips(relationViewLabelProvider, parent);

        // register resize listener for cutting the tooltips
        addResizeListener(parent, cellLabelProviders);

        toggleLinking(Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.LINK_TO_EDITOR));

        // try to add listeners once on startup, and register for model changes:
        hookModelLoadListener();
        proceedingFilterDisabledToggleListener = event -> {
            if (PreferenceConstants.FILTER_INFORMATION_NETWORKS_BY_PROCEEDING
                    .equals(event.getProperty())) {
                viewer.refresh();
            }
        };
        Activator.getDefault().getPreferenceStore()
                .addPropertyChangeListener(proceedingFilterDisabledToggleListener);

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
    private void addResizeListener(final Composite parent,
            final List<PathCellLabelProvider> cellLabelProviders) {

        parent.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                for (PathCellLabelProvider c : cellLabelProviders) {
                    c.updateShellWidthAndX(parent.getShell().getBounds().width,
                            parent.getShell().getBounds().x);
                }
            }

        });
    }

    private void hookModelLoadListener() {
        this.loadListener = new DefaultModelLoadListener() {

            private AtomicBoolean bsiModelListenerRegistered = new AtomicBoolean(false);
            private AtomicBoolean isoModelListenerRegistered = new AtomicBoolean(false);
            private AtomicBoolean bpModelListenerRegistered = new AtomicBoolean(false);

            @Override
            public void closed(BSIModel model) {
                Display.getDefault().asyncExec(() -> viewer.setInput(new PlaceHolder("")));
            }

            @Override
            public void loaded(BSIModel model) {
                if (bsiModelListenerRegistered.compareAndSet(false, true)) {
                    CnAElementFactory.getInstance().ifModelLoaded(
                            bsiModel -> bsiModel.addBSIModelListener(contentProvider));
                }
            }

            @Override
            public void loaded(ISO27KModel model) {
                if (isoModelListenerRegistered.compareAndSet(false, true)) {
                    CnAElementFactory.getInstance()
                            .ifIsoModelLoaded(isoModel -> CnAElementFactory.getInstance()
                                    .getISO27kModel().addISO27KModelListener(contentProvider));
                }
            }

            @Override
            public void loaded(BpModel model) {
                if (bpModelListenerRegistered.compareAndSet(false, true)) {
                    CnAElementFactory.getInstance().ifBpModelLoaded(
                            bpModel -> bpModel.addModITBOModelListener(contentProvider));
                }
            }

        };
        CnAElementFactory.getInstance().addLoadListener(loadListener);
    }

    protected void removeModelListeners() {
        if (CnAElementFactory.isModelLoaded()) {
            CnAElementFactory.getLoadedModel().removeBSIModelListener(contentProvider);
        }
        if (CnAElementFactory.isIsoModelLoaded()) {
            CnAElementFactory.getInstance().getISO27kModel()
                    .removeISO27KModelListener(contentProvider);
        }
        if (CnAElementFactory.isBpModelLoaded()) {
            CnAElementFactory.getInstance().getBpModel().removeBpModelListener(contentProvider);
        }
    }

    public CnATreeElement getInputElement() {
        return this.inputElmt;
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(RelationView.this::fillContextMenu);
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
        selectionListener = this::pageSelectionChanged;
        getSite().getPage().addPostSelectionListener(selectionListener);

        /**
         * Own selection provider returns a CnALin k Object of the selected row.
         * Uses the viewer for all other methods.
         */
        getSite().setSelectionProvider(viewer);

        getSite().getPage().addPartListener(linkWithEditorPartListener);
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        CnAElementFactory.getInstance().removeLoadListener(loadListener);
        removeModelListeners();
        getSite().getPage().removePostSelectionListener(selectionListener);
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        Activator.getDefault().getPreferenceStore()
                .removePropertyChangeListener(proceedingFilterDisabledToggleListener);
        super.dispose();
    }

    protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part == this) {
            return;
        }
        if (!getSite().getPage().isPartVisible(this)) {
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
            readOnly = part instanceof CatalogView;
            setNewInput((CnATreeElement) element);
        }
    }

    private void setNewInput(CnATreeElement elmt) {
        this.inputElmt = elmt;
        loadLinks(elmt);
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
        jumpToAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.ARROW_IN));

        linkWithEditorAction = new Action(Messages.RelationView_2, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                toggleLinking(isChecked());
            }
        };
        linkWithEditorAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
        linkWithEditorAction.setChecked(isLinkingActive());
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(event -> {
            ISelection selection = event.getViewer().getSelection();
            if (!selection.isEmpty()) {
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                CnALink link = (CnALink) obj;

                // open the object on the other side of the link:
                if (CnALink.isDownwardLink(inputElmt, link))
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependency(), readOnly);
                else
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependant(), readOnly);
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
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#getInputElmt()
     */
    @Override
    public CnATreeElement getInputElmt() {
        if (inputElmt != null) {
            checkAndRetrieve(inputElmt);
        }
        return inputElmt;
    }

    private static void checkAndRetrieve(@NonNull CnATreeElement inputElmt) {
        CnATreeElement elementWithProperties = Retriever.checkRetrieveElement(inputElmt);
        inputElmt.setEntity(elementWithProperties.getEntity());
        CnATreeElement elementWithLinks = Retriever.checkRetrieveLinks(inputElmt, true);
        inputElmt.setLinksDown(elementWithLinks.getLinksDown());
        inputElmt.setLinksUp(elementWithLinks.getLinksUp());
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.bsi.views.IRelationTable#setInputElmt(sernet.gs
     * .ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void setInputElmt(CnATreeElement inputElmt) {
        this.inputElmt = inputElmt;
    }

    /*
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#reloadAll()
     */
    @Override
    public void reloadAll() {
        loadLinks(inputElmt);
    }

    protected void toggleLinking(boolean checked) {
        this.linkingActive = checked;
        if (checked) {
            Optional.ofNullable(getSite().getPage().getActiveEditor())
                    .ifPresent(this::editorActivated);
        }
    }

    protected boolean isLinkingActive() {
        return linkingActive;
    }

    /*
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
            BSIElementEditorInput editorInput = (BSIElementEditorInput) activeEditor
                    .getEditorInput();
            readOnly = editorInput.isReadOnly();
        }
        setNewInput(element);
    }
}