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
package sernet.verinice.service;

import sernet.verinice.interfaces.updatenews.IUpdateNewsService;
import sernet.verinice.model.updateNews.UpdateNewsMessageEntry;

/**
 * this implementation is used when running verinice.PRO.
 * verinice.PRO has a different update-strategy than the standalone
 * version, and there is (currently) no need for a serious implementation
 * of this functionality. 
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class UpdateNewsServiceDummy implements IUpdateNewsService {

    private static final String DUMMY_VERSION_NUMBER = "99.0.0";
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentInstalledVersion()
     */
    @Override
    public String getCurrentInstalledVersion() {
        return DUMMY_VERSION_NUMBER;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#isUpdateNecessary()
     */
    @Override
    public boolean isUpdateNecessary(String installedVersion) {
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getNewsFromRepository(java.lang.String)
     */
    @Override
    public UpdateNewsMessageEntry getNewsFromRepository(String newsRepository) {
        return null;
    }

}
