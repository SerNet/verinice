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

import java.util.*;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * Data model for Link Tables.
 *
 * This data model converts data from a verinice graph to a table (aka a list of lists)
 * based on a {@link ILinkTableConfiguration} object.
 *
 * See {@link LinkTableService} for an introduction to link tables and for a definition
 * of column paths.
 *
 * To create a link table with this data model call:
 *
 * LinkTableDataModel dm = new LinkTableDataModel(veriniceGraph, linkTableConfiguration);
 * dm.init();
 * List<List<String>> table = dm.getResult();
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class LinkTableDataModel {

    static final String COLUMN_SEPERATOR = "#";

    private static final Logger LOG = Logger.getLogger(LinkTableDataModel.class);

    private VeriniceGraph graph;
    private ILinkTableConfiguration configuration;
    private List<ColumnPath> columnPaths;
    // Data for a BIRT data set
    private List<List<String>> resultTable;


    /**
     * @param graph
     * @param columnPathes
     * @deprecated Use {@link LinkTableConfiguration} to create instances
     */
    @Deprecated
    public LinkTableDataModel(VeriniceGraph graph, String[] columnPathes) {
        super();
        this.graph = graph;
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        for (String columnPath : columnPathes) {
            builder.addColumnPath(columnPath);
        }
        this.configuration = builder.build();
    }

    public LinkTableDataModel(VeriniceGraph graph, ILinkTableConfiguration configuration) {
        super();
        this.graph = graph;
        this.configuration = configuration;
    }

    public void init() {      
        createColumnPaths();
        loadData();
        createResultTable();  
    }

    private void createColumnPaths() {
        columnPaths = new LinkedList<>();
        int n = 0;
        Iterator<String> columnStringIterator = configuration.getColumnPaths().iterator();
        for (IPathElement pathElement : configuration.getPathElements()) {
            ColumnPath path = new ColumnPath(n, columnStringIterator.next());
            path.setPathElements(pathElement);
            columnPaths.add(path);
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
     * Creates a map with all data in the link table.
     * Each entry of the map holds the data of one cell of the result table.
     * The key of the map is a path of db-ids followed by the index of the column:
     *
     * <DB-ID>[.<DB-ID>]#<COLUMN-INDEX>
     *
     * e.g.
     * You have the following VQL path in a LTR file:
     * "incident_scenario/asset/person-iso.person-iso_surname"
     * The key of one entry in the would be:
     * <incident_scenario-id>.<asset-id>.<person-iso-id>#3
     *
     * @return A map with all data in the link table
     */
    private Map<String, String[]> createResultMap() {
        Map<String, String[]> resultMap = new HashMap<>();
        // Iterate over all column pathes
        for (ColumnPath columnPath : columnPaths) {
            Map<String, String> columnPathResults = columnPath.createResultMap();  
            // Iterate over results of the column path
            Set<String> keySet = columnPathResults.keySet();
            for (String key : keySet) {
                String newKey = adaptColumnIdIfNeeded(columnPath, columnPathResults, key);

                String[] row = new String[columnPaths.size()];
                row[columnPath.getNumber()] = columnPathResults.get(key);
                resultMap.put(newKey + COLUMN_SEPERATOR + columnPath.getNumber(), row);
            }
        }
        // Fill up empty rows
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rows before fill up: ");
            LinkTableDataModel.log(LOG, resultMap);
        }
        fillEmptyRows(resultMap);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rows after fill up: ");
            LinkTableDataModel.log(LOG, resultMap);
        }
        return resultMap;
    }

    private String adaptColumnIdIfNeeded(ColumnPath columnPath,
            Map<String, String> columnPathResults, String key) {
        Map<String, Object> result = columnPath.getResult().get("ROOT");
        // Add the result to the map
        String[] split = key.split("\\" + IPathElement.RESULT_KEY_SEPERATOR);
        ArrayList<String> asList = new ArrayList<>(Arrays.asList(split));
        String id;
        String newKey = key;
        while(!asList.isEmpty()){
            id = asList.remove(0);

            Object object = result.get(id);
            if (object instanceof Map<?, ?>) {
                while (result.get(id) instanceof Map<?, ?> && result.containsKey(id)) {
                    result = (Map<String, Object>) result.get(id);
                }
            } else if (object instanceof LinkTableResult) {
                LinkTableResult res = (LinkTableResult) object;

                newKey = newKey.replace(String.valueOf(res.getElementId()),
                        String.valueOf(res.getEdgeId()));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("colNum: " + columnPath.getNumber() + ", columnPathEntry: "
                            + columnPathResults.get(key) + ", edgeId:"
                            + res.getEdgeId() + ", elementId: "
                            + res.getElementId() + ", old key:" + key + ", new key:"
                            + newKey);
                }
            }
        }
        return newKey;
    }

    /**
     * Fills empty columns in rows with data of linked objects
     * from other rows.
     *
     * @param resultMap A map with all rows
     */
    private void fillEmptyRows(Map<String, String[]> resultMap) {
        for (int i = 0; i < columnPaths.size(); i++) {
            // find non-empty values for this row
            Set<String> keys = resultMap.keySet();
            for (String key : keys) {
                String value = resultMap.get(key)[i];
                if((value)!=null) {
                    // fill rows with found values if needed
                    fillEmptyRows(resultMap, i, removeRowNumber(key), value);
                }
            }
        }
        fillEmptyOfParentElements(resultMap);
    }

    private void fillEmptyOfParentElements(Map<String, String[]> allRowMap) {
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
     * to the first occurrence of a parent delimiter '<'.
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
        return pathElement.getChild() instanceof ElementPropertyElement;
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
        int i = key.indexOf(LinkTableDataModel.COLUMN_SEPERATOR);
        if(i==-1) {
            return key;
        }
        return key.substring(0, i);
    }

    public static void log(Logger logger, Map<String, String[]> valueMap) {
        List<String> keyList =  new LinkedList<>(valueMap.keySet());
    
        Collections.sort(keyList);
        for (String key : keyList) {
            logger.debug(key + ":" + Arrays.toString(valueMap.get(key)));
        }
    }

}
