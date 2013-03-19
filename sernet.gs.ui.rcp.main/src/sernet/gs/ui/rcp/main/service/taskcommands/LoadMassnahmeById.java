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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.CnaStructureHelper;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class LoadMassnahmeById  extends GenericCommand  {
    
    /**
     * 
     */

    private static final Logger LOG = Logger.getLogger(LoadMassnahmeById.class);
    private Integer dbId; 
    private MassnahmenUmsetzung massnahme;

    
    public LoadMassnahmeById(Integer dbId) {
       this.dbId=dbId;   
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    public void execute() {
        
            
            IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
            RetrieveInfo ri = RetrieveInfo.getPropertyInstance().setProperties(true).setLinksUp(true).setLinksDownProperties(true).setLinksDown(true).setLinksUpProperties(true).setParent(true);
            massnahme = dao.retrieve(dbId, ri);
    }
                
    
    public MassnahmenUmsetzung getElmt() {
        return massnahme;
    }
} 
