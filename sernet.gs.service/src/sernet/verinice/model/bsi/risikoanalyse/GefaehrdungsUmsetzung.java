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
package sernet.verinice.model.bsi.risikoanalyse;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class GefaehrdungsUmsetzung extends CnATreeElement implements IGefaehrdungsBaumElement, ITypedElement {

    private IGefaehrdungsBaumElement gefaehrdungsParent;

    public static final String GEFAEHRDUNG_ALTERNATIVE_A = "A"; //$NON-NLS-1$
    public static final String GEFAEHRDUNG_ALTERNATIVE_B = "B"; //$NON-NLS-1$
    public static final String GEFAEHRDUNG_ALTERNATIVE_C = "C"; //$NON-NLS-1$
    public static final String GEFAEHRDUNG_ALTERNATIVE_D = "D"; //$NON-NLS-1$

    public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_A = Messages.GefaehrdungsUmsetzung_4;
    public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_B = Messages.GefaehrdungsUmsetzung_5;
    public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_C = Messages.GefaehrdungsUmsetzung_6;
    public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_D = Messages.GefaehrdungsUmsetzung_7;

    private static final String[] ALTERNATIVEN = { GEFAEHRDUNG_ALTERNATIVE_A, GEFAEHRDUNG_ALTERNATIVE_B, GEFAEHRDUNG_ALTERNATIVE_C, GEFAEHRDUNG_ALTERNATIVE_D, };

    private static final String[] ALTERNATIVEN_TEXT = { GEFAEHRDUNG_ALTERNATIVE_TEXT_A, GEFAEHRDUNG_ALTERNATIVE_TEXT_B, GEFAEHRDUNG_ALTERNATIVE_TEXT_C, GEFAEHRDUNG_ALTERNATIVE_TEXT_D, };

    public static final String TYPE_ID = "gefaehrdungsumsetzung"; //$NON-NLS-1$

    public static final String PROP_ID = "gefaehrdungsumsetzung_id"; //$NON-NLS-1$
    public static final String PROP_TITEL = "gefaehrdungsumsetzung_titel"; //$NON-NLS-1$
    public static final String PROP_KATEGORIE = "gefaehrdungsumsetzung_kategorie"; //$NON-NLS-1$
    public static final String PROP_ALTERNATIVE = "gefaehrdungsumsetzung_alternative"; //$NON-NLS-1$

    public static final String PROP_OKAY = "gefaehrdungsumsetzung_okay"; //$NON-NLS-1$
    public static final String PROP_OKAY_YES = "gefaehrdungsumsetzung_okay_yes"; //$NON-NLS-1$
    public static final String PROP_OKAY_NO = "gefaehrdungsumsetzung_okay_no"; //$NON-NLS-1$

    public static final String PROP_URL = "gefaehrdungsumsetzung_url"; //$NON-NLS-1$
    public static final String PROP_STAND = "gefaehrdungsumsetzung_stand"; //$NON-NLS-1$
    public static final String PROP_DESCRIPTION = "gefaehrdungsumsetzung_description"; //$NON-NLS-1$

    public int getAlternativeIndex() {
        int i = -1;
        for (String alt : ALTERNATIVEN) {
            i++;
            if (alt.equals(getAlternative())) {
                return i;
            }
        }
        return -1;
    }

    public GefaehrdungsUmsetzung(CnATreeElement parent) {
        super(parent);
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GefaehrdungsUmsetzung)) {
            return false;
        }
        GefaehrdungsUmsetzung gef2 = (GefaehrdungsUmsetzung) obj;

        if (gef2.getDbId() == null || getDbId() == null) {
            return super.equals(obj);
        }

        return gef2.getDbId().equals(getDbId());
    }

    protected GefaehrdungsUmsetzung() {
        // hibernate constructor
    }

    public void setKategorieAsString(String newKategorie) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KATEGORIE), newKategorie);
    }

    public void setAlternative(String newAlternative) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ALTERNATIVE), newAlternative);
    }

    public String getAlternative() {
        return getEntity().getSimpleValue(PROP_ALTERNATIVE);
    }

    /**
     * @return the okay
     */
    public Boolean getOkay() {
        return getEntity().isSelected(PROP_OKAY_YES);
    }

    /**
     * @param okay
     *            the okay to set
     */
    public void setOkay(Boolean newOkay) {
        if (newOkay) {
            getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OKAY), PROP_OKAY_YES);
        } else {
            getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_OKAY), PROP_OKAY_NO);
        }
    }

    /**
     * returns the list of children (RisikoMassnahmenUmsetzungen) in the tree.
     */
    @Override
    public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
        List<IGefaehrdungsBaumElement> children = new ArrayList<IGefaehrdungsBaumElement>(getChildren().size());
        for (Object object : getChildren()) {
            if (object instanceof IGefaehrdungsBaumElement) {
                children.add((IGefaehrdungsBaumElement) object);
            }
        }
        return children;
    }

    /**
     * adds a child (RisikoMassnahmenUmsetzungen) in the tree.
     */
    public void addGefaehrdungsBaumChild(IGefaehrdungsBaumElement newChild) {
        if (newChild instanceof CnATreeElement) {
            addChild((CnATreeElement) newChild);
        }
    }

    @Override
    public boolean canContain(Object obj) {
        if (obj instanceof MassnahmenUmsetzung) {
            return true;
        }
        return false;
    }

    /**
     * removes one child (RisikoMassnahmenUmsetzungen) from the tree
     */
    public void removeGefaehrdungsBaumChild(IGefaehrdungsBaumElement child) {
        if (child instanceof CnATreeElement) {
            removeChild((CnATreeElement) child);
        }
    }

    @Override
    public String getTitle() {
        return "[" + getAlternative() + "] " + //$NON-NLS-1$ //$NON-NLS-2$
                getEntity().getSimpleValue(PROP_TITEL) + " (" + getAlternativeText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getText() {
        return getEntity().getSimpleValue(PROP_TITEL);
    }

    @Override
    public String getId() {
        return getEntity().getSimpleValue(PROP_ID);
    }

    public void setId(String id) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ID), id);
    }

    public String getKategorie() {
        return getEntity().getSimpleValue(PROP_KATEGORIE);
    }

    public void setKategorie(String kategorie) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KATEGORIE), kategorie);
    }

    public void setGefaehrdungsParent(GefaehrdungsBaumRoot parent) {
        this.gefaehrdungsParent = parent;
    }

    @Override
    public void setTitel(String titel) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_TITEL), titel);
    }

    public String getAlternativeText() {
        try {
            return ALTERNATIVEN_TEXT[getAlternativeIndex()];
        } catch (IndexOutOfBoundsException e) {
            Logger.getLogger(this.getClass()).debug(e);
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
        return this.gefaehrdungsParent;
    }

    public String getUrl() {
        return getEntity().getSimpleValue(PROP_URL);
    }

    public void setUrl(String url) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_URL), url);
    }

    public String getStand() {
        return getEntity().getSimpleValue(PROP_STAND);
    }

    public void setStand(String stand) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_STAND), stand);
    }

    public void setDescription(String beschreibung) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DESCRIPTION), beschreibung);
    }

    @Override
    public String getDescription() {
        return getEntity().getSimpleValue(PROP_DESCRIPTION);
    }

    public static String[] getAlternativenText() {
        return ALTERNATIVEN_TEXT;
    }
}
