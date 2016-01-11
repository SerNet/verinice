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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.sernet.sync.data.SyncAttribute;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Exports the references types defined by the SCNA.xml
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class ExportReferenceTypes {

    private static final int INVALID_DATABASE_ID = -1;

    private static final Logger LOG = Logger.getLogger(ExportReferenceTypes.class);

    private static final String EXPORT_REFERENCES = "[export references]";

    private final ICommandService commandService;

    private final Map<Integer, String> dbId2ExtId;

    public ExportReferenceTypes(ICommandService commandService) {
        this.commandService = commandService;
        this.dbId2ExtId = new HashMap<>();
    }

    /**
     * Maps the entity database id to the external id and store this value in
     * the proper sync attribute.
     *
     * @param syncAttribute
     *            Stores the property value for data transfer via vna format.
     * @param properties
     *            The reference properties. They have to be of the referenc
     *            type.
     *
     */
    public void mapEntityDatabaseId2ExtId(SyncAttribute syncAttribute, PropertyList properties) {

        for (Property property : properties.getProperties()) {

            LOG.debug(EXPORT_REFERENCES + " transform : " + property);

            int dbid = INVALID_DATABASE_ID;
            dbid = castDatabaseId2Int(property, dbid);

            // abort if no valid database id has been found.
            if(INVALID_DATABASE_ID != dbid){
                writeExternalIdToSyncAttributes(syncAttribute, property, dbid);
            }
        }
    }

    private void writeExternalIdToSyncAttributes(SyncAttribute syncAttribute, Property property, int dbid) {
        
        boolean fetchedExternalId = fetchExternalId(property, dbid);

        if (fetchedExternalId) {
            // finally set the extid instead of the dbid to the
            // exported
            LOG.debug(EXPORT_REFERENCES
                    + " map property " + property 
                    + " dbid: " + dbid 
                    + " to extid: " + dbId2ExtId.get(dbid));

            List<String> attributes = syncAttribute.getValue();
            attributes.add(dbId2ExtId.get(dbid));
        }
    }

    private boolean fetchExternalId(Property property, int dbid) {
        // if no extid is present create one and store in dbId2ExtId
        if (!dbId2ExtId.containsKey(dbid)) {

            LoadCnAElementsByEntityIds<CnATreeElement> loadCnAElement = loadElement(dbid);
            if (isElementNotLoaded(loadCnAElement)) {
                LOG.debug(EXPORT_REFERENCES + " no entity found for this reference property: " + property);
                return false;
            }

            // get the one and only element
            CnATreeElement referenceTarget = loadCnAElement.getElements().get(0);
            if (referenceTarget != null) {
                String extId = ExportFactory.createExtId(referenceTarget);
                dbId2ExtId.put(dbid, extId);
            }

        }

        return true;
    }

    private static Integer castDatabaseId2Int(Property property, Integer dbid) {
        try {
            return Integer.valueOf(property.getPropertyValue());
        } catch (NumberFormatException e) {
            LOG.error(EXPORT_REFERENCES
                    + "seems not to be a entity dbid: "
                    + property.getPropertyValue());

        }
        return dbid;
    }

    private static boolean isElementNotLoaded(LoadCnAElementsByEntityIds<CnATreeElement> loadCnAElement) {
        return loadCnAElement.getElements() == null || loadCnAElement.getElements().isEmpty();
    }

    private LoadCnAElementsByEntityIds<CnATreeElement> loadElement(int entityId) {

        // The children has also to be loaded, since the index process failed
        // when updating these elements later.
        RetrieveInfo retrieveInfo = RetrieveInfo
                .getPropertyInstance()
                .setPermissions(true).setChildren(true);

        // Maybe it is a lack in the API or there is something misunderstood by
        // the programmer. Loading elements by the entity id is only possible
        // via a whole collection, but the database id is the primary key and
        // that's why it is unique.
        //
        // For future works there should be an command which also accept a
        // single id through its parameters.
        //
        // If a better solution is available this code should be refactored,
        // because it is unreadable.
        Collection<Integer> entityIds = new HashSet<>();
        entityIds.add(entityId);

        LoadCnAElementsByEntityIds<CnATreeElement> loadElementCommand = new LoadCnAElementsByEntityIds<>(
                CnATreeElement.class,
                entityIds,
                retrieveInfo);

            try {
                loadElementCommand = commandService.executeCommand(loadElementCommand);
            } catch (CommandException e) {
                LOG.error(EXPORT_REFERENCES
                        + " mapping a reference to external id failed during export: "
                        + e.getLocalizedMessage(), e);
            }

        return loadElementCommand;

    }

    /**
     * Add reference from entity database id to external id.
     */
    public void addReference2ExtId(Integer dbId, String extId) {
        dbId2ExtId.put(dbId, extId);
    }
}
