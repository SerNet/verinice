/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels <bw@sernet.de>.
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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.sync;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.sernet.sync.sync.SyncRequest.SyncVnaSchemaVersion;

/**
 * Represents the schema version, which is supported by this verinice version
 * and to which other verinice archive schemes this is compatible.
 *
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public final class VnaSchemaVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String vnaSchemaVersion;

    final private Set<String> compatibleSchemaVersions;

    private VnaSchemaVersion(String vnaSchemaVersion, List<String> compatibleSchemaVersions) {

        this.vnaSchemaVersion = vnaSchemaVersion;
        this.compatibleSchemaVersions = new HashSet<>();

        if(compatibleSchemaVersions != null) {
            this.compatibleSchemaVersions.addAll(compatibleSchemaVersions);
        }

        // Schema is always compatible to itself, so put it into the list.
        this.compatibleSchemaVersions.add(vnaSchemaVersion);
    }

    public String getVnaSchemaVersion() {
        return vnaSchemaVersion;
    }

    public Set<String> getCompatibleSchemaVersions() {
        return compatibleSchemaVersions;
    }


    public static VnaSchemaVersion createVnaSchemaVersion(
            SyncVnaSchemaVersion syncVnaSchemaVersion) {
        if (syncVnaSchemaVersion == null) {
            throw new IllegalArgumentException("sync schema may not be null");
        }

        return new VnaSchemaVersion(syncVnaSchemaVersion.getVnaSchemaVersion(),
                syncVnaSchemaVersion.getCompatibleVersions());
    }

    public static VnaSchemaVersion createVnaSchemaVersion(String vnaSchemaVersion,
            List<String> compatibleSchemaVersions) {
        return new VnaSchemaVersion(vnaSchemaVersion, compatibleSchemaVersions);
    }
}
