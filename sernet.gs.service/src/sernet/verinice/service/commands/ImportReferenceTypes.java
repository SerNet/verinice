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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncAttribute;

/**
 * Imports properties of type references which are defined by the SNCA.xml.
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class ImportReferenceTypes {

    private static final String IMPORT_REFERENCES = "[import references]";

    private static final Logger LOG = Logger.getLogger(ImportReferenceTypes.class);

    private final Map<CnATreeElement, List<SyncAttribute>> cnaTreeElement2SyncAttributes;

    private final IBaseDao<CnATreeElement, Serializable> dao;

    private ICommandService iCommandService;

    private Map<String, CnATreeElement> idElementMap;

    ImportReferenceTypes(IBaseDao<CnATreeElement, Serializable> iBaseDao, ICommandService iCommandService, Map<String, CnATreeElement> idElementMap) {
        this.dao = iBaseDao;
        this.iCommandService = iCommandService;
        this.idElementMap = idElementMap;
        this.cnaTreeElement2SyncAttributes = new HashMap<>();
    }

    /**
     * Keep a cnatreeelement along with its sync attributes in a kind of memory
     * cache, so the references can be resolved by {
     * {@link #replaceExternalIdsWithDbIds()}.
     *
     * <p>
     * Only sync attributes from type references are taken into account. Note
     * that the sync attributes actually have no type, so the type is deduced
     * from the name of the sync attribute.
     * </p>
     *
     * <p>
     * Background for this: In the sync attribute is only stored an external id,
     * which is later replaced by the entity id of the target entity. But this
     * can only happened after the complete import has already been done.
     * </p>
     *
     * @param cnaTreeElement
     *            The source cna tree element, which may contains a property of
     *            type reference.
     * @param syncAttribute
     *            All the property values the cna treeelements contains but in
     *            the form they are stored in the vna file, which may differ
     *            from what is stored in the database.
     * @param propertyId
     *            The id of the property according to the sync attributes.
     */
    public void trackReferences(CnATreeElement cnaTreeElement, SyncAttribute syncAttribute, String propertyId) {

        PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(cnaTreeElement.getTypeId(), propertyId);

        if (isReference(propertyType)) {
            LOG.debug(IMPORT_REFERENCES + " cache element " + cnaTreeElement.getTitle());
            mapCnATreeElement2SyncAttributes(cnaTreeElement, syncAttribute);
        }
    }

    private static boolean isReference(PropertyType propertyType) {
        return propertyType != null && propertyType.isReference();
    }

    private void mapCnATreeElement2SyncAttributes(CnATreeElement cnaTreeElement, SyncAttribute syncAttribute) {
        if (!cnaTreeElement2SyncAttributes.containsKey(cnaTreeElement)) {
            cnaTreeElement2SyncAttributes.put(cnaTreeElement, new ArrayList<SyncAttribute>());
        }

        cnaTreeElement2SyncAttributes.get(cnaTreeElement).add(syncAttribute);
    }

    /**
     * Replaces all the external ids with the actual database ids of the
     * according entitiy.
     *
     * <p>
     * Should be called, after the complete import is done.
     * </p>
     *
     */
    public void replaceExternalIdsWithDbIds() {

        Set<Entry<CnATreeElement, List<SyncAttribute>>> entrySet = cnaTreeElement2SyncAttributes.entrySet();

        for (Map.Entry<CnATreeElement, List<SyncAttribute>> syncElement : entrySet) {

            CnATreeElement hydratedElement = hydrateCnaTreeElement(syncElement);
            List<SyncAttribute> syncAttributes = syncElement.getValue();

            for (SyncAttribute syncAttribute : syncAttributes) {

                String name = syncAttribute.getName();
                PropertyList propertiesList = hydratedElement.getEntity().getProperties(name);

                // replace ext id with db ids
                overWriteExternalIdWithDatabaseId(hydratedElement, propertiesList);

                updateElement(hydratedElement);
            }
        }
    }

    private void updateElement(CnATreeElement hydratedElement) {
        SaveElement<CnATreeElement> saveElement = new SaveElement<>(hydratedElement);

        try {
            iCommandService.executeCommand(saveElement);
        } catch (CommandException e) {
            LOG.error(IMPORT_REFERENCES + "error after import the new references: " + e.getLocalizedMessage(), e);
        }
    }

    private void overWriteExternalIdWithDatabaseId(CnATreeElement hydratedElement, PropertyList propertyList) {

        List<Property> properties = propertyList.getProperties();
        Iterator<Property> iterator = properties.iterator();

        while (iterator.hasNext()) {

            Property prop = iterator.next();

            String cnaElementExtId = prop.getPropertyValue();
            CnATreeElement referenceTarget = idElementMap.get(cnaElementExtId);

            if (referenceTarget != null) {

                String entityDbId = "" + referenceTarget.getEntity().getDbId();
                prop.setPropertyValue(entityDbId);

                LOG.debug(IMPORT_REFERENCES
                        + " reference resolved: found target " + referenceTarget.getTitle()
                        + " by extId " + cnaElementExtId
                        + " in memory cache: " + hydratedElement.getTitle());
            } else {
                LOG.debug(IMPORT_REFERENCES
                        + " remove reference which can not be resolved: "
                        + cnaElementExtId);
                iterator.remove();
            }
        }
    }

    private CnATreeElement hydrateCnaTreeElement(Map.Entry<CnATreeElement, List<SyncAttribute>> syncElement) {
        String uuid = syncElement.getKey().getUuid();
        RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance();
        return dao.findByUuid(uuid, ri);
    }
}
