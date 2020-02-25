/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
 ******************************************************************************/
package sernet.verinice.model.report;

import java.io.Serializable;

public class FileMetaData implements Serializable {
    private static final long serialVersionUID = -6227153791012989625L;
    private String filename;
    private String checkSum;

    public FileMetaData(String filename, String checksum) {
        this.filename = filename;
        this.checkSum = checksum;
    }

    public String getFilename() {
        return filename;
    }

    public String getChecksum() {
        return checkSum;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + ((checkSum == null) ? 0 : checkSum.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileMetaData other = (FileMetaData) obj;
        if (filename == null) {
            if (other.filename != null) {
                return false;
            }
        } else if (!filename.equals(other.filename)) {
            return false;
        }
        if (checkSum == null) {
            if (other.checkSum != null) {
                return false;
            }
        } else if (!checkSum.equals(other.checkSum)) {
            return false;
        }

        return true;
    }
}
