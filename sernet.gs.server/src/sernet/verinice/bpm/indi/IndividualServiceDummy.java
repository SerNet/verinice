/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm.indi;

import sernet.verinice.bpm.ProcessServiceVerinice;
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bpm.ProcessInformation;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualServiceDummy extends ProcessServiceVerinice implements IIndividualService {

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIndividualService#startProcess(sernet.verinice.interfaces.bpm.IndividualServiceParameter)
     */
    @Override
    public IProcessStartInformation startProcess(IndividualServiceParameter parameter) {
        return new ProcessInformation(0);
    }

}
