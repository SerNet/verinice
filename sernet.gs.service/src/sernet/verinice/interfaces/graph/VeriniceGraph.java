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
package sernet.verinice.interfaces.graph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Result and data-transfer object of {@link GraphService}.
 * 
 * This class provides helper methods to get verinice
 * links and parent child relations from the graph.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class VeriniceGraph implements Serializable{

    private transient Logger log = Logger.getLogger(VeriniceGraph.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(VeriniceGraph.class);
        }
        return log;
    }
    
    private Graph<CnATreeElement, Edge> graph;

    public VeriniceGraph() {
        super();
        graph = new Pseudograph<CnATreeElement, Edge>(Edge.class);
    }

    public void addVertex(CnATreeElement element) {
        getGraph().addVertex(element);       
    }

    public void addEdge(CnATreeElement parent, CnATreeElement childWithProperties) {
        getGraph().addEdge(parent, childWithProperties, new Edge(parent, childWithProperties));     
    }
    
    public void addEdge(CnATreeElement parent, CnATreeElement childWithProperties, String typeId) {
        getGraph().addEdge(parent, childWithProperties, new Edge(parent, childWithProperties,typeId));     
    }
    
    /**
     * Returns all elements
     * 
     * @return All elements
     */
    public Set<CnATreeElement> getElements() {
        return getGraph().vertexSet();
    }
    
    /**
     * Returns all elements of type "typeId".
     * 
     * @param typeId Type of returned elements
     * @return All elements of type "typeId".
     */
    public Set<CnATreeElement> getElements(String typeId) {
        HashSet<CnATreeElement> elements = new HashSet<CnATreeElement>();
        Set<CnATreeElement> allElements = getElements();
        
        if(typeId==null || allElements==null) {
            return elements;
        }
        
        for (CnATreeElement element : allElements) {
            if(typeId.equals(element.getTypeId())){
                elements.add(element);
            }
        }       
        return elements;
    }
    
    /**
     * Returns all link targets of an source element.
     * If there are no link targets, an empty list is returned.
     * 
     * @param source Source element
     * @return A set of target elements
     */
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source) {
        return getLinkTargets(source, null);
    }
    
    /**
     * Returns link targets of an source element.
     * Returned links are of type "typeId".
     * If there are no link targets of this type, an empty list is returned.
     * 
     * @param source Source element
     * @param typeId Type of returned links
     * @return A set of target elements
     */
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source, String typeId) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning link targets of elment: " + source.getTitle() + ", link type is: " + typeId + "...");
        }
        Set<Edge> edgeList = getGraph().edgesOf(source);
        Set<CnATreeElement> linkTargets = new HashSet<CnATreeElement>();       
        for (Edge edge : edgeList) {
            if(typeId==null || typeId.equals(edge.getType())) {
                CnATreeElement edgeSource = edge.getSource();
                CnATreeElement edgeTarget = edge.getTarget();
                CnATreeElement target = (edgeSource.equals(source)) ? edgeTarget : edgeSource;
                linkTargets.add(target);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Link found, source: " + source.getTitle() + ", target: " + target.getTitle() + ", link type: " + typeId);
                }
            }
        }
        return linkTargets;
    }
    
    public void log() {    
        if (getLog().isInfoEnabled()) {
            logStatistics();
        }
        if (getLog().isDebugEnabled()) {
            logVertices();
        }        
    }
    
    private void logStatistics() {
        if(getGraph()!=null) {
            getLog().debug("Number vertices: " + getGraph().vertexSet().size());
            getLog().debug("Number edges: " + getGraph().edgeSet().size());
        }

    }
    
    private void logVertices() {
        if(getGraph()!=null) {
            for (CnATreeElement element : getGraph().vertexSet()) {
                getLog().debug(element.getTitle());
                Set<Edge> edges = getGraph().edgesOf(element);
                for (Edge edge : edges) {
                    CnATreeElement target = edge.getTarget();
                    if(target.equals(element)) {
                        target = edge.getSource();
                    }
                    getLog().debug("  |-" + edge.getType() + " -> " + target.getTitle());
                }
            }
        }
    }
    


    public Graph<CnATreeElement, Edge> getGraph() {
        return graph;
    }
}
