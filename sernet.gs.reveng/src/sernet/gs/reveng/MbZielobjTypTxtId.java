package sernet.gs.reveng;

/**
 * MbZielobjTypTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjTypTxtId implements java.io.Serializable {

	// Fields

	private Integer zotImpId;
	private Integer zotId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MbZielobjTypTxtId() {
	}

	/** full constructor */
	public MbZielobjTypTxtId(Integer zotImpId, Integer zotId, Short sprId) {
		this.zotImpId = zotImpId;
		this.zotId = zotId;
		this.sprId = sprId;
	}

	// Property accessors

	public Integer getZotImpId() {
		return this.zotImpId;
	}

	public void setZotImpId(Integer zotImpId) {
		this.zotImpId = zotImpId;
	}

	public Integer getZotId() {
		return this.zotId;
	}

	public void setZotId(Integer zotId) {
		this.zotId = zotId;
	}

	public Short getSprId() {
		return this.sprId;
	}

	public void setSprId(Short sprId) {
		this.sprId = sprId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof MbZielobjTypTxtId))
			return false;
		MbZielobjTypTxtId castOther = (MbZielobjTypTxtId) other;

		return ((this.getZotImpId() == castOther.getZotImpId()) || (this
				.getZotImpId() != null
				&& castOther.getZotImpId() != null && this.getZotImpId()
				.equals(castOther.getZotImpId())))
				&& ((this.getZotId() == castOther.getZotId()) || (this
						.getZotId() != null
						&& castOther.getZotId() != null && this.getZotId()
						.equals(castOther.getZotId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZotImpId() == null ? 0 : this.getZotImpId().hashCode());
		result = 37 * result
				+ (getZotId() == null ? 0 : this.getZotId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}