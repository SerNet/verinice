/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.service.linktable.generator.GraphLinkedTableCreator;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 * Service to create Link Tables. Link Tables are used as a data source in BIRT
 * reports or to export CSV data. See interface {@link ILinkTableService} for
 * documentation.
 *
 * This implementation uses verinice graphs to load data from the server. It
 * creates {@link GraphCommand}s and executes them with the
 * {@link ICommandService}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkTableService implements ILinkTableService {

    private static final Logger LOG = Logger.getLogger(LinkTableService.class);

    ICommandService commandService;

    /** Default implementation of link creator */
    private LinkedTableCreator linkedTableCreator = new GraphLinkedTableCreator();

    @Override
    public List<List<String>> createTable(ILinkTableConfiguration configuration) {
        VeriniceGraph graph = getVeriniceGraph(configuration);
        return linkedTableCreator.createTable(graph, configuration);
    }

    private VeriniceGraph getVeriniceGraph(ILinkTableConfiguration configuration) {

        GraphCommand graphCommand = createCommand(configuration);
        try {
            graphCommand = getCommandService().executeCommand(graphCommand);
        } catch (CommandException e) {
            LOG.error("Command exception while creating link table", e);
            throw new LinkTableException("Error while creating link table: " + e.getMessage(), e);
        }

        return graphCommand.getGraph();
    }

    @Override
    public List<List<String>> createTable(String vltFilePath) {
        ILinkTableConfiguration conf = VeriniceLinkTableIO.readLinkTableConfiguration(vltFilePath);
        return createTable(conf);
    }

    protected GraphCommand createCommand(ILinkTableConfiguration configuration) {
        GraphCommand command = new GraphCommand();
        GraphElementLoader loader = new GraphElementLoader();
        loader.setScopeIds(configuration.getScopeIdArray());
        Set<String> objectTypeIds = configuration.getObjectTypeIds();
        loader.setTypeIds(objectTypeIds.toArray(new String[objectTypeIds.size()]));
        command.addLoader(loader);
        for (String relation : configuration.getLinkTypeIds()) {
            command.addRelationId(relation);
        }
        return command;
    }

    protected ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private static ICommandService createCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

    @Override
    public void setLinkTableCreator(LinkedTableCreator linkedTableCreator) {
        this.linkedTableCreator = linkedTableCreator;
    }

}
