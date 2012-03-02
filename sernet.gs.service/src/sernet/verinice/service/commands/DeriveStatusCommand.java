/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;


/**
 *
 */

@SuppressWarnings("serial")
public class DeriveStatusCommand extends GenericCommand implements IChangeLoggingCommand {
    
    private static String TAG_MATURITY_LVL_0 = "ISA_MATLVL_0";
    private static String TAG_MATURITY_LVL_1 = "ISA_MATLVL_1";
    private static String TAG_MATURITY_LVL_2 = "ISA_MATLVL_2";
    private static String TAG_MATURITY_LVL_3 = "ISA_MATLVL_3";
    private static String TAG_MATURITY_LVL_4 = "ISA_MATLVL_4";
    private static String TAG_MATURITY_LVL_5 = "ISA_MATLVL_5";
    
    
    private transient Logger LOG = Logger.getLogger(DeriveStatusCommand.class);
    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
    private List<CnATreeElement> changedElements;
    private String stationId;
    private int samtTopicCount = 0;

    private int measureCount = 0;
    
    transient private IBaseDao<CnATreeElement, Serializable> dao;
    
    private CnATreeElement selectedControlgroup = null;
    
    public DeriveStatusCommand(CnATreeElement controlgroup){
        this.selectedControlgroup = controlgroup;
        changedElements = new LinkedList<CnATreeElement>();
        this.stationId = ChangeLogEntry.STATION_ID;
    }
    
