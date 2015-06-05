package sernet.gs.reveng;

/**
 * MbZeiteinheitenTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbZeiteinheitenTxtId implements java.io.Serializable {

	// Fields

	private Integer zeiImpId;
	private Integer zeiId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MbZeiteinheitenTxtId() {
	}

	/** full constructor */
	public MbZeiteinheitenTxtId(Integer zeiImpId, Integer zeiId, Short sprId) {
		this.zeiImpId = zeiImpId;
		this.zeiId = zeiId;
		this.sprId = sprId;
	}

	// Property accessors

	public Integer getZeiImpId() {
		return this.zeiImpId;
	}

	public void setZeiImpId(Integer zeiImpId) {
		this.zeiImpId = zeiImpId;
	}

	public Integer getZeiId() {
		return this.zeiId;
	}

	public void setZeiId(Integer zeiId) {
		this.zeiId = zeiId;
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
		if (!(other instanceof MbZeiteinheitenTxtId))
			return false;
		MbZeiteinheitenTxtId castOther = (MbZeiteinheitenTxtId) other;

		return ((this.getZeiImpId() == castOther.getZeiImpId()) || (this
				.getZeiImpId() != null
				&& castOther.getZeiImpId() != null && this.getZeiImpId()
				.equals(castOther.getZeiImpId())))
				&& ((this.getZeiId() == castOther.getZeiId()) || (this
						.getZeiId() != null
						&& castOther.getZeiId() != null && this.getZeiId()
						.equals(castOther.getZeiId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getZeiImpId() == null ? 0 : this.getZeiImpId().hashCode());
		result = 37 * result
				+ (getZeiId() == null ? 0 : this.getZeiId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}