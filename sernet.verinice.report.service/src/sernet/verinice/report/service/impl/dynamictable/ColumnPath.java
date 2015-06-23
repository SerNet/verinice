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
package sernet.verinice.report.service.impl.dynamictable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * A ColumnPath is a description for a report column in 
 * GenericDataModel. See GenericDataModel for a description of column path definitions.
 * 
 * @see GenericDataModel
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
        pathElements = new LinkedList<IPathElement>();
        parseColumnString();
    }

    private void parseColumnString() {
        StringTokenizer st = new StringTokenizer(
                columnString, 
                new String(new char[]{IPathElement.DELIMITER_LINK,IPathElement.DELIMITER_LINK_TYPE,IPathElement.DELIMITER_CHILD,IPathElement.DELIMITER_PARENT,IPathElement.DELIMITER_PROPERTY}),
                true);
        if(st.hasMoreTokens()) {
            IPathElement  parentPathElement = new RootElement(st.nextToken());
            pathElements.add(parentPathElement);
            while (st.hasMoreElements()) {
                parentPathElement = createPathElement(st, parentPathElement);
            }
            
        }
    }

    private IPathElement createPathElement(StringTokenizer st, IPathElement parentPathElement) {
        String delimiter = st.nextToken();
        IPathElement element = PathElementFactory.getElement(delimiter);
        if(element==null) {
            throw new ColumnPathParseException("Error in column path: " + columnString);
        }
        if(st.hasMoreTokens()) {
            element.setTypeId(st.nextToken());
        } else {
            throw new ColumnPathParseException("Error in column path: " + columnString);
        }
        pathElements.add(element);
        parentPathElement.setChild(element);
        return element;
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
    
    public Map<String, String> getValueMap() {
        return pathElements.get(0).createResultMap(new HashMap<String, String>(),null);
    }
    
    public int getNumber() {
        return number;
    }


    public void setNumber(int number) {
        this.number = number;
    }
    
    
}
