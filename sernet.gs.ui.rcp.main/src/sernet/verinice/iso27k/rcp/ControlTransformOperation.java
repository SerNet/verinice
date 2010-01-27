/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.UIPlugin;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.service.IItem;
import sernet.verinice.iso27k.service.ITransformer;
import sernet.verinice.iso27k.service.ItemControlTransformer;

/**
 * Operation with transforms items from the {@link CatalogView}
 * to {@link Control}s. Created controls are added to a {@link ControlGroup}.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ControlTransformOperation implements IRunnableWithProgress {

	private static final Logger LOG = Logger.getLogger(ControlTransformOperation.class);
	
	private ICommandService commandService;
	
	@SuppressWarnings("unchecked")
	private Group selectedGroup;
	
	private int numberOfControls;
	
	public int getNumberOfControls() {
		return numberOfControls;
	}


	@SuppressWarnings("unchecked")
	public ControlTransformOperation(Group selectedGroup) {
		this.selectedGroup = selectedGroup;	
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {
		try {	
			Activator.inheritVeriniceContextState();
			List<IItem> itemList = extractChildren(DNDItems.getItems());
			numberOfControls = itemList.size();
			StringBuilder sb = new StringBuilder();
			sb.append("Transforming ").append(numberOfControls).append(" catalog items to controls.");
			monitor.beginTask(sb.toString(), numberOfControls);
			ITransformer<IItem,Control> transformer = new ItemControlTransformer(itemList);
			List<Control> controlList = transformer.getResultList();
			int i = 0;
			for (Control control : controlList) {
				if(monitor.isCanceled()) {
					LOG.warn("Transforming canceled. " + i + " of " + numberOfControls + " items transformed.");
					break;
				}
				monitor.setTaskName(getText(numberOfControls,i,control.getTitle()));
				selectedGroup.addChild(control);
				control.setParent(selectedGroup);
				SaveElement<Control> saveCommand = new SaveElement<Control>(control);
				saveCommand = getCommandService().executeCommand(saveCommand);
				control = saveCommand.getElement();
				control.setParent(selectedGroup);
				CnAElementFactory.getModel(control).childAdded(selectedGroup, control);
				CnAElementFactory.getModel(control).databaseChildAdded(control);
				monitor.worked(i++);
			}		
		} catch (Exception e) {
			LOG.error("Error while transforming to control", e);
		} finally {
			monitor.done();
		}

	}
	
	/**
	 * Extracts all children from dragged items.
	 * Returns a list with all sub-items of dragged items. 
	 * Items with children are not included in the returned list.
	 * Only "leaves" will be returned.
	 * 
	 * @param items A list with dragged items
	 * @return A list with all sub-items of dragged items
	 */
	private List<IItem> extractChildren(List<IItem> itemDragList) {
		List<IItem> itemList = new ArrayList<IItem>();
		for (IItem item : itemDragList) {
			extractChildren(item,itemList);
		}
		return itemList;
	}

	/**
	 * Recursive method to implement extractChildren(List<IItem> itemDragList)
	 * 
	 * @param item
	 * @param itemList
	 */
	private void extractChildren(final IItem item, List<IItem> itemList) {
		if(item.getItems()==null || item.getItems().size()==0) {
			itemList.add(item);
		} else {
			for (IItem child : item.getItems()) {
				extractChildren(child,itemList);
			}
		}	
	}

	/**
	 * @param n
	 * @param i
	 * @param title
	 */
	private String getText(int n, int i, String title) {
		StringBuilder sb = new StringBuilder();
		sb.append(i).append(" of ").append(n).append(" items transformed. ");
		sb.append("Transforming item: ").append(title);
		return sb.toString();
	}


	public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}

}
