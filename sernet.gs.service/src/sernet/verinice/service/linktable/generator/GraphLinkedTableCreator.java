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
package sernet.verinice.service.linktable.generator;

import static sernet.verinice.interfaces.graph.DepthFirstConditionalSearchPathes.traverse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.DepthFirstConditionalSearchPathes;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.interfaces.graph.VeriniceGraphFilter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkedTableCreator;
import sernet.verinice.service.linktable.RowComparator;
import sernet.verinice.service.linktable.generator.mergepath.VqlAst;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Creates a linked table for vql queries.
 *
 * <p>
 * It uses a depth first traversal on {@link VeriniceGraph}. The algorithm is
 * divisible in 4 steps.
 * </p>
 *
 * <ul>
 * <li>1. Merge all column paths to a AST ({@link VqlAst}).</li>
 * <li>2. Find all possible potential points for a traversal.</li>
 * <li>3. Iterate over potential starting points and filter all matching paths
 * with the help of {@link VqlContext}, which walks through a {@link VqlAst}
 * data structure.</li>
 * </ul>
 *
 * <p>
 * All the steps in detail:
 * </p>
 *
 * <h2>Potential starting points</h2>
 *
 * <p>
 * Every {@link VqlAst} has a root, because it is a tree. This root node has an
 * specific type id. The verinice graph is filtered for all nodes which have
 * this type id.
 * </p>
 *
 * <h2>Traversal</h2>
 *
 * <p>
 * After extracting paths and starting nodes we start a
 * {@link DepthFirstConditionalSearchPathes} traversal on the verinice graph.
 * The {@link VqlContext} is used to determine if edges and nodes in the
 * verinice graph are valid.
 * </p>
 *
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GraphLinkedTableCreator implements LinkedTableCreator {

    private VqlAst vqlAst;
    private VeriniceGraph veriniceDataGraph;
    private Map<String, String> columnHeader2Alias;
    private Map<String, Integer> columnPath2TablePosition;

    private static final Logger LOG = Logger.getLogger(GraphLinkedTableCreator.class);
    private LtrPrintRowsTraversalListener traversalListener;
    private LtrTraversalFilter filter;

    @Override
    public List<List<String>> createTable(VeriniceGraph veriniceGraph, ILinkTableConfiguration conf) {

        this.veriniceDataGraph = veriniceGraph;
        this.vqlAst = new VqlAst(conf);

        VqlNode root = vqlAst.getRoot();
        String typeId = root.getPath();

        storeColumnHeaderOrderAndAlias(conf);
        Set<CnATreeElement> roots = getRootNodes(typeId);

        List<Map<String, String>> table = doCreateTable(roots);

        return convertToTable(table);
    }

    private Set<CnATreeElement> getRootNodes(final String typeId) {
        return veriniceDataGraph.filter(new VeriniceGraphFilter() {
            @Override
            public boolean filter(CnATreeElement node) {
                return typeId.equals(node.getTypeId());
            }
        });
    }

    private List<Map<String, String>> doCreateTable(Set<CnATreeElement> roots) {
        List<Map<String, String>> table = new ArrayList<>();
        for (CnATreeElement potentialRoot : roots) {
            VeriniceGraphResult scanVeriniceGraph = scanVeriniceGraph(potentialRoot);
            table.addAll(scanVeriniceGraph.getResult());
        }
        return table;
    }

    private VeriniceGraphResult scanVeriniceGraph(CnATreeElement potentialRoot) {

        VqlContext vqlNavigator = new VqlContext(vqlAst);
        filter = new LtrTraversalFilter(vqlNavigator);
        VeriniceGraphResult result = new VeriniceGraphResult();
        traversalListener = new LtrPrintRowsTraversalListener(vqlNavigator, filter, veriniceDataGraph, result);

        traverse(veriniceDataGraph, potentialRoot, filter, traversalListener);
        return traversalListener.getResult();
    }

    private List<List<String>> convertToTable(List<Map<String, String>> table) {

        List<List<String>> stringTable = new LinkedList<>();

        for (Map<String, String> map : table) {

            String[] row = new String[columnPath2TablePosition.size()];

            for(Entry<String, Integer> pos : columnPath2TablePosition.entrySet()){
                row[pos.getValue()] = map.containsKey(pos.getKey()) ? map.get(pos.getKey()) : "";
            }


            stringTable.add(Arrays.asList(row));

            if(LOG.isDebugEnabled()) {
                LOG.debug("Add row to link table: [" + StringUtils.join(row, ", ") + "]");
            }
        }

        Collections.sort(stringTable, new RowComparator());
        stringTable.add(0, getAliasHeader());
        return stringTable;
    }

    private void storeColumnHeaderOrderAndAlias(ILinkTableConfiguration conf) {
        int position = 0;
        columnHeader2Alias = new HashMap<>();
        columnPath2TablePosition = new HashMap<>();
        for (String s : conf.getColumnPaths()) {
            List<String> columnPathAsList = ColumnPathParser.getColumnPathAsList(s);
            List<String> removeAlias = ColumnPathParser.removeAlias(columnPathAsList);
            String join = StringUtils.join(removeAlias, "");
            columnPath2TablePosition.put(join, position);
            columnHeader2Alias.put(join, ColumnPathParser.extractAlias(s));
            position++;
        }
    }

    private List<String> getAliasHeader() {

        // replaces column pathes with aliases
        String[] aliasHeader = new String[columnHeader2Alias.size()];
        for (Map.Entry<String, String> e : columnHeader2Alias.entrySet()) {
            int position = columnPath2TablePosition.get(e.getKey());
            aliasHeader[position] = e.getValue().equals(StringUtils.EMPTY) ? e.getKey() : e.getValue();
        }

        return Arrays.asList(aliasHeader);
    }

}
