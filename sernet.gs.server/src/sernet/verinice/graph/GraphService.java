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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.DirectedVeriniceGraph;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.UndirectedVeriniceGraph;
import sernet.verinice.interfaces.graph.VeriniceGraph;
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
 * You have to call "create()" to initialize the service.
 * After that you can start to use the service.
 * 
 * @see http://jgrapht.org/
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphService implements IGraphService, Serializable {
    
    private static final Logger LOG = Logger.getLogger(GraphService.class);
    private static final Logger LOG_RUNTIME = Logger.getLogger(GraphService.class.getName() + ".runtime");
    
    private VeriniceGraph graph;
    
    private boolean loadLinks = true;

    private String[] relationIds;
    
    private List<IGraphElementLoader> loaderList;
    
    private IBaseDao<CnATreeElement, Long> cnaTreeElementDao;
    
    private IBaseDao<CnALink, CnALink.Id> cnaLinkDao;
    
    private Map<String, CnATreeElement> uuidMap = new HashMap<>();

    @Override
    public VeriniceGraph create() {
        graph = new UndirectedVeriniceGraph();
        doCreate();
        return graph;
    }


    @Override
    public VeriniceGraph createDirectedGraph(){
        graph = new DirectedVeriniceGraph();

        doCreate();
        return graph;
    }

    private void doCreate(){
        long time = initRuntime();
        uuidMap.clear();
        loadVerticesAndRelatives();
        if(isLoadLinks()) {
            loadLinks();
        } else {
            LOG.info("Loading of links is disabled.");
        }
        log();
        logRuntime("Graph generation runtime: ", time);
    }
    
    /**
     * Loads all vertices and adds them to the graph.
     * An edge for each children is added if the child is part of the graph.
     */
    private void loadVerticesAndRelatives() {
        List<CnATreeElement> elementList = new LinkedList<>();
        for (IGraphElementLoader loader : getLoaderList()) {
            loader.setCnaTreeElementDao(getCnaTreeElementDao());
            elementList.addAll(loader.loadElements());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(elementList.size() + " relevant elements found");
        }
        for (CnATreeElement element : elementList) {
            graph.addVertex(element);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Vertex added: " + element.getTitle() + " [" + element.getTypeId() + "]" );
            }
            uuidMap.put(element.getUuid(), element);
        }
        for (CnATreeElement parent : elementList) {
            Set<CnATreeElement> children = parent.getChildren();
            for (CnATreeElement child : children) {
                createParentChildEdge(parent, child);
            }
        }
    }

    protected void createParentChildEdge(CnATreeElement parent, CnATreeElement child) {
        CnATreeElement childWithProperties = uuidMap.get(child.getUuid());
        if(childWithProperties!=null) {
            graph.addEdge(new Edge(parent, childWithProperties));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Edge added: " + parent.getTitle() + " - " + childWithProperties.getTitle() + ", relatives" );
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No Edge added, child was not found. Child type / uuid: " + child.getTypeId() + " / " + child.getUuid() + ", Parent is: " + parent.getTitle() );
        }
    }

    private void loadLinks() {
        DetachedCriteria linkCrit = DetachedCriteria.forClass(CnALink.class);
        linkCrit.setFetchMode("dependant", FetchMode.JOIN);
        linkCrit.setFetchMode("dependency", FetchMode.JOIN);
        if (getRelationIds() != null && getRelationIds().length > 0) {
            linkCrit.add(Restrictions.in("id.typeId", getRelationIds()));
        }
        linkCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        @SuppressWarnings("unchecked")
        List<CnALink> linkList = getCnaLinkDao().findByCriteria(linkCrit);
        if (LOG.isInfoEnabled()) {
            LOG.info(linkList.size() + " relevant links found");
        }
        for (CnALink link : linkList) {
            CnATreeElement source = uuidMap.get(link.getDependant().getUuid());
            CnATreeElement target = uuidMap.get(link.getDependency().getUuid());
            Edge edge = createEdge(link);
            if(edge!=null) {
                
                graph.addEdge(edge);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Edge added: " + source.getTitle() + " <-> " + target.getTitle() + " [" + link.getRelationId() + "]");
                }
            }
        }
    }

    private Edge createEdge(CnALink link) {
        CnATreeElement source = uuidMap.get(link.getDependant().getUuid());
        CnATreeElement target = uuidMap.get(link.getDependency().getUuid());
        Edge edge = null;
        if(source!=null && target!=null) {
            edge = new Edge(source, target);
            edge.setType(link.getRelationId());
            edge.setDescription(link.getComment());
            edge.setRiskAvailability(link.getRiskAvailability());
            edge.setRiskConfidentiality(link.getRiskConfidentiality());
            edge.setRiskIntegrity(link.getRiskIntegrity());
            edge.setRiskAvailabilityWithControls(link.getRiskAvailabilityWithControls());
            edge.setRiskConfidentialityWithControls(link.getRiskConfidentialityWithControls());
            edge.setRiskIntegrityWithControls(link.getRiskIntegrityWithControls());
            edge.setRiskTreatment(link.getRiskTreatment());
        }
        return edge;
    }

    @Override
    public VeriniceGraph getGraph() {
        return graph;
    }

    public boolean isLoadLinks() {
        return loadLinks;
    }

    @Override
    public void setLoadLinks(boolean loadLinks) {
        this.loadLinks = loadLinks;
    }

    public String[] getRelationIds() {
        return (relationIds != null) ? relationIds.clone() : null;
    }

    @Override
    public void setRelationIds(String[] relationIds) {
        this.relationIds = (relationIds != null) ? relationIds.clone() : null;
    }
    
    public IBaseDao<CnATreeElement, Long> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    public void setCnaTreeElementDao(IBaseDao<CnATreeElement, Long> cnaTreeElementDao) {
        this.cnaTreeElementDao = cnaTreeElementDao;
    }
    
    public IBaseDao<CnALink, CnALink.Id> getCnaLinkDao() {
        return cnaLinkDao;
    }

    public void setCnaLinkDao(IBaseDao<CnALink, CnALink.Id> cnaLinkDao) {
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
    
    private void log() {  
        getGraph().log();        
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
