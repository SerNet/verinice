/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.List;

import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * Use IGraphCommand to operate with a VeriniceGraph.
 *
 * Do not override execute in a GraphCommand, use
 * executeWithGraph instead.
 * 
 * Before executing you should add {@link IGraphElementLoader}
 * and relation ids to narrow elements in the graph. 
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IGraphCommand extends ICommand {

    /**
     * Override this method to operate with the {@link VeriniceGraph}.
     * VeriniceGraph is initialized before this method is called.
     * Do not use execute in a GraphCommand.
     */
    void executeWithGraph();
    
    /**
     * Returns the {@link VeriniceGraph} when
     * execute was called from CommandService.
     * 
     * @return The VeriniceGraph
     */
    VeriniceGraph getGraph();

    /**
     * Add a relation id. Command will load only relations with one of the
     * id you set here.
     * 
     * @param id A relation id from SNCA.xml
     */
    void addRelationId(String id);
    
    
    /**
     * Set relation ids. Command will load only relations with one of the
     * ids you set here.
     * 
     * @param relationIdList A list of relation ids
     */
    void setRelationIds(List<String> relationIdList);
    
    /**
     * Command will load only relations with one of the
     * ids returned of this method.
     * 
     * @return The relation ids
     */
    List<String> getRelationIds();

    /**
     * Add an element loader. Command will use loaders to
     * load elements for the graph
     * 
     * @param loader An element loader
     */
    void addLoader(IGraphElementLoader loader);
    
    
    /**
     * Set the element loaders. Command will use loaders to
     * load elements for the graph
     * 
     * @param loader An list with element loader
     */
    void setLoader(List<IGraphElementLoader> loader);
    
    
    /**
     * @return The element loader list
     */
    List<IGraphElementLoader> getLoader();
    
    /**
     * Sets the graph service. Use by command service to inject the service.
     * @param graphService The graph service
     */
    void setGraphService(IGraphService graphService);
    
    /**
     * @return The graph service
     */
    IGraphService getGraphService();

}
