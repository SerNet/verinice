/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sernet.verinice.model.common.CnATreeElement;


@SuppressWarnings("serial")
public class VeriniceSearchResultRow implements Serializable{

    public final static String OCCURENCE_PROPERTY_NAME = "occurence-path";
    
    private Map<String, String> properties;

    private VeriniceSearchResultTable parent;

    /**
     * Should be uuid of corresponding {@link CnATreeElement}
     */
    private String identifier;

    private Occurence occurence;

    public VeriniceSearchResultRow(String identifier, Occurence occurence){
        this.properties = new HashMap<String, String>(0);
        this.identifier = identifier;
        this.occurence = occurence;
        this.properties.put(OCCURENCE_PROPERTY_NAME, occurence.toString());
    }

    public VeriniceSearchResultRow(VeriniceSearchResultTable parent, String identifier, Occurence occurence){
        this(identifier, occurence);
        this.parent = parent;
    }


    public String getValueFromResultString(String propertyType){
        if(properties.containsKey(propertyType)){
            return properties.get(propertyType);
        }
        return "";
    }
    
    public Set<String> getPropertyTypes() {
        return this.properties.keySet();
    }
    
    public int getNumberOfProperties() {
        return this.properties.size();
    }

    public void addProperty(String propertyType, String value){
        properties.put(propertyType, value);
    }

    public String getIdentifier(){
        return identifier;
    }

    public String getFieldOfOccurence(){
        return occurence.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VeriniceSearchResultRow other = (VeriniceSearchResultRow) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VeriniceSearchResultRow [properties=" + properties + ", identifier=" + identifier + ", occurence=" + occurence + "]";
    }

    public VeriniceSearchResultTable getParent() {
        return parent;
    }

    public Occurence getOccurence() {
        return occurence;
    }


}
