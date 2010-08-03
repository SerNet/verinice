/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
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
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.verinice.samt.rcp;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.w3c.dom.Document;

import org.eclipse.jface.dialogs.MessageDialog;

import sernet.gs.ui.rcp.main.DOMUtil;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ExportCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * {@link Action} that exports assessment objects from the
 * database to an XML file at the selected path. This uses
 * {@link SamtExportDialog} to retrieve user selections.
 */
@SuppressWarnings("restriction")
public class ExportSelfAssessment implements IViewActionDelegate
{
	public static final String ID = "sernet.verinice.samt.rcp.ExportSelfAssessment"; //$NON-NLS-1$
	
	private static final Logger LOG = Logger.getLogger(ExportSelfAssessment.class);
	
	Organization selectedOrganization;
	
	 /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
	
	/*
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
    @Override
    public void run(IAction action) {
		SamtExportDialog dialog = new SamtExportDialog(Display.getCurrent().getActiveShell(), selectedOrganization);
		if( dialog.open() == Dialog.OK )
		{
			LinkedList<CnATreeElement> exportElements = new LinkedList<CnATreeElement>();
			CnATreeElement selectedElement = dialog.getSelectedElement();
			if(selectedElement!=null) {
    			exportElements.add(selectedElement);
    			ExportCommand exportCommand = new ExportCommand(exportElements, selectedElement.getUuid());
    
    			try
    			{
    				exportCommand = ServiceFactory.lookupCommandService().executeCommand(exportCommand);
    			}
    			catch(CommandException ex)
    			{
    				LOG.error("Error while exporting assessment", ex); //$NON-NLS-1$
    				ExceptionUtil.log(ex, Messages.ExportSelfAssessment_1);
    			}
    			
    			Document doc = exportCommand.getExportDocument();
    			DOMUtil.writeDocumentToFile(doc, dialog.getFilePath(), dialog.getEncryptOutput());
    			String title = "";
    			if(selectedElement instanceof Organization) {
    			    title = ((Organization)selectedElement).getTitle();
    			}
    			
    			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
    			        Messages.ExportSelfAssessment_2, 
    			        NLS.bind(Messages.ExportSelfAssessment_3, new Object[] {title, dialog.getFilePath()}));
			}
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
      if(selection instanceof ITreeSelection) {
          ITreeSelection treeSelection = (ITreeSelection) selection;
          Object selectedElement = treeSelection.getFirstElement();
          if(selectedElement instanceof Organization) {
              selectedOrganization = (Organization) selectedElement;
          }
      }
    }
}
