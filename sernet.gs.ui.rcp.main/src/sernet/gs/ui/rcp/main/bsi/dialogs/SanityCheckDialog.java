/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class SanityCheckDialog {

	public static boolean checkLayer(Shell shell, int schicht, int targetSchicht) {
		if (schicht != targetSchicht) {
			if (MessageDialog.openQuestion(
					shell,
					"Sanity Check",
					"Sie wollen einen Baustein der Schicht " + schicht
					+ " einem Zielobjekt der Schicht " + targetSchicht
					+ " zuordnen.\n\nWissen Sie, was Sie tun?")) {
				return true;
			}
			return false;
		}
		return true;
	}

}
