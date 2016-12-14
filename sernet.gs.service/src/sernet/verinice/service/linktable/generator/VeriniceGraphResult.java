/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Wrapper for the rows built by {@link LtrPrintRowsTraversalListener}.
 * 
 * This wrapper stores all the results from one traversal for a given root. Thus
 * it decides wether it duplicates lines or add values to the current row.
 *
 * 1. If the algorithm steps deeper into the graph, the value is always add to
 * an existing row.
 *
 * 2. If the algorithm is finished with a node and steps up, it duplicates the
 * last row und set this as current row.
 *
 * 3. If the algorithm steps down again check, if the current row is add to the
 * final result row and start with step 1.
 *
 *
 *
 * @author Ruth Motza <rm[at]sernet[dot]de>
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
final class VeriniceGraphResult {

    private List<VeriniceGraphResultRow> linkedTableRows;

    private VeriniceGraphResultRow currentLinkedTableRow;

    private boolean justDuplicated;

    private Deque<VeriniceGraphResultEntry> entryStack = new LinkedList<>();

    VeriniceGraphResult() {
        linkedTableRows = new ArrayList<>();
        currentLinkedTableRow = new VeriniceGraphResultRow();
        linkedTableRows.add(currentLinkedTableRow);
    }

    void addValue(VqlNode vqlNode, VqlEdge vqlEdge, Edge edge, CnATreeElement element, int depth) {

        VeriniceGraphResultEntry veriniceGraphResultEntry = new VeriniceGraphResultEntry(vqlNode, vqlEdge, edge, element, depth);

        currentLinkedTableRow.addEntry(veriniceGraphResultEntry);

        if (!linkedTableRows.contains(currentLinkedTableRow)) {
            linkedTableRows.add(currentLinkedTableRow);
        }

        entryStack.addFirst(veriniceGraphResultEntry);
        justDuplicated = false;
    }

    void removeValue() {

        if (entryStack.isEmpty()) {
            return;
        }

        VeriniceGraphResultEntry pop = entryStack.removeFirst();

        if (!justDuplicated && !pop.isParentRelation()) {
            currentLinkedTableRow = currentLinkedTableRow.duplicate();
            justDuplicated = true;
        }

        if (!pop.isParentRelation()) {
            currentLinkedTableRow.removeEntry(pop);
        }
    }

    List<Map<String, String>> getResult() {

        List<Map<String, String>> rows = new ArrayList<>();

        for (VeriniceGraphResultRow row : linkedTableRows) {
            rows.add(row.getExpandedRow());
        }
        return rows;
    }
}
