/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.util.Set;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 */
public class ModelDummySafeguards extends ChangeLoggingCommand {

    private Set<String> moduleUuidsFromScope;
    private Set<CnATreeElement> targetElements;

    private String stationId;

    public ModelDummySafeguards(Set<String> moduleUuidsFromScope,
            Set<CnATreeElement> targetElements) {
        super();
        this.moduleUuidsFromScope = moduleUuidsFromScope;
        this.targetElements = targetElements;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

}
