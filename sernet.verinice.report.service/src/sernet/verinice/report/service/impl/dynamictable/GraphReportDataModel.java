/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl.dynamictable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.connect.URLUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This data model simplifies the user request to implement a report template that
 * displays a table over all elements of type $a, and all to that element linked elements of 
 * type $b. <br>
 * The data model should be used in verinice reports only, usage (in a dataset) should look like this:
 * <pre>
 * ============================================================================================
 * command = new GraphCommand();
 * loader = new GraphElementLoader();
 * loader.setScopeId(root);
 * loader.setTypeIds(new String[]{ Asset.TYPE_ID, IncidentScenario.TYPE_ID});
 * command.addLoader(loader);
 * command.addRelationId(IncidentScenario.REL_INCSCEN_ASSET);
 * command.addRelationId("rel_person_incscen_modl");
 * command = helper.execute(command);          
 * graph = command.getGraph();

 * dm = new GraphReportDataModel(graph, new String[]{"asset/asset_name", "asset/incident_scenario/incident_scenario_name"});
 * dm.init();
 * return dm.getResults(); 
 * ============================================================================================
 * </pre>
 * 
 * it returns a {@link List<List<String>}, so that a standard report table could be filled with that data
 * 
 * Syntax for Strings passed to constructor:<br>
 * Strings are always constructed as a kind of path over different entitytypes and ending 
 * with a property type. Entitytypes could be separated with 4 different operators:
 * <pre>
 *  - LINK_TYPE_DELIMITER = '/'
 *      this separates two entitytypes that are linked to each other AND a entitytype and the
 *      property which should be put into the table
 *      e.g.: asset/asset_name, asset/incident_scenario/incident_scenario_name
 *  - CHILD_TYPE_DELIMITER = '>'
 *      this separates two entitytypes that are in a parent>child relation
 *       e.g.: baustein-umsetzung>massnahmen-umsetzung/mnums_name
 *  - PARENT_TYPE_DELIMITER = '<'
 *      this separates two entitytypes that are in a child<parent relation
 *      e.g.: massnahmen-umsetzung<baustein-umsetzung/bstumsetzung_name
 *  - END_OF_PATH_DELIMITER = '#'
 *      this is for internal use only, to mark the end of a propertypath, please do not use this manually
 * </pre>
 * 
 * class is marked deprecated, please use {@link GenericDataModel} only
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@Deprecated
public class GraphReportDataModel  {

    private static final Logger LOG = Logger.getLogger(GraphReportDataModel.class);

    private VeriniceGraph graph;

    private String[] userColumnStrings;

    private List<List<String>> table;

    private static final char LINK_TYPE_DELIMITER = '/';
    private static final char CHILD_TYPE_DELIMITER = '>';
    private static final char PARENT_TYPE_DELIMITER = '<';
    private static final char END_OF_PATH_DELIMITER = '#';
    
    private static final String EMPTY_PROPERTY = "empty_property";

    private static final NumericStringComparator NSC = new NumericStringComparator();
    
    String propertyPath;
    
    /**
     * user generated input string are being parsed into two categories, operators and operands,
     * and beeing stored on stacks (in reverse order)
     */
    private Stack<String> operandStack;
    private Stack<String> operatorStack;
    private String operand;
    private String operator;
    
    
    private Set<CnATreeElement> elementSet;
    private CnATreeElement parentElement;
    private CnATreeElement element;

    // used for storing temporary results and final result generation
    private Map<String, TableRow> resultMap;

    // graph must be created within report template as shown above in class comment
    public GraphReportDataModel(VeriniceGraph graph, String[] columns) {
        this.graph = graph;
        this.userColumnStrings = (columns != null) ? columns.clone() : null;
        this.resultMap = new HashMap<String, TableRow>();
        this.operandStack = new Stack<String>();
        this.operatorStack = new Stack<String>();
    }

    
    public void init() {
        try {
           doInit();            
        } catch( Exception e) {
            LOG.error("Error while creating data model", e);
        }       
    }
    
