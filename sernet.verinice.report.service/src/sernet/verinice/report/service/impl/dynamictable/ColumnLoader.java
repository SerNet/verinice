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
import java.util.Map;
import java.util.SortedMap;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ColumnLoader {

    private int number;
    private ColumnPath columnPath;

    public ColumnLoader(int n, String columnString) {
        super();
        number = n;
        this.columnPath = new ColumnPath(columnString);
    }

    public void load(VeriniceGraph graph) {
        columnPath.load(graph);
    }
    
    public Map<String, Map<String, Object>> getResult() {
        return columnPath.getResult();
    }


    public int getNumber() {
        return number;
    }


    public void setNumber(int number) {
        this.number = number;
    }
    
    public Map<String, String> getValueMap() {
        return columnPath.getValueMap();
    }
    
}
