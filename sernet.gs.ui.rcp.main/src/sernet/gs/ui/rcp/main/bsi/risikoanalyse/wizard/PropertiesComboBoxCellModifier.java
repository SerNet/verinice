package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;

public class PropertiesComboBoxCellModifier implements ICellModifier {
	private Viewer viewer;
	private RiskAnalysisWizard wizard;
	private RiskHandlingPage page;

	public PropertiesComboBoxCellModifier(Viewer viewer, RiskAnalysisWizard wizard, RiskHandlingPage page) {
		this.viewer = viewer;
		this.wizard = wizard;
		this.page = page;
	}

	public boolean canModify(Object element, String property) {
		 if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property))
			 return true;
		 return false;
	}
	
	public Object getValue(Object element, String property) {
	    GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
	    
	    if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property))
	      return gef.getAlternativeIndex();
	    
	    return null;
	}

	public void modify(Object element, String property, Object value) {
		Object elmt = ((TableItem)element).getData();
		
		ArrayList<GefaehrdungsUmsetzung> arrListRiskGefaehrdungen = 
			wizard.getNotOKGefaehrdungsUmsetzungen();
		
		if (elmt instanceof GefaehrdungsUmsetzung) {
			GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) elmt;
			if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
				int index = (Integer) value;
				switch (index) {
				case 0:
					gef.setAlternative(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_A);
					if (! arrListRiskGefaehrdungen.contains(gef)) {
						arrListRiskGefaehrdungen.add(gef);
					}
					break;
				case 1:
					gef.setAlternative(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_B);
					if (arrListRiskGefaehrdungen.contains(gef)) {
						arrListRiskGefaehrdungen.remove(gef);
					}
					break;
				case 2:
					gef.setAlternative(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_C);
					if (arrListRiskGefaehrdungen.contains(gef)) {
						arrListRiskGefaehrdungen.remove(gef);
					}
					break;
				case 3:
					gef.setAlternative(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_D);
					if (arrListRiskGefaehrdungen.contains(gef)) {
						arrListRiskGefaehrdungen.remove(gef);
					}
					break;
				default:
					break;
				}
				
				// TODO Alternative in DB speichern
				
				wizard.setNotOKGefaehrdungsUmsetzungen(arrListRiskGefaehrdungen);
				viewer.refresh();
				
				if (wizard.getNotOKGefaehrdungsUmsetzungen().isEmpty()) {
					page.setPageComplete(false);
				} else {
					page.setPageComplete(true);
				}  /* end if isEmpty */
			}  /* end if equals */
		}  /* end if instanceof */
	}  /* end method modify() */
}  /* end class PropertiesComboBoxCellModifier */
