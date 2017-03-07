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
 *     Alexander Koderman - initial API and implementation
 *     Benjamin Weißenfels <bw[at]sernet[dot]de>
 *     Daniel Murygin <dm[at]sernet[dot]de>
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Service implementation to run a ISO/IEC 27005 risk analysis.
 *  
 * This implementation loads data by IGraphService and a CnALink dao
 * to save links.
 *  
 * @author Alexander Koderman
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RiskAnalysisServiceImpl implements RiskAnalysisService {
    
    private static final transient Logger LOG = Logger.getLogger(RiskAnalysisServiceImpl.class);
    private static final Logger LOG_RUNTIME = Logger.getLogger(RiskAnalysisServiceImpl.class.getName() + ".runtime");
    
    private IGraphService graphService;  
    private IBaseDao<CnALink, Serializable> cnaLinkDao;
    
    public RiskAnalysisServiceImpl() {
        super();
    }
    
    public RiskAnalysisServiceImpl(IGraphService graphService, IBaseDao<CnALink,Serializable> cnaLinkDao) {
        this.graphService = graphService;
        this.cnaLinkDao = cnaLinkDao;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.RiskAnalysisService#runRiskAnalysis(java.lang.Long[])
     */
    @Override
    public void runRiskAnalysis(Integer... organizationIds) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running a risk analysis on organizations with database ids: " + organizationIds + "...");
        }
        
        long time = initRuntime();   
        
        VeriniceGraph graph = loadGraph(organizationIds);  
        RiskAnalysisJob job = new RiskAnalysisJob(graph, getCnaLinkDao());      
        job.runRiskAnalysis();
        
        logRuntime("runRiskAnalysis() runtime : ", time);
    }
   
    private VeriniceGraph loadGraph(Integer[] scopeIds) { 
        IGraphElementLoader loader = new GraphElementLoader();
        if(scopeIds!=null) {
            loader.setScopeIds(scopeIds);
        }
        loader.setTypeIds(new String[]{Asset.TYPE_ID, IncidentScenario.TYPE_ID, Control.TYPE_ID, Threat.TYPE_ID, Vulnerability.TYPE_ID});
        getGraphService().setLoader(loader);
        return getGraphService().create() ;          
    }

    public IBaseDao<CnALink, Serializable> getCnaLinkDao() {
        return cnaLinkDao;
    }

    public void setCnaLinkDao(IBaseDao<CnALink, Serializable> cnaLinkDao) {
        this.cnaLinkDao = cnaLinkDao;
    }

    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }
    
    static long initRuntime() {
        long time = 0;
        if (LOG_RUNTIME.isDebugEnabled()) {
            time = System.currentTimeMillis();
        }
        return time;
    }
    
    static void logRuntime(String message, long starttime) {
        LOG_RUNTIME.debug(message + TimeFormatter.getHumanRedableTime(System.currentTimeMillis()-starttime));
    }

}
