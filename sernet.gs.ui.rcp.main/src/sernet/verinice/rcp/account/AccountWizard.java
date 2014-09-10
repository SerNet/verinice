/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.rcp.account;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard to start jBPM process "individual-task" defined in individual-task.jpdl.xml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccountWizard extends Wizard {

    private static final Logger LOG = Logger.getLogger(AccountWizard.class);
    
    //private DescriptionPage descriptionPage;

    public AccountWizard() {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle("Account");
    }

    @Override
    public void addPages() {
        //descriptionPage = new DescriptionPage(elementTitle);
        //addPage(descriptionPage);
     
    }

    @Override
    public boolean performFinish() {

        return true;
    }

}
