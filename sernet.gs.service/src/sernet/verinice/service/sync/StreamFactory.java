/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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

import java.io.InputStream;

/**
 * Factory to create stream out of verinice import export schema files.
 * This class is used by ExportCommand to put static files to 
 * verinice archives while exporting.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class StreamFactory {

    private static final String DATA_XSD = "sernet/verinice/service/sync/data.xsd";
    private static final String MAPPING_XSD = "sernet/verinice/service/sync/mapping.xsd";
    private static final String SYNC_XSD = "sernet/verinice/service/sync/sync.xsd";
    private static final String README = "sernet/verinice/service/sync/readme.txt";
    
    public static InputStream getDataXsdAsStream() {
        return StreamFactory.class.getClassLoader().getResourceAsStream(DATA_XSD);
    }
    
    public static InputStream getMappingXsdAsStream() {
        return StreamFactory.class.getClassLoader().getResourceAsStream(MAPPING_XSD);
    }
    
    public static InputStream getSyncXsdAsStream() {
        return StreamFactory.class.getClassLoader().getResourceAsStream(SYNC_XSD);
    }
    
    public static InputStream getReadmeAsStream() {
        return StreamFactory.class.getClassLoader().getResourceAsStream(README);
    }
}
