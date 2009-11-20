package sernet.gs.web;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;

public class UmsetzungConverter implements Converter {

	public static final String ENTBERHRLICH = "Entbehrlich";
	
	public static final String JA = "Ja";
	
	public static final String NEIN = "Nein";
	
	public static final String TEILWEISE = "Teilweise";
	
	public static final String UNBEARBEITET = "Unbearbeitet";

	private static final String ESTABLISHED = "Level 3: Established";

	private static final String MANAGED = "Level 2: Managed";

	private static final String OPTIMIZING = "Level 5: Optimizing";

	private static final String PERFORMED = "Level 1: Performed";

	private static final String PREDICTABLE = "Level 4: Predictable";
	
	
	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String text) {
		Object value = null;
		if(ENTBERHRLICH.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH;
		} else if(JA.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_JA;
		} else if(NEIN.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_NEIN;
		} else if(TEILWEISE.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE;
		} else if(UNBEARBEITET.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET;
		} else if(ESTABLISHED.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED;
		} else if(MANAGED.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_MANAGED;
		} else if(OPTIMIZING.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING;
		} else if(PERFORMED.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED;
		} else if(PREDICTABLE.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE;
		}
		return value;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		String text = null;
		if(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH.equals(value)) {
			text=ENTBERHRLICH;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_JA.equals(value)) {
			text=JA;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_NEIN.equals(value)) {
			text=NEIN;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE.equals(value)) {
			text=TEILWEISE;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(value)) {
			text=UNBEARBEITET;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED.equals(value)) {
			text=ESTABLISHED;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_MANAGED.equals(value)) {
			text=MANAGED;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING.equals(value)) {
			text=OPTIMIZING;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED.equals(value)) {
			text=PERFORMED;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE.equals(value)) {
			text=PREDICTABLE;
		}
		return text;
	}

}
