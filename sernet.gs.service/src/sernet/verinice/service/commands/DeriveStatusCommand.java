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
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;


/**
 *  Command determines all generic and specific measures (controls) that are
 *  linked to an isa question (samttopic) and transfers the maturity of the question
 *  to the measures, if maturity not unset or NA.
 *  For existing specific measures, that are set NA (by the user), always the linked generic measure
 *  is set to implemented_yes. Otherwise, the specific measure will be marked implemented_yes
 *  and the generic one will be marked implemented_na. 
 *  for maturity of 4 or 5, all measures up to lvl3 will be set
 *  for maturity of 0, all measures will stay implemented_not_set, but the generic ones which are link
 *  to a specific measure, which will be marked implemented_na.
 */
@SuppressWarnings("serial")
public class DeriveStatusCommand extends ChangeLoggingCommand implements IChangeLoggingCommand {
    
    // 0,4,5 only here for future use
    private static final String TAG_MATURITY_LVL_0 = "ISA_MATLVL_0";
    private static final String TAG_MATURITY_LVL_1 = "ISA_MATLVL_1";
    private static final String TAG_MATURITY_LVL_2 = "ISA_MATLVL_2";
    private static final String TAG_MATURITY_LVL_3 = "ISA_MATLVL_3";
    private static final String TAG_MATURITY_LVL_4 = "ISA_MATLVL_4";
    private static final String TAG_MATURITY_LVL_5 = "ISA_MATLVL_5";
    
    
    private transient Logger log = Logger.getLogger(DeriveStatusCommand.class);
    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
    private List<CnATreeElement> changedElements;
    private String stationId;
    
    private int samtTopicCount = 0;

    private int measureCount = 0;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    private CnATreeElement selectedControlgroup = null;
    
