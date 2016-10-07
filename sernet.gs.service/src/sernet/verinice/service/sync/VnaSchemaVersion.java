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
 * Represents the vna schema version.
 *
 * <p>
 * There is a verinice schema version itself, which is an arbitrary string, but
 * has to start with the prefix <strong><code>vna-</code></strong>.
 * </p>
 *
 * <p>
 * The second part is a list which contains version strings to which this vna
 * schema version is compatible with.
 * </p>
 *
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public final class VnaSchemaVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String vnaSchemaVersion;

    private final Set<String> compatibleSchemaVersions;

    private final String PREFIX = "vna-";

    private VnaSchemaVersion(String vnaSchemaVersion, List<String> compatibleSchemaVersions) {

        validateVersionString(vnaSchemaVersion);
        validateVersionStrings(compatibleSchemaVersions);

        this.vnaSchemaVersion = vnaSchemaVersion;
        this.compatibleSchemaVersions = new HashSet<>();

        if (compatibleSchemaVersions != null) {
            this.compatibleSchemaVersions.addAll(compatibleSchemaVersions);
        }

        // Schema is always compatible to itself, so put it into the list.
        this.compatibleSchemaVersions.add(vnaSchemaVersion);
    }

    private void validateVersionStrings(List<String> compatibleSchemaVersions) {
        for (String version : compatibleSchemaVersions) {
            validateVersionString(version);
        }
    }

    private void validateVersionString(String vnaSchemaVersion2) {
        if (vnaSchemaVersion2 == null) {
            throw new IllegalArgumentException("vna schema string may not null");
        }

        if (!vnaSchemaVersion2.startsWith(PREFIX)) {
            throw new IllegalArgumentException("vna schema does not start with: " + PREFIX);
        }

    }

    public String getVnaSchemaVersion() {
        return vnaSchemaVersion;
    }

    public Set<String> getCompatibleSchemaVersions() {
        return compatibleSchemaVersions;
    }

    /**
     * Creates a verinice schema version which can be validate by
     * {@link VnaSchemaChecker}.
     *
     * @param syncVnaSchemaVersion
     *            Represents this class JAXB serialized.
     *
     */
    public static VnaSchemaVersion createVnaSchemaVersion(SyncVnaSchemaVersion syncVnaSchemaVersion) {
        if (syncVnaSchemaVersion == null) {
            throw new IllegalArgumentException("sync schema may not be null");
        }

        return new VnaSchemaVersion(syncVnaSchemaVersion.getVnaSchemaVersion(),
                syncVnaSchemaVersion.getCompatibleVersions());
    }

    /**
     * * Creates a verinice schema version which can be validate by
     * {@link VnaSchemaChecker}.
     *
     *
     * @param vnaSchemaVersion
     *            Must start with "vna-" and represent the schema version of a
     *            verinice xml import/export.
     * @param compatibleSchemaVersions
     *            A list with versions the first param is compatible with. Every
     *            entry must also start with "vna-".
     */
    public static VnaSchemaVersion createVnaSchemaVersion(String vnaSchemaVersion,
            List<String> compatibleSchemaVersions) {
        return new VnaSchemaVersion(vnaSchemaVersion, compatibleSchemaVersions);
    }
}
