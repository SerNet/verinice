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
package sernet.verinice.service.report;

import java.io.IOException;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 *
 */
public class SaveToReportDepositCommand extends GenericCommand {
    
    private static final Logger LOG = Logger.getLogger(SaveToReportDepositCommand.class);

    
    private byte[] rptdesign;
    
    private ReportTemplateMetaData metadata;
    
    private IReportDepositService depositService;
    
    private String locale;
    
    public SaveToReportDepositCommand(byte[] rptDesign, ReportTemplateMetaData metaData, String locale){
        this.rptdesign = rptDesign;
        this.metadata = metaData;
        this.locale = locale;
        ServerInitializer.inheritVeriniceContextState();
        this.depositService = (IReportDepositService)VeriniceContext.get(VeriniceContext.REPORT_DEPOSIT_SERVICE);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {     
        try {
            depositService.addToServerDeposit(metadata, rptdesign, locale);
        } catch (IOException e) {
            throw new RuntimeCommandException(e);
        }
    }
    
    

}
