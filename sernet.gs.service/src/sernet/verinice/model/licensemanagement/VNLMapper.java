/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.licensemanagement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXB;

/**
 * Class provides methods to transform license-management-data (stored in 
 * vnl-files)from hibernate representation to xml representation 
 * and the other way round
 * 
 * Please note: all data stored to a file or to database is encrypted data
 * that could not be used / changed manually 
 *  
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class VNLMapper {
    
    private static VNLMapper instance;
    
    private VNLMapper(){
        
    }
    
    public static VNLMapper getInstance(){
        if(instance == null){
            instance = new VNLMapper();
        }
        return instance;
    }
    
    /**
     * transforms license management information that is stored in 
     * a file (given via byte[] @param xmlData) into the hibernate
     * representation {@link LicenseManagementEntry} of that data
     */
    public LicenseManagementEntry unmarshalXML(byte[] xmlData){
        InputStream inputStream = new ByteArrayInputStream(xmlData);
        de.sernet.model.licensemanagement.LicenseManagementEntry xmlObject =
                JAXB.unmarshal(inputStream, de.sernet.model.licensemanagement.
                LicenseManagementEntry.class);
        return xmlToPojo(xmlObject);
    }
    
    
    /**
     * maps the xml representation 
     * {@link de.sernet.model.licensemanagement.LicenseManagementEntry}
     * to the POJO representation {@link LicenseManagementEntry}
     * of license management informations 
     */
    public LicenseManagementEntry xmlToPojo(de.sernet.model.
            licensemanagement.LicenseManagementEntry xmlObject){
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(xmlObject.getE1());
        entry.setLicenseID(xmlObject.getE2());
        entry.setSalt(xmlObject.getE3());
        entry.setUserPassword(xmlObject.getE4());
        entry.setValidUntil(xmlObject.getE5());
        entry.setValidUsers(xmlObject.getE6());
        return entry;
    }
    
    
    

}
