package sernet.verinice.service.test;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.FirstLinkedElementsLoader;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.DAOFactory;
import sernet.verinice.service.commands.RemoveElement;

public class FirstLinkedElementsLoaderTest extends AbstractModernizedBaseProtection {
    private static final Logger LOG = Logger.getLogger(FirstLinkedElementsLoaderTest.class);

    @Resource(name = "graphService")
    private IGraphService graphService;

    @Resource(name = "cnaTreeElementDao")
    private IBaseDao<CnATreeElement, Long> elementDao;

    @Resource(name = "daoFactory")
    private DAOFactory factory;
    private Set<CnATreeElement> elementsToClear = new HashSet<>();

    IBaseDao<CnALink, Serializable> linkDao;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        linkDao = factory.getDAO(CnALink.class);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        LOG.info("Delete Objects: " + elementsToClear);
        removeElements(elementsToClear);
    }

    @Test
    public void testSimpleLinkloading() throws CommandException {
        LOG.info("testLinkloading");
        ItNetwork itNetwork = createNewBPOrganization();
        ApplicationGroup applicationGroup = createGroup(itNetwork, ApplicationGroup.class);
        Application application = createElement(applicationGroup, Application.class);
        // add the it-network and the children of the applicationGroup
        int baseGraph = itNetwork.getChildren().size() + 2;

        ItNetwork itNetwork1 = createNewBPOrganization();
        ApplicationGroup applicationGroup1 = createGroup(itNetwork1, ApplicationGroup.class);
        Application application1 = createElement(applicationGroup1, Application.class);

        ItNetwork itNetwork2 = createNewBPOrganization();
        ApplicationGroup applicationGroup2 = createGroup(itNetwork2, ApplicationGroup.class);
        Application application2 = createElement(applicationGroup2, Application.class);

        elementsToClear.add(itNetwork);
        elementsToClear.add(itNetwork1);
        elementsToClear.add(itNetwork2);

        createLink(application, application1, null);
        createLink(application, application2, null);
        createLink(application1, application, null);

        VeriniceGraph veriniceGraph = createGraphWithLoader(new FirstLinkedElementsLoader(),
                itNetwork.getScopeId());

        Set<CnATreeElement> connectedSetOf = veriniceGraph.getElements();
        assertTrue(connectedSetOf.contains(itNetwork));
        assertTrue(connectedSetOf.contains(application1));
        assertTrue(connectedSetOf.contains(application2));
        assertEquals(baseGraph + 2, connectedSetOf.size());
    }

    @Test
    public void testSimpleLinkloadingOppositeDirection() throws CommandException {
        LOG.info("testSimpleLinkloadingOppositeDirection");
        ItNetwork itNetwork = createNewBPOrganization();
        ApplicationGroup applicationGroup = createGroup(itNetwork, ApplicationGroup.class);
        Application application = createElement(applicationGroup, Application.class);
        // add the it-network and the children of the applicationGroup
        int baseGraph = itNetwork.getChildren().size() + 2;

        ItNetwork itNetwork1 = createNewBPOrganization();
        ApplicationGroup applicationGroup1 = createGroup(itNetwork1, ApplicationGroup.class);
        Application application1 = createElement(applicationGroup1, Application.class);

        ItNetwork itNetwork2 = createNewBPOrganization();
        ApplicationGroup applicationGroup2 = createGroup(itNetwork2, ApplicationGroup.class);
        Application application2 = createElement(applicationGroup2, Application.class);

        elementsToClear.add(itNetwork);
        elementsToClear.add(itNetwork1);
        elementsToClear.add(itNetwork2);

        createLink(application, application1, null);
        createLink(application2, application, null);
        createLink(application1, application, null);

        VeriniceGraph veriniceGraph = createGraphWithLoader(new FirstLinkedElementsLoader(),
                itNetwork.getScopeId());

        Set<CnATreeElement> connectedSetOf = veriniceGraph.getElements();
        assertTrue(connectedSetOf.contains(itNetwork));
        assertTrue(connectedSetOf.contains(application1));
        assertTrue(connectedSetOf.contains(application2));
        assertEquals(baseGraph + 2, connectedSetOf.size());
    }

    @Test
    public void testLinkloading() throws CommandException {
        LOG.info("testLinkloading");
        ItNetwork itNetwork = createNewBPOrganization();
        ApplicationGroup applicationGroup = createGroup(itNetwork, ApplicationGroup.class);
        Application application = createElement(applicationGroup, Application.class);
        // add the it-network and the children of the applicationGroup
        int baseGraph = itNetwork.getChildren().size() + 2;

        ItNetwork itNetwork1 = createNewBPOrganization();
        ApplicationGroup applicationGroup1 = createGroup(itNetwork1, ApplicationGroup.class);
        Application application1 = createElement(applicationGroup1, Application.class);

        ItNetwork itNetwork2 = createNewBPOrganization();
        ApplicationGroup applicationGroup2 = createGroup(itNetwork2, ApplicationGroup.class);
        Application application2 = createElement(applicationGroup2, Application.class);

        ItNetwork itNetwork3 = createNewBPOrganization();
        ApplicationGroup applicationGroup3 = createGroup(itNetwork3, ApplicationGroup.class);
        Application application3 = createElement(applicationGroup3, Application.class);
        elementsToClear.add(itNetwork);
        elementsToClear.add(itNetwork1);
        elementsToClear.add(itNetwork2);
        elementsToClear.add(itNetwork3);

        createLink(application, application1, null);
        createLink(application, application2, null);
        createLink(application2, application3, null);
        createLink(application1, application, null);

        VeriniceGraph veriniceGraph = createGraphWithLoader(new FirstLinkedElementsLoader(),
                itNetwork.getScopeId());

        Set<CnATreeElement> connectedSetOf = veriniceGraph.getElements();
        assertTrue(connectedSetOf.contains(itNetwork));
        assertTrue(connectedSetOf.contains(application1));
        assertTrue(connectedSetOf.contains(application2));
        assertFalse(connectedSetOf.contains(application3));
        assertEquals(baseGraph + 2, connectedSetOf.size());
    }

    @Test
    public void testLinkloadingTwoScopes() throws CommandException {
        LOG.info("testLinkloadingTwoScopes");
        ItNetwork itNetwork = createNewBPOrganization();
        ApplicationGroup applicationGroup = createGroup(itNetwork, ApplicationGroup.class);
        Application application = createElement(applicationGroup, Application.class);
        // add the it-network and the children of the applicationGroup
        int baseGraph = itNetwork.getChildren().size() + 2;

        ItNetwork itNetwork1 = createNewBPOrganization();
        ApplicationGroup applicationGroup1 = createGroup(itNetwork1, ApplicationGroup.class);
        Application application1 = createElement(applicationGroup1, Application.class);
        baseGraph += itNetwork1.getChildren().size() + 2;

        ItNetwork itNetwork2 = createNewBPOrganization();
        ApplicationGroup applicationGroup2 = createGroup(itNetwork2, ApplicationGroup.class);
        Application application2 = createElement(applicationGroup2, Application.class);

        ItNetwork itNetwork3 = createNewBPOrganization();
        ApplicationGroup applicationGroup3 = createGroup(itNetwork3, ApplicationGroup.class);
        Application application3 = createElement(applicationGroup3, Application.class);
        elementsToClear.add(itNetwork);
        elementsToClear.add(itNetwork1);
        elementsToClear.add(itNetwork2);
        elementsToClear.add(itNetwork3);

        createLink(application, application1, null);
        createLink(application1, application2, null);
        createLink(application2, application3, null);

        VeriniceGraph veriniceGraph = createGraphWithLoader(new FirstLinkedElementsLoader(),
                itNetwork.getScopeId(), itNetwork1.getScopeId());

        Set<CnATreeElement> connectedSetOf = veriniceGraph.getElements();
        assertTrue(connectedSetOf.contains(itNetwork));
        assertTrue(connectedSetOf.contains(application1));
        assertTrue(connectedSetOf.contains(application2));
        assertFalse(connectedSetOf.contains(application3));
        assertEquals(baseGraph + 1, connectedSetOf.size());
    }

    private VeriniceGraph createGraphWithLoader(GraphElementLoader additionLoader,
            Integer... scopeIds) {
        GraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setScopeIds(scopeIds);
        additionLoader.setScopeIds(scopeIds);

        long startTime = System.currentTimeMillis();
        VeriniceGraph veriniceGraph = graphService
                .createDirectedGraph(Arrays.asList(graphElementLoader, additionLoader));
        LOG.info("Graph size: " + veriniceGraph.getElements().size());
        long timeNeeded = System.currentTimeMillis() - startTime;
        LOG.info("--->" + timeNeeded);
        return veriniceGraph;
    }

    private List<CnATreeElement> removeElements(Set<CnATreeElement> elementsToClear2)
            throws CommandException {
        RemoveElement<CnATreeElement> removeElement = new RemoveElement<CnATreeElement>(
                elementsToClear2);
        removeElement = commandService.executeCommand(removeElement);
        return removeElement.getChangedElements();
    }

}
