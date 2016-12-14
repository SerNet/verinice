package sernet.verinice.service.linktable.mergevql;
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

import static org.junit.Assert.assertTrue;
import static sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO.readLinkTableConfiguration;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.Test;

import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.generator.mergepath.VqlAst;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Not really a test. Prints only the {@link VqlAst} to the stdout for analyzing
 * purposes.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VqlAstTest {

    private static final String ROOT_ELEMENT = "auditgroup";
    private final String TEST_VLT_FILE = "child-relations.vlt";

    @Test
    public void testMergingVqlTrees(){
        ILinkTableConfiguration conf = readLinkTableConfiguration(getFilePath(TEST_VLT_FILE));
        VqlAst mergedVqlAst = new VqlAst(conf);

        DirectedGraph<VqlNode, VqlEdge> vqlAst = mergedVqlAst.getVqlGraph();

        VqlNode root = mergedVqlAst.getRoot();

        BreadthFirstIterator<VqlNode, VqlEdge> breadthFirstIterator = new BreadthFirstIterator<>(vqlAst, root);
        while (breadthFirstIterator.hasNext()){
            VqlNode next = breadthFirstIterator.next();
            assertTrue(next.getPath().contains(ROOT_ELEMENT));
            testForRoot(vqlAst, next);
            assertTrue("was " + next.getPropertyTypes().size() + " for " + next,
                    next.getPropertyTypes().size() == 1);
            Set<String> pathes = new HashSet<>();
            assertProperty(next);
            for (VqlEdge edge : vqlAst.outgoingEdgesOf(next)) {
                assertUniquePath(pathes, edge);
                assertTrue("was type " + edge.getEdgeType(), edge.getEdgeType() == EdgeType.CHILD);
                assertTrue(edge.getPath().contains(ROOT_ELEMENT));

            }
        }
    }

    private void assertUniquePath(Set<String> pathes, VqlEdge edge) {
        assertTrue("path " + edge.getPath() + " already inserted, path must be unique",
                pathes.add(edge.getPath()));
    }

    private void assertProperty(VqlNode next) {
        String property = next.getPropertyTypes().toArray(new String[1])[0];
        assertTrue("text must be part of propertyId", property.contains(next.getTypeId()));
    }

    private void testForRoot(DirectedGraph<VqlNode, VqlEdge> vqlAst, VqlNode next) {
        if (next.getPath().equals(ROOT_ELEMENT)) {
            assertTrue(vqlAst.outgoingEdgesOf(next).size() == 1);
        }
    }

    private String getFilePath(String fileName) {
        return this.getClass().getResource(fileName).getPath();
    }
}
