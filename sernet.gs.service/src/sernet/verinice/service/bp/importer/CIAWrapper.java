/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
package sernet.verinice.service.bp.importer;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class CIAWrapper {
    
    private boolean confidentiality;
    private boolean integrity;
    private boolean availability;

    public CIAWrapper(boolean confidentiality, 
            boolean integrity, 
            boolean availability) {
        super();
        this.confidentiality = confidentiality;
        this.integrity = integrity;
        this.availability = availability;
    }

    
    public CIAWrapper() {}


    /**
     * @return the confidentiality
     */
    public boolean isConfidentiality() {
        return confidentiality;
    }


    /**
     * @param confidentiality the confidentiality to set
     */
    public void setConfidentiality(boolean confidentiality) {
        this.confidentiality = confidentiality;
    }


    /**
     * @return the integrity
     */
    public boolean isIntegrity() {
        return integrity;
    }


    /**
     * @param integrity the integrity to set
     */
    public void setIntegrity(boolean integrity) {
        this.integrity = integrity;
    }


    /**
     * @return the availability
     */
    public boolean isAvailability() {
        return availability;
    }


    /**
     * @param availability the availability to set
     */
    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
    
    
    
    

}
