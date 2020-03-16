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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.service.commands.CnATypeMapper;
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

    public static CnALink createLink(CnATreeElement source, CnATreeElement target,
            String relationId) {
        CreateLink<CnATreeElement, CnATreeElement> command = new CreateLink<>(source, target,
                relationId, false);
        try {
            command = commandService.executeCommand(command);
            CnALink link = command.getLink();
            Domain dependantDomain = CnATypeMapper
                    .getDomainFromTypeId(link.getDependant().getTypeId());
            Domain dependencyDomain = CnATypeMapper
                    .getDomainFromTypeId(link.getDependency().getTypeId());
            Set<Domain> relevantDomains = EnumSet.of(dependantDomain, dependencyDomain);

            if (relevantDomains.contains(Domain.BASE_PROTECTION_OLD)
                    && CnAElementFactory.isModelLoaded()) {
                CnAElementFactory.getLoadedModel().linkAdded(link);
            }
            if (relevantDomains.contains(Domain.ISM) && CnAElementFactory.isIsoModelLoaded()) {
                CnAElementFactory.getInstance().getISO27kModel().linkAdded(link);
            }
            if (relevantDomains.contains(Domain.BASE_PROTECTION)
                    && CnAElementFactory.isBpModelLoaded()) {
                CnAElementFactory.getInstance().getBpModel().linkAdded(link);
            }
            return link;
        } catch (CommandException e) {
            LOGGER.error("Link creation failed", e);
            return null;
        }
    }

    public static void createLinks(Set<CnATreeElement> sources, CnATreeElement target,
            String relationId) {
        for (CnATreeElement source : sources) {
            Optional.ofNullable(createLink(source, target, relationId))
                    .ifPresent(target::addLinkUp);
        }
    }

    public static void createLinks(CnATreeElement source, Set<CnATreeElement> targets,
            String relationId) {
        for (CnATreeElement target : targets) {
            Optional.ofNullable(createLink(source, target, relationId))
                    .ifPresent(source::addLinkDown);
        }
    }

}
