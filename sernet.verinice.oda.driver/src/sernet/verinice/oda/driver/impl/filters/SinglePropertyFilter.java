/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman
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
 *     Alexander Koderman - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.driver.impl.filters;

import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.oda.FilterChain;
import sernet.verinice.interfaces.oda.IChainableFilter;

/**
 * Return true if the given value is present for the given property.
 * 
 * The value is given as a regular expression and will be matched against the
 * value of the given property-ID.
 * 
 * To use this class comfortably, use the static convenience methods provided by the <code>Filters</code> factory class.
 * 
 * @author akoderman
 *
 */
public class SinglePropertyFilter implements IChainableFilter {
    

    private static final Logger LOG = Logger.getLogger(SinglePropertyFilter.class);

    private static final long serialVersionUID = 4382916136268014218L;
    private String propertyKey;
    private String propertyValueRegex;
    private String entityType;

    /**
     * 
     * Constructor to create a filter for a single property value.
     * 
     * @param propertyKey
     *            The wanted property-type-ID.
     * @param propertyValueRegex
     *            A regular expression to match the value of the given property
     *            against. The filter will run a case-sensitive regular
     *            expression match against the property value. The property
     *            value will be a literal value entered by the user, a date
     *            value in milliseconds, a translated user-facing String for a
     *            property-option or an integer value.
     */
    public SinglePropertyFilter(String entityType, String propertyKey, String propertyValueRegex) {
        super();
        this.entityType = entityType;
        this.propertyKey = propertyKey;
        this.propertyValueRegex = propertyValueRegex;
    }
    
    public SinglePropertyFilter(String propertyKey, String propertyValueRegex) {
        this(null, propertyKey, propertyValueRegex);
    }

    /**
     * Returns a <code>FilterChain</code> containing just this single element.
     */
    @Override
    public FilterChain asList() {
        return new FilterChain(this) {
            
            @Override
            public boolean matches(Entity entity) {
                return SinglePropertyFilter.this.matches(entity);
            }
        };
    }

    @Override
    public boolean matches(Entity entity) {
        // if the filter is not meant for this entity it should not be applied to it - 
        // therefore it acts non-interfering by returning true (not filtering the element):
        if ( Optional.ofNullable(this.entityType).isPresent()
                && !entity.getEntityType().equals(this.entityType) )
            return true;
            
        Pattern pattern = Pattern.compile(propertyValueRegex);
        String value = entity.getPropertyValue(propertyKey);
        boolean find = pattern.matcher(value).find();
        
        if (LOG.isDebugEnabled())
            LOG.debug("Filter for entityType '" + entityType + ", " + this.propertyKey + ", " + this.propertyValueRegex 
                    + "' returned " + find + " for element " + entity.getEntityType() );
        
        return find;
    }

}
