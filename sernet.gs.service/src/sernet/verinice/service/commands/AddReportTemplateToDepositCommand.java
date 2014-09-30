/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.service.commands;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.service.report.SaveToReportDepositCommand;

/**
 *
 */
public class AddReportTemplateToDepositCommand extends ChangeLoggingCommand implements IChangeLoggingCommand {

    private static final Logger LOG = Logger.getLogger(AddReportTemplateToDepositCommand.class);
    
    private boolean isStandalone;
    
    private String reportName;
    
    private String[] outputFormats;
    
    private String reportFilename;
    
    private byte[] rptDesignFile;
    
    public AddReportTemplateToDepositCommand(String reportName, String[] outputFormats, byte[] rptDesign, String filename, boolean isStandalone){
        this.isStandalone = isStandalone;
        this.reportName = reportName;
        this.outputFormats = outputFormats;
        this.reportFilename = filename;
        this.rptDesignFile = rptDesign;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        ReportTemplateMetaData metadata = new ReportTemplateMetaData(reportFilename, reportName, outputFormats, true);
            writeToServerDeposit(metadata, rptDesignFile);
    }

    private boolean writeToServerDeposit(ReportTemplateMetaData template, byte[] file){
        // call command on server
        SaveToReportDepositCommand command = new SaveToReportDepositCommand(file, template);
        try{
            getCommandService().executeCommand(command);
        } catch(CommandException e){
            LOG.error("Error storing template to server", e);
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return 0;
    }
    
}
