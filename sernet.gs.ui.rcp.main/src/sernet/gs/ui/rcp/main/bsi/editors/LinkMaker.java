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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.WorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.views.IRelationTable;
import sernet.gs.ui.rcp.main.bsi.views.RelationByNameSorter;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer.PathCellLabelProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewContentProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.DirectedHuiRelation;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
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

    private static final int RELATION_COMBO_WIDTH = 200;
    
    private static final int FORM_ATTACHMENT_NUMERATOR_DEFAULT = 100;
    private static final int FORM_ATTACHMENT_OFFSET_DEFAULT = 5;

    // SWT
    RelationTableViewer viewer;
    private WorkbenchPart part;
    private SelectionListener linkAction;
    private SelectionListener unlinkAction;
    private Action doubleClickAction;

    // SWT widgets
    Combo comboElementType;
    private Label label;
    private Button addLinkButton;
    private Button removeLinkButton;
    
    // JFaces
    final ComboViewer relationComboViewer = new ComboViewer(this, SWT.READ_ONLY);

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

    public LinkMaker(Composite parent, WorkbenchPart part) {
        super(parent, SWT.BORDER);
        FormLayout formLayout = new FormLayout();
        this.setLayout(formLayout);
        this.part = part;
    }

    public void createPartControl(Boolean isWriteAllowed) {

        this.writeable = isWriteAllowed;

        createLabel();
        createElementTypeCombo();
        createRelationCombo();
        createButtonAddLink();
        createButtonRemoveLink();
        initLinkTableViewer();

        // listeners to reload view:
        CnAElementFactory.getLoadedModel().addBSIModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(relationViewContentProvider);
        // listeners to remove stale links from currently open object in editor
        // to prevent conflicts when saving:
        linkRemover = new LinkRemover(this);
        CnAElementFactory.getLoadedModel().addBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(linkRemover);

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.RECREATE);
        List<PathCellLabelProvider> cellLabelProviders = viewer.initToolTips(relationViewLabelProvider, this);
        // register resize listener for cutting the tooltips
        addResizeListener(cellLabelProviders);

        linkAction = new CreateLinkSelectionListener(this);
        unlinkAction = new RemoveLinkSelectionListener(this);
        this.addLinkButton.addSelectionListener(linkAction);
        this.removeLinkButton.addSelectionListener(unlinkAction);
        
        createFilter();

        createDoubleClickAction();
        hookDoubleClickAction();
    }

    private void createLabel() {
        label = new Label(this, SWT.NULL);
        label.setText(Messages.LinkMaker_0);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, FORM_ATTACHMENT_OFFSET_DEFAULT);
        formData.left = new FormAttachment(0, FORM_ATTACHMENT_OFFSET_DEFAULT);
        label.setLayoutData(formData);
        label.pack();
    }

    private void createElementTypeCombo() {
        comboElementType = new Combo(this, SWT.READ_ONLY);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, FORM_ATTACHMENT_OFFSET_DEFAULT);
        formData.left = new FormAttachment(label, FORM_ATTACHMENT_OFFSET_DEFAULT);
        comboElementType.setLayoutData(formData);
    }

    private void createRelationCombo() {

        FormData formData = new FormData();
        formData.top = new FormAttachment(0, FORM_ATTACHMENT_OFFSET_DEFAULT);
        formData.left = new FormAttachment(comboElementType, FORM_ATTACHMENT_OFFSET_DEFAULT);
        formData.width = RELATION_COMBO_WIDTH;
        relationComboViewer.getCombo().setLayoutData(formData);
        
        relationComboViewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                ArrayList<DirectedHuiRelation> relations = new ArrayList<>();
                if (inputElement instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<DirectedHuiRelation> input = (Set<DirectedHuiRelation>) inputElement;
                    relations.addAll(input);
                    return relations.toArray();
                }

                return relations.toArray();
            }
        });
                
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

        addRelationComboListener();
    }

    private void createButtonAddLink() {
        addLinkButton = new Button(this, SWT.PUSH);
        FormData formData = new FormData();
        formData.top = new FormAttachment(comboElementType, 0, SWT.CENTER);
        formData.left = new FormAttachment(relationComboViewer.getCombo(),
                FORM_ATTACHMENT_OFFSET_DEFAULT);
        addLinkButton.setLayoutData(formData);
        addLinkButton.setText(Messages.LinkMaker_1);
        addLinkButton.setToolTipText(Messages.LinkMaker_2);
        addLinkButton.setEnabled(false);
    }

    private void createButtonRemoveLink() {
        removeLinkButton = new Button(this, SWT.PUSH);
        FormData formData = new FormData();
        formData.top = new FormAttachment(comboElementType, 0, SWT.CENTER);
        formData.left = new FormAttachment(addLinkButton, FORM_ATTACHMENT_OFFSET_DEFAULT);
        removeLinkButton.setLayoutData(formData);
        removeLinkButton.setText(Messages.LinkMaker_3);
        removeLinkButton.setEnabled(writeable && checkRights());
        removeLinkButton.setToolTipText(Messages.LinkMaker_4);
    }

    private void initLinkTableViewer() {
        viewer = new RelationTableViewer(this, this, SWT.FULL_SELECTION | SWT.MULTI, true);
        FormData formData = new FormData();
        formData.top = new FormAttachment(addLinkButton, 2);
        formData.left = new FormAttachment(0, 1);
        formData.right = new FormAttachment(FORM_ATTACHMENT_NUMERATOR_DEFAULT, -1);
        formData.bottom = new FormAttachment(FORM_ATTACHMENT_NUMERATOR_DEFAULT, -1);
        viewer.getTable().setLayoutData(formData);
        viewer.getTable().setEnabled(writeable);
        relationViewContentProvider = new RelationViewContentProvider(this, viewer);
        viewer.setContentProvider(relationViewContentProvider);

        relationViewLabelProvider = new RelationViewLabelProvider(this);
        viewer.setLabelProvider(relationViewLabelProvider);
        viewer.setSorter(new RelationByNameSorter(this, IRelationTable.COLUMN_TITLE, IRelationTable.COLUMN_TYPE_IMG));

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

        Set<HuiRelation> forwardRelations = huiTypeFactory.getPossibleRelations(sourceEntityTypeId,
                targetEntityTypeId);
        Set<HuiRelation> backwardRelations = new HashSet<>();
        if (!sourceEntityTypeId.equals(targetEntityTypeId)) {
            backwardRelations = huiTypeFactory.getPossibleRelations(targetEntityTypeId,
                    sourceEntityTypeId);
        }
        Set<DirectedHuiRelation> relations = collateRelations(forwardRelations, backwardRelations);
        relationComboViewer.setInput(relations);

        int relationItemCount = relationComboViewer.getCombo().getItemCount();
        if (relationItemCount <= 1) {
            relationComboViewer.getCombo().setEnabled(false);
        } else {
            relationComboViewer.getCombo().setEnabled(true);
        }
        addLinkButton.setEnabled(relationItemCount > 0 && writeable && checkRights());

        selectRelationType();
    }

    private Set<DirectedHuiRelation> collateRelations(Set<HuiRelation> forwardRelations,
            Set<HuiRelation> backwardRelations) {

        Set<DirectedHuiRelation> collatedRelations = new TreeSet<>(
                getDirectedHuiRelationComparator());

        for (HuiRelation forwardRelation : forwardRelations) {
            collatedRelations
                    .add(DirectedHuiRelation.getDirectedHuiRelation(forwardRelation, true));
        }
        for (HuiRelation backwardRelation : backwardRelations) {
            collatedRelations
                    .add(DirectedHuiRelation.getDirectedHuiRelation(backwardRelation, false));
        }

        return collatedRelations;
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

    private Comparator<DirectedHuiRelation> getDirectedHuiRelationComparator() {
        return new Comparator<DirectedHuiRelation>() {
            Collator collator = Collator.getInstance(Locale.getDefault());

            @Override
            public int compare(DirectedHuiRelation relation1, DirectedHuiRelation relation2) {
                return collator.compare(relation1.getLabel(), relation2.getLabel());
            }
        };
    }

    private void addRelationComboListener() {
        relationComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
 
                StructuredSelection selection = (StructuredSelection) relationComboViewer
                        .getSelection();
                DirectedHuiRelation selectedRelation = (DirectedHuiRelation) selection
                        .getFirstElement();
                if (selectedRelation != null) {
                    addLinkButton.setEnabled(true);
                } else {
                    addLinkButton.setEnabled(false);
                }
            }
        });
    }

    private void initNamesForElementTypeCombo() {
        elementTypeNamesAndIds = new TreeMap<String, String>();

        if (allPossibleRelations == null) {
            elementTypeNames = new String[0];
        } else {
            for (HuiRelation huiRelation : allPossibleRelations) {
                // from or to element, show other side respectively:
                String targetEntityTypeID = huiRelation.getTo();
                String sourceEntityTypeID = huiRelation.getFrom();
                String targetEntityTypeName = huiTypeFactory.getEntityType(targetEntityTypeID).getName();
                String sourceEntityTypeName = huiTypeFactory.getEntityType(sourceEntityTypeID).getName();
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
        allPossibleRelations = new ArrayList<HuiRelation>();
        String entityTypeID = inputElmt.getEntity().getEntityType();

        allPossibleRelations.addAll(huiTypeFactory.getPossibleRelationsFrom(entityTypeID));
        allPossibleRelations.addAll(huiTypeFactory.getPossibleRelationsTo(entityTypeID));
    }

    private void createDoubleClickAction() {
        doubleClickAction = new Action() {

            @Override
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                CnALink link = (CnALink) obj;

                // open the object on the other side of the link:
                if (CnALink.isDownwardLink(getInputElmt(), link)) {
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependency());
                } else {
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependant());
                }
            }
        };
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
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
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
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
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getBpModel().removeBpModelListener(relationViewContentProvider);

        CnAElementFactory.getLoadedModel().removeBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(linkRemover);
        CnAElementFactory.getInstance().getBpModel().removeBpModelListener(linkRemover);

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
            RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
            return service.isEnabled(getRightID());
        }
    }
}
