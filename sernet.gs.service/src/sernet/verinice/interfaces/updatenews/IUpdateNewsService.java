/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.updatenews;

import sernet.verinice.model.updateNews.UpdateNewsException;
import sernet.verinice.model.updateNews.UpdateNewsMessageEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface IUpdateNewsService {
    
    public static final String VERINICE_VERSION_PATTERN = 
            "\\b\\d{1}?[\\.]\\d{2}[\\.]\\d{1}\\b";
    
    String getCurrentInstalledVersion();
    
    UpdateNewsMessageEntry getNewsFromRepository(String newsRepository) throws UpdateNewsException;
    
    boolean isUpdateNecessary(String installedVersion) throws UpdateNewsException;
    
}
