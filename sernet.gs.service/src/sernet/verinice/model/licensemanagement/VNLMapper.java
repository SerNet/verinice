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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXB;

import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;
import sernet.verinice.service.commands.ExportFactory;

/**
 * Class provides methods to transform license-management-data (stored in 
 * vnl-files)from hibernate representation to xml representation 
 * and the other way round
 * 
 * please note: all data stored to a file or to database is encrypted data
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
        return xmlToHibernate(xmlObject);
    }
    
    /**
     * transforms the hibernate representation 
     * {@link LicenseManagementEntry} of license management information
     * into an byte[] that can be used to store the information within a file 
     **/
    public byte[] marshalLicenseManagementEntry(LicenseManagementEntry entry){
        de.sernet.model.licensemanagement.LicenseManagementEntry xmlObject =
                hibernateToXml(entry);
        OutputStream outputStream = new ByteArrayOutputStream();
        ExportFactory.marshal(xmlObject, outputStream);
        return ((ByteArrayOutputStream)outputStream).toByteArray();
    }
    
    /**
     * maps the xml representation 
     * {@link de.sernet.model.licensemanagement.LicenseManagementEntry}
     * to the hibernate representation {@link LicenseManagementEntry}
     * of license management informations 
     */
    public LicenseManagementEntry xmlToHibernate(de.sernet.model.licensemanagement.LicenseManagementEntry xmlObject){
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(xmlObject.getContentIdentifier());
        entry.setLicenseID(xmlObject.getLicenseID());
        entry.setSalt(xmlObject.getSalt());
        entry.setUserPassword(xmlObject.getUserPassword());
        entry.setValidUntil(xmlObject.getValidUntil());
        entry.setValidUsers(xmlObject.getValidUsers());
        return entry;
    }
    
    /**
     * maps the hibernate representation
     * {@link LicenseManagementEntry} to the xml representation
     * {@link de.sernet.model.licensemanagement.LicenseManagementEntry}
     * of license management informations
     * @param entry
     * @return
     */
    public de.sernet.model.licensemanagement.LicenseManagementEntry hibernateToXml(LicenseManagementEntry entry){
        de.sernet.model.licensemanagement.LicenseManagementEntry xmlObject = new de.sernet.model.licensemanagement.LicenseManagementEntry();
        xmlObject.setContentIdentifier(entry.getContentIdentifier());
        xmlObject.setLicenseID(entry.getLicenseID());
        xmlObject.setSalt(entry.getSalt());
        xmlObject.setUserPassword(entry.getUserPassword());
        xmlObject.setValidUntil(entry.getValidUntil());
        xmlObject.setValidUsers(entry.getValidUsers());
        return xmlObject;
    }
    
    

}
