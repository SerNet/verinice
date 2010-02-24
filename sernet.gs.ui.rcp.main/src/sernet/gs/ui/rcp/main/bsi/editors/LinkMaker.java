/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.views.IRelationTable;
import sernet.gs.ui.rcp.main.bsi.views.RelationByNameSorter;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewContentProvider;
import sernet.gs.ui.rcp.main.bsi.views.RelationViewLabelProvider;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRelationsFor;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HuiRelation;

/**
 * A SWT composite that allow the user to create links (relations) to other objects, display the existing links, 
 * change or delete them, jump between linked items in the editor area etc.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
// TODO akoderman this view should also allow the user to create new elements and link them to the current element with one button
// i.e. for server select "Add&Link" to create a new person and link it as "owner" immediately.
public class LinkMaker extends Composite implements IRelationTable {

	private CnATreeElement inputElmt;
	private boolean writeable;
	private Set<HuiRelation> relationsFromHere;
	private RelationTableViewer viewer;
	private Combo combo;
	private Action doubleClickAction;
	private RelationViewContentProvider contentProvider;
	
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
		
		Label label1 = new Label(this, SWT.NULL	);
		label1.setText("Relations to: ");
		label1.setVisible(false);

		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 5);
		label1.setLayoutData(formData);
		label1.pack();
		
		combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		FormData formData2 = new FormData();
		formData2.top = new FormAttachment(0, 5);
		formData2.left = new FormAttachment(label1, 5);
		combo.setLayoutData(formData2);
		combo.setVisible(false);
		combo.pack();

		viewer = new RelationTableViewer(this, this, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		FormData formData3 = new FormData();
		formData3.top = new FormAttachment(combo, 1);
		formData3.left = new FormAttachment(0, 1);
		formData3.right = new FormAttachment(100, -1);
		formData3.bottom = new FormAttachment(100, -1);
		viewer.getTable().setLayoutData(formData3);
		viewer.getTable().setEnabled(writeable);
		
		contentProvider = new RelationViewContentProvider(this, viewer);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new RelationViewLabelProvider(this));
		viewer.setSorter(new RelationByNameSorter(IRelationTable.COLUMN_TITLE, this));
		
		CnAElementFactory.getInstance().getLoadedModel().addBSIModelListener(contentProvider);
		CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(contentProvider);
		createDoubleClickAction();
		hookDoubleClickAction();
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		CnAElementFactory.getInstance().getLoadedModel().removeBSIModelListener(contentProvider);
		CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(contentProvider);
		super.dispose();
	}

	/**
	 * 
	 */
	private void createDoubleClickAction() {
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
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


	private String[] getNames(Set<HuiRelation> relationsFromHere2) {
		if (relationsFromHere2 == null)
			return new String[0];
		
		String[] names = new String[relationsFromHere2.size()];
        int i=0;
		for (HuiRelation huiRelation : relationsFromHere) {
			String targetEntityTypeID = huiRelation.getTo();
			names[i] = HitroUtil.getInstance().getTypeFactory().getEntityType(targetEntityTypeID).getName();
			i++;
		}
		return names;
	}

	/**
	 * @return
	 */
	private void fillPossibleLinkLists() {
		relationsFromHere = new HashSet<HuiRelation>();
		EntityType entityType = HitroUtil.getInstance().getTypeFactory().getEntityType(inputElmt.getEntity().getEntityType());
		
		relationsFromHere = entityType.getPossibleRelations();
		
		
		// relations are always modelled from one side of the relation. We will allow the user to create links both FROM the current element
		// as well as TO the current element (because he doesn't know, from which side a relation is modelled):
		// FIXME akoderman still to do
		
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#getInputElmt()
	 */
	public CnATreeElement getInputElmt() {
		return inputElmt;
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#setInputElmt(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void setInputElmt(CnATreeElement inputElmt) {
		if (inputElmt == null || this.inputElmt == inputElmt)
			return;
		
		this.inputElmt = inputElmt;
		fillPossibleLinkLists();
		combo.setItems(getNames(relationsFromHere));
		viewer.setInput(inputElmt);
	}


	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.views.IRelationTable#reloadAll()
	 */
	public void reloadAll() {
		reloadLinks();
	}


	/**
	 * 
	 */
	private void reloadLinks() {


		if (!CnAElementHome.getInstance().isOpen()) {
			return;
		}

		viewer.setInput(new PlaceHolder("Lade Relationen..."));

		WorkspaceJob job = new WorkspaceJob("Lade Relationen...") {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				Activator.inheritVeriniceContextState();

				try {
					monitor.setTaskName("Lade Relationen...");

					FindRelationsFor command = new FindRelationsFor(inputElmt);
					command = ServiceFactory.lookupCommandService()
							.executeCommand(command);
					final CnATreeElement linkElmt = command.getElmt();

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(linkElmt);
						}
					});
				} catch (Exception e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							viewer.setInput(new PlaceHolder("Fehler beim Laden."));
						}
					});

					ExceptionUtil.log(e, "Fehler beim Laden von Beziehungen.");
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
			
	}
}
