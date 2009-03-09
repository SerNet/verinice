/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.FindMassnahmenForTodoView;

/**
 * Gets Massnahmen from current BSIModel and reacts to model changes.
 * 
 * Update performed in synchronized thread, but only if
 * necessary, to optimize performance.
 * 
 * @author koderman@sernet.de
 *
 */
class MassnahmenUmsetzungContentProvider implements IStructuredContentProvider, 
				IBSIModelListener {

	final int ADD     = 0;
	final int UPDATE  = 1;
	final int REMOVE  = 2;
	final int REFRESH = 3;
	
	
	private TableViewer viewer;
	private IMassnahmenListView todoView;
	
	public MassnahmenUmsetzungContentProvider(IMassnahmenListView todoView) {
		this.todoView = todoView;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		BSIModel model = CnAElementFactory.getLoadedModel();
		model.removeBSIModelListener(this);
		model.addBSIModelListener(this);
	}


	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof PlaceHolder)
			return new Object[] {inputElement};
		
		List<TodoViewItem> mns = (List<TodoViewItem>) inputElement;
		return mns.toArray(new Object[mns.size()]);
		
	}

	public void dispose() {
		BSIModel model = CnAElementFactory.getLoadedModel();
		model.removeBSIModelListener(this);
	}
	
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		if (child instanceof BausteinUmsetzung)
			reloadModel();		
	}
	
	public void linkChanged(CnALink link) {
		if (link.getDependency() instanceof Person)
			updateViewer(this.REFRESH, null);
	}
	
	public void linkAdded(CnALink link) {
		if (link.getDependency() instanceof Person) {
//			updateViewer(this.REFRESH, null);
			reloadModel();
		}
	}
	
	public void linkRemoved(CnALink link) {
		if (link.getDependency() instanceof Person) {
			reloadModel();
		}
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		if (child instanceof MassnahmenUmsetzung) {
			try {
				FindMassnahmenForTodoView command = new FindMassnahmenForTodoView(child.getDbId());
				command = ServiceFactory.lookupCommandService().executeCommand(
						command);
				List<TodoViewItem> items = command.getAll();
				if (items.size()>0) {
					TodoViewItem item = items.get(0);
					updateViewer(REMOVE, item);
					updateViewer(ADD, item);
				}
			} catch (CommandException e) {
				Logger.getLogger(this.getClass()).debug("Fehler beim Aktualisieren von TodoView", e);
			}
//			updateViewer(UPDATE, child);
		}
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		if (child instanceof BausteinUmsetzung
				|| child instanceof IBSIStrukturElement)
			reloadModel();
	}
	
	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		if (source != null
				&& (source.equals(IBSIModelListener.SOURCE_BULK_EDIT)
				|| source.equals(IBSIModelListener.SOURCE_KONSOLIDATOR)
				)
			)
			reloadModel();
	}
	
	private void reloadModel() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
					todoView.setInput(true);
					} catch (RuntimeException e) {
						ExceptionUtil.log(e, "Konnte Realisierungsplan nicht neu laden.");
					}
				}
			});
	}
	
	
	private void updateViewer(final int type, final Object child) {
		if (Display.getCurrent() != null) {
			switch (type) {
			case ADD:
				viewer.add(child);
				return;
			case UPDATE:
				viewer.refresh(child);
				return;
			case REMOVE:
				viewer.remove(child);
				return;
			case REFRESH:
				viewer.refresh();
				return;
			}
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				switch (type) {
				case ADD:
					viewer.add(child);
					return;
				case UPDATE:
					viewer.refresh(child);
					return;
				case REMOVE:
					viewer.remove(child);
					return;
				case REFRESH:
					viewer.refresh();
					return;
				}
			}
		});
	}

	public void databaseChildAdded(CnATreeElement child) {
	}

	public void databaseChildChanged(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildRemoved(CnATreeElement child) {
		childRemoved(child.getParent(), child);
	}

	public void modelReload(BSIModel newModel) {
		// TODO Auto-generated method stub
		
	}
}
