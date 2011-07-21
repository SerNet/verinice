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
package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;

/**
 * Action in SAMT/ISA view toolbar registered in sernet.verinice.samt.rcp plugin.xml.
 * 
 * Creates a new organization with a new ISA. After creation the tree view is expanded to
 * show the user ISA topics.
 * 
 * {@link AddISAToOrganisation} is a context menu action to create a new ISA
 * in an existing organization.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class AddSelfAssessment implements IViewActionDelegate {

    private static final Logger LOG = Logger.getLogger(AddSelfAssessment.class);

    public static final String TITEL_ORGANIZATION = Messages.AddSelfAssessment_0;
    public static final String TITEL = Messages.AddSelfAssessment_1;

  

    private SamtView samtView = null;
    
    private CreateNewSelfAssessmentService samtService = new CreateNewSelfAssessmentService();
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
        if(view instanceof SamtView) {
            samtView = (SamtView) view;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        try {
            samtService.createSelfAssessment();
            if(Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.EXPAND_ISA) && samtView!=null) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        samtView.expand();
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Could not create self-assessment", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddSelfAssessment_2);
        }

    }

   

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

   

}
