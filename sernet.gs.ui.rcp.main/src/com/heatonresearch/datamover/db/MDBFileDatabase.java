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
package com.heatonresearch.datamover.db;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class MDBFileDatabase extends Database {

	/**
	 * MDB access using the jdbc odbc does not support metadata, so we return
	 * the list of tables we need. 
	 */
	public Collection<String> listTables() throws DatabaseException {
		ArrayList<String> list = new ArrayList<String>();
		list.add("MB_BAUST");
		list.add("MB_DRINGLICHKEIT");
		list.add("MB_DRINGLICHKEIT_TXT");
		list.add("MB_MASSN");
		list.add("MB_ROLLE_TXT");
		list.add("MB_SCHICHT");
		list.add("MB_STATUS");
		list.add("M_BSTN_STATUS");
		list.add("MB_ZEITEINHEITEN_TXT");
		list.add("MB_ZIELOBJ_SUBTYP");
		list.add("MB_ZIELOBJ_SUBTYP_TXT");
		list.add("MB_ZIELOBJ_TYP");
		list.add("MB_ZIELOBJ_TYP_TXT");
		list.add("M_GSIEGEL");
		list.add("M_METASTATUS");
		list.add("M_METATYP");
		list.add("MOD_ZOBJ_BST");
		list.add("MOD_ZOBJ_BST_MASS");
		list.add("MOD_ZOBJ_BST_MASS_MITARB");
		list.add("MOD_ZOBJ_BST_MITARB");
		list.add("M_SCHUTZBEDARFKATEG_TXT");
		list.add("MS_CM_STATE");
		list.add("M_UMSETZ_STAT");
		list.add("M_UMSETZ_STAT_TXT");
		list.add("M_YESNO");
		list.add("NMB_NOTIZ");
		list.add("N_ZIELOBJEKT");
		list.add("N_ZIELOBJEKT_ROLLEN");
		list.add("N_ZIELOBJ_ZIELOBJ");
		list.add("N_ZOB_SB");
		list.add("SYS_IMPORT");
		return list;

	}

	public String processType(String type, int size) {
//		 if( type.equalsIgnoreCase("varchar")
//				|| type.equalsIgnoreCase("longchar")
//				|| type.equalsIgnoreCase("guid")
//				 )
//		      type = "VARCHAR (10000)";
//		 
//		 else if( type.equalsIgnoreCase("bit") 
//				 || type.equalsIgnoreCase("byte"))
//			 type = "SMALLINT";
//
//		 else if( type.equalsIgnoreCase("datetime") )
//			 type = "TIMESTAMP";

		return type.trim();
	}
}
