package sernet.gs.reveng;

/**
 * MUmsetzStatTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MUmsetzStatTxtId implements java.io.Serializable {

	// Fields

	private Short ustId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MUmsetzStatTxtId() {
	}

	/** full constructor */
	public MUmsetzStatTxtId(Short ustId, Short sprId) {
		this.ustId = ustId;
		this.sprId = sprId;
	}

	// Property accessors

	public Short getUstId() {
		return this.ustId;
	}

	public void setUstId(Short ustId) {
		this.ustId = ustId;
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
		if (!(other instanceof MUmsetzStatTxtId))
			return false;
		MUmsetzStatTxtId castOther = (MUmsetzStatTxtId) other;

		return ((this.getUstId() == castOther.getUstId()) || (this.getUstId() != null
				&& castOther.getUstId() != null && this.getUstId().equals(
				castOther.getUstId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getUstId() == null ? 0 : this.getUstId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}