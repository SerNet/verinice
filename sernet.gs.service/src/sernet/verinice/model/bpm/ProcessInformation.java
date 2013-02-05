/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.model.bpm;

import java.io.Serializable;

import sernet.verinice.interfaces.bpm.IProcessStartInformation;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ProcessInformation implements IProcessStartInformation, Serializable {

    private int number;
    
    public ProcessInformation() {
        super();
        number=0;
    }


    public ProcessInformation(int number) {
        super();
        this.number = number;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessStartInformation#getNumber()
     */
    @Override
    public int getNumber() {
        return this.number;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessStartInformation#increaseNumber()
     */
    @Override
    public void increaseNumber() {
        this.number++;
    }

}
