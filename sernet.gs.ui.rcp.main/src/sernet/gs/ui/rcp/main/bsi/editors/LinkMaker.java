/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Moritz Reiter - enhancement and refactoring
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.WorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.views.IRelationTable;
import sernet.gs.ui.rcp.main.bsi.views.RelationComparator;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer.PathCellLabelProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewContentProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.DirectedHuiRelation;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.HuiRelationUtil;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A SWT composite that allow the user to create links (relations) to other
 * objects, display the existing links, change or delete them, jump between
 * linked items in the editor area etc.
 * 
 * @author koderman[at]sernet[dot]de
 */
public class LinkMaker extends Composite implements IRelationTable {

    static final String LAST_SELECTED_RELATION_PREF_PREFIX = "last_selected_relation_for";

    private static final int COMPOSITE_WIDTH_HINT = 400;
    // SWT
    RelationTableViewer viewer;
    private WorkbenchPart part;

    // SWT widgets
    Combo comboElementType;
    private Button addLinkButton;
    private Button removeLinkButton;

    // JFaces
    final ComboViewer relationComboViewer;

    // Utilities
    static IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
    private static HUITypeFactory huiTypeFactory = HitroUtil.getInstance().getTypeFactory();
    private EntityTypeFilter entityTypeFilter;
    private LinkRemover linkRemover;
    private RelationViewContentProvider relationViewContentProvider;
    private RelationViewLabelProvider relationViewLabelProvider;

    CnATreeElement inputElmt;
    SortedMap<String, String> elementTypeNamesAndIds;
    String selectedElementTypeId;
    private List<HuiRelation> allPossibleRelations;
    private String[] elementTypeNames;
    //
    private int oldSelection = -1;
    private boolean writeable;
    private IPropertyChangeListener proceedingFilterDisabledToggleListener;
    private Composite composite;

