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
package sernet.verinice.service.sync;

import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IVeriniceArchive {

    byte[] getVeriniceXml();

    SyncMapping getSyncMapping();

    SyncData getSyncData();

    String getSourceId();

    void setSourceId(String sourceId);
    
    void setSyncMapping(SyncMapping syncMapping);

    void setSyncData(SyncData syncData);

    byte[] getFileData(String fileName);
    
    void clear();
    
}
