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

import java.net.URL;
import java.util.Locale;

import sernet.verinice.interfaces.updatenews.IUpdateNewsService;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class UpdateNewsServiceDummy implements IUpdateNewsService {

    private static final String DUMMY_VERSION_NUMBER = "99.0.0";
    private static final String DUMMY_NEWS_MESSAGE = "Dummy-Message";
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentInstalledVersion()
     */
    @Override
    public String getCurrentInstalledVersion() {
        return DUMMY_VERSION_NUMBER;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentNewsMessage()
     */
    @Override
    public String getCurrentNewsMessage(Locale locale) {
        return DUMMY_NEWS_MESSAGE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentNewsVersion()
     */
    @Override
    public String getCurrentNewsVersion() {
        return DUMMY_VERSION_NUMBER;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getLatestNewsFromRepository()
     */
    @Override
    public String getLatestNewsFromRepository() {
        return DUMMY_NEWS_MESSAGE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#isUpdateNecessary()
     */
    @Override
    public boolean isUpdateNecessary() {
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getUpdateSite()
     */
    @Override
    public URL getUpdateSite() {
        return null;
    }

}