    public DeriveStatusCommand(CnATreeElement controlgroup){
        this.selectedControlgroup = controlgroup;
        changedElements = new LinkedList<CnATreeElement>();
        this.stationId = ChangeLogEntry.STATION_ID;
    }
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(DeriveStatusCommand.class);
        }
        return log;
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
        try {
            if(!selectedControlgroup.isChildrenLoaded()){
                selectedControlgroup = (ControlGroup) hydrate(selectedControlgroup, RetrieveInfo.getPropertyChildrenInstance());
            }
            List<SamtTopic> list = getAllSamtTopics((ControlGroup)selectedControlgroup);
            if(list.size() > 0){
                for(SamtTopic t : list){
                    processSamtTopic(t);
                }
            }
        } finally {
            shutdownCache();
        }
    }

    private void processSamtTopic(SamtTopic t) {
        RetrieveInfo ri = new RetrieveInfo().setChildren(true).setLinksUp(true).setChildrenProperties(true).setParent(true);
        t = (SamtTopic)hydrate(t, ri);
        String maturity = t.getEntity().getSimpleValue(SamtTopic.PROP_MATURITY);
        if(Integer.parseInt(maturity) == 0){ // special case maturity equals 0
            setMaturityZeroMeasures(t);
        }
        else if(maturity != null && !maturity.equals(String.valueOf(SamtTopic.IMPLEMENTED_NA_NUMERIC))){
            Integer matVal = Integer.parseInt(maturity);
            if(matVal > 0 && matVal <= 5){ // standard case,maturity between 1 and 5
                setControlDone(getAllMeasuresToSet(t, maturity), matVal.intValue());
            }
        } else if(maturity != null && maturity.equals(String.valueOf(SamtTopic.IMPLEMENTED_NA_NUMERIC))){ // special case, maturity equals NA
            setAllLinksNA(t);
        }
    }
    
    /**
     * Sets all with SamtTopic t linked generic/specific measures to implemented_no
     * used for case: maturity equals 0 
     * @param t
     */
    private void setMaturityZeroMeasures(SamtTopic t){
        RetrieveInfo info = new RetrieveInfo().setChildren(true).setChildrenProperties(true).setLinksUp(true).setLinksUpProperties(true).setParent(true);
        t = (SamtTopic)hydrate(t, info);
        for(CnALink link : t.getLinksUp()){
            CnATreeElement elmt = link.getRelationObject(t, link);
            info = new RetrieveInfo().setChildren(true).setLinksDown(true).setLinksUp(true).setChildrenProperties(true).setParent(true);
            elmt = hydrate(elmt, info);
            Set<CnALink> links = elmt.getLinksUp();
            links.addAll(elmt.getLinksDown());
            for(CnALink link2 : links){
                CnATreeElement msElmt = link2.getRelationObject(elmt, link2);
                info = new RetrieveInfo().setChildren(true).setChildrenProperties(true).setParent(true);
                msElmt = hydrate(msElmt, info);
                if(msElmt instanceof Control){
                    elmt.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NO);
                    measureCount++;
                    changedElements.add(msElmt);
                }
            }
        }
    }
    
    /**
     * sets all with SamtTopic t linked generic/specific measures to implemented_na
     * used for case: maturity equals na
     * @param t
     */
    private void setAllLinksNA(SamtTopic t){
        for(CnALink mgLink : t.getLinksUp()){
            CnATreeElement dependant = mgLink.getRelationObject(t, mgLink);
            if(dependant instanceof Control){
                RetrieveInfo ri = new RetrieveInfo().setChildren(true).setLinksUp(true).setLinksDown(true).setParent(true);
                dependant = hydrate(dependant, ri);
                dependant.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA); // set generic measure to na
                changedElements.add(dependant);
                for(CnATreeElement msDep : getAllMeasures(dependant, getAllLinks(dependant), true)){
                    if(msDep instanceof Control){
                        ri = RetrieveInfo.getPropertyChildrenInstance().setParent(true);
                        msDep = hydrate(msDep, ri);
                        msDep.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA);
                        changedElements.add(msDep);
                    }
                }
            }
        }
    }
    
    /**
     * sets all measures with maturity <= matToSet to implemented_yes
     * the rest is set to implemented_no if old value was not implemented_not_set (or non existant)
     * @param measures
     * @param matToSet
     */
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
    
    /**
     * returns all (generic and specific) measures linked from a given isa question and 
     * a maturity that needs to be derived
     * @param topic
     * @param maturity
     * @return
     */
    private Set<Control> getAllMeasuresToSet(SamtTopic topic, String maturity){
        Set<Control> list = new HashSet<Control>();
        boolean counterIncreased = false;
        Set<Control> mgSetNA = new HashSet<Control>();
        for(CnALink mglink : getAllLinks(topic)){
            CnATreeElement genericMeasure = mglink.getRelationObject(topic, mglink);
            RetrieveInfo ri = new RetrieveInfo().setChildren(true).setLinksDown(true).setLinksUp(true).setChildrenProperties(true).setParent(true);
            genericMeasure = hydrate(genericMeasure, ri);
            if(genericMeasure instanceof Control){ // check/add for specific measures
                Set<CnATreeElement> specificMeasures = getAllMeasures(genericMeasure, getAllLinks(genericMeasure), true);
                if(specificMeasures.size() > 0){
                    for(CnATreeElement specificMeasure : specificMeasures){
                        if(!specificMeasure.isChildrenLoaded()){
                            ri = new RetrieveInfo().setChildren(true).setChildrenProperties(true).setParent(true);
                            specificMeasure = hydrate(specificMeasure, ri);
                        }
                        String optVal = specificMeasure.getEntity().getOptionValue(Control.PROP_IMPL);
                        // adds specific measure if fits to maturity condition
                        if(specificMeasure instanceof Control && (Integer.parseInt(getMaturityValueByTag((Control)specificMeasure)) <= Integer.parseInt(maturity) &&
                                (optVal == null || !optVal.equals(Control.IMPLEMENTED_NA)))){
                            list.add((Control)specificMeasure);
                            mgSetNA.add((Control)genericMeasure);
                            if(!counterIncreased){
                                counterIncreased = true;
                                samtTopicCount++;
                            }
                        } else if(optVal != null && optVal.equals(Control.IMPLEMENTED_NA)){ // specific measure is na, so take generic measure
                            list.add((Control)genericMeasure);
                            if(!counterIncreased){
                                counterIncreased = true;
                                samtTopicCount++;
                            }
                        }
                    }
                } else { //check for/add generic measures
                    String optVal = genericMeasure.getEntity().getOptionValue(Control.PROP_IMPL);
                    // adds generic measure if fits to maturity condition
                    if(genericMeasure instanceof Control && (Integer.parseInt(getMaturityValueByTag((Control)genericMeasure)) <= Integer.parseInt(maturity) ||
                            (optVal == null))){
                        list.add((Control)genericMeasure);
                        if(!counterIncreased){
                            counterIncreased = true;
                            samtTopicCount++;
                        }
                    }
                }
            }
        }
        setControlsTo(mgSetNA, Control.IMPLEMENTED_NA); // set all MG linked to MS to NA
        return list;
    }
    
    public void setControlsTo(Set<Control> list, String lvlToSet){
        for(Control c : list){
            c.getEntity().setSimpleValue(getImplPropertyType(), lvlToSet);
            measureCount++;
            changedElements.add(c);
        }
    }
    
    /**
     * measures are tagged for maturity lvl, edit this
     * if lvl 4 or 5 will be relevant in future versions
     * @param c
     * @return
     */
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
    
    /**
     * returns (recursive) all samttopics contained in a controlgroup 
     * @param group
     * @return
     */
    private List<SamtTopic> getAllSamtTopics(ControlGroup group){
        LinkedList<SamtTopic> list = new LinkedList<SamtTopic>();
        RetrieveInfo ri = new RetrieveInfo().setChildren(true).setParent(true).setChildrenProperties(true);
        group = (ControlGroup)hydrate(group, ri);
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
        RetrieveInfo ri_;
        if (element == null){
            return element;
        }
        if(ri != null){
            ri_ = ri;
        } else { // no ri set, take default values (which is expensive)
            ri_ = RetrieveInfo.getPropertyChildrenInstance();
            ri_.setLinksDown(true);
            ri_.setLinksUp(true);
            ri_.setLinksDownProperties(true);
            ri_.setLinksUpProperties(true);
            ri_.setParent(true);
        }
        
        CnATreeElement elementFromCache = getElementFromCache(element.getUuid());
        if(elementFromCache!=null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Element from cache: " + elementFromCache.getTitle());
            }
            return elementFromCache;
        }
        
        element = getDao().retrieve(element.getDbId(), ri_);
        
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
    
    private CnATreeElement getElementFromCache(String uuid){
        Element cachedElement = getCache().get(uuid);
        if(cachedElement != null){
            return (CnATreeElement)cachedElement.getValue();
        } else {
            return null;
        }
    }
    
    private Cache createCache() {
        cacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        cache = new Cache(cacheId, 20000, false, false, 600, 500);
        manager.addCache(cache);
        return cache;
    }
    
    private void shutdownCache() {
        CacheManager.getInstance().shutdown();
        manager=null;
    }
    
}
