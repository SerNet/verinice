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

import java.util.regex.Pattern;

import sernet.hui.common.connect.Property;

public abstract class Schutzbedarf {

	public static final String VERTRAULICHKEIT 	= "_vertraulichkeit"; //$NON-NLS-1$
	public static final String VERFUEGBARKEIT 		= "_verfuegbarkeit"; //$NON-NLS-1$
	public static final String INTEGRITAET 		= "_integritaet"; //$NON-NLS-1$
	
	private static Pattern patVertraulichkeit = Pattern.compile(".*" + VERTRAULICHKEIT + "$"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Pattern patVerfuegbarkeit = Pattern.compile(".*" + VERFUEGBARKEIT + "$"); //$NON-NLS-1$ //$NON-NLS-2$
	private static Pattern patIntegritaet = Pattern.compile(".*" + INTEGRITAET + "$"); //$NON-NLS-1$ //$NON-NLS-2$

	public static final String VERTRAULICHKEIT_BEGRUENDUNG	= "_vertraulichkeit_begruendung"; //$NON-NLS-1$
	public static final String VERFUEGBARKEIT_BEGRUENDUNG	= "_verfuegbarkeit_begruendung"; //$NON-NLS-1$
	public static final String INTEGRITAET_BEGRUENDUNG		= "_integritaet_begruendung"; //$NON-NLS-1$

	public static final String SUFFIX_NONE 		= ""; //$NON-NLS-1$
	public static final String SUFFIX_NORMAL 		= "_normal"; //$NON-NLS-1$
	public static final String SUFFIX_HOCH 		= "_hoch"; //$NON-NLS-1$
	public static final String SUFFIX_SEHRHOCH 	= "_sehrhoch"; //$NON-NLS-1$
	
	
	public static final int UNDEF 		= 0;
	public static final int NORMAL 	= 1;
	public static final int HOCH 		= 2; 
	public static final int SEHRHOCH	= 3;
	
	public static final String MAXIMUM = "Maximumprinzip"; //$NON-NLS-1$

	public static final String ERGAENZENDEANALYSE = "_ergaenzendeanalyse"; //$NON-NLS-1$
	private static final String ERGAENZENDEANALYSE_NOETIG = "_modell"; //$NON-NLS-1$
	
	
	public static int toInt(String option) {
		if (option.indexOf(SUFFIX_SEHRHOCH) > -1){
			return SEHRHOCH;
		}
		if (option.indexOf(SUFFIX_HOCH) > -1){
			return HOCH;
		}
		return NORMAL;
	}
	
	public static boolean isMaximumPrinzip(String description) {
		return description.indexOf(Schutzbedarf.MAXIMUM) != -1;
	}

	public static boolean isVerfuegbarkeit(Property prop) {
		return patVerfuegbarkeit.matcher(prop.getPropertyTypeID()).matches();
	}
	
	public static boolean isVertraulichkeit(Property prop) {
		return patVertraulichkeit.matcher(prop.getPropertyTypeID()).matches();
	}
	
	public static boolean isIntegritaet(Property prop) {
		return patIntegritaet.matcher(prop.getPropertyTypeID()).matches();
	}

	public static String toOption(String typeId, String schutzbedarf, int level) {
		StringBuffer buf = new StringBuffer();
		buf.append(typeId);
		buf.append(schutzbedarf);
		buf.append(getLevel(level));
		return buf.toString();
	}

	private static String getLevel(int i) {
		switch(i) {
		case NORMAL:
			return SUFFIX_NORMAL;
		case HOCH:
			return SUFFIX_HOCH;
		case SEHRHOCH:
			return SUFFIX_SEHRHOCH;
		}
		return SUFFIX_NONE;
	}

	
	
	public static boolean isVerfuegbarkeitBegruendung(Property prop) {
		return prop.getPropertyTypeID().indexOf(VERFUEGBARKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isVertraulichkeitBegruendung(Property prop) {
		return prop.getPropertyTypeID().indexOf(VERTRAULICHKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isIntegritaetBegruendung(Property prop) {
		return prop.getPropertyTypeID().indexOf(INTEGRITAET_BEGRUENDUNG) != -1;
	}
	
	public static boolean isVerfuegbarkeitBegruendung(String prop) {
		return prop.indexOf(VERFUEGBARKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isVertraulichkeitBegruendung(String prop) {
		return prop.indexOf(VERTRAULICHKEIT_BEGRUENDUNG) != -1;
	}
	
	public static boolean isIntegritaetBegruendung(String prop) {
		return prop.indexOf(INTEGRITAET_BEGRUENDUNG) != -1;
	}

	public static boolean isMgmtReviewNeeded(String propertyValue) {
		return propertyValue.indexOf(ERGAENZENDEANALYSE_NOETIG)!=-1;
	}
}
