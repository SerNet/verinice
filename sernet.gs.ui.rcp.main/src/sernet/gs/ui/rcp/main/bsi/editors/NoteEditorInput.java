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

import java.security.SecureRandom;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.verinice.model.bsi.Note;

public class NoteEditorInput implements IEditorInput {

	private Note input;

	public NoteEditorInput(Note selection) {
		input = selection;
	}
	
	public Integer getId() {
		if(isIsAvailable())
		    return input.getDbId();
		else {
		    return new SecureRandom().nextInt(Integer.MAX_VALUE);
		}
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
		return "Notiz";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
	public Note getInput() {
		return input;
	}

	public void setInput(Note input) {
		this.input = input;
	}

	private boolean isIsAvailable() {
        return input != null && input.getDbId() != null;
    }

}
