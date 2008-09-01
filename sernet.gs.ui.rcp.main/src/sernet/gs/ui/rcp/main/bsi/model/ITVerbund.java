package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class ITVerbund extends CnATreeElement 
	implements IBSIStrukturElement {
	
	private static final String TYPE_ID = "itverbund"; //$NON-NLS-1$
	public static final String PROP_NAME = "itverbund_name"; //$NON-NLS-1$
	
	private String kuerzel = " ";
	
	private static EntityType entityType;
	
	public String getKuerzel() {
		return kuerzel;
	}
	
	
	public ITVerbund(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME),
				Messages.ITVerbund_2);
	}
	
	public int getSchicht() {
		return 1;
	}
	
	private ITVerbund() {
		// hibernate
	}
	
	public void createNewCategories() {
		addChild(new PersonenKategorie(this));
		addChild(new GebaeudeKategorie(this));
		addChild(new RaeumeKategorie(this));
		addChild(new AnwendungenKategorie(this));
		addChild(new ServerKategorie(this));
		addChild(new ClientsKategorie(this));
		addChild(new SonstigeITKategorie(this));
		addChild(new NKKategorie(this));
		addChild(new TKKategorie(this));
	}
	
	public CnATreeElement getCategory(String id) {
		for (CnATreeElement category : getChildren()) {
			if (category.getTypeId() != null
					&& category.getTypeId().equals(id))
				return (CnATreeElement) category;
		}
		return null;
	}
	
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
		}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Baustein
				|| obj instanceof BausteinUmsetzung
				|| obj instanceof AnwendungenKategorie 
				|| obj instanceof ClientsKategorie
				|| obj instanceof SonstigeITKategorie
				|| obj instanceof GebaeudeKategorie
				|| obj instanceof NKKategorie
				|| obj instanceof PersonenKategorie
				|| obj instanceof RaeumeKategorie 
				|| obj instanceof ServerKategorie
				|| obj instanceof TKKategorie
				|| obj instanceof FinishedRiskAnalysis
				)
			return true;
		return false;
	}

}
