/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter <mr@sernet.de>.
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
 *     Moritz Reiter <mr@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import sernet.verinice.iso27k.rcp.GreenbonePerspective;

/**
 * @author Moritz Reiter
 *
 */
public class GreenboneIntroAction extends ShowPerspectiveIntroAction {

    @Override
    public String getCheatSheetId() {
        return "sernet.verinice.greenbone.cheatsheets.overview";
    }

    @Override
    public String getPerspectiveId() {
        return GreenbonePerspective.ID;
    }
}