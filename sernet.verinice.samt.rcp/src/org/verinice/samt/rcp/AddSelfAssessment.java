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
package org.verinice.samt.rcp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.verinice.samt.service.CreateSelfAssessment;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.service.commands.CsvFile;
import sernet.verinice.iso27k.service.commands.LoadModel;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
public class AddSelfAssessment implements IViewActionDelegate {

    private static final Logger LOG = Logger.getLogger(AddSelfAssessment.class);

    public static final String TITEL = Messages.AddSelfAssessment_0;

    private ICommandService commandService;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        try {
            // load iso-model
            LoadModel loadModel = new LoadModel();
            loadModel = getCommandService().executeCommand(loadModel);
            ISO27KModel model = loadModel.getModel();
            // create self-assessment 
            CreateSelfAssessment command = new CreateSelfAssessment(model, TITEL);
            command.setCsvFile(getCsvFile());
            command = getCommandService().executeCommand(command);
            Organization organization = command.getSelfAssessment();
            if (organization != null) {
                CnAElementFactory.getModel(organization).childAdded(model, organization);
                CnAElementFactory.getModel(organization).databaseChildAdded(organization);
                EditorFactory.getInstance().openEditor(organization);
            }
        } catch (Exception e) {
            LOG.error("Could not create self-assessment", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AddSelfAssessment_2);
        }

    }

    /**
     * Load the self assessment CSV-File:
     * SamtWorkspace.SAMT_CATALOG_FILE_NAME
     * from the file system.
     * 
     * File is saved in verinice workspace in folder:
     * SamtWorkspace.getInstance().getConfDir().
     * 
     * @return the self assessment CSV-File
     * @throws IOException if file can not be found
     */
    private CsvFile getCsvFile() throws IOException {
        CsvFile csvFile = null;    
        final String fullSamtCatalogPath = getFullSamtCatalogPath();
        try {
            csvFile = new CsvFile(fullSamtCatalogPath,getCharset());
        } catch (RuntimeException e) {
            LOG.error("Error while reading samt catalog file from path: " + fullSamtCatalogPath, e); //$NON-NLS-1$
            throw e;
        } catch (IOException e) {
            LOG.error("Error while reading samt catalog file from path: " + fullSamtCatalogPath, e); //$NON-NLS-1$
            throw e;
        } catch (Exception e) {
            final String message = "Error while reading samt catalog file from path: " + fullSamtCatalogPath; //$NON-NLS-1$
            LOG.error(message, e);
            throw new RuntimeException(message, e);
        }
        return csvFile;
    }

    private Charset getCharset() {
        String charsetName = Activator.getDefault().getPreferenceStore().getString(SamtPreferencePage.CHARSET_SAMT);
        if(charsetName==null || charsetName.equals("")) { //$NON-NLS-1$
            charsetName = Activator.getDefault().getPreferenceStore().getDefaultString(SamtPreferencePage.CHARSET_SAMT); 
        }
        return Charset.forName(charsetName);
    }

    private String getFullSamtCatalogPath() {
        return new StringBuffer(SamtWorkspace.getInstance().getConfDir()).append(File.separatorChar).append(SamtWorkspace.SAMT_CATALOG_FILE_NAME).toString();
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

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
