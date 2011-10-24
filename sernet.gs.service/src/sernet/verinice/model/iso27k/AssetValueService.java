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
package sernet.verinice.model.iso27k;

import java.util.regex.Pattern;

import sernet.hui.common.connect.Property;

public abstract class AssetValueService {

	public static final String CONFIDENTIALITY 	       = "_value_confidentiality"; //$NON-NLS-1$
	public static final String AVAILABILITY 		   = "_value_availability"; //$NON-NLS-1$
	public static final String INTEGRITY 		       = "_value_integrity"; //$NON-NLS-1$
	public static final String EXPLANATION	           = "_value_comment"; //$NON-NLS-1$

	public static final String METHOD_CONFIDENTIALITY	               = "_value_method_confidentiality"; //$NON-NLS-1$
	public static final String METHOD_AVAILABILITY	                   = "_value_method_availability"; //$NON-NLS-1$
	public static final String METHOD_INTEGRITY	                       = "_value_method_integrity"; //$NON-NLS-1$
	
	private static Pattern pat_vertraulichkeit = Pattern.compile(".*" + CONFIDENTIALITY + "$"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Pattern pat_verfuegbarkeit  = Pattern.compile(".*" + AVAILABILITY + "$"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Pattern pat_integritaet     = Pattern.compile(".*" + INTEGRITY + "$"); //$NON-NLS-1$ //$NON-NLS-2$


	public static final int VALUE_UNDEF 		= Integer.MIN_VALUE;
    public static final int METHOD_AUTO = 1;
	
	public static boolean isVerfuegbarkeit(Property prop) {
		return pat_verfuegbarkeit.matcher(prop.getPropertyTypeID()).matches();
	}
	
	public static boolean isVertraulichkeit(Property prop) {
		return pat_vertraulichkeit.matcher(prop.getPropertyTypeID()).matches();
	}
	
	public static boolean isIntegritaet(Property prop) {
		return pat_integritaet.matcher(prop.getPropertyTypeID()).matches();
	}

	
}
