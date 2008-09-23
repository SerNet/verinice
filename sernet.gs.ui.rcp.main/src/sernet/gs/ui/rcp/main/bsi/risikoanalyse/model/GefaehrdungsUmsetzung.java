package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.demo.html.Entities;
import org.eclipse.swt.graphics.Image;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Messages;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.GefaehrdungsBaumRoot;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.IGefaehrdungsBaumElement;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class GefaehrdungsUmsetzung extends CnATreeElement
	implements IGefaehrdungsBaumElement {

	private List<IGefaehrdungsBaumElement> gefaehrdungsChildren = new ArrayList<IGefaehrdungsBaumElement>();
	private IGefaehrdungsBaumElement gefaehrdungsParent;
	
	private static EntityType entityType;

	
	
	public static final String GEFAEHRDUNG_ALTERNATIVE_A = "A";
	public static final String GEFAEHRDUNG_ALTERNATIVE_B = "B";
	public static final String GEFAEHRDUNG_ALTERNATIVE_C = "C";
	public static final String GEFAEHRDUNG_ALTERNATIVE_D = "D";
	
	public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_A = "A Reduktion";
	public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_B = "B Umstrukturierung";
	public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_C = "C Übernahme";
	public static final String GEFAEHRDUNG_ALTERNATIVE_TEXT_D = "D Transfer";
	

	public static final String[] ALTERNATIVEN = {GEFAEHRDUNG_ALTERNATIVE_A,
		GEFAEHRDUNG_ALTERNATIVE_B,
		GEFAEHRDUNG_ALTERNATIVE_C,
		GEFAEHRDUNG_ALTERNATIVE_D,};
	
	public static final String[] ALTERNATIVEN_TEXT = {
		GEFAEHRDUNG_ALTERNATIVE_TEXT_A,
		GEFAEHRDUNG_ALTERNATIVE_TEXT_B, 
		GEFAEHRDUNG_ALTERNATIVE_TEXT_C, 
		GEFAEHRDUNG_ALTERNATIVE_TEXT_D, 
	};
	
	public static final String TYPE_ID 				= "gefaehrdungsumsetzung";
	
	public static final String PROP_ID 				= "gefaehrdungsumsetzung_id";
	public static final String PROP_TITEL			= "gefaehrdungsumsetzung_titel";
	public static final String PROP_KATEGORIE 		= "gefaehrdungsumsetzung_kategorie";
	public static final String PROP_ALTERNATIVE 	= "gefaehrdungsumsetzung_alternative";
	
	public static final String PROP_OKAY 			= "gefaehrdungsumsetzung_okay";
	public static final String PROP_OKAY_YES 		= "gefaehrdungsumsetzung_okay_yes";
	public static final String PROP_OKAY_NO 		= "gefaehrdungsumsetzung_okay_no";
	
	public static final String PROP_URL 			= "gefaehrdungsumsetzung_url";
	public static final String PROP_STAND 			= "gefaehrdungsumsetzung_stand";
	public static final String PROP_DESCRIPTION 	= "gefaehrdungsumsetzung_description";

	
	public int getAlternativeIndex() {
		int i=-1;
		for (String alt : ALTERNATIVEN) {
			i++;
			if (alt.equals(getAlternative()))
				return i;
		}
		return -1;
	}
	
	protected GefaehrdungsUmsetzung(CnATreeElement parent) {
		super(parent);
		
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GefaehrdungsUmsetzung))
			return false;
		GefaehrdungsUmsetzung gef2 = (GefaehrdungsUmsetzung) obj;

		if (gef2.getDbId() == null || this.getDbId() == null)
			return super.equals(obj);
		
		return gef2.getDbId().equals(this.getDbId());
	}
	
	private GefaehrdungsUmsetzung() {
		// hibernate constructor
	}
	
	public void setKategorieAsString(String newKategorie) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_KATEGORIE),
				newKategorie);
	}
	
	public void setAlternative(String newAlternative) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_ALTERNATIVE),
				newAlternative);
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
		if (newOkay)
			getEntity().setSimpleValue(entityType.getPropertyType(PROP_OKAY),
					PROP_OKAY_YES);
		else
			getEntity().setSimpleValue(entityType.getPropertyType(PROP_OKAY),
					PROP_OKAY_NO);
	}

	/**
	 * returns the list of children (RisikoMassnahmenUmsetzungen)
	 *  in the tree.
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		return gefaehrdungsChildren;
	}
	
	/**
	 * returns the list of children (RisikoMassnahmenUmsetzungen)
	 *  in the tree.
	 */
	public void addGefaehrdungsBaumChild(IGefaehrdungsBaumElement newChild) {
		if (! (gefaehrdungsChildren.contains(newChild))) {
			gefaehrdungsChildren.add(newChild);
		}
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof MassnahmenUmsetzung)
			return true;
		return false;
	}
	
	/**
	 * returns the list of children (RisikoMassnahmenUmsetzungen)
	 *  in the tree.
	 */
	public void removeGefaehrdungsBaumChild(IGefaehrdungsBaumElement child) {
		gefaehrdungsChildren.remove(child);
	}

	public String getTitel() {
		return  "[" + getAlternative() + "] " + 
			getEntity().getSimpleValue(PROP_TITEL)
			+ " (" + getAlternativeText() + ")";
	}
	
	
	@Override
	public String getTypeId() {
		return this.TYPE_ID;
	}

	public String getText() {
		return getEntity().getSimpleValue(PROP_TITEL);
	}

	public Image getImage() {
		return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
	}

	public String getId() {
		return getEntity().getSimpleValue(PROP_ID);
	}

	public void setId(String id) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_ID),
				id);
	}

	public String getKategorie() {
		return getEntity().getSimpleValue(PROP_KATEGORIE);
	}

	public void setKategorie(String kategorie) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_KATEGORIE),
				kategorie);
	}


	public void setGefaehrdungsParent(GefaehrdungsBaumRoot parent) {
		this.gefaehrdungsParent = parent;
	}

	
	public void setTitel(String titel) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_TITEL),
				titel);
	}

	public String getAlternativeText() {
		try {
			return ALTERNATIVEN_TEXT[getAlternativeIndex()];
		} catch (IndexOutOfBoundsException e) {
			Logger.getLogger(this.getClass()).debug(e);
		}
		return "";
	}

	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		return this.gefaehrdungsParent;
	}

	public String getUrl() {
		return getEntity().getSimpleValue(PROP_URL);
	}
	
	public void setUrl(String url) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_URL),
				url);
	}
	
	public String getStand() {
		return getEntity().getSimpleValue(PROP_STAND);
	}
	
	public void setStand(String stand) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_STAND),
				stand);
	}

	public void setDescription(String beschreibung) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_DESCRIPTION),
				beschreibung);
	}
	
	public String getDescription() {
		return getEntity().getSimpleValue(PROP_DESCRIPTION);
	}
}
