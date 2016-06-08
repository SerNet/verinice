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
import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.PARENT;
import static sernet.verinice.service.linktable.antlr.VqlParserTokenTypes.PROP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import antlr.CommonAST;
import antlr.collections.AST;
import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.antlr.VqlParser;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VqlAst {

    private final static Logger LOG = Logger.getLogger(VqlAst.class);

    private ILinkTableConfiguration linkTableConfiguration;
    private DirectedGraph<VqlNode, VqlEdge> vqlAst;
    private VqlNode root;

    public VqlAst(ILinkTableConfiguration linkTableConfiguration) {
        this.linkTableConfiguration = linkTableConfiguration;
        this.setVqlAst(new DefaultDirectedGraph<VqlNode, VqlEdge>(VqlEdge.class));
        createQueryTree();
    }

    public VqlNode getRoot() {
        return root;
    }

    private void createQueryTree() {

        for (String columnPath : linkTableConfiguration.getColumnPathes()) {

            VqlParser parser = ColumnPathParser.parse(columnPath);
            CommonAST ast = (CommonAST) parser.getAST();

            root = getNode(ast.getText(), ast.getText());
            vqlAst.addVertex(root);

            traverseAst(ast.getNextSibling(), root);
        }
    }

    private void traverseAst(AST ast, VqlNode lastNode) {

        if (ast == null) {
            return;
        }

        VqlNode currentNode = null;
        VqlEdge vqlEdge = null;
        String text = ast.getNextSibling().getText();

        if (PROP == ast.getType()) {
            lastNode.addPropertyType(text);
            System.out.println("add property: " + text);
            return;
        }

        if (LINK == ast.getType()) {
            String path = lastNode.getPath() + "/" + text;
            currentNode = getNode(ast.getNextSibling().getText(), path);
            vqlEdge = getEdge(EdgeType.CHILD, lastNode.getPath(), lastNode, currentNode);
        }

        if (CHILD == ast.getType()) {
            String path = lastNode.getPath() + ">" + text;
            currentNode = getNode(ast.getNextSibling().getText(), path);
            vqlEdge = getEdge(EdgeType.CHILD, lastNode.getPath(), lastNode, currentNode);
        }

        if (PARENT == ast.getType()) {
            String path = lastNode.getPath() + "<" + text;
            currentNode = getNode(ast.getNextSibling().getText(), path);
            vqlEdge = getEdge(EdgeType.PARENT, lastNode.getPath(), lastNode, currentNode);
        }

        
        if(!vqlAst.containsVertex(currentNode)){
            vqlAst.addVertex(currentNode);
        }
        
        if(!vqlAst.containsEdge(vqlEdge)){
            vqlAst.addEdge(lastNode, currentNode, vqlEdge);
        }
        
        traverseAst(ast.getNextSibling().getNextSibling(), currentNode);
    }

    Map<VqlNode, VqlNode> nodes = new HashMap<>();

    private VqlNode getNode(String text, String path) {
        VqlNode node = new VqlNode(text, path);
        if (nodes.containsKey(node)) {
            return nodes.get(node);
        }

        nodes.put(node, node);
        return nodes.get(node);
    }

    Map<VqlEdge, VqlEdge> edges = new HashMap<>();

    private VqlEdge getEdge(EdgeType type, String path, VqlNode lastNode, VqlNode currentNode) {
        VqlEdge vqlEdge = new VqlEdge(EdgeType.LINK, lastNode.getPath(), lastNode, currentNode);
        if (edges.containsKey(vqlEdge)) {
            return edges.get(vqlEdge);
        }

        edges.put(vqlEdge, vqlEdge);
        return edges.get(vqlEdge);
    }

    public DirectedGraph<VqlNode, VqlEdge> getVqlAst() {
        return vqlAst;
    }

    public void setVqlAst(DirectedGraph<VqlNode, VqlEdge> vqlAst) {
        this.vqlAst = vqlAst;
    }

    public Set<Path> getPaths() {

        final Stack<VertexTraversalEvent<VqlNode>> stack = new Stack<>();
        final Set<Path> paths = new HashSet<>();

        DepthFirstIterator<VqlNode, VqlEdge> iterator = new DepthFirstIterator<>(vqlAst, root);
        iterator.addTraversalListener(new TraversalListener<VqlNode, VqlEdge>() {

            @Override
            public void vertexTraversed(VertexTraversalEvent<VqlNode> e) {

                stack.push(e);

                if (isLeaf(e.getVertex())) {
                    paths.add(createPath(stack));
                }
            }

            @Override
            public void vertexFinished(VertexTraversalEvent<VqlNode> e) {
                stack.pop();
            }

            @Override
            public void edgeTraversed(EdgeTraversalEvent<VqlNode, VqlEdge> e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
                // TODO Auto-generated method stub

            }

            private Path createPath(Stack<VertexTraversalEvent<VqlNode>> stack) {

                Iterator<VertexTraversalEvent<VqlNode>> pathIterator = stack.iterator();
                Path path = new Path();

                while (pathIterator.hasNext()) {
                    VertexTraversalEvent<VqlNode> next = pathIterator.next();
                    path.addPathElement((PathElement) next.getVertex());
                }

                return path;
            }

            private boolean isLeaf(VqlNode vertex) {
                return vqlAst.outgoingEdgesOf(vertex).size() == 0;
            }
        });

        while (iterator.hasNext()) {
            iterator.next();
        }

        return paths;
    }

    /**
     * Filter for nodes which properties should be written to the result table.
     * 
     * Returns all nodes in abstract syntax tree, which contains at least one a
     * property type.
     */
    public Set<VqlNode> getMatchedNodes() {
        Set<VqlNode> matchingNodes = new HashSet<>();
        for (VqlNode n : vqlAst.vertexSet()) {
            if (n.isMatch()) {
                matchingNodes.add(n);
            }
        }

        return matchingNodes;
    }
}
