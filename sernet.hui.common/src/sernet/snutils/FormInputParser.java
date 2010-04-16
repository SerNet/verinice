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
package sernet.snutils;

import java.sql.Date;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Class to parse different user text inputs like dates, curencies or
 * simple number formats.
 * 
 * @author prack
 * @version $Id: FormInputParser.java,v 1.1 2005/12/27 16:41:18 aprack Exp $
 */
public abstract class FormInputParser {
	private static final boolean GROUPING = true;
	private static NumberFormat numFmt;
	private static NumberFormat priceFmt; 
	
	static {
		numFmt = NumberFormat.getNumberInstance(Locale.GERMANY);
		numFmt.setGroupingUsed(GROUPING);
		priceFmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);
		priceFmt.setGroupingUsed(GROUPING);
	}
	
	public static float stringToCurrency(String fieldName, String s) throws DBException {
		try {
			priceFmt.setGroupingUsed(GROUPING);
			return priceFmt.parse(s).floatValue();
		}
		catch (ParseException e) {
			numFmt.setGroupingUsed(GROUPING);
			try {
				return numFmt.parse(s).floatValue();
			}
			catch (ParseException e1) {
				throw new DBException("Falsches Format f�r " + fieldName + "."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	public static String currencyToString(float value) {
		priceFmt.setGroupingUsed(GROUPING);
		return priceFmt.format(value);
	}
	
	public static float stringToFloat(String fieldName, String s) throws DBException {
		try {
			numFmt.setGroupingUsed(GROUPING);
			return numFmt.parse(s).floatValue();
		}
		catch (ParseException e) {
			throw new DBException("Falsches Format f�r " + fieldName + "."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public static short stringToShort(String fieldName, String s) throws DBException {
		try {
			numFmt.setGroupingUsed(GROUPING);
			return numFmt.parse(s).shortValue();
		}
		catch (ParseException e) {
			throw new DBException("Falsches Format f�r " + fieldName + "."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public static String dateToString(Date date) throws AssertException {
		try {
			if (date == null)
				return ""; //$NON-NLS-1$
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy"); //$NON-NLS-1$
			return dateFormat.format(date);
		}
		catch (IllegalArgumentException e) {
			throw new AssertException("Falsches / fehlendes Datum: " + date.toString()); //$NON-NLS-1$
		}
	}
	
	public static Date stringToDate(String string) throws AssertException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy"); //$NON-NLS-1$
			dateFormat.setLenient(true);
			return new Date(dateFormat.parse(string).getTime());
		}
		catch (IllegalArgumentException e) {
			throw new AssertException("Falsches / fehlendes Datum: " + string); //$NON-NLS-1$
		}
		catch (ParseException e) {
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
			dateFormat2.setLenient(true);
			try {
				return new Date(dateFormat2.parse(string).getTime());
			}
			catch (ParseException e1) {
				SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				dateFormat3.setLenient(true);
				try {
					return new Date(dateFormat3.parse(string).getTime());
				}
				catch (ParseException e2) {
					throw new AssertException("Falsches / fehlendes Datum: " + string); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * @param f
	 * @return
	 */
	public static String floatToString(float f) {
		return numFmt.format(f);
	}

}
