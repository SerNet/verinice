package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class NetzKomponente extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "netzkomponente"; //$NON-NLS-1$
	public static final String PROP_NAME = "netzkomponente_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "netzkomponente_kuerzel"; //$NON-NLS-1$
	public static final String PROP_TAG			= "netzkomponente_tag"; //$NON-NLS-1$
	public static final String PROP_ERLAEUTERUNG = "netzkomponente_erlaeuterung"; //$NON-NLS-1$
	
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public NetzKomponente(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME), 
				"Neue Verbindung");
	}
	
	public int getSchicht() {
		return 4;
	}
	
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	
	
	private NetzKomponente() {
		
	}
	
	@Override
	public String getTitel() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return CnaStructureHelper.canContain(obj);
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}

	public void setErlaeuterung(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KUERZEL), name);
	}

	public void setAnzahl(int anzahl) {
		// do nothing
	}



}
