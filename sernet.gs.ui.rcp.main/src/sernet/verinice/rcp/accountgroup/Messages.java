/**
 * Copyright 2015 Moritz Reiter.
 * 
 * This file is part of verinice.
 * 
 * verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with verinice. If not, see <http://www.gnu.org/licenses/>.
 */

package sernet.verinice.rcp.accountgroup;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("restriction")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "sernet.verinice.rcp.accountgroup.messages";

    public static String loadDataJoblabel; // NOSONAR

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
