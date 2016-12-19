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

import antlr.CommonAST;
import antlr.collections.AST;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.antlr.VqlParser;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;

/**
 * Merges all column path to tree, which is a kind of a VQL abstract syntax
 * tree.
 *
 * <p>
 * So this input of column path:
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
        this.vqlGraph = new DefaultDirectedGraph<>(VqlEdge.class);
        createQueryTree();
    }

    /**
     * Returns the root of the VqlQuery
     *
     * @return The root node of the VQL-AST. In the example above it would be
     *         the assetgroup.
     */
    public VqlNode getRoot() {
        return root;
    }

    private void createQueryTree() {

        for (String columnPath : linkTableConfiguration.getColumnPaths()) {

            VqlParser parser = ColumnPathParser.parse(columnPath);
            CommonAST ast = (CommonAST) parser.getAST();

            root = getNode(ast.getText(), ast.getText());
            vqlGraph.addVertex(root);

            traverseColumnPathAst(ast.getNextSibling());
        }
    }

    /**
     * Creates a decorated abstracted syntax tree.
     *
     *
     * @param ast
     *            It is the first operator of a column path, e.g. ('>' | '<' |
     *            '.').
     */
    private void traverseColumnPathAst(AST ast) {

        // Short overview over the algorithm:
        // <pre>
        // assetgroup > assetgroup > asset / control . title
        // | | |
        // leftNode op rightNode
        // </pre>
        VqlNode leftNode = root;
        AST op = ast;
        VqlNode rightNode = null;
        VqlEdge incomingEdge = null;

        // The edge type from antlr. We need this to make the decision if a
        // property belongs to hui relation.
        int lastEdgeType = NO_EDGE_TYPE;

        while (op != null) {

            String valueOfRightNode = op.getNextSibling().getText();

            if (PROP == op.getType()) {
                handleProperty(leftNode, incomingEdge, lastEdgeType, valueOfRightNode);
                break;
            }

            if (LT == op.getType()) {
                String nodePath = leftNode.getPath() + "/" + valueOfRightNode;
                String edgePath = getEdgePathForLink(leftNode, incomingEdge, valueOfRightNode);
                rightNode = getNode(op.getNextSibling().getText(), nodePath);
                incomingEdge = getEdge(EdgeType.LINK, edgePath, leftNode, rightNode);
                lastEdgeType = LT;
            }

            if (LINK == op.getType()) {
                String nodePath = leftNode.getPath() + "/" + valueOfRightNode;
                String edgePath = getEdgePathForLink(leftNode, incomingEdge, valueOfRightNode);
                rightNode = getNode(op.getNextSibling().getText(), nodePath);
                incomingEdge = getEdge(EdgeType.LINK, edgePath, leftNode, rightNode);
                lastEdgeType = LINK;
            }

            if (CHILD == op.getType()) {
                String path = leftNode.getPath() + ">" + valueOfRightNode;
                rightNode = getNode(op.getNextSibling().getText(), path);
                incomingEdge = getEdge(EdgeType.CHILD, path, leftNode, rightNode);
                lastEdgeType = CHILD;
            }

            if (PARENT == op.getType()) {
                String path = leftNode.getPath() + "<" + valueOfRightNode;
                rightNode = getNode(op.getNextSibling().getText(), path);
                incomingEdge = getEdge(EdgeType.PARENT, path, leftNode, rightNode);
                lastEdgeType = PARENT;
            }

            addNodeToGraph(rightNode);
            addEdgeToGraph(leftNode, rightNode, incomingEdge);

            // shift to next triple: node op node
            leftNode = rightNode;
            op = op.getNextSibling().getNextSibling();
        }
    }

    private String getEdgePathForLink(VqlNode leftNode, VqlEdge incomingEdge, String valueOfRightNode) {
        String edgePath = (incomingEdge == null ? leftNode.getPath() : incomingEdge.getPath()) + "/" + valueOfRightNode;
        return edgePath;
    }

    private void handleProperty(VqlNode leftNode, VqlEdge incomingEdge, int lastEdgeType, String valueOfRightNode) {
        if (lastEdgeType != NO_EDGE_TYPE && LT == lastEdgeType) {
            incomingEdge.addPropertyType(valueOfRightNode);
        } else {
            leftNode.addPropertyType(valueOfRightNode);
        }
    }

    private void addEdgeToGraph(VqlNode leftNode, VqlNode rightNode, VqlEdge vqlEdge) {
        if (!vqlGraph.containsEdge(vqlEdge)) {
            vqlGraph.addEdge(leftNode, rightNode, vqlEdge);
        }
    }

    private void addNodeToGraph(VqlNode rightNode) {
        if (!vqlGraph.containsVertex(rightNode)) {
            vqlGraph.addVertex(rightNode);
        }
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

    public Set<VqlEdge> getOutgoingEdges(VqlNode node){
        return vqlGraph.outgoingEdgesOf(node);
    }

    public DirectedGraph<VqlNode, VqlEdge> getVqlGraph() {
        return vqlGraph;
    }
}
