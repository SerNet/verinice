package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class Anwendung extends CnATreeElement 
	implements IBSIStrukturElement {
	
	private final ISchutzbedarfProvider schutzbedarfProvider 
		= new SchutzbedarfAdapter(this);
	
	
	private final ILinkChangeListener linkChangeListener
		= new MaximumSchutzbedarfListener(this);

	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "anwendung"; //$NON-NLS-1$

	public static final String PROP_NAME = "anwendung_name"; //$NON-NLS-1$

	public static final String PROP_KUERZEL = "anwendung_kuerzel"; //$NON-NLS-1$

	public static final String PROP_PERSBEZ 		= "anwendung_persbez"; //$NON-NLS-1$
	public static final String PROP_PERSBEZ_JA 		= "anwendung_persbez_1"; //$NON-NLS-1$
	public static final String PROP_PERSBEZ_NEIN 	= "anwendung_persbez_2"; //$NON-NLS-1$
	@Deprecated
	public static final String PROP_BENUTZER_OLD = "anwendung_benutzer"; //$NON-NLS-1$
	@Deprecated
	public static final String PROP_EIGENTUEMER_OLD = "anwendung_eigentümer"; //$NON-NLS-1$

	public static final String PROP_TAG			= "anwendung_tag";

	public static final String PROP_ERLAEUTERUNG = "anwendung_erlaeuterung";

	public static final String PROP_PROZESSBEZUG				= "anwendung_prozessbezug";
	public static final String PROP_PROZESSBEZUG_UNTERSTUETZEND = "anwendung_prozessbezug_1";
	public static final String PROP_PROZESSBEZUG_WICHTIG 		= "anwendung_prozessbezug_2";
	public static final String PROP_PROZESSBEZUG_WESENTLICH 	= "anwendung_prozessbezug_3";
	public static final String PROP_PROZESSBEZUG_HOCHGRADIG 	= "anwendung_prozessbezug_4";


	private static final String PROP_VERARBEITETE_INFORMATIONEN = "anwendung_prozess_informationen";

	private static final String PROP_PROZESSBESCHREIBUNG = "anwendung_prozess";

	private static final String PROP_DRINGLICHKEIT_BEGRUENDUNG = "anwendung_prozessbezug_begruendung";



	private static EntityType entityType;

	public int getSchicht() {
		return 5;
	}

	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}

	/**
	 * Create new BSIElement.
	 * 
	 * @param parent
	 */
	public Anwendung(CnATreeElement parent) {
		super(parent);
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME),
				Messages.Anwendung_4);
	}

	private Anwendung() {

	}

	@Override
	public String getTitel() {
		return getEntity().getProperties(PROP_NAME).getProperty(0)
				.getPropertyValue();
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_NAME), name);
	}

	public void setErlaeuterung(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_KUERZEL), name);
	}
	
	public void setPersonenbezogen(boolean perso) {
		PropertyType type = entityType.getPropertyType(PROP_PERSBEZ);
		getEntity().setSimpleValue(type, perso ? PROP_PERSBEZ_JA : PROP_PERSBEZ_NEIN);
	}
	
	

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof IDatenschutzElement)
			return true;
		return CnaStructureHelper.canContain(obj);
	}

	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public ISchutzbedarfProvider getSchutzbedarfProvider() {
		return schutzbedarfProvider;
	}

	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}

	public void setAnzahl(int anzahl) {
		// do nothing
	}

	public void setVerarbeiteteInformationen(String value) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_VERARBEITETE_INFORMATIONEN), value);
	}

	public void setProzessBeschreibung(String value) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_PROZESSBESCHREIBUNG), value);
	}

	public void setProzessWichtigkeitBegruendung(String value) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_DRINGLICHKEIT_BEGRUENDUNG), value);
	}

	public void setProzessWichtigkeit(String value) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_PROZESSBEZUG), value);
	}

}
