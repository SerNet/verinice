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
package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;


public interface ISchutzbedarfProvider {
	public int getVertraulichkeit();
	public int getVerfuegbarkeit();
	public int getIntegritaet();
	
	public void setVertraulichkeit(int i, CascadingTransaction ta);
	public void setIntegritaet(int i, CascadingTransaction ta);
	public void setVerfuegbarkeit(int i, CascadingTransaction ta);
	
	public String getVertraulichkeitDescription();
	public String getIntegritaetDescription();
	public String getVerfuegbarkeitDescription();

	public void setVertraulichkeitDescription(String text, CascadingTransaction ta);
	public void setIntegritaetDescription(String text, CascadingTransaction ta);
	public void setVerfuegbarkeitDescription(String text, CascadingTransaction ta);
	
	public void updateVertraulichkeit(CascadingTransaction ta);
	public void updateIntegritaet(CascadingTransaction ta);
	public void updateVerfuegbarkeit(CascadingTransaction ta);
}
