/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class RemoveLink<T extends CnALink> extends ChangeLoggingCommand
        implements IChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(RemoveLink.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(RemoveLink.class);
        }
        return log;
    }

    private String stationId;
    // private CnALink element;
    // private Integer dependantId;
    // private Integer dependencyId;
    // private String typeId;

    private Set<CnALink> elements = new HashSet<>();
    private List<CnATreeElement> changeElements = new ArrayList<>();

    private class LinkData {
        public LinkData(Integer dependantId, Integer dependencyId, String typeId) {
            super();
            this.dependantId = dependantId;
            this.dependencyId = dependencyId;
            this.typeId = typeId;
        }

        private Integer dependantId;
        private Integer dependencyId;
        private String typeId;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((dependantId == null) ? 0 : dependantId.hashCode());
            result = prime * result + ((dependencyId == null) ? 0 : dependencyId.hashCode());
            result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LinkData other = (LinkData) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (dependantId == null) {
                if (other.dependantId != null)
                    return false;
            } else if (!dependantId.equals(other.dependantId))
                return false;
            if (dependencyId == null) {
                if (other.dependencyId != null)
                    return false;
            } else if (!dependencyId.equals(other.dependencyId))
                return false;
            if (typeId == null) {
                if (other.typeId != null)
                    return false;
            } else if (!typeId.equals(other.typeId))
                return false;
            return true;
        }

        private RemoveLink getOuterType() {
            return RemoveLink.this;
        }
    }

    private Set<LinkData> linkData = new HashSet<>();

    public RemoveLink() {
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    public RemoveLink(CnALink link) {
        this.stationId = ChangeLogEntry.STATION_ID;
        // this.element = link;
        elements.add(link);
    }

    public RemoveLink(Integer dependantId, Integer dependencyId, String typeId) {
        super();
        // this.dependantId = dependantId;
        // this.dependencyId = dependencyId;
        // this.typeId = typeId;

        linkData.add(new LinkData(dependantId, dependencyId, typeId));
    }

    public boolean addLinkData(Integer dependantId, Integer dependencyId, String typeId) {
        return linkData.add(new LinkData(dependantId, dependencyId, typeId));
    }

    @Override
    public void execute() {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Looking for link to remove.");
        }

        IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
        doRemove(dao);
    }

    /**
     * @param dao
     */
    private void doRemove(IBaseDao<CnALink, Serializable> dao) {
        for (CnALink cnALink : elements) {
            CnALink aLink = dao.findById(cnALink.getId());
            if (aLink != null) {
                removeLink(aLink, dao);
            }
        }

        for (LinkData linkData : linkData) {
            CnALink aLink = dao.findById(
                    new CnALink.Id(linkData.dependantId, linkData.dependencyId, linkData.typeId));
            if (aLink != null) {
                removeLink(aLink, dao);
            }
        }

        // if(element!=null) {
        // element = dao.findById(element.getId());
        // } else {
        // element = dao.findById(new CnALink.Id(dependantId, dependencyId,
        // typeId));
        // }
        // if (element != null) {
        // if (getLog().isDebugEnabled()) {
        // getLog().debug("Found link, removing " + element.getId());
        // }
        // element.remove();
        // dao.delete(element);
        // dao.flush();
        // } else {
        // getLog().warn("Link was already deleted while trying to delete it.");
        // }
    }

    private void removeLink(CnALink aLink, IBaseDao<CnALink, Serializable> dao) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Found link, removing " + aLink.getId());
        }
        changeElements.add(aLink.getDependant());
        changeElements.add(aLink.getDependency());

        aLink.remove();
        dao.delete(aLink);
        dao.flush();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
     */
    @Override
    public void clear() {
        // element = null;
        elements.clear();
        linkData.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangeType ()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        // return link category item:
        // List<CnATreeElement> result = new ArrayList<>();
        // if (element != null) {
        // result.add(element.getDependant());
        // result.add(element.getDependency());
        // }
        // return result;
        return changeElements;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

}
