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
package sernet.verinice.model.bsi;

import sernet.verinice.model.common.CascadingTransaction;

public interface ISchutzbedarfProvider {
	public int getVertraulichkeit();
	public int getVerfuegbarkeit();
	public int getIntegritaet();
	
	public void setVertraulichkeit(int i);
	public void setIntegritaet(int i);
	public void setVerfuegbarkeit(int i);
	
	public String getVertraulichkeitDescription();
	public String getIntegritaetDescription();
	public String getVerfuegbarkeitDescription();
	
	public boolean isCalculatedConfidentiality();
	public boolean isCalculatedIntegrity();
	public boolean isCalculatedAvailability();

	public void setVertraulichkeitDescription(String text);
	public void setIntegritaetDescription(String text);
	public void setVerfuegbarkeitDescription(String text);
	
	public void updateVertraulichkeit(CascadingTransaction ta);
	public void updateIntegritaet(CascadingTransaction ta);
	public void updateVerfuegbarkeit(CascadingTransaction ta);
}