    private void doInit() throws CommandException {
        // iterate over all userStrings
        for (int i = 0; i < userColumnStrings.length; i++) { 
            propertyPath = userColumnStrings[i];
            if (LOG.isInfoEnabled()) {
                LOG.info("Inspecting propertyPath:\t" + propertyPath + "\n\n\n");
            }
            // clear stack for new string
            clearStacks();
            // fill stacks with new input
            fillStacks(reversePropertyPath(propertyPath));
            if (operatorStack.size() != operandStack.size()) {
                LOG.warn("wrong stacksizes detected. " + operandStack.size() + " operands and " + operatorStack.size() + " operators. Should always be equal (including termination operator, added automatically)");
            }
            elementSet = graph.getElements(operandStack.peek());
            parentElement = null;
            if(LOG.isDebugEnabled()){
                LOG.debug("first round, " + elementSet.size() + " elements of type:\t" + operand);
            }           
            createColumn("", i);
        }
    } 

    /**
     * Iterate over a set of elements which are loaded from the {@link VeriniceGraph}, 
     * which is initialized within a report dataset. method iterates a single propertyPath at first
     * and linked elements at second to navigate to a given property and add that to a list, 
     * that represent a row in the report table 
     * 
     * @param rowId - identifier of root element for next row, used for storing row in hashmap
     * @param propertyPosition - position of property in result row  
     */
    private void createColumn( String rowId, int propertyPosition){
        if(LOG.isDebugEnabled()){
            LOG.debug("Now iterating " + elementSet.size() + " elements");
        } 
        for(CnATreeElement elementFromSet : elementSet){ // handle every element of current set (data loaded from graph)
            this.element = elementFromSet;
            handleElement(rowId, propertyPosition);
        }
    }

    private void handleElement(String rowId, int propertyPosition) {
        String newRowId = new StringBuilder().append(rowId).append("#").append(element.getDbId()).toString();
        operator = operatorStack.pop();
        operand = operandStack.pop();
        String currentOperator = operator;
        String currentOperand = operand;
        logElement(rowId, newRowId);
        char operatorChar = operator.toCharArray()[0];
        if(LINK_TYPE_DELIMITER == operatorChar){
            parentElement = handleLinkOperator(element.getTypeId(), newRowId, propertyPosition);
        } else if(CHILD_TYPE_DELIMITER == operatorChar || PARENT_TYPE_DELIMITER == operatorChar){
            handleParentChildOperator(newRowId, propertyPosition, operatorChar);
        } else if(END_OF_PATH_DELIMITER == operatorChar){
            LOG.error("something went wrong here, point should not be reached");
        } else if(LOG.isDebugEnabled()){
            LOG.debug("used operator is not support, please contact your support or read the api - documenation");
        }
        operandStack.push(currentOperand);
        operatorStack.push(currentOperator);
    }

    /**
     * Load children or parent element(s) (depends on operator) of type nextEntityType and 
     * passes them to back to doOneStep()
     * 
     * @param rowId
     * @param propertyPosition
     * @param operator
     */
    private void handleParentChildOperator(
            String rowId, 
            int propertyPosition,
            char operatorChar) {
        operand = operandStack.peek();
        elementSet = getElementsFromGraph(operatorChar, operand);
        parentElement = element;
        operator = operatorStack.peek();
        createColumn(rowId, propertyPosition);
    }

    /**
     * Load linked elements of type nextEntityType and passes them to back to doOneStep()
     * 
     * @param typeId
     * @param rowId
     * @param propertyPosition
     * @return
     */
    private CnATreeElement handleLinkOperator(String typeId, String rowId, int propertyPosition) {
        if(!(operandStack.isEmpty()) && isPropertyIdOfTypeId(operandStack.peek(), typeId)){
            parentElement = handlePropertyOperand(rowId, propertyPosition);               
        } else if(!(operandStack.isEmpty()) && isEntityType(operandStack.peek())){       
            elementSet = getElementsFromGraph(operator.toCharArray()[0], operandStack.peek());
            parentElement = element;
            createColumn(rowId, propertyPosition);        
        } 
        if(operandStack.isEmpty()){
            // in case of no linked element / no property for given positon and element just add "" instead of null
            if(resultMap.containsKey(rowId)){
                TableRow row = resultMap.get(rowId); 
                if(row.getProperty(propertyPosition) == null){
                    row.addProperty("", propertyPosition);
                } 
            }
        }
        return parentElement;
    }

