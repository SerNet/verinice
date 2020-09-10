/*******************************************************************************
 * Copyright (c) 2020 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.test;

import java.io.Serializable;

import javax.annotation.Resource;

import org.hamcrest.CoreMatchers;
import org.hibernate.criterion.DetachedCriteria;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@Transactional
public class ScopeOnlyUserCompendiumAccessTest extends AbstractModernizedBaseProtection {

    private final String userName = "scope-only-user";

    @Resource(name = "configurationDao")
    private IDao<Configuration, Serializable> configurationDao;

    @Resource(name = "permissionDAO")
    private IBaseDao<Permission, Serializable> permissionDao;

    private ItNetwork catalog1;

    @Before
    public void enablePermissionHandling() throws CommandException {
        Configuration conf = new Configuration();
        ItNetwork itNetwork = createNewBPOrganization();
        BpPersonGroup personGroup = createGroup(itNetwork, BpPersonGroup.class);
        BpPerson person = createElement(personGroup, BpPerson.class);
        conf.setPerson(person);
        conf.setUser(userName);
        conf.setScopeOnly(true);
        configurationDao.merge(conf);
        CatalogModel catalogModel = (CatalogModel) elementDao
                .findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).get(0);
        catalog1 = new ItNetwork(catalogModel);
        elementDao.saveOrUpdate(catalog1);
        catalog1.setScopeId(catalog1.getDbId());
        elementDao.saveOrUpdate(catalog1);

        authService.setPermissionHandlingNeeded(true);
        authService.setUsername(userName);

    }

    @Test
    public void scopeOnlyUserWithoutPermissinCannotAccessCatalogElements() {
        Assert.assertThat(elementDao.findAll(), CoreMatchers.not(JUnitMatchers.hasItems(catalog1)));
    }

    @Test
    public void scopeOnlyUserWithPermissionCanAccessCatalogElements() {
        catalog1.addPermission(Permission.createPermission(catalog1, userName, true, false));
        elementDao.saveOrUpdate(catalog1);

        Assert.assertThat(elementDao.findAll(), JUnitMatchers.hasItems(catalog1));
    }
}