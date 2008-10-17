package sernet.gs.ui.rcp.main.common.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.Query;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.views.DocumentLinkRoot;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.swt.widgets.URL.URLUtil;

public class HibernateDocumentLinkDAO implements IDocumentLinkDAO {

	public DocumentLinkRoot findEntries(Set<String> allIDs) {
		DocumentLinkRoot root = new DocumentLinkRoot();
		List<CnATreeElement> elements = CnAElementFactory.getCurrentModel()
				.getAllElements();
		for (CnATreeElement element : elements) {
			Entity entity = element.getEntity();
			if (entity == null)
				continue;

			for (String id : allIDs) {
				PropertyList properties = entity.getProperties(id);
				if (properties != null && properties.getProperties() != null
						&& properties.getProperties().size() > 0) {

					String url = properties.getProperty(0).getPropertyValue();
					DocumentLink documentLink = new DocumentLink(URLUtil
							.getName(url), URLUtil.getHref(url));
					DocumentReference reference = new DocumentReference(element);
					documentLink.addChild(reference);
					root.addChild(documentLink);
				}
			}
		}
		return root;
	}

}
