/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test.helper.vnaimport;

import org.junit.After;
import org.junit.Before;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class BeforeEachVNAImportHelper extends AbstractVNAImportHelper {

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }

    
    @After
    public void tearDown() throws CommandException
    {
        super.tearDown();
    }
}
