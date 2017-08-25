package ITBP2VNA;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import ITBP2VNA.generated.DocumentType;

/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ITBPParser {
    
    private final static Logger LOG = Logger.getLogger(ITBPParser.class);

    public static ITBPParser instance;
    
    public ITBPParser() {
    }
    
    public DocumentType parseFile(File xmlFile) {
        
        DocumentType document = null;

        try {
        
            JAXBContext context = JAXBContext.newInstance(DocumentType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<DocumentType> o =  (JAXBElement<DocumentType>)unmarshaller.unmarshal(xmlFile);
            document = o.getValue();

        } catch (JAXBException e) {
            LOG.error("Error while parsing ITBP-Document:\t" + xmlFile.getAbsolutePath());
        } 
        
        return document;
    }
    
    public static ITBPParser getInstance() {
        return instance;
    }

}
