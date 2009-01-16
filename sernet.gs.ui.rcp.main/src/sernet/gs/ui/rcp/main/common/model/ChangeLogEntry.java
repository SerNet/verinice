package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.hibernate.id.GUIDGenerator;

/**
 * Transaction log to log modifications to database items.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeLogEntry {

	public  static final int UPDATE = 0;
	public  static final int INSERT = 1;
	public  static final int DELETE = 2;;
	
	private Integer dbId;
	
	private Integer elementId;
	private String elementClass;
	private Date changetime;
	private int change;
	private String stationId;
	
	/**
	 * Session ID to identify changes made by this particular client during its lifetime.
	 */
	public final static String  STATION_ID = UUID.randomUUID().toString();
	
	public String getStationId() {
		return stationId;
	}


	 ChangeLogEntry() {
		// default constructor for hibernate
	}

	public ChangeLogEntry(CnATreeElement element, int change) {
		elementId = element.getDbId();
		elementClass = element.getClass().getName();
		this.change = change;
		stationId = STATION_ID;
	}

	public String getElementClass() {
		return elementClass;
	}

	public Date getChangetime() {
		return changetime;
	}

	public Integer getElementId() {
		return elementId;
	}

	public void setElementId(Integer elementId) {
		this.elementId = elementId;
	}

	public int getChange() {
		return change;
	}

	public void setChange(int change) {
		this.change = change;
	}

	public void setElementClass(String elementClass) {
		this.elementClass = elementClass;
	}

	public void setChangetime(Date changetime) {
		this.changetime = changetime;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}


}
