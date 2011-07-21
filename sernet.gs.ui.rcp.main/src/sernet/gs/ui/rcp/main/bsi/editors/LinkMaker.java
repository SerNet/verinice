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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.bsi.views.IRelationTable;
import sernet.gs.ui.rcp.main.bsi.views.RelationByNameSorter;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewContentProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRelationsFor;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;

/**
 * A SWT composite that allow the user to create links (relations) to other
 * objects, display the existing links, change or delete them, jump between
 * linked items in the editor area etc.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
// TODO akoderman this view should also allow the user to create new elements
// and link them to the current element with one button
// i.e. for server select "Add&Link" to create a new person and link it as
// "owner" immediately.
public class LinkMaker extends Composite implements IRelationTable {

    private static final Logger LOG = Logger.getLogger(LinkMaker.class);
    
    private CnATreeElement inputElmt;
    private boolean writeable;
    private List<HuiRelation> allPossibleRelations;
    private RelationTableViewer viewer;
    private Combo combo;
    private Action doubleClickAction;
    private RelationViewContentProvider contentProvider;
    private Button buttonLink;
    private Button buttonUnlink;
    private SelectionListener linkAction;
    private String[] names;
    SortedMap<String, String> namesAndIds;
    private EntityTypeFilter elementTypeFilter;
    private SelectionListener unlinkAction;
    private LinkRemover linkRemover;
    
    static int oldSelection =-1;

    /**
     * @param parent
     * @param style
     */
    public LinkMaker(Composite parent) {
        super(parent, SWT.BORDER);
        FormLayout formLayout = new FormLayout();
        this.setLayout(formLayout);
    }

    /**
     * @param cnAElement
     * @param isWriteAllowed
     */
    public void createPartControl(Boolean isWriteAllowed) {
        this.writeable = isWriteAllowed;


        Label label1 = new Label(this, SWT.NULL);
        label1.setText(Messages.LinkMaker_0);

        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 5);
        label1.setLayoutData(formData);
        label1.pack();

        combo = new Combo(this, SWT.READ_ONLY);
        FormData formData2 = new FormData();
        formData2.top = new FormAttachment(0, 5);
        formData2.left = new FormAttachment(label1, 5);
        combo.setLayoutData(formData2);
        // combo.pack();

        buttonLink = new Button(this, SWT.PUSH);
        FormData formData3 = new FormData();
        formData3.top = new FormAttachment(combo, 0, SWT.CENTER);
        formData3.left = new FormAttachment(combo, 5);
        buttonLink.setLayoutData(formData3);
        buttonLink.setText(Messages.LinkMaker_1);
        buttonLink.setToolTipText(Messages.LinkMaker_2);
        buttonLink.setEnabled(false);
        // buttonLink.pack();

        buttonUnlink = new Button(this, SWT.PUSH);
        FormData formData5 = new FormData();
        formData5.top = new FormAttachment(combo, 0, SWT.CENTER);
        formData5.left = new FormAttachment(buttonLink, 5);
        buttonUnlink.setLayoutData(formData5);
        buttonUnlink.setText(Messages.LinkMaker_3);
        buttonUnlink.setEnabled(writeable);
        buttonUnlink.setToolTipText(Messages.LinkMaker_4);
        // buttonUnlink.pack();

        viewer = new RelationTableViewer(this, this, SWT.FULL_SELECTION | SWT.MULTI, true);
        FormData formData6 = new FormData();
        formData6.top = new FormAttachment(buttonLink, 2);
        formData6.left = new FormAttachment(0, 1);
        formData6.right = new FormAttachment(100, -1);
        formData6.bottom = new FormAttachment(100, -1);
        viewer.getTable().setLayoutData(formData6);
        viewer.getTable().setEnabled(writeable);

        contentProvider = new RelationViewContentProvider(this, viewer);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new RelationViewLabelProvider(this));
        viewer.setSorter(new RelationByNameSorter(this, IRelationTable.COLUMN_TITLE, IRelationTable.COLUMN_TYPE_IMG));

        // listeners to reload view:
        CnAElementFactory.getLoadedModel().addBSIModelListener(contentProvider);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(contentProvider);

        // listeners to remove stale links from currently open object in editor
        // to prevent conflicts when saving:
        linkRemover = new LinkRemover(this);
        CnAElementFactory.getLoadedModel().addBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(linkRemover);

        createDoubleClickAction();
        hookDoubleClickAction();

        createButtonListeners();
        hookButtonListeners();

        createFilter();
    }

    /**
     * 
     */
    private void createFilter() {
        elementTypeFilter = new EntityTypeFilter(this.viewer);

        combo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                oldSelection = combo.getSelectionIndex();
                setFilter(combo.getSelectionIndex());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
    }

    /**
     * @param selectionIndex
     */
    protected void setFilter(int selectionIndex) {
        if (selectionIndex == 0) {
            elementTypeFilter.setEntityType(null);
            buttonLink.setEnabled(false);
            return;
        }
        Object[] array = namesAndIds.entrySet().toArray();
        String entType = ((Entry<String, String>) array[selectionIndex]).getValue();
        elementTypeFilter.setEntityType(entType);

        // enable link button if we have write permissions:
        buttonLink.setEnabled(writeable);
    }

    /**
	 * 
	 */
    private void hookButtonListeners() {
        this.buttonLink.addSelectionListener(linkAction);
        this.buttonUnlink.addSelectionListener(unlinkAction);
    }

    /**
	 * 
	 */
    private void createButtonListeners() {
        linkAction = new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                // create new link to object
                Object[] array = namesAndIds.entrySet().toArray();
                String selectedType = ((Entry<String, String>) array[combo.getSelectionIndex()]).getValue();
                CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(viewer.getControl().getShell(), selectedType, inputElmt);
                
                if (dialog.open() != Window.OK)
                    return;
                List<CnATreeElement> linkTargets = dialog.getSelectedElements();

                // this method also fires events for added links:
                CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(getInputElmt(), linkTargets);
                
                // refresh viewer because it doesn't listen to events:
                reloadLinks();
            }
        };

        unlinkAction = new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                // delete link:

                if (viewer.getSelection().isEmpty())
                    return;

                List selection = ((IStructuredSelection) viewer.getSelection()).toList();
                boolean confirm = MessageDialog.openConfirm(viewer.getControl().getShell(), Messages.LinkMaker_5, NLS.bind(Messages.LinkMaker_6, selection.size()));
                if (!confirm)
                    return;

                for (Object object : selection) {
                    CnALink link = (CnALink) object;
                    try {
                        CnAElementHome.getInstance().remove(link);
                        //viewer.remove(link);
                        inputElmt.removeLinkDown(link);

                        // notify local listeners:
                        if (CnAElementFactory.isModelLoaded()) {
                            CnAElementFactory.getLoadedModel().linkRemoved(link);
                        }
                        CnAElementFactory.getInstance().getISO27kModel().linkRemoved(link);
                    } catch (Exception e1) {
                        LOG.error("Error while removing link",e1);
                        ExceptionUtil.log(e1, Messages.LinkMaker_7);
                    }
                }

            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(contentProvider);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(contentProvider);

        CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(linkRemover);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(linkRemover);

        super.dispose();
    }

    /**
	 * 
	 */
    private void createDoubleClickAction() {
        doubleClickAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                CnALink link = (CnALink) obj;

                // open the object on the other side of the link:
                if (CnALink.isDownwardLink(getInputElmt(), link))
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependency());
                else
                    EditorFactory.getInstance().updateAndOpenObject(link.getDependant());
            }
        };

    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void initNamesForCombo() {
        if (allPossibleRelations == null)
            names = new String[0];

        namesAndIds = new TreeMap<String, String>();

        for (HuiRelation huiRelation : allPossibleRelations) {
            // from or to element, show other side respectively:
            String targetEntityTypeID = huiRelation.getTo();
            String sourceEntityTypeID = huiRelation.getFrom();

            if (sourceEntityTypeID.equals(this.inputElmt.getEntity().getEntityType())) {
                namesAndIds.put(HitroUtil.getInstance().getTypeFactory().getEntityType(targetEntityTypeID).getName(), targetEntityTypeID);
            } else {
                namesAndIds.put(HitroUtil.getInstance().getTypeFactory().getEntityType(sourceEntityTypeID).getName(), sourceEntityTypeID);
            }
        }

        namesAndIds.put(Messages.LinkMaker_8, null);

        Set<String> names = namesAndIds.keySet();
        this.names = new String[names.size()];
        this.names = (String[]) names.toArray(new String[names.size()]);
    }

    /**
     * @return
     */
    private void fillPossibleLinkLists() {
        // get relations from here to other elements:
        allPossibleRelations = new ArrayList<HuiRelation>();
        EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(inputElmt.getEntity().getEntityType());
        allPossibleRelations.addAll(entityType.getPossibleRelations());

        // get relations from other elements to this one:
        allPossibleRelations.addAll(HitroUtil.getInstance().getTypeFactory().getPossibleRelationsTo(inputElmt.getEntity().getEntityType()));

    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#getInputElmt()
     */
    public CnATreeElement getInputElmt() {
        return inputElmt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#reload()
     */
    public void reload(CnALink oldLink, CnALink newLink) {
        newLink.setDependant(oldLink.getDependant());
        newLink.setDependency(oldLink.getDependency());

        boolean removedLinkDown = inputElmt.removeLinkDown(oldLink);
        boolean removedLinkUp = inputElmt.removeLinkUp(oldLink);
        if (removedLinkUp)
            inputElmt.addLinkUp(newLink);
        if (removedLinkDown)
            inputElmt.addLinkDown(newLink);

        viewer.refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.bsi.views.IRelationTable#setInputElmt(sernet.gs
     * .ui.rcp.main.common.model.CnATreeElement)
     */
    public void setInputElmt(CnATreeElement inputElmt) {
        if (inputElmt == null || this.inputElmt == inputElmt)
            return;

        if (oldSelection==-1)
            oldSelection = combo.getSelectionIndex();
        this.inputElmt = inputElmt;
        fillPossibleLinkLists();
        initNamesForCombo();
        combo.setItems(names);
        if (oldSelection != -1 && oldSelection < combo.getItemCount()) {
            combo.select(oldSelection);
            setFilter(combo.getSelectionIndex());
        }
        viewer.setInput(inputElmt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#reloadAll()
     */
    public void reloadAll() {
        reloadLinks();
    }

    /**
	 * 
	 */
    private void reloadLinks() {

        if (!CnAElementHome.getInstance().isOpen() || inputElmt == null) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                viewer.setInput(new PlaceHolder(Messages.LinkMaker_9));
            }
        });

        WorkspaceJob job = new WorkspaceJob(Messages.LinkMaker_10) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                Activator.inheritVeriniceContextState();

                try {
                    monitor.setTaskName(Messages.LinkMaker_11);

                    FindRelationsFor command = new FindRelationsFor(inputElmt);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    final CnATreeElement linkElmt = command.getElmt();

                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            viewer.setInput(linkElmt);
                        }
                    });
                } catch (Exception e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            viewer.setInput(new PlaceHolder(Messages.LinkMaker_12));
                        }
                    });
                    LOG.error("Error while searching relations",e);
                    ExceptionUtil.log(e, Messages.LinkMaker_13);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();

    }
}
