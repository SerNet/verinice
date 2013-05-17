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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;

import sernet.gs.service.TimeFormatter;
import sernet.verinice.hibernate.HibernateDao;
import sernet.verinice.hibernate.TreeElementDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A service to load and analyse the element network of verinice
 * with JGraphT.
 * 
 * JGraphT is a free Java graph library that provides mathematical graph-theory objects and algorithms.
 * 
 * Use {@link IGraphElementLoader} to configure which elements are loaded by the service.
 * 
 * To configure link types use method: setRelationIds(String[] relationIds).
 *  
 *  You have to call "create()" to initialize the service.
 *  After that you can start to use the service.
 * 
 * @see http://jgrapht.org/
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphService implements IGraphService {
    
    private static final Logger LOG = Logger.getLogger(GraphService.class);
    private static final Logger LOG_RUNTIME = Logger.getLogger(GraphService.class.getName() + ".runtime");
    
    private Graph<CnATreeElement, Edge> graph;
    
    private String[] relationIds;
    
    private List<IGraphElementLoader> loaderList;
    
    private TreeElementDao<CnATreeElement, Long> cnaTreeElementDao;
    
    private HibernateDao<CnALink, CnALink.Id> cnaLinkDao;
    
    private Map<String, CnATreeElement> uuidMap = new Hashtable<String, CnATreeElement>();

   
    @Override
    public void create() {
        long time = initRuntime();
        graph = new Pseudograph<CnATreeElement, Edge>(Edge.class);
        uuidMap.clear();
        
        loadVerticesAndRelatives();     
        loadLinks();
         
        log();
        logRuntime("create, runtime: ", time);
    }
    
    /**
     * Loads all vertices and adds them to the graph.
     * An edge for each children is added if the child is part of the graph.
     */
    private void loadVerticesAndRelatives() {
        List<CnATreeElement> elementList = new LinkedList<CnATreeElement>();
        for (IGraphElementLoader loader : getLoaderList()) {
            loader.setCnaTreeElementDao(getCnaTreeElementDao());
            elementList.addAll(loader.loadElements());
        }
        for (CnATreeElement element : elementList) {
            graph.addVertex(element);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Vertex added: " + element.getTitle() );
            }
            uuidMap.put(element.getUuid(), element);
        }
        for (CnATreeElement parent : elementList) {
            Set<CnATreeElement> children = parent.getChildren();
            for (CnATreeElement child : children) {
                CnATreeElement childWithProperties = uuidMap.get(child.getUuid());
                if(childWithProperties!=null) {
                    graph.addEdge(parent, childWithProperties, new Edge(parent, childWithProperties));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Edge added: " + parent.getTitle() + " - " + childWithProperties.getTitle() + ", relatives" );
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("No Edge added, child was not found. Child type / uuid: " + child.getTypeId() + " / " + child.getUuid() + ", Parent is: " + parent.getTitle() );
                }
            }
        }
    }

    private void loadLinks() {
        DetachedCriteria linkCrit = DetachedCriteria.forClass(CnALink.class);
        linkCrit.setFetchMode("dependant", FetchMode.JOIN);
        linkCrit.setFetchMode("dependency", FetchMode.JOIN);
        if(getRelationIds()!=null) {
            linkCrit.add(Restrictions.in("id.typeId", getRelationIds()));
        }
        linkCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<CnALink> linkList = getCnaLinkDao().findByCriteria(linkCrit);
        if (LOG.isInfoEnabled()) {
            LOG.info(linkList.size() + " relevant links found");
        }
        for (CnALink link : linkList) {
            CnATreeElement source = uuidMap.get(link.getDependant().getUuid());
            CnATreeElement target = uuidMap.get(link.getDependency().getUuid());
            if(source!=null && target!=null) {
                graph.addEdge(source, target, new Edge(source, target, link.getRelationId()));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Edge added: " + source.getTitle() + " - " + target.getTitle() + ", " + link.getRelationId());
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphService#getElements()
     */
    @Override
    public Set<CnATreeElement> getElements() {
        return getGraph().vertexSet();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphService#getElements(java.lang.String)
     */
    @Override
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
    
       
    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphService#getLinkTargets(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source) {
        return getLinkTargets(source, null);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphService#getLinkTargets(sernet.verinice.model.common.CnATreeElement, java.lang.String)
     */
    @Override
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source, String typeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning link targets of elment: " + source.getTitle() + ", link type is: " + typeId);
        }
        Set<Edge> edgeList = getGraph().edgesOf(source);
        Set<CnATreeElement> linkTargets = new HashSet<CnATreeElement>();       
        for (Edge edge : edgeList) {
            if(typeId==null || typeId.equals(edge.getType())) {
                CnATreeElement edgeSource = edge.getSource();
                CnATreeElement edgeTarget = edge.getTarget();
                linkTargets.add((edgeSource.equals(source)) ? edgeTarget : edgeSource);
            }
        }
        return linkTargets;
    }

    @Override
    public Graph<CnATreeElement, Edge> getGraph() {
        return graph;
    }

    public String[] getRelationIds() {
        return (relationIds != null) ? relationIds.clone() : null;
    }

    @Override
    public void setRelationIds(String[] relationIds) {
        this.relationIds = (relationIds != null) ? relationIds.clone() : null;
    }
    
    public TreeElementDao<CnATreeElement, Long> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    public void setCnaTreeElementDao(TreeElementDao<CnATreeElement, Long> cnaTreeElementDao) {
        this.cnaTreeElementDao = cnaTreeElementDao;
    }
    
    public HibernateDao<CnALink, CnALink.Id> getCnaLinkDao() {
        return cnaLinkDao;
    }

    public void setCnaLinkDao(HibernateDao<CnALink, CnALink.Id> cnaLinkDao) {
        this.cnaLinkDao = cnaLinkDao;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphService#setLoader(sernet.verinice.graph.IGraphElementLoader[])
     */
    @Override
    public void setLoader(IGraphElementLoader... loaderArray) {
        loaderList = Arrays.asList(loaderArray);       
    }
    
    public List<IGraphElementLoader> getLoaderList() {
        return loaderList;
    }

    public void setLoaderList(List<IGraphElementLoader> loaderList) {
        this.loaderList = loaderList;
    }

    private void logStatistics() {
        if(graph!=null) {
            LOG.debug("Number vertices: " + graph.vertexSet().size());
            LOG.debug("Number edges: " + graph.edgeSet().size());
        }

    }
    
    private void log() {    
        if (LOG.isInfoEnabled()) {
            logStatistics();
        }
        if (LOG.isDebugEnabled()) {
            logVertices();
        }        
    }
    
    private void logVertices() {
        if(graph!=null) {
            for (CnATreeElement element : graph.vertexSet()) {
                LOG.debug(element.getTitle());
                Set<Edge> edges = graph.edgesOf(element);
                for (Edge edge : edges) {
                    CnATreeElement target = edge.getTarget();
                    if(target.equals(element)) {
                        target = edge.getSource();
                    }
                    LOG.debug("  |-" + edge.getType() + " -> " + target.getTitle());
                }
            }
        }
    }
    
    private long initRuntime() {
        long time = 0;
        if (LOG_RUNTIME.isDebugEnabled()) {
            time = System.currentTimeMillis();
        }
        return time;
    }
    
    private void logRuntime(String message, long starttime) {
        LOG_RUNTIME.debug(message + TimeFormatter.getHumanRedableTime(System.currentTimeMillis()-starttime));
    }

  
}
