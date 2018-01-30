/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.Perspective;
import sernet.gs.ui.rcp.main.actions.GSMBasicSecurityCheckAction;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.actions.ShowKonsolidatorAction;
import sernet.gs.ui.rcp.main.bsi.actions.BausteinZuordnungAction;
import sernet.gs.ui.rcp.main.bsi.actions.GSMBausteinZuordnungAction;
import sernet.gs.ui.rcp.main.bsi.actions.NaturalizeAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinUmsetzungTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IBSIStrukturElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.SearchViewElementTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditor;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.filter.LebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ObjektLebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NullModel;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.rcp.tree.TreeUpdateListener;
import sernet.verinice.service.commands.crud.LoadCnAElementByType;
import sernet.verinice.service.tree.ElementManager;

/**
 * View for a tree structure, representing the data model for modeling an IT
 * Baseline Protection implementation comprising IT Networks, associated
 * Controls, Risk Analysis's etc.
 * 
 * @author koderman[at]sernet[dot]de *
 */
public class BsiModelView extends RightsEnabledView
        implements IAttachedToPerspective, ILinkedWithEditorView {

    private static final Logger LOG = Logger.getLogger(BsiModelView.class);

    public static final String ID = "sernet.gs.ui.rcp.main.views.bsimodelview"; //$NON-NLS-1$

    private Action doubleClickAction;

    private DrillDownAdapter drillDownAdapter;

    private BSIModel model;

    private TreeViewer viewer;

    private BSIModelViewFilterAction filterAction;

    private ElementManager elementManager;

    private Action expandAllAction;

    private Action collapseAction;
    
    private Action linkWithEditorAction;

    private ShowBulkEditAction bulkEditAction;

    private ShowAccessControlEditAction accessControlEditAction;

    private NaturalizeAction naturalizeAction;

    private Action selectEqualsAction;

    private ShowKonsolidatorAction konsolidatorAction;

    private GSMBasicSecurityCheckAction gsmbasicsecuritycheckAction;

    private BausteinZuordnungAction bausteinZuordnungAction;

    private GSMBausteinZuordnungAction gsmbausteinZuordnungAction;

    private IModelLoadListener modelLoadListener;

    private TreeUpdateListener bsiModelListener;

    private boolean linkingActive = false;
    
    private IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);

    public BsiModelView() {
        
        elementManager = new ElementManager();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        model.removeBSIModelListener(bsiModelListener);
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        super.dispose();
    }

    public void setNullModel() {
        model = new NullModel();

        refreshModelAsync();

    }

    private void refreshModelAsync() {

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    viewer.setInput(model);
                    viewer.refresh();
                } catch (Exception e) {
                    ExceptionUtil.log(e, Messages.BsiModelView_18);
                }
            }
        });
    }

    @Override
    public String getRightID(){
        return ActionRightIDs.BSIMODELVIEW;
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

    private void addBSIFilter() {
        viewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IDatenschutzElement) {
                    return false;
                }
                return true;
            }

        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.
     * widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            LOG.error("Error while creating organization view", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.BsiModelView_7);
        }

        loadItBaselineProtectionCatalogs();
    }

    private void initView(Composite parent) {
        IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();

        viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        drillDownAdapter = new DrillDownAdapter(viewer);
        
        TreeContentProvider contentProvider = new TreeContentProvider(elementManager);
        viewer.setContentProvider(contentProvider);
                
        viewer.setLabelProvider(new DecoratingLabelProvider(new BSIModelViewLabelProvider(),
                workbench.getDecoratorManager()));
        viewer.setSorter(new CnAElementByTitelSorter());
        toggleLinking(Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.LINK_TO_EDITOR));

        getSite().setSelectionProvider(viewer);
        makeActions();
        createPullDownMenu();
        hookContextMenu();
        hookDoubleClickAction();
        hookDNDListeners();
        addBSIFilter();
        fillLocalToolBar();
        getSite().getPage().addPartListener(linkWithEditorPartListener);
        setNullModel();
    }

    protected void startInitDataJob() {
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.BsiModelView_5) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.BsiModelView_5, IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", //$NON-NLS-1$
                            Messages.BsiModelView_9, e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    private void initData() {
        if (CnAElementFactory.isModelLoaded()) {
            setModel(CnAElementFactory.getLoadedModel());
        } else if (modelLoadListener == null) {
            // model is not loaded yet: add a listener to load data when it's
            // laoded
            modelLoadListener = new IModelLoadListener() {
                @Override
                public void closed(BSIModel model) {
                    // nothing to do
                }

                @Override
                public void loaded(BSIModel model) {
                    startInitDataJob();
                }

                @Override
                public void loaded(ISO27KModel model) {
                    // nothing to do            
                }

                @Override
                public void loaded(BpModel model) {
                 // nothing to do
                    
                }

                @Override
                public void loaded(CatalogModel model) {
                    // do nothing
                }
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }

    private static void loadItBaselineProtectionCatalogs() {
        WorkspaceJob job = new OpenCataloguesJob(Messages.BSIMassnahmenView_0);
        JobScheduler.scheduleInitJob(job);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(new GroupMarker("content")); //$NON-NLS-1$
        manager.add(new Separator());
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator());
        manager.add(new GroupMarker("special")); //$NON-NLS-1$
        manager.add(bulkEditAction);
        manager.add(accessControlEditAction);
        manager.add(naturalizeAction);
        manager.add(selectEqualsAction);
        selectEqualsAction.setEnabled(bausteinSelected());
        manager.add(konsolidatorAction);
        manager.add(gsmbasicsecuritycheckAction);
        manager.add(bausteinZuordnungAction);
        manager.add(gsmbausteinZuordnungAction);

        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(expandAllAction);
        manager.add(collapseAction);
    }

    private boolean bausteinSelected() {
        IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
        if (!sel.isEmpty() && sel.size() == 1
                && sel.getFirstElement() instanceof BausteinUmsetzung) {
            return true;
        }
        return false;
    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.filterAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        manager.add(linkWithEditorAction);
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());

        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void hookDNDListeners() {

        Transfer[] dropTypes = new Transfer[] { IGSModelElementTransfer.getInstance(),
                BausteinUmsetzungTransfer.getInstance(),
                IBSIStrukturElementTransfer.getInstance(),
                SearchViewElementTransfer.getInstance(),
                ISO27kElementTransfer.getInstance() };
        Transfer[] dragTypes = new Transfer[] { IBSIStrukturElementTransfer.getInstance(),
                BausteinUmsetzungTransfer.getInstance() };

        int operations = DND.DROP_COPY | DND.DROP_MOVE;
        viewer.addDropSupport(operations, dropTypes, new BSIModelViewDropListener(viewer));
        viewer.addDragSupport(operations, dragTypes, new BSIModelViewDragListener(viewer));
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void makeActions() {

        final int newSelDefaultSize = 10;
        selectEqualsAction = new Action() {

            @Override
            public void run() {

                IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
                Object o = sel.getFirstElement();
                if (o instanceof BausteinUmsetzung) {
                    BausteinUmsetzung sourceBst = (BausteinUmsetzung) o;
                    ArrayList<BausteinUmsetzung> newsel = new ArrayList<>(newSelDefaultSize);
                    newsel.add(sourceBst);

                    try {
                        LoadCnAElementByType<BausteinUmsetzung> command = new LoadCnAElementByType<BausteinUmsetzung>(
                                BausteinUmsetzung.class);
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        List<BausteinUmsetzung> bausteine = command.getElements();

                        for (BausteinUmsetzung bst : bausteine) {
                            if (bst.getKapitel().equals(sourceBst.getKapitel())) {
                                newsel.add(bst);
                            }
                        }
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, ""); //$NON-NLS-1$
                    }

                    viewer.setSelection(new StructuredSelection(newsel));
                }
            }
        };
        selectEqualsAction.setText(Messages.BsiModelView_11);

        bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(),
                Messages.BsiModelView_13);

        accessControlEditAction = new ShowAccessControlEditAction(
                getViewSite().getWorkbenchWindow(), Messages.BsiModelView_14);

        naturalizeAction = new NaturalizeAction(getViewSite().getWorkbenchWindow());

        konsolidatorAction = new ShowKonsolidatorAction(getViewSite().getWorkbenchWindow(),
                Messages.BsiModelView_15);

        gsmbasicsecuritycheckAction = new GSMBasicSecurityCheckAction(
                getViewSite().getWorkbenchWindow(), Messages.BsiModelView_19);

        bausteinZuordnungAction = new BausteinZuordnungAction(getViewSite().getWorkbenchWindow());

        gsmbausteinZuordnungAction = new GSMBausteinZuordnungAction(
                getViewSite().getWorkbenchWindow());

        doubleClickAction = new Action() {

            @Override
            public void run() {
                
                Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();

                if (sel instanceof FinishedRiskAnalysis) {
                    FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) sel;
                    if (CnAElementHome.getInstance().isWriteAllowed(analysis)) {
                        RiskAnalysisWizard wizard = new RiskAnalysisWizard(analysis.getParent(),
                                analysis);
                        wizard.init(PlatformUI.getWorkbench(), null);
                        WizardDialog wizDialog = new org.eclipse.jface.wizard.WizardDialog(
                                new Shell(), wizard);
                        wizDialog.setPageSize(wizard.getWidth(), wizard.getHeight());

                        wizDialog.open();
                    } else {
                        MessageDialog.openError(viewer.getTree().getShell(),
                                Messages.BsiModelView_RA_0, Messages.BsiModelView_RA_1);
                    }
                } else {
                    EditorFactory.getInstance().updateAndOpenObject(sel);
                }
            }
        };

        BSIModelElementFilter modelElementFilter = new BSIModelElementFilter(viewer);

        filterAction = new BSIModelViewFilterAction(Messages.BsiModelView_3,
                new MassnahmenUmsetzungFilter(viewer), new MassnahmenSiegelFilter(viewer),
                new LebenszyklusPropertyFilter(viewer),
                new ObjektLebenszyklusPropertyFilter(viewer), modelElementFilter,
                new TagFilter(viewer));

        expandAllAction = new Action() {

            @Override
            public void run() {

                expandAll();
            }
        };
        expandAllAction.setText(Messages.BsiModelView_16);
        expandAllAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAction = new Action() {

            @Override
            public void run() {

                viewer.collapseAll();
            }
        };
        collapseAction.setText(Messages.BsiModelView_17);
        collapseAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

        linkWithEditorAction = new Action(Messages.BsiModelView_6, IAction.AS_CHECK_BOX) {

            @Override
            public void run() {

                toggleLinking(isChecked());
            }
        };
        linkWithEditorAction.setChecked(isLinkingActive());
        linkWithEditorAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
    }

    protected void buildBausteinUmsetzung(BausteinUmsetzung sourceBst) {

        final int newSelDefaultSize = 10;

        ArrayList<BausteinUmsetzung> newsel = new ArrayList<>(newSelDefaultSize);
        newsel.add(sourceBst);
        try {
            LoadCnAElementByType<BausteinUmsetzung> command = new LoadCnAElementByType<>(BausteinUmsetzung.class);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<BausteinUmsetzung> bausteine = command.getElements();

            for (BausteinUmsetzung bst : bausteine) {
                if (bst.getKapitel().equals(sourceBst.getKapitel())) {
                    newsel.add(bst);
                }
            }

        } catch (CommandException e) {
            ExceptionUtil.log(e, ""); //$NON-NLS-1$
        }

        viewer.setSelection(new StructuredSelection(newsel));
    }

    private void expandAll() {

        // TODO: do this a new thread and show user a progress bar
        viewer.expandAll();
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private void createPullDownMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(filterAction);
        menuManager.add(expandAllAction);
        menuManager.add(collapseAction);

        menuManager.add(new Separator());
    }

    public void setModel(BSIModel newModel) {

        // create listener only once:
        if (bsiModelListener == null) {
            bsiModelListener = new TreeUpdateListener(viewer, elementManager);
        }

        if (model != null) {
            // remove listener from old model:
            model.removeBSIModelListener(bsiModelListener);
        }

        this.model = newModel;
        model.addBSIModelListener(bsiModelListener);

        refreshModelAsync();

    }

    public IStructuredSelection getSelection() {
        return (IStructuredSelection) viewer.getSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    @Override
    public String getPerspectiveId() {
        return Perspective.ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.
     * eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart editor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this)) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(editor);
        if(element == null){
            element = getElementFromAttachment(editor);
        }
        if (element != null && ((element instanceof IBSIStrukturElement)
                || (element instanceof MassnahmenUmsetzung)
                || (element instanceof BausteinUmsetzung))) {
           viewer.setSelection(new StructuredSelection(element),true);          
        }      
        return;    
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
    
    /**
     * gets Element that is referenced by attachment shown by editor
     * 
     * @param editor
     *            - ({@link AttachmentEditor}) Editor of {@link Attachment}
     * @return {@link CnATreeElement}
     */
    private static CnATreeElement getElementFromAttachment(IEditorPart editor) {
        return AttachmentEditorInput.extractCnaTreeElement(editor);
    }
}
