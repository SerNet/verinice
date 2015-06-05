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
    
    private static final int MAX_RGB = 255;
    private static final int MIN_RGB = 0;
    private static final int GREY_RGB = 240;
    
    private static final Display DISPLAY = (Display.getCurrent() != null)
            ? Display.getCurrent() : Display.getDefault();
    
	public static final Color BLACK = new Color(DISPLAY, MIN_RGB,MIN_RGB,MIN_RGB);
	public static final Color RED = new Color(DISPLAY, MAX_RGB,MIN_RGB,MIN_RGB);
	public static final Color YELLOW = new Color(DISPLAY, 250,250,120);
	public static final Color GREY = new Color(DISPLAY, GREY_RGB,GREY_RGB,GREY_RGB);
	public static final Color WHITE = new Color(DISPLAY, MAX_RGB,MAX_RGB,MAX_RGB);

}
