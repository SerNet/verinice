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
package sernet.verinice.bpm;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IProcessServiceIsa;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Creates one control-execution processes for every control.
 * Used in {@link ProcessJob} configured by Spring configuration file: 
 * sernet/gs/server/spring/veriniceserver-jbpm.xml 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaProcessCreator implements IProcessCreater {

    private final Logger log = Logger.getLogger(IsaProcessCreator.class);
    
    private IProcessServiceIsa processService;
    private IBaseDao<SamtTopic, Integer> samtTopicDao;
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IProcessCreater#create()
     */
    @Override
    public void create() {       
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        ri.setLinksDown(true);
        List<SamtTopic> controlList = getSamtTopicDao().findAll(ri);
        if (log.isDebugEnabled()) {
            log.debug("Number of isa topics: " + controlList.size());
        }
        for (SamtTopic control : controlList) {
            getProcessService().handleSamtTopic(control);
        }
    }


    public IProcessServiceIsa getProcessService() {
        return processService;
    }

    public void setProcessService(IProcessServiceIsa processService) {
        this.processService = processService;
    }

    public IBaseDao<SamtTopic, Integer> getSamtTopicDao() {
        return samtTopicDao;
    }

    public void setSamtTopicDao(IBaseDao<SamtTopic, Integer> samtTopicDao) {
        this.samtTopicDao = samtTopicDao;
    }


    public void setControlDao(IBaseDao<SamtTopic, Integer> samtTopicDao) {
        this.samtTopicDao = samtTopicDao;
    }

}
