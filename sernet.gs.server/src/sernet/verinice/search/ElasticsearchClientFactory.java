package sernet.verinice.search;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.DisposableBean;

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

    private Node node = null;
    private Client client = null;
    private Settings settings = null;

    public void init() {
        if (node == null || node.isClosed()) {
            // Build and start the node
            node = NodeBuilder.nodeBuilder().settings(buildNodeSettings()).node();
            // Get a client
            client = node.client();
            // Wait for Yellow status
            client
                .admin()
                .cluster()
                .prepareHealth()
                .setWaitForYellowStatus()
                .setTimeout(TimeValue.timeValueMinutes(1))
                .execute()
                .actionGet();
        }
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
        // Build settings
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
            .put("node.name", "elasticsearch-" + NetworkUtils.getLocalAddress().getHostName())
            .put("node.data", true)
            .put("cluster.name", "elasticsearch-cluster-" + NetworkUtils.getLocalAddress().getHostName())
            .put("index.store.type", "niofs")
            //.put("index.store.fs.memory.enabled", "true")
            .put("gateway.type", "local")
            .put("path.data", "./elasticsearch/data")
            .put("path.work", "./elasticsearch/work")
            .put("path.logs", "./elasticsearch/logs")
            .put("node.local", true);
        if (settings != null) {
            builder.put(settings);
        }
        return builder.build();
    }

}
