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
package sernet.verinice.web.poseidon.services;

import java.io.Serializable;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.service.model.IObjectModelService;

/**
 * Provides Interface to Spring IoC in order to retrieve data for web
 * visualizations.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class GenericChartService implements Serializable {

    IObjectModelService getObjectService() {
        return (IObjectModelService) VeriniceContext.get(VeriniceContext.OBJECT_MODEL_SERVICE);
    }

    IGraphService getGraphService() {
        return (IGraphService) VeriniceContext.get(VeriniceContext.GRAPH_SERVICE);
    }
}
