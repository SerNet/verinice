/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * Loads all parent titles of elements with UUIDs in uuidList recursively.
 * Parent titles of each element are binded to one string:
 * "Organization > Group > Audit > Group"
 * 
 * Results for are put to a Map, key is the UUID value ist the parent string.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadParentTitles extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadParentTitles.class);
    
    private static final List<String> TOP_LEVEL_TYPE_LIST = Arrays.asList(
            ISO27KModel.TYPE_ID,
            BSIModel.TYPE_ID);
    
    private List<String> uuidList;
    
    private Map<String, String> uuidParentInformationMap;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    public LoadParentTitles(List<String> uuidList) {
        super();
        this.uuidList = uuidList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
       uuidParentInformationMap = new Hashtable<String, String>(uuidList.size());
       for (String uuid : uuidList) {
           CnATreeElement element = getDao().findByUuid(uuid, new RetrieveInfo());
           uuidParentInformationMap.put(uuid, loadParentInformation(element,""));    
       }
    }

    /**
     * Load the title of the parents of an element recursively.
     * 
     * 
     * @param parentId
     * @param parentInformation
     */
    private String loadParentInformation(CnATreeElement element, String parentInformation) {   
        if(element.getParentId()!=null) {
            CnATreeElement parent = getDao().retrieve(element.getParentId(), RetrieveInfo.getPropertyInstance());
            if(!isTopLevelElement(parent)) {
                StringBuilder sb = new StringBuilder(parent.getTitle());
                if(parentInformation!=null && !parentInformation.isEmpty()) {
                    sb.append(" > ").append(parentInformation);
                }
                parentInformation = sb.toString();         
                parentInformation = loadParentInformation(parent,parentInformation);    
            }
        }
        return parentInformation;
    }

    private boolean isTopLevelElement(CnATreeElement element) {      
        return TOP_LEVEL_TYPE_LIST.contains(element.getTypeId());
    }

    protected IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    public Map<String, String> getParentInformationMap() {
        return uuidParentInformationMap;
    }
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadParentTitles.class);
        }
        return log;
    }

}
