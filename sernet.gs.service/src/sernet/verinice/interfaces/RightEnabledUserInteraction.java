/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * Sebastian Hagedorn - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

/**
 * Right enabled user interactions checks if a right ID is assigned to the user
 * account.
 * 
 * @author Sebastian Hagedorn
 */
public interface RightEnabledUserInteraction {

    /**
     * Ask RightServiceClient if user is authorized to perform the interaction.
     * 
     * @return true if authorized, false if not
     */
    public boolean checkRights();
    
    /**
     * @return The rightID of the interaction
     */
    public String getRightID();
    
}
