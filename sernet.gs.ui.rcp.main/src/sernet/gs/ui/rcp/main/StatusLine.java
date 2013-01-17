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
package sernet.gs.ui.rcp.main;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;


public class StatusLine {

    private static final Logger LOG = Logger.getLogger(StatusLine.class);
    
	private static IStatusLineManager getStatusLine() {
		try {
			IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (activePart instanceof IViewPart) {
				return ((IViewPart)activePart).getViewSite().getActionBars().getStatusLineManager();
			}
			if (activePart instanceof IEditorPart) {
				return ((IEditorPart)activePart).getEditorSite().getActionBars().getStatusLineManager();
			}
		} catch (RuntimeException e) {
		    LOG.error("Error while getting status line manager.", e);
		}
		return null;
	}
	
	public static void setMessage(String message) {
		if (getStatusLine() != null) {
			getStatusLine().setMessage(message);
		}
	}

	public static void setErrorMessage(String message) {
		if (getStatusLine() != null) {
			getStatusLine().setErrorMessage(message);
		}
	}

}