    public LinkMaker(Composite parent, WorkbenchPart part) {
        super(parent, SWT.BORDER);
        this.setLayout(new GridLayout());
        this.part = part;

        composite = new Composite(this, SWT.NONE);

        GridData gdComposite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        gdComposite.widthHint = COMPOSITE_WIDTH_HINT;
        gdComposite.verticalIndent = 1;
        gdComposite.minimumWidth = COMPOSITE_WIDTH_HINT;
        composite.setLayoutData(gdComposite);
        composite.setLayout(new GridLayout(5, false));

        Label label = new Label(composite, SWT.NULL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        label.setText(Messages.LinkMaker_0);

        comboElementType = new Combo(composite, SWT.READ_ONLY);
        comboElementType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        relationComboViewer = new ComboViewer(composite, SWT.READ_ONLY);
        Combo combo = relationComboViewer.getCombo();
        GridData gdCombo1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        combo.setLayoutData(gdCombo1);

        relationComboViewer.setContentProvider(new ArrayContentProvider());

        relationComboViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof DirectedHuiRelation) {
                    DirectedHuiRelation relation = (DirectedHuiRelation) element;
                    return relation.getLabel();
                } else {
                    return "unknown object type";
                }
            }

        });
        combo.setEnabled(false);

        // add a "fake" element to set an initial/minimum width for the combo
        relationComboViewer.setInput(Collections.singleton(""));

        addRelationComboListener();

        addLinkButton = new Button(composite, SWT.PUSH);

        addLinkButton.setText(Messages.LinkMaker_1);
        addLinkButton.setToolTipText(Messages.LinkMaker_2);
        addLinkButton.setEnabled(false);

        removeLinkButton = new Button(composite, SWT.PUSH);

        removeLinkButton.setText(Messages.LinkMaker_3);
        removeLinkButton.setToolTipText(Messages.LinkMaker_4);
    }

    public void createPartControl(Boolean isWriteAllowed, boolean showRiskColumns) {

        this.writeable = isWriteAllowed;

        removeLinkButton.setEnabled(writeable && checkRights());
        initLinkTableViewer(showRiskColumns);

        // listeners to reload view:
        CnAElementFactory.getLoadedModel().addBSIModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getISO27kModel()
                .addISO27KModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getBpModel()
                .addModITBOModelListener(relationViewContentProvider);
        proceedingFilterDisabledToggleListener = event -> {
            if (PreferenceConstants.FILTER_INFORMATION_NETWORKS_BY_PROCEEDING
                    .equals(event.getProperty())) {
                viewer.refresh();
            }
        };
        Activator.getDefault().getPreferenceStore()
                .addPropertyChangeListener(proceedingFilterDisabledToggleListener);

        // listeners to remove stale links from currently open object in editor
        // to prevent conflicts when saving:
        linkRemover = new LinkRemover(this);
        CnAElementFactory.getLoadedModel().addBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(linkRemover);
        CnAElementFactory.getInstance().getBpModel().addModITBOModelListener(linkRemover);

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.RECREATE);
        List<PathCellLabelProvider> cellLabelProviders = viewer
                .initToolTips(relationViewLabelProvider, this);
        // register resize listener for cutting the tooltips
        addResizeListener(cellLabelProviders);

        SelectionListener linkAction = new CreateLinkSelectionListener(this);
        SelectionListener unlinkAction = new RemoveLinkSelectionListener(this);
        this.addLinkButton.addSelectionListener(linkAction);
        this.removeLinkButton.addSelectionListener(unlinkAction);

        createFilter();
        hookDoubleClickAction();
    }

    private void initLinkTableViewer(boolean showRiskColumns) {
        viewer = new RelationTableViewer(this, this, SWT.FULL_SELECTION | SWT.MULTI,
                showRiskColumns);
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        viewer.getTable().setEnabled(writeable);
        relationViewContentProvider = new RelationViewContentProvider(this, viewer);
        viewer.setContentProvider(relationViewContentProvider);

        relationViewLabelProvider = new RelationViewLabelProvider(this);
        viewer.setLabelProvider(relationViewLabelProvider);
        viewer.setComparator(new RelationComparator(IRelationTable.COLUMN_TITLE,
                IRelationTable.COLUMN_TYPE_IMG));

        part.getSite().setSelectionProvider(viewer);
    }

    /**
     * Tracks changes of viewpart size and delegates them to the tooltip
     * provider.
     */
    private void addResizeListener(final List<PathCellLabelProvider> cellLabelProviders) {
        addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                for (PathCellLabelProvider c : cellLabelProviders) {
                    c.updateShellWidthAndX(getShell().getBounds().width, getShell().getBounds().x);
                }
            }
        });
    }

    private void createFilter() {
        entityTypeFilter = new EntityTypeFilter(viewer);
        comboElementType.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                oldSelection = comboElementType.getSelectionIndex();
                setFilter(comboElementType.getSelectionIndex());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    protected void setFilter(int selectionIndex) {
        if (selectionIndex == 0) {
            entityTypeFilter.setEntityType(null);
            addLinkButton.setEnabled(false);
            relationComboViewer.getCombo().setEnabled(false);
            return;
        }

        Object[] array = elementTypeNamesAndIds.entrySet().toArray();

        @SuppressWarnings("unchecked")
        Entry<String, String> entry = (Entry<String, String>) array[selectionIndex];
        selectedElementTypeId = entry.getValue();
        entityTypeFilter.setEntityType(selectedElementTypeId);

        // enable link button if we have write permissions:
        addLinkButton.setEnabled(writeable && checkRights());

        fillRelationCombo();
    }

    private void fillRelationCombo() {
        String sourceEntityTypeId = inputElmt.getEntityType().getId();
        String targetEntityTypeId = selectedElementTypeId;

        Set<DirectedHuiRelation> relations = HuiRelationUtil
                .getAllRelationsBothDirections(sourceEntityTypeId, targetEntityTypeId);
        relationComboViewer.setInput(relations);

        int relationItemCount = relationComboViewer.getCombo().getItemCount();
        if (relationItemCount <= 1) {
            relationComboViewer.getCombo().setEnabled(false);
        } else {
            relationComboViewer.getCombo().setEnabled(true);
        }
        addLinkButton.setEnabled(relationItemCount > 0 && writeable && checkRights());

        selectRelationType();
        composite.pack(true);
    }

    private void selectRelationType() {
        Object firstElementInRelationCombo = relationComboViewer.getElementAt(0);
        relationComboViewer.setSelection(new StructuredSelection(firstElementInRelationCombo));

        String prefStoreKey = LAST_SELECTED_RELATION_PREF_PREFIX + inputElmt.getTypeId()
                + selectedElementTypeId;

        if (prefStore.contains(prefStoreKey)) {
            String relationId = prefStore.getString(prefStoreKey);
            Object comboItems = relationComboViewer.getInput();

            if (comboItems instanceof Set) {
                for (Object item : (Set<?>) comboItems) {
                    setSelection(item, relationId);
                }
            }
        }
    }

    private void setSelection(Object item, String relationId) {
        if (item instanceof DirectedHuiRelation) {
            DirectedHuiRelation relation = (DirectedHuiRelation) item;
            if (relation.getHuiRelation().getId().equals(relationId)) {
                relationComboViewer.setSelection(new StructuredSelection(relation));
            }
        }
    }

    private void addRelationComboListener() {
        relationComboViewer.addSelectionChangedListener(event -> {

            StructuredSelection selection = (StructuredSelection) relationComboViewer
                    .getSelection();
            DirectedHuiRelation selectedRelation = (DirectedHuiRelation) selection
                    .getFirstElement();
            addLinkButton.setEnabled(writeable && selectedRelation != null);
        });
    }

    private void initNamesForElementTypeCombo() {
        elementTypeNamesAndIds = new TreeMap<>();

        if (allPossibleRelations == null) {
            elementTypeNames = new String[0];
        } else {
            for (HuiRelation huiRelation : allPossibleRelations) {
                // from or to element, show other side respectively:
                String targetEntityTypeID = huiRelation.getTo();
                String sourceEntityTypeID = huiRelation.getFrom();
                String targetEntityTypeName = huiTypeFactory.getEntityType(targetEntityTypeID)
                        .getName();
                String sourceEntityTypeName = huiTypeFactory.getEntityType(sourceEntityTypeID)
                        .getName();
                if (sourceEntityTypeID.equals(inputElmt.getEntity().getEntityType())) {
                    elementTypeNamesAndIds.put(targetEntityTypeName, targetEntityTypeID);
                } else {
                    elementTypeNamesAndIds.put(sourceEntityTypeName, sourceEntityTypeID);
                }
            }
        }

        elementTypeNamesAndIds.put(Messages.LinkMaker_8, null);

        Set<String> namesSet = elementTypeNamesAndIds.keySet();
        elementTypeNames = new String[namesSet.size()];
        elementTypeNames = namesSet.toArray(new String[namesSet.size()]);
    }

    private void fillPossibleLinkLists() {
        allPossibleRelations = new ArrayList<>();
        String entityTypeID = inputElmt.getEntity().getEntityType();

        allPossibleRelations.addAll(huiTypeFactory.getPossibleRelationsFrom(entityTypeID));
        allPossibleRelations.addAll(huiTypeFactory.getPossibleRelationsTo(entityTypeID));
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(event -> {
            ISelection selection = event.getViewer().getSelection();
            if (!selection.isEmpty()) {
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                CnALink link = (CnALink) obj;

                // open the object on the other side of the link:
                if (CnALink.isDownwardLink(getInputElmt(), link)) {
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependency());
                } else {
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependant());
                }
            }
        });
    }

    @Override
    public CnATreeElement getInputElmt() {
        return inputElmt;
    }

    @Override
    public void reload(CnALink oldLink, CnALink newLink) {
        if (newLink != null) {
            newLink.setDependant(oldLink.getDependant());
            newLink.setDependency(oldLink.getDependency());
        }
        boolean removedLinkDown = inputElmt.removeLinkDown(oldLink);
        boolean removedLinkUp = inputElmt.removeLinkUp(oldLink);
        if (removedLinkUp) {
            inputElmt.addLinkUp(newLink);
        }
        if (removedLinkDown) {
            inputElmt.addLinkDown(newLink);
        }
        viewer.refresh();
    }

    @Override
    public void setInputElmt(CnATreeElement inputElmt) {
        if (inputElmt == null || this.inputElmt == inputElmt) {
            return;
        }
        if (oldSelection == -1) {
            oldSelection = comboElementType.getSelectionIndex();
        }
        this.inputElmt = inputElmt;
        fillPossibleLinkLists();
        initNamesForElementTypeCombo();
        comboElementType.setItems(elementTypeNames);
        if (oldSelection != -1 && oldSelection < comboElementType.getItemCount()) {
            comboElementType.select(oldSelection);
            setFilter(comboElementType.getSelectionIndex());
        }
        viewer.setInput(inputElmt);
    }

    @Override
    public void reloadAll() {
        reloadLinks();
    }

    void reloadLinks() {
        if (!CnAElementHome.getInstance().isOpen() || inputElmt == null) {
            return;
        }

        Display.getDefault().asyncExec(() -> {
            if (!viewer.getControl().isDisposed()) {
                viewer.setInput(new PlaceHolder(Messages.LinkMaker_9));
            }
        });

        WorkspaceJob job = new ReloadLinksWorkspaceJob(inputElmt, viewer, Messages.LinkMaker_10);

        job.setUser(false);
        job.schedule();

    }

    @Override
    public void dispose() {
        CnAElementFactory.getLoadedModel().removeBSIModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getISO27kModel()
                .removeISO27KModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getBpModel()
                .removeBpModelListener(relationViewContentProvider);

        CnAElementFactory.getLoadedModel().removeBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(linkRemover);
        CnAElementFactory.getInstance().getBpModel().removeBpModelListener(linkRemover);
        Activator.getDefault().getPreferenceStore()
                .removePropertyChangeListener(proceedingFilterDisabledToggleListener);

        super.dispose();
    }

    private static String getRightID() {
        return ActionRightIDs.EDITLINKS;
    }

    private static boolean checkRights() {
        // no right management should be used
        if (getRightID() == null) {
            return true;
        }
        // id set but empty, right not granted, action disabled
        else if (("").equals(getRightID())) {
            return false;
            // right management enabled, check rights and return true if right
            // enabled / false if not
        } else {
            Activator.inheritVeriniceContextState();
            RightsServiceClient service = (RightsServiceClient) VeriniceContext
                    .get(VeriniceContext.RIGHTS_SERVICE);
            return service.isEnabled(getRightID());
        }
    }
}
