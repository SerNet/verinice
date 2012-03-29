/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.actions;

import sernet.gs.ui.rcp.main.Perspective;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ShowGrundschutzPerspectiveIntroAction extends ShowPerspectiveIntroAction {

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.ShowPerspectiveIntroAction#getCheatSheetId()
     */
    @Override
    public String getCheatSheetId() {
        return "sernet.gs.ui.rcp.main.cheatsheet1";
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.ShowPerspectiveIntroAction#getPerspectiveId()
     */
    @Override
    public String getPerspectiveId() {
        return Perspective.ID;
    }

}


