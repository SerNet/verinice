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

import java.util.Set;

/**
 * Provides some additional information like the schema version which where
 * offered by the imported vna file.
 * 
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class VnaSchemaException extends VeriniceArchiveNotValidException {

    private static final long serialVersionUID = 1L;
    private String vnaSchemaVersion;
    private Set<String> offeredVnaSchemaVersions;

    public VnaSchemaException(String msg, String vnaSchemaVersion, Set<String> offeredVnaSchemaVersions) {
        super(msg);
        this.vnaSchemaVersion = vnaSchemaVersion;
        this.offeredVnaSchemaVersions = offeredVnaSchemaVersions;
    }

    public String getVnaSchemaVersion() {
        return vnaSchemaVersion;
    }

    public Set<String> getOfferedVnaSchemaVersions() {
        return offeredVnaSchemaVersions;
    }

}
