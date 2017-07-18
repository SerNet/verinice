/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh[at]sernet[dot]de>.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.gsimport;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.gs.ui.rcp.gsimport.messages"; //$NON-NLS-1$
    public static String Import_Task_1;
    public static String GSImportException_1;
    public static String GSImportMappingView_1;
    public static String GSImportMappingView_2;
    public static String GstoolTypeValidator_0;
    public static String GstoolTypeValidator_1;
    public static String GSImportMappingView_newEntry;
    public static String UnknownTypeDialog_0;
    public static String UnknownTypeDialog_1;
    public static String UnknownTypeDialog_2;
    public static String UnknownTypeDialog_3;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
