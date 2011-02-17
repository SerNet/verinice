/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.audit.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import sernet.verinice.iso27k.rcp.RcpProgressObserver;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.iso27k.service.PasteService;
import sernet.verinice.rcp.IProgressRunnable;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

public class PasteOperation implements IProgressRunnable {
    
    /**
     * 
     */
    private IProgressObserver progressObserver;      
    private PasteService service;
    private String message;
    private String id;
    
    @SuppressWarnings("unchecked")
    public PasteOperation(PasteService service, String message, String id) {
        this.service = service;
        this.message =  message;
    }
      
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor)  {    
        service.setProgressObserver(new RcpProgressObserver(monitor));
        service.run();
    }

    public int getNumberOfElements() {
        int n = 0;
        if(service!=null) {
            n = service.getNumberOfElements();
        }
        return n;
    }

    public void openInformation() {
        InfoDialogWithShowToggle.openInformation(
                "Status Information",  
                NLS.bind(message, getNumberOfElements(), (service.getGroup()!=null) ? service.getGroup().getTitle() : ""), 
                "Don't show this message again (You can change this in the preferences)", 
                id);
        
    }
}