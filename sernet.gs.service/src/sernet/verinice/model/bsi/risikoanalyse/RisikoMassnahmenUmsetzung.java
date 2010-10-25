/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package sernet.verinice.model.bsi.risikoanalyse;

import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.ITypedElement;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author ahanekop[at]sernet[dot]de
 * 
 */
@SuppressWarnings("serial")
public class RisikoMassnahmenUmsetzung extends MassnahmenUmsetzung implements IGefaehrdungsBaumElement, ITypedElement {

    private GefaehrdungsUmsetzung parent;
    private RisikoMassnahme massnahme;

    public static final String TYPE_ID = "mnums"; //$NON-NLS-1$

    public RisikoMassnahmenUmsetzung(CnATreeElement superParent, GefaehrdungsUmsetzung myParent, RisikoMassnahme massnahme) {
        super();
        // use the same entity as in MassnahmenUmsetzung
        setEntity(new Entity(MassnahmenUmsetzung.TYPE_ID));
        this.parent = myParent;
        this.massnahme = massnahme;
        setStufe('Z');
    }

    public RisikoMassnahmenUmsetzung(CnATreeElement superParent, GefaehrdungsUmsetzung myParent) {
        super();
        // use the same entity as in MassnahmenUmsetzung
        setEntity(new Entity(MassnahmenUmsetzung.TYPE_ID));
        this.parent = myParent;
    }

    RisikoMassnahmenUmsetzung() {
        // hibernate constructor
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    /**
     * Must be implemented due to Interface IGefaehrdungsBaumElement.
     * 
     * A RisikoMassnahmenUmsetzung never has children, therefore always returns
     * null.
     * 
     * @return - null
     */
    public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
        return null;
    }

    /**
     * Must be implemented due to Interface IGefaehrdungsBaumElement.
     * 
     * Returns the parent element in the tree, which is a GefaehrdungsUmsetzung.
     * 
     * @return - the parent element "parent" (IGefaehrdungsBaumElement)
     */
    public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
        // TODO Auto-generated method stub
        return parent;
    }

    /**
     * Sets the parent element "parent" (GefaehrdungsUmsetzung) in the tree if
     * the parent is null, else nothing.
     * 
     * @param newParent
     *            - new GefaehrdungsUmsetzung which is to be the new parent
     */
    public void setGefaehrdungsBaumParent(GefaehrdungsUmsetzung newParent) {
        if (parent == null) {
            parent = newParent;
        }
    }

    /**
     * Must be implemented due to Interface IGefaehrdungsBaumElement.
     * 
     * Calls the local getTitle() method.
     * 
     * @return - name of the RisikoMassnahmenUmsetzung
     */
    public String getText() {
        return this.getTitle();
    }

    /**
     * Overrides and calls MassnahmenUmsetzung.getTitle().
     * 
     * @return - title of the RisikoMassnahmenUmsetzung
     */
    @Override
    public String getTitle() {
        return super.getName();
    }

    /**
     * Implemented for reasons of conformity.
     * 
     * Calls MassnahmenUmsetzung.setName() to set the title of a
     * RisikoMassnahmenUmsetzung.
     * 
     * @param name
     *            - new name of the RisikoMassnahmenUmsetzung
     */
    public void setTitle(String name) {
        super.setName(name);
    }

    /**
     * Must be implemented due to Interface IGefaehrdungsBaumElement.
     * 
     * @return - description (String) of the RisikoMassnahmenUmsetzung.
     */
    public String getDescription() {
        return massnahme.getDescription();
    }

    /**
     * Returns the nuber of the Massnahme.
     * 
     * @return the number
     */
    public String getNumber() {
        return getKapitel();
    }

    /**
     * Sets the number of the Massnahme.
     * 
     * @param newNumber
     *            the number to set
     */
    public void setNumber(String newNumber) {
        setKapitel(newNumber);
    }

    public boolean isInitNeeded() {
        return massnahme == null;
    }

    /**
     * Returns the <code>RisikoMassnahme</code>
     * 
     * Note: The result is only valid after calling
     * <code>RisikoMassnahmeHome.getInstance().initRisikoMassnahmeUmsetzung(risikoMassnahmenUmsetzung);</code>
     * 
     * @return
     */
    public RisikoMassnahme getRisikoMassnahme() {
        return massnahme;
    }

    public void setMassnahme(RisikoMassnahme massnahme) {
        this.massnahme = massnahme;
    }
}
