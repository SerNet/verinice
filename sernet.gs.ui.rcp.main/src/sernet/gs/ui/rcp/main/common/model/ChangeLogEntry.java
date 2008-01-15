package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Transaction log to log modifications to database items.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeLogEntry {

	private Integer dbid;
	private String elementClass;
	private Date timestamp;
	private Integer elementId;

	public ChangeLogEntry(CnATreeElement element) {
		elementId = element.getDbId();
		elementClass = element.getClass().getName();
	}
	
	
	
	public ChangeLogEntry(Serializable identifier, Object element) {
		try {
			dbid = (Integer) identifier;
			elementClass = element.getClass().getName();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}



	public String getElementClass() {
		return elementClass;
	}

	public Integer getDbid() {
		return dbid;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Integer getElementid() {
		return elementId;
	}

	public void setElementid(Integer elementid) {
		this.elementId = elementid;
	}

	
	

}
