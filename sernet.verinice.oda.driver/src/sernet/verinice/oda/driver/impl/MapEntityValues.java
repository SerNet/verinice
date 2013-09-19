/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <rschuster[at]tarent[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <rschuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.driver.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Command to retrieve property values for the given elements, making them available
 * to BIRT reports by lazily initializing all of them.
 * 
 * Properties (normal ones but moreso: references) require an existing session to load, therefore this is a
 * command to be executed on the server.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MapEntityValues extends GenericCommand {
    
	private List<List<String>> result;
	
	private String[] propertyTypes;
	private Class<?>[] classes;

    private List<Integer> inputIDs;
    
   private transient Logger log = Logger.getLogger(MapEntityValues.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(MapEntityValues.class);
        }
        return log;
    }

    private boolean addDbId;

    private String typeID;
	
	
	/**
     * @param input
     * @param props
     * @param classes2
     * @param addDbId
     */
    public MapEntityValues(String typeID, List<Integer> inputIDs, String[] props, Class<?>[] classes2, boolean addDbId) {
        this.typeID = typeID;
        this.inputIDs =inputIDs;
        this.propertyTypes = (props != null) ? props.clone() : null;
        this.classes = (classes2 != null) ? classes2.clone() : null;
        this.addDbId = addDbId;
    }

    @SuppressWarnings("unchecked")
	public void execute() {
        
        result = new ArrayList<List<String>>(inputIDs.size());
        
        for (Integer dbid : inputIDs)
        {
            IBaseDao<CnATreeElement, Serializable> dao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(typeID);
            CnATreeElement e = dao.findById(dbid);
            List<String> row = LoadEntityValues.retrievePropertyValues(e.getEntity(), propertyTypes, classes);
            if (addDbId) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Adding dbid: " + e.getDbId() + " to " + e.getTitle());
                }
                row.add(e.getDbId().toString());
            }
            result.add(row);
        }
    }
	
	
	public List<List<String>> getResult()
	{
		return result;
	}

}
