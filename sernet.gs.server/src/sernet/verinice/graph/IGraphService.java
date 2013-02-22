/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.graph;

import java.util.Set;

import org.jgrapht.Graph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * A service to load and analyze the element network of verinice
 * with JGraphT.
 * 
 * JGraphT is a free Java graph library that provides mathematical graph-theory objects and algorithms.
 * 
 * Call one of the filter methods to configure which elements are loaded by the service:
 *  setScopeId(Integer scopeId)
 *  setTypeIds(String[] typeIds)
 *  setRelationIds(String[] relationIds)
 *  setElementFilter(IElementFilter elementFilter)
 *  
 *  You have to call "create()" to initialize the service.
 *  After that you can start to use the service.
 * 
 * @see http://jgrapht.org/
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IGraphService {

    void setScopeId(Integer scopeId);

    void setTypeIds(String[] typeIds);
    
    void setRelationIds(String[] relationIds);
    
    void setElementFilter(IElementFilter elementFilter);
    
    /**
     * Initializes and creates the JGraphT graph.
     * Call this before using the graph.
     */
    void create();
    
    /**
     * Returns the element network as JGraphT graph.
     * Call create() before using this method.
     * 
     * @return The element network as JGraphT graph.
     */
    Graph<CnATreeElement, Edge> getGraph();
    
    /**
     * Returns all link targets of an source element.
     * 
     * @param source Source element
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(CnATreeElement source);
    
    /**
     * Returns link targets of an source element.
     * Returned links are of type "typeId".
     * 
     * @param source Source element
     * @param typeId Type of returned links
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(CnATreeElement source, String typeId);
}
