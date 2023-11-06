/*******************************************************************************
 * Copyright (c) 2023 Urs Zeidler <uz@sernet.de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package sernet.verinice.bcm.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.viewers.internal.GraphModelEntityFactory;
import org.eclipse.zest.core.viewers.internal.GraphModelEntityRelationshipFactory;
import org.eclipse.zest.core.viewers.internal.GraphModelFactory;
import org.eclipse.zest.core.viewers.internal.IStylingGraphModelFactory;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.bp.BCMUtils.BCMProperties;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElementLabelGenerator;
import sernet.verinice.model.common.Domain;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.service.commands.CnATypeMapper;
import sernet.verinice.service.commands.task.FindRelationsFor;
import sernet.verinice.zest.AbstractZestLabelProvider;

public class BiaView extends RightsEnabledView
        implements IZoomableWorkbenchPart, ILinkedWithEditorView, ISelectionListener {

    public static final String ID = "sernet.verinice.bcm.ui.biaview"; //$NON-NLS-1$
    private AtomicBoolean working = new AtomicBoolean(false);
    private AtomicBoolean linkWithEditor = new AtomicBoolean(false);
    private GraphViewer graphViewer;
    private Displaymode displayMode = Displaymode.MIN_MTPD;
    private ViewMode viewMode = ViewMode.UNDEFINED;
    private int selectedLayoutIndex = 0;
    private Color black = new Color(0, 0, 0);
    private Color otherRelation;
    private Color selected;
    private List<String> affectedElementTypes = List.of(Application.TYPE_ID, ItSystem.TYPE_ID,
            IcsSystem.TYPE_ID, Device.TYPE_ID, Network.TYPE_ID, Room.TYPE_ID, Asset.TYPE_ID);
    private Map<String, Color> linkColors = new HashMap<>();
    private Set<String> processRelationIds;
    private Set<CnATreeElement> currentElements = Collections.emptySet();
    private LinkWithEditorPartListener linkWithEditorlistener = new LinkWithEditorPartListener(
            this);
    private List<LayoutAlgorithm> layoutAlgos;
    private List<String> layoutAlgoImages;
    private List<String> layoutAlgoTooltip;
    private List<ToolItem> modeActions;
    private Domain selectedDomain;

    private final class BasicGraphViewer extends GraphViewer {
        private IStylingGraphModelFactory modelFactory;

        private BasicGraphViewer(Composite composite, int style) {
            super(composite, style);
        }

        protected IStylingGraphModelFactory getFactory() {
            modelFactory = new GraphModelEntityRelationshipFactory(this) {
                @Override
                public void styleConnection(GraphConnection conn) {
                    super.styleConnection(conn);
                    ((List<?>) conn.getConnectionFigure().getChildren()).stream()
                            .filter(Label.class::isInstance).map(Label.class::cast)
                            .forEach(l -> l.setOpaque(true));
                }
            };
            return modelFactory;
        }
    }

    public enum Displaymode {
        MIN_MTPD, RTO, RPO
    }

    public enum ViewMode {
        PROCESS(List.of(Displaymode.MIN_MTPD, Displaymode.RTO, Displaymode.RPO)), //
        RESOURCE(List.of(Displaymode.RTO, Displaymode.RPO)), //
        UNDEFINED(Collections.emptyList());

        ViewMode(List<Displaymode> list) {
            this.availableModes = list;
        }

        private List<Displaymode> availableModes;

        public List<Displaymode> getAvailableModes() {
            return availableModes;
        }
    }

    public class ZestNodeContentProvider extends ArrayContentProvider
            implements IGraphEntityRelationshipContentProvider {

        @Override
        public Object[] getRelationships(Object source, Object dest) {
            if (source instanceof CnATreeElement) {
                CnATreeElement ce = (CnATreeElement) source;
                return ce.getLinksDown().stream().filter(l -> l.getDependency().equals(dest))
                        .toArray();
            }
            return new Object[] {};
        }
    }

    public class ZestLabelProvider extends AbstractZestLabelProvider {
        @Override
        public String getText(Object element) {

            if (element instanceof CnATreeElement) {
                CnATreeElement ce = (CnATreeElement) element;
                return CnATreeElementLabelGenerator.getElementTitle(ce) + "\n" + toLabelValue(ce); //$NON-NLS-1$
            }
            if (element instanceof CnALink) {
                CnALink link = (CnALink) element;
                return CnALink.getRelationName(link.getDependant(), link);

            }
            return ""; //$NON-NLS-1$
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof CnATreeElement) {
                CnATreeElement ce = (CnATreeElement) element;
                return CnAImageProvider.getImage(ce, true);
            }

            return null;
        }

        @Override
        public Color getBorderColor(Object entity) {
            if (currentElements.contains(entity))
                return selected;
            return null;
        }

        @Override
        public int getBorderWidth(Object entity) {
            if (currentElements.contains(entity))
                return 2;
            return 1;
        }

        @Override
        public Color getForegroundColour(Object entity) {
            if (currentElements.contains(entity))
                return black;
            return null;
        }

        @Override
        public int getConnectionStyle(Object rel) {
            return ZestStyles.CONNECTIONS_DIRECTED;
        }

        @Override
        public Color getColor(Object rel) {
            if (rel instanceof CnALink) {
                CnALink cl = (CnALink) rel;
                String typeId = cl.getRelationId();

                return linkColors.getOrDefault(typeId, otherRelation);
            }
            return null;
        }

        private String toLabelValue(CnATreeElement source) {
            BCMProperties bcmProperties = sernet.verinice.model.bp.BCMUtils
                    .getPropertiesForElement(source);
            switch (displayMode) {
            case MIN_MTPD:
                if (selectedDomain == Domain.ISM) {
                    String propertyMtpdMin = source.getTypeId()+"_bcm_mtpd"; //$NON-NLS-1$
                    return Messages.BiaView_MtpdPrefix + propertyValue(source, propertyMtpdMin);
                } else {
                    String propertyMtpdMin = bcmProperties.propertyMtpdMin;
                    return Messages.BiaView_MinMtpdPrefix + propertyValue(source, propertyMtpdMin);
                }
            case RTO:
                String propertyRto = isProcess(source) ? bcmProperties.propertyRto
                        : bcmProperties.propertyMinRto;
                return Messages.BiaView_RtoPrefix + propertyValue(source, propertyRto);
            case RPO:
                String propertyRpo = bcmProperties.propertyRpo;
                return Messages.BiaView_RPOPrefix + propertyValue(source, propertyRpo);
            default:
                return Messages.BiaView_noValue;
            }
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        Composite compositeHeader = new Composite(parent, SWT.NONE);
        compositeHeader.setLayout(new GridLayout(4, false));
        compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        ToolBar toolBar = new ToolBar(compositeHeader, SWT.FLAT | SWT.RIGHT);
        toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        modeActions = Arrays.stream(Displaymode.values()).map(dm -> {
            ToolItem item = new ToolItem(toolBar, SWT.RADIO);
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    displayMode = dm;
                    graphViewer.refresh();
                }
            });
            item.setText(dm.name());
            item.setData(dm);
            item.setToolTipText(Messages.bind(Messages.BiaView_viewModeTooltip, dm.name()));
            item.setSelection(dm == displayMode);
            return item;
        }).collect(Collectors.toList());

        createLayoutAlgos();
        ToolBar toolBarLayout = new ToolBar(compositeHeader, SWT.FLAT | SWT.RIGHT);
        layoutAlgos.forEach(la -> {
            ToolItem toolItemLayout = new ToolItem(toolBarLayout, SWT.RADIO);
            toolItemLayout.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    graphViewer.setLayoutAlgorithm(la, true);
                    selectedLayoutIndex = layoutAlgos.indexOf(la);
                }
            });
            toolItemLayout.setSelection(selectedLayoutIndex == layoutAlgos.indexOf(la));
            toolItemLayout.setImage(ImageCache.getInstance()
                    .getCustomImage(layoutAlgoImages.get(layoutAlgos.indexOf(la))));
            toolItemLayout.setToolTipText(layoutAlgoTooltip.get(layoutAlgos.indexOf(la)));
        });

        ToolBar toolBarActions = new ToolBar(compositeHeader, SWT.FLAT | SWT.RIGHT);
        ToolItem toolItemSave = new ToolItem(toolBarActions, SWT.PUSH);
        toolItemSave.setImage(ImageCache.getInstance().getImage(ImageCache.SAVE));
        toolItemSave.setToolTipText(Messages.BiaView_saveTooltip);
        toolItemSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(),
                        SWT.SAVE);
                fileDialog.setFilterExtensions(new String[] { "*.png" }); //$NON-NLS-1$
                fileDialog.setOverwrite(true);
                String open = fileDialog.open();
                if (open == null) {
                    return;
                }
                saveImage(open);
            }
        });
        ToolItem tltmLi = new ToolItem(toolBarActions, SWT.CHECK);
        tltmLi.setToolTipText(Messages.BiaView_linkWithEditorTooltip);
        tltmLi.setImage(ImageCache.getInstance().getImage(ImageCache.LINKED));

        tltmLi.setSelection(linkWithEditor.get());
        tltmLi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                linkWithEditor.set(!linkWithEditor.get());
            }
        });

        updateGraphColors();
        processRelationIds = linkColors.keySet();
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
            if (event.getProperty().startsWith("bia_view_")) { //$NON-NLS-1$
                updateGraphColors();
                graphViewer.refresh();
            }
        });

        graphViewer = new BasicGraphViewer(parent, SWT.NONE);
        graphViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        graphViewer.setContentProvider(new ZestNodeContentProvider());
        graphViewer.setLabelProvider(new ZestLabelProvider());
        graphViewer.setLayoutAlgorithm(layoutAlgos.get(selectedLayoutIndex), true);
        graphViewer.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent ev) {
                ((List<?>) graphViewer.getStructuredSelection().toList()).stream()
                        .forEach(e -> EditorFactory.getInstance().openEditor(e));
            }
        });
        Control control = graphViewer.getControl();
        GridData gdControl = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        control.setLayoutData(gdControl);

        getViewSite().getPage().addSelectionListener(this);
        getViewSite().getPage().addPartListener(linkWithEditorlistener);
        getViewSite().getActionBars().getMenuManager().add(new ZoomContributionViewItem(this));
    }

    @Override
    public void dispose() {
        getViewSite().getPage().removeSelectionListener(this);
        getViewSite().getPage().removePartListener(linkWithEditorlistener);
        super.dispose();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part == BiaView.this) {
            return;
        }
        if (!(selection instanceof IStructuredSelection)
                || ((IStructuredSelection) selection).isEmpty()
                || ((IStructuredSelection) selection).size() != 1) {
            return;
        }
        if (((IStructuredSelection) selection).getFirstElement() instanceof CnATreeElement) {
            CnATreeElement ce = (CnATreeElement) ((IStructuredSelection) selection)
                    .getFirstElement();
            setSelectedObjects(ce);
        }
    }

    @Override
    public void editorActivated(IEditorPart activeEditor) {
        if (!linkWithEditor.get())
            return;

        CnATreeElement element = BSIElementEditorInput.extractElement(activeEditor);
        if (!currentElements.contains(element)) {
            setSelectedObjects(element);
        }
    }

    @Override
    public AbstractZoomableViewer getZoomableViewer() {
        return graphViewer;
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.BIAVIEW;
    }

    @Override
    public String getViewId() {
        return ID;
    }

    private void setSelectedObjects(CnATreeElement selectedElement) {
        if (Set.of(selectedElement).equals(currentElements) || working.get())
            return;

        if (isProcess(selectedElement)) {
            viewMode = ViewMode.PROCESS;
        } else if (isElement(selectedElement)) {
            viewMode = ViewMode.RESOURCE;
        } else { // clear graph for all other objects
            graphViewer.setInput(Collections.emptyList());
            return;
        }
        working.set(true);

        if (!viewMode.availableModes.contains(displayMode)) {
            displayMode = viewMode.availableModes.get(0);
        }
        
        selectedDomain = CnATypeMapper.getDomainFromTypeId(selectedElement.getTypeId());
        graphViewer.setInput(Collections.emptyList());
        currentElements = Set.of(selectedElement);

        modeActions.stream().forEach(ti -> {
            ti.setEnabled(viewMode.availableModes.contains(ti.getData())
                    && isRpoEnabled((Displaymode) ti.getData()));
            ti.setSelection(displayMode.equals(ti.getData()));
            if (ti.getData() == Displaymode.MIN_MTPD && selectedDomain == Domain.ISM) {
                ti.setText(Messages.BiaView_ISM_Mtpd);
                ti.setToolTipText(Messages.bind(Messages.BiaView_viewModeTooltip, Messages.BiaView_ISM_Mtpd));
                ti.getParent().getParent().layout(true);
            } else if (ti.getData() == Displaymode.MIN_MTPD
                    && selectedDomain == Domain.BASE_PROTECTION) {
                ti.setText(Messages.BiaView_MOGS_Mtpd);
                ti.setToolTipText(Messages.bind(Messages.BiaView_viewModeTooltip, Messages.BiaView_MOGS_Mtpd));
                ti.getParent().getParent().layout(true);
            }
        });

        WorkspaceJob loadJob = new WorkspaceJob(Messages.BiaView_loadJobText) {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                Activator.inheritVeriniceContextState();
                try {
                    List<CnATreeElement> union = loadData(List.of(selectedElement));
                    if (graphViewer != null) {
                        Display.getDefault().asyncExec(() -> graphViewer.setInput(union));
                    }
                } catch (Exception e) {
                    return Status.error(e.getLocalizedMessage());
                } finally {
                    working.set(false);
                }
                return Status.OK_STATUS;
            }
        };

        loadJob.setUser(true);
        loadJob.schedule();
    }

    private void createLayoutAlgos() {
        int layoutStyles = LayoutStyles.ENFORCE_BOUNDS | LayoutStyles.NO_LAYOUT_NODE_RESIZING;
        LayoutAlgorithm tree = new TreeLayoutAlgorithm(layoutStyles);
        RadialLayoutAlgorithm radialLayoutAlgorithm = new RadialLayoutAlgorithm(layoutStyles);
        LayoutAlgorithm hoirzontal = new HorizontalTreeLayoutAlgorithm(layoutStyles);
        VerticalLayoutAlgorithm verticalLayout = new VerticalLayoutAlgorithm(layoutStyles);
        SpringLayoutAlgorithm springLayout = new SpringLayoutAlgorithm(layoutStyles);
        HorizontalShift horizontalShift = new HorizontalShift(layoutStyles);

        layoutAlgos = List.of(tree, hoirzontal, verticalLayout,radialLayoutAlgorithm, springLayout);
        layoutAlgoImages = List.of("tree-icons/Grey/Tree.png", "tree-icons/Grey/Size Horz.png", //$NON-NLS-1$ //$NON-NLS-2$
                "tree-icons/Grey/Size Vert.png", "tree-icons/Grey/Gear.png", //$NON-NLS-1$ //$NON-NLS-2$
                "tree-icons/Grey/Sitemap.png"); //$NON-NLS-1$
        layoutAlgoTooltip = List.of(Messages.BiaView_tree_Layout,
                Messages.BiaView_horizontal_layout, Messages.BiaView_vertical_layout,
                Messages.BiaView_radial_layout, Messages.BiaView_spring_layout);
    }

    private void updateGraphColors() {
        Color link1 = new Color(
                PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(),
                        PreferenceConstants.BIA_VIEW_LINK_COLOR_1));
        Color link2 = new Color(
                PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(),
                        PreferenceConstants.BIA_VIEW_LINK_COLOR_2));
        otherRelation = new Color(
                PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(),
                        PreferenceConstants.BIA_VIEW_LINK_COLOR_3));
        selected = new Color(
                PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(),
                        PreferenceConstants.BIA_VIEW_SELECTED_COLOR));

        linkColors.put("rel_bp_businessprocess_bp_businessprocess2", link1); //$NON-NLS-1$
        linkColors.put("rel_process_bp_process2", link1); //$NON-NLS-1$
        linkColors.put("rel_bp_businessprocess_bp_businessprocess3", link2); //$NON-NLS-1$
        linkColors.put("rel_bp_businessprocess_bp_businessprocess4", link2); //$NON-NLS-1$
        linkColors.put("rel_process_bp_process3", link2); //$NON-NLS-1$
        linkColors.put("rel_process_bp_process4", link2); //$NON-NLS-1$
    }

    private boolean isElementRelation(CnALink link) {
        return isProcess(link.getDependant()) || isProcess(link.getDependency());
    }

    private boolean isProcessRelation(CnALink link) {
        return processRelationIds.contains(link.getRelationId());
    }

    private boolean isProcessOrElementRelation(CnALink cnalink1) {
        return isElementRelation(cnalink1) || isProcessRelation(cnalink1);
    }

    private boolean isProcess(CnATreeElement ce) {
        return BusinessProcess.TYPE_ID.equals(ce.getObjectType())
                || Process.TYPE_ID.equals(ce.getObjectType());
    }

    private boolean isElement(CnATreeElement selectedElement) {
        String entityType = selectedElement.getEntity().getEntityType();
        return affectedElementTypes.contains(entityType);
    }

    private boolean isProcessOrElement(CnATreeElement ce) {
        return isProcess(ce) || isElement(ce);
    }

    private boolean isRpoEnabled(Displaymode dm) {
        if (viewMode == ViewMode.RESOURCE && dm == Displaymode.RPO) {
            return currentElements.stream().map(ce -> {
                BCMProperties bcmProperties = sernet.verinice.model.bp.BCMUtils
                        .getPropertiesForElement(ce);
                String propertyRpo = bcmProperties.propertyRpo;
                return !propertyValue(ce, propertyRpo).equals("-"); //$NON-NLS-1$
            }).reduce(true, (v, x) -> v && x);
        }
        return true;
    }

    private List<CnATreeElement> loadData(List<CnATreeElement> selectedProcesses) {
        switch (viewMode) {
        case PROCESS:
            return loadProcessData(selectedProcesses);
        case RESOURCE:
            return loadResourceData(selectedProcesses);
        default:
            return Collections.emptyList();
        }
    }

    private List<CnATreeElement> loadProcessData(List<CnATreeElement> selectedProcesses) {
        List<CnATreeElement> loadedProcesses = selectedProcesses.stream().distinct()
                .map(this::loadLinks).collect(Collectors.toList());

        List<CnATreeElement> linkedElements = loadedProcesses.stream()
                .flatMap(ce -> Stream.concat(
                        ce.getLinksDown().stream().filter(this::isProcessOrElementRelation)
                                .map(CnALink::getDependency),
                        ce.getLinksUp().stream().filter(this::isProcessOrElementRelation)
                                .map(CnALink::getDependant)))
                .filter(this::isProcessOrElement).distinct().map(this::loadLinks)
                .collect(Collectors.toList());

        List<CnATreeElement> union = new ArrayList<>(
                loadedProcesses.size() + linkedElements.size());
        union.addAll(loadedProcesses);
        union.addAll(linkedElements);
        return union;
    }

    private List<CnATreeElement> loadResourceData(List<CnATreeElement> selectedElement) {
        List<CnATreeElement> loadedElements = selectedElement.stream().distinct()
                .map(this::loadLinks).collect(Collectors.toList());

        List<CnATreeElement> linkedElements = loadedElements.stream()
                .flatMap(ce -> Stream.concat(
                        ce.getLinksDown().stream().filter(this::isElementRelation)
                                .map(CnALink::getDependency),
                        ce.getLinksUp().stream().filter(this::isElementRelation)
                                .map(CnALink::getDependant)))
                .distinct().map(this::loadLinks).collect(Collectors.toList());

        List<CnATreeElement> union = new ArrayList<>(loadedElements.size() + linkedElements.size());
        union.addAll(loadedElements);
        union.addAll(linkedElements);
        return union;
    }

    /**
     * Initialize the element by loading the links.
     */
    private CnATreeElement loadLinks(CnATreeElement elmt) {
        try {
            FindRelationsFor command = new FindRelationsFor(elmt);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            return command.getElmt();
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveImage(String file) {
        GC gc = new GC(graphViewer.getControl());
        Rectangle bounds = graphViewer.getControl().getBounds();
        Image image = new Image(graphViewer.getControl().getDisplay(), bounds);
        try {
            gc.copyArea(image, 0, 0);
            ImageLoader imageLoader = new ImageLoader();
            imageLoader.data = new ImageData[] { image.getImageData() };
            imageLoader.save(file, SWT.IMAGE_PNG);
        } finally {
            image.dispose();
            gc.dispose();
        }
    }

    private String propertyValue(CnATreeElement source, String propertyId) {
        PropertyType propertyType = HUITypeFactory.getInstance()
                .getPropertyType(source.getEntity().getEntityType(), propertyId);
        if (propertyType == null)
            return ""; //$NON-NLS-1$
        if (propertyType.isNumericSelect()) {
            Integer numeric = source.getEntity().getNumericValue(propertyId);
            return numeric != null ? propertyType.getNameForValue(numeric) : ""; //$NON-NLS-1$
        } else {
            return source.getEntity().getPropertyValue(propertyId);
        }
    }

}
