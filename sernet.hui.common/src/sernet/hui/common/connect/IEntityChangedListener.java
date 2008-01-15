/*
 * This file is part of the SerNet Customer Database Application (SNKDB).
 * Copyright Alexander Prack, 2004.
 * 
 *  SNKDB is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   SNKDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SNKDB; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package sernet.hui.common.connect;

import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;

/**
 * @author prack
 */
public interface IEntityChangedListener {
	public void dependencyChanged(IMLPropertyType type, IMLPropertyOption opt);
	public void selectionChanged(IMLPropertyType type, IMLPropertyOption opt);
	public void propertyChanged(PropertyChangedEvent event);
}
