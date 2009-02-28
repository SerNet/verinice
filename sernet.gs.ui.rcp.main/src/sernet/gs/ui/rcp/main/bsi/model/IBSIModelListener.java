package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public interface IBSIModelListener {
	
	public static final String SOURCE_BULK_EDIT 	= "source bulk edit";
	public static final String SOURCE_KONSOLIDATOR 	= "source konsolidator";
	public static final String SOURCE_EDITOR 		= "source editor";

	void childAdded(CnATreeElement category, CnATreeElement child);

	void childRemoved(CnATreeElement category, CnATreeElement child);

	void childChanged(CnATreeElement category, CnATreeElement child);
	
	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	void modelRefresh();

	/**
	 * Request complete refresh of model, it depends on the listener how this method will
	 * be implemented, i.e. what parts of the model really need to be refreshed (only visible objects etc.).
	 * 
	 * @param source the cause or source for this model refresh. Should be the object causing the model change
	 * or one of the predefined sources given in {@link IBSIModelListener}. 
	 */
	void modelRefresh(Object source);
	
	void linkChanged(CnALink link);
	
	void linkRemoved(CnALink link);
	
	void linkAdded(CnALink link);
	
	
	
	/**
	 * New element was added to the database (by another client), hint to view that it should reload from database.
	 * @param child
	 */
	void databaseChildAdded(CnATreeElement child);
	
	/**
	 * Element was deleted in database.
	 * Hint to view that it should reload from database.
	 * 
	 * @param child
	 */
	void databaseChildRemoved(CnATreeElement child);
	
	/**
	 * Element was changed in the database either by another client or by a cascading action performed
	 * on the server.
	 * 
	 * Hint to view that it should reload from the database.
	 * 
	 * @param child
	 */
	void databaseChildChanged(CnATreeElement child);
	
	public void modelReload(BSIModel newModel);
	

}
