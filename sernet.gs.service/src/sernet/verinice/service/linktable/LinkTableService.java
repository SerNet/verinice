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

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 * Service to create Link Tables. Link Tables are used as a data source in BIRT reports or
 * to export CSV data. See interface {@link ILinkTableService} for documentation.
 *
 * This implementation uses verinice graphs to load data from the server.
 * It creates {@link GraphCommand}s and executes them with the {@link ICommandService}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkTableService implements ILinkTableService {

    private static final Logger LOG = Logger.getLogger(LinkTableService.class);

    ICommandService commandService;

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.ILinkTableService#createTable(sernet.verinice.report.service.impl.dynamictable.LinkTableConfiguration)
     */
    @Override
    public List<List<String>> createTable(ILinkTableConfiguration configuration) {
        try {
            return doCreateTable(configuration);
        } catch (CommandException e) {
            LOG.error("Command exception while creating link table", e);
            throw new RuntimeCommandException(e);
        } catch (RuntimeException e) {
            LOG.error("RuntimeException while creating link table", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error while creating link table", e);
            throw new LinkTableException("Error while creating link table: " + e.getMessage() ,e);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.report.service.impl.dynamictable.ILinkTableService#createTable(java.lang.String)
     */
    @Override
    public List<List<String>> createTable(String vltFilePath) {
        try {
            return doCreateTable(VeriniceLinkTableIO.readLinkTableConfiguration(vltFilePath));
        } catch (CommandException e) {
            LOG.error("Command exception while creating link table", e);
            throw new RuntimeCommandException(e);
        } catch (RuntimeException e) {
            LOG.error("RuntimeException while creating link table", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error while creating link table", e);
            throw new LinkTableException("Error while creating link table: " + e.getMessage() ,e);
        }
    }

    private List<List<String>> doCreateTable(ILinkTableConfiguration configuration) throws CommandException {
        GraphCommand command = createCommand(configuration);
        command = getCommandService().executeCommand(command);
        GenericDataModel dm = new GenericDataModel(command.getGraph(), configuration);
        dm.init();
        return dm.getResult();
    }

    private GraphCommand createCommand(ILinkTableConfiguration configuration) {
        GraphCommand command = new GraphCommand();
        GraphElementLoader loader = new GraphElementLoader();
        loader.setScopeIds(configuration.getScopeIdArray());
        Set<String> objectTypeIds = configuration.getObjectTypeIds();
        loader.setTypeIds(objectTypeIds.toArray(new String[objectTypeIds.size()]));
        command.addLoader(loader);
        for(String relation : configuration.getLinkTypeIds()){
            command.addRelationId(relation);
        }
        return command;
    }

    private ICommandService getCommandService() {
        if(commandService==null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private static ICommandService createCommandService() {
        return(ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
