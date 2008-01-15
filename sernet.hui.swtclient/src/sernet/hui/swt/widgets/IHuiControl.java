package sernet.hui.swt.widgets;

import org.eclipse.swt.widgets.Control;

/**
 * Interface for all custom widgets that are used
 * to edit properties.
 * 
 * @author koderman
 *
 */
public interface IHuiControl {
	public void create();
	public void setFocus();
	public boolean validate();
	public Control getControl();
	
	/**
	 * Compare current contents of field with propert and update if necessary.
	 *
	 */
	public void update();
}
