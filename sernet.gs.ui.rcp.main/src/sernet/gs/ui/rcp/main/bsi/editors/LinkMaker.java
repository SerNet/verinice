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
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.bsi.views.IRelationTable;
import sernet.gs.ui.rcp.main.bsi.views.RelationByNameSorter;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer.RelationTableCellLabelProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewContentProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRelationsFor;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelObject;
import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A SWT composite that allow the user to create links (relations) to other
 * objects, display the existing links, change or delete them, jump between
 * linked items in the editor area etc.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
@SuppressWarnings("restriction")
public class LinkMaker extends Composite implements IRelationTable {

    private static final Logger LOG = Logger.getLogger(LinkMaker.class);
    
    private static final int COMBO_RELATION_TYPE_WIDTH = 200;
    
    private final int formAttachmentNumeratorDefault = 100;
    private final int formAttachmentOffsetDefault = 5;
    
    private final static String LAST_SELECTED_LINK_TYPE_PREF_PREFIX = "last_selected_link_type_for_";

    // SWT
    private WorkbenchPart part;
    private RelationTableViewer viewer;
    private SelectionListener linkAction;
    private SelectionListener unlinkAction;
    private Action doubleClickAction;
    
    // SWT widgets
    private Label label;
    private Combo comboElementType;
    private Combo comboLinkType;
    private Button buttonAddLink;
    private Button buttonRemoveLink;
    
    // Utilities 
    private static HUITypeFactory huiTypeFactory = HitroUtil.getInstance().getTypeFactory();
    private static IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
    private EntityTypeFilter elementTypeFilter;
    private LinkRemover linkRemover;
    private RelationViewContentProvider relationViewContentProvider;
    private RelationViewLabelProvider relationViewLabelProvider;
    private ComboModel<HuiRelation> comboModelLinkType;
   
    private CnATreeElement inputElmt;
    private List<HuiRelation> allPossibleRelations;    
    private SortedMap<String, String> elementTypeNamesAndIds;
    private String[] elementTypeNames;
    private String selectedInComboElementTypeId;  
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
        createComboElementType();    
        createComboLinkType();
        createButtonAddLink();
        createButtonRemoveLink();
        initLinkTableViewer();

        // listeners to reload view:
        CnAElementFactory.getLoadedModel().addBSIModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(relationViewContentProvider);
        // listeners to remove stale links from currently open object in editor to prevent conflicts when saving:
        linkRemover = new LinkRemover(this);
        CnAElementFactory.getLoadedModel().addBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(linkRemover);

