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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.updatenews.IUpdateNewsService;
import sernet.verinice.model.updateNews.UpdateNewsException;
import sernet.verinice.model.updateNews.UpdateNewsMessageEntry;

/**
 * Service to provide functionality to parse a json-message hosted on a server,
 * that provides information about an available software-update for verinice.
 * Messages on a server has to look like this:
 * 
 * <pre>
 * <code>
 * {
 *    "version" : "1.13.0",
 *    "message" : "&lt;h1> Update News Headline &lt;/h1>Some text
 *              that describes the Update and informs &lt;p> the user&lt;/p>",
 *    "message_de" : "&lt;h1> Update News Überschrift &lt;/h1>Text
 *              der das Update beschreibt und den 
 *              &lt;p>Benutzer informiert&lt;/p>",
 *    "updatesite" : "http://path_to/updateSite"          
 *           
 * }
 * </code>
 * </pre>
 * 
 * The html within the message will be interpreted by an instance of
 * org.eclipse.swt.browser.Browser, javascript is turned off.
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class UpdateNewsService implements IUpdateNewsService {

    private static final Logger LOG = Logger.getLogger(UpdateNewsService.class);

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // valid for one session to cache the entry
    private UpdateNewsMessageEntry sessionNewsEntry;

    /*
     * configured via clients preference-store, reference:
     * 
     * sernet.gs.ui.rcp.main.preferences.
     * PreferenceConstants.STANDALONE_UPDATENEWS_URL
     * 
     * To replace / change it manually add the following line to the end of the
     * file verinice.ini:
     * 
     * -Dstandalone_updatenews_url=http://url.of/your/choice.json
     */
    private String newsLocation;

    /**
     * compares version string configured in applications oc.product to the one
     * that is configured in the news message (formatted as json) using an
     * instance of the {@link NumericStringComparator}
     * 
     * ncs.compare(installedVersion, availableVersion) == 1
     * 
     * means, that installedVersion is smaller than availableVersion
     * 
     */
    @Override
    public boolean isUpdateNecessary(String installedVersion) throws UpdateNewsException {
        if (this.newsLocation == null) {
            return false;
        }
        NumericStringComparator ncs = new NumericStringComparator();
        String availableVersion = getNewsFromRepository(this.newsLocation).getVersion();
        LOG.debug("version string from news-repo:\t" + availableVersion);
        final Pattern p = Pattern.compile(IUpdateNewsService.VERINICE_VERSION_PATTERN);
        final Matcher matcher = p.matcher(availableVersion);
        if (matcher.find()) {
            availableVersion = matcher.group();
        }

        int result = ncs.compare(availableVersion, installedVersion);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Compare " + installedVersion + "(installed) with " + availableVersion
                    + "(available) = " + result);
        }
        return result == 1;
    }

    private void loadNewsFromRepository(String url) {
        try {
            URL repositoryURL = new URL(url);
            URLConnection conn = repositoryURL.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            try (InputStream in = conn.getInputStream()) {
                this.sessionNewsEntry = parseNewsEntry(
                        IOUtils.toString(in, StandardCharsets.UTF_8.name()));
            }
        } catch (IOException e) {
            LOG.info("Can not read update news from URL:  " + url + " " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e);
            }
        } catch (Exception e) {
            LOG.error("Error while reading read update news.", e);
        }
    }

    private UpdateNewsMessageEntry parseNewsEntry(String newsEntry) throws UpdateNewsException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("JSON: " + newsEntry);
        }
        try {
            return gson.fromJson(newsEntry, UpdateNewsMessageEntry.class);
        } catch (JsonSyntaxException e) {
            LOG.error("Error parsing json", e);
            throw new UpdateNewsException("Error parsing json document", e);
        }
    }

    /**
     * loads the latest news entry from the configured server as plain text
     * (plain json) and returns it as a string
     */
    @Override
    public UpdateNewsMessageEntry getNewsFromRepository(String url) throws UpdateNewsException {
        this.newsLocation = url;
        if (this.sessionNewsEntry != null) {
            return this.sessionNewsEntry;
        }
        loadNewsFromRepository(url);
        return this.sessionNewsEntry;

    }
}
