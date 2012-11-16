/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.awt.dnd.DropTargetListener;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * A DropPerformer is added to a {@link MetaDropAdapter} and performs a drop.
 * As you can see a DropPerformer is not a {@link DropTargetListener} or a
 * {@link ViewerDropAdapter}. It is not noticed about any GUI event itself and will only work
 * as a part of the MetaDropAdapter.
  * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface DropPerformer {

	/**
	 * Performs the drop
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 * @param target the drop target
	 */
	boolean performDrop(Object data, Object target, Viewer viewer);

	/**
	 * Validates a drop
	 * 
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 * @param target the drop target
	 */
	boolean validateDrop(Object target, int operation, TransferData transferType);
	
	
	/**
	 * Returns true if this performer is active
	 * Should return the current result of validateDrop
	 * 
	 * @return true or false
	 */
	boolean isActive();

}
