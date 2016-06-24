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

import static sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO.readLinkTableConfiguration;

import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.Test;

import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.generator.mergepath.Path;
import sernet.verinice.service.linktable.generator.mergepath.Path.PathElement;
import sernet.verinice.service.linktable.generator.mergepath.VqlAst;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Not really a test. Prints only the {@link VqlAst} to the stdout for analyzing purposes.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class VqlAstTest {


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
            System.out.println(next);
            for(VqlEdge edge : vqlAst.outgoingEdgesOf(next))
                System.out.println("\t" + edge);
        }
    }

    private String getFilePath(String fileName) {
        return this.getClass().getResource(fileName).getPath();
    }

    @Test
    public void testGetPathes(){

        ILinkTableConfiguration conf = readLinkTableConfiguration(getFilePath(TEST_VLT_FILE));
        VqlAst mergedVqlAst = new VqlAst(conf);

        for(Path p : mergedVqlAst.getPaths()){
            for(PathElement pElement : p.getPathElements()){
                System.out.print(pElement.node + " <----- ");
                System.out.print(pElement.edge);
                System.out.println("\n");
            }
            System.out.println("");
        }

    }
}