    /**
     * Reads a property of a given element, computes the row identifier, 
     * loads (if existant) row from result hashmap, and adds property to row 
     * (which is instantiated if non-existant)
     * 
     * @param rowId
     * @param propertyPosition
     * @return
     */
    private CnATreeElement handlePropertyOperand(String rowId, int propertyPosition) {
        String existingPath = rowId;
        
        if(!resultMap.containsKey(existingPath)) {      
            // check recursive upwards if id is already in map
            if(StringUtils.countMatches(rowId, "#") != 1 && rowId.startsWith("#")){
                existingPath = rowId.substring(0, rowId.lastIndexOf('#'));
            } 
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Searching for " + existingPath + " on map");
        }
        if(resultMap.containsKey(existingPath)){
            createSubRow(rowId, propertyPosition, existingPath);
        } else {
            parentElement = createNewRootRow(rowId, operandStack.peek(), propertyPosition);
        }
        return parentElement;
    }
    
    /**
     * Creates a new {@link TableRow} with a given rootElement and a subelement element.
     * Creates a row to an element which is a sub(linked) element to a given one.
     * 
     * @param rowId
     * @param propertyPosition
     * @param parentRowId
     */
    private void createSubRow(String rowId, int propertyPosition, String parentRowId) {
        TableRow oldRow = resultMap.get(parentRowId);
        TableRow newRow = new TableRow(element.getDbId(), userColumnStrings.length, rowId);
        newRow.setProperties(oldRow.getPropertyList().toArray(new String[oldRow.getPropertyList().size()]));
        if(LOG.isDebugEnabled()){
            LOG.debug("used \"Oldrow\":\t" + oldRow.toString());
            LOG.debug("Row before insert:\t" + newRow.toString());
        }
        String propertyId = operandStack.peek();
        if(newRow.getProperty(propertyPosition).equals("")){
            if(LOG.isDebugEnabled()){
                LOG.debug("Inserting property at position " + propertyPosition);
            }
            String value;
            if(!(rowId.equals(EMPTY_PROPERTY))){
                value = getPropertyValue(propertyId);
            } else {
                value = "";
            }
            
            newRow.addProperty(value, propertyPosition);
        } else {
            if(LOG.isDebugEnabled()){
                LOG.debug("Appending Property");
            }
            newRow.addProperty(getPropertyValue(propertyId));
        }
        resultMap.put(rowId, newRow);
        if(LOG.isDebugEnabled()){
            LOG.debug("Added following row to map:\t" + newRow.toString());
        }
    }

    /**
     * Creates a new table row for a given {@link CnATreeElement} rootElement.
     * 
     * @param rowId
     * @param element
     * @return
     */
    private CnATreeElement createNewRootRow(String rowId, String propertyId, int propertyPosition) {
        if(parentElement == null){
            parentElement = element;
        }
        TableRow row = new TableRow(parentElement.getDbId(), userColumnStrings.length, rowId);
        String value = "";
        if(!(propertyId.equals(EMPTY_PROPERTY))){
            value = getPropertyValue(propertyId);
        } else {
            value = "";
        }
        if(value == null){
            value = "";
        }
        row.addProperty(value, propertyPosition);
        if(LOG.isDebugEnabled()){
            LOG.debug("Added following row to map:\t" + row.toString());
        }
        resultMap.put(rowId, row);
        return parentElement;
    }


    private String getPropertyValue(String propertyId) {
        String value = element.getEntity().getSimpleValue(propertyId);
        PropertyType propertyType = getPropertyType(propertyId);
        if(propertyType.isURL()) {
            value = URLUtil.getHref(value);
        }      
        return value;
    }


