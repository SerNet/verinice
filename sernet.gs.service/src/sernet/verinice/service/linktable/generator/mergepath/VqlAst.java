/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable.generator.mergepath;

import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.CHILD;
import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.LINK;
import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.LT;
import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.PARENT;
import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.PROP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import antlr.CommonAST;
import antlr.collections.AST;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.antlr.VqlParser;
import sernet.verinice.service.linktable.antlr.VqlParserTokenTypes;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;

/**
 * Merges all column pathes to tree, which is a kind of a VQL abstract syntax
 * tree.
 *
 * <p>
 * So this input of column pathes:
 * </p>
 *
 * <pre>
 *  assetgroup.title
 *  assetgroup > asset.title
 *  assetgroup > asset.description
 *  assetgroup > asset:control.title
 *  assetgroup > asset / control.title
 * </pre>
 *
 * <p>
 * Is converted to a tree with directed edges like this:
 * </p>
 *
 * <pre>
 *
 *                    * assetgroup[title]
 *                    |
 *                    * asset[title, description]
 *                   / \
 *  CnaLink[title]  /   \
 *                 /     \
 * control[title] *       * person[name, surname]
 * </pre>
 *
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VqlAst {

    private static final int NO_EDGE_TYPE = -1;
    private ILinkTableConfiguration linkTableConfiguration;
    private DirectedGraph<VqlNode, VqlEdge> vqlGraph;
    private VqlNode root;

    // keep track of already created nodes.
    Map<VqlEdge, VqlEdge> edges = new HashMap<>();
    Map<VqlNode, VqlNode> nodes = new HashMap<>();

    /**
     * Creates a VQL-Ast.
     *
     * @param linkTableConfiguration
     *            May not be null.
     *
     */
    public VqlAst(ILinkTableConfiguration linkTableConfiguration) {
        this.linkTableConfiguration = linkTableConfiguration;
        this.vqlGraph = new DefaultDirectedGraph<VqlNode, VqlEdge>(VqlEdge.class);
        createQueryTree();
    }

    /**
     * Returns the root of the VqlQuery
     *
     * <pre>
     *
     *                    * assetgroup[title]
     *                    |
     *                    * asset[title, description]
     *                   / \
     *  CnaLink[title]  /   \
     *                 /     \
     * control[title] *       * person[name, surname]
     * </pre>
     *
     * @return The root node of the VQL-AST. In the example above it would be
     *         the assetgroup.
     */
    public VqlNode getRoot() {
        return root;
    }

    private void createQueryTree() {

        for (String columnPath : linkTableConfiguration.getColumnPathes()) {

            VqlParser parser = ColumnPathParser.parse(columnPath);
            CommonAST ast = (CommonAST) parser.getAST();

            root = getNode(ast.getText(), ast.getText());
            vqlGraph.addVertex(root);

            traverseColumnPathAst(ast.getNextSibling(), root, null, NO_EDGE_TYPE);
        }
    }

    /**
     * Creates a decorated abstracted syntax tree.
     *
     * <pre>
     * assetgroup   >   assetgroup > asset / control . title
     *      |       |       |
     *   leftNode   op   rightNode
     * </pre>
     *
     *
     * @param op
     *            Is the recursion anchor. If a {@link VqlParserTokenTypes#PROP}
     *            is seen the end of the path is reached and the recursion
     *            stops.
     * @param leftNode
     *            The node of the left side of the operator.
     * @param incomingEdge
     *            The incoming edge. In the example above it is not the dot
     *            operator, it is the child operator. May be null.
     * @param lastEdgeType
     *            The type which is provided from the {@link ColumnPathParser}
     *            of the last incoming edge. May be null.
     */
    private void traverseColumnPathAst(AST op, VqlNode leftNode, VqlEdge incomingEdge, int lastEdgeType) {

        if (op == null) {
            return;
        }

        VqlNode rightNode = null;
        VqlEdge vqlEdge = null;
        String valueOfRightNode = op.getNextSibling().getText();
        int nextEdgeType = NO_EDGE_TYPE;

        if (PROP == op.getType()) {

            if (lastEdgeType != NO_EDGE_TYPE && LT == lastEdgeType) {
                incomingEdge.addPropertyType(valueOfRightNode);
            } else {
                leftNode.addPropertyType(valueOfRightNode);
            }

            return;
        }

        if (LT == op.getType()) {
            String nodePath = leftNode.getPath() + "/" + valueOfRightNode;
            String edgePath = (incomingEdge == null ? leftNode.getPath() : incomingEdge.getPath()) + ":" + valueOfRightNode;
            rightNode = getNode(op.getNextSibling().getText(), nodePath);
            vqlEdge = getEdge(EdgeType.LINK, edgePath, leftNode, rightNode);
            nextEdgeType = LT;
        }

        if (LINK == op.getType()) {
            String nodePath = leftNode.getPath() + "/" + valueOfRightNode;
            String edgePath = (incomingEdge == null ? leftNode.getPath() : incomingEdge.getPath()) + ":" + valueOfRightNode;
            rightNode = getNode(op.getNextSibling().getText(), nodePath);
            vqlEdge = getEdge(EdgeType.LINK, edgePath, leftNode, rightNode);
            nextEdgeType = LINK;
        }

        if (CHILD == op.getType()) {
            String path = leftNode.getPath() + ">" + valueOfRightNode;
            rightNode = getNode(op.getNextSibling().getText(), path);
            vqlEdge = getEdge(EdgeType.CHILD, path, leftNode, rightNode);
            nextEdgeType = CHILD;
        }

        if (PARENT == op.getType()) {
            String path = leftNode.getPath() + "<" + valueOfRightNode;
            rightNode = getNode(op.getNextSibling().getText(), path);
            vqlEdge = getEdge(EdgeType.PARENT, path, leftNode, rightNode);
            nextEdgeType = PARENT;
        }

        if (!vqlGraph.containsVertex(rightNode)) {
            vqlGraph.addVertex(rightNode);
        }

        if (!vqlGraph.containsEdge(vqlEdge)) {
            vqlGraph.addEdge(leftNode, rightNode, vqlEdge);
        }

        AST nextLeftNode = op.getNextSibling().getNextSibling();
        traverseColumnPathAst(nextLeftNode, rightNode, vqlEdge, nextEdgeType);
    }

    private VqlNode getNode(String text, String path) {
        VqlNode node = new VqlNode(text, path);
        if (nodes.containsKey(node)) {
            return nodes.get(node);
        }

        nodes.put(node, node);
        return nodes.get(node);
    }

    private VqlEdge getEdge(EdgeType type, String path, VqlNode lastNode, VqlNode currentNode) {
        VqlEdge vqlEdge = new VqlEdge(type, path, lastNode, currentNode);
        if (edges.containsKey(vqlEdge)) {
            return edges.get(vqlEdge);
        }

        edges.put(vqlEdge, vqlEdge);
        return edges.get(vqlEdge);
    }

    /**
     * Returns all possible pathes of the vql tree to the each leaf. So in this
     * case:
     *
     *
     * <pre>
     *
     *                    * assetgroup[title]
     *                    |
     *                    * asset[title, description]
     *                   / \
     *  CnaLink[title]  /   \ CnaLink
     *                 /     \
     * control[title] *       * person[name, surname]
     * </pre>
     *
     * The method returns two pathes:
     *
     * <pre>
     *      [
     *          [(assetgroup, null), (asset, child), (control, link)],
     *          [(assetgroup, null), (asset, child), (person, link)]
     *      ]
     * </pre>
     *
     * @return A list of all possible{@link Path} to each leaf.
     */
    public final Set<Path> getPaths() {

        DepthFirstIterator<VqlNode, VqlEdge> iterator = new DepthFirstIterator<>(vqlGraph, root);
        FilterPathes filterPathes = new FilterPathes(this);
        iterator.addTraversalListener(filterPathes);

        // iterate of the whole tree
        while (iterator.hasNext()) {
            iterator.next();
        }

        return filterPathes.getPaths();
    }

    /**
     * Filter for nodes which properties should be written to the result table.
     * 
     * Returns all nodes in abstract syntax tree, which contains at least one a
     * property type.
     */
    public Set<VqlNode> getMatchedNodes() {
        Set<VqlNode> matchingNodes = new HashSet<>();
        for (VqlNode n : vqlGraph.vertexSet()) {
            if (n.isMatch()) {
                matchingNodes.add(n);
            }
        }

        return matchingNodes;
    }

    /**
     * Filter for edges with {@link EdgeType#LINK} and at least one property
     * type.
     *
     */
    public Set<VqlEdge> getMatchedEdges() {
        Set<VqlEdge> matchingEdges = new HashSet<>();
        for (VqlEdge e : vqlGraph.edgeSet()) {
            if (e.isMatch()) {
                matchingEdges.add(e);
            }
        }

        return matchingEdges;
    }

    public DirectedGraph<VqlNode, VqlEdge> getVqlGraph() {
        return vqlGraph;
    }
}
