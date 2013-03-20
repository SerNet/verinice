/*******************************************************************************
 * Copyright (c) 2013 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * 
 * 
 * @author Julia Haas <jh[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadMassnahmeById extends GenericCommand {

    // Logger for commands, NEVER use log, use getLog() instead
    private transient Logger log = Logger.getLogger(LoadMassnahmeById.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadMassnahmeById.class);
        }
        return log;
    }

    private Integer dbId;
    private MassnahmenUmsetzung massnahme;

    public LoadMassnahmeById(Integer dbId) {
        this.dbId = dbId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance().setProperties(true).setLinksUp(true).setLinksDownProperties(true).setLinksDown(true).setLinksUpProperties(true).setParent(true);
        massnahme = dao.retrieve(dbId, ri);
    }

    public MassnahmenUmsetzung getElmt() {
        return massnahme;
    }
}
