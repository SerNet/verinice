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
package sernet.verinice.model.iso27k;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public interface IControl {

    // constants for different implementation  states of controls:
    static final String PROP_IMPL = "control_implemented";

    static final String IMPLEMENTED_NO = "control_implemented_no";
    static final String IMPLEMENTED_YES = "control_implemented_yes";
    static final String IMPLEMENTED_PARTLY = "control_implemented_partly";
    static final String IMPLEMENTED_NA = "control_implemented_na";
    static final String IMPLEMENTED_NOTEDITED = "control_implemented_notedited";
    static final int IMPLEMENTED_NOTEDITED_NUMERIC = -2;
    static final int IMPLEMENTED_NA_NUMERIC = -1;

    
    
    
    
    String getTitle();
    
    /**
     * @param replaceAll
     */
    void setTitel(String replaceAll);

    String getDescription();
    
    /**
     * @param description
     */
    void setDescription(String description);

    void setMaturity(String value);
    
    int getMaturity();
    
    int getThreshold1();
    
    void setThreshold1(String value);
    
    int getThreshold2();

    void setThreshold2(String value);
    
    /**
     * Returns the used weight.
     * @return
     */
    int getWeight1();
    
    /**
     * Sets the suggested weight for maturity calculation.
     * @param value
     */
    void setWeight1(String value);
    
    /**
     * Returns the used weight.
     * @return
     */
    int getWeight2();
    
    /**
     * Sets the actually used weight for maturity calculation.
     * @param value
     */
    void setWeight2(String value);
    
    /**
     * @return the type-id of the implementation of {@link IControl} in SNCA.xml
     */
    String getTypeId();

    /**
     * @return the id of the maturity property in SNCA.xml
     */
    String getMaturityPropertyId();
    
    boolean isImplemented();

}