    public Logger getLog() {
        if (LOG == null) {
            LOG = Logger.getLogger(ExportCommand.class);
        }
        return LOG;
    }
    
    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = createDao();
        }
        return dao;
    }
    
    private IBaseDao<CnATreeElement, Serializable> createDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!selectedControlgroup.isChildrenLoaded()){
            selectedControlgroup = (ControlGroup) hydrate(selectedControlgroup, null);
        }
        List<SamtTopic> list = getAllSamtTopics((ControlGroup)selectedControlgroup);
        if(list.size() > 0){
            for(SamtTopic t : list){
                RetrieveInfo ri = new RetrieveInfo().setChildren(true).setLinksUp(true);
                t = (SamtTopic)hydrate(t, ri);
                String maturity = t.getEntity().getSimpleValue(SamtTopic.PROP_MATURITY);
                if(Integer.parseInt(maturity) == 0){
                    setMaturityZeroMeasures(t);
                }
                else if(maturity != null && !maturity.equals(String.valueOf(SamtTopic.IMPLEMENTED_NA_NUMERIC))){
                    Integer matVal = Integer.parseInt(maturity);
                    if(matVal >= 0 && matVal <= 3){
                        setControlDone(getAllMeasuresToSet(t, maturity), matVal.intValue());
                    }
                } else if(maturity != null && maturity.equals(String.valueOf(SamtTopic.IMPLEMENTED_NA_NUMERIC))){
                    setAllLinksNA(t);
                }
            }
        }
    }
    
    private void setMaturityZeroMeasures(SamtTopic t){
        t = (SamtTopic)hydrate(t, null);
        for(CnALink link : t.getLinksUp()){
            CnATreeElement elmt = link.getRelationObject(t, link);
            RetrieveInfo info = new RetrieveInfo().setChildren(true).setLinksDown(true).setLinksUp(true);
            elmt = hydrate(elmt, info);
            Set<CnALink> links = elmt.getLinksUp();
            links.addAll(elmt.getLinksDown());
            for(CnALink link2 : links){
                CnATreeElement msElmt = link2.getRelationObject(elmt, link2);
                info = new RetrieveInfo().setChildren(true).setChildrenProperties(true);
                msElmt = hydrate(msElmt, info);
                if(msElmt instanceof Control){
                    elmt.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NO);
                    measureCount++;
                    changedElements.add(msElmt);
                }
            }
        }
    }
    
    private void setAllLinksNA(SamtTopic t){
        for(CnALink mgLink : t.getLinksUp()){
            CnATreeElement dependant = mgLink.getRelationObject(t, mgLink);
            if(dependant instanceof Control){
                RetrieveInfo ri = new RetrieveInfo().setChildren(true).setLinksUp(true).setLinksDown(true);
                dependant = hydrate(dependant, ri);
                dependant.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA); // set generic measure to na
                changedElements.add(dependant);
                for(CnATreeElement msDep : getAllMeasures(dependant, getAllLinks(dependant), true)){
                    if(msDep instanceof Control){
                        msDep.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA);
                        changedElements.add(msDep);
                    }
                }
            }
        }
    }
    
    private void setControlDone(Set<Control> measures, int matToSet){
        for(Control c : measures){
            Integer tagMat = Integer.parseInt(getMaturityValueByTag(c));
            if(tagMat.intValue() <= matToSet){ // default case, set implemented to yes
                c.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_YES);
                measureCount++;
                changedElements.add(c);
            } else { 
                String simpleValue = c.getEntity().getSimpleValue(Control.PROP_IMPL);
                String optionValue = c.getEntity().getOptionValue(Control.PROP_IMPL);
                if(simpleValue != null && !simpleValue.equals("") && optionValue != null){ // maturity of element > maturity to set AND implemented equals NOT nothing, unset elmt
                    c.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NO);
                    measureCount++;
                    changedElements.add(c);
                }
            }
        }
    }
    
    private Set<CnALink> getAllLinks(CnATreeElement elmt){
        Set<CnALink> set = new HashSet<CnALink>();
        set.addAll(elmt.getLinksUp());
        set.addAll(elmt.getLinksDown());
        return set;
    }
    /**
     * returns all measures to one given parent, set specific to true if you want to get
     * specific but general measures
     * @param parent
     * @param links
     * @param specific
     * @return
     */
    private Set<CnATreeElement> getAllMeasures(CnATreeElement parent, Set<CnALink> links, boolean specific){
        Set<CnATreeElement> set = new HashSet<CnATreeElement>();
        for(CnALink link : links){
            CnATreeElement elmt = link.getRelationObject(parent, link);
            if(!specific && parent instanceof SamtTopic && elmt instanceof Control) { // generic measures
                set.add(elmt); // deprecated, never ever reached
            } else if (specific && parent instanceof Control && elmt instanceof Control) { // specific measures
                set.add(elmt);
            }
        }
        return set;
    }
    
    private Set<Control> getAllMeasuresToSet(SamtTopic topic, String maturity){
        Set<Control> list = new HashSet<Control>();
        boolean counterIncreased = false;
        for(CnALink mglink : getAllLinks(topic)){
            CnATreeElement dependant = mglink.getRelationObject(topic, mglink);
            RetrieveInfo ri = new RetrieveInfo().setChildren(true).setLinksDown(true).setLinksUp(true);
            dependant = hydrate(dependant, ri);
            if(dependant instanceof Control){
                Set<CnATreeElement> specificMeasures = getAllMeasures(dependant, getAllLinks(dependant), true);
                if(specificMeasures.size() > 0){
                    for(CnATreeElement e : specificMeasures){
                        if(!e.isChildrenLoaded()){
                            ri = new RetrieveInfo().setChildren(true).setChildrenProperties(true);
                            e = hydrate(e, ri);
                        }
                        String optVal = e.getEntity().getOptionValue(Control.PROP_IMPL);
                        // adds specific measure if fits to maturity condition
                        if(e instanceof Control && (Integer.parseInt(getMaturityValueByTag((Control)e)) <= Integer.parseInt(maturity) &&
                                (optVal == null || !optVal.equals(Control.IMPLEMENTED_NA)))){
                            list.add((Control)e);
                            if(!counterIncreased){
                                counterIncreased = true;
                                samtTopicCount++;
                            }
                        } else if(optVal != null && optVal.equals(Control.IMPLEMENTED_NA)){ // specific measure is na, so take generic measure
                            list.add((Control)dependant);
                            if(!counterIncreased){
                                counterIncreased = true;
                                samtTopicCount++;
                            }
                        }
                    }
                } else {
                    String optVal = dependant.getEntity().getOptionValue(Control.PROP_IMPL);
                    // adds generic measure if fits to maturity condition
                    if(dependant instanceof Control && (Integer.parseInt(getMaturityValueByTag((Control)dependant)) <= Integer.parseInt(maturity) ||
                            (optVal == null))){
                        list.add((Control)dependant);
                        if(!counterIncreased){
                            counterIncreased = true;
                            samtTopicCount++;
                        }
                    }
                }
            }
        }
        return list;
    }
    
    private String getMaturityValueByTag(Control c){
        String maturity = "-1";
        for(String tag : c.getTags()){
            if(tag.equals(TAG_MATURITY_LVL_1)){
                maturity = "1";
            } else if (tag.equals(TAG_MATURITY_LVL_2)){
                maturity = "2";
            } else if (tag.equals(TAG_MATURITY_LVL_3)){
                maturity = "3";
            }
        }
        return maturity;
    }
    
    private List<SamtTopic> getAllSamtTopics(ControlGroup group){
        LinkedList<SamtTopic> list = new LinkedList<SamtTopic>();
        if(!group.isChildrenLoaded()){
            RetrieveInfo ri = new RetrieveInfo().setChildren(true);
            group = (ControlGroup)hydrate(group, ri);
        }
        for(CnATreeElement child : group.getChildren()){
            if(child instanceof SamtTopic){
                list.add((SamtTopic)child);
            } else if (child instanceof ControlGroup){
                list.addAll(getAllSamtTopics((ControlGroup)child));
            }
        }
        return list;
    }
    
    private PropertyType getImplPropertyType(){
        HUITypeFactory factory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
        return factory.getPropertyType(Control.TYPE_ID, Control.PROP_IMPL);
    }
    
    public int getSamtTopicCount() {
        return samtTopicCount;
    }
    
    public int getMeasureCount() {
        return measureCount;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return 0;
    }
    
    /*************************************************
     * Hydrate {@code element}, including all of its
     * successor elements and properties.
     * 
     * @param element
     *************************************************/
    private CnATreeElement hydrate(CnATreeElement element, RetrieveInfo ri)
    { 
        if (element == null)
            return element;
        if(ri == null){ // no ri set, take default values (which is expensive)
            ri = RetrieveInfo.getPropertyChildrenInstance();
            ri.setLinksDown(true);
            ri.setLinksUp(true);
            ri.setLinksDownProperties(true);
            ri.setLinksUpProperties(true);
        }
        
        Element cachedElement = getCache().get(element.getUuid());
        if(cachedElement!=null) {
            element = (CnATreeElement) cachedElement.getValue();
            if (getLog().isDebugEnabled()) {
                getLog().debug("Element from cache: " + element.getTitle());
            }
            return element;
        }
        
        element = getDao().retrieve(element.getDbId(), ri);
        
        getCache().put(new Element(element.getUuid(), element));
           
        return element;
    }
    
    private Cache getCache() {  
        if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache==null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache = createCache();
        } else {
            cache = manager.getCache(cacheId);
        }
        return cache;
    }
    
    private Cache createCache() {
        cacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        cache = new Cache(cacheId, 20000, false, false, 600, 500);
        manager.addCache(cache);
        return cache;
    }

}
