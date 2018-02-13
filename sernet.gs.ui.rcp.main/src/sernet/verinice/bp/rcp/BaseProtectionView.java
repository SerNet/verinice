/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp;

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
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.bsi.actions.NaturalizeAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionModelingTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditor;
import sernet.gs.ui.rcp.main.bsi.editors.AttachmentEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.iso27k.rcp.action.CollapseAction;
import sernet.verinice.iso27k.rcp.action.ExpandAction;
import sernet.verinice.iso27k.rcp.action.HideEmptyFilter;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TagParameter;
import sernet.verinice.model.common.TypeParameter;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.rcp.ViewFilterAction;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.bp.BaseProtectionPerspective;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.rcp.tree.TreeLabelProvider;
import sernet.verinice.rcp.tree.TreeUpdateListener;
import sernet.verinice.service.tree.ElementManager;

/**
 * This view shows all elements of a base protection IT network
 * in a tree. It provides action to add, edit, delete and manage these elements.
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 * @author Daniel Murygin dm[at]sernet.de
 */
public class BaseProtectionView extends RightsEnabledView 
    implements IAttachedToPerspective, ILinkedWithEditorView {
    
    private static final Logger LOG = Logger.getLogger(BaseProtectionView.class);
    
    public static final String ID = "sernet.verinice.bp.rcp.BaseProtectionView"; //$NON-NLS-1$
    
    private static int operations = DND.DROP_COPY | DND.DROP_MOVE;   
    private Object mutex = new Object();
    
    protected TreeViewer viewer;
    private TreeContentProvider contentProvider;
    private ElementManager elementManager;
    
    private DrillDownAdapter drillDownAdapter;
    
    private IModelLoadListener modelLoadListener;
    private IBpModelListener modelUpdateListener;
    private IPartListener2 linkWithEditorPartListener  = new LinkWithEditorPartListener(this);   

    private Action doubleClickAction;
    private Action linkWithEditorAction;
    private ShowBulkEditAction bulkEditAction;
    private ExpandAction expandAction;
    private Action expandAllAction;
    private CollapseAction collapseAction;
    private Action collapseAllAction;
    private ViewFilterAction filterAction;
    private ShowAccessControlEditAction accessControlEditAction;
    private NaturalizeAction naturalizeAction;
    
    private MetaDropAdapter metaDropAdapter;
   
    private boolean linkingActive = false;
    
    public BaseProtectionView() {
        super();
        elementManager = new ElementManager();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        super.createPartControl(parent);
        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            LOG.error("Error while creating organization view", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.BaseProtectionView_ErrorCreating);
        }
    }

    protected void initView(Composite parent) {
        IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
        if (CnAElementFactory.isBpModelLoaded()) {
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        }

        contentProvider = new TreeContentProvider(elementManager);
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new DecoratingLabelProvider(new TreeLabelProvider(), workbench.getDecoratorManager()));
        viewer.setSorter(new BaseProtectionTreeSorter());
        toggleLinking(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LINK_TO_EDITOR));

        getSite().setSelectionProvider(viewer);
        hookContextMenu();
        makeActions();
        addActions();
        fillToolBar();
        hookDndListeners();

        getSite().getPage().addPartListener(linkWithEditorPartListener);
        viewer.refresh(true);
    }

    protected void startInitDataJob() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MotITBPview: startInitDataJob"); //$NON-NLS-1$
        }
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.BaseProtectionView_Loading_1) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.BaseProtectionView_Loading_2, IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data",e); //$NON-NLS-1$ //$NON-NLS-2$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    protected void initData() { 
        if (LOG.isDebugEnabled()) {
            LOG.debug("BaseProtectionView: initData"); //$NON-NLS-1$
        }
        synchronized (mutex) {
            if(CnAElementFactory.isBpModelLoaded()) {
                if (modelUpdateListener == null ) {
                    // model listener should only be created once!
                    if (LOG.isDebugEnabled()){
                        Logger.getLogger(this.getClass()).debug("Creating modelUpdateListener for BaseProtectionView."); //$NON-NLS-1$
                    }
                    modelUpdateListener = new TreeUpdateListener(viewer,elementManager);
                    CnAElementFactory.getInstance().getBpModel().addModITBOModelListener(modelUpdateListener);
                    Display.getDefault().syncExec(new Runnable(){
                        @Override
                        public void run() {
                            setInput(CnAElementFactory.getInstance().getBpModel());
                            viewer.refresh();
                        }
                    });
                }
            } else if(modelLoadListener==null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ISMView No model loaded, adding model load listener."); //$NON-NLS-1$
                }
                // model is not loaded yet: add a listener to load data when it's loaded
                modelLoadListener = new IModelLoadListener() {                  
                    @Override
                    public void closed(BSIModel model) { /* nothing to do*/  }                 
                    @Override
                    public void loaded(BSIModel model) { /* nothing to do*/  }                 
                    @Override
                    public void loaded(ISO27KModel model) { /* nothing to do*/  }

                    @Override
                    public void loaded(BpModel model) {
                        startInitDataJob();
                    }

                    @Override
                    public void loaded(CatalogModel model) { /* nothing to do */ }
                };
                CnAElementFactory.getInstance().addLoadListener(modelLoadListener);             
            }
        }
    }

    public void setInput(BpModel model) {
        viewer.setInput(model);
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
    
    private void hookDndListeners() {
        Transfer[] dragTypes = new Transfer[] { BaseProtectionElementTransfer.getInstance() };
        Transfer[] dropTypes = new Transfer[] { IGSModelElementTransfer.getInstance(),
                BaseProtectionElementTransfer.getInstance(),
                BaseProtectionModelingTransfer.getInstance()};

        viewer.addDragSupport(operations, dragTypes, new BSIModelViewDragListener(viewer));
        viewer.addDropSupport(operations, dropTypes, metaDropAdapter);
    }
     
    protected void fillContextMenu(IMenuManager manager) {
        manager.add(new GroupMarker("content")); //$NON-NLS-1$
        manager.add(new Separator());
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator());
        manager.add(new GroupMarker("special")); //$NON-NLS-1$
        manager.add(bulkEditAction);
        manager.add(accessControlEditAction);
        manager.add(naturalizeAction);
        manager.add(new Separator());
        manager.add(expandAction);
        manager.add(collapseAction);
        drillDownAdapter.addNavigationActions(manager); 
    }
    
    private void makeActions() {
        
        doubleClickAction = new Action() {
            @Override
            public void run() {
                if(viewer.getSelection() instanceof IStructuredSelection) {
                    Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                    EditorFactory.getInstance().updateAndOpenObject(sel);
                }
            }
        };
        
        makeExpandAndCollapseActions();
     
        bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(), 
                Messages.BaseProtectionView_BulkEdit);
              
        BSIModelViewDropListener bsiDropAdapter;
        metaDropAdapter = new MetaDropAdapter(viewer);
        bsiDropAdapter = new BSIModelViewDropListener(viewer);
        BbModelingDropPerformer modelingDropPerformer = new BbModelingDropPerformer();
        GsCatalogModelingDropPerformer gsCatalogModelingDropPerformer = 
                new GsCatalogModelingDropPerformer();
        metaDropAdapter.addAdapter(modelingDropPerformer);
        metaDropAdapter.addAdapter(bsiDropAdapter);
        metaDropAdapter.addAdapter(gsCatalogModelingDropPerformer);
        
        linkWithEditorAction = new Action(Messages.BaseProtectionView_LinkWithEditor, IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                toggleLinking(isChecked());
            }
        };
        linkWithEditorAction.setChecked(isLinkingActive());
        linkWithEditorAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.LINKED));
        naturalizeAction = new NaturalizeAction(getViewSite().getWorkbenchWindow());
        accessControlEditAction = new ShowAccessControlEditAction(getViewSite().getWorkbenchWindow(), Messages.BaseProtectionView_AccessControl);
        

        makeFilterAction();
    }

    private void makeFilterAction() {
        HideEmptyFilter hideEmptyFilter = createHideEmptyFilter();
        TypeParameter typeParameter = createTypeParameter();
        TagParameter tagParameter = new TagParameter();
        filterAction = new ViewFilterAction(viewer,
                "Filter...", //  //$NON-NLS-1$
                tagParameter,
                hideEmptyFilter,
                typeParameter);
        filterAction.setTypes(ViewFilterAction.BASE_PROTECTION_TYPES);
        elementManager.addParameter(tagParameter);
        if(typeParameter!=null) {
            elementManager.addParameter(typeParameter);
        }
    }

    protected void makeExpandAndCollapseActions() {
        expandAction = new ExpandAction(viewer, contentProvider);
        expandAction.setText(Messages.BaseProtectionView_ExpandChildren);
        expandAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));
        
        collapseAction = new CollapseAction(viewer);
        collapseAction.setText(Messages.BaseProtectionView_CollapseChildren);
        collapseAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
        
        expandAllAction = new Action() {
            @Override
            public void run() {
                expandAll();
            }
        };
        expandAllAction.setText(Messages.BaseProtectionView_ExpandAll);
        expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAllAction = new Action() {
            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText(Messages.BaseProtectionView_CollapseAll);
        collapseAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
    }
    
    protected void expandAll() {
        viewer.expandAll();
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
     * Override this in subclasses to hide empty groups
     * on startup.
     * 
     * @return a HideEmptyFilter
     */
    protected HideEmptyFilter createHideEmptyFilter() {
        return new HideEmptyFilter(viewer);
    }

    /**
     * Override this in subclasses to hide empty groups
     * on startup.
     * 
     * @return a {@link TypeParameter}
     */
    protected TypeParameter createTypeParameter() {
        return new TypeParameter();
    }
    
    private void addActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });     
        viewer.addSelectionChangedListener(expandAction);
        viewer.addSelectionChangedListener(collapseAction);
    }
    
    protected void fillToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        drillDownAdapter.addNavigationActions(manager);
        manager.add(filterAction);
        manager.add(linkWithEditorAction);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart editor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this)) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(editor);
        if(element == null && editor.getEditorInput() instanceof AttachmentEditorInput){
            element = getElementFromAttachment(editor);                
        }
        if(element == null || !(element instanceof IBpElement)) {
            return;
        }       
        if (LOG.isDebugEnabled()) {
            LOG.debug("Element in editor: " + element.getUuid()); //$NON-NLS-1$
            LOG.debug("Expanding tree now to show element..."); //$NON-NLS-1$
        }
        viewer.setSelection(new StructuredSelection(element),true);        
        LOG.debug("Tree is expanded."); //$NON-NLS-1$
    }
    
    /**
     * gets Element that is referenced by attachment shown by editor
     * @param editor - ({@link AttachmentEditor}) Editor of {@link Attachment}
     * @return {@link CnATreeElement}
     */
    private CnATreeElement getElementFromAttachment(IEditorPart editor) {
        return AttachmentEditorInput.extractCnaTreeElement(editor);
    }

    @Override
    public String getPerspectiveId() {
        return BaseProtectionPerspective.ID;
    }
    
    @Override
    public void dispose() {
        elementManager.clearCache();
        if(CnAElementFactory.isBpModelLoaded()) {
            CnAElementFactory.getInstance().getBpModel().removeBpModelListener(modelUpdateListener);
        }
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
//        getSite().getPage().removePartListener(linkWithEditorPartListener);
        super.dispose();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BASEPROTECTIONVIEW;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }
    
}
