/*******************************************************************************
 * SyncNamespaceUtil.java
 *
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * 14.08.2009
 * 
 * @author Andreas Becker
 ******************************************************************************/
package sernet.gs.ui.rcp.main.sync;

import org.jaxen.SimpleNamespaceContext;
import org.jdom.Namespace;

public class SyncNamespaceUtil
{
	public static final Namespace SYNC_NS = Namespace.getNamespace( "sync", "http://www.sernet.de/sync/sync" );
	public static final Namespace DATA_NS = Namespace.getNamespace( "data", "http://www.sernet.de/sync/data" );
	public static final Namespace MAPPING_NS = Namespace.getNamespace( "map", "http://www.sernet.de/sync/mapping" );
	
	public static SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
	
	static
	{
		nsContext.addNamespace( "sync", "http://www.sernet.de/sync/sync" );
		nsContext.addNamespace( "data", "http://www.sernet.de/sync/data" );
		nsContext.addNamespace( "map", "http://www.sernet.de/sync/mapping" );
	}

}
