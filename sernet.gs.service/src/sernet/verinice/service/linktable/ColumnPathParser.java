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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import sernet.verinice.service.linktable.antlr.VqlLexer;
import sernet.verinice.service.linktable.antlr.VqlParser;

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

    private static final String ALIAS_DELIMITER = " AS ";
    
    private ColumnPathParser() throws InstantiationException {
        throw new InstantiationException("Do not create instances of ColumnPathParser, use public static methods");
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

        if ("as".equalsIgnoreCase(alias)) {
            return path.subList(0, toIndex);
        }

        return path;
    }
    
    /**
     * Extracts the alias from a column path element list.
     *
     * <pre>
     * [assets, >, asset, ., title, as, name] -> name
     * </pre>
     *
     * @param pathElementList
     *            A valid list of path elements generated by {@link #getColumnPathAsList(String)}
     * @return the alias or empty string if no alias is defined.
     */
    public static String extractAlias(List<String> pathElementList){

        if (pathElementList.size() < 2) {
            return StringUtils.EMPTY;
        }

        int toIndex = pathElementList.size() - 2;
        String alias = pathElementList.get(toIndex);

        if ("as".equalsIgnoreCase(alias)) {
            String aliasValue = pathElementList.get(pathElementList.size() - 1);
            return aliasValue.replace("_", " ");
        }

        return StringUtils.EMPTY;
    }
    
    /**
     * Extracts the alias from a column path. This method does not use
     * the ANTLR parser due to problems with German umlauts.

     *
     * @param path A valid path 
     * @return the alias or empty string if no alias is defined.
     */
    public static String extractAlias(String path){

        if(path.contains(ALIAS_DELIMITER)) {
            return path.substring(path.indexOf(ALIAS_DELIMITER) + 4);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Returns all object type ids of a column path.
     * 
     * @param columnPath A column path 
     * @return All object type ids of a column path
     */
    public static Set<String> getObjectTypeIds(String columnPath) {
        Set<String> objectTypeIds = new HashSet<>();
        List<String> pathElements = ColumnPathParser.getColumnPathAsList(columnPath);
        for (Iterator<String> pathElementIterator = pathElements.iterator(); pathElementIterator.hasNext();) {
            objectTypeIds.add(pathElementIterator.next());
            if(pathElementIterator.hasNext() && ".".equals(pathElementIterator.next())) {
                break;
            }    
        }
        return objectTypeIds;
    }

}
