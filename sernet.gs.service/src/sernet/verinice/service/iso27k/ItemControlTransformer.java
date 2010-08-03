/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.iso27k;

import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Transforms {@link IItem} from CatalogView to ISO 27k {@link Control}s
 * or {@link ControlGroup}s
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author koderman[at]sernet[dot]de
 */
public class ItemControlTransformer {

	/**
	 * Transforms a catalog item to a IControl.
	 * IControl instances will be created by an IControlFactory
	 * 
	 * @param item an item from a control catalog
	 * @return an ISO 27k control
	 */
	public static <T extends IControl> T transformGeneric(IItem item, T control) {
		if(item.getName()!=null) {
			control.setTitel(item.getName().replaceAll("\\s", " "));
		}
		control.setDescription(item.getDescription());
		if (item.isMaturityLevelSupport()) {		
			control.setMaturity(item.getMaturity());
			control.setWeight1(item.getWeight1());
			control.setWeight2(item.getWeight2());
			control.setThreshold1(item.getThreshold1());
			control.setThreshold2(item.getThreshold2());
		}	
		return control;
	}
	
	/**
     * Transforms a catalog item to a control.
     * 
     * @param item an item from a control catalog
     * @return an ISO 27k control
     */
    public static Control transform(IItem item) {
        Control control = new Control();
        if(item.getName()!=null) {
            control.setTitel(item.getName().replaceAll("\\s", " "));
        }
        control.setDescription(item.getDescription());
        if (item.isMaturityLevelSupport()) {        
            control.setMaturity(item.getMaturity());
            control.setWeight1(item.getWeight1());
            control.setWeight2(item.getWeight2());
            control.setThreshold1(item.getThreshold1());
            control.setThreshold2(item.getThreshold2());
        }   
        return control;
    }

	/**
	 * Transforms a catalog item to a control group.
	 * 
	 * @param item an item from a control catalog
	 * @return an ISO 27k control group
	 */
	public static ControlGroup transformToGroup(IItem item) {
		ControlGroup controlGroup = new ControlGroup();
		if(item.getName()!=null) {
			// replace all whitespace with " "
			controlGroup.setTitel(item.getName().replaceAll("\\s", " "));
		}
		return controlGroup;
	}
	
	/**
	 * Truncates a text if it is longer than maxWidth.
	 * If text is truncated three dots ("...") are added in the end.
	 * 
	 * @param text
	 * @param maxWidth
	 * @return truncated text (if text is longer than maxWidth) and three dots ("...") 
	 */
	public static String truncate(String text, int maxWidth) {
	    String truncatedText = text;
	    if(text!=null && text.length()>maxWidth) {
	        truncatedText = new StringBuilder(text.substring(0, (maxWidth-3))).append("...").toString();
	    }
	    return truncatedText;
	}
	
	/**
	 * Teilt einen String in mehrere Zeilen, wenn er laenger als
	 * maxWidth ist.
	 * 
	 * Zeilenumbrueche werden nur nach Leerzeichen eingefuegt.
	 * Wenn der String keine Leerzeichen enthaelt, wird kein Umbruch
	 * eingefuegt, auch wenn er laenger als maxLength ist.
	 * 
	 * @param text 
     * @param maxWidth
	 * @return text splitted in more than one line if longer than maxWidth
	 */
	public static String addLineBreaks(String text, int maxWidth) {
		StringBuilder sbAll = new StringBuilder();
		if(text!=null && text.length()>maxWidth) {
			String[] wordArray = text.split("\\s");
			StringBuilder sbLine = new StringBuilder();
			for (String word : wordArray) {
				if((sbLine.length() + word.length())>maxWidth) {
					// new line
					if(sbAll.length()>0) {
						sbAll.append("\n");
					}
					sbAll.append(sbLine.toString());
					sbLine = new StringBuilder();				
				}
				if(sbLine.length()>0) {
					sbLine.append(" ");
				}
				sbLine.append(word);
			}
			if(sbLine.length()>0) {
				sbAll.append("\n").append(sbLine.toString());
			}
		} else {
			sbAll.append(text);
		}
		return sbAll.toString();
	}

    /**
     * @param item
     * @param iso27kGroup
     * @return
     */
    public static <T extends IISO27kGroup> T transformToGroup(IItem item, T iso27kGroup) {
        if(item.getName()!=null) {
            // replace all whitespace with " "
            iso27kGroup.setTitel(item.getName().replaceAll("\\s", " "));
        }
        return iso27kGroup;
    }

}
