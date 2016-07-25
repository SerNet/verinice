/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import antlr.ANTLRException;
import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.antlr.VqlLexer;
import sernet.verinice.service.linktable.antlr.VqlParser;
import sernet.verinice.service.linktable.antlr.VqlParserTokenTypes;

/**
 * Parser for column pathes of Link Tables.
 * See {@link LinkTableService} for an introduction to link tables and for a definition
 * of column paths.
 *
 * Do not instantiate this class, use public static methods.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class ColumnPathParser {

    private static final Logger LOG = Logger.getLogger(ColumnPathParser.class);

    private ColumnPathParser() throws InstantiationException {
        throw new InstantiationException("Do not create instances of ColumnPathParser, use public static methods");
    }

    /**
     * Returns an array of {@link IPathElement}s
     * for a set of columns paths. Each IPathElement
     * has at least one child which represents the
     * elements in the column path. See {@link LinkTableService} for a definition
     * of column paths.
     *
     * @param columnPaths A set of column paths.
     * @return A {@link IPathElement} for each columns path
     */
    public static IPathElement[] getPathElements(Set<String> columnPaths) {
        if(columnPaths==null) {
            return new IPathElement[0];
        }
        IPathElement[] pathElements = new IPathElement[columnPaths.size()];
        int i = 0;
        for (String columnPath : columnPaths) {
            pathElements[i] = getPathElement(columnPath);
            i++;
        }
        return pathElements;
    }

    /**
     * Returns a {@link IPathElement}
     * for a columns paths. A IPathElement
     * has at least one child which represents the
     * elements in the column path. See {@link LinkTableService} for a definition
     * of column paths.
     *
     * @param columnPath A column path
     * @return A {@link IPathElement} for the columns path
     */
    public static IPathElement getPathElement(String columnPath) {
        VqlParser parser = parse(columnPath);
        CommonAST parseTree = (CommonAST)parser.getAST();
        IPathElement rootPathElement = createRootPathElement(parseTree);
        addChild(rootPathElement,parseTree.getNextSibling());
        rootPathElement.setAlias(getAlias(parseTree));
        return rootPathElement;
    }

    public static void throwExceptionIfInvalid(String columnPath) { // throws ColumnPathParseException
        parse(columnPath);
    }

    /**
     * Parses a link table (LTR) columnPath and returns the path
     * as an instance of class VqlParser. Class VqlParser is generated 
     * by ANTLR.
     * 
     * If a parse error occurs a ColumnPathParseException is thrown.
     * 
     * @param columnPath A link table (LTR) columnPath
     * @return Column path as an instance of class VqlParser
     */
    public static VqlParser parse(String columnPath) {
        return parse(columnPath,false);
    }
    
    /**
     * Parses a link table (LTR) columnPath and returns the path
     * as an instance of class VqlParser. Class VqlParser is generated 
     * by ANTLR.
     * 
     * Parse exceptions are ignored if parameter ignoreParseExceptions
     * is true. Ignored means that a parse exception is logged via Log4j
     * but not rethrown.
     * 
     * If parameter ignoreParseExceptions is true parse exceptions are wrapped
     * in a ColumnPathParseException and rethrown afterwards.
     * 
     * @param columnPath A link table (LTR) columnPath
     * @param ignoreParseExceptions
     * @return Column path as an instance of class VqlParser
     */
    public static VqlParser parse(String columnPath, boolean ignoreParseExceptions) {
        VqlLexer lexer = new VqlLexer(new StringReader(columnPath));
        VqlParser parser = new VqlParser(lexer);
        try {
            parser.expr();
        } catch (RecognitionException | TokenStreamException e) {
            handleParseError(e, columnPath, ignoreParseExceptions);
        }
        return parser;
    }

    protected static void handleParseError(ANTLRException e, String columnPath, boolean ignoreParseExceptions) {
        final String message = "Error while parsing VQL column path: " + columnPath;
        if(ignoreParseExceptions) {
            LOG.error(message, e);
        } else {
            throw new ColumnPathParseException(message, e);
        }
    }

    /**
     * Returns all object type ids in a set of column paths.
     * Object type ids are defined in file SNCA.xml.
     *
     * @param columnPaths A set of column paths
     * @return All object type ids in the column path set
     */
    public static Set<String> getObjectTypeIds(Set<String> columnPaths) {
        IPathElement[] pathElements = getPathElements(columnPaths);
        return getObjectTypeIds(pathElements);
    }

    /**
     * Returns all object type ids in a path element array.
     * Object type ids are defined in file SNCA.xml.
     *
     * @param pathElements An array of pathElements
     * @return All object type ids in the array of pathElements
     */
    public static Set<String> getObjectTypeIds(IPathElement[] pathElements) {
        Set<String> objectTypeIds = new HashSet<>();
        for (IPathElement pathElement : pathElements) {
            addObjectTypeIds(pathElement, objectTypeIds);
        }
        return objectTypeIds;
    }
    
    private static void addObjectTypeIds(IPathElement pathElement, Set<String> objectTypeIds) {
        if(pathElement instanceof LinkElement || pathElement instanceof ChildElement
           || pathElement instanceof ParentElement || pathElement instanceof RootElement) {
            objectTypeIds.add(pathElement.getTypeId());
        }
        if(pathElement.getChild()!=null) {
            addObjectTypeIds(pathElement.getChild(), objectTypeIds);
        }
    }
    
    /**
     * Returns all property type ids in a set of column paths.
     * Property type ids are defined in file SNCA.xml.
     *
     * @param columnPaths A set of column paths
     * @return All object type ids in the column path set
     */
    public static Set<String> getPropertyTypeIds(Set<String> columnPaths) {
        IPathElement[] pathElements = getPathElements(columnPaths);
        return getPropertyTypeIds(pathElements);
    }
    
    /**
     * Returns all property type ids in a path element array.
     * Property type ids are defined in file SNCA.xml.
     *
     * @param columnPathes An array of pathElements
     * @return All object type ids in the array of pathElements
     */
    public static Set<String> getPropertyTypeIds(IPathElement[] pathElements) {
        Set<String> propertyTypeIds = new HashSet<>();
        for (IPathElement pathElement : pathElements) {
            addPropertyTypeIds(pathElement, propertyTypeIds);
        }
        return propertyTypeIds;
    }

    private static void addPropertyTypeIds(IPathElement pathElement, Set<String> propertyTypeIds) {
        if(pathElement instanceof ElementPropertyElement ) {
            propertyTypeIds.add(pathElement.getTypeId());
        }
        if(pathElement.getChild()!=null) {
            addPropertyTypeIds(pathElement.getChild(), propertyTypeIds);
        }
    }

    

    private static IPathElement<CnATreeElement,CnATreeElement> createRootPathElement(CommonAST element) {
        return new RootElement(element.getText());
    }

    private static <C> void addChild(IPathElement<?,C> pathElement, AST element) {
        if(isChild(element)) {
            AST next = element.getNextSibling();
            if(!isChild(next)) {
                String message = "Missing successor of element: " + element.getText() + " in VQL column path";
                LOG.error(message);
                throw new ColumnPathParseException(message);
            }
            IPathElement<C,?> child = createPathElement(pathElement, element, next);
            pathElement.setChild(child);
            addChild(child, next.getNextSibling());
        }
    }

    /**
     * Returns the (optional) alias (or name) of a column path
     *
     * @param parseTree The parse tree of a column path
     * @return The alias of a column path or null if no alias is set
     */
    private static String getAlias(CommonAST parseTree) {
        AST current = parseTree;
        String alias = null;
        while(current!=null && alias==null) {
            alias = getAlias(current);
            current = current.getNextSibling();
        }
        return alias;
    }

    private static String getAlias(AST current) {
        String alias = null;
        if(isAlias(current)) {
            AST aliasAst = current.getNextSibling();
            if(aliasAst==null) {
                String message = "Missing successor of element: " + current.getText() + " in VQL column path";
                LOG.error(message);
                throw new ColumnPathParseException(message);
            }
            alias =  aliasAst.getText();
        }
        return alias;
    }

    private static boolean isChild(AST element) {
        return element!=null && !isAlias(element);
    }

    private static boolean isAlias(AST current) {
        return current.getType()==VqlParserTokenTypes.LITERAL_as||current.getType()==VqlParserTokenTypes.LITERAL_AS;
    }

    private static <P,E,C> IPathElement<E,C> createPathElement(IPathElement<P, E> parent, AST element, AST next) {
        IPathElement<E,C> pathElement = PathElementFactory.getElement(parent, element.getType());
        pathElement.setTypeId(next.getText());
        return pathElement;
    }

    /**
     * Returns a list with all elements of a column path.
     * 
     * For path "asset<assetgroup.assetgroup_name" a
     * list with the following elements is returned: 
     * "asset", "<", "assetgroup", "." and "assetgroup_name"
     * 
     * If a parse error occurs a ColumnPathParseException is thrown.
     * 
     * @param columnPath A link table (LTR) columnPath
     * @return A list with all elements of the path
     */
    public static List<String> getColumnPathAsList(String columnPath) {
        return getColumnPathAsList(columnPath,false);        
    }
    
    /**
     * Returns a list with all elements of a column path.
     * 
     * For path "asset<assetgroup.assetgroup_name" a
     * list with the following elements is returned: 
     * "asset", "<", "assetgroup", "." and "assetgroup_name"
     * 
     * Parse exceptions are ignored if parameter ignoreParseExceptions
     * is true. Ignored means that a parse exception is logged via Log4j
     * but not rethrown.
     * 
     * If parameter ignoreParseExceptions is true parse exceptions are wrapped
     * in a ColumnPathParseException and rethrown afterwards.
     * 
     * @param columnPath A link table (LTR) columnPath
     * @param ignoreParseExceptions
     * @return A list with all elements of the path
     */
    public static List<String> getColumnPathAsList(String columnPath, boolean ignoreParseExceptions) {
        VqlParser parser = parse(columnPath, ignoreParseExceptions);
        AST parseTree = parser.getAST();
        ArrayList<String> list = new ArrayList<>();
        while(parseTree != null){
            list.add(parseTree.getText());
            parseTree = parseTree.getNextSibling();
        }
        return list;
        
    }


    /**
     * Removes an alias from a column path list.
     *
     * <pre>
     * [assets, >, asset, ., title, as, name] -> [assets, >, asset, ., title]
     * </pre>
     *
     * @param path
     *            A valid path generated by {@link #getColumnPathAsList(String)}
     * @return A list without alias. If no alias is contained the path is
     *         returned unchanged.
     */
    public static List<String> removeAlias(List<String> path) {

        if (path.size() < 2) {
            return path;
        }

        int toIndex = path.size() - 2;
        String alias = path.get(toIndex);

        if (alias.equalsIgnoreCase("as")) {
            return path.subList(0, toIndex);
        }

        return path;
    }

    /**
     * Extracts the alias from a column path list.
     *
     * <pre>
     * [assets, >, asset, ., title, as, name] -> name
     * </pre>
     *
     * @param path
     *            A valid path generated by {@link #getColumnPathAsList(String)}
     * @return the alias or empty string if no alias is defined.
     */
    public static String extractAlias(List<String> path){

        if (path.size() < 2) {
            return StringUtils.EMPTY;
        }

        int toIndex = path.size() - 2;
        String alias = path.get(toIndex);

        if (alias.equalsIgnoreCase("as")) {
            String aliasValue = path.get(path.size() - 1);
            aliasValue = aliasValue.replace("__", " ");
            return aliasValue;
        }

        return StringUtils.EMPTY;
    }

}
