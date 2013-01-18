/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.validation;

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.model.validation.CnAValidation;

/**
 *
 */
public class RefreshValidationView {
    
    private List<CnAValidation> validations;
    
    private TableViewer viewer;
    
    public RefreshValidationView(List<CnAValidation> validationList, TableViewer tableViewer){
        this.validations = validationList;
        this.viewer = tableViewer;
    }
    
    public void refresh(){
        try {
            Display.getDefault().syncExec(new Runnable(){
                public void run() {
                    viewer.setInput(validations);
                }
            });
            
        } catch (Exception t) {
            CnAValidationView.LOG.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }

}
