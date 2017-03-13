/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
package sernet.verinice.web.poseidon.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.web.ElementInformation;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class LazyVeriniceTreeNode extends DefaultTreeNode {

    private final Logger log = Logger.getLogger(LazyVeriniceTreeNode.class);


    private static final long serialVersionUID = 1L;

    private ElementInformation data;

    private boolean childrenLoaded = false;

    public LazyVeriniceTreeNode(ElementInformation data, LazyVeriniceTreeNode parent) {
        super(data, parent);
        this.data = data;

    }

    @Override
    public List<TreeNode> getChildren() {
        return super.getChildren();
    }

    @Override
    public int getChildCount() {
        return super.getChildCount();
    }

    @Override
    public boolean isLeaf() {
       return false;
    }

    void fetchChildren() {

        if (!childrenLoaded && data != null) {

            childrenLoaded = true;

            CnATreeElement element = data.getElement();

            List<LazyVeriniceTreeNode> elementInformations = new ArrayList<>();
            for (CnATreeElement e : element.getChildren()) {
                ElementInformation eInformation = new ElementInformation(e);
                elementInformations.add(new LazyVeriniceTreeNode(eInformation, this));
                log.debug("add child: " + eInformation.getTitle());

            }
        }
    }
}
