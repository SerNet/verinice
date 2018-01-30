/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels <bw@sernet.de>.
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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.Assert;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.ExportCommand;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * Tests the import and export of SNCA properties of type references.
 * 
 * This test assumes that every reference can be resolved, what means that the
 * referenced element is contained in the vna file or already stored in the
 * database.
 * 
 * So for this test the ITVerbund is modeled like this:
 * 
 * <pre>
 *  * ITVerbund
 *  |
 *  * Server
 *  |\ 
 *  | S-1 .................
 *  |                   .  .
 *  * Staff             .  .
 *  |\                  .  .
 *  | * M-1 Person One ..  .
 *  | |                    .
 *  | * M-1 Person Two .....
 * </pre>
 * 
 * <p>
 * The dotted lines are the references.
 * </p>
 * 
 * <h2>
 * The test algorithm works as follows:
 * </h2>
 * 
 * <ol>
 * <li>Set the reference by hand (dotted lines).</li>
 * <li>Export the file as temporarily vna file.</li>
 * <li>Delete ITVerbund from the database.</li>
 * <li>Reimport from the temporily vna file.</li>
 * <li>Check the references.</li>
 * </ol>
 * 
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class ExportAndImportReferencesTest extends BeforeEachVNAImportHelper {
    
    private static final String SOURCE_ELEMENT_EXTERNAL_ID = "ENTITY_262147";
    private static final String IT_VERBUND_1_EXTERNAL_ID = "ENTITY_262144";
    private static final String TARGET_PERSON_1_EXTERNAL_ID = "ENTITY_262145";
    private static final String TARGET_PERSON_2_EXTERNAL_ID = "ENTITY_262146";
    
    private static final String SERVER_ANWENDER_LINK = "server_anwender_link";
    
    private static final String SOURCE_ID = "b3305b";   
   
    private static final String VNA_FILENAME_WITH_COMPLETE_REFERENCES = "export_import_references_test.vna";
   
    private static final String IMPORT_REFERENCES_PREFIX = "[import references]";
    private static final String EXPORT_REFERENCES_PREFIX = "[export references]";
    private final Logger LOG = Logger.getLogger(ExportAndImportReferencesTest.class);

    @Test
    public void testExportAndImportOfHuiReferences() throws CommandException, IOException, SyncParameterException {

        /** Set references */
        CnATreeElement itVerbund1 = loadElement(SOURCE_ID, IT_VERBUND_1_EXTERNAL_ID);

        CnATreeElement source = loadElement(SOURCE_ID, SOURCE_ELEMENT_EXTERNAL_ID);
        RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance().setPermissions(true);
        source = Retriever.retrieveElement(source, ri);

        CnATreeElement targetPerson1 = loadElement(SOURCE_ID, TARGET_PERSON_1_EXTERNAL_ID);
        CnATreeElement targetPerson2 = loadElement(SOURCE_ID, TARGET_PERSON_2_EXTERNAL_ID);

        // set reference by hand
        Integer targetPerson1EntityId = targetPerson1.getEntity().getDbId();
        Integer targetPerson2EntityId = targetPerson2.getEntity().getDbId();

        Entity sourceEntity = source.getEntity();
        PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(source.getTypeId(), SERVER_ANWENDER_LINK);

        sourceEntity.createNewProperty(propertyType, String.valueOf(targetPerson1EntityId));
        sourceEntity.createNewProperty(propertyType, String.valueOf(targetPerson2EntityId));

        LOG.info(IMPORT_REFERENCES_PREFIX + " store the update properties in database");
        source = updateElement(source);

        /** Export a new vna and import it */
        List<CnATreeElement> toExport = new ArrayList<>();
        toExport.add(itVerbund1);
        ExportCommand exportCommand = new ExportCommand(toExport, SOURCE_ID, true);
        String filePath = FilenameUtils.concat(
                System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR),
                "export-test.vna");
        exportCommand.setFilePath(filePath);

        LOG.info(EXPORT_REFERENCES_PREFIX + " export vna file " + filePath);
        commandService.executeCommand(exportCommand);

        LOG.info(EXPORT_REFERENCES_PREFIX + " remove itverbund " + itVerbund1.getTitle());
        removeITVerbund(itVerbund1);

        LOG.info(IMPORT_REFERENCES_PREFIX + " from file " + filePath);
        importFile(filePath, getSyncParameter());

        LOG.info(IMPORT_REFERENCES_PREFIX + " delete file " + filePath);
        File vnaArchive = new File(filePath);
        vnaArchive.delete();

        /** validate the imported vna */
        CnATreeElement itverbundImported = loadElement(SOURCE_ID, IT_VERBUND_1_EXTERNAL_ID);
        CnATreeElement sourceImported = loadElement(SOURCE_ID, SOURCE_ELEMENT_EXTERNAL_ID);
        CnATreeElement targetPerson1Imported = loadElement(SOURCE_ID, TARGET_PERSON_1_EXTERNAL_ID);
        CnATreeElement targetPerson2Imported = loadElement(SOURCE_ID, TARGET_PERSON_2_EXTERNAL_ID);

        Entity sourceEntityImported = sourceImported.getEntity();
        PropertyType propertyTypeImported = HUITypeFactory.getInstance().getPropertyType(source.getTypeId(), SERVER_ANWENDER_LINK);

        int targetPerson1ImportedEntityId = targetPerson1Imported.getEntity().getDbId();
        int targetPerson2ImportedEntityId = targetPerson2Imported.getEntity().getDbId();

        PropertyList properties = sourceEntityImported.getProperties(propertyTypeImported.getId());

        Assert.assertFalse(IMPORT_REFERENCES_PREFIX + " the list of referneces may not be null.", properties.getProperties() == null);
        Assert.assertFalse(IMPORT_REFERENCES_PREFIX + " the list of references may not be empty.", properties.getProperties().isEmpty());
        int numberOfReferences = 2;
        Assert.assertTrue(IMPORT_REFERENCES_PREFIX + " should only conatain 2 values", properties.getProperties().size() == numberOfReferences);

        for (Property prop : properties.getProperties()) {
            int entityDbId = prop.getNumericPropertyValue();
            Assert.assertTrue(IMPORT_REFERENCES_PREFIX + " the reference " + entityDbId + " was not correctly imported", validateReferences(targetPerson1ImportedEntityId, targetPerson2ImportedEntityId, entityDbId));
        }

        LOG.info(IMPORT_REFERENCES_PREFIX + "delete the imported it-verbund" + itverbundImported.getTitle());
        removeITVerbund(itverbundImported);
    }

    private void removeITVerbund(CnATreeElement itverbund) throws CommandException {
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(itverbund);
        commandService.executeCommand(removeCommand);
    }

    private boolean validateReferences(int targetPerson1ImportedEntityId, int targetPerson2ImportedEntityId, int numericPropertyValue) {
        return numericPropertyValue == targetPerson1ImportedEntityId || numericPropertyValue == targetPerson2ImportedEntityId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.test.helper.vnaimport.AbstractVNAImportHelper
     * #getFilePath()
     */
    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME_WITH_COMPLETE_REFERENCES).getPath();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.test.helper.vnaimport.AbstractVNAImportHelper
     * #getSyncParameter()
     */
    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
