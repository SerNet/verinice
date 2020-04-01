package sernet.verinice.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.DisposableBean;

import sernet.verinice.interfaces.IDirectoryCreator;
import sernet.verinice.interfaces.IVeriniceConstants;

/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElasticsearchClientFactory implements DisposableBean {

    private static final Logger LOG = Logger.getLogger(ElasticsearchClientFactory.class);

    private static final String SERNET_VERINICE_SEARCH_ANALYSIS_JSON = "sernet/verinice/search/analysis";
    private static final String SEPERATOR_LANGUAGE = "_";
    private static final String SEPERATOR_EXTENSION = ".";
    private static final String JSON_EXTENSION = "json";

    private Node node = null;
    private Client client = null;
    private IDirectoryCreator directoryCreator;

    public void init() {
        if (node == null || node.isClosed()) {
            // Build and start the node
            node = NodeBuilder.nodeBuilder().settings(buildNodeSettings()).node();
            if (LOG.isDebugEnabled()) {
                for (Entry<String, String> entry : node.settings().getAsMap().entrySet()) {
                    LOG.debug("nodeEntry:\t <" + entry.getKey() + ", " + entry.getValue() + ">");
                }
            }
            // Get a client
            client = node.client();
            configure();
            Map<String, String> map = ImmutableSettings.builder().internalMap();
            for (Entry<String, String> e : map.entrySet()) {
                LOG.error("ES Setting:\t<" + e.getKey() + ", " + e.getValue() + ">");
            }
            // Wait for Yellow status
            client.admin().cluster().prepareHealth()
                    // yellow means, there are no replicas that could be used,
                    // since we are running on 1 node only, we are not going to
                    // have any replicas available, so yellow is ok for us
                    .setWaitForYellowStatus().setTimeout(TimeValue.timeValueMinutes(1)).execute()
                    .actionGet();
        }
    }

    private void configure() {
        if (!client.admin().indices().prepareExists(ISearchDao.INDEX_NAME).execute().actionGet()
                .isExists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating index " + ISearchDao.INDEX_NAME + "...");
            }
            try {
                Builder analysisConf = getAnylysisConf();
                if (LOG.isDebugEnabled()) {
                    Map<String, String> map = analysisConf.internalMap();
                    for (Entry<String, String> e : map.entrySet()) {
                        LOG.debug("ES Settings:\t<" + e.getKey() + ", " + e.getValue() + ">");
                    }
                }
                client.admin().indices().prepareCreate(ISearchDao.INDEX_NAME)
                        .setSettings(analysisConf).addMapping(ElementDao.TYPE_NAME, getMapping())
                        .execute().actionGet();
            } catch (IndexAlreadyExistsException e) {
                // https://github.com/elastic/elasticsearch/issues/8105
                LOG.warn("Index " + ISearchDao.INDEX_NAME
                        + " already existed when trying to create it, trying to use it.");
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Index " + ISearchDao.INDEX_NAME + " exists");
        }
    }

    private Builder getAnylysisConf() {
        return ImmutableSettings.settingsBuilder()
                .loadFromClasspath(getSearchAnalysisConfiguration());
    }

    private String getSearchAnalysisConfiguration() {
        return new StringBuilder().append(SERNET_VERINICE_SEARCH_ANALYSIS_JSON)
                .append(SEPERATOR_LANGUAGE).append(Locale.getDefault().getLanguage())
                .append(SEPERATOR_EXTENSION).append(JSON_EXTENSION).toString().toLowerCase();
    }

    private String getMapping() {
        InputStream in = this.getClass()
                .getResourceAsStream("/sernet/verinice/search/mapping.json");
        String mapping = null;
        try {
            mapping = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            LOG.error("Error while reading mapping file", e);
        }
        return mapping;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        if (node != null && !node.isClosed()) {
            node.close();
        }
    }

    public Client getClient() {
        return client;
    }

    protected Settings buildNodeSettings() {
        final int shards = 2;
        // Build settings
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
                .put("node.name", "elasticsearch-" + NetworkUtils.getLocalAddress().getHostName())
                .put("node.data", true)
                .put("cluster.name",
                        "elasticsearch-cluster-" + NetworkUtils.getLocalAddress().getHostName())
                // .put("index.store.fs.memory.enabled", "true")
                .put("gateway.type", "local").put("path.data", getDirectoryCreator().create("data"))
                .put("path.work", getDirectoryCreator().create("work"))
                .put("path.logs", getDirectoryCreator().create("logs")).put("node.local", true)
                .put("index.number_of_shards", shards)
                // disables (false to enable) swapping from memory to disk
                .put("bootstrap.mlockall", true)
                // prevents deleting all indexes of cluster by "accident"
                .put("action.disable_delete_all_indices", true)
                // EXPERIMENTAL, could cause perfomance issues, TEST
                .put("index.store.compress.stored", true)
                // EXPERIMENTAL, as above;
                .put("index.store.compress.tv", true)
                // disable the REST API since it has security vulnerabilities
                // and we don't use it anyway
                .put("http.enabled", false);

        setOSDependentFileSystem(builder);

        return builder.build();
    }

    public IDirectoryCreator getDirectoryCreator() {
        return directoryCreator;
    }

    public void setDirectoryCreator(IDirectoryCreator directoryCreator) {
        this.directoryCreator = directoryCreator;
    }

    /**
     * sets platform dependent fs for storing the index see
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules-store.html
     * this should be "mmapfs" on win64 and "simplefs" on win32 and "niofs" else
     **/
    private ImmutableSettings.Builder setOSDependentFileSystem(ImmutableSettings.Builder builder) {
        String fileSystem = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            if (System.getProperty(IVeriniceConstants.OS_ARCH).contains("64")) {
                fileSystem = "mmapfs";
            } else {
                fileSystem = "simplefs";
            }
        } else {
            fileSystem = "niofs";
        }
        builder.put("index.store.type", fileSystem);
        return builder;
    }

}
