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
package sernet.verinice.service.risk;

import java.io.Serializable;
import java.util.Arrays;

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
 * This implementation loads data by IGraphService and saves links by CnALink dao.
 *  
 * This service is managed by the Spring framework. It is configured in file
 *  veriniceserver-risk-analysis.xml (On the server / verinice.PRO) or
 *  veriniceserver-risk-analysis-standalone.xml (verinice stanalone)
 *  
 * This service is configured as a singleton 
 * (see: https://en.wikipedia.org/wiki/Singleton_pattern).
 *  
 * @author Alexander Koderman
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RiskAnalysisServiceImpl implements RiskAnalysisService {
    
    private static final transient Logger LOG = Logger.getLogger(RiskAnalysisServiceImpl.class);
    private static final Logger LOG_RUNTIME = Logger.getLogger(RiskAnalysisServiceImpl.class.getName() + ".runtime");
    
    public enum RiskCalculationMethod {
        ADDITION, MULTIPLICATION
    }  
    public static final RiskCalculationMethod RISK_CALCULATION_METHOD_DEFAULT = RiskCalculationMethod.ADDITION;
    
    /**
     * The riskCalculationMethod is configured in 
     * veriniceserver-plain.properties[.default|.local] respectively
     * veriniceserver-risk-analysis[-standalone].xml
     * 
     * If no value is set in these files, RISK_CALCULATION_METHOD_DEFAULT is used.
     */
    private RiskCalculationMethod riskCalculationMethod = RISK_CALCULATION_METHOD_DEFAULT;
    
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
    public void runRiskAnalysis(RiskAnalysisConfiguration configuration) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running a risk analysis on organizations with database ids: " + Arrays.toString(configuration.getOrganizationDbIds()) + "...");
        }     
        long time = initRuntime();   
        
        VeriniceGraph graph = loadGraph(configuration.getOrganizationDbIds());  
        
        RiskAnalysisJob job = new RiskAnalysisJob(graph, getCnaLinkDao());
        configureRiskCalculator(job);
        job.runRiskAnalysis();
        
        logRuntime("runRiskAnalysis() runtime : ", time);
    }

    private void configureRiskCalculator(RiskAnalysisJob job) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Risk calculation method is: " + riskCalculationMethod);
        }
        switch (riskCalculationMethod) {
        case ADDITION:
            job.setRiskCalculator(new RiskAdder());
            break;
        case MULTIPLICATION:
            job.setRiskCalculator(new RiskMultiplier());
            break;
        default:
            if (LOG.isInfoEnabled()) {
                LOG.info("Setting risk calculation method to the default: addition");
            }
            job.setRiskCalculator(new RiskAdder());
            break;
        }
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

    public RiskCalculationMethod getRiskCalculationMethod() {
        return riskCalculationMethod;
    }


    public void setRiskCalculationMethod(RiskCalculationMethod riskCalculationArithmetic) {
        this.riskCalculationMethod = riskCalculationArithmetic;
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
