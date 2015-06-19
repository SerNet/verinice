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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GenericDataModel {

    static final String COLUMN_SEPERATOR = "#";

    private static final Logger LOG = Logger.getLogger(GenericDataModel.class);
    
    private VeriniceGraph graph;
    private String[] columnStrings;
    private List<ColumnLoader> columnLoaders;
    private List<List<String>> resultTable;
    
    private Map<String, List<String[]>> rowMap;
    
    public GenericDataModel(VeriniceGraph graph, String[] columnStrings) {
        super();
        this.graph = graph;
        this.columnStrings = (columnStrings != null) ? columnStrings.clone() : null;
    }
    
    public void init() {
        try {
           doInit();            
        } catch( Exception e) {
            LOG.error("Error while creating data model", e);
        }       
    }
    
    private void doInit()  {
        createColumnLoader();
        loadData();
        createResultTable();
    }

    private void createColumnLoader() {
        columnLoaders = new LinkedList<ColumnLoader>();
        int n = 0;
        for (String columnString : columnStrings) {
            columnLoaders.add(new ColumnLoader(n, columnString));
            n++;
        }       
    }

    private void loadData() {
        for (ColumnLoader columnLoader : columnLoaders) {
            columnLoader.load(this.graph);
        }        
    }

    private void createResultTable() {  
        Map<String, String[]> allRowMap = createMapWithAllRows();  
        resultTable = TableGenerator.createTable(allRowMap);      
    }

    private Map<String, String[]> createMapWithAllRows() {
        Map<String, String[]> allRowMap = new HashMap<String, String[]>(); 
        for (ColumnLoader columnLoader : columnLoaders) {
            Map<String, String> valueMap = columnLoader.getValueMap();
            Set<String> keySet = valueMap.keySet();
            for (String key : keySet) {
                String[] row = new String[columnLoaders.size()];
                row[columnLoader.getNumber()] = valueMap.get(key);
                allRowMap.put(key + COLUMN_SEPERATOR + columnLoader.getNumber(), row);
            }
                       
        }
        return allRowMap;
    }

    public List<List<String>> getResult() {
        return resultTable;
    }
    
}
