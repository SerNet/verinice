/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.swt.widgets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public abstract class Colors {
	public static final Color BLACK = new Color(Display.getCurrent(), 0,0,0);;
	//public static Color RED = new Color(Display.getCurrent(), 255,00,00);
	public static Color YELLOW = new Color(Display.getCurrent(), 250,250,120);
	public static Color GREY = new Color(Display.getDefault(), 240,240,240);
	//public static Color WHITE = new Color(Display.getDefault(), 255,255,255);

}
