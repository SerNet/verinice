/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.ILinkTableService;
import sernet.verinice.service.linktable.LinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.generator.GraphLinkedTableCreator;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

class LoadData {

    private String sourceId;
    private List<String> orgExtIds;
    private String vltFile;

    private ILinkTableConfiguration configuration;

    public List<List<String>> loadAndExecuteVLT(
            BiFunction<String, String, CnATreeElement> elementLoader) throws CommandException {
        ILinkTableService linkTableService = new LinkTableService();
        linkTableService.setLinkTableCreator(new GraphLinkedTableCreator());
        String vltPath = this.getClass().getResource(getVltFile()).getPath();
        configuration = VeriniceLinkTableIO.readLinkTableConfiguration(vltPath);
        writeScopeAndExtIdToLinkTableConfiguration(elementLoader);
        return linkTableService.createTable(configuration);
    }

    private void writeScopeAndExtIdToLinkTableConfiguration(
            BiFunction<String, String, CnATreeElement> elementLoader) throws CommandException {

        LinkTableConfiguration changedConfiguration = cloneConfiguration(configuration);
        for (String extId : getOrgExtIds()) {
            CnATreeElement org = elementLoader.apply(getSourceId(), extId);
            changedConfiguration.addScopeId(org.getScopeId());
        }

        configuration = changedConfiguration;
    }

    private LinkTableConfiguration cloneConfiguration(ILinkTableConfiguration configuration) {
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        builder.setColumnPathes(configuration.getColumnPaths())
                .setLinkTypeIds(configuration.getLinkTypeIds());
        if (configuration.getScopeIdArray() != null) {
            builder.setScopeIds(new HashSet<>(Arrays.asList(configuration.getScopeIdArray())));
        }
        return builder.build();
    }

    public String getVltFile() {
        return vltFile;
    }

    public void setVltFile(String vltFile) {
        this.vltFile = vltFile;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public List<String> getOrgExtIds() {
        return orgExtIds;
    }

    public void setOrgExtIds(List<String> orgExtIds) {
        this.orgExtIds = orgExtIds;
    }

}