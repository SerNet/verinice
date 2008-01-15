package sernet.hui.common.multiselectionlist;



/**
 * an option to display in the multi selection list
 * @author prack
 */
public interface IMLPropertyOption {

	/**
	 * the display name of the option (i.e. "Brown")
	 * @return
	 */
	String getName();

	/**
	 * the internal id of the option (i.e. "colour3")
	 * @return
	 */
	String getId();
	
	/**
	 * Optional Context Menu to be displayed on right click.
	 * 
	 * @return swt.Listener interface that is called on MenuDetect Events.
	 */
	IContextMenuListener getContextMenuListener();

}
