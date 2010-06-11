/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster
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
 *     Robert Schuster <r.schuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.samt.model.SamtTopic;

/**
 * Given a {@link ControlGroup} instance this loads all reachable (= recursive) 
 * @link SamtTopic} instances and hydrates.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadAllSamtTopics extends GenericCommand implements IAuthAwareCommand {

    private transient Logger log = Logger.getLogger(LoadAllSamtTopics.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadAllSamtTopics.class);
        }
        return log;
    }
    
    private transient IAuthService authService;
    
    private Integer id;
    
    private List<SamtTopic> result;
    
    public LoadAllSamtTopics(ControlGroup cg)
    {
    	id = cg.getDbId();
    }
    
    @Override
    public void execute() {
        IBaseDao<ControlGroup, Serializable> dao = getDaoFactory().getDAO(ControlGroup.class);
        
        ControlGroup cg = dao.findById(id);
        
        ArrayList<SamtTopic> resultList = new ArrayList<SamtTopic>();
        loadSamtTopics(cg, resultList);
        result = resultList;
    }
    
    private void loadSamtTopics(ControlGroup cg, ArrayList<SamtTopic> result)
    {
    	if (cg == null)
    		return;
    	
    	for (CnATreeElement e : cg.getChildren())
    	{
    		if (e instanceof SamtTopic)
    		{
    			SamtTopic st = (SamtTopic) e;
    			result.add(st);
    			hydrate(st);
    		}
    		else if (e instanceof ControlGroup)
    		{
    			loadSamtTopics((ControlGroup) e, result);
    		}
    		else
    		{
    			log.warn("found unexpected child for control group: " + e);
    		}
    			
    	}
    }
    
    private void hydrate(SamtTopic st)
    {
    	st.getTitle();
    	st.getWeight2();
    	st.getThreshold2();
    	st.getMaturity();
    }
    
    public List<SamtTopic> getAllSamtTopics()
    {
    	return result;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    public IAuthService getAuthService() {
        return authService;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService(sernet.gs.ui.rcp.main.service.IAuthService)
     */
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }


}
