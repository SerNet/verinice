/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
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
 *     Moritz Reiter - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.common.model;

import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;

/**
 * Utility class for creating Links (CnALink). Could get expanded to allow more
 * operations, like removing.
 * 
 * @author Moritz Reiter
 */
public final class LinkUtil {
    
    private static final Logger LOGGER = Logger.getLogger(LinkUtil.class);

    private static final ICommandService commandService = CnAElementHome.getInstance()
            .getCommandService();

    private LinkUtil() {
    }

    public static void createLink(CnATreeElement source, CnATreeElement target, String relationId) {
        CreateLink<CnATreeElement, CnATreeElement> command = new CreateLink<>(source,
                target, relationId);
        try {
            commandService.executeCommand(command);
        } catch (CommandException e) {
            // SonarLint 1.0.0 is not satisfied with this. Looks like a false positive to me.
            LOGGER.error("Link creation failed", e);
        }
    }

    public static void createLinks(Set<CnATreeElement> sources, CnATreeElement target,
            String relationId) {
        for (CnATreeElement source : sources) {
            CreateLink<CnATreeElement, CnATreeElement> command = new CreateLink<>(source,
                    target, relationId);
            try {
                commandService.executeCommand(command);
            } catch (CommandException e) {
                // SonarLint 1.0.0 is not satisfied with this. Looks like a false positive to me.
                LOGGER.error("Link creation failed", e);
            }
        }
    }

    public static void createLinks(CnATreeElement source, Set<CnATreeElement> targets,
            String relationId) {
        for (CnATreeElement target : targets) {
            CreateLink<CnATreeElement, CnATreeElement> command = new CreateLink<>(source,
                    target, relationId);
            try {
                commandService.executeCommand(command);
            } catch (CommandException e) {
                // SonarLint 1.0.0 is not satisfied with this. Looks like a false positive to me.
                LOGGER.error("Link creation failed", e);
            }
        }
    }
}
