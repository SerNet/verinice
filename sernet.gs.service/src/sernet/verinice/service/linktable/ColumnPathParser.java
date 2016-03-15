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
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
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
        VqlLexer lexer = new VqlLexer(new StringReader(columnPath));
        VqlParser parser = new VqlParser(lexer);
        try {
            parser.expr();
        } catch (RecognitionException | TokenStreamException e) {
            String message = "Error while parsing VQL column path: " + columnPath;
            LOG.error(message, e);
            throw new ColumnPathParseException(message, e);
        }
        CommonAST parseTree = (CommonAST)parser.getAST();
        IPathElement rootPathElement = createRootPathElement(parseTree);
        addChild(rootPathElement,parseTree.getNextSibling());
        rootPathElement.setAlias(getAlias(parseTree));
        return rootPathElement;
    }

    /**
     * Returns all object type ids of one column path.
     * object type ids are defined in file SNCA.xml.
     *
     * @param columnPathes A column path
     * @return All object type ids in the column path
     */
    public static Set<String> getObjectTypeIds(Set<String> columnPathes) {
        IPathElement[] pathElements = getPathElements(columnPathes);
        return getObjectTypeIds(pathElements);
    }

    /**
     * Returns all object type ids of the path element array.
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

    private static IPathElement createRootPathElement(CommonAST element) {
        return new RootElement(element.getText());
    }

    private static void addChild(IPathElement pathElement, AST element) {
        if(isChild(element)) {
            AST next = element.getNextSibling();
            if(!isChild(next)) {
                String message = "Missing successor of element: " + element.getText() + " in VQL column path";
                LOG.error(message);
                throw new ColumnPathParseException(message);
            }
            IPathElement child = createPathElement(element, next);
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

    private static IPathElement createPathElement(AST element, AST next) {
        IPathElement pathElement = PathElementFactory.getElement(element.getType());
        pathElement.setTypeId(next.getText());
        return pathElement;
    }

}
