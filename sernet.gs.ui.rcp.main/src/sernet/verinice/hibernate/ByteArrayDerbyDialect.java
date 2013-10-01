/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.hibernate;

import java.sql.Types;

import org.hibernate.dialect.DerbyDialect;

/**
 * Special hibernate dialect for Derby to store byte-arrays mapped as binary types
 * in BLOB columns.
 * 
 * Hibernates buils in DerbyDialect uses varchar for binary types which in limited to
 * 32kb in Derby.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ByteArrayDerbyDialect extends DerbyDialect {
	public ByteArrayDerbyDialect() {
		super();
		// override mapping to "varchar($l) for bit data" from DerbyDialect(DB2Dialect)
		registerColumnType( Types.VARBINARY, "blob($l)" );
	}

}
