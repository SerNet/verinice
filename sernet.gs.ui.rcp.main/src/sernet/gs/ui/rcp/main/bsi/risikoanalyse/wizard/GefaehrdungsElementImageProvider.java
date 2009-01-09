package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.swt.graphics.Image;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

public class GefaehrdungsElementImageProvider {

	public static Image getImage(Object element) {
		if (element instanceof GefaehrdungsUmsetzung) {
			return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
		}
		
		else if (element instanceof GefaehrdungsBaumRoot ) {
			return ImageCache.getInstance().getImage(ImageCache.BAUSTEIN);
			
		}

		else if (element instanceof RisikoMassnahmenUmsetzung ) {
			return ImageCache.getInstance().getImage(ImageCache.RISIKO_MASSNAHMEN_UMSETZUNG);
			
		}
		
		return ImageCache.getInstance().getImage(
				ImageCache.UNKNOWN);
	}

}
