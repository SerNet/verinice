package sernet.gs.ui.rcp.main.common.model;

import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.views.DocumentLinkRoot;

public interface IDocumentLinkDAO {

	DocumentLinkRoot findEntries(Set<String> allIDs);

	

}
