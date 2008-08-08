package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;
import org.eclipse.swt.graphics.Image;

public interface IGefaehrdungsBaumElement {
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren();
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent();
	public Image getImage();
	public String getText();
}
