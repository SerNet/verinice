package sernet.hui.common.multiselectionlist;


import java.util.ArrayList;

/**
 * a list of options for a given name (property type).
 * 
 * @author prack
 */
public interface IMLPropertyType {

	/**
	 * Returns all options for this property type.
	 * i.e. black, brown, beige
	 * 
	 * @return
	 */
	ArrayList getOptions();
	
	/**
	 * Return a name for this list of options (i.e. "colour")
	 * 
	 * @return
	 */
	String getName();
	
	String getId();
	
	boolean isMultiselect();

}
