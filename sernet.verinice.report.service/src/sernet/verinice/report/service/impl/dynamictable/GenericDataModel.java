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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * Data model for Link-Table-Reports
 * 
 * This data model simplifies the user request to implement a report template that
 * displays a table over all elements of type $a, and all to that element linked elements of 
 * type $b. <br>
 * The data model should be used in verinice reports only, usage (in a dataset) should look like this:
 * <pre>
 * ============================================================================================
 * command = new GraphCommand();
 * loader = new GraphElementLoader();
 * loader.setScopeId(root);
 * loader.setTypeIds(new String[]{ AssetGroup.TYPE_ID, Asset.TYPE_ID, IncidentScenario.TYPE_ID});
 * command.addLoader(loader);
 * command.addRelationId(IncidentScenario.REL_INCSCEN_ASSET);
 * command.addRelationId("rel_person_incscen_modl");
 * command = helper.execute(command);          
 * graph = command.getGraph();

 * dm = new GenericDataModel(graph, new String[]{
 *   "asset<assetgroup.assetgroup_name",
 *   "asset/asset_name", 
 *   "asset:person_iso", 
 *   "asset/person_iso.person_iso_name", 
 *   "asset/incident_scenario.incident_scenario_name"});
 * dm.init();
 * return dm.getResults(); 
 * ============================================================================================
 * </pre>
 * 
 * It returns a {@link List<List<String>}, so that a standard report table could be filled with that data
 * 
 * Syntax for Strings passed to constructor:<br>
 * Strings are always constructed as a kind of path over different entity types and ending 
 * with a property type. Entity types could be separated with 4 different operators:
 * <pre>
 *  - LINK_DELIMITER = '/'
 *      This separates two entity types that are linked to each other. Must be followed
 *      by a PROPERTY_DELIMITER
 *      e.g.: asset/person_iso, asset/incident_scenario
 *  - LINK_TYPE_DELIMITER = ':'
 *      This separates two entity types that are linked to each other. Outputs the type of the link
 *      e.g.: asset:person_iso, control:person_iso/
 *  - CHILD_DELIMITER = '>'
 *      This separates two entity types that are in a parent>child relation
 *       e.g.: baustein-umsetzung>massnahmen-umsetzung.mnums_name
 *  - PARENT_DELIMITER = '<'
 *      This separates two entity types that are in a child<parent relation
 *      e.g.: massnahmen-umsetzung<baustein-umsetzung.bstumsetzung_name
 *  - PROPERTY_DELIMITER = '.'
 *      This separates an entity type from a property type of the entity
 *      e.g.: asset.asset_name
 * </pre>
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class GenericDataModel {

    static final String COLUMN_SEPERATOR = "#";

    private static final Logger LOG = Logger.getLogger(GenericDataModel.class);
    
    private VeriniceGraph graph;
    private String[] columnStrings;
    private List<ColumnPath> columnPaths;
    // Data for a BIRT data set
    private List<List<String>> resultTable;
    
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
        createColumnPaths();
        loadData();
        createResultTable();
    }

    private void createColumnPaths() {
        columnPaths = new LinkedList<>();
        int n = 0;
        for (String columnString : columnStrings) {
            columnPaths.add(new ColumnPath(n, columnString));
            n++;
        }       
    }

    private void loadData() {
        for (ColumnPath columnPath : columnPaths) {
            columnPath.load(this.graph);
        }        
    }

    /**
     * Creates a table with all data in the list of {@link ColumnPath}s: columnPaths.
     * The table data is stored in a list of list of strings: <code>resultTable</code>.
     * The result table is used for a BIRT data set in Link-Table-Reports.
     */
    private void createResultTable() {  
        Map<String, String[]> allRowMap = createResultMap();  
        resultTable = TableGenerator.createTable(allRowMap);      
    }

    /**
     * Creates a map with all data in the list of {@link ColumnPath}s: columnPaths.
     * Each entry of the map holds the data of one cell of the result table.
     * The key of the map is a path of db-ids followed by the index of the column:
     * 
     * <DB-ID>[.<DB-ID>]#<COLUMN-INDEX>
     * 
     * If you have the following object path in a report template for column 4: 
     * "incident_scenario/asset/person-iso.person-iso_surname"
     * One entry in the map for this path would be:
     * 589839.589837.589828#3 
     * 
     * @return A map with all data
     */
    private Map<String, String[]> createResultMap() {
        Map<String, String[]> allRowMap = new HashMap<>(); 
        for (ColumnPath columnPath : columnPaths) {
            Map<String, String> valueMap = columnPath.getValueMap();
            Set<String> keySet = valueMap.keySet();
            for (String key : keySet) {
                String[] row = new String[columnPaths.size()];
                row[columnPath.getNumber()] = valueMap.get(key);
                allRowMap.put(key + COLUMN_SEPERATOR + columnPath.getNumber(), row);
            }                  
        }
        GenericDataModel.log(allRowMap);
        fillEmptyRows(allRowMap);
        GenericDataModel.log(allRowMap);
        return allRowMap;
    }

    /**
     * Fills empty columns in rows with data of linked objects
     * from other rows.
     *
     * @param allRowMap A map with all rows
     */
    private void fillEmptyRows(Map<String, String[]> allRowMap) {
        for (int i = 0; i < columnPaths.size(); i++) {
            // find non-empty values for this row
            Set<String> keys = allRowMap.keySet();
            for (String key : keys) {
                String value = allRowMap.get(key)[i];
                if((value)!=null) {
                    // fill rows with found values if needed
                    fillEmptyRows(allRowMap, i, removeRowNumber(key), value);                                   
                }
            }
        }
        // fill up columns of ParentElements
        for (int i = 0; i < columnPaths.size(); i++) {
            // find non-empty values for this row
            List<String> keys = new LinkedList<>(allRowMap.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                String value = allRowMap.get(key)[i];
                if((value)!=null) {
                    String parentKey = getChildKey(columnPaths.get(i),key);
                    if(parentKey!=null) {
                        fillEmptyGroupRows(allRowMap, i, parentKey, value);
                    }
                }
            }
        }
        
    }

    /**
     * Returns the key of the path from the beginning
     * to the first occurence of a parent delimiter '<'.
     * If there is parent delimiter null is returned.
     * 
     * If the path is: "samt_topic<controlgroup.controlgroup_name"
     * And the key <samt_topic_id>.<controlgroup_id>#5
     * <samt_topic_id> is returned.
     * 
     * @param pathWithParent A ColumnPath with a {@link ParentElement}
     * @param key The key of a row
     * @return The key of child from the first parent element 
     *         or null if there is no parent element
     */
    private static String getChildKey(ColumnPath pathWithParent, String key) {
        StringBuilder sb = new StringBuilder();
        String strippedKey = removeRowNumber(key);
        StringTokenizer st = new StringTokenizer(strippedKey, ".");
        int n = 0;
        boolean isValidParentPath = false;
        for (IPathElement pathElement : pathWithParent.getPathElements()) {
            if(pathElement instanceof ParentElement) {
                if(isFollowedByPropertyOrParent(pathElement)) {
                    isValidParentPath = true;              
                }
                break;
            }
            if(st.hasMoreTokens()) {
                if(n>0) {
                    sb.append(".");
                }
                sb.append(st.nextToken());
            }
            n++;
        }
        return (isValidParentPath && sb.length()>0) ? sb.toString() : null;
    }

    /**
     * @param pathElement A path element
     * @return true if pathElement is followed by parent element(s) or property element
     */
    private static boolean isFollowedByPropertyOrParent(IPathElement pathElement) {
        if(pathElement.getChild() instanceof ParentElement) {
            return isFollowedByPropertyOrParent(pathElement.getChild());
        }
        return pathElement.getChild() instanceof PropertyElement;
    }

    /**
     * Fill column i of all rows with values
     * if key of column contains key. 
     * 
     * @param allRowMap A map with all rows
     * @param i Column index
     * @param key Object db-id path of a row
     * @param value The value of the column with index i
     */
    private static void fillEmptyRows(Map<String, String[]> allRowMap, int i, String key, String value) {
        Set<String> keys = allRowMap.keySet();
        for (String keyCurrent : keys) {
            if(keyCurrent.startsWith(key)) {
                String[] row = allRowMap.get(keyCurrent);
                if(row!=null) {
                    row[i] = value;
                    allRowMap.put(keyCurrent, row);
                }          
            }
        }
    }
    
    private static void fillEmptyGroupRows(Map<String, String[]> allRowMap, int i, String key, String value) {
        Set<String> keys = allRowMap.keySet();
        for (String keyCurrent : keys) {
            String strippedKey = removeRowNumber(keyCurrent);
            if(strippedKey.startsWith(key)) {
                String[] row = allRowMap.get(keyCurrent);
                if(row != null && row[i] == null ) {
                    row[i] = value;
                    allRowMap.put(keyCurrent, row);
                }          
            }
        }
    }

    /**
     * Return all data in a table.
     * You can use this result as a BIRT data set
     * 
     * @return All data in a table for BIRT data sets
     */
    public List<List<String>> getResult() {
        return resultTable;
    }
    
    public static String removeRowNumber(String key) {
        int i = key.indexOf(GenericDataModel.COLUMN_SEPERATOR);
        if(i==-1) {
            return key;
        }
        return key.substring(0, i);
    }
    
    public static void log(Map<String, String[]> valueMap) {
        if (LOG.isDebugEnabled()) {
            List<String> keyList =  new LinkedList<>(valueMap.keySet());
          
            Collections.sort(keyList);
            for (String key : keyList) {
                LOG.debug(key + ":" + Arrays.toString(valueMap.get(key)));
            }
        }
    }
    
}
