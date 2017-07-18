/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.crud.ExecuteHQLInReportCommand;

public class AttachmentEditorInput implements IEditorInput {

	private Attachment input;
	
	

	public AttachmentEditorInput(Attachment selection) {
		input = selection;
	}
	
	public String getId() {
		return (input!=null && input.getEntity()!=null) ? input.getEntity().getUuid() : null;
	}

	public boolean exists() {
		return true;
	}
	
	
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
	
	public String getName() {
		return input.getTitel();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return input.getTitel();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public Attachment getInput() {
		return input;
	}

	public void setInput(Attachment input) {
		this.input = input;
	}

	/**
	 * retrieves {@link CnATreeElement} that is referenced by {@link Attachment} via hql (fully initialized)
	 * method is used by (ism/itgs)-modelviews, if linking is active, selection will change to referenced {@link CnATreeElement}
	 * when attachment, via {@link FileView} is opened.
	 * only do this, if editor is {@link AttachmentEditor}
	 * @param editor - {@link IEditorPart}
	 * @return {@link CnATreeElement}
	 */
	public static CnATreeElement extractCnaTreeElement(IEditorPart editor){
	    CnATreeElement element = null;
	    if(editor.getEditorInput() instanceof AttachmentEditorInput){
	        Attachment a = ((AttachmentEditorInput)editor.getEditorInput()).getInput();
	        String hql = "from CnATreeElement elmt " +
	                "left join fetch elmt.entity as entity " + 
	                "left join fetch entity.typedPropertyLists as propertyList " + 
	                "left join fetch propertyList.properties as props " +
	                "where elmt.dbId = ?";
	        Object[] params = new Object[]{a.getCnATreeElementId()};
	        ExecuteHQLInReportCommand hqlCommand = new ExecuteHQLInReportCommand(hql, params, CnATreeElement.class);
	        try {
	            hqlCommand = ServiceFactory.lookupCommandService().executeCommand(hqlCommand);
	            element = (CnATreeElement)((ArrayList)hqlCommand.getResult()).get(0);
	            element = Retriever.retrieveElement(element, RetrieveInfo.getPropertyInstance());
	        } catch (CommandException e) {
	            Logger.getLogger(AttachmentEditorInput.class).error("Error loading attachment containing cnatreeelement", e);
	        }
	    }
        return element;
	}
}
