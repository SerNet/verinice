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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.IPropertyAdapter;
import sernet.verinice.service.linktable.PropertyAdapterFactory;
import sernet.verinice.service.linktable.generator.mergepath.Path;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

final class LtrPrintRowsTraversalListener implements sernet.verinice.interfaces.graph.TraversalListener {

    private static final Logger LOG = Logger.getLogger(LtrPrintRowsTraversalListener.class);

    private Queue<CnATreeElement> cnaTreeElementQueue = new LinkedList<>();
    private Map<CnATreeElement, Edge> incomingEdges = new HashMap<>();

    private Path p;
    final List<Map<String, String>> result = new ArrayList<>();
    private Set<String> columnHeader;

    LtrPrintRowsTraversalListener(Path p, Set<String> columnHeader) {
        this.p = p;
        this.columnHeader = columnHeader;
    }

    @Override
    public void nodeTraversed(CnATreeElement node, int depth) {

        LOG.debug("traversed node: " + node.getTitle() + ":" + node.getTypeId());

        cnaTreeElementQueue.add(node);

        if (isLeaf(node, depth)) {
            printRow();
        }
    }

    @Override
    public void nodeFinished(CnATreeElement node, int depth) {

        LOG.debug("finished node: " + node.getTitle() + ":" + node.getTypeId());

        cnaTreeElementQueue.remove(node);
        incomingEdges.remove(node);
    }

    private boolean isLeaf(CnATreeElement node, int depth) {
        return depth == p.getPathElements().size() - 1;
    }

    void printRow() {

        Map<String, String> row = initRow();
        Iterator<CnATreeElement> iterator = cnaTreeElementQueue.iterator();
        int i = 0;

        while (iterator.hasNext()) {

            CnATreeElement next = iterator.next();
            VqlNode pathElement = p.getPathElements().get(i).node;
            VqlEdge incomingEdge = p.getPathElements().get(i).edge;
            storeInRow(row, next, pathElement);
            storeInRow(row, next, incomingEdge);

            i++;
        }

        result.add(row);
    }

    private void storeInRow(Map<String, String> row, CnATreeElement node, VqlEdge incominVqlgEdge) {

        if (incominVqlgEdge != null && incominVqlgEdge.isMatch()) {

            Edge edge = incomingEdges.get(node);

            for (String propertyType : incominVqlgEdge.getPropertyTypes()) {

                String pathforProperty = incominVqlgEdge.getPathforProperty(propertyType);

                if ("I".equals(propertyType)) {
                    row.put(pathforProperty, String.valueOf(edge.getRiskIntegrity()));
                }

                else if ("C".equals(propertyType)) {
                    row.put(pathforProperty, String.valueOf(edge.getRiskConfidentiality()));
                }

                else if ("A".equals(propertyType)) {
                    row.put(pathforProperty, String.valueOf(edge.getRiskAvailability()));
                }

                else if ("description".equals(propertyType)) {
                    row.put(pathforProperty, edge.getDescription());
                }

                else if ("title".equals(propertyType)) {
                    row.put(pathforProperty, edge.getType());
                }

            }
        }
    }

    private void storeInRow(Map<String, String> row, CnATreeElement element, VqlNode pathElement) {
        if (pathElement.isMatch()) {
            for (String propertyType : pathElement.getPropertyTypes()) {
                IPropertyAdapter adapter = PropertyAdapterFactory.getAdapter(element);
                String propertyValue = adapter.getPropertyValue(propertyType);
                row.put(pathElement.getPathForProperty(propertyType), propertyValue);
                LOG.debug("Add row to result set: " + row);
            }
        }
    }

    private Map<String, String> initRow() {
        Map<String, String> row = new TreeMap<>(new NumericStringComparator());
        for (String id : columnHeader) {
            row.put(id, "");
        }
        return row;
    }

    @Override
    public void edgeTraversed(CnATreeElement source, CnATreeElement target, Edge edge, int depth) {

        LOG.debug("traversed edge: " + edge + " depth: " + depth);

        if (target != null) {
            incomingEdges.put(target, edge);
            LOG.debug("push edge with key: " + target + " -> " + edge);
        }
    }
}