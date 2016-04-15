/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.model;

import java.io.Serializable;
import java.util.*;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class ObjectModelContainer implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<String, Set<String>> allRelationPartners = new HashMap<>();
    private Map<String, Set<String>> allPossibleProperties = new HashMap<>();
    private Map<String, String> allLabels = new HashMap<>();
    private Map<String, String> allRelationLabels = new HashMap<>();
    private Set<String> allTypeIds = new HashSet<>();
    private Map<String, Set<String>> possibleChildren = new HashMap<>();
    private Map<String, Set<String>> possibleParents = new HashMap<>();

    public ObjectModelContainer() {
        // needed to create an empty object;
    }

    public ObjectModelContainer(Map<String, Set<String>> allRelationPartners,
            Map<String, Set<String>> allPossibleProperties, Map<String, String> allLabels,
            Map<String, String> allRelationLabels, Set<String> allTypeIds,
            Map<String, Set<String>> possibleChildren, Map<String, Set<String>> possibleParents) {
        super();
        this.allRelationPartners = allRelationPartners;
        this.allPossibleProperties = allPossibleProperties;
        this.allLabels = allLabels;
        this.allRelationLabels = allRelationLabels;
        this.allTypeIds = allTypeIds;
        this.possibleChildren = possibleChildren;
        this.possibleParents = possibleParents;
    }

    public Map<String, Set<String>> getAllRelationPartners() {
        return allRelationPartners;
    }

    public void setAllRelationPartners(Map<String, Set<String>> allRelationPartners) {
        this.allRelationPartners = allRelationPartners;
    }

    public Map<String, Set<String>> getAllPossibleProperties() {
        return allPossibleProperties;
    }

    public void setAllPossibleProperties(Map<String, Set<String>> allPossibleProperties) {
        this.allPossibleProperties = allPossibleProperties;
    }

    public Map<String, String> getAllLabels() {
        return allLabels;
    }

    public void setAllLabels(Map<String, String> allLabels) {
        this.allLabels = allLabels;
    }

    public Map<String, String> getAllRelationLabels() {
        return allRelationLabels;
    }

    public void setAllRelationLabels(Map<String, String> allRelationLabels) {
        this.allRelationLabels = allRelationLabels;
    }

    public Set<String> getAllTypeIds() {
        return allTypeIds;
    }

    public void setAllTypeIds(Set<String> allTypeIds) {
        this.allTypeIds = allTypeIds;
    }

    public Map<String, Set<String>> getPossibleChildren() {
        return possibleChildren;
    }

    public void setPossibleChildren(Map<String, Set<String>> possibleChildren) {
        this.possibleChildren = possibleChildren;
    }

    public Map<String, Set<String>> getPossibleParents() {
        return possibleParents;
    }

    public void setPossibleParents(Map<String, Set<String>> possibleParents) {
        this.possibleParents = possibleParents;
    }

}
