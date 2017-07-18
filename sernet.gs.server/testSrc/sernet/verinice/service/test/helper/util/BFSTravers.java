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
package sernet.verinice.service.test.helper.util;

import java.util.LinkedList;
import java.util.Queue;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class BFSTravers implements CnATreeTraverser {

    @Override
    public void traverse(CnATreeElement root, CallBack callback) {

        root = Retriever.retrieveElement(root, new RetrieveInfo().setChildren(true));

        Queue<CnATreeElement> queue = new LinkedList<CnATreeElement>();
        queue.add(root);
        CnATreeElement current;

        while (!queue.isEmpty()) {
            current = queue.poll();
            current = (CnATreeElement) Retriever.retrieveElement(current, new RetrieveInfo().setChildren(true));
            queue.addAll(current.getChildren());
            callback.execute(current);
        }
    }
}
