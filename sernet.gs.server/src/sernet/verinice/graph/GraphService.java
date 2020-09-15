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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
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
 * A service to load and analyse the element network of verinice with JGraphT.
 * 
 * JGraphT is a free Java graph library that provides mathematical graph-theory
 * objects and algorithms.
 * 
 * Use {@link IGraphElementLoader} to configure which elements are loaded by the
 * service.
 * 
 * To configure link types use method: setRelationIds(String[] relationIds).
 * 
 * You have to call "create()" to initialize the service. After that you can
 * start to use the service.
 * 
 * @see http://jgrapht.org/
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphService implements IGraphService, Serializable {

    private static final Logger LOG = Logger.getLogger(GraphService.class);
    private static final Logger LOG_RUNTIME = Logger
            .getLogger(GraphService.class.getName() + ".runtime");

    private IBaseDao<CnATreeElement, Long> cnaTreeElementDao;

    private IBaseDao<CnALink, CnALink.Id> cnaLinkDao;

    @Override
    public VeriniceGraph create(List<? extends IGraphElementLoader> loaderList) {
        return create(loaderList, true);
    }

    @Override
    public VeriniceGraph create(List<? extends IGraphElementLoader> loaderList, boolean loadLinks) {
        return create(loaderList, null, loadLinks);
    }

    @Override
    public VeriniceGraph create(List<? extends IGraphElementLoader> loaderList,
            String[] relationIds) {
        return create(loaderList, relationIds, true);
    }

    @Override
    public VeriniceGraph create(List<? extends IGraphElementLoader> loaderList,
            String[] relationIds, boolean loadLinks) {
        VeriniceGraph graph = new UndirectedVeriniceGraph();
        doCreate(graph, loaderList, relationIds, loadLinks);
        return graph;
    }

    @Override
    public VeriniceGraph createDirectedGraph(List<? extends IGraphElementLoader> loaderList) {
        return createDirectedGraph(loaderList, true);
    }

    @Override
    public VeriniceGraph createDirectedGraph(List<? extends IGraphElementLoader> loaderList,
            boolean loadLinks) {
        return createDirectedGraph(loaderList, null, loadLinks);
    }

    @Override
    public VeriniceGraph createDirectedGraph(List<? extends IGraphElementLoader> loaderList,
            String[] relationIds, boolean loadLinks) {
        VeriniceGraph graph = new DirectedVeriniceGraph();

        doCreate(graph, loaderList, relationIds, loadLinks);
        return graph;
    }

    private void doCreate(VeriniceGraph graph, List<? extends IGraphElementLoader> loaderList,
            String[] relationIds, boolean loadLinks) {
        long time = initRuntime();
        Map<Integer, CnATreeElement> elementsByDBId = loadVerticesAndRelatives(graph, loaderList);
        if (loadLinks) {
            if (!elementsByDBId.isEmpty()) {
                loadLinks(graph, relationIds, elementsByDBId);
            }
        } else {
            LOG.info("Loading of links is disabled.");
        }
        graph.log();
        logRuntime("Graph generation runtime: ", time);
    }

    /**
     * Loads all vertices and adds them to the graph. An edge for each children
     * is added if the child is part of the graph.
     */
    private Map<Integer, CnATreeElement> loadVerticesAndRelatives(VeriniceGraph graph,
            List<? extends IGraphElementLoader> loaderList) {
        List<CnATreeElement> elementList = new LinkedList<>();
        for (IGraphElementLoader loader : loaderList) {
            loader.setCnaTreeElementDao(getCnaTreeElementDao());
            elementList.addAll(loader.loadElements());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(elementList.size() + " relevant elements found");
        }
        Map<Integer, CnATreeElement> elementsByDBId = new HashMap<>(elementList.size());
        for (CnATreeElement element : elementList) {
            graph.addVertex(element);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Vertex added: " + element.getTitle() + " [" + element.getTypeId() + "]");
            }
            elementsByDBId.put(element.getDbId(), element);
        }
        for (CnATreeElement parent : elementList) {
            Set<CnATreeElement> children = parent.getChildren();
            for (CnATreeElement child : children) {
                createParentChildEdge(parent, child, graph, elementsByDBId);
            }
        }
        return elementsByDBId;
    }

    protected void createParentChildEdge(CnATreeElement parent, CnATreeElement child,
            VeriniceGraph graph, Map<Integer, CnATreeElement> elementsByDBId) {
        CnATreeElement childWithProperties = elementsByDBId.get(child.getDbId());
        if (childWithProperties != null) {
            graph.addEdge(new Edge(parent, childWithProperties));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Edge added: " + parent.getTitle() + " - "
                        + childWithProperties.getTitle() + ", relatives");
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No Edge added, child was not found. Child type / uuid: " + child.getTypeId()
                    + " / " + child.getUuid() + ", Parent is: " + parent.getTitle());
        }
    }

    private void loadLinks(VeriniceGraph graph, String[] relationIds,
            Map<Integer, CnATreeElement> elementsByDBId) {
        long time = initRuntime();
        DetachedCriteria linkCrit = DetachedCriteria.forClass(CnALink.class);
        addLinkRestriction(linkCrit, elementsByDBId);
        if (relationIds != null && relationIds.length > 0) {
            linkCrit.add(Restrictions.in("id.typeId", relationIds));
        }
        linkCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        @SuppressWarnings("unchecked")
        List<CnALink> linkList = getCnaLinkDao().findByCriteria(linkCrit);
        if (LOG.isInfoEnabled()) {
            LOG.info(linkList.size() + " relevant links found");
        }
        for (CnALink link : linkList) {
            CnATreeElement source = elementsByDBId.get(link.getId().getDependantId());
            CnATreeElement target = elementsByDBId.get(link.getId().getDependencyId());
            Edge edge = createEdge(link, source, target);
            if (edge != null) {

                graph.addEdge(edge);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Edge added: " + source.getTitle() + " <-> " + target.getTitle()
                            + " [" + link.getRelationId() + "]");
                }
            }
        }
        logRuntime("Load links runtime: ", time);
    }

    public void addLinkRestriction(DetachedCriteria linkCrit,
            Map<Integer, CnATreeElement> elementsByDBId) {
        if (elementsByDBId.size() <= 1000) {
            Set<Integer> elementDbIds = elementsByDBId.keySet();
            linkCrit.add(Restrictions.and(Restrictions.in("id.dependantId", elementDbIds),
                    Restrictions.in("id.dependencyId", elementDbIds)));
        } else {
            Set<Integer> scopeIds = elementsByDBId.values().stream().map(CnATreeElement::getScopeId)
                    .collect(Collectors.toSet());
            linkCrit.createAlias("dependant", "dependant").createAlias("dependency", "dependency")
                    .add(Restrictions.and(Restrictions.in("dependant.scopeId", scopeIds),
                            Restrictions.in("dependency.scopeId", scopeIds)));
        }
    }

    private Edge createEdge(CnALink link, CnATreeElement source, CnATreeElement target) {
        Edge edge = null;
        if (source != null && target != null) {
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

    private long initRuntime() {
        long time = 0;
        if (LOG_RUNTIME.isDebugEnabled()) {
            time = System.currentTimeMillis();
        }
        return time;
    }

    private void logRuntime(String message, long starttime) {
        LOG_RUNTIME.debug(message
                + TimeFormatter.getHumanRedableTime(System.currentTimeMillis() - starttime));
    }

}
