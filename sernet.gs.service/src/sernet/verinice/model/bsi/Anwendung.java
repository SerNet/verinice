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
package sernet.verinice.model.bsi;

import java.util.Collection;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyType;
import sernet.snutils.TagHelper;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.ds.IDatenschutzElement;

@SuppressWarnings("serial")
public class Anwendung extends CnATreeElement implements IBSIStrukturElement {

    private static final Logger log = Logger.getLogger(Anwendung.class);

    private static final int SCHICHT = 5;

    private final IReevaluator schutzbedarfProvider = new ProtectionRequirementsAdapter(this);

    private final ILinkChangeListener linkChangeListener = new MaximumProtectionRequirementsListener(
            this);

    // ID must correspond to entity definition in XML description
    public static final String TYPE_ID = "anwendung"; //$NON-NLS-1$

    public static final String PROP_NAME = "anwendung_name"; //$NON-NLS-1$

    public static final String PROP_KUERZEL = "anwendung_kuerzel"; //$NON-NLS-1$

    public static final String PROP_PERSBEZ = "anwendung_persbez"; //$NON-NLS-1$
    public static final String PROP_PERSBEZ_JA = "anwendung_persbez_1"; //$NON-NLS-1$
    public static final String PROP_PERSBEZ_NEIN = "anwendung_persbez_2"; //$NON-NLS-1$
    @Deprecated
    public static final String PROP_BENUTZER_OLD = "anwendung_benutzer"; //$NON-NLS-1$
    @Deprecated
    public static final String PROP_EIGENTUEMER_OLD = "anwendung_eigentümer"; //$NON-NLS-1$

    public static final String PROP_TAG = "anwendung_tag"; //$NON-NLS-1$

    public static final String PROP_ERLAEUTERUNG = "anwendung_erlaeuterung"; //$NON-NLS-1$

    public static final String PROP_PROZESSBEZUG = "anwendung_prozessbezug"; //$NON-NLS-1$
    public static final String PROP_PROZESSBEZUG_UNTERSTUETZEND = "anwendung_prozessbezug_1"; //$NON-NLS-1$
    public static final String PROP_PROZESSBEZUG_WICHTIG = "anwendung_prozessbezug_2"; //$NON-NLS-1$
    public static final String PROP_PROZESSBEZUG_WESENTLICH = "anwendung_prozessbezug_3"; //$NON-NLS-1$
    public static final String PROP_PROZESSBEZUG_HOCHGRADIG = "anwendung_prozessbezug_4"; //$NON-NLS-1$

    private static final String PROP_VERARBEITETE_INFORMATIONEN = "anwendung_prozess_informationen"; //$NON-NLS-1$

    private static final String PROP_PROZESSBESCHREIBUNG = "anwendung_prozess"; //$NON-NLS-1$

    private static final String PROP_DRINGLICHKEIT_BEGRUENDUNG = "anwendung_prozessbezug_begruendung"; //$NON-NLS-1$

    public static final String PROP_VERTRAULICHKEIT = "anwendung_vertraulichkeit"; //$NON-NLS-1$
    public static final String PROP_VERTRAULICHKEIT_NORMAL = "anwendung_vertraulichkeit_normal"; //$NON-NLS-1$
    public static final String PROP_VERTRAULICHKEIT_HOCH = "anwendung_vertraulichkeit_hoch"; //$NON-NLS-1$
    public static final String PPROP_VERTRAULICHKEIT_SEHR_HOCH = "anwendung_vertraulichkeit_sehrhoch"; //$NON-NLS-1$
    public static final String PROP_VERFUEGBARKEIT = "anwendung_verfuegbarkeit"; //$NON-NLS-1$
    public static final String PROP_VERFUEGBARKEIT_NORMAL = "anwendung_verfuegbarkeit_normal"; //$NON-NLS-1$
    public static final String PROP_VERFUEGBARKEIT_HOCH = "anwendung_verfuegbarkeit_hoch"; //$NON-NLS-1$
    public static final String PROP_VERFUEGBARKEIT_SEHR_HOCH = "anwendung_verfuegbarkeit_sehrhoch"; //$NON-NLS-1$
    public static final String PROP_INTEGRITAET = "anwendung_integritaet"; //$NON-NLS-1$
    public static final String PROP_INTEGRITAET_NORMAL = "anwendung_integritaet_normal"; //$NON-NLS-1$
    public static final String PROP_INTEGRITAET_HOCH = "anwendung_integritaet_hoch"; //$NON-NLS-1$
    public static final String PROP_INTEGRITAET_SEHR_HOCH = "anwendung_integritaet_sehrhoch"; //$NON-NLS-1$

    public static final String PROP_ESA_ENTSCHEIDUNG_DURCH = "anwendung_ergaenzendeanalyse_entscheidung_durch"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_AM = "anwendung_ergaenzendeanalyse_entscheidung_am"; //$NON-NLS-1$
    public static final String PROP_ESA_ENTSCHEIDUNG_BIS = "anwendung_ergaenzendeanalyse_entscheidung_bis"; //$NON-NLS-1$

    @Override
    public int getSchicht() {
        return SCHICHT;
    }

    @Override
    public String getKuerzel() {
        return getEntity().getPropertyValue(PROP_KUERZEL);
    }

    /**
     * Create new BSIElement.
     * 
     * @param parent
     */
    public Anwendung(CnATreeElement parent) {
        super(parent);
        setEntity(new Entity(TYPE_ID));
        getEntity().initDefaultValues(getTypeFactory());
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }

    protected Anwendung() {

    }

    @Override
    public String getTitle() {
        return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    public void setErlaeuterung(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ERLAEUTERUNG), name);
    }

    public void setKuerzel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KUERZEL), name);
    }

    public void setPersonenbezogen(boolean perso) {
        PropertyType type = getEntityType().getPropertyType(PROP_PERSBEZ);
        getEntity().setSimpleValue(type, perso ? PROP_PERSBEZ_JA : PROP_PERSBEZ_NEIN);
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public boolean canContain(Object obj) {
        if (obj instanceof IDatenschutzElement) {
            return true;
        }
        return CnaStructureHelper.canContain(obj);
    }

    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }

    @Override
    public IReevaluator getProtectionRequirementsProvider() {
        return schutzbedarfProvider;
    }

    @Override
    public Collection<? extends String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

    public void setAnzahl(int anzahl) {
        // do nothing
    }

    public void setVerarbeiteteInformationen(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_VERARBEITETE_INFORMATIONEN),
                value);
    }

    public void setProzessBeschreibung(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_PROZESSBESCHREIBUNG),
                value);
    }

    public void setProzessWichtigkeitBegruendung(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DRINGLICHKEIT_BEGRUENDUNG),
                value);
    }

    public void setProzessWichtigkeit(String value) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_PROZESSBEZUG), value);
    }

}