    private PropertyType getPropertyType(String propertyId) {      
        return getEntityType().getPropertyType(propertyId);
    }


    private EntityType getEntityType() {
        return HUITypeFactory.getInstance().getEntityType(element.getTypeId());
    }


    /**
     * Reverses a string, based on operators.
     * e.g:
     * input: entityType1/entityType2>entityType3/property
     * output: property/entityType3>entityType2/entityType1
     * 
     * @param path
     * @return
     */
    private String reversePropertyPath(String path) {
        StringBuilder revStr = new StringBuilder("");
        int end = path.length(); // substring takes the end index -1
        int counter = path.length() - 1;
        for (int i = path.length() - 1; i >= 0; i--) {
            if (path.charAt(i) == '/' || path.charAt(i) == '<' || path.charAt(i) == '>' || path.charAt(i) == '#' || i == 0) {
                if (i != 0) {
                    revStr.append(path.substring(i + 1, end));
                    revStr.append(path.charAt(i));
                } else {
                    revStr.append(path.substring(i, end));
                }
                end = counter;
            }
            counter--;
        }
        return revStr.toString();
    }

    /**
     * Returns if propertyId is an existing property Id of the entity referenced by typeId.
     * 
     * @param propertyId
     * @param typeId
     * @return
     */
    private boolean isPropertyIdOfTypeId(String propertyId, String typeId) {
        if (propertyId != null && typeId != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking if <" + propertyId + "> is property of: " + typeId);
            }
            return Arrays.asList(HUITypeFactory.getInstance().getEntityType(typeId).getAllPropertyTypeIds()).contains(propertyId);
        }
        return false;
    }

    /**
     * Loads elements of a given type, 
     * relation and element using an instance of {@link VeriniceGraph}.
     * 
     * @param element
     * @param operator
     * @param typeId
     * @return
     */
    private Set<CnATreeElement> getElementsFromGraph(char operator, String typeId) {
        Set<CnATreeElement> resultSet = new HashSet<CnATreeElement>(0);
        switch (operator) {
        case CHILD_TYPE_DELIMITER:
            resultSet = graph.getChildren(element);
            break;
        case PARENT_TYPE_DELIMITER:
            resultSet.add(graph.getParent(element));
            break;
        case LINK_TYPE_DELIMITER:
            resultSet = graph.getLinkTargetsByElementType(element, typeId);
            break;
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("returning " + resultSet.size() + " from graph, loaded for element " + element.getTitle() + "<" + element.getDbId() + "> and operator <" + operator + "> and typeId <" + typeId + ">" );
        }
        if(resultSet.size() == 0){
           resultSet.hashCode(); 
        }
        return resultSet;

    }

    /**
     * Checks if given string is a (in SNCA.xml) defined entity type
     * 
     * @param entityTypeId
     * @return
     */
    private boolean isEntityType(String entityTypeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if " + entityTypeId + " is a valid entityTypeId");
        }
        for (EntityType entityType : HUITypeFactory.getInstance().getAllEntityTypes()) {
            if (entityType.getId().equals(entityTypeId)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(entityTypeId + " is a valid entityTypeId");
                }
                return true;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(entityTypeId + " is NOT a valid entityTypeId");
        }
        return false;
    }

    /**
     * Main method to parse user given strings (propertypaths). 
     * Elements/tokens are separated onto two stacks, operatorStack and operandStack
     * 
     * @param propertyPath
     */
    private void fillStacks(String propertyPath) {
        if (!propertyPath.startsWith(String.valueOf(END_OF_PATH_DELIMITER))) {
            operatorStack.push(String.valueOf(END_OF_PATH_DELIMITER));
        }
        char nextDelimiter = getNextDelimiter(propertyPath);

        String term = propertyPath.substring(0, propertyPath.indexOf(nextDelimiter));
        while (!"".equals(propertyPath) && !"".equals(term)) {
            if (!(END_OF_PATH_DELIMITER == nextDelimiter)) {
                term = propertyPath.substring(0, propertyPath.indexOf(nextDelimiter));
            } else {
                term = propertyPath;

            }
            if (term != null && !("".equals(term))) {
                operandStack.push(term);
            }
            if (!(END_OF_PATH_DELIMITER == nextDelimiter)) {
                operatorStack.push(String.valueOf(nextDelimiter));
            }
            if (!(END_OF_PATH_DELIMITER == nextDelimiter)) {
                propertyPath = propertyPath.substring(propertyPath.indexOf(nextDelimiter) + 1);
            } else {
                propertyPath = "";
            }
            nextDelimiter = getNextDelimiter(propertyPath);
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Parsing finished, stacks filled, looking like this:");
            LOG.debug("OperandStack:\t" + operandStack.toString());
            LOG.debug("OperatorStack:\t" + operatorStack.toString());
        }

    }
    
    private void clearStacks() {
        operandStack.clear();
        operand = null;
        operatorStack.clear();
        operator = null;
    }

    /**
     * used for parsing a propertypath, returns next delimiter in a given string
     * @param propertyPath
     * @return
     */
    private char getNextDelimiter(String propertyPath) {
        for (char c : propertyPath.toCharArray()) {
            switch (c) {
            case LINK_TYPE_DELIMITER:
                return LINK_TYPE_DELIMITER;
            case CHILD_TYPE_DELIMITER:
                return CHILD_TYPE_DELIMITER;
            case PARENT_TYPE_DELIMITER:
                return PARENT_TYPE_DELIMITER;
            default:
                continue;
            }
        }
        return END_OF_PATH_DELIMITER;
    }


    /**
     * @return the results (the dataSet data)
     */
    public List<List<String>> getResults() {
        List<List<String>> tmpList = new LinkedList<List<String>>();
        
        List<String> keyList = new LinkedList<String>(resultMap.keySet());
        Collections.sort(keyList);
        
        List<String> keyListCleaned = cleanUpKeyList(keyList);
                
        for (String cleanKey : keyListCleaned) {
            List<String> list = resultMap.get(cleanKey).getPropertyList();
            Collections.replaceAll(list, null, "");
            tmpList.add(list);
        }
        this.table = new ArrayList<List<String>>();
        table.addAll(tmpList);
        
        Collections.sort(table, new Comparator<List<String>>() {

            @Override
            public int compare(List<String> row1, List<String> row2) {
                return compareRows(row1,row2);
            }

            
        });
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Result looks like:\t" + table.toString());
        }
        return table;
    }
    
    private int compareRows(List<String> row1, List<String> row2) {
        return compareRows(row1, row2, 0);
    }


    private int compareRows(List<String> row1, List<String> row2, int i) {
        int result = 0;
        if(row1.size()>i && row2.size()>i && row1.get(i)!=null && row2.get(i)!=null) {          
            result = NSC.compare(row1.get(i), row2.get(i));
            if(result==0) {
                result = compareRows(row1, row2, i+1);
            }
        }
        return result;
    }


    private List<String> cleanUpKeyList(List<String> keyList) {
        List<String> keyListCleaned = new LinkedList<String>();
        String lastKey = null;      
        for (String key : keyList) {
            if(lastKey!=null) {
                if(!key.startsWith(lastKey) || key.equals(lastKey)) {
                    keyListCleaned.add(lastKey);
                }
            }
            lastKey = key;
        }  
        if(!keyListCleaned.contains(lastKey)) {
            keyListCleaned.add(lastKey);
        }
        return keyListCleaned;
    }
    
    private void logElement(String rowId, String newRowId) {
        if(LOG.isInfoEnabled()){
            LOG.info("Handling " + element.getTitle() + " (" + element.getTypeId() + "," + element.getDbId() + ") ");
            LOG.info("Row id: " + rowId + ", new row id: " + newRowId);
            LOG.info("Operand: " + operand + ", operand stack: " + operandStack.toString());
            LOG.info("Operator: " + operator + ", operator stack: " + operatorStack.toString() + "\n\n");
        }
    }
}