        // init tooltip provider
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.RECREATE);
        List<RelationTableCellLabelProvider> cellLabelProviders = viewer.initToolTips(relationViewLabelProvider, this);
        // register resize listener for cutting the tooltips
        addResizeListener(cellLabelProviders);

        createButtonListeners();
        hookButtonListeners();
  
        createFilter();    
        
        addComboLinkTypeListener();
        
        createDoubleClickAction();
        hookDoubleClickAction();
    }

    private void createLabel() {
        label = new Label(this, SWT.NULL);
        label.setText(Messages.LinkMaker_0);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, formAttachmentOffsetDefault);
        formData.left = new FormAttachment(0, formAttachmentOffsetDefault);
        label.setLayoutData(formData);
        label.pack();
    }

    private void createComboElementType() {
        comboElementType = new Combo(this, SWT.READ_ONLY);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, formAttachmentOffsetDefault);
        formData.left = new FormAttachment(label, formAttachmentOffsetDefault);
        comboElementType.setLayoutData(formData);
    }
    
    private void createComboLinkType() {
        comboLinkType = new Combo(this, SWT.READ_ONLY);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, formAttachmentOffsetDefault);
        formData.left = new FormAttachment(comboElementType, formAttachmentOffsetDefault);
        formData.width = COMBO_RELATION_TYPE_WIDTH;
        comboLinkType.setLayoutData(formData);
    }
    
    private void createButtonAddLink() {
        buttonAddLink = new Button(this, SWT.PUSH);
        FormData formData = new FormData();
        formData.top = new FormAttachment(comboElementType, 0, SWT.CENTER);
        formData.left = new FormAttachment(comboLinkType, formAttachmentOffsetDefault);
        buttonAddLink.setLayoutData(formData);
        buttonAddLink.setText(Messages.LinkMaker_1);
        buttonAddLink.setToolTipText(Messages.LinkMaker_2);
        buttonAddLink.setEnabled(false);
    }
    
    private void createButtonRemoveLink() {
        buttonRemoveLink = new Button(this, SWT.PUSH);
        FormData formData = new FormData();
        formData.top = new FormAttachment(comboElementType, 0, SWT.CENTER);
        formData.left = new FormAttachment(buttonAddLink, formAttachmentOffsetDefault);
        buttonRemoveLink.setLayoutData(formData);
        buttonRemoveLink.setText(Messages.LinkMaker_3);
        buttonRemoveLink.setEnabled(writeable && checkRights());
        buttonRemoveLink.setToolTipText(Messages.LinkMaker_4);
    }
    
    private void initLinkTableViewer() {
        viewer = new RelationTableViewer(this, this, SWT.FULL_SELECTION | SWT.MULTI, true);
        FormData formData = new FormData();
        formData.top = new FormAttachment(buttonAddLink, 2);
        formData.left = new FormAttachment(0, 1);
        formData.right = new FormAttachment(formAttachmentNumeratorDefault, -1);
        formData.bottom = new FormAttachment(formAttachmentNumeratorDefault, -1);
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
     * Tracks changes of viewpart size and delegates them to the tooltip provider.
     */
    private void addResizeListener(final List<RelationTableCellLabelProvider> cellLabelProviders) {
        addControlListener(new ControlAdapter() {
        
            @Override
            public void controlResized(ControlEvent e) {
                for (RelationTableCellLabelProvider c : cellLabelProviders) {
                    c.updateShellWidthAndX(getShell().getBounds().width, getShell().getBounds().x);
                }
            }
        });
    }

    private void createFilter() {      
        elementTypeFilter = new EntityTypeFilter(viewer);
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
            elementTypeFilter.setEntityType(null);
            buttonAddLink.setEnabled(false);
            comboLinkType.setEnabled(false);
            return;
        }
        
        Object[] array = elementTypeNamesAndIds.entrySet().toArray();
        
        @SuppressWarnings("unchecked")
        Entry<String, String> entry = (Entry<String, String>) array[selectionIndex];
        selectedInComboElementTypeId = entry.getValue();
        elementTypeFilter.setEntityType(selectedInComboElementTypeId);

        // enable link button if we have write permissions:
        buttonAddLink.setEnabled(writeable && checkRights());
        
        fillComboLinkType();
    }

    private void fillComboLinkType() {
        comboModelLinkType = getComboModelLinkType();
        Comparator<ComboModelObject<HuiRelation>> huiRelationComparator = getHuiRelationComparator();        
        
        String elementTypeInEditor = inputElmt.getEntityType().getId();
        comboModelLinkType.addAll(huiTypeFactory.getPossibleRelations(elementTypeInEditor,
                            selectedInComboElementTypeId));
        comboModelLinkType.addAll(huiTypeFactory.getPossibleRelations(selectedInComboElementTypeId,
                        elementTypeInEditor));
        comboModelLinkType.sort(huiRelationComparator);
               
        comboLinkType.setItems(comboModelLinkType.getLabelArray());
        selectComboLinkTypeItem();
    
        if (comboModelLinkType.size() > 1) {
            comboLinkType.setEnabled(true);
        } else {
            comboLinkType.setEnabled(false);
        }
    }

    private void selectComboLinkTypeItem() {
        String prefStoreKey = LAST_SELECTED_LINK_TYPE_PREF_PREFIX + selectedInComboElementTypeId;
        if (prefStore.contains(prefStoreKey)) {
            String relationTypeId = prefStore.getString(prefStoreKey);
            HuiRelation huiRelation = huiTypeFactory.getRelation(relationTypeId);
            comboModelLinkType.setSelectedObject(huiRelation);
        } else {
            comboModelLinkType.setSelectedIndex(0);
        }
        comboLinkType.select(comboModelLinkType.getSelectedIndex());
    }

    private ComboModel<HuiRelation> getComboModelLinkType() {
        return new ComboModel<HuiRelation>(new IComboModelLabelProvider<HuiRelation>() {
            @Override
            public String getLabel(HuiRelation element) {
                return element.getName();
            }
        });
    }

    private Comparator<ComboModelObject<HuiRelation>> getHuiRelationComparator() {
        return new Comparator<ComboModelObject<HuiRelation>>() {
            Collator collator = Collator.getInstance(Locale.getDefault());
            
            @Override
            public int compare(
                            ComboModelObject<HuiRelation> comboModelObject1,
                            ComboModelObject<HuiRelation> comboModelObject2) {
                String comboModelObject1Name = comboModelObject1.getObject().getName();
                String comboModelObject2Name = comboModelObject2.getObject().getName();
                return collator.compare(comboModelObject1Name, comboModelObject2Name);
            }
        };
    }
    
    private void addComboLinkTypeListener() {
        comboLinkType.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelLinkType.setSelectedIndex(comboLinkType.getSelectionIndex());
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    private void createButtonListeners() {
        linkAction = new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                // create new link to object
                Object[] array = elementTypeNamesAndIds.entrySet().toArray();
                @SuppressWarnings("unchecked")
                String selectedElementType = ((Entry<String, String>) array[comboElementType.getSelectionIndex()]).getValue();
                CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(
                                viewer.getControl().getShell(), selectedElementType, inputElmt);
                if (dialog.open() != Window.OK) {
                    return;
                }
                List<CnATreeElement> linkTargets = dialog.getSelectedElements();
                
                String selectedLinkType = comboModelLinkType.getSelectedObject().getId();                                
                prefStore.putValue(LAST_SELECTED_LINK_TYPE_PREF_PREFIX + selectedElementType, selectedLinkType);
                                
                // this method also fires events for added links:
                CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(
                                getInputElmt(), linkTargets, selectedLinkType);
                // refresh viewer because it doesn't listen to events:
                reloadLinks();
            }
        };

        unlinkAction = new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                // delete link:
                if (viewer.getSelection().isEmpty()) {
                    return;
                }
                List<?> selection = ((IStructuredSelection) viewer.getSelection()).toList();
                String msg = NLS.bind(Messages.LinkMaker_6, selection.size());
                boolean confirm = MessageDialog.openConfirm(getShell(), Messages.LinkMaker_5, msg);
                if (!confirm) {
                    return;
                }
                CnALink link = null;
                for (Object object : selection) {
                    link = (CnALink) object;
                    try {
                        CnAElementHome.getInstance().remove(link);
                        inputElmt.removeLinkDown(link);
                    } catch (Exception e1) {
                        LOG.error("Error while removing link", e1);
                        ExceptionUtil.log(e1, Messages.LinkMaker_7);
                    }
                }
                // calling linkRemoved for one link reloads all changed links
                if (link != null) {
                    // notify local listeners:
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getLoadedModel().linkRemoved(link);
                    }
                    CnAElementFactory.getInstance().getISO27kModel().linkRemoved(link);
                }
            }
        };
    }
    
    private void hookButtonListeners() {
        this.buttonAddLink.addSelectionListener(linkAction);
        this.buttonRemoveLink.addSelectionListener(unlinkAction);
    }

    private void initNamesForComboElementType() {
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
        initNamesForComboElementType();
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

    private void reloadLinks() {
        if (!CnAElementHome.getInstance().isOpen() || inputElmt == null) {
            return;
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                viewer.setInput(new PlaceHolder(Messages.LinkMaker_9));
            }
        });

        WorkspaceJob job = new WorkspaceJob(Messages.LinkMaker_10) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();

                try {
                    monitor.setTaskName(Messages.LinkMaker_11);

                    FindRelationsFor command = new FindRelationsFor(inputElmt);
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
                            viewer.setInput(new PlaceHolder(Messages.LinkMaker_12));
                        }
                    });
                    LOG.error("Error while searching relations", e);
                    ExceptionUtil.log(e, Messages.LinkMaker_13);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();

    }
    
    @SuppressWarnings("static-access")
    @Override
    public void dispose() {
        CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(relationViewContentProvider);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(relationViewContentProvider);
        
        CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(linkRemover);
                
        super.dispose();
    }
    
    private String getRightID() {
        return ActionRightIDs.EDITLINKS;
    }
    
    private boolean checkRights() {
        // no right management should be used
        if (getRightID() == null) {
            return true;
        }
        // id set but empty, right not granted, action disabled
        else if (getRightID().equals("")) {
            return false;
            // right management enabled, check rights and return true if right enabled / false if not
        } else {
            Activator.inheritVeriniceContextState();
            RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
            return service.isEnabled(getRightID());
        }
    }
}
