/**
 * Copyright 2016 Moritz Reiter.
 *
 * <p>This file is part of Verinice.
 *
 * <p>Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * <p>Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see http://www.gnu.org/licenses/.
 */

package sernet.verinice.samt.rcp;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;


/**
 * @author Moritz Reiter
 */
public class IsaDecoratorUtilTest extends BeforeEachVNAImportHelper {
    
    
    @Before
    public void create() {
        Audit audit = new Audit();
    }   

    @Test
    public void test() {

        fail("Not yet implemented");
    }

    @Override
    protected String getFilePath() {

        // TODO Auto-generated method stub
        return null;
    }



    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {

        // TODO Auto-generated method stub
        return null;
    }

}
