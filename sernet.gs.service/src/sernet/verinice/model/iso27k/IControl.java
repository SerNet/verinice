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
    public static final String PROP_IMPL = "control_implemented";

    public static final String IMPLEMENTED_NO = "control_implemented_no";
    public static final String IMPLEMENTED_YES = "control_implemented_yes";
    public static final String IMPLEMENTED_PARTLY = "control_implemented_partly";
    public static final String IMPLEMENTED_NA = "control_implemented_na";
    public static final String IMPLEMENTED_NOTEDITED = "control_implemented_notedited";
    public static final int IMPLEMENTED_NOTEDITED_NUMERIC = -2;
    public static final int IMPLEMENTED_NA_NUMERIC = -1;

    
    
    
    
    public String getTitle();
    
    /**
     * @param replaceAll
     */
    void setTitel(String replaceAll);

    public String getDescription();
    
    /**
     * @param description
     */
    void setDescription(String description);

    public void setMaturity(String value);
    
    public int getMaturity();
    
    public int getThreshold1();
    
    public void setThreshold1(String value);
    
    public int getThreshold2();

    public void setThreshold2(String value);
    
    /**
     * Returns the used weight.
     * @return
     */
    public int getWeight1();
    
    /**
     * Sets the suggested weight for maturity calculation.
     * @param value
     */
    public void setWeight1(String value);
    
    /**
     * Returns the used weight.
     * @return
     */
    public int getWeight2();
    
    /**
     * Sets the actually used weight for maturity calculation.
     * @param value
     */
    public void setWeight2(String value);
    
    /**
     * @return the type-id of the implementation of {@link IControl} in SNCA.xml
     */
    public String getTypeId();

    /**
     * @return the id of the maturity property in SNCA.xml
     */
    public String getMaturityPropertyId();
    
    public boolean isImplemented();

}
