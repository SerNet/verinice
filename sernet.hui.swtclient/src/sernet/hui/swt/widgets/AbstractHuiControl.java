/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler.
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
 *     Urs Zeidler
 ******************************************************************************/
package sernet.hui.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Collect some basic behavior like resource managing.
 *
 */
public abstract class AbstractHuiControl implements IHuiControl {

	protected Composite composite;
	protected Label label;
	protected Font currentFont;

	public AbstractHuiControl(Composite composite) {
		super();
		this.composite = composite;
		this.composite.addDisposeListener(l->{
			if (currentFont != null && !currentFont.isDisposed()) {
				currentFont.dispose();
			}
		});
	}

	protected void refontLabel(boolean showValidationHint) {
		if (showValidationHint) {
			FontData fontData = label.getFont().getFontData()[0];
			if (currentFont != null && !currentFont.isDisposed()) {
				currentFont.dispose();
			}
			Font newFont = new Font(composite.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
			label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));
			label.setFont(newFont);
			currentFont = newFont;
		} else  if (currentFont != null && !currentFont.isDisposed()) {
			FontData fontData = label.getFont().getFontData()[0];
	    	currentFont.dispose();
	    	Font newFont = new Font(composite.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.NONE));
	        label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
	        label.setFont(newFont);
	        currentFont = newFont;
	    }
	}

}
