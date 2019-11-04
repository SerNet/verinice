/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
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
 *     Urs Zeidler - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.interfaces.oda.IChainableFilter;
import sernet.verinice.interfaces.oda.IFilteringCommand;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Process;

/**
 * Loads all related assets to processes.
 * 
 * Returns two dimensional data structure of objects' DB-Ids as
 * 
 * <code>List&lt;List&lt;String&gt;&gt;</code>.
 * 
 * For instance:
 * <table border="1">
 * <tr>
 * <td>Process ID</td>
 * <td>Asset ID</td>
 * </tr>
 * <tr>
 * <td>42</td>
 * <td>23</td>
 * </tr>
 * <tr>
 * <td>43</td>
 * <td>24</td>
 * </tr>
 * <tr>
 * <td>44</td>
 * <td>25</td>
 * </tr>
 * </tr>
 * </table>
 */
public class LoadProcessAndAsset extends GraphCommand implements IFilteringCommand {
    private static final long serialVersionUID = 3972535701538215222L;
    private static final String REL_ASSET_ASSET = "rel_asset_asset";
    private static final String REL_PROCESS_ASSET = "rel_process_asset";
    private List<List<String>> commandResult;
    private IChainableFilter resultsFilter;
    private boolean isFilterActive;

    public LoadProcessAndAsset(int rootId) {
        super();

        GraphElementLoader processLoader = new GraphElementLoader();
        processLoader.setScopeId(rootId);
        processLoader.setTypeIds(new String[] { Process.TYPE_ID, Asset.TYPE_ID });
        addLoader(processLoader);

        
        addRelationId(REL_ASSET_ASSET);
        addRelationId(REL_PROCESS_ASSET);

        this.isFilterActive = false;
    }

    @Override
    public void executeWithGraph() {
        VeriniceGraph processGraph = getGraph();
        Set<CnATreeElement> processes = processGraph.getElements(Process.TYPE_ID);

        List<List<String>> result = new LinkedList<>();
        for (CnATreeElement process : processes) {
            // skip this process if a filter is active and it doesn't match:
            if (this.isFilterActive && !this.resultsFilter.matches(process.getEntity()))
                continue;

            Set<CnATreeElement> linkTargets = processGraph.getLinkTargets(process);
            Set<CnATreeElement> assetPerProcess = new HashSet<>();
            for (CnATreeElement cnATreeElement : linkTargets) {
                addAllRequiredAssets(processGraph, cnATreeElement, assetPerProcess);
            }
            assetPerProcess.stream().forEach(a -> result
                    .add(Arrays.asList(process.getDbId().toString(), a.getDbId().toString())));
        }
        commandResult = result;
    }

    private void addAllRequiredAssets(VeriniceGraph processGraph, CnATreeElement cnATreeElement,
            Set<CnATreeElement> assetPerProcess) {
        if (!Asset.TYPE_ID.equals(cnATreeElement.getTypeId())
                || assetPerProcess.contains(cnATreeElement)) {
            return;
        }
        assetPerProcess.add(cnATreeElement);
        Set<CnALink> linksDown = cnATreeElement.getLinksDown();
        Set<CnATreeElement> linkTargets = processGraph.getLinkTargets(cnATreeElement);
        Set<Integer> linkedDependencies = linksDown.stream().map(CnALink::getDependency)
                .map(CnATreeElement::getDbId).collect(Collectors.toSet());
        for (CnATreeElement asset : linkTargets) {
            if (linkedDependencies.contains(asset.getDbId())) {
                addAllRequiredAssets(processGraph, asset, assetPerProcess);
            }
        }
    }

    public List<List<String>> getElements() {
        return commandResult;
    }

    /**
     * Notice: the filter set here will only be applied to the processes in the
     * result set. Filtering ALL loaded objects is currently not a requirement
     * for this command.
     */
    @Override
    public void setFilterCriteria(IChainableFilter filter) {
        this.resultsFilter = filter;
        this.isFilterActive = Optional.ofNullable(filter).isPresent();
    }

    @Override
    public boolean isFilterActive() {
        return this.isFilterActive;
    }
}
