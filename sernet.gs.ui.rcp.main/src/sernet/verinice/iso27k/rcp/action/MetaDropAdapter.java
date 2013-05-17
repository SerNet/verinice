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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * MetaDropAdapter is a {@link ViewerDropAdapter} with a list of {@link DropPerformer}s.
 * Since RCP allowes only one ViewerDropAdapter for a view you can
 * a add more than one DropPerformer to a view.
 * 
 * The MetaDropAdapter delegates performing of drops to the DropPerformers.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class MetaDropAdapter extends ViewerDropAdapter {
	
	private static final Logger LOG = Logger.getLogger(MetaDropAdapter.class);
	
	private Set<DropPerformer> performerSet;

	/**
	 * @param viewer
	 */
	public MetaDropAdapter(Viewer viewer) {
		super(viewer);
		performerSet = new HashSet<DropPerformer>();
	}
	
	public void addAdapter(DropPerformer adapter) {
		performerSet.add(adapter);
	}
	
	public void removeAdapter(DropPerformer adapter) {
		performerSet.remove(adapter);
	}
	
	public void clear() {
		performerSet.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(Object data) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("performDrop...");
		}
		boolean success = false;
		try {
		for (DropPerformer adapter : performerSet) {
			if(adapter.isActive()) {
				if(adapter.performDrop(data, getCurrentTarget(), getViewer())) {
					success = true;
					if (LOG.isDebugEnabled()) {
						LOG.debug("performDrop, success: " + adapter);
					}
				}
			} else if (LOG.isDebugEnabled()) {
				LOG.debug("performDrop, adapter is not active: " + adapter);
			}
		}
		} catch( RuntimeException e ) {
			LOG.error("Error while performing drop.", e);
			throw e;
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		boolean success = false;
		for (DropPerformer adapter : performerSet) {
			if(adapter.validateDrop(target, operation, transferType)) {
				success = true;
			}
		}
		return success;
	}

}
