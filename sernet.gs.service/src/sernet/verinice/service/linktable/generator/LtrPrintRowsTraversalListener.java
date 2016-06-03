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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.IPropertyAdapter;
import sernet.verinice.service.linktable.PropertyAdapterFactory;
import sernet.verinice.service.linktable.generator.mergepath.Path;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

final class LtrPrintRowsTraversalListener implements sernet.verinice.interfaces.graph.TraversalListener {


    private static final Logger LOG = Logger.getLogger(LtrPrintRowsTraversalListener.class);
    
    private Queue<CnATreeElement> queue = new LinkedList<>();
    private Path p;
    final List<Map<String, String>> result = new ArrayList<>();
    private Set<String> columnHeader;

    LtrPrintRowsTraversalListener(Path p, Set<String> columnHeader) {
        this.p = p;
        this.columnHeader = columnHeader;
        
        LOG.debug("traverse path: " + p);
    }

    @Override
    public void nodeTraversed(CnATreeElement node, int depth) {

        LOG.debug("traversed node: " + node.getTitle() + ":" + node.getTypeId());
        
        queue.add(node);
        
        if (isLeaf(node, depth)) {
            printRow();
        }
    }

    @Override
    public void nodeFinished(CnATreeElement node, int depth) {
        
        LOG.debug("finished node: " + node.getTypeId());
        
        queue.remove(node);
    }

    
    private boolean isLeaf(CnATreeElement node, int depth) {
        return depth == p.getPathElements().size() - 1;
    }

    void printRow() {
        Map<String, String> row = initRow();
        Iterator<CnATreeElement> iterator = queue.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            CnATreeElement next = iterator.next();
            VqlNode pathElement = (VqlNode) p.getPathElements().get(i);
            storeInRow(row, next, pathElement);
            i++;
        }
        
        result.add(row);
    }

    private void storeInRow(Map<String, String> row, CnATreeElement element, VqlNode pathElement) {
        if (pathElement.isMatch()) {
            for (String propertyType : pathElement.getPropertyTypes()) {
                IPropertyAdapter adapter = PropertyAdapterFactory.getAdapter(element);
                String propertyValue = adapter.getPropertyValue(propertyType);
                row.put(pathElement.getPathForProperty(propertyType), propertyValue);
            }
        }
        
        LOG.debug("Add row to result set: " + row);
        
    }

    private Map<String, String> initRow() {
        Map<String, String> row = new TreeMap<>(new NumericStringComparator());
        for (String id : columnHeader) {
            row.put(id, "");
        }
        return row;
    }
}