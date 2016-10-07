/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin <dm@sernet.de>.
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
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import de.sernet.sync.risk.SyncControl;
import de.sernet.sync.risk.SyncRiskAnalysis;
import de.sernet.sync.risk.SyncScenario;
import de.sernet.sync.risk.SyncScenarioList;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Imports a set of risk analysises (IT Baseline Protection) from
 * XML objects Risk. RiskAnalysisImporter is called during VNA import
 * in command SyncInsertUpdateCommand.
 * 
 * Risk schema definition:
 * sernet/verinice/service/sync/risk.xsd
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class RiskAnalysisImporter {

    private List<SyncRiskAnalysis>  syncRiskAnalysisList;
    private List<SyncScenario> syncScenarioList;
    private List<SyncControl> syncControlList;
    private Map<String, CnATreeElement> extIdElementMap;
    private IBaseDao<FinishedRiskAnalysisLists, Serializable> finishedRiskAnalysisListsDao;
    private IBaseDao<OwnGefaehrdung, Serializable> ownGefaehrdungDao;
    private IBaseDao<RisikoMassnahme, Serializable> risikoMassnahmeDao;
    private IBaseDao<CnATreeElement, Serializable> elementDao;
    
    /**
     * @param syncRiskAnalysisList
     * @param list2 
     * @param list 
     */
    public RiskAnalysisImporter(List<SyncRiskAnalysis> syncRiskAnalysisList, 
            List<SyncScenario> syncScenarioList, 
            List<SyncControl> syncControlList) {
        this.syncRiskAnalysisList = syncRiskAnalysisList;
        this.syncScenarioList = syncScenarioList;
        this.syncControlList = syncControlList;
    }

    public void run() {
        importOwnSzenarios();
        importOwnControls();
        importRiskAnalyses();
    }

    private void importRiskAnalyses() {
        for (SyncRiskAnalysis syncRiskAnalysis : syncRiskAnalysisList) {
            importRiskAnalysis(syncRiskAnalysis);
        }
    }

    private void importRiskAnalysis(SyncRiskAnalysis syncRiskAnalysis) {
       CnATreeElement riskAnalysis = extIdElementMap.get(syncRiskAnalysis.getExtId());
       if(riskAnalysis==null) {
           return;
       }
       FinishedRiskAnalysisLists riskAnalysisList = loadRiskAnalysis(riskAnalysis);
       if(riskAnalysisList==null) {      
           riskAnalysisList = new FinishedRiskAnalysisLists();
           riskAnalysisList.setFinishedRiskAnalysisId(riskAnalysis.getDbId());        
       } else {
           riskAnalysisList.getAssociatedGefaehrdungen().clear();
           riskAnalysisList.getAllGefaehrdungsUmsetzungen().clear();
           riskAnalysisList.getNotOKGefaehrdungsUmsetzungen().clear();
       }
    
       SyncScenarioList scenarioList = syncRiskAnalysis.getScenarios();
       riskAnalysisList.getAssociatedGefaehrdungen().addAll(getScenarioSet(scenarioList));
       
       scenarioList = syncRiskAnalysis.getScenariosNotTreated();
       riskAnalysisList.getAllGefaehrdungsUmsetzungen().addAll(getScenarioSet(scenarioList));
       
       scenarioList = syncRiskAnalysis.getScenariosReduction();
       Set<GefaehrdungsUmsetzung> scenarioNotOk = getScenarioSet(scenarioList);
       riskAnalysisList.getNotOKGefaehrdungsUmsetzungen().addAll(scenarioNotOk);    
            
       finishedRiskAnalysisListsDao.merge(riskAnalysisList);
       
       changeControlTypes(scenarioNotOk);
    }

    /**
     * Changes the type of the control which are childs of
     * a risk analysis to RisikoMassnahmenUmsetzung.HIBERNATE_TYPE_ID.
     * 
     * @param scenarioNotOk Scenarios of a risk analysis
     */
    private void changeControlTypes(Set<GefaehrdungsUmsetzung> scenarioNotOk) {
        for (GefaehrdungsUmsetzung scenario : scenarioNotOk) {
            Set<CnATreeElement> controls = scenario.getChildren();
            for (final CnATreeElement control : controls) {
                if(MassnahmenUmsetzung.TYPE_ID.equals(control.getTypeId())) {
                    elementDao.executeCallback(new HibernateCallback() {                    
                        @Override
                        public Object doInHibernate(Session s) throws SQLException {
                            StringBuilder sb = new StringBuilder();
                            sb.append("UPDATE cnatreeelement SET object_type ='");
                            sb.append(RisikoMassnahmenUmsetzung.HIBERNATE_TYPE_ID);
                            sb.append("' WHERE dbid= ").append(control.getDbId());
                            Query q = s.createSQLQuery(sb.toString());
                            return q.executeUpdate();
                        }
                    });
                }
            }
        }
    }

    private FinishedRiskAnalysisLists loadRiskAnalysis(CnATreeElement riskAnalysis) {
        DetachedCriteria criteria = DetachedCriteria.forClass(FinishedRiskAnalysisLists.class);
        criteria.add(Restrictions.eq("finishedRiskAnalysisId", riskAnalysis.getDbId()));
        @SuppressWarnings("unchecked")
        List<FinishedRiskAnalysisLists> result = finishedRiskAnalysisListsDao.findByCriteria(criteria);
        if(result!=null && !result.isEmpty()) {
            return result.get(0);         
        } else {
            return null;
        }
    }
    
    private void importOwnSzenarios() {
        if(syncScenarioList==null) {
            return;
        }
        for (SyncScenario syncScenario : syncScenarioList) {
            importSzenario(syncScenario);
        }
    }
    
    private void importSzenario(SyncScenario syncScenario) {
        OwnGefaehrdung scenario = loadSzenario(syncScenario);
        if(scenario==null) {
            scenario = new OwnGefaehrdung();
        }
        scenario.setBeschreibung(syncScenario.getDescription());
        scenario.setId(syncScenario.getNumber());
        scenario.setTitel(syncScenario.getName());
        scenario.setUuid(syncScenario.getUuid());
        if(syncScenario.getCategory()!=null && !syncScenario.getCategory().isEmpty()) {
            scenario.setOwnkategorie(syncScenario.getCategory());
        }
        ownGefaehrdungDao.merge(scenario);       
    }
    
    private OwnGefaehrdung loadSzenario(SyncScenario syncScenario) {
        DetachedCriteria criteria = DetachedCriteria.forClass(OwnGefaehrdung.class);
        criteria.add(Restrictions.eq("uuid", syncScenario.getUuid()));
        @SuppressWarnings("unchecked")
        List<OwnGefaehrdung> result = ownGefaehrdungDao.findByCriteria(criteria);
        if(result!=null && !result.isEmpty()) {
            return result.get(0);                   
        } else {
            return null;
        }
            
    }

    private void importOwnControls() {
        if(syncControlList==null) {
            return;
        }
        for (SyncControl syncControl : syncControlList) {
            importControl(syncControl);
        }
    }

    private void importControl(SyncControl syncControl) {
        RisikoMassnahme control = loadControl(syncControl);    
        if(control==null) {
            control = new RisikoMassnahme();
        }
        control.setDescription(syncControl.getDescription());
        control.setName(syncControl.getName());
        control.setNumber(syncControl.getNumber());
        control.setUuid(syncControl.getUuid());
        risikoMassnahmeDao.merge(control);
    }

    private RisikoMassnahme loadControl(SyncControl syncControl) {
        DetachedCriteria criteria = DetachedCriteria.forClass(RisikoMassnahme.class);
        criteria.add(Restrictions.eq("uuid", syncControl.getUuid()));
        @SuppressWarnings("unchecked")
        List<RisikoMassnahme> result = risikoMassnahmeDao.findByCriteria(criteria);
        if(result!=null && !result.isEmpty()) {
            return result.get(0);       
        } else {
            return null;
        }
    }

    private Set<GefaehrdungsUmsetzung> getScenarioSet(SyncScenarioList syncScenarioList) {
       Set<GefaehrdungsUmsetzung> scenarioSet = new HashSet<>();
       for (String extId : syncScenarioList.getExtId()) {
           GefaehrdungsUmsetzung gefaehrdung = (GefaehrdungsUmsetzung) extIdElementMap.get(extId);
           if(gefaehrdung!=null) {
               scenarioSet.add(gefaehrdung);
           }
       }
       return scenarioSet;
    }

    public void setExtIdElementMap(Map<String, CnATreeElement> extIdElementMap) {
        this.extIdElementMap = extIdElementMap;
    }

    public void setFinishedRiskAnalysisListsDao(IBaseDao<FinishedRiskAnalysisLists, Serializable> dao) {
       this.finishedRiskAnalysisListsDao = dao;
    }
    
    public void setOwnGefaehrdungDao(IBaseDao<OwnGefaehrdung, Serializable> dao) {
       this.ownGefaehrdungDao = dao;
    }
    
    public void setRisikoMassnahmeDao(IBaseDao<RisikoMassnahme, Serializable> dao) {
       this.risikoMassnahmeDao = dao;
    }
    
    public void setElementDao(IBaseDao<CnATreeElement, Serializable> dao) {
        this.elementDao = dao;
     }

}
