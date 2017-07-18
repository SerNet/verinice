/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.snutils.AssertException;
import sernet.snutils.FormInputParser;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;

/**
 * THis test class contains tests for basic
 * functions in class {@link Entity}.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class EntityTest extends ContextConfiguration {

    /**
     * Tests method Entity.getPropertyValue(String) for
     * a huiproperty with inputtype="line"
     */
    @Test
    public void testGetStringPropertyValue() throws NumberFormatException, AssertException {
        ServerInitializer.inheritVeriniceContextState();
        final String TITLE = "Email Server (Headquarter)";
        Entity entityAsset = new Entity(Asset.TYPE_ID);
        entityAsset.createNewProperty(Asset.PROP_NAME, TITLE);
        assertEquals(TITLE, entityAsset.getPropertyValue(Asset.PROP_NAME));
    }
    
    /**
     * Tests method Entity.getPropertyValue(String) 
     * and method Entity.getDate(String) for
     * a huiproperty with inputtype="date"
     */
    @Test
    public void testGetDatePropertyValue() throws NumberFormatException, AssertException {
        ServerInitializer.inheritVeriniceContextState();
        Entity entityAudit = new Entity(Audit.TYPE_ID);
        Calendar now = Calendar.getInstance();
        
        String nowAsTimestampString = Property.convertCalendarToString(now);
        entityAudit.createNewProperty(Audit.PROP_STARTDATE, nowAsTimestampString);
        assertEquals(now.getTime(), entityAudit.getDate(Audit.PROP_STARTDATE)); 
        
        java.sql.Date nowAsSqlDate = new java.sql.Date(Long.parseLong(nowAsTimestampString));
        String nowAsFormatedString = FormInputParser.dateToString(nowAsSqlDate);      
        assertEquals(nowAsFormatedString, entityAudit.getPropertyValue(Audit.PROP_STARTDATE));
    }

}
