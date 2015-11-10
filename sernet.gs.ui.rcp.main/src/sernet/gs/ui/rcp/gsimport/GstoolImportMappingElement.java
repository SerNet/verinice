package sernet.gs.ui.rcp.gsimport;

import java.io.Serializable;

import sernet.hui.common.connect.ITypedElement;

@SuppressWarnings("serial")
public class GstoolImportMappingElement implements Serializable, Comparable<GstoolImportMappingElement>, ITypedElement {

    public static final String TYPE_ID = "gstool_mapping";

    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public int compareTo(GstoolImportMappingElement o) {
//        return key.compareTo(o.getKey());

        int rc = 0;
        if (o == null) {
            rc = -1;
        } else {
            rc = this.key.compareToIgnoreCase(o.getKey());
        }
        return rc;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    public GstoolImportMappingElement(String key, String value) {
        super();
        this.key = key;
        this.value = value;
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
        GstoolImportMappingElement other = (GstoolImportMappingElement) obj;

        if (key == null) {
            if (other.getKey() != null) {
                return false;
            }
        } else if (key.compareTo(other.getKey()) != 0) {
            return false;
        }

        if (value == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (value.compareTo(other.getValue()) != 0) {
            return false;
        }
        return true;

    }


    @Override
    public String toString() {
        String string = key + "=" + value;
        return string;
    }

}
