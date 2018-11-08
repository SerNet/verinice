/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.test;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.search.JsonBuilder;
import sernet.verinice.service.commands.UpdatePermissions;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class JsonBuilderTest extends AbstractModernizedBaseProtection {

    private JsonBuilder jsonBuilder = new JsonBuilder();

    @Resource(name = "permissionDAO")
    private IBaseDao<Permission, Serializable> permissionDao;

    @Test
    @Transactional
    @Rollback(true)
    public void testNonIndexableElements() {
        Assert.assertEquals(null, jsonBuilder.getJson(new ImportBpGroup(null)));
        Assert.assertEquals(null, jsonBuilder.getJson(new ImportIsoGroup(null)));
        Assert.assertEquals(null, jsonBuilder.getJson(new ImportBsiGroup(null)));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testIndexBpPerson() throws CommandException {
        ItNetwork network = createNewBPOrganization();
        PersonGroup persons = createGroup(network, PersonGroup.class);
        CnATreeElement bpPerson = createElement(persons, BpPerson.class);
        String json = jsonBuilder.getJson(bpPerson);
        Assert.assertNotNull(json);
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(json);
        JsonElement elementType = jsonObject.get(ISearchService.ES_FIELD_ELEMENT_TYPE);
        Assert.assertNotNull(elementType);
        Assert.assertEquals(BpPerson.TYPE_ID, elementType.getAsString());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testIndexElementLoadedWithoutPermissions() throws CommandException {
        ItNetwork network = createNewBPOrganization();
        PersonGroup persons = createGroup(network, PersonGroup.class);
        CnATreeElement bpPerson = createElement(persons, BpPerson.class);
        Permission permission = Permission.createPermission(bpPerson, "foobar", true, false);
        UpdatePermissions up = new UpdatePermissions(bpPerson, Collections.singleton(permission),
                Collections.emptySet(), false, true);
        commandService.executeCommand(up);
        elementDao.flush();
        elementDao.clear();
        // VN-2172
        CnATreeElement bpPersonReloaded = reloadElement(bpPerson);
        elementDao.executeCallback(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.evict(bpPersonReloaded);
                return null;
            }
        });
        Assert.assertFalse(Retriever.arePermissionsInitialized(bpPersonReloaded));
        String json = jsonBuilder.getJson(bpPersonReloaded);
        Assert.assertNotNull(json);
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(json);
        JsonArray permissionRoles = jsonObject
                .getAsJsonArray(ISearchService.ES_FIELD_PERMISSION_ROLES);
        Assert.assertNotNull(permissionRoles);
        Assert.assertEquals(1, permissionRoles.size());
        JsonObject permissionRole = permissionRoles.get(0).getAsJsonObject();
        Assert.assertEquals("foobar",
                permissionRole.get(ISearchService.ES_FIELD_PERMISSION_NAME).getAsString());
        // 1 means read allowed, see
        // sernet.verinice.search.JsonBuilder.addPermissions(XContentBuilder,
        // CnATreeElement)
        Assert.assertEquals(1,
                permissionRole.get(ISearchService.ES_FIELD_PERMISSION_VALUE).getAsInt());

    }

}
