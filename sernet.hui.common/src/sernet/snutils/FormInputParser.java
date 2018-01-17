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
 * Class to parse different user text inputs like dates, currencies or simple
 * number formats.
 * 
 * @author prack
 * @version $Id: FormInputParser.java,v 1.1 2005/12/27 16:41:18 aprack Exp $
 */
public abstract class FormInputParser {

    private static final String DEFAULT_DATE_PATTERN = "EEE, dd.MM.yyyy"; //$NON-NLS-1$

    /**
     * @deprecated This instance is not thread-safe and should not be used
     *             outside of this class. It is being kept for API compatibility
     *             reasons only.
     */
    @Deprecated
    public static final SimpleDateFormat DATE_FORMAT_DEFAULT = createDateFormat(DEFAULT_DATE_PATTERN);
    private static final boolean GROUPING = true;

    public static float stringToCurrency(String fieldName, String s) throws DBException {
        try {
            return createPriceFormat().parse(s).floatValue();
        } catch (ParseException e) {
            try {
                return createNumberFormat().parse(s).floatValue();
            } catch (ParseException e1) {
                throw new DBException("Wrong format for " + fieldName + "."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public static String currencyToString(float value) {
        return createPriceFormat().format(value);
    }

    public static float stringToFloat(String fieldName, String s) throws DBException {
        try {

            return createNumberFormat().parse(s).floatValue();
        } catch (ParseException e) {
            throw new DBException("Wrong format for " + fieldName + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static short stringToShort(String fieldName, String s) throws DBException {
        try {
            return createNumberFormat().parse(s).shortValue();
        } catch (ParseException e) {
            throw new DBException("Wrong format for " + fieldName + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static String dateToString(Date date) throws AssertException {
        try {
            if (date == null) {
                return ""; //$NON-NLS-1$
            }
            return createDateFormat(DEFAULT_DATE_PATTERN).format(date);
        } catch (IllegalArgumentException e) {
            throw new AssertException("Wrong / missing date: " + date.toString()); //$NON-NLS-1$
        }
    }

    public static Date stringToDate(String string) throws AssertException {
        if (string == null || string.length() < 5) {
            throw new AssertException("Wrong / missing date: " + string);
        }
        String datePattern;
        if (string.charAt(2) == '.') {
            datePattern = "dd.MM.yyyy"; //$NON-NLS-1$
        } else if (string.charAt(4) == '-') {
            datePattern = "yyyy-MM-dd"; //$NON-NLS-1$
        } else {
            datePattern = DEFAULT_DATE_PATTERN;
        }
        try {
            return new Date(createDateFormat(datePattern).parse(string).getTime());
        } catch (ParseException e) {
            throw new AssertException("Wrong / missing date: " + string); //$NON-NLS-1$
        }
    }

    /**
     * @param f
     * @return
     */
    public static String floatToString(float f) {
        return createNumberFormat().format(f);
    }

    private static SimpleDateFormat createDateFormat(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern); // $NON-NLS-1$
        dateFormat.setLenient(true);
        return dateFormat;
    }

    private static NumberFormat createNumberFormat() {
        NumberFormat numFmt = NumberFormat.getNumberInstance(Locale.GERMANY);
        numFmt.setGroupingUsed(GROUPING);
        return numFmt;
    }

    private static NumberFormat createPriceFormat() {
        NumberFormat priceFmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        priceFmt.setGroupingUsed(GROUPING);
        return priceFmt;
    }
}