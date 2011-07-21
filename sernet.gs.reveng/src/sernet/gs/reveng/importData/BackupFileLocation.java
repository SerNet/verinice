package sernet.gs.reveng.importData;

import java.io.Serializable;

public class BackupFileLocation implements Serializable {
	private String mdfLogicalName;
	private String mdfFileName;
	private String ldfLogicalName;
	private String ldfFileName;
	
	public String getMdfLogicalName() {
		return mdfLogicalName;
	}
	public void setMdfLogicalName(String mdfLogicalName) {
		this.mdfLogicalName = mdfLogicalName;
	}
	public String getMdfFileName() {
		return mdfFileName;
	}
	public void setMdfFileName(String mdfFileName) {
		this.mdfFileName = mdfFileName;
	}
	public String getLdfLogicalName() {
		return ldfLogicalName;
	}
	public void setLdfLogicalName(String ldfLogicalName) {
		this.ldfLogicalName = ldfLogicalName;
	}
	public String getLdfFileName() {
		return ldfFileName;
	}
	public void setLdfFileName(String ldfFileName) {
		this.ldfFileName = ldfFileName;
	}
	
	
}
