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
import java.util.Collections;
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
 *  linked to an ISA question (SamtTopic) and transfers the maturity of the question
 *  to the measures, if maturity not unset or NA.
 *  
 *  For existing specific measures, that are set NA (by the user), always the linked generic measure
 *  is set to implemented_yes. Otherwise, the specific measure will be marked implemented_yes
 *  and the generic one will be marked implemented_na.
 *   
 *  For maturity of 4 or 5, all measures up to level 3 will be set
 *  
 *  For maturity of 0, all measures will stay implemented_not_set, but the generic ones which are link
 *  to a specific measure, which will be marked implemented_na.
 */
@SuppressWarnings("serial")
public class DeriveStatusCommand extends ChangeLoggingCommand implements IChangeLoggingCommand {
    
    private transient Logger log = Logger.getLogger(DeriveStatusCommand.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(DeriveStatusCommand.class);
        }
        return log;
    }
    
    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
    private List<CnATreeElement> changedElements;
    private String stationId;
    
    private int samtTopicCount = 0;
    private int measureCount = 0;
    private CnATreeElement selectedControlgroup = null;
    
    private transient Set<Control> mgSetNA;
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    public DeriveStatusCommand(CnATreeElement controlgroup){
        this.selectedControlgroup = controlgroup;
        changedElements = new LinkedList<CnATreeElement>();
        this.stationId = ChangeLogEntry.STATION_ID;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            mgSetNA = new HashSet<Control>();
            if(!selectedControlgroup.isChildrenLoaded()){
                selectedControlgroup = hydrate(selectedControlgroup, RetrieveInfo.getPropertyChildrenInstance());
            }
            List<SamtTopic> allTopics = getAllTopics((ControlGroup)selectedControlgroup);
            if(allTopics.size() > 0){
                for(SamtTopic t : allTopics){
                    processSamtTopic(t);
                    samtTopicCount++;
                }
            }
        } finally {
            shutdownCache();
        }
    }

    private void processSamtTopic(SamtTopic topic) {
        RetrieveInfo ri = new RetrieveInfo().setLinksUp(true);
        topic = (SamtTopic)hydrate(topic, ri);
        int maturity = topic.getMaturity();
        if(maturity == 0){ // special case maturity equals 0
            setAllLinkedMeasuresToNo(topic);
        } else if( maturity != SamtTopic.IMPLEMENTED_NA_NUMERIC) {
            Integer topicMaturity = maturity;
            if(topicMaturity > 0 && topicMaturity <= 5){ // standard case,maturity between 1 and 5
                processMeasure(getAllMeasuresToSet(topic, maturity), topicMaturity);
            }
        } else { // special case, maturity equals NA
            setAllLinkedMeasuresToNA(topic);
        }
    }
    
    /**
     * Set all measures with maturity <= topicMaturity to implemented_yes.
     * 
     * The rest is set to implemented_no if old value was not implemented_not_set (or non existent).
     * 
     * @param measures A set of measures
     * @param topicMaturity Maturity of samt topic
     */
    private void processMeasure(Set<Control> measures, int topicMaturity){
        for(Control measure : measures){
            Integer controlMaturity = measure.getMaturityValueByTag();
            if(controlMaturity <= topicMaturity){ // default case, set implemented to yes
                measure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_YES);
                measureCount++;
                changedElements.add(measure);
            } else if(measure.isImplementationNotEdited()){ 
                measure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NO);
                measureCount++;
                changedElements.add(measure);
            }
        }
    }
    
    /**
     * Sets all measures linked to a ISA topic to IMPLEMENTED_NO, 
     * but the generic ones which are link to a specific measure, which will be marked IMPLEMENTED_NA
     * Used for case: maturity equals 0.
     * 
     * @param topic An ISA topic
     */
    private void setAllLinkedMeasuresToNo(SamtTopic topic){
        for(CnALink link : topic.getLinksUp()){
            CnATreeElement genericMeasure = link.getRelationObject(topic, link);
            RetrieveInfo ri = new RetrieveInfo().setLinksDown(true).setLinksUp(true).setProperties(true);
            genericMeasure = hydrate(genericMeasure, ri);
            Set<CnALink> links = genericMeasure.getLinksUp();
            links.addAll(genericMeasure.getLinksDown());
            genericMeasure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NO);
            measureCount++;
            changedElements.add(genericMeasure);
            for(CnALink link2 : links){
                CnATreeElement specificMeasure = link2.getRelationObject(genericMeasure, link2);
                ri = RetrieveInfo.getPropertyInstance();
                specificMeasure = hydrate(specificMeasure, ri);
                if(specificMeasure instanceof Control){
                    genericMeasure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA);
                    specificMeasure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NO);
                    measureCount++;
                    changedElements.add(specificMeasure);
                }
            }
        }
    }
    
    /**
     * Sets all generic/specific measures linked with a SamtTopic to IMPLEMENTED_NA.
     * Used for case: maturity equals NA.
     * 
     * @param topic An ISA topic
     */
    private void setAllLinkedMeasuresToNA(SamtTopic topic){
        for(CnALink link : topic.getLinksUp()){
            CnATreeElement genericMeasure = link.getRelationObject(topic, link);
            if(genericMeasure instanceof Control){
                RetrieveInfo ri = new RetrieveInfo().setProperties(true).setLinksUp(true).setLinksDown(true);
                genericMeasure = hydrate(genericMeasure, ri);
                genericMeasure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA); // set generic measure to na
                changedElements.add(genericMeasure);
                measureCount++;
                for(CnATreeElement specificMeasure : getAllMeasures(genericMeasure, getAllLinks(genericMeasure), true)){
                    if(specificMeasure instanceof Control){
                        ri = RetrieveInfo.getPropertyChildrenInstance().setParent(true);
                        specificMeasure = hydrate(specificMeasure, ri);
                        specificMeasure.getEntity().setSimpleValue(getImplPropertyType(), Control.IMPLEMENTED_NA);
                        changedElements.add(specificMeasure);
                        measureCount++;
                    }
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
     * Returns all measures to one given parent, set specific to true if you want to get
     * specific but general measures.
     * 
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
                set.add(hydrate(elmt, RetrieveInfo.getPropertyInstance())); // deprecated, never ever reached
            } else if (specific && parent instanceof Control && elmt instanceof Control) { // specific measures
                set.add(hydrate(elmt, RetrieveInfo.getPropertyInstance()));
            }
        }
        return set;
    }
    
    /**
     * Returns all (generic and specific) measures linked from a given ISA question and 
     * a maturity that needs to be derived.
     * 
     * @param topic A SAMT topic /  ISA question
     * @param maturity Maturity level
     * @return Measures linked to the question
     */
    private Set<Control> getAllMeasuresToSet(SamtTopic topic, int maturity){
        Set<Control> set = new HashSet<Control>();        
        for(CnALink link : getAllLinks(topic)){
            CnATreeElement element = link.getRelationObject(topic, link);
            set.addAll(getMeasures(element));
        }
        setControlsTo(mgSetNA, Control.IMPLEMENTED_NA); // set all MG linked to MS to NA
        return set;
    }

    public Set<Control> getMeasures(CnATreeElement element) {
        Set<Control> set = Collections.emptySet(); 
        RetrieveInfo ri = new RetrieveInfo().setLinksDown(true).setLinksUp(true).setProperties(true);
        element = hydrate(element, ri);
        if(element instanceof Control){ // check/add for specific measures
            Control genericMeasure = (Control) element;
            set = getMeasuresFromTopic( genericMeasure);
        }
        return set;
    }

    public Set<Control> getMeasuresFromTopic(Control genericMeasure) {
        Set<Control> set = new HashSet<Control>(); 
        Set<CnATreeElement> specificMeasures = getAllMeasures(genericMeasure, getAllLinks(genericMeasure), true);      
        for(CnATreeElement specificMeasure : specificMeasures){
            String optVal = getImplOption(specificMeasure);
            // adds specific measure if fits to maturity condition
            if(specificMeasure instanceof Control 
               && (optVal == null || !optVal.equals(Control.IMPLEMENTED_NA))){
                set.add((Control)specificMeasure);
                mgSetNA.add(genericMeasure);
            } else if(optVal != null && optVal.equals(Control.IMPLEMENTED_NA)){ // specific measure is na, so take generic measure
                set.add(genericMeasure);
            }
        }
        if(specificMeasures.isEmpty()){ //check for/add generic measures
            String optVal = getImplOption(genericMeasure);
            // adds generic measure if fits to maturity condition
            if(optVal == null){
                set.add(genericMeasure);
            }
        }
        return set;
    }

    public String getImplOption(CnATreeElement measure) {
        String propName = Control.PROP_IMPL;
        return getOption(measure, propName);
    }

    public String getOption(CnATreeElement measure, String propName) {
        String value = measure.getEntity().getOptionValue(propName);
        if(value!=null && value.isEmpty()) {
            value=null;
        }
        return value;
    }
    
    public void setControlsTo(Set<Control> controls, String levelToSet){
        for(Control c : controls){
            String current = getOption(c, getImplPropertyType().getId());
            if(current==null || !current.equals(levelToSet)) {
                c.getEntity().setSimpleValue(getImplPropertyType(), levelToSet);
                measureCount++;
                changedElements.add(c);
            }
        }
    }
    
    /**
     * Returns (recursive) all ISA topics contained in a control group.
     *  
     * @param group A control group
     * @return All ISA topic
     */
    private List<SamtTopic> getAllTopics(ControlGroup group){
        LinkedList<SamtTopic> list = new LinkedList<SamtTopic>();
        group = (ControlGroup)hydrate(group, RetrieveInfo.getChildrenInstance());
        for(CnATreeElement child : group.getChildren()){
            if(child instanceof SamtTopic){
                list.add((SamtTopic)child);
            } else if (child instanceof ControlGroup){
                list.addAll(getAllTopics((ControlGroup)child));
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
    
    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = createDao();
        }
        return dao;
    }
    
    private IBaseDao<CnATreeElement, Serializable> createDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }
    
}
