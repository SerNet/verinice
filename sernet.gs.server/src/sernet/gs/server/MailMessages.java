/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server;

import org.eclipse.osgi.util.NLS;

final class MailMessages extends NLS {
	private static final String BUNDLE_NAME = "sernet.gs.server.mailmessages"; //$NON-NLS-1$
	public static String MailJob_1;
	public static String MailJob_2;
	public static String MailJob_3;
	public static String MailJob_4;
	public static String MailJob_5;
	public static String MailJob_6;
	public static String MailJob_7;
	public static String MailJob_8;
	public static String MailJob_9;
	public static String MailJob_10;
	public static String MailJob_11;
    public static String MailJob_14;
    public static String MailJob_16;
    public static String MailJob_18;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MailMessages.class);
	}

	private MailMessages() {
	}
}
