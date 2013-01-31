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
	int getVertraulichkeit();
	int getVerfuegbarkeit();
	int getIntegritaet();
	
	void setVertraulichkeit(int i);
	void setIntegritaet(int i);
	void setVerfuegbarkeit(int i);
	
	String getVertraulichkeitDescription();
	String getIntegritaetDescription();
	String getVerfuegbarkeitDescription();
	
	boolean isCalculatedConfidentiality();
	boolean isCalculatedIntegrity();
	boolean isCalculatedAvailability();

	void setVertraulichkeitDescription(String text);
	void setIntegritaetDescription(String text);
	void setVerfuegbarkeitDescription(String text);
	
	void updateVertraulichkeit(CascadingTransaction ta);
	void updateIntegritaet(CascadingTransaction ta);
	void updateVerfuegbarkeit(CascadingTransaction ta);
}
