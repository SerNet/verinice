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
package sernet.verinice.service.commands.unify;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Loads a mapping from elements of  a source-group to a destination-group.
 * 
 * Mapping is creating by replaceable {@link IElementMapper} instances.
 * 
 * Default mapper {@link IsaMapper} is searching for elements in the destination group 
 * with the same number or the same title as in the source group.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadUnifyMapping extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadUnifyMapping.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadUnifyMapping.class);
        }
        return log;
    }
    
    public static final String NUMBER_REGEX_PATTERN = "^\\d+(\\.\\d+)*(\\.)? ";
    
    private String sourceUuid;   
    private String destinationUuid;   
    private String mapperId = ElementMapperFactory.DEFAULT_MAPPER_ID;
    private List<UnifyMapping> mappings;   
    private transient IBaseDao<ControlGroup, Serializable> dao;
    
    /**
     * @param sourceUuid UUID of the source group
     * @param destinationUuid UUID of the destination group
     */
    public LoadUnifyMapping(String sourceUuid, String destinationUuid) {
        this(sourceUuid, destinationUuid, ElementMapperFactory.DEFAULT_MAPPER_ID);
    }
    
    /**
     * @param sourceUuid UUID of the source group
     * @param destinationUuid UUID of the destination group
     * @param mapperId The id of a IElementMapper
     */
    public LoadUnifyMapping(String sourceUuid, String destinationUuid, String mapperId) {
        super();
        this.sourceUuid = sourceUuid;
        this.destinationUuid = destinationUuid;
        this.mapperId = mapperId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        Map<String, CnATreeElement> sourceMap = loadChildrenTitleMap(getSourceUuid());
        Map<String, CnATreeElement> destinationMap = loadChildrenTitleMap(getDestinationUuid());
        IElementMapper mapper = getMapper();
        mapper.validate(sourceMap, destinationMap);
        mappings = mapper.createMapping(sourceMap, destinationMap);
    }

    private Map<String, CnATreeElement> loadChildrenTitleMap(String uuidParent) {
        return loadChildrenTitleMap(uuidParent, new Hashtable<String, CnATreeElement>());
    }
           
    private Map<String, CnATreeElement> loadChildrenTitleMap(String uuidParent, Map<String, CnATreeElement> map) {
        RetrieveInfo ri = RetrieveInfo.getChildrenInstance().setChildrenProperties(true);
        ControlGroup source = getDao().findByUuid(uuidParent, ri);
        if(source!=null) {
            for (CnATreeElement element : source.getChildren()) {
                if(element instanceof IISO27kGroup) {
                    map = loadChildrenTitleMap(element.getUuid(), map);
                } else {
                    map.put(getNumberOrTitle(element.getTitle()), element);
                }
                
            }
        }
        return map;
    }
    
    public static String getNumberOrTitle(String title) {
        String numberOrTitle = title;
        Pattern pattern = Pattern.compile(NUMBER_REGEX_PATTERN);
        Matcher matcher = pattern.matcher(title);
        if(matcher.find()) {
            numberOrTitle = title.substring(0,matcher.end()).trim();
        }
        return numberOrTitle;
    }
    
    private IElementMapper getMapper() {
        return ElementMapperFactory.getMapper(mapperId);
    }


    public List<UnifyMapping> getMappings() {
        return mappings;
    }

    protected String getSourceUuid() {
        return sourceUuid;
    }

    protected String getDestinationUuid() {
        return destinationUuid;
    }

    @SuppressWarnings("unchecked")
    protected IBaseDao<ControlGroup, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(ControlGroup.TYPE_ID);
        }
        return dao;
    }

}
