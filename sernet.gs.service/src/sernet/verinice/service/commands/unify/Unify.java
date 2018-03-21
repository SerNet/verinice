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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveLink;

/**
 * Unifies a set of elements defined in a list of mappings.
 * Each mapping contains the UUID of a source and a destination element.
 * This command copies all properties from the source to the destination elements. 
 * Empty properties from the source will not delete existing value in the destination.
 * Properties from the blacklist (<code>propertyTypeBlacklist</code> are ignored. 
 * 
 * Optional this command also copies links from the source to the destination elements.
 * 
 * This command uses the builder pattern:
 * https://en.wikipedia.org/wiki/Builder_pattern
 * To create new instances use this code:
 * 
 * Unify unifyCommand = new Unify.Builder(mappings).copyLinks(true).build();
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Unify extends ChangeLoggingCommand implements IChangeLoggingCommand {
    
    private transient Logger log = Logger.getLogger(Unify.class);
    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(Unify.class);
        }
        return log;
    }
    
    /**
     * A list with property-ids from SNCA.xml.
     * These properties are ignored while unifying.
     */
    public static final List<String> PROPERTY_TYPE_BLACKLIST = Arrays.asList(
            SamtTopic.PROP_DESC,
            SamtTopic.PROP_NAME,
            SamtTopic.PROP_VERSION,
            SamtTopic.PROP_WEIGHT,
            SamtTopic.PROP_OWNWEIGHT,
            SamtTopic.PROP_MIN1,
            SamtTopic.PROP_MIN2,
            Control.PROP_NAME,
            Control.PROP_DESC);
    
    private boolean copyLinks = false;
    private boolean deleteSourceLinks = false;
    private boolean dontCopyPropertyValues = false;
    private List<String> propertyTypeBlacklist = PROPERTY_TYPE_BLACKLIST;   
    private List<UnifyMapping> mappings;
    
    private List<CnATreeElement> changedElementList;
    private transient IBaseDao<CnATreeElement, Serializable> dao; 
    private String stationId;
    
    /**
     * Creates a new Unify command with a builder.
     * This constructor ist private. To create a unify command use
     * this code:
     * 
     * Unify unifyCommand = new Unify.Builder(mappings).copyLinks(true).build();
     * 
     * @param builder A builder for a unify command
     * @see https://en.wikipedia.org/wiki/Builder_pattern 
     */
    private Unify(Builder builder) {
        super();
        
        // Required parameters
        mappings = builder.mappings;

        // Optional parameters
        copyLinks = builder.copyLinks;
        deleteSourceLinks = builder.deleteSourceLinks;
        dontCopyPropertyValues = builder.dontCopyPropertyValues;
    }

    /**
     * See class comment first to understand what's happening here.
     * 
     * If unifyinf fails for one {@link UnifyMapping} executing is
     * continued with the next element after logging the exception.
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(mappings!=null) {
            changedElementList = new ArrayList<CnATreeElement>(mappings.size());
            for (UnifyMapping mapping : mappings) {
                unify(mapping);               
            }
        }
    }

    /**
     * Unifies one {@link UnifyMapping}.
     * 
     * @param mapping A mapping with a source and an destination element
     */
    private void unify(UnifyMapping mapping) {
        UnifyElement source = mapping.getSourceElement();
        List<UnifyElement> destinationList = mapping.getDestinationElements();
        for (UnifyElement destination : destinationList) {
            try{
                unify(source,destination);
            } catch (CommandException e){
                getLog().error("Error unifying elements",e);
            }
        }
    }
    
    /**
     * Copies all properties from the source to the destination element.
     * 
     * @param source The source element of a single unify process.
     * @param destination The destination element of a single unify process.
     */
    private void unify(UnifyElement source, UnifyElement destination) throws CommandException {
        if(source==null || destination==null) {
            return;
        }
        CnATreeElement sourceElement = getDao().findByUuid(source.getUuid(), RetrieveInfo.getPropertyInstance());
        CnATreeElement destinationElement = getDao().findByUuid(destination.getUuid(), RetrieveInfo.getPropertyInstance());
        if(!dontCopyPropertyValues){         
            destinationElement.getEntity().copyEntity(sourceElement.getEntity(),propertyTypeBlacklist);
        }
        if(copyLinks){
            destinationElement = unifyLinks(sourceElement, destinationElement);
        }
        if(deleteSourceLinks){
            sourceElement = deleteLinks(sourceElement);
            getDao().saveOrUpdate(sourceElement);
            changedElementList.add(sourceElement);
        }
        getDao().saveOrUpdate(destinationElement);
        changedElementList.add(destinationElement);
    }
    
    /**
     * Copies all links from the source element to the destination element.
     * 
     * @param sourceElement The source element of a single unify process.
     * @param destinationElement The destination element of a single unify process.
     * @return The destination elment with all links from the source element.
     */
    private CnATreeElement unifyLinks(CnATreeElement sourceElement, CnATreeElement destinationElement) throws CommandException{
        // downLink links dependant -> dependency
        for(CnALink linkDown : sourceElement.getLinksDown()){
            createLink(destinationElement, linkDown.getDependency(), linkDown.getRelationId());
        }
        for(CnALink linkUp : sourceElement.getLinksUp()){
            createLink(linkUp.getDependant(), destinationElement, linkUp.getRelationId());
        }
        return destinationElement;
    }
    
    /**
     * Removes all links from an element.
     * 
     * @param element An element
     * @return The element without links
     */
    private CnATreeElement deleteLinks(CnATreeElement element) throws CommandException{
        CnALink[] downLinks = element.getLinksDown().toArray(new CnALink[element.getLinksDown().size()]);
        for(int i = 0; i < downLinks.length; i++){
            removeLink(downLinks[i]);
        }
        CnALink[] upLinks = element.getLinksUp().toArray(new CnALink[element.getLinksUp().size()]); 
        for(int i = 0; i < upLinks.length; i++){
            removeLink(upLinks[i]);
        }
        return element;
    }
    
    private void removeLink(CnALink link)throws sernet.verinice.interfaces.CommandException{
        RemoveLink command = new RemoveLink(link);
        getCommandService().executeCommand(command);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CnALink createLink(CnATreeElement source, CnATreeElement destination, String relationId) throws CommandException {
        CreateLink command = new CreateLink(source, destination, relationId);
        command = getCommandService().executeCommand(command);
        return command.getLink();
    }

 
    protected void setPropertyTypeBlacklist(List<String> propertyTypeBlacklist) {
        this.propertyTypeBlacklist = propertyTypeBlacklist;
    }

    protected IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
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
        return changedElementList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }
    
    /**
     * A builder to create an unify command.
     * 
     * @see https://en.wikipedia.org/wiki/Builder_pattern
     */
    public static class Builder {
        
        // Required parameters
        private final List<UnifyMapping> mappings;

        // Optional parameters - initialize with default values
        private boolean copyLinks = false;
        private boolean deleteSourceLinks = false;
        private boolean dontCopyPropertyValues = false;
        
   
        /**
         * @param mappings The unify mapping for the command
         * @return The Unify builder
         */
        public Builder(List<UnifyMapping> mappings) {
            super();
            this.mappings = mappings;
        }
        
        /**
         * @param copyLinks If true the command copies all links from the source to the destination elements.
         * @return The Unify builder
         */
        public Builder copyLinks(boolean copyLinks) {
            this.copyLinks = copyLinks;
            return this;
        }
        
        /**
         * @param deleteSourceLinks If true the command deletes all links from the source elements.
         * @return The Unify builder
         */
        public Builder deleteSourceLinks(boolean deleteSourceLinks) {
            this.deleteSourceLinks = deleteSourceLinks;
            return this;
        }
        
        /**
         * @param dontCopyAttributes If true the command does not copy propertiy values from the source to the destination elements.
         * @return The Unify builder
         */
        public Builder dontCopyPropertyValues(boolean dontCopyPropertyValues) {
            this.dontCopyPropertyValues = dontCopyPropertyValues;
            return this;
        }
        
        /**
         * @return The Unify command
         */
        public Unify build() {
            return new Unify(this);
        }
    }
}
