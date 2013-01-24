/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.rcp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import sernet.gs.service.CsvFile;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.samt.service.CreateSelfAssessment;
import sernet.verinice.service.iso27k.LoadModel;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CreateNewSelfAssessmentService {
    private static final Logger LOG = Logger.getLogger(CreateNewSelfAssessmentService.class);
    
    private ICommandService commandService;

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
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
    protected CsvFile getCsvFile() throws IOException {
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
        StringBuilder sb = new StringBuilder(SamtWorkspace.getInstance().getConfDir()).append(File.separatorChar);
        sb.append(Activator.getDefault().getPreferenceStore().getString(SamtPreferencePage.CATALOG_FILENAME)).toString();
        return sb.toString();
    }
    
    public void createSelfAssessment() throws CommandException, IOException {
        createSelfAssessment(null);
    }
    
    /**
     * @throws CommandException
     * @param addSelfAssessment
     * @throws IOException
     */
    public void createSelfAssessment(final AuditGroup parent) throws CommandException, IOException {
        CreateSelfAssessment command;
        ISO27KModel model = null;
        if(parent==null) {
            // load the model to create a new organization first
            LoadModel loadModel = new LoadModel();
            loadModel = getCommandService().executeCommand(loadModel);
            model = loadModel.getModel();
            command = new CreateSelfAssessment(model, AddSelfAssessment.TITEL_ORGANIZATION, AddSelfAssessment.TITEL);
        } else {
            command = new CreateSelfAssessment(parent, AddSelfAssessment.TITEL_ORGANIZATION, AddSelfAssessment.TITEL);
        }
        
        // create self-assessment 
        command.setCsvFile(getCsvFile());
        command = getCommandService().executeCommand(command);
        Organization organization = command.getOrganization();
        AuditGroup auditGroup = command.getAuditGroup();
        Audit isaAudit = command.getIsaAudit();
        if(parent==null) { 
            CnAElementFactory.getModel(organization).childAdded(model, organization);
            CnAElementFactory.getModel(organization).databaseChildAdded(organization);
        } else {
            CnAElementFactory.getModel(isaAudit).childAdded(auditGroup, isaAudit);
            CnAElementFactory.getModel(isaAudit).databaseChildAdded(isaAudit);     
        }
        
    }

}


