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

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
        "classpath:/sernet/gs/server/spring/veriniceserver-search-base.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-search.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-updatenews-dummy.xml", //NON-NLS-1$
        "classpath:/sernet/gs/server/spring/veriniceserver-licensemanagement.xml", //NON-NLS-1$
        "classpath:/verinice-test.xml"
})
public abstract class ContextConfiguration  {


}
