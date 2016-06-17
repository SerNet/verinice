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

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.interfaces.graph.VeriniceGraphFilter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkedTableCreator;
import sernet.verinice.service.linktable.RowComparator;
import sernet.verinice.service.linktable.generator.mergepath.Path;
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
 * <li>1. Find all possible potential points for a traversal.</li>
 * <li>2. Merge all column pathes to a AST ({@link VqlAst}).</li>
 * <li>3. Extract all possible search pathes from the AST.</li>
 * <li>4. Iterate over potential starting points and filter all matching pathes
 * with the help of the search pathes from step 3.</li>
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GraphLinkedTableCreator implements LinkedTableCreator {

    private VqlAst ast;
    private VeriniceGraph graph;
    private Map<String, String> columnHeader2Alias;
    private Map<String, Integer> columnPath2TablePosition;

    private static final Logger LOG = Logger.getLogger(GraphLinkedTableCreator.class);
    private LtrPrintRowsTraversalListener traversalListener;
    private LtrTraversalFilter filter;

    @Override
    public List<List<String>> createTable(VeriniceGraph veriniceGraph, ILinkTableConfiguration conf) {

        this.ast = new VqlAst(conf);
        this.graph = veriniceGraph;
        VqlNode root = ast.getRoot();
        final String typeId = root.getPath();

        storeColumnHeaderOrderAndAlias(conf);

        Set<CnATreeElement> roots = filterRootNodes(typeId);

        List<Map<String, String>> table = createLTRTable(roots);

        return convertToTable(table);
    }


    private Set<CnATreeElement> filterRootNodes(final String typeId) {
        Set<CnATreeElement> roots = graph.filter(new VeriniceGraphFilter() {
            @Override
            public boolean filter(CnATreeElement node) {
                return typeId.equals(node.getTypeId());
            }
        });
        return roots;
    }

    private List<Map<String, String>> createLTRTable(Set<CnATreeElement> roots) {
        List<Map<String, String>> table = new ArrayList<>();
        for (CnATreeElement potentialRoot : roots) {
            for (Path p : ast.getPaths()) {                
                List<Map<String, String>> rows = scanVeriniceGraph(potentialRoot, p);
                table.addAll(rows);
            }
        }
        return table;
    }



    private List<Map<String, String>> scanVeriniceGraph(CnATreeElement potentialRoot, final Path p) {

        traversalListener = new LtrPrintRowsTraversalListener(p, getLTRHeaderColumnPathes());
        filter = new LtrTraversalFilter(p);

        traverse(graph, potentialRoot, filter, traversalListener);
        return traversalListener.getResult();
    }

    private List<List<String>> convertToTable(List<Map<String, String>> table) {

        List<List<String>> stringTable = new LinkedList<>();

        for (Map<String, String> map : table) {

            String[] row = new String[map.size()];

            for(Entry<String, String> e : map.entrySet()){
                int position = columnPath2TablePosition.get(e.getKey());
                row[position] = e.getValue();
            }

            stringTable.add(Arrays.asList(row));
            LOG.debug("Add row to link table: " + row);
        }

        Collections.sort(stringTable, new RowComparator());
        stringTable.add(0, getAliasHeader());
        return stringTable;
    }

    private void storeColumnHeaderOrderAndAlias(ILinkTableConfiguration conf) {
        int position = 0;
        columnHeader2Alias = new HashMap<>();
        columnPath2TablePosition = new HashMap<>();
        for(String s : conf.getColumnPathes()){
           List<String> columnPathAsList = ColumnPathParser.getColumnPathAsList(s);
           List<String> removeAlias = ColumnPathParser.removeAlias(columnPathAsList);
           String join = StringUtils.join(removeAlias, "");
           columnPath2TablePosition.put(join, position);
           columnHeader2Alias.put(join, ColumnPathParser.extractAlias(columnPathAsList));
           position++;
        }
    }

    private Set<String> getLTRHeaderColumnPathes() {
        return columnHeader2Alias.keySet();
    }


    private List<String> getAliasHeader() {

        // replaces column pathes with aliases
        String[] aliasHeader = new String[columnHeader2Alias.size()];
        for(Map.Entry<String, String> e : columnHeader2Alias.entrySet()){
            int position = columnPath2TablePosition.get(e.getKey());
            aliasHeader[position] = (e.getValue().equals(StringUtils.EMPTY) ? e.getKey() : e.getValue());

        }

        return Arrays.asList(aliasHeader);
    }


}
