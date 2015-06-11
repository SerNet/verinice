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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command simplifies the user request to implement a report template that
 * displays a table over all elements of type $a, and all to that element linked elements of 
 * type $b. 
 * The command should be used in verinice reports only, usage (in a dataset) should look like this:
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

 * c2 = new CreateReportTableFromGraphCommand(graph, new String[]{"asset/asset_name", "asset/incident_scenario/incident_scenario_name"});
 * return helper.execute(c2).getResults(); 
 * ============================================================================================
 * 
 * it returns a {@link List<List<String>}, so that a standard report table could be filled with that data
 * 
 * Syntax for Strings passed to constructor:
 * Strings are always constructed as a kind of path over different entitytypes and ending 
 * with a property type. Entitytypes could be separated with 4 different operators:
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
 *      
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class CreateReportTableFromGraphCommand extends GenericCommand implements ICachedCommand {

    private static final long serialVersionUID = 201506091444L;

    private static final Logger LOG = Logger.getLogger(CreateReportTableFromGraphCommand.class);

    private VeriniceGraph graph;

    private String[] userColumnStrings;

    private boolean resultInjectedFromCache = false;

    private List<List<String>> table;

    private static final char LINK_TYPE_DELIMITER = '/';
    private static final char CHILD_TYPE_DELIMITER = '>';
    private static final char PARENT_TYPE_DELIMITER = '<';
    private static final char END_OF_PATH_DELIMITER = '#';
    
    private static final String EMPTY_PROPERTY = "empty_property";

    /**
     * user generated input string are being parsed into two categories, operators and operands,
     * and beeing stored on stacks (in reverse order)
     */
    private Stack<String> operandStack;
    private Stack<String> operatorStack;

    // used for storing temporary results and final result generation
    private Map<String, TableRow> resultMap;

    // graph must be created within report template as shown above in class comment
    public CreateReportTableFromGraphCommand(VeriniceGraph graph, String[] columns) {
        this.graph = graph;
        this.userColumnStrings = (columns != null) ? columns.clone() : null;
        this.resultMap = new HashMap<String, TableRow>();
        this.operandStack = new Stack<String>();
        this.operatorStack = new Stack<String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {

        if (!resultInjectedFromCache) {
            // iterate over all userStrings
            for (int i = 0; i < userColumnStrings.length; i++) { 
                String propertyPath = userColumnStrings[i];
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Inspecting user propertyPath:\t" + propertyPath);
                }
                // clear stack for new string
                operandStack.clear();
                operatorStack.clear();
                // fill stacks with new input
                fillStacks(reversePropertyPath(propertyPath));
                if (operatorStack.size() != operandStack.size()) {
                    LOG.warn("wrong stacksizes detected. " + operandStack.size() + " operands and " + operatorStack.size() + " operators. Should always be equal (including termination operator, added automatically)");
                }
                // first input here is an empty, since we have not parsed/loaded any strings/elements now
                doOneStep(Collections.EMPTY_SET, operandStack.pop(), operatorStack.pop(), "", i, null);

            }
        }

    }
    
    /**
     * iterate over a set of elements which are loaded from the {@link VeriniceGraph}, which is initialized within a report dataset. method iterates a single propertyPath at first
     * and linked elements at second to navigate to a given property and add that to a list, that represent a row in the report table 
     * @param set - list of elements beeing inspected
     * @param nextTypeId - next element type that needs to be loaded
     * @param currentOperator - next operator that defines if children/parent/linked elements are loaded in current iteration
     * @param currentIdentifier - identifier of root element for next row, used for storing row in hashmap
     * @param propertyPosition - position of property in result row  
     * @param rootElement - root element to linked/parent/child elements, that are beeing inspected in current iteration
     * TODO: find cool name here!
     */
    private void doOneStep(Set<CnATreeElement> set, String nextTypeId, String currentOperator, String currentIdentifier, int propertyPosition, CnATreeElement rootElement){
        set = initFirstRound(set, nextTypeId);
        if(LOG.isDebugEnabled()){
            LOG.debug("Now iterating " + set.size() + " elements of type " + set.toArray(new CnATreeElement[set.size()])[0].getTypeId());
        }
        for(CnATreeElement element : set){ // handle every element of current set (data loaded from graph)
            String newIdentifier = new StringBuilder().append(currentIdentifier).append("#").append(element.getDbId()).toString();
            char operator = currentOperator.toCharArray()[0];
            if(LOG.isDebugEnabled()){
                LOG.debug("Handling " + element.getTypeId() +  " " + (Arrays.asList(set.toArray(new CnATreeElement[set.size()])).indexOf(element) + 1) + "/" + set.size());
                LOG.debug("CurrentIdentifier:\t" + currentIdentifier);
                LOG.debug("NewIdentifier:\t" + newIdentifier);
                LOG.debug("CurrentOperator:\t" + currentOperator);
            }
            if(LINK_TYPE_DELIMITER == operator){
                rootElement = handleLinkOperator(nextTypeId, newIdentifier, propertyPosition, rootElement, element);
            } else if(CHILD_TYPE_DELIMITER == operator || PARENT_TYPE_DELIMITER == operator){
                handleParentChildOperator(newIdentifier, propertyPosition, element, operator);
            } else if(END_OF_PATH_DELIMITER == operator){
                LOG.error("something went wrong here, point should not be reached");
            }
        }
        if(set.size() == 0 && rootElement != null){
            handleNoLinkedElement(rootElement, propertyPosition, (new StringBuilder().append(currentIdentifier).append("#").append(rootElement.getDbId()).toString()));
        }
        // if all elements of type are iterated, pop (remove) type & operator from stack
        if(!(operandStack.isEmpty())){
            operandStack.pop();
        }
        if(!(operatorStack.isEmpty())){
            operatorStack.pop();
        }
    }

    /**
     * load children or parent element(s) (depends on operator) of type nextEntityType and passes them to back to doOneStep()
     * @param currentIdentifier
     * @param propertyPosition
     * @param element
     * @param operator
     */
    private void handleParentChildOperator(String currentIdentifier, int propertyPosition, CnATreeElement element, char operator) {
        String nextEntityType = operandStack.peek();
        doOneStep(getElementsFromGraph(element, operator, nextEntityType), nextEntityType, operatorStack.peek(), currentIdentifier, propertyPosition, element);
    }

    /**
     * load linked elements of type nextEntityType and passes them to back to doOneStep()
     * @param nextTypeId
     * @param currentIdentifier
     * @param propertyPosition
     * @param rootElement
     * @param element
     * @return
     */
    private CnATreeElement handleLinkOperator(String nextTypeId, String currentIdentifier, int propertyPosition, CnATreeElement rootElement, CnATreeElement element) {
        if(!(operandStack.isEmpty()) && isPropertyIdOfTypeId(operandStack.peek(), nextTypeId)){
            rootElement = handlePropertyOperand(currentIdentifier, propertyPosition, rootElement, element);
        } else if(!(operandStack.isEmpty()) && isEntityType(operandStack.peek())){
            String nextEntityType = operandStack.pop();
            doOneStep(getElementsFromGraph(element, operatorStack.peek().toCharArray()[0], nextEntityType), nextEntityType, operatorStack.pop(), currentIdentifier, propertyPosition, element);
        }
        return rootElement;
    }

    /**
     * reads a property of a given element, computes the row identifier, loads (if existant) row from result hashmap, and adds property to row (which is instantiated if non-existant)
     * @param currentIdentifier
     * @param propertyPosition
     * @param rootElement
     * @param element
     * @return
     */
    private CnATreeElement handlePropertyOperand(String currentIdentifier, int propertyPosition, CnATreeElement rootElement, CnATreeElement element) {
        String existingPath = "";
        // check recursive upwards if id is already existant on map
        if(StringUtils.countMatches(currentIdentifier, "#") != 1 && currentIdentifier.startsWith("#")){
            existingPath = currentIdentifier.substring(0, currentIdentifier.lastIndexOf('#'));
        } else {
            existingPath = currentIdentifier;
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Searching for " + existingPath + " on map");
        }
        if(resultMap.containsKey(existingPath)){
            createSubRow(currentIdentifier, propertyPosition, element, existingPath);
        } else {
            rootElement = createNewRootRow(currentIdentifier, rootElement, element, operandStack.peek());
        }
        return rootElement;
    }
    
    /**
     * if no element is found for current operator and root, we have to insert an empty string ("")
     * @param element
     * @param propertyPosition
     * @param identifier
     */
    private void handleNoLinkedElement(CnATreeElement element, int propertyPosition, String identifier){
        if(identifier == null || identifier.isEmpty()){
            identifier = "#" + element.getDbId();
        }
        if(resultMap.containsKey(identifier)){
            createSubRow(EMPTY_PROPERTY, propertyPosition, element, identifier);
        } else {
            createNewRootRow(identifier, null, element, EMPTY_PROPERTY);
        }
        
    }

    /**
     * creates a new table row for a given {@link CnATreeElement} rootElement
     * @param currentIdentifier
     * @param rootElement
     * @param element
     * @return
     */
    private CnATreeElement createNewRootRow(String currentIdentifier, CnATreeElement rootElement, CnATreeElement element, String propertyId) {
        if(rootElement == null){
            rootElement = element;
        }
        TableRow row = new TableRow(rootElement.getDbId(), userColumnStrings.length, currentIdentifier);
        String value = "";
        if(!(propertyId.equals(EMPTY_PROPERTY))){
            value = element.getEntity().getSimpleValue(propertyId);
        } else {
            value = "";
        }
        if(value == null){
            value = "";
        }
        row.addProperty(value);
        if(LOG.isDebugEnabled()){
            LOG.debug("Added following row to map:\t" + row.toString());
        }
        resultMap.put(currentIdentifier, row);
        return rootElement;
    }

    /**
     * creates a new {@link TableRow} with a given rootElement and a subelement element
     * Creates a row to an element which is a sub(linked) element to a given one
     * @param currentIdentifier
     * @param propertyPosition
     * @param element
     * @param rootIdentifier
     */
    private void createSubRow(String currentIdentifier, int propertyPosition, CnATreeElement element, String rootIdentifier) {
        TableRow oldRow = resultMap.get(rootIdentifier);
        TableRow newRow = new TableRow(element.getDbId(), userColumnStrings.length, currentIdentifier);
        newRow.setProperties(oldRow.getPropertyList().toArray(new String[oldRow.getPropertyList().size()]));
        if(newRow.getProperty(propertyPosition) == null){
            newRow.addProperty(element.getEntity().getSimpleValue(operandStack.peek()), propertyPosition);
        } else {
            newRow.addProperty(element.getEntity().getSimpleValue(operandStack.peek()));
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Added following row to map:\t" + newRow.toString());
        }
        resultMap.put(currentIdentifier, newRow);
    }


    /**
     * in case of iterating first propertyPath the set needs to be initialized with elements, this is done by this method
     * @param set
     * @param nextTypeId
     * @return
     */
    private Set<CnATreeElement> initFirstRound(Set<CnATreeElement> set, String nextTypeId) {
        if(set.size() == 0 && isEntityType(nextTypeId)){ // first round always starts with an empty set, just in case init that
            set = graph.getElements(nextTypeId);
            if(LOG.isDebugEnabled()){
                LOG.debug("first round, filling list with " + set.size() + " elements of type:\t" + nextTypeId);
            }
        }
        return set;
    }

    /**
     * reverses a string, based on operators.
     * e.g:
     * input: entityType1/entityType2>entityType3/property
     * output: property/entityType3>entityType2/entityType1
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
     * returns if propertyId is an existing property Id of the entity referenced by typeId
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
     * loads elements of a given type, relation and element using an instance of {@link VeriniceGraph}
     * @param element
     * @param operator
     * @param typeId
     * @return
     */
    private Set<CnATreeElement> getElementsFromGraph(CnATreeElement element, char operator, String typeId) {
        Set<CnATreeElement> resultSet = new HashSet<CnATreeElement>(0);
        switch (operator) {
        case CHILD_TYPE_DELIMITER:
            resultSet = getChildrenOf(element);
            break;
        case PARENT_TYPE_DELIMITER:
            resultSet.add(getParentOf(element));
            break;
        case LINK_TYPE_DELIMITER:
            resultSet = graph.getLinkTargetsByElementType(element, typeId);
            break;
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Returning " + resultSet.size() + " elements from graph, determined by operator " + operator + " and root:" + element.getDbId());
        }
        return resultSet;

    }

    /**
     * returns all children of parent using a given {@link VeriniceGraph}
     * @param parent
     * @return
     */
    private Set<CnATreeElement> getChildrenOf(CnATreeElement parent) {
        return graph.getChildren(parent);
    }

    /**
     * returns parent element of a given {@link CnATreeElement} using a given {@link VeriniceGraph} 
     * @param child
     * @return
     */
    private CnATreeElement getParentOf(CnATreeElement child) {
        return graph.getParent(child);
    }

    /**
     * checks if given string is a (in SNCA.xml) defined entity type
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
     * main method to parse user given strings (propertypaths). elements/tokens are separated onto two stacks, operatorStack and operandStack
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

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getCanonicalName());
        sb.append(this.graph.toString());
        for (String column : this.userColumnStrings) {
            sb.append(String.valueOf(column));
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang
     * .Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.table = (List<List<String>>) result;
        resultInjectedFromCache = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return getResults();
    }

    /**
     * @return the results (the dataSet data)
     */
    public List<List<String>> getResults() {
        Set<List<String>> tmpSet = new HashSet<List<String>>();
        for (String key : resultMap.keySet()) {
            List<String> list = resultMap.get(key).getPropertyList();
            Collections.replaceAll(list, null, "");
            tmpSet.add(list);
        }
        this.table = new ArrayList<List<String>>();
        table.addAll(tmpSet);
        if(LOG.isDebugEnabled()){
            LOG.debug("Result looks like:\t" + table.toString());
        }
        return table;
    }
}
