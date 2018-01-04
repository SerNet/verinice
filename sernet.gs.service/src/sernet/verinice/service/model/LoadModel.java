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
package sernet.verinice.service.model;

import java.util.List;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 * Load model classes. Model are special, because there is only one instance in
 * a verinice life cycle.
 *
 * @see ISO27KModel
 * @see BSIModel
 * @see CatalogModel
 * @see BpModel
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class LoadModel<T extends CnATreeElement> extends GenericCommand implements INoAccessControl {

    private static final long serialVersionUID = 1L;

    private Class<T> modelClass;

    private T model;

    public LoadModel(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    public void execute() {
        RetrieveInfo ri = new RetrieveInfo();
        ri.setChildren(true);
        @SuppressWarnings("unchecked")
        List<T> modelList = getDaoFactory().getDAO(modelClass).findAll(ri);
        if (modelList != null) {
            if (modelList.size() > 1) {
                throw new LoadModelException("More than one " + modelClass.getSimpleName() + " found.");
            } else if (modelList.size() == 1) {
                model = modelList.get(0);
            }
        }
    }

    public T getModel() {
        return model;
    }

}
