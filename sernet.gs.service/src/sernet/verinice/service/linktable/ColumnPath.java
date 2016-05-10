/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.verinice.service.linktable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * A ColumnPath is a description for a report column in
 * LinkTableDataModel. See LinkTableDataModel for a description of column path definitions.
 *
 * @see LinkTableDataModel
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ColumnPath {

    private String columnString;
    private int number;

    private List<IPathElement> pathElements;

    public ColumnPath(int n, String columnString) {
        super();
        number = n;
        this.columnString = columnString;
        pathElements = new LinkedList<>();
    }

    public void setPathElements(IPathElement element) {
        addPathElements(element);
    }

    private void addPathElements(IPathElement element) {
        if(element!=null) {
            pathElements.add(element);
            addPathElements(element.getChild());
        }
    }

    /**
     * @param graph
     */
    public void load(VeriniceGraph graph) {
        pathElements.get(0).load(null, graph);
    }

    public Map<String, Map<String, Object>> getResult() {
        return pathElements.get(0).getResult();
    }

    public Map<String, String> createResultMap() {
        return pathElements.get(0).addResultToMap(new HashMap<String, String>(),null);
    }

    public int getNumber() {
        return number;
    }


    public void setNumber(int number) {
        this.number = number;
    }

    public List<IPathElement> getPathElements() {
        return pathElements;
    }

    public String getColumnString() {
        return columnString;
    }


}
