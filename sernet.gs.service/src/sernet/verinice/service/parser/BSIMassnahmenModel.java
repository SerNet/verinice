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
package sernet.verinice.service.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.scraper.GSScraper;
import sernet.gs.scraper.IGSSource;
import sernet.gs.scraper.PatternGSHB2005_2006;
import sernet.gs.scraper.PatternGSHB2009;
import sernet.gs.scraper.URLGSSource;
import sernet.gs.scraper.ZIPGSSource;
import sernet.gs.service.GSServiceException;
import sernet.gs.service.VeriniceCharset;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBSIConfig;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.ILayoutConfig;
import sernet.verinice.interfaces.IProgress;
import sernet.verinice.model.bsi.BausteinUmsetzung;

public class BSIMassnahmenModel {

    private static final Logger LOG = Logger.getLogger(BSIMassnahmenModel.class);

    private static List<Baustein> cache;

    private static GSScraper scrape;

    private static String previouslyReadFile = ""; //$NON-NLS-1$

    private IBSIConfig config;

    private String language = "";

    private String encoding = VeriniceCharset.CHARSET_UTF_8.name();

    private ILayoutConfig layoutConfig;

    public BSIMassnahmenModel() {
    }

    public BSIMassnahmenModel(IBSIConfig config) {
        this.config = config;
    }

