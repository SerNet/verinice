/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package sernet.gs.service;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * A Comparator which deals with alphabet characters 'naturally', but 
 * deals with numerics numerically. Leading 0's are ignored numerically,
 * but do come into play if the number is equal. Thus aaa119yyyy comes before 
 * aaa0119xxxx regardless of x or y.
 *
 * The comparison should be very performant as it only ever deals with 
 * issues at a character level and never tries to consider the 
 * numerics as numbers.
 * 
 * @see Test class: sernet.verinice.service.test.NumericStringComparatorTest
 * @author bayard@generationjava.com
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class AbstractNumericStringComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = -9196544676244562514L;
    
    private transient Logger log = Logger.getLogger(AbstractNumericStringComparator.class);
    private Logger getLog(){
        if (log == null)
            log = Logger.getLogger(NumericStringComparator.class);
        return log;
    }
    
    // Collator for basic string comparison
    private transient Collator collator = Collator.getInstance(Locale.getDefault()); 
    
    /**
     * Returns a collator for the default locale for this instance
     * of the Java Virtual Machine.
     * 
     * For a German / Germany locale the sorting is done according to
     * DIN 5007 Var.1. 
     * See: https://de.wikipedia.org/wiki/Alphabetische_Sortierung#Deutschland
     * for a definition of DIN 5007 Var.1
     * 
     * @see Test class: sernet.verinice.service.test.NumericStringComparatorTest
     * @return A collator
     */
    private Collator getCollator(){
        if (collator == null)
            collator = Collator.getInstance(Locale.getDefault());
        return collator;
    }
	
	public AbstractNumericStringComparator() {
	}
	
	public abstract String convertToString(T o);

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(T o1, T o2) {
		if (o1 == null) {
			return 1;
		} else if (o2 == null) {
			return -1;
		}
		String s1 = convertToString(o1);
        String s2 = convertToString(o2);
        
		return compareString(s1, s2);
	}

    private int compareString(String string1, String string2) {
        string1 = string1.toLowerCase();
        string2 = string2.toLowerCase();
        
		// find the first digit.
		int idx1 = getFirstDigitIndex(string1);
		int idx2 = getFirstDigitIndex(string2);

		// no digits found, compare string the ordinary way 
		if ((idx1 == -1) || (idx2 == -1)
				|| (!string1.substring(0, idx1).equals(string2.substring(0, idx2)))) {
			return compareStringOrdinary(string1, string2);
		}

		// find the last digit
		int edx1 = getLastDigitIndex(string1, idx1);
		int edx2 = getLastDigitIndex(string2, idx2);

		String sub1 = null;
		String sub2 = null;

		if (edx1 == -1) {
			sub1 = string1.substring(idx1);
		} else {
			sub1 = string1.substring(idx1, edx1);
		}

		if (edx2 == -1) {
			sub2 = string2.substring(idx2);
		} else {
			sub2 = string2.substring(idx2, edx2);
		}

		// deal with zeros at start of each number
		int zero1 = countZeroes(sub1);
		int zero2 = countZeroes(sub2);

		sub1 = sub1.substring(zero1);
		sub2 = sub2.substring(zero2);

		// if equal, then recurse with the rest of the string
		// need to deal with zeroes so that 00119 appears after 119
		if (sub1.equals(sub2)) {
			int ret = 0;
			if (zero1 > zero2) {
				ret = 1;
			} else if (zero1 < zero2) {
				ret = -1;
			}
			
			if (edx1 != -1) {
				if(edx2 > -1) {
					try {
						int comp = compareString(string1.substring(edx1), string2.substring(edx2));
						if (comp != 0) {
							ret = comp;
						}
					} catch (Exception e) {
						getLog().error("Fehler bei Stringvergleich: " + string1 + " : " + string2,e);
					}
				} else {
					ret = 1;
				}
			} else if (edx2 != -1) {
				ret = -1;
			}
			return ret;
		} else {
			// if a numerical string is smaller in length than another
			// then it must be less.
			if (sub1.length() != sub2.length()) {
				return (sub1.length() < sub2.length()) ? -1 : 1;
			}
		}

		// now we get to do the string based numerical thing :)
		// going to assume that the individual character for the
		// number has the right order. ie) '9' > '0'
		// possibly bad in i18n.
		char[] chr1 = sub1.toCharArray();
		char[] chr2 = sub2.toCharArray();

		int sz = chr1.length;
		for (int i = 0; i < sz; i++) {
			// this should give better speed
			if (chr1[i] != chr2[i]) {
				return (chr1[i] < chr2[i]) ? -1 : 1;
			}
		}

		return 0;
    }

    /**
     * Compares string1 to string2 according to the
     * collation rules for the Collator. See the Collator
     * class description for more details.
     * 
     * @param string1
     * @param string2
     * @return Returns an integer less than, equal to or greater than zero 
     * depending on whether string1 is less than, equal to or greater 
     * than string2.
     */
    protected int compareStringOrdinary(String string1, String string2) {
        return getCollator().compare(string1, string2);
    }

	private static int getFirstDigitIndex(String str) {
		return getFirstDigitIndex(str, 0);
	}

	private static int getFirstDigitIndex(String str, int start) {
        for (int i = start; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return i;
            }
        }
        return -1;
	}

	private static int getLastDigitIndex(String str, int start) {
		return getLastDigitIndex(str.toCharArray(), start);
	}

	private static int getLastDigitIndex(char[] chrs, int start) {
		int sz = chrs.length;

		for (int i = start; i < sz; i++) {
			if (!Character.isDigit(chrs[i])) {
				return i;
			}
		}

		return -1;
	}

	private static int countZeroes(String str) {
		int count = 0;

		// assuming str is small...
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '0') {
				count++;
			} else {
				break;
			}
		}

		return count;
	}

}
