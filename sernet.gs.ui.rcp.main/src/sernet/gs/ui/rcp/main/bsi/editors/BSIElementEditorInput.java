/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Wraps BSI element as editor input.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class BSIElementEditorInput implements IEditorInput {

	private CnATreeElement element;
	

	public BSIElementEditorInput(CnATreeElement element) {
		this.element = element;
	}
	
	public boolean exists() {
		return true;
	}
	
	public CnATreeElement getCnAElement() {
		return element;
	}
	
	public Entity getEntity() {
		return element.getEntity();
	}

	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages()
			.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
	
	public String getId() {
		return element.getId();
	}

	public String getName() {
		return element.getTitle().substring(0, 
		            element.getTitle().length() < 21 
		            ? element.getTitle().length() 
		            : 20  
		       );
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return element.getTitle();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	

	

}
