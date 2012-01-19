package sernet.gs.reveng;

/**
 * MbRolleTxtId entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class MbRolleTxtId implements java.io.Serializable {

	// Fields

	private Integer rolImpId;
	private Integer rolId;
	private Short sprId;

	// Constructors

	/** default constructor */
	public MbRolleTxtId() {
	}

	/** full constructor */
	public MbRolleTxtId(Integer rolImpId, Integer rolId, Short sprId) {
		this.rolImpId = rolImpId;
		this.rolId = rolId;
		this.sprId = sprId;
	}

	// Property accessors

	public Integer getRolImpId() {
		return this.rolImpId;
	}

	public void setRolImpId(Integer rolImpId) {
		this.rolImpId = rolImpId;
	}

	public Integer getRolId() {
		return this.rolId;
	}

	public void setRolId(Integer rolId) {
		this.rolId = rolId;
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
		if (!(other instanceof MbRolleTxtId))
			return false;
		MbRolleTxtId castOther = (MbRolleTxtId) other;

		return ((this.getRolImpId() == castOther.getRolImpId()) || (this
				.getRolImpId() != null
				&& castOther.getRolImpId() != null && this.getRolImpId()
				.equals(castOther.getRolImpId())))
				&& ((this.getRolId() == castOther.getRolId()) || (this
						.getRolId() != null
						&& castOther.getRolId() != null && this.getRolId()
						.equals(castOther.getRolId())))
				&& ((this.getSprId() == castOther.getSprId()) || (this
						.getSprId() != null
						&& castOther.getSprId() != null && this.getSprId()
						.equals(castOther.getSprId())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getRolImpId() == null ? 0 : this.getRolImpId().hashCode());
		result = 37 * result
				+ (getRolId() == null ? 0 : this.getRolId().hashCode());
		result = 37 * result
				+ (getSprId() == null ? 0 : this.getSprId().hashCode());
		return result;
	}

}