/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IElementFilter;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.model.ICountElementService;

/**
 * Provides several {@link CnATreeElement} objects which are necessary for
 * rendering the main menu of verinice web.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "menuService")
@SessionScoped
public class MenuService implements Serializable {

    private static final Logger log = Logger.getLogger(MenuService.class);

    private List<ITVerbund> itNetworks;

    private List<Organization> organizations;

    private List<ControlGroup> catalogs;

    private Map<String, Boolean> typeIsAllMenuVisibleMap = new HashMap<>();

    /**
     * Returns a Set of all IT-Networks which the current logged in user is
     * allowed to see.
     *
     */
    public List<ITVerbund> getVisibleItNetworks() {

        if (itNetworks != null) {
            return itNetworks;
        }

        loadMenuData();
        return itNetworks;
    }

    /**
     * Returns a Set of all {@link Organization} which the current logged in
     * user is allowed to see.
     *
     */
    public List<Organization> getVisibleOrganisations() {

        if (organizations != null) {
            return organizations;
        }

        loadMenuData();
        return organizations;
    }

    public List<ControlGroup> getCatalogs() {

        if (catalogs != null) {
            return catalogs;
        }

        loadMenuData();
        return catalogs;
    }

    /**
     * Returns true if a menu entry with data of all objects of a
     * given type is visible.
     *
     * The return values is cached in a map.
     *
     * @param typeID
     *            An object type id
     * @return true if an a "All" menu entry is visible false if not
     */
    public boolean isAllMenuVisible(String typeID) {
        Boolean isAllMenuVisible = typeIsAllMenuVisibleMap.get(typeID);
        if (isAllMenuVisible == null) {
            isAllMenuVisible = loadAllMenuState(typeID);
            typeIsAllMenuVisibleMap.put(typeID, isAllMenuVisible);
        }
        return isAllMenuVisible;
    }

    /**
     * Loads the number of elements and a limit for a given type ID. If the
     * number is smaller or equal than the limit an "All" menu entry is visible.
     *
     * @param typeID
     *            An object type id
     * @return true if an a "All" menu entry is visible false if not
     */
    protected boolean loadAllMenuState(String typeID) {
        Map<String, Long> idNumberMap = getCountElementService().getNumberOfAllTypes();
        Integer typeLimit = getCountElementService().getLimit(typeID);
        boolean isAllMenuVisible = isAllMenuVisible(typeID, idNumberMap, typeLimit);
        if (log.isDebugEnabled()) {
            log.debug("All menu visible: " + isAllMenuVisible + " (number of " + typeID + ": "
                    + idNumberMap.get(typeID) + ", limit: " + typeLimit + ")");
        }
        return isAllMenuVisible;
    }

    /**
     * Check if the "All" menu should be visible in the webfrontend.
     *
     * @param typeID
     *            An object type id
     * @param idNumberMap
     *            the numberMap from the count service
     * @param typeLimit
     *            the limit of entries
     * @return true if the map not contains the type or when the number of
     *         entries is less than the given typeLimit or the typeLimit is less then zero
     */
    private boolean isAllMenuVisible(String typeID, Map<String, Long> idNumberMap, Integer typeLimit) {
        if (!idNumberMap.containsKey(typeID)) {
            return true;
        }
        return idNumberMap.get(typeID) <= typeLimit || typeLimit < 0;
    }

    /**
     * Get all {@link ControlGroup} which are tagged with the SNCA property
     * "samt_topic_is_catalog"
     */
    public void loadMenuData() {

        IGraphService graphService = getGraphService();
        graphService.setLoadLinks(false);
        GraphElementLoader graphElementLoader = new GraphElementLoader();
        graphElementLoader.setTypeIds(new String[] { ITVerbund.TYPE_ID, Organization.TYPE_ID, ControlGroup.TYPE_ID });
        graphElementLoader.setElementFilter(new IElementFilter() {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean check(CnATreeElement element) {

                if (element instanceof ControlGroup) {
                    ControlGroup controlGroup = (ControlGroup) element;
                    String property = controlGroup.getEntity().getPropertyValue(ControlGroup.PROP_IS_CATALOG);
                    return "1".equals(property);
                }

                return true;
            }
        });

        graphService.setLoader(graphElementLoader);
        VeriniceGraph veriniceGraph = graphService.create();

        itNetworks = sortByTitle(veriniceGraph.getElements(ITVerbund.class));
        organizations = sortByTitle(veriniceGraph.getElements(Organization.class));
        catalogs = sortByTitle(veriniceGraph.getElements(ControlGroup.class));
    }

    private <T extends CnATreeElement> List<T> sortByTitle(Set<T> cnATreeElements) {
        List<T> sortedItNetworks = new ArrayList<>();
        Collections.sort(sortedItNetworks, new Comparator<T>() {

            NumericStringComparator comp = new NumericStringComparator();

            @Override
            public int compare(T o1, T o2) {
                return comp.compare(o1.getTitle(), o2.getTitle());
            }
        });

        sortedItNetworks.addAll(cnATreeElements);
        return sortedItNetworks;
    }

    IGraphService getGraphService() {
        return (IGraphService) VeriniceContext.get(VeriniceContext.GRAPH_SERVICE);
    }

    ICountElementService getCountElementService() {
        return (ICountElementService) VeriniceContext.get(VeriniceContext.COUNT_SERVICE);
    }

}
