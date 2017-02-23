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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.iso27k.service.RiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.common.CnALink;

/**
 * 
 * Run ISO 27005-style risk analysis.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RunRiskAnalysisCommand extends GraphCommand implements INoAccessControl {
    
    private transient Logger log = Logger.getLogger(RunRiskAnalysisCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(RunRiskAnalysisCommand.class);
        }
        return log;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {      
        IBaseDao<CnALink, Serializable> cnaLinkDao = getDaoFactory().getDAO(CnALink.class);     
        RiskAnalysisService ra = new RiskAnalysisServiceImpl(getGraphService(), cnaLinkDao);
        ra.runRiskAnalysis();     
    }

}


