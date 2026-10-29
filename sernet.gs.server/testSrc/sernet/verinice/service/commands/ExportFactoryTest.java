/**
 * Copyright 2026 Sven Neuhaus.
 *
 * This file is part of Verinice.
 *
 * Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - Sven Neuhaus - initial API and implementation
 */
package sernet.verinice.service.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import de.sernet.sync.data.SyncAttribute;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;
import de.sernet.sync.sync.SyncRequest.SyncVnaSchemaVersion;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.service.sync.StreamFactory;

/**
 * Tests for {@link ExportFactory}, especially the conversion of verinice
 * properties to the VNA XML format.
 */
public class ExportFactoryTest {

    private static HUITypeFactory huiTypeFactory;

    @BeforeClass
    public static void setupTypeFactory() throws Exception {
        if (!VeriniceContext.exists(VeriniceContext.HUI_TYPE_FACTORY)) {
            huiTypeFactory = HUITypeFactory.createInstance(ExportFactoryTest.class
                    .getResource("/" + HUITypeFactory.HUI_CONFIGURATION_FILE));
            VeriniceContext.put(VeriniceContext.HUI_TYPE_FACTORY, huiTypeFactory);
        } else {
            huiTypeFactory = HUITypeFactory.getInstance();
        }
    }

    /**
     * A property without a value must not be exported as
     * {@code <value xsi:nil="true"/>}, because {@code value} is declared as a
     * non-nillable {@code xsd:string} in data.xsd.
     */
    @Test
    public void nullValueIsExportedAsEmptyString() {
        Entity entity = createRequirementWithProperties((String) null);

        List<SyncAttribute> attributes = export(entity);

        assertEquals(1, attributes.size());
        assertEquals(Collections.singletonList(""), attributes.get(0).getValue());
        assertEquals(1, attributes.get(0).getLimitedLicense().size());
        assertEquals(1, attributes.get(0).getLicenseContentId().size());
    }

    /**
     * Replacing null values must not change the number or the order of the
     * values, since the importer maps values to license information by index.
     */
    @Test
    public void nullValuesAreReplacedWithoutChangingListSize() {
        Entity entity = createRequirementWithProperties(null, "yes");

        List<SyncAttribute> attributes = export(entity);

        assertEquals(1, attributes.size());
        assertEquals(Arrays.asList("", "yes"), attributes.get(0).getValue());
        assertEquals(2, attributes.get(0).getLimitedLicense().size());
        assertEquals(2, attributes.get(0).getLicenseContentId().size());
    }

    /**
     * The exported request must validate against the VNA schema that is bundled
     * with every exported archive.
     */
    @Test
    public void exportedRequestIsValidAgainstVnaSchema() throws Exception {
        Entity entity = createRequirementWithProperties((String) null);

        byte[] xml = marshal(createSyncRequest(export(entity)));

        String xmlAsString = new String(xml, StandardCharsets.UTF_8);
        assertFalse("Export must not contain xsi:nil values",
                xmlAsString.contains("xsi:nil"));

        validateAgainstBundledSchema(xml);
    }

    private Entity createRequirementWithProperties(String... values) {
        Entity entity = new Entity(BpRequirement.TYPE_ID);
        PropertyType propertyType = huiTypeFactory.getPropertyType(BpRequirement.TYPE_ID,
                BpRequirement.PROP_IMPLEMENTATION_STATUS);
        for (String value : values) {
            entity.createNewProperty(propertyType, value);
        }
        return entity;
    }

    private List<SyncAttribute> export(Entity entity) {
        List<SyncAttribute> attributes = new ArrayList<>();
        ExportFactory.transform(entity, attributes, BpRequirement.TYPE_ID, huiTypeFactory, null);
        return attributes;
    }

    private SyncRequest createSyncRequest(List<SyncAttribute> attributes) {
        SyncObject syncObject = new SyncObject();
        syncObject.setExtId("exported-element");
        syncObject.setExtObjectType(BpRequirement.TYPE_ID);
        syncObject.getSyncAttribute().addAll(attributes);

        SyncData syncData = new SyncData();
        syncData.getSyncObject().add(syncObject);

        SyncVnaSchemaVersion schemaVersion = new SyncVnaSchemaVersion();
        schemaVersion.setVnaSchemaVersion("vna-1.2");
        schemaVersion.getCompatibleVersions().add("vna-1.2");

        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setSourceId("ExportFactoryTest");
        syncRequest.setSyncData(syncData);
        syncRequest.setSyncMapping(new SyncMapping());
        syncRequest.setSyncVnaSchemaVersion(schemaVersion);
        return syncRequest;
    }

    private byte[] marshal(SyncRequest syncRequest) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExportFactory.marshal(syncRequest, outputStream);
        return outputStream.toByteArray();
    }

    private void validateAgainstBundledSchema(byte[] xml) throws Exception {
        Path schemaDirectory = Files.createTempDirectory("vna-schema");
        try {
            File syncXsd = copyToSchemaDirectory(schemaDirectory, "sync.xsd",
                    StreamFactory.getSyncXsdAsStream());
            copyToSchemaDirectory(schemaDirectory, "data.xsd", StreamFactory.getDataXsdAsStream());
            copyToSchemaDirectory(schemaDirectory, "mapping.xsd",
                    StreamFactory.getMappingXsdAsStream());

            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(syncXsd);
            schema.newValidator().validate(new StreamSource(new ByteArrayInputStream(xml)));
        } finally {
            delete(schemaDirectory);
        }
    }

    private File copyToSchemaDirectory(Path directory, String name, InputStream inputStream)
            throws Exception {
        File target = directory.resolve(name).toFile();
        try (InputStream in = inputStream) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private void delete(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }
}
