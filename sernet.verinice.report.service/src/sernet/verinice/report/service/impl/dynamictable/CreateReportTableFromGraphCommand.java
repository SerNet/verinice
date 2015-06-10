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
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
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

    private Stack<String> operandStack = new Stack<String>();
    private Stack<String> operatorStack = new Stack<String>();

    private HashMap<String, TableRow> resultMap;

    public CreateReportTableFromGraphCommand(VeriniceGraph graph, String[] columns) {
        this.graph = graph;
        this.userColumnStrings = columns;
        this.table = new ArrayList<List<String>>();
        this.resultMap = new HashMap<String, TableRow>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {

        if (!resultInjectedFromCache) {
         // i= 1 to skip he first, already handled,  entry
            for (int i = 0; i < userColumnStrings.length; i++) { 
                String propertyPath = userColumnStrings[i];
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Inspecting user propertyPath:\t" + propertyPath);
                }
                operandStack.clear();
                operatorStack.clear();
                fillStacks(reversePropertyPath(propertyPath));
                if (operatorStack.size() != operandStack.size()) {
                    LOG.warn("wrong stacksizes detected. " + operandStack.size() + " operands and " + operatorStack.size() + " operators. Should always be equal (including termination operator, added automatically)");
                }
                doOneStep(Collections.EMPTY_SET, operandStack.pop(), operatorStack.pop(), "", i, null);

            }
        }

    }

    private void doOneStep(Set<CnATreeElement> set, String nextTypeId, String currentOperator, String currentIdentifier, int propertyPosition, CnATreeElement rootElement){
        set = initFirstRound(set, nextTypeId);
        for(CnATreeElement element : set){ // handle every element of current set (data loaded from graph)
            currentIdentifier = getNewIdentifier(currentIdentifier, rootElement, element);
            char operator = currentOperator.toCharArray()[0];
            if(LOG.isDebugEnabled()){
                LOG.debug("New Identifier:\t" + currentIdentifier);
                LOG.debug("CurrentOperator:\t" + currentOperator);
            }
            if(LINK_TYPE_DELIMITER == operator){
                rootElement = handleLinkOperator(nextTypeId, currentIdentifier, propertyPosition, rootElement, element);
            } else if(CHILD_TYPE_DELIMITER == operator || PARENT_TYPE_DELIMITER == operator){
                handleParentChildOperator(currentIdentifier, propertyPosition, element, operator);
            } else if(END_OF_PATH_DELIMITER == operator){
                LOG.error("something went wrong here, point should not be reached");
            }
        }
    }

    /**
     * @param currentIdentifier
     * @param propertyPosition
     * @param element
     * @param operator
     */
    private void handleParentChildOperator(String currentIdentifier, int propertyPosition, CnATreeElement element, char operator) {
        String nextEntityType = operandStack.pop();
        doOneStep(getElementsFromGraph(element, operator, nextEntityType), nextEntityType, operatorStack.pop(), currentIdentifier, propertyPosition, element);
    }

    /**
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
     * @param currentIdentifier
     * @param propertyPosition
     * @param rootElement
     * @param element
     * @return
     */
    private CnATreeElement handlePropertyOperand(String currentIdentifier, int propertyPosition, CnATreeElement rootElement, CnATreeElement element) {
        String rootIdentifier = initRootIdentifier(rootElement, element);
        if(resultMap.containsKey(rootIdentifier)){
            createSubRow(currentIdentifier, propertyPosition, element, rootIdentifier);
        } else {
            rootElement = createNewRootRow(currentIdentifier, rootElement, element);
        }
        return rootElement;
    }

    /**
     * @param currentIdentifier
     * @param rootElement
     * @param element
     * @return
     */
    private CnATreeElement createNewRootRow(String currentIdentifier, CnATreeElement rootElement, CnATreeElement element) {
        if(rootElement == null){
            rootElement = element;
        }
        TableRow row = new TableRow(rootElement.getDbId(), userColumnStrings.length, currentIdentifier);
        row.addProperty(element.getEntity().getSimpleValue(operandStack.pop()));
        resultMap.put(currentIdentifier, row);
        return rootElement;
    }

    /**
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
        resultMap.put(currentIdentifier, newRow);
    }

    /**
     * @param rootElement
     * @param element
     * @return
     */
    private String initRootIdentifier(CnATreeElement rootElement, CnATreeElement element) {
        String rootIdentifier = "";
        if(rootElement == null){
            rootIdentifier = "#" + String.valueOf(element.getDbId());
        } else {
            rootIdentifier = "#" + String.valueOf(rootElement.getDbId());
        }
        return rootIdentifier;
    }

    /**
     * @param currentIdentifier
     * @param rootElement
     * @param element
     * @return
     */
    private String getNewIdentifier(String currentIdentifier, CnATreeElement rootElement, CnATreeElement element) {
        if(LOG.isDebugEnabled()){
            LOG.debug("Old Identifier:\t" + currentIdentifier);
        }
        if(rootElement == null){
            currentIdentifier = currentIdentifier + "#" + String.valueOf(element.getDbId());
        } else {
            if(!currentIdentifier.startsWith("#" + String.valueOf(rootElement.getDbId()))){
                currentIdentifier = "#" +  String.valueOf(rootElement.getDbId());
            } else {
                currentIdentifier = currentIdentifier + "#" + element.getDbId();
            }
        }
        return currentIdentifier;
    }

    /**
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

    private boolean isPropertyIdOfTypeId(String propertyId, String typeId) {
        if (propertyId != null && typeId != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Checking if <" + propertyId + "> is property of:\t " + typeId);
            }
            return Arrays.asList(HUITypeFactory.getInstance().getEntityType(typeId).getAllPropertyTypeIds()).contains(propertyId);
        }
        return false;
    }

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

        return resultSet;

    }

    private Set<CnATreeElement> getChildrenOf(CnATreeElement parent) {
        return graph.getChildren(parent);
    }

    private CnATreeElement getParentOf(CnATreeElement child) {
        return graph.getParent(child);
    }

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

    private boolean isOperator(String term) {
        return Arrays.asList(new String[] { String.valueOf(LINK_TYPE_DELIMITER), String.valueOf(CHILD_TYPE_DELIMITER), String.valueOf(PARENT_TYPE_DELIMITER), String.valueOf(END_OF_PATH_DELIMITER) }).contains(term);
    }

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

    }

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
     * @return the results
     */
    public List<List<String>> getResults() {
        for (String key : resultMap.keySet()) {
            table.add(resultMap.get(key).getPropertyList());
        }
        return table;
    }
}
