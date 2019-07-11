/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations={
        "classpath:/sernet/gs/server/spring/veriniceserver-plain.xml",
        "classpath:/sernet/gs/server/spring/veriniceserver-common.xml", //$NON-NLS-1$
        "classpath:/sernet/gs/server/spring/command-actionid-mapping.xml", //$NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-daos-common.xml", //$NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-daos-osgi.xml", //$NON-NLS-1$
        "classpath:/sernet/verinice/service/test/spring/veriniceserver-security-osgi-test.xml", //$NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-ldap.xml", //$NON-NLS-1$
        "classpath:/sernet/verinice/service/test/spring/veriniceserver-jbpm.xml", //$NON-NLS-1$
        "classpath:/sernet/verinice/service/test/spring/veriniceserver-rightmanagement.xml", //NON-NLS-1$
        "classpath:/sernet/verinice/service/test/spring/veriniceserver-reportdeposit.xml", //NON-NLS-1$
        "classpath:/sernet/verinice/service/test/spring/veriniceserver-account.xml",
        "classpath:/sernet/gs/server/spring/veriniceserver-search-dummy.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-updatenews-dummy.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-licensemanagement.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-risk-analysis-standalone.xml", //NON-NLS-1$
        "classpath:/verinice-test.xml"

})
public abstract class ContextConfiguration  {

    @Resource(name="cnaTreeElementDao")
    protected IBaseDao<CnATreeElement, Integer> elementDao;
    
    @Before
    public void ensureModelsAreCreated() {
        if (elementDao.findByCriteria(DetachedCriteria.forClass(BSIModel.class)).isEmpty()) {
            elementDao.merge(new BSIModel());
        }
        if (elementDao.findByCriteria(DetachedCriteria.forClass(ISO27KModel.class)).isEmpty()) {
            elementDao.merge(new ISO27KModel());
        }
        if (elementDao.findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).isEmpty()) {
            elementDao.merge(new CatalogModel());
        }
    }

    protected static Set<CnATreeElement> getChildrenWithTypeId(CnATreeElement element,
            String typeId) {
        return element.getChildren().stream().filter(child -> child.getTypeId().equals(typeId))
                .collect(Collectors.toSet());
    }

    protected static CnATreeElement findChildWithTypeId(CnATreeElement element, String typeId) {
        return element.getChildren().stream().filter(child -> child.getTypeId().equals(typeId))
                .findFirst().orElse(null);
    }

    protected static CnATreeElement findChildWithTitle(CnATreeElement element, String title) {
        return element.getChildren().stream().filter(child -> title.equals(child.getTitle()))
                .findFirst().orElse(null);
    }

    protected static Set<CnALink> getLinksWithType(CnATreeElement element, String linkType) {
        return Stream.concat(element.getLinksDown().stream(), element.getLinksUp().stream())
                .filter(link -> link.getRelationId().equals(linkType)).collect(Collectors.toSet());
    }

    protected static Set<CnATreeElement> getDependantsFromLinks(Set<CnALink> links) {
        return links.stream().map(CnALink::getDependant).collect(Collectors.toSet());
    }

    protected static Set<CnATreeElement> getDependenciesFromLinks(Set<CnALink> links) {
        return links.stream().map(CnALink::getDependency).collect(Collectors.toSet());
    }
}
