/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Stack;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * Tests if the path /org/asset_group/.../asset is correct loaded.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class LoadAncestorsTest extends CommandServiceProvider {

    private static final int MAX_TEST_TREE_DEPTH = 100;

    @Resource(name = "commandService")
    protected ICommandService commandService;

    private static final Logger LOG = Logger.getLogger(LoadAncestorsTest.class);

    private Organization organization;

    @Test
    public void execute() {

        try {
            LOG.info("test " + LoadAncestorsTest.class.getCanonicalName());

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            organization = createOrganization(getClass().getSimpleName() + "_" + dateFormat.format(cal.getTime()));
            PathAndLeafHolder pathAndLeafHolder = new PathAndLeafHolder();
            pathAndLeafHolder.appendElementToPath(organization.getTitle());
            int depth = new Random().nextInt(MAX_TEST_TREE_DEPTH);
            LOG.info("start creating a tree of depth " + depth);
            pathAndLeafHolder = makeDeepHierachy(organization, depth, pathAndLeafHolder);
            LOG.info("created a tree of depth " + depth);

            // fetch the child from the database
            RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
            LoadAncestors command = new LoadAncestors(pathAndLeafHolder.leaf.getTypeId(), pathAndLeafHolder.leaf.getUuid(), ri);
            command = commandService.executeCommand(command);
            CnATreeElement databaseChild = command.getElement();

            // compare the local path and the one fetched from database
            while (databaseChild.getParent() != null) {
                assertTrue("check path part " + databaseChild.getTitle() + "failed", databaseChild.getTitle().equals(pathAndLeafHolder.popLastElementFromPath()));
                databaseChild = databaseChild.getParent();
            }

            assertTrue(databaseChild.getTypeId().equals(ISO27KModel.TYPE_ID));

        } catch (CommandException e) {
            fail("initiating organization failed");
        }
    }

    /**
     * Builds one deep hierarchy and put exactly one child at the end.
     * 
     * @param org
     *            The organization which is used for the deep tree.
     * @param depth
     *            The resulting depth of the tree.
     * @throws CommandException
     */
    @SuppressWarnings("unchecked")
    public PathAndLeafHolder makeDeepHierachy(Organization org, int depth, PathAndLeafHolder pathAndLeafHolder) throws CommandException {

        assertNotNull("organization may not be null", org);
        assertFalse("organization may not be empty", org.getChildren().isEmpty());

        // get more or less random asset group
        Group<CnATreeElement> assetGroupRoot = (Group<CnATreeElement>) org.getChildren().iterator().next();
        pathAndLeafHolder.appendElementToPath(assetGroupRoot.getTitle());

        return makeDeepHierarchy(depth, assetGroupRoot, pathAndLeafHolder);

    }

    /**
     * Builds a deep degenerated tree with one leaf/child
     * 
     * @param children
     * @param depth
     * @throws CommandException
     */
    private PathAndLeafHolder makeDeepHierarchy(int depth, Group<CnATreeElement> parent, PathAndLeafHolder pathAndLeafHolder) throws CommandException {

        if (depth <= 0) {
            CnATreeElement c = createNewElement(parent, depth);
            parent.addChild(c);
            pathAndLeafHolder.appendElementToPath(c.getTitle());
            pathAndLeafHolder.leaf = c;
            return pathAndLeafHolder;
        }

        @SuppressWarnings("unchecked")
        Group<CnATreeElement> child = (Group<CnATreeElement>) createNewNamedGroup(parent, parent.getTitle().split("-")[0] + "-" + depth);
        parent.addChild(child);
        pathAndLeafHolder.appendElementToPath(child.getTitle());
        return makeDeepHierarchy(depth - 1, child, pathAndLeafHolder);
    }

    @After
    public void removeElements() throws CommandException {
        removeOrganization(organization);
    }

    /**
     * Holds the path and the deepest element of the generated by {#link
     * {@link LoadAncestors#execute()} hierarchy.
     * 
     * 
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     * 
     */
    static class PathAndLeafHolder {
        private Stack<String> treePath;
        private CnATreeElement leaf;

        PathAndLeafHolder() {
            treePath = new Stack<String>();
        }

        public String popLastElementFromPath() {
            return treePath.pop();
        }

        public void appendElementToPath(String element) {
            treePath.push(element);
        }
    }

}
