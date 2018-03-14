package sernet.verinice.service.bp.importer;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import ITBP2VNA.generated.module.Document;

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
public final class ITBPParser {

    private static final Logger LOG = Logger.getLogger(ITBPParser.class);

    private static ITBPParser instance;

    private ITBPParser() {
    }

    public Document parseModule(File moduleXMLFile) {

        Document moduleDocument = null;

        try {

            JAXBContext context = JAXBContext.newInstance(Document.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            moduleDocument = (Document) unmarshaller.unmarshal(moduleXMLFile);

        } catch (JAXBException e) {
            logParseException(moduleXMLFile, e);
        }

        return moduleDocument;

    }

    public ITBP2VNA.generated.threat.Document parseThreat(File threatXMLFile) {

        ITBP2VNA.generated.threat.Document threadDocument = null;

        try {

            JAXBContext context = JAXBContext.newInstance(ITBP2VNA.generated.threat.Document.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            threadDocument = (ITBP2VNA.generated.threat.Document) unmarshaller
                    .unmarshal(threatXMLFile);

        } catch (JAXBException e) {
            logParseException(threatXMLFile, e);
        }

        return threadDocument;

    }

    public ITBP2VNA.generated.implementationhint.Document parseImplementationHint(
            File implHintXMLFile) {

        ITBP2VNA.generated.implementationhint.Document implHintDocument = null;

        try {

            JAXBContext context = JAXBContext
                    .newInstance(ITBP2VNA.generated.implementationhint.Document.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            implHintDocument = (ITBP2VNA.generated.implementationhint.Document) unmarshaller
                    .unmarshal(implHintXMLFile);

        } catch (JAXBException e) {
            logParseException(implHintXMLFile, e);
        }

        return implHintDocument;

    }

    public static ITBPParser getInstance() {
        if (instance == null) {
            instance = new ITBPParser();
        }
        return instance;
    }

    private static void logParseException(File moduleXMLFile, JAXBException e) {
        LOG.error("Error while parsing ITBP-Document:\t" + moduleXMLFile.getAbsolutePath(), e);
    }

}
