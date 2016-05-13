/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.IFinishedRiskAnalysisListsDao;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadRiskAnalyses extends GenericCommand implements ICommand {

    private IBaseDao<FinishedRiskAnalysis, Serializable> raDao;
    private IFinishedRiskAnalysisListsDao raListDao;
    
    private Integer parentDbId;
    private boolean useParentId = false;
    
    private List<FinishedRiskAnalysis> raList;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    /**
     * use this one, to delete all riskanalyses for a complete scope
     * @param scopeId
     */
    public LoadRiskAnalyses(Integer scopeId) {
        parentDbId = scopeId; 
    }
    
    /**
     * use this, to delete riskanalyes beneath a specified parent
     * @param parentDbId
     * @param useParent
     */
    public LoadRiskAnalyses(Integer parentDbId, boolean useParent){
        this.parentDbId = parentDbId;
        this.useParentId = useParent;
    }

    @Override
    public void execute() {
        DetachedCriteria criteria = DetachedCriteria.forClass(FinishedRiskAnalysis.class);
        if (!useParentId){
            criteria.add(Restrictions.eq("scopeId", parentDbId));
        } else {
            criteria.add(Restrictions.eq("parentId", parentDbId));
        }
        raList = getRaDao().findByCriteria(criteria);
    }

    public List<FinishedRiskAnalysis> getRaList() {
        return raList;
    }

    public IBaseDao<FinishedRiskAnalysis, Serializable> getRaDao() {
        if(raDao==null) {
            raDao = getDaoFactory().getDAO(FinishedRiskAnalysis.class);      
        }
        return raDao;
    }
    
    public IFinishedRiskAnalysisListsDao getRaListDao() {
        if(raListDao==null) {
            raListDao = getDaoFactory().getFinishedRiskAnalysisListsDao();      
        }
        return raListDao;
    }
    
 
    

}
