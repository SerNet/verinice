package sernet.gs.ui.rcp.main.bsi.views;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Label provider fpr BSI model elements.
 * 
 * @author koderman@sernet.de
 * 
 */
public class BSIModelViewLabelProvider extends LabelProvider {
	
	public BSIModelViewLabelProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;
	
	
		public Image getImage(Object obj) {
//			Logger.getLogger(this.getClass()).debug("getImage " + obj);
			
			Object cachedObject = cache.getCachedObject(obj);
			if (cachedObject == null) {
				cache.addObject(obj);
			} else {
				obj = cachedObject;
			}
			
			if (obj instanceof BausteinUmsetzung) {
				BausteinUmsetzung bu = (BausteinUmsetzung) obj;
				switch (bu.getErreichteSiegelStufe()) {
				case 'A':
					return ImageCache.getInstance().getImage(
							ImageCache.BAUSTEIN_UMSETZUNG_A);
				case 'B':
					return ImageCache.getInstance().getImage(
							ImageCache.BAUSTEIN_UMSETZUNG_B);
				case 'C':
					return ImageCache.getInstance().getImage(
							ImageCache.BAUSTEIN_UMSETZUNG_C);
				}
				// else return default image
				return ImageCache.getInstance().getImage(
						ImageCache.BAUSTEIN_UMSETZUNG);
			} 
			
			else if (obj instanceof LinkKategorie) {
				return ImageCache.getInstance().getImage(ImageCache.LINKS);
			} 
			
			else if (obj instanceof CnALink) {
				CnALink link = (CnALink) obj;
				return CnAImageProvider.getImage(link.getDependency());
			}
			
			CnATreeElement el = (CnATreeElement) obj;
			return CnAImageProvider.getImage(el);
		}

		public String getText(Object obj) {
//			Logger.getLogger(this.getClass()).debug("getLabel "+obj);
			
			if (obj == null)
				return "<null>";
			
			Object cachedObject = cache.getCachedObject(obj);
			if (cachedObject == null) {
				cache.addObject(obj);
			} else {
				obj = cachedObject;
			}

			if (obj instanceof IBSIStrukturElement) {
				IBSIStrukturElement el = (IBSIStrukturElement) obj;
				CnATreeElement el2 = (CnATreeElement) obj;
				return el.getKuerzel() + " " + el2.getTitel();
			}

			else if (obj instanceof LinkKategorie)
				return ((LinkKategorie) obj).getTitle();

			else if (obj instanceof CnALink) {
				CnALink link = (CnALink) obj;
				return link.getTitle();
			}

			CnATreeElement el = (CnATreeElement) obj;
			return el.getTitel();
		}

}
