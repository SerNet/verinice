/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import org.apache.commons.lang.StringUtils;

import sernet.verinice.interfaces.GenericCommand;

/**
 * Load the location to store vnl-Files in
 * from file veriniceserver-plain.properties, property: 
 * veriniceserver.vnl.repository
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LoadVNLRepoLocation extends GenericCommand {

    private String vnlRepoLocation = "";
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        String property = PropertyLoader.getVnlRepositoryLocation();
        if(StringUtils.isNotEmpty(property)){
            this.vnlRepoLocation = property;
        }

    }
    
    public String getLicenseRepoLocation(){
        return this.vnlRepoLocation;
    }

}
