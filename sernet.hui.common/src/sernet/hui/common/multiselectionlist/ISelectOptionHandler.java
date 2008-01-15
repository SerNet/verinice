package sernet.hui.common.multiselectionlist;



/**
 * Handle selection events in multi selection list.
 * @author prack
 */
public interface ISelectOptionHandler {

	/**
	 * Option selected.
	 * 
	 * @param type optionlist that was selected.
	 * @param option the selected option
	 */
	void select(IMLPropertyType type, IMLPropertyOption option);

	/**
	 * Option deselected.
	 * 
	 * @param type option list that was unselected.
	 * @param option the unselected option
	 */
	void unselect(IMLPropertyType type, IMLPropertyOption option) ;

}
