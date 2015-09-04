package sernet.gs.reveng;

/**
 * MbZielobjSubtypTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjSubtypTxtId implements java.io.Serializable {

	// Fields

	private Integer zosImpId;
	private Integer zotId;
	private Integer zosId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MbZielobjSubtypTxtId() {
	}

	/** full constructor */
	public MbZielobjSubtypTxtId(Integer zosImpId, Integer zotId, Integer zosId,
			Short sprId) {
		this.zosImpId = zosImpId;
		this.zotId = zotId;
		this.zosId = zosId;
		this.sprId = sprId;
	}

	// Property accessors

	public Integer getZosImpId() {
		return this.zosImpId;
	}

	public void setZosImpId(Integer zosImpId) {
		this.zosImpId = zosImpId;
	}

	public Integer getZotId() {
		return this.zotId;
	}

	public void setZotId(Integer zotId) {
		this.zotId = zotId;
	}

	public Integer getZosId() {
		return this.zosId;
	}

	public void setZosId(Integer zosId) {
		this.zosId = zosId;
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
		if (!(other instanceof MbZielobjSubtypTxtId))
			return false;
		MbZielobjSubtypTxtId castOther = (MbZielobjSubtypTxtId) other;

		return ((this.getZosImpId() == castOther.getZosImpId()) || (this
				.getZosImpId() != null
				&& castOther.getZosImpId() != null && this.getZosImpId()
				.equals(castOther.getZosImpId())))
				&& ((this.getZotId() == castOther.getZotId()) || (this
						.getZotId() != null
						&& castOther.getZotId() != null && this.getZotId()
						.equals(castOther.getZotId())))
				&& ((this.getZosId() == castOther.getZosId()) || (this
						.getZosId() != null
						&& castOther.getZosId() != null && this.getZosId()
						.equals(castOther.getZosId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZosImpId() == null ? 0 : this.getZosImpId().hashCode());
		result = 37 * result
				+ (getZotId() == null ? 0 : this.getZotId().hashCode());
		result = 37 * result
				+ (getZosId() == null ? 0 : this.getZosId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}