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
package sernet.verinice.iso27k.service;

import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.rcp.CatalogView;

/**
 * Transforms {@link IItem} from {@link CatalogView} to ISO 27k {@link Control}s
 * or {@link ControlGroup}s
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ItemControlTransformer {

	/**
	 * Transforms a catalog item to a control.
	 * 
	 * @param item an item from a control catalog
	 * @return an ISO 27k control
	 */
	public static Control transform(IItem item) {
		Control control = new Control();
		control.setAbbreviation(item.getNumberString());
		if(item.getName()!=null) {
			control.setTitel(item.getName().replaceAll("\\s", " "));
		}
		control.setDescription(item.getDescription());
		
		if (item.isMaturityLevelSupport()) {
			
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
		StringBuilder sb = new StringBuilder();
		if(item.getNumberString()!=null) {
			sb.append(item.getNumberString());
		}
		if(item.getName()!=null) {
			if(sb.length()>0) {
				sb.append(" ");
			}
			sb.append(item.getName().replaceAll("\\s", " "));
			controlGroup.setTitel(sb.toString());
		}
		return controlGroup;
	}
	
	/**
	 * @param name2
	 * @return
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

}
