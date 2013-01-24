/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.commands;

/**
 * used by the comprehensive samt report at the end of the report
 * when not used, executing the report twice (or more) in a row will result in a outOfMemoryException
 * with the GC overhead limit exceeded cause.
 */

import net.sf.ehcache.CacheManager;
import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class ClearCacheCommand extends GenericCommand {

	public ClearCacheCommand(){
	}
	
	@Override
	public void execute() {
		CacheManager.getInstance().clearAll();
		CacheManager.getInstance().removalAll();
	}
}
