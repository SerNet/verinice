package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Image;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.GefaehrdungsBaumRoot;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.IGefaehrdungsBaumElement;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

public class GefaehrdungsUmsetzung extends CnATreeElement
	implements IGefaehrdungsBaumElement {

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.IGefaehrdungsBaumElement#getDescription()
	 */
	public String getDescription() {
		return "";
	}

	private String id;
	private String titel;
	private String kategorie;
	private Boolean okay;
	private String alternative;
	private GefaehrdungsBaumRoot parent;
	private List<IGefaehrdungsBaumElement> children = new ArrayList<IGefaehrdungsBaumElement>();
	

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
	
	private static final String TYPE_ID = "gefaehrdungsumsetzung";
	
	public int getAlternativeIndex() {
		int i=-1;
		for (String alt : ALTERNATIVEN) {
			i++;
			if (alt.equals(alternative))
				return i;
		}
		return -1;
	}
	
	public GefaehrdungsUmsetzung(Gefaehrdung source) {
		this.id = source.getId();
		this.titel = source.getTitel();
		this.kategorie = source.getKategorieAsString();
		this.okay = true;
		this.alternative = GEFAEHRDUNG_ALTERNATIVE_C;
	}
	
	
	
	public void setKategorieAsString(String newKategorie) {
		this.kategorie = newKategorie;
	}
	
	public void setAlternative(String newAlternative) {
		this.alternative = newAlternative;
	}
	
	public String getAlternative() {
		return alternative;
	}



	/**
	 * @return the okay
	 */
	public Boolean getOkay() {
		return okay;
	}

	/**
	 * @param okay the okay to set
	 */
	public void setOkay(Boolean newOkay) {
		this.okay = newOkay;
	}

	/**
	 * returns the list of children (RisikoMassnahmenUmsetzungen)
	 *  in the tree.
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		return children;
	}
	
	/**
	 * returns the list of children (RisikoMassnahmenUmsetzungen)
	 *  in the tree.
	 */
	public void addGefaehrdungsBaumChild(IGefaehrdungsBaumElement newChild) {
		if (! (children.contains(newChild))) {
			children.add(newChild);
		}
	}
	
	/**
	 * returns the list of children (RisikoMassnahmenUmsetzungen)
	 *  in the tree.
	 */
	public void removeGefaehrdungsBaumChild(IGefaehrdungsBaumElement child) {
		children.remove(child);
	}

	/**
	 * returns the parent element (GefaehrdungsbaumRoot) in the tree
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		return parent;
	}


	public String getTitel() {
		return this.titel;
	}
	

	
	@Override
	public String getTypeId() {
		return this.TYPE_ID;
	}

	public String getText() {
		return titel;
	}

	public Image getImage() {
		return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKategorie() {
		return kategorie;
	}

	public void setKategorie(String kategorie) {
		this.kategorie = kategorie;
	}


	public void setParent(GefaehrdungsBaumRoot parent) {
		this.parent = parent;
	}

	


	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getAlternativeText() {
		return ALTERNATIVEN_TEXT[getAlternativeIndex()];
	}
}
