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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.Note;

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

}