    /**
     * Loads the
     * 
     * @param mon
     * @return
     * @throws GSServiceException
     * @throws IOException
     */
    public synchronized List<Baustein> loadBausteine(IProgress mon)
            throws GSServiceException, IOException {
        if (config instanceof BSIConfigurationRemoteSource) {
            LOG.debug(Messages.BSIMassnahmenModel_0);
            return loadBausteineRemote();
        }
        final int maxTaskSteps = 5;

        String gsPath = config.getGsPath();
        boolean fromZipFile = config.isFromZipFile();
        IGSSource gsSource = null;
        String cacheDir = config.getCacheDir();

        if (LOG.isInfoEnabled()) {
            LOG.info("Loading Grundschutzkatalog, path: " + gsPath);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache dir is: " + cacheDir);
            LOG.debug("Grundschutzkatalog isFromZipFile=" + fromZipFile);
        }

        // did user really change the path to file?
        if (!(previouslyReadFile.equals(gsPath))) {
            previouslyReadFile = gsPath;

            try {
                if (fromZipFile) {
                    gsSource = new ZIPGSSource(gsPath);
                } else {
                    gsSource = new URLGSSource(gsPath);
                }
            } catch (IOException e) {
                LOG.error(Messages.BSIMassnahmenModel_9 + gsPath + Messages.BSIMassnahmenModel_2);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("stacktrace: ", e); //$NON-NLS-1$
                }
                return null;
            }

            if (gsSource.getVintage().equals(IGSSource.VINTAGE_2009)) {
                scrape = new GSScraper(gsSource, new PatternGSHB2009());
            } else {
                scrape = new GSScraper(gsSource, new PatternGSHB2005_2006());
            }

            scrape.setCacheDir(cacheDir); // $NON-NLS-1$

            Logger.getLogger(BSIMassnahmenModel.class)
                    .debug("Setting GS-Cache to " + scrape.getCacheDir()); //$NON-NLS-1$
            mon.beginTask(Messages.BSIMassnahmenModel_3, maxTaskSteps);
            List<Baustein> alleBst = new ArrayList<>();

            processBausteinLayer(mon, alleBst, GSScraper.CATALOG_MODULE_B01, 0);
            processBausteinLayer(mon, alleBst, GSScraper.CATALOG_MODULE_B02, 1);
            processBausteinLayer(mon, alleBst, GSScraper.CATALOG_MODULE_B03, 2);
            processBausteinLayer(mon, alleBst, GSScraper.CATALOG_MODULE_B04, 3);
            processBausteinLayer(mon, alleBst, GSScraper.CATALOG_MODULE_B05, 4);
            processBausteinLayer(mon, alleBst, GSScraper.CATALOG_MODULE_B00, 5);

            this.language = scrape.getLanguage();

            mon.done();
            Logger.getLogger(BSIMassnahmenModel.class).debug(Messages.BSIMassnahmenModel_4);

        }
        return cache;
    }

    private void processBausteinLayer(IProgress mon, List<Baustein> alleBst, String layer,
            int layerDescription) throws GSServiceException, IOException {
        mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[layerDescription]);
        alleBst.addAll(scrapeBausteine(layer));
        mon.worked(1);
    }

    private List<Baustein> loadBausteineRemote() throws GSServiceException {
        // use remote source
        try {
            LoadBausteine command = new LoadBausteine();
            command = getCommandServive().executeCommand(command);
            return command.getBausteine();
        } catch (Exception e) {
            LOG.error("Error while loading module.", e);
            throw new GSServiceException(e);
        }
    }

    public InputStream getBaustein(String url, String stand) throws GSServiceException {

        if (config instanceof BSIConfigurationRemoteSource) {
            return getBausteinFromServer(url, stand);
        }

        InputStream bausteinText = null;
        try {
            bausteinText = scrape.getBausteinText(url, stand);
        } catch (Exception e) {
            // ignore
        }
        return bausteinText;
    }

    private InputStream getBausteinFromServer(String url, String stand) throws GSServiceException {
        GetBausteinText command = new GetBausteinText(url, stand);
        try {
            command = getCommandServive().executeCommand(command);
            String bausteinText = command.getBausteinText();
            return stringToStream(bausteinText, command.getEncoding());
        } catch (CommandException | UnsupportedEncodingException e) {
            throw new GSServiceException(e.getCause());
        }
    }

    private InputStream stringToStream(String text, String encoding)
            throws UnsupportedEncodingException {
        return new ByteArrayInputStream(text.getBytes(encoding));
    }

    public InputStream getMassnahme(String url, String stand) throws GSServiceException {

        if (config instanceof BSIConfigurationRemoteSource) {
            return getMassnahmeFromServer(url, stand);
        }

        InputStream massnahme = null;
        try {
            massnahme = scrape.getMassnahme(url, stand);
        } catch (Exception e) {
            // ignore
        }
        return massnahme;
    }

    public String getMassnahmeHtml(String url, String stand) throws GSServiceException {
        final int utf8NoBreakSpace = 160;
        try {
            InputStreamReader read = new InputStreamReader(getMassnahme(url, stand),
                    VeriniceCharset.CHARSET_UTF_8); // $NON-NLS-1$
            BufferedReader buffRead = new BufferedReader(read);
            StringBuilder b = new StringBuilder();
            String line;
            boolean skip = false;
            boolean skipComplete = false;

            String cssFile = getLayoutConfig().getCssFilePath();

            while ((line = buffRead.readLine()) != null) {
                if (!skipComplete) {
                    if (line.matches(".*div.*id=\"menuoben\".*") //$NON-NLS-1$
                            || line.matches(".*div.*class=\"standort\".*")) { //$NON-NLS-1$
                        skip = true;
                    } else if (line.matches(".*div.*id=\"content\".*")) { //$NON-NLS-1$
                        skip = false;
                        skipComplete = true;
                    }
                }

                // we strip away images et al to keep just the information we
                // need:
                line = line.replace("../../media/style/css/screen.css", cssFile); //$NON-NLS-1$
                line = line.replace("../../../screen.css", cssFile); //$NON-NLS-1$
                line = line.replace("../../screen.css", cssFile); //$NON-NLS-1$
                line = line.replace("../screen.css", cssFile); //$NON-NLS-1$
                line = line.replaceAll("<a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
                line = line.replaceAll("</a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
                line = line.replaceAll("<img.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
                line = line.replace((char) utf8NoBreakSpace, ' ');
                line = line.replace("<hr.*?>", "");
                line = line.replace("<hr class=\"linievorcopy\" style=\"width: 730px;\">", "");

                if (!skip) {
                    b.append(line);
                }
            }
            buffRead.close();
            read.close();
            return b.toString();
        } catch (Exception e) {
            LOG.error(Messages.BSIMassnahmenModel_7, e);
            throw new GSServiceException(Messages.BSIMassnahmenModel_8, e);
        }
    }

    private InputStream getMassnahmeFromServer(String url, String stand) throws GSServiceException {
        try {
            GetMassnahmeText command = new GetMassnahmeText(url, stand);
            command = getCommandServive().executeCommand(command);
            String text = command.getText();
            encoding = command.getEncoding();
            return stringToStream(text, getEncoding());
        } catch (CommandException | UnsupportedEncodingException e) {
            throw new GSServiceException(e.getCause());
        }
    }

    private List<Baustein> scrapeBausteine(String schicht) throws GSServiceException, IOException {
        List<Baustein> bausteine = scrape.getBausteine(schicht);
        for (Baustein baustein : bausteine) {
            List<Massnahme> massnahmen = scrape.getMassnahmen(baustein.getUrl());
            baustein.setMassnahmen(massnahmen);

            List<Gefaehrdung> gefaehrdungen = scrape.getGefaehrdungen(baustein.getUrl());
            baustein.setGefaehrdungen(gefaehrdungen);
        }
        return bausteine;
    }

    public InputStream getGefaehrdung(String url, String stand) throws GSServiceException {

        if (config instanceof BSIConfigurationRemoteSource) {
            return getGefaehrdungFromServer(url, stand);
        }

        InputStream gefaehrdung = null;
        try {
            gefaehrdung = scrape.getGefaehrdung(url, stand);
        } catch (GSServiceException e) {
            LOG.error("Error while getting gefaehrdung", e);

        }
        return gefaehrdung;
    }

    private InputStream getGefaehrdungFromServer(String url, String stand)
            throws GSServiceException {
        try {
            GetGefaehrdungText command = new GetGefaehrdungText(url, stand);
            command = getCommandServive().executeCommand(command);
            String text = command.getText();
            encoding = command.getEncoding();
            return stringToStream(text, getEncoding());
        } catch (CommandException | UnsupportedEncodingException e) {
            throw new GSServiceException(e.getCause());
        }
    }

    /**
     * Discards already loaded data.
     */
    private void flushCache() {
        if (scrape != null) {
            scrape.flushCache();
        }
    }

    /**
     * Changes the {@link IBSIConfig} instance that is used for this model.
     * 
     * <p>
     * Note: Changing the configuration object may make loading the catalogues
     * from a different location. For this reason the method has the side effect
     * of flushing already loaded data.
     * </p>
     * 
     * @param config
     */
    public void setBSIConfig(IBSIConfig config) {
        flushCache();

        this.config = config;
    }

    public IBSIConfig getBSIConfig() {
        return config;
    }

    public ILayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(ILayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }

    public String getEncoding() {
        if (scrape != null) {
            return scrape.getPatterns().getEncoding();
        }
        if (this.encoding != null) {
            return encoding;
        }
        return "iso-8859-1"; //$NON-NLS-1$
    }

    public String getLanguage() {
        return language;
    }

    private ICommandService getCommandServive() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
