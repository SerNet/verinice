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
package sernet.gs.scraper;

import java.util.regex.Pattern;

public interface IGSPatterns {

	public abstract String getGefName();

	public abstract String getBausteinPattern();

	public abstract String getMassnahmePattern();

	public abstract String getGefaehrdungPattern();

	public abstract String getTitlePattern();

	public abstract Pattern getStandPat();

	public abstract Pattern getBaustPat();

	public abstract Pattern getSchichtPat();

	public abstract String getMassnahmeVerantwortlichePattern();
	
	public abstract String getEncoding();


}
