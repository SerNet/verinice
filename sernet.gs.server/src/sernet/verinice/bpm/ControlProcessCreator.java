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
import sernet.verinice.model.iso27k.Control;

/**
 * Creates one control-execution processes for every control.
 * Used in {@link ProcessJob} configured by Spring configuration file: 
 * sernet/gs/server/spring/veriniceserver-jbpm.xml 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ControlProcessCreator implements IProcessCreater {

    private final Logger log = Logger.getLogger(ControlProcessCreator.class);
    
    private IProcessServiceIsa processService;
    private IBaseDao<Control, Integer> controlDao;
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IProcessCreater#create()
     */
    @Override
    public void create() {       
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        ri.setLinksDown(true);
        List<Control> controlList = getControlDao().findAll(ri);
        if (log.isDebugEnabled()) {
            log.debug("Number of controls: " + controlList.size());
        }
        for (Control control : controlList) {
            getProcessService().handleControl(control);
        }
    }


    public IProcessServiceIsa getProcessService() {
        return processService;
    }

    public void setProcessService(IProcessServiceIsa processService) {
        this.processService = processService;
    }

    public IBaseDao<Control, Integer> getControlDao() {
        return controlDao;
    }

    public void setControlDao(IBaseDao<Control, Integer> daoControl) {
        this.controlDao = daoControl;
    }

}
