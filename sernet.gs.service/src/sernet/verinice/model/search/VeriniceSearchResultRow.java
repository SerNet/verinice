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

    private Map<String, String> properties;

    /**
     * Should be uuid of corresponding {@link CnATreeElement}
     */
    private String identifier;

    private String fieldOfOccurence;


    public VeriniceSearchResultRow(String identifier, String occurence){
        this.properties = new HashMap<String, String>(0);
        this.identifier = identifier;
        this.fieldOfOccurence = occurence;
    }


    public String getValueFromResultString(String propertyType){
        if(properties.containsKey(propertyType)){
            return properties.get(propertyType);
        }
        return "";
    }

    public void addProperty(String propertyType, String value){
        properties.put(propertyType, value);
    }

    public String getIdentifier(){
        return identifier;
    }

    public String getFieldOfOccurence(){
        return fieldOfOccurence;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldOfOccurence == null) ? 0 : fieldOfOccurence.hashCode());
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        if (fieldOfOccurence == null) {
            if (other.fieldOfOccurence != null)
                return false;
        } else if (!fieldOfOccurence.equals(other.fieldOfOccurence))
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }
}
